package com.randomthoughtsever.plainly.data

import android.content.Context
import android.net.Uri
import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

object Blog {
    const val HOST = "randomthoughtsever.blogspot.com"
    const val BASE = "https://$HOST"
    const val PRIVACY = "$BASE/2021/06/privacy-policy.html"
}

/** One article, read from the blog's public JSON feed. */
data class Post(
    val id: String,
    val title: String,
    val url: String,
    val path: String,
    val published: String,
    val labels: List<String>,
    val html: String,
    val image: String?,
    val excerpt: String,
    val minutes: Int,
) {
    val label: String get() = labels.firstOrNull { it != "Start Here" } ?: "Guide"

    fun toJson(): JSONObject = JSONObject()
        .put("id", id).put("title", title).put("url", url).put("path", path).put("published", published)
        .put("labels", JSONArray(labels)).put("html", html).put("image", image ?: "").put("excerpt", excerpt).put("minutes", minutes)

    companion object {
        fun fromJson(o: JSONObject) = Post(
            o.getString("id"), o.getString("title"), o.getString("url"), o.getString("path"), o.optString("published"),
            o.optJSONArray("labels")?.let { a -> List(a.length()) { a.getString(it) } } ?: emptyList(),
            o.optString("html"), o.optString("image").ifBlank { null }, o.optString("excerpt"), o.optInt("minutes", 3),
        )
    }
}

class Repository(private val context: Context) {
    private val memory = ConcurrentHashMap<String, Post>()
    private val cacheDir = File(context.cacheDir, "feed").apply { mkdirs() }
    /** Cover art per post path: the copy built into the app, then the website's live list (so new posts get theirs). */
    @Volatile private var covers: Map<String, String> = runCatching {
        val o = JSONObject(context.assets.open("covers.json").bufferedReader().readText())
        o.keys().asSequence().associateWith { o.getString(it) }
    }.getOrDefault(emptyMap()) + readCoverFile()
    private val coverFile get() = File(context.filesDir, "covers-live.json")
    @Volatile private var coversCheckedAt = 0L

    private fun readCoverFile(): Map<String, String> = runCatching {
        val o = JSONObject(File(context.filesDir, "covers-live.json").readText())
        o.keys().asSequence().associateWith { o.getString(it) }
    }.getOrDefault(emptyMap())

    /**
     * Reads the website's current cover list (the same one the site's design layer uses: version.txt names the release,
     * and bd-redesign.js carries BD_COVERS). Checked at most every 30 minutes.
     */
    suspend fun refreshCovers() = withContext(Dispatchers.IO) {
        if (System.currentTimeMillis() - coversCheckedAt < 30 * 60_000) return@withContext
        coversCheckedAt = System.currentTimeMillis()
        runCatching {
            val ver = fetch("https://raw.githubusercontent.com/venkateshramu07-design/My_Repo/ccr-ee047f91-rbyv4x/seo/theme/version.txt").trim()
            if (!ver.matches(Regex("[0-9a-f]{40}"))) return@runCatching
            val js = fetch("https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@$ver/seo/theme/bd-redesign.js")
            val json = Regex("BD_COVERS\\s*=\\s*(\\{.*?\\});", RegexOption.DOT_MATCHES_ALL).find(js)?.groupValues?.get(1) ?: return@runCatching
            val o = JSONObject(json)
            val live = o.keys().asSequence().filter { it.startsWith("/") }.associateWith { o.getString(it) }
                .filterValues { it.startsWith("https://cdn.jsdelivr.net/gh/venkateshramu07-design/") }
            if (live.isEmpty()) return@runCatching
            coverFile.writeText(JSONObject(live).toString())
            covers = covers + live
        }
    }

    /** The newest post's id and title (a tiny request), used to spot new posts as soon as they're published. */
    suspend fun newest(): Pair<String, String>? = withContext(Dispatchers.IO) {
        runCatching {
            val e = JSONObject(fetch("${Blog.BASE}/feeds/posts/summary?alt=json&max-results=1&orderby=published"))
                .getJSONObject("feed").getJSONArray("entry").getJSONObject(0)
            e.getJSONObject("id").getString("\$t").substringAfter("post-") to e.getJSONObject("title").getString("\$t")
        }.getOrNull()
    }
    @Volatile private var pathIndex: Map<String, String>? = null

    suspend fun latest(start: Int = 1, count: Int = 20): List<Post> {
        if (start == 1) runCatching { kotlinx.coroutines.withTimeoutOrNull(5000) { refreshCovers() } }
        return list("${Blog.BASE}/feeds/posts/default?alt=json&orderby=published&max-results=$count&start-index=$start")
    }

    suspend fun topic(label: String, start: Int = 1, count: Int = 20): List<Post> =
        list("${Blog.BASE}/feeds/posts/default/-/${Uri.encode(label)}?alt=json&max-results=$count&start-index=$start")

    /** Blogger finds the posts; SearchRank puts the most relevant first. If a multi-word search finds nothing, its longest word is tried. */
    suspend fun search(query: String): List<Post> {
        var found = list("${Blog.BASE}/feeds/posts/default?alt=json&max-results=50&q=${Uri.encode(query)}")
        val ws = SearchRank.words(query)
        if (found.isEmpty() && ws.size > 1) found = list("${Blog.BASE}/feeds/posts/default?alt=json&max-results=50&q=${Uri.encode(ws.maxBy { it.length })}")
        return withContext(Dispatchers.Default) { SearchRank.rank(query, found) }
    }

    fun cached(id: String): Post? = memory[id]

    suspend fun post(id: String): Post? {
        memory[id]?.let { return it }
        return runCatching {
            val o = JSONObject(get("${Blog.BASE}/feeds/posts/default/$id?alt=json")).getJSONObject("entry")
            parse(o).also { memory[it.id] = it }
        }.getOrNull()
    }

    /** Finds a post id from a blog address such as /2026/10/some-post.html (used for links between articles). */
    suspend fun idForPath(path: String): String? {
        val clean = path.substringBefore('?').substringBefore('#')
        memory.values.firstOrNull { it.path == clean }?.let { return it.id }
        val index = pathIndex ?: loadIndex().also { pathIndex = it }
        return index[clean]
    }

    private suspend fun loadIndex(): Map<String, String> {
        val map = HashMap<String, String>()
        var start = 1
        while (start < 1000) {
            val body = runCatching { get("${Blog.BASE}/feeds/posts/summary?alt=json&max-results=150&start-index=$start") }.getOrNull() ?: break
            val entries = JSONObject(body).getJSONObject("feed").optJSONArray("entry") ?: break
            for (i in 0 until entries.length()) {
                val e = entries.getJSONObject(i)
                val id = e.getJSONObject("id").getString("\$t").substringAfter("post-")
                map[pathOf(alternate(e))] = id
            }
            if (entries.length() == 0) break
            start += entries.length()
        }
        return map
    }

    private suspend fun list(url: String): List<Post> {
        val body = get(url)
        val entries = JSONObject(body).getJSONObject("feed").optJSONArray("entry") ?: return emptyList()
        return List(entries.length()) { parse(entries.getJSONObject(it)) }
            .filterNot { it.labels.isEmpty() && it.path.startsWith("/2021/06/") } // legal pages
            .onEach { memory[it.id] = it }
    }

    @Volatile private var indexCache: List<Entry>? = null
    @Volatile private var indexAt = 0L
    private val indexFile get() = File(context.filesDir, "index.json")

    /**
     * Every guide on the blog (title, topic, date and its quick answer), from the light summary feed: about 300 posts
     * in 2-3 small requests. Kept in memory and on the phone, refreshed every 10 minutes, so a new post shows up quickly.
     */
    suspend fun index(force: Boolean = false): List<Entry> = withContext(Dispatchers.IO) {
        indexCache?.let { if (!force && System.currentTimeMillis() - indexAt < 10 * 60_000) return@withContext it }
        val fresh = runCatching {
            val all = ArrayList<Entry>()
            var start = 1
            while (start < 2000) {
                val entries = JSONObject(fetch("${Blog.BASE}/feeds/posts/summary?alt=json&orderby=published&max-results=150&start-index=$start"))
                    .getJSONObject("feed").optJSONArray("entry") ?: break
                if (entries.length() == 0) break
                for (i in 0 until entries.length()) all += entry(entries.getJSONObject(i))
                start += entries.length() // Blogger may return fewer than asked for, so only an empty page means the end
            }
            all.filterNot { it.labels.isEmpty() && it.path.startsWith("/2021/06/") }.filterNot { it.title.startsWith("Sitemap", true) }
        }.getOrNull()
        if (fresh != null && fresh.isNotEmpty()) {
            indexCache = fresh; indexAt = System.currentTimeMillis()
            runCatching { indexFile.writeText(JSONArray(fresh.map { e -> JSONObject().put("id", e.id).put("title", e.title).put("path", e.path)
                .put("url", e.url).put("labels", JSONArray(e.labels)).put("published", e.published).put("quick", e.quick) }).toString()) }
            fresh
        } else indexCache ?: runCatching {
            val a = JSONArray(indexFile.readText())
            List(a.length()) { i -> a.getJSONObject(i).let { o ->
                val path = o.getString("path")
                Entry(o.getString("id"), o.getString("title"), path, o.getString("url"),
                    o.getJSONArray("labels").let { l -> List(l.length()) { l.getString(it) } }, o.getString("published"), o.getString("quick"), coverFor(path))
            } }.also { indexCache = it }
        }.getOrDefault(emptyList())
    }

    fun coverFor(path: String): String? = covers[path]?.let { "$it-800.webp" }

    private fun entry(e: JSONObject): Entry {
        val url = alternate(e).replace("http://", "https://")
        val path = pathOf(url)
        val summary = e.optJSONObject("summary")?.optString("\$t").orEmpty().replace(Regex("\\s+"), " ").trim()
        val quick = summary.substringAfter("Quick answer:", summary).removePrefix("Updated").replace(Regex("^\\s*\\d{1,2} \\w+ \\d{4}\\s*"), "").trim()
        return Entry(
            id = e.getJSONObject("id").getString("\$t").substringAfter("post-"),
            title = Html.fromHtml(e.getJSONObject("title").getString("\$t"), Html.FROM_HTML_MODE_LEGACY).toString(),
            path = path, url = url,
            labels = e.optJSONArray("category")?.let { a -> List(a.length()) { a.getJSONObject(it).getString("term") } } ?: emptyList(),
            published = e.getJSONObject("published").getString("\$t").take(10),
            quick = quick.take(400).let { if (quick.length > 400) it.substringBeforeLast(' ') + "…" else it },
            image = coverFor(path),
        )
    }

    private fun alternate(e: JSONObject): String {
        val links = e.getJSONArray("link")
        for (i in 0 until links.length()) if (links.getJSONObject(i).getString("rel") == "alternate") return links.getJSONObject(i).getString("href")
        return Blog.BASE
    }

    private fun pathOf(url: String) = Uri.parse(url).path ?: "/"

    private fun parse(e: JSONObject): Post {
        val id = e.getJSONObject("id").getString("\$t").substringAfter("post-")
        val url = alternate(e).replace("http://", "https://")
        val path = pathOf(url)
        val html = e.optJSONObject("content")?.optString("\$t") ?: e.optJSONObject("summary")?.optString("\$t") ?: ""
        val labels = e.optJSONArray("category")?.let { a -> List(a.length()) { a.getJSONObject(it).getString("term") } } ?: emptyList()
        val text = plain(html)
        return Post(
            id = id,
            title = Html.fromHtml(e.getJSONObject("title").getString("\$t"), Html.FROM_HTML_MODE_LEGACY).toString(),
            url = url, path = path,
            published = e.getJSONObject("published").getString("\$t").take(10),
            labels = labels, html = html,
            image = covers[path]?.let { "$it-800.webp" } ?: firstImage(html),
            excerpt = excerpt(html, text),
            minutes = (text.split(Regex("\\s+")).size / 230).coerceAtLeast(2),
        )
    }

    private fun firstImage(html: String): String? = Regex("<img[^>]+src=\"([^\"]+)\"").findAll(html)
        .map { it.groupValues[1] }
        .firstOrNull { it.startsWith("https://") && !it.contains("emoji") }

    private fun plain(html: String): String = Html.fromHtml(
        html.replace(Regex("(?is)<(script|style)[^>]*>.*?</\\1>"), " "), Html.FROM_HTML_MODE_LEGACY,
    ).toString().replace('￼', ' ').replace(Regex("\\s+"), " ").trim()

    private fun excerpt(html: String, text: String): String {
        val quick = Regex("(?is)<p[^>]*>\\s*<strong>\\s*Quick answer:?\\s*</strong>(.*?)</p>").find(html)?.groupValues?.get(1)
        val base = quick?.let { plain(it) } ?: text.removePrefix("Updated").let { t ->
            t.replace(Regex("^\\s*\\d{1,2} \\w+ \\d{4}\\s*"), "")
        }
        return base.take(220).let { if (base.length > 220) it.substringBeforeLast(' ') + "…" else it }.trim()
    }

    /** Network first, falling back to the last saved copy so lists still open offline. */
    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val file = File(cacheDir, sha(url))
        try {
            val c = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 12000; readTimeout = 20000
                setRequestProperty("User-Agent", "PlainlyApp/1.0 (Android)")
            }
            c.inputStream.bufferedReader().use { it.readText() }.also { file.writeText(it) }
        } catch (e: Exception) {
            if (file.exists()) file.readText() else throw e
        }
    }

    private fun fetch(url: String): String {
        val c = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000; readTimeout = 15000; useCaches = false
            setRequestProperty("User-Agent", "PlainlyApp/1.1 (Android)")
        }
        return c.inputStream.bufferedReader().use { it.readText() }
    }

    private fun sha(s: String) = MessageDigest.getInstance("SHA-1").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
}
