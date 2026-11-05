package com.randomthoughtsever.plainly.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import java.util.concurrent.TimeUnit

/**
 * "New guide" notifications. Android lets an app check in the background about every 15 minutes, so a new post is
 * announced within roughly a quarter of an hour (Android may wait longer when the battery is low). Inside the app,
 * Today refreshes the moment it's opened and checks every minute while it's on screen.
 */
object NewPosts {
    const val CHANNEL = "new-guides"
    const val EXTRA_POST = "post_id"
    private const val WORK = "new-guides-check"

    fun prefs(c: Context) = c.getSharedPreferences("plainly", Context.MODE_PRIVATE)
    fun enabled(c: Context) = prefs(c).getBoolean("notify", true)
    fun canNotify(c: Context) = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun setEnabled(c: Context, on: Boolean) { prefs(c).edit().putBoolean("notify", on).apply(); schedule(c) }

    /** WorkManager starts itself on a phone; in automated tests (Robolectric) it doesn't, so a failure there is ignored. */
    fun schedule(c: Context) = runCatching { scheduleNow(c) }.onFailure { android.util.Log.w("Plainly", "New-guide checks not scheduled", it) }

    private fun scheduleNow(c: Context) {
        val wm = WorkManager.getInstance(c)
        if (!enabled(c)) { wm.cancelUniqueWork(WORK); return }
        val req = PeriodicWorkRequestBuilder<Worker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        wm.enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP, req)
    }

    class Worker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
            val c = applicationContext
            if (!enabled(c)) return@withContext Result.success()
            val body = runCatching {
                (URL("${Blog.BASE}/feeds/posts/summary?alt=json&max-results=5&orderby=published").openConnection() as HttpURLConnection).run {
                    connectTimeout = 10000; readTimeout = 15000; useCaches = false
                    setRequestProperty("User-Agent", "PlainlyApp/1.1 (Android)")
                    inputStream.bufferedReader().use { it.readText() }
                }
            }.getOrNull() ?: return@withContext Result.retry()
            val entries = JSONObject(body).getJSONObject("feed").optJSONArray("entry") ?: return@withContext Result.success()
            val posts = List(entries.length()) { i ->
                val e = entries.getJSONObject(i)
                Triple(e.getJSONObject("id").getString("\$t").substringAfter("post-"), e.getJSONObject("title").getString("\$t"),
                    e.getJSONObject("published").getString("\$t"))
            }
            val p = prefs(c)
            val last = p.getString("lastPublished", null)
            val newest = posts.maxOfOrNull { it.third } ?: return@withContext Result.success()
            p.edit().putString("lastPublished", newest).apply()
            if (last == null) return@withContext Result.success() // first check only records where we are
            posts.filter { it.third > last }.take(3).forEach { (id, title, _) -> notify(c, id, title) }
            Result.success()
        }
    }

    fun notify(c: Context, id: String, title: String) {
        if (!canNotify(c)) return
        val nm = c.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL) == null)
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "New guides", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "A notification when a new guide is published"
            })
        val open = PendingIntent.getActivity(c, id.hashCode(), Intent(c, MainActivity::class.java).putExtra(EXTRA_POST, id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF1F4E79.toInt())
            .setContentTitle("New on Plainly")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(c).notify(id.hashCode(), n) }
    }
}
