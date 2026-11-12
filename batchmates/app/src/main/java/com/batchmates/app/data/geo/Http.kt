package com.batchmates.app.data.geo

import android.content.Context
import com.batchmates.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

/**
 * Tiny HTTP client with a disk cache. Free OpenStreetMap services ask clients to cache and
 * to identify themselves, and caching also makes the app feel instant on the second visit.
 */
class Http(context: Context) {
    private val cacheDir = File(context.cacheDir, "geo").apply { mkdirs() }
    private val userAgent = "BatchMates/${BuildConfig.VERSION_NAME} (Android school-reunion app)"

    suspend fun get(url: String, ttlMs: Long = WEEK, readTimeoutMs: Int = 20_000): String =
        cached(url, ttlMs) { request(url, null, readTimeoutMs) }

    suspend fun postForm(url: String, field: String, value: String, ttlMs: Long = WEEK, readTimeoutMs: Int = 30_000): String =
        cached(url + "\n" + value, ttlMs) { request(url, "$field=" + URLEncoder.encode(value, "UTF-8"), readTimeoutMs) }

    /**
     * Downloads a gzipped JSON file, asking every mirror AT THE SAME TIME and taking the first good answer
     * (some networks block one CDN or another). Cached on disk. A 404 means "no data here" and is cached as "".
     * Anything that isn't real JSON (an ISP block page, a captive portal) is rejected and never cached.
     */
    suspend fun getGzipJson(mirrors: List<String>, ttlMs: Long): String = cached("gz:" + mirrors.first(), ttlMs) {
        firstGood(mirrors)
    }

    private suspend fun firstGood(mirrors: List<String>): String = kotlinx.coroutines.coroutineScope {
        val answer = kotlinx.coroutines.CompletableDeferred<String>()
        val jobs = mirrors.map { url ->
            launch(Dispatchers.IO) {
                runCatching { fetchGzip(url) }.onSuccess { answer.complete(it) }
            }
        }
        launch { jobs.joinAll(); answer.completeExceptionally(IllegalStateException("No mirror answered. Check your internet.")) }
        try { answer.await() } finally { jobs.forEach { it.cancel() } ; coroutineContext.cancelChildren() }
    }

    private fun fetchGzip(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 7_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("User-Agent", userAgent)
            when (val code = conn.responseCode) {
                404 -> return ""
                !in 200..299 -> error("HTTP $code")
            }
            val bytes = conn.inputStream.use { it.readBytes() }
            // Some CDNs already decompress (Content-Encoding: gzip); only gunzip real gzip data.
            val gz = bytes.size > 2 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()
            val text = if (gz) java.util.zip.GZIPInputStream(bytes.inputStream()).bufferedReader().use { it.readText() } else String(bytes)
            check(looksLikeJson(text)) { "Not data (blocked or captive page)" }
            return text
        } finally {
            conn.disconnect()
        }
    }

    private fun looksLikeJson(text: String) = text.trimStart().let { it.startsWith("{") || it.startsWith("[") }

    private suspend fun cached(key: String, ttlMs: Long, fetch: suspend () -> String): String = withContext(Dispatchers.IO) {
        val file = File(cacheDir, sha1(key))
        if (file.exists() && System.currentTimeMillis() - file.lastModified() < ttlMs) {
            val text = file.readText()
            if (text.isEmpty() || looksLikeJson(text)) return@withContext text
            file.delete() // a poisoned entry from an older version
        }
        try {
            fetch().also { text ->
                // Cache only real data ("" = known-empty). Block pages must not poison the cache for a month.
                if (text.isEmpty() || looksLikeJson(text)) file.writeText(text)
                else error("The server returned a web page instead of data. Your network may be blocking it.")
            }
        } catch (e: Exception) {
            if (file.exists() && looksLikeJson(file.readText())) file.readText() else throw e // stale beats nothing when offline
        }
    }

    private fun request(url: String, form: String?, readTimeoutMs: Int): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = readTimeoutMs
            conn.setRequestProperty("User-Agent", userAgent)
            conn.setRequestProperty("Accept-Language", "en")
            if (form != null) {
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                conn.outputStream.use { it.write(form.toByteArray()) }
            }
            val code = conn.responseCode
            if (code !in 200..299) error("Map service busy (HTTP $code). Please try again in a moment.")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun sha1(s: String) = MessageDigest.getInstance("SHA-1").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    companion object {
        const val DAY = 24 * 60 * 60 * 1000L
        const val WEEK = 7 * DAY
    }
}
