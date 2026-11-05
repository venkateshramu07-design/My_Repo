package com.randomthoughtsever.plainly.data

import android.content.Context
import android.content.SharedPreferences
import com.randomthoughtsever.plainly.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime

/**
 * The community without Firebase: it runs on the blog's own comments, which Blogger already stores and serves.
 *
 * Reading is native and needs no account: the public comment feeds (all recent comments, or one guide's comments) are
 * parsed into notes. Writing goes through Blogger's own comment form for that guide, opened in a Custom Tab, because
 * Blogger has no API for posting comments and its sign-in doesn't work inside an app's WebView. The app copies what the
 * reader wrote, they paste it and tap Publish, and when the comment shows up in the feed the app recognises it as theirs.
 *
 * The blog's comment settings apply: who may comment (now Google accounts), and whether the owner approves comments first.
 * General posts that aren't about one guide go to a "board" post or page, if community.properties names one (board=id).
 */
object BlogTalk {
    const val BLOG_ID = "6417357851361576411"
    var base = Blog.BASE
    var board = BuildConfig.COMMUNITY_BOARD

    data class Note(val id: String, val postId: String, val postUrl: String, val name: String, val uid: String, val photo: String,
                    val text: String, val published: Long, val url: String)
    data class Voice(val uid: String, val name: String, val photo: String, val notes: Int, val guides: Int, val last: Long)

    private var prefs: SharedPreferences? = null
    /** Called when one of the reader's comments first appears on the blog (the app gives XP for it). */
    var onPublished: ((Note) -> Unit)? = null
    fun init(context: Context) { prefs = context.getSharedPreferences("blogtalk", Context.MODE_PRIVATE) }

    /** Blogger's comment page for one post or page. Signing in and publishing happen there. */
    fun formUrl(postId: String) = "https://www.blogger.com/comment/fullpage/post/$BLOG_ID/$postId"

    @Volatile private var cache: Pair<Long, List<Note>>? = null

    /** The newest comments across the blog (spam and comments on non-guide pages left out), newest first. */
    suspend fun recent(force: Boolean = false): List<Note> = withContext(Dispatchers.IO) {
        cache?.takeIf { !force && System.currentTimeMillis() - it.first < 60_000 }?.let { return@withContext it.second }
        parse(get("$base/feeds/comments/default?alt=json&max-results=200")).visible()
            .also { cache = System.currentTimeMillis() to it; claim(it) }
    }

    /** One guide's (or the board's) comments, oldest first, like a conversation. */
    suspend fun on(postId: String): List<Note> = withContext(Dispatchers.IO) {
        parse(get("$base/feeds/$postId/comments/default?alt=json&max-results=500")).visible().sortedBy { it.published }.also { claim(it) }
    }

    /** The most active readers in the recent comments: a leaderboard everyone sees the same way. */
    fun voices(notes: List<Note>): List<Voice> = notes.groupBy { it.uid }.map { (uid, n) ->
        val latest = n.maxBy { it.published }
        Voice(uid, latest.name, latest.photo, n.size, n.map { it.postId }.distinct().size, latest.published)
    }.sortedWith(compareByDescending<Voice> { it.notes }.thenByDescending { it.last })

    // ---------- your comments ----------

    data class Pending(val postId: String, val text: String, val at: Long)

    /** Remembers what the reader is about to publish, so the app can recognise it when it appears. */
    fun expect(postId: String, text: String) {
        val p = prefs ?: return
        val list = pending().filter { System.currentTimeMillis() - it.at < 7 * 86_400_000L } + Pending(postId, text, System.currentTimeMillis())
        p.edit().putStringSet("pending", list.takeLast(20).map { "${it.postId}\u0001${it.at}\u0001${it.text}" }.toSet()).apply()
    }
    fun pending(): List<Pending> = prefs?.getStringSet("pending", emptySet()).orEmpty().mapNotNull { s ->
        s.split('\u0001', limit = 3).takeIf { it.size == 3 }?.let { Pending(it[0], it[2], it[1].toLongOrNull() ?: 0) }
    }.sortedBy { it.at }
    /** Waiting for this post: written and copied, not seen on the blog yet. */
    fun waitingOn(postId: String) = pending().lastOrNull { it.postId == postId }
    fun forget(w: Pending) = prefs?.let { p ->
        p.edit().putStringSet("pending", p.getStringSet("pending", emptySet()).orEmpty().filterNot { it.startsWith("${w.postId}\u0001${w.at}\u0001") }.toSet()).apply()
    }
    fun mine(n: Note) = prefs?.getStringSet("mineIds", emptySet()).orEmpty().contains(n.id)
    fun myUids(): Set<String> = prefs?.getStringSet("mineUids", emptySet()).orEmpty()

    /** Turns pending texts that now appear on the blog into "yours", and ticks the first steps. */
    private fun claim(notes: List<Note>) {
        val p = prefs ?: return
        val waiting = pending()
        if (waiting.isEmpty()) return
        val found = waiting.associateWith { w -> notes.firstOrNull { it.postId == w.postId && it.published >= w.at - 120_000 && same(it.text, w.text) } }
        val hits = found.filterValues { it != null }
        if (hits.isEmpty()) return
        p.edit().putStringSet("mineIds", p.getStringSet("mineIds", emptySet()).orEmpty() + hits.values.map { it!!.id })
            .putStringSet("mineUids", p.getStringSet("mineUids", emptySet()).orEmpty() + hits.values.map { it!!.uid })
            .putStringSet("pending", p.getStringSet("pending", emptySet()).orEmpty().filterNot { s -> hits.keys.any { s.startsWith("${it.postId}\u0001${it.at}\u0001") } }.toSet())
            .apply()
        hits.keys.forEach { Community.markPosted(onGuide = it.postId != board) }
        hits.values.forEach { n -> onPublished?.invoke(n!!) }
    }

    /** Same comment, allowing for the small changes Blogger or a paste make (spacing, punctuation, a trimmed end). */
    fun same(published: String, written: String): Boolean {
        val a = key(published); val b = key(written)
        if (a.isEmpty() || b.isEmpty()) return false
        val n = minOf(a.length, b.length, 60)
        return n >= minOf(12, b.length) && a.take(n) == b.take(n)
    }
    private fun key(t: String) = t.lowercase().filter { it.isLetterOrDigit() }

    // ---------- hide and block (on the phone) ----------

    fun hide(n: Note) = prefs?.edit()?.putStringSet("hidden", hidden() + n.id)?.apply()
    fun block(n: Note) = prefs?.edit()?.putStringSet("blocked", blocked() + n.uid)?.apply()
    fun unblockAll() = prefs?.edit()?.remove("blocked")?.remove("hidden")?.apply()
    private fun hidden() = prefs?.getStringSet("hidden", emptySet()).orEmpty()
    private fun blocked() = prefs?.getStringSet("blocked", emptySet()).orEmpty()
    private fun List<Note>.visible() = filter { it.id !in hidden() && it.uid !in blocked() }

    // ---------- the feed ----------

    /** Parses a Blogger comment feed. Spam (links to other sites) and comments on pages other than guides or the board are dropped. */
    fun parse(json: String): List<Note> {
        val entries = JSONObject(json).optJSONObject("feed")?.optJSONArray("entry") ?: return emptyList()
        return (0 until entries.length()).mapNotNull { i ->
            val e = entries.getJSONObject(i)
            val html = e.optJSONObject("content")?.optString("\$t").orEmpty()
            if (spam(html)) return@mapNotNull null
            val text = plain(html).ifBlank { return@mapNotNull null }
            val reply = e.optJSONObject("thr\$in-reply-to")
            val postUrl = reply?.optString("href").orEmpty().substringBefore('?').substringBefore('#')
            val postId = reply?.optString("ref").orEmpty().substringAfterLast("post-")
            if (postId.isBlank() || !(Regex("/\\d{4}/\\d{2}/[^/]+\\.html$").containsMatchIn(postUrl) || postId == board)) return@mapNotNull null
            val author = e.optJSONArray("author")?.optJSONObject(0)
            val name = author?.optJSONObject("name")?.optString("\$t").orEmpty().ifBlank { "Reader" }
            val profile = author?.optJSONObject("uri")?.optString("\$t").orEmpty()
            val photo = author?.optJSONObject("gd\$image")?.optString("src").orEmpty().let { if (it.startsWith("//")) "https:$it" else it }
                .takeUnless { it.contains("blogblog.com") || it.contains("blank.gif") || it.isBlank() }.orEmpty()
            val links = e.optJSONArray("link")
            val url = (0 until (links?.length() ?: 0)).map { links!!.getJSONObject(it) }.firstOrNull { it.optString("rel") == "alternate" }?.optString("href").orEmpty()
            Note(e.optJSONObject("id")?.optString("\$t").orEmpty().substringAfterLast("post-"), postId, postUrl, name,
                profile.substringAfterLast('/').ifBlank { "name:$name" }, photo, text, time(e.optJSONObject("published")?.optString("\$t")), url)
        }.sortedByDescending { it.published }
    }

    /** Comment HTML to text: line breaks kept, tags and entities gone. */
    fun plain(html: String): String = Jsoup.parseBodyFragment(html.replace(Regex("(?i)<br\\s*/?>"), "\n")).body().wholeText()
        .lines().joinToString("\n") { it.trim() }.replace(Regex("\n{3,}"), "\n\n").trim()

    /** Links to another site, or a bare web address that isn't the blog: the usual comment spam. */
    fun spam(html: String): Boolean {
        val hrefs = Regex("(?i)href=\"([^\"]+)\"").findAll(html).map { it.groupValues[1] }.toList()
        val bare = Regex("(?i)(https?://|www\\.)[\\w.-]+").findAll(plain(html)).map { it.value }.toList()
        return (hrefs + bare).any { !it.contains(Blog.HOST) && !it.startsWith("#") }
    }

    private fun time(s: String?) = runCatching { OffsetDateTime.parse(s).toInstant().toEpochMilli() }.getOrDefault(0L)

    private fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15_000; c.readTimeout = 20_000
        return try {
            if (c.responseCode == 404) """{"feed":{}}""" else c.inputStream.bufferedReader().use { it.readText() }
        } finally { c.disconnect() }
    }
}
