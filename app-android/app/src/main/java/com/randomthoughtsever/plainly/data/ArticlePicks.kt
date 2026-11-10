package com.randomthoughtsever.plainly.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.randomthoughtsever.plainly.MainActivity
import com.randomthoughtsever.plainly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * "Worth a read": every 2 hours, one random guide from the blog. Separate from the new-guide alerts and the daily
 * reminder. Skips legal/info pages and anything picked recently, stays quiet at night (22:00-08:00 on the phone's clock),
 * and replaces its previous notification instead of stacking up. Has its own switch in Settings and its own channel.
 */
object ArticlePicks {
    const val CHANNEL = "article-picks"
    private const val WORK = "article-picks"
    private const val NOTIFICATION_ID = 0x5EED
    private const val KEEP_RECENT = 60
    const val QUIET_FROM = 22
    const val QUIET_TO = 8

    fun enabled(c: Context) = NewPosts.prefs(c).getBoolean("picks", true)
    fun setEnabled(c: Context, on: Boolean) { NewPosts.prefs(c).edit().putBoolean("picks", on).apply(); schedule(c) }

    fun schedule(c: Context) = runCatching {
        val wm = WorkManager.getInstance(c)
        if (!enabled(c)) { wm.cancelUniqueWork(WORK); return@runCatching }
        val req = PeriodicWorkRequestBuilder<Worker>(2, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        wm.enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP, req)
    }.onFailure { android.util.Log.w("Plainly", "Article picks not scheduled", it) }

    /** Night is quiet: no pick between [QUIET_FROM]:00 and [QUIET_TO]:00. */
    fun quiet(hour: Int) = hour >= QUIET_FROM || hour < QUIET_TO

    /** Legal and site pages (no topic label, or the sitemap) are never suggested. */
    fun suggestible(labels: List<String>, title: String) = labels.isNotEmpty() && !title.startsWith("Sitemap", ignoreCase = true)

    data class Pick(val id: String, val title: String, val summary: String)

    /** Pure choice, tested: a random suggestible post not picked recently; if all were recent, any suggestible one. */
    fun choose(posts: List<Triple<Pick, List<String>, Boolean>>, recent: Set<String>, rnd: Random): Pick? {
        val ok = posts.filter { (p, labels, _) -> suggestible(labels, p.title) }
        val fresh = ok.filter { it.first.id !in recent }
        return (fresh.ifEmpty { ok }).randomOrNull(rnd)?.first
    }

    /** The post's opening sentence, without the "Updated <date>" line and "Quick answer:"/"In short:" labels. */
    fun teaser(summary: String): String {
        val s = summary.replace(Regex("\\s+"), " ").trim()
            .replace(Regex("^Updated \\d{1,2} \\p{L}+ \\d{4}\\s*"), "")
            .replace(Regex("^(Quick answer|In short|Short answer|The short answer)\\s*:\\s*", RegexOption.IGNORE_CASE), "")
        return if (s.length > 140) s.take(137).substringBeforeLast(' ') + "…" else s
    }

    private fun get(url: String): String = (URL(url).openConnection() as HttpURLConnection).run {
        connectTimeout = 10000; readTimeout = 20000; useCaches = false
        setRequestProperty("User-Agent", "PlainlyApp/1.1 (Android)")
        inputStream.bufferedReader().use { it.readText() }
    }

    class Worker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
            val c = applicationContext
            if (!enabled(c) || !NewPosts.canNotify(c) || quiet(LocalTime.now().hour)) return@withContext Result.success()
            val rnd = Random(System.nanoTime())
            val total = runCatching {
                JSONObject(get("${Blog.BASE}/feeds/posts/summary?alt=json&max-results=0")).getJSONObject("feed")
                    .getJSONObject("openSearch\$totalResults").getString("\$t").toInt()
            }.getOrNull() ?: return@withContext Result.retry()
            if (total <= 0) return@withContext Result.success()
            // A random window of 25 posts anywhere in the blog, then a random pick inside it.
            val start = 1 + rnd.nextInt(maxOf(1, total - 24))
            val entries = runCatching {
                JSONObject(get("${Blog.BASE}/feeds/posts/summary?alt=json&max-results=25&start-index=$start"))
                    .getJSONObject("feed").optJSONArray("entry")
            }.getOrNull() ?: return@withContext Result.retry()
            val posts = List(entries.length()) { i ->
                val e = entries.getJSONObject(i)
                val labels = e.optJSONArray("category")?.let { a -> List(a.length()) { a.getJSONObject(it).getString("term") } }.orEmpty()
                val summary = e.optJSONObject("summary")?.optString("\$t").orEmpty().replace(Regex("\\s+"), " ").trim()
                Triple(Pick(e.getJSONObject("id").getString("\$t").substringAfter("post-"), e.getJSONObject("title").getString("\$t"), summary), labels, true)
            }
            val prefs = NewPosts.prefs(c)
            val recent = prefs.getString("picksRecent", "").orEmpty().split(',').filter { it.isNotBlank() }
            val pick = choose(posts, recent.toSet(), rnd) ?: return@withContext Result.success()
            notify(c, pick)
            prefs.edit().putString("picksRecent", (recent + pick.id).takeLast(KEEP_RECENT).joinToString(",")).apply()
            Result.success()
        }
    }

    fun notify(c: Context, p: Pick) {
        if (!NewPosts.canNotify(c)) return
        val nm = c.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL) == null)
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "Worth a read", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "A guide from the blog every 2 hours (not at night)"
            })
        val open = PendingIntent.getActivity(c, NOTIFICATION_ID, Intent(c, MainActivity::class.java).putExtra(NewPosts.EXTRA_POST, p.id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val text = teaser(p.summary)
        val n = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF1F4E79.toInt())
            .setContentTitle("📖 Worth a read: ${p.title}")
            .setContentText(text.ifBlank { p.title })
            .setStyle(NotificationCompat.BigTextStyle().bigText(text.ifBlank { p.title }))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        if (!NewPosts.canNotify(c)) return
        @android.annotation.SuppressLint("MissingPermission") // checked just above (canNotify)
        fun post() = NotificationManagerCompat.from(c).notify(NOTIFICATION_ID, n)
        runCatching { post() }
    }
}
