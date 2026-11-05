package com.randomthoughtsever.plainly.data

import androidx.compose.runtime.mutableStateOf
import android.content.Context
import android.content.SharedPreferences
import com.randomthoughtsever.plainly.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

/**
 * Plainly's community, on Firebase (Cloud Firestore + anonymous sign-in), spoken to through Google's REST APIs.
 * Nobody makes an account: the app signs in anonymously in the background and gets a random name and emoji, which
 * people can change. Posts, comments, likes, poll votes and the streak leaderboard live in Firestore; the security
 * rules in /firebase/firestore.rules decide what each person may write (only their own posts, one like and one vote
 * each, counters that can't be faked). Reporting goes to a reports collection the owner reviews; blocking is on the phone.
 *
 * Switched on by community.properties (projectId, apiKey). Without it, the Community tab says it's opening soon.
 */
object Community {
    data class Post(val id: String, val uid: String, val name: String, val avatar: String, val text: String, val topic: String, val guide: String,
                    val createdAt: Long, val likes: Int, val comments: Int)
    data class Comment(val id: String, val uid: String, val name: String, val avatar: String, val text: String, val createdAt: Long)
    data class Leader(val uid: String, val name: String, val avatar: String, val streak: Int, val reads: Int)

    // Where to talk to. Tests point these at the Firebase emulators.
    var projectId = BuildConfig.COMMUNITY_PROJECT
    var apiKey = BuildConfig.COMMUNITY_KEY
    var authBase = "https://identitytoolkit.googleapis.com"
    var tokenBase = "https://securetoken.googleapis.com"
    var firestoreBase = "https://firestore.googleapis.com"
    val enabled get() = projectId.isNotBlank() && apiKey.isNotBlank()

    private lateinit var prefs: SharedPreferences
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val authLock = Mutex()
    private var idToken: String? = null
    private var expiresAt = 0L

    fun init(context: Context) { prefs = context.getSharedPreferences("community", Context.MODE_PRIVATE); joinedBox.value = prefs.getBoolean("joined", false) }

    private val joinedBox = mutableStateOf(false)
    /** Whether the reader has joined (on the phone; joining needs no server). Screens redraw when it changes. */
    var joinedState: Boolean
        get() = joinedBox.value
        set(v) { joinedBox.value = v; if (::prefs.isInitialized) prefs.edit().putBoolean("joined", v).apply() }
    var welcomed: Boolean
        get() = ::prefs.isInitialized && prefs.getBoolean("welcomed", false)
        set(v) { prefs.edit().putBoolean("welcomed", v).apply() }
    /** First-steps progress: posted at least once; joined a guide's discussion. */
    val posted get() = ::prefs.isInitialized && prefs.getBoolean("posted", false)
    val discussed get() = ::prefs.isInitialized && prefs.getBoolean("discussed", false)
    val uid: String? get() = if (::prefs.isInitialized) prefs.getString("uid", null) else null

    private val docs get() = "$firestoreBase/v1/projects/$projectId/databases/(default)/documents"
    private val docName get() = "projects/$projectId/databases/(default)/documents"

    // ---------- sign-in ----------

    /** Signs in anonymously once (kept on the phone) and refreshes the short-lived token when needed. */
    private suspend fun token(): String = authLock.withLock {
        idToken?.let { if (System.currentTimeMillis() < expiresAt - 60_000) return it }
        val refresh = prefs.getString("refresh", null)
        val o = if (refresh != null) runCatching {
            JSONObject(http("POST", "$tokenBase/v1/token?key=$apiKey", "grant_type=refresh_token&refresh_token=$refresh", form = true))
                .let { JSONObject().put("idToken", it.getString("id_token")).put("refreshToken", it.getString("refresh_token"))
                    .put("localId", it.getString("user_id")).put("expiresIn", it.getString("expires_in")) }
        }.getOrNull() else null
        val auth = o ?: JSONObject(http("POST", "$authBase/v1/accounts:signUp?key=$apiKey", JSONObject().put("returnSecureToken", true).toString()))
        prefs.edit().putString("refresh", auth.getString("refreshToken")).putString("uid", auth.getString("localId")).apply()
        idToken = auth.getString("idToken"); expiresAt = System.currentTimeMillis() + auth.getString("expiresIn").toLong() * 1000
        idToken!!
    }

    // ---------- reading ----------

    suspend fun feed(limit: Int = 40): List<Post> = withContext(Dispatchers.IO) {
        query(null, "posts", "createdAt", true, limit).map(::post).filterVisible()
    }

    /** Posts about one guide (its discussion), newest first. */
    suspend fun discussion(guide: String): List<Post> = withContext(Dispatchers.IO) {
        query(null, "posts", null, true, 50, where = "guide" to guide).map(::post).sortedByDescending { it.createdAt }.filterVisible()
    }

    suspend fun comments(postId: String): List<Comment> = withContext(Dispatchers.IO) {
        query("posts/$postId", "comments", "createdAt", false, 100).map { d ->
            val f = d.fields()
            Comment(d.id(), f.str("uid"), f.str("name"), f.str("avatar"), f.str("text"), f.time("createdAt"))
        }.filter { it.uid !in blocked() }
    }

    suspend fun getPost(id: String): Post? = withContext(Dispatchers.IO) {
        runCatching { post(JSONObject(http("GET", "$docs/posts/$id", null, auth = token()))) }.getOrNull()
    }

    /** Which of these posts the reader has liked. */
    suspend fun liked(ids: List<String>): Set<String> = withContext(Dispatchers.IO) {
        val me = uid ?: return@withContext emptySet()
        if (ids.isEmpty()) return@withContext emptySet()
        val body = JSONObject().put("documents", JSONArray(ids.map { "$docName/posts/$it/likes/$me" }))
        val out = JSONArray("[" + http("POST", "$docs:batchGet", body.toString(), auth = token()).trim().removePrefix("[").removeSuffix("]") + "]")
        (0 until out.length()).mapNotNull { i -> out.getJSONObject(i).optJSONObject("found")?.getString("name")?.split("/")?.let { it[it.size - 3] } }.toSet()
    }

    suspend fun poll(quizId: String): IntArray = withContext(Dispatchers.IO) {
        val f = runCatching { JSONObject(http("GET", "$docs/polls/$quizId", null, auth = token())).fields() }.getOrNull()
        IntArray(4) { f?.int("o$it") ?: 0 }
    }

    suspend fun leaders(): List<Leader> = withContext(Dispatchers.IO) {
        query(null, "users", "streak", true, 20).map { d -> val f = d.fields(); Leader(d.id(), f.str("name"), f.str("avatar"), f.int("streak"), f.int("reads")) }
            .filter { it.uid !in blocked() }
    }

    // ---------- writing ----------

    /** Why a text can't be posted, or null if it's fine. */
    fun problem(text: String): String? {
        val t = text.trim()
        return when {
            t.length < 2 -> "Write a little more."
            t.length > 500 -> "Keep it under 500 characters."
            Regex("(?i)(https?://|www\\.)").findAll(t).any { !it.value.contains("blogspot") } && !t.contains("randomthoughtsever.blogspot.com") ->
                "Links aren't allowed, except to guides on the blog."
            BAD.containsMatchIn(t) -> "Please keep it friendly. That word isn't allowed here."
            Regex("(?i)(whatsapp|telegram|dm me|call me|\\+?\\d[\\d\\s-]{9,})").containsMatchIn(t) -> "For everyone's safety, don't share contact details."
            else -> null
        }
    }

    suspend fun createPost(name: String, avatar: String, text: String, topic: String, guide: String = ""): Post = withContext(Dispatchers.IO) {
        problem(text)?.let { error(it) }
        val me = token().let { uid!! }
        val id = newId()
        val fields = JSONObject().put("uid", s(me)).put("name", s(name)).put("avatar", s(avatar)).put("text", s(text.trim())).put("topic", s(topic))
            .put("guide", s(guide)).put("likes", i(0)).put("comments", i(0))
        commit(JSONObject().put("update", JSONObject().put("name", "$docName/posts/$id").put("fields", fields))
            .put("currentDocument", JSONObject().put("exists", false))
            .put("updateTransforms", JSONArray().put(JSONObject().put("fieldPath", "createdAt").put("setToServerValue", "REQUEST_TIME"))))
        prefs.edit().putBoolean("posted", true).apply { if (guide.isNotBlank()) putBoolean("discussed", true) }.apply()
        Post(id, me, name, avatar, text.trim(), topic, guide, System.currentTimeMillis(), 0, 0)
    }

    suspend fun comment(postId: String, name: String, avatar: String, text: String): Unit = withContext(Dispatchers.IO) {
        problem(text)?.let { error(it) }
        repeat(3) { attempt ->
            val me = token().let { uid!! }
            val current = getPost(postId) ?: error("This post was removed.")
            val n = current.comments + 1
            val cid = "${me}_$n"
            val fields = JSONObject().put("uid", s(me)).put("name", s(name)).put("avatar", s(avatar)).put("text", s(text.trim()))
            val ok = runCatching {
                commit(JSONObject().put("update", JSONObject().put("name", "$docName/posts/$postId/comments/$cid").put("fields", fields))
                    .put("currentDocument", JSONObject().put("exists", false))
                    .put("updateTransforms", JSONArray().put(JSONObject().put("fieldPath", "createdAt").put("setToServerValue", "REQUEST_TIME"))),
                    increment("posts/$postId", "comments", 1))
            }.isSuccess
            if (ok) { prefs.edit().putBoolean("discussed", true).apply(); prefs.edit().putStringSet("myComments", prefs.getStringSet("myComments", emptySet())!! + "posts/$postId/comments/$cid").apply(); return@withContext }
            if (attempt == 2) error("Couldn't post your comment. Try again.")
        }
    }

    suspend fun like(postId: String, on: Boolean): Unit = withContext(Dispatchers.IO) {
        val me = token().let { uid!! }
        val doc = "$docName/posts/$postId/likes/$me"
        if (on) commit(JSONObject().put("update", JSONObject().put("name", doc).put("fields", JSONObject().put("at", i(1))))
            .put("currentDocument", JSONObject().put("exists", false)), increment("posts/$postId", "likes", 1))
        else commit(JSONObject().put("delete", doc).put("currentDocument", JSONObject().put("exists", true)), increment("posts/$postId", "likes", -1))
    }

    suspend fun deletePost(postId: String): Unit = withContext(Dispatchers.IO) { token(); commit(JSONObject().put("delete", "$docName/posts/$postId")) }

    suspend fun report(target: String, reason: String): Unit = withContext(Dispatchers.IO) {
        val me = token().let { uid!! }
        commit(JSONObject().put("update", JSONObject().put("name", "$docName/reports/${newId()}")
            .put("fields", JSONObject().put("uid", s(me)).put("target", s(target)).put("reason", s(reason.take(200)))))
            .put("updateTransforms", JSONArray().put(JSONObject().put("fieldPath", "createdAt").put("setToServerValue", "REQUEST_TIME"))))
        hide(target)
    }

    /** One vote per person per quiz; the total for that option goes up by one in the same write. Quiet if offline. */
    fun vote(quizId: String, option: Int) {
        if (!enabled || !::prefs.isInitialized) return
        io.launch { runCatching { voteNow(quizId, option) } }
    }

    suspend fun voteNow(quizId: String, option: Int): Unit = withContext(Dispatchers.IO) {
        val me = token().let { uid!! }
        commit(JSONObject().put("update", JSONObject().put("name", "$docName/polls/$quizId/votes/$me").put("fields", JSONObject().put("o", i(option))))
            .put("currentDocument", JSONObject().put("exists", false)), increment("polls/$quizId", "o$option", 1))
    }

    /** Puts the reader on the streak leaderboard (only if they've chosen to be on it). */
    fun publishStats(name: String, avatar: String, streak: Int, reads: Int) {
        if (!enabled || !::prefs.isInitialized || !onLeaderboard) return
        io.launch { runCatching { publishStatsNow(name, avatar, streak, reads) } }
    }

    suspend fun publishStatsNow(name: String, avatar: String, streak: Int, reads: Int): Unit = withContext(Dispatchers.IO) {
        val me = token().let { uid!! }
        commit(JSONObject().put("update", JSONObject().put("name", "$docName/users/$me")
            .put("fields", JSONObject().put("name", s(name)).put("avatar", s(avatar)).put("streak", i(streak.coerceIn(0, 3650))).put("reads", i(reads.coerceIn(0, 100000)))))
            .put("updateTransforms", JSONArray().put(JSONObject().put("fieldPath", "updatedAt").put("setToServerValue", "REQUEST_TIME"))))
    }

    /**
     * Deletes everything this person put in the community: their posts, their comments, their leaderboard row, and
     * their anonymous sign-in itself. Afterwards the app gets a new anonymous identity if they post again.
     */
    suspend fun deleteMe(): Unit = withContext(Dispatchers.IO) {
        val me = uid ?: return@withContext
        val t = token()
        val mine = query(null, "posts", null, true, 300, where = "uid" to me).map { it.getString("name") }
        val comments = prefs.getStringSet("myComments", emptySet())!!.map { "$docName/$it" }
        (mine + comments + "$docName/users/$me").chunked(400).forEach { batch -> commit(*batch.map { JSONObject().put("delete", it) }.toTypedArray()) }
        runCatching { http("POST", "$authBase/v1/accounts:delete?key=$apiKey", JSONObject().put("idToken", t).toString()) }
        prefs.edit().remove("myComments").apply()
        resetSession()
    }

    /** Tests only: forget the signed-in person, so the next request signs in as someone new. */
    internal fun resetSession() { idToken = null; expiresAt = 0; prefs.edit().remove("refresh").remove("uid").apply() }
    /** Tests only: send raw writes, to check the security rules refuse cheating. */
    internal suspend fun rawCommit(vararg writes: JSONObject) = withContext(Dispatchers.IO) { commit(*writes) }
    internal val documentRoot get() = docName

    var onLeaderboard: Boolean
        get() = ::prefs.isInitialized && prefs.getBoolean("leaderboard", true)
        set(v) { prefs.edit().putBoolean("leaderboard", v).apply() }
    var acceptedRules: Boolean
        get() = ::prefs.isInitialized && prefs.getBoolean("rules", false)
        set(v) { prefs.edit().putBoolean("rules", v).apply() }

    // ---------- blocking and hiding (on the phone) ----------

    fun block(uid: String) = prefs.edit().putStringSet("blocked", blocked() + uid).apply()
    fun unblockAll() = prefs.edit().remove("blocked").apply()
    fun blocked(): Set<String> = if (::prefs.isInitialized) prefs.getStringSet("blocked", emptySet())!!.toSet() else emptySet()
    private fun hide(target: String) = prefs.edit().putStringSet("hidden", hidden() + target).apply()
    private fun hidden(): Set<String> = prefs.getStringSet("hidden", emptySet())!!.toSet()
    private fun List<Post>.filterVisible() = filter { it.uid !in blocked() && "posts/${it.id}" !in hidden() }

    // ---------- Firestore REST plumbing ----------

    private fun increment(path: String, field: String, by: Int) = JSONObject().put("transform", JSONObject().put("document", "$docName/$path")
        .put("fieldTransforms", JSONArray().put(JSONObject().put("fieldPath", field).put("increment", i(by)))))

    private suspend fun commit(vararg writes: JSONObject) {
        http("POST", "$docs:commit", JSONObject().put("writes", JSONArray(writes.toList())).toString(), auth = token())
    }

    private suspend fun query(parent: String?, collection: String, orderBy: String?, desc: Boolean, limit: Int, where: Pair<String, String>? = null): List<JSONObject> {
        val q = JSONObject().put("from", JSONArray().put(JSONObject().put("collectionId", collection))).put("limit", limit)
        if (orderBy != null) q.put("orderBy", JSONArray().put(JSONObject().put("field", JSONObject().put("fieldPath", orderBy)).put("direction", if (desc) "DESCENDING" else "ASCENDING")))
        if (where != null) q.put("where", JSONObject().put("fieldFilter", JSONObject().put("field", JSONObject().put("fieldPath", where.first))
            .put("op", "EQUAL").put("value", s(where.second))))
        val url = if (parent == null) "$docs:runQuery" else "$docs/$parent:runQuery"
        val arr = JSONArray(http("POST", url, JSONObject().put("structuredQuery", q).toString(), auth = token()))
        return (0 until arr.length()).mapNotNull { arr.getJSONObject(it).optJSONObject("document") }
    }

    private fun post(d: JSONObject): Post { val f = d.fields()
        return Post(d.id(), f.str("uid"), f.str("name"), f.str("avatar"), f.str("text"), f.str("topic"), f.str("guide"), f.time("createdAt"), f.int("likes"), f.int("comments")) }

    private fun JSONObject.id() = getString("name").substringAfterLast('/')
    private fun JSONObject.fields() = optJSONObject("fields") ?: JSONObject()
    private fun JSONObject.str(k: String) = optJSONObject(k)?.optString("stringValue").orEmpty()
    private fun JSONObject.int(k: String) = optJSONObject(k)?.let { it.optString("integerValue").toIntOrNull() ?: it.optDouble("doubleValue", 0.0).toInt() } ?: 0
    private fun JSONObject.time(k: String) = optJSONObject(k)?.optString("timestampValue")?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() } ?: 0L
    private fun s(v: String) = JSONObject().put("stringValue", v)
    private fun i(v: Int) = JSONObject().put("integerValue", v.toString())
    private fun newId(): String { val a = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; return (1..20).map { a.random() }.joinToString("") }

    private fun http(method: String, url: String, body: String?, auth: String? = null, form: Boolean = false): String {
        val c = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method; connectTimeout = 12000; readTimeout = 20000; useCaches = false
            setRequestProperty("Content-Type", if (form) "application/x-www-form-urlencoded" else "application/json")
            auth?.let { setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) { doOutput = true; outputStream.use { it.write(body.toByteArray()) } }
        }
        val code = c.responseCode
        val text = (if (code in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) throw CommunityError(code, text.take(300))
        return text
    }

    class CommunityError(val code: Int, body: String) : Exception("Community request failed ($code): $body")

    /** Words that aren't allowed in posts and names (a short list of slurs and obscenities; the reports queue catches the rest). */
    private val BAD = Regex("(?i)\\b(fuck\\w*|shit\\w*|bitch\\w*|cunt\\w*|nigg\\w*|fag\\w*|retard\\w*|whore\\w*|slut\\w*|rape\\w*|kill yourself|kys)\\b")
}
