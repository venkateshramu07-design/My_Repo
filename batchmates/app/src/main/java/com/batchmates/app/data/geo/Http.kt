package com.batchmates.app.data.geo

import android.content.Context
import com.batchmates.app.BuildConfig
import kotlinx.coroutines.Dispatchers
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

    private suspend fun cached(key: String, ttlMs: Long, fetch: () -> String): String = withContext(Dispatchers.IO) {
        val file = File(cacheDir, sha1(key))
        if (file.exists() && System.currentTimeMillis() - file.lastModified() < ttlMs) return@withContext file.readText()
        try {
            fetch().also { file.writeText(it) }
        } catch (e: Exception) {
            if (file.exists()) file.readText() else throw e // stale beats nothing when offline
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
