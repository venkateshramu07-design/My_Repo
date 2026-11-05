package com.randomthoughtsever.plainly.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.randomthoughtsever.plainly.MainActivity
import com.randomthoughtsever.plainly.R
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * An evening nudge (around 7 pm) while a challenge is running and today's check-in isn't done yet, or when the
 * streak would break. Nothing is sent when there's nothing to do. Uses the same notification permission as new guides.
 */
object Reminders {
    private const val WORK = "daily-nudge"
    private const val CHANNEL = "reminders"

    fun schedule(c: Context) = runCatching {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(19, 0); if (!next.isAfter(now)) next = next.plusDays(1)
        val req = PeriodicWorkRequestBuilder<Worker>(1, TimeUnit.DAYS).setInitialDelay(Duration.between(now, next).toMinutes(), TimeUnit.MINUTES).build()
        WorkManager.getInstance(c).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP, req)
    }

    class Worker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val c = applicationContext
            if (!NewPosts.canNotify(c) || !NewPosts.prefs(c).getBoolean("reminders", true)) return Result.success()
            val profile = Profile(c)
            val today = LocalDate.now().toString()
            val pending = profile.joined().filter { today !in it.days }.mapNotNull { j -> CHALLENGES.firstOrNull { it.key == j.key } }
            val text = when {
                pending.isNotEmpty() -> "${pending.first().emoji} ${pending.first().title}: ${pending.first().action.lowercase()} to keep going."
                profile.dailyDone().size < 3 -> "Your Daily 3 is waiting: a tip, a minute of calm and one quiz question."
                else -> return Result.success()
            }
            val nm = c.getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL) == null) nm.createNotificationChannel(NotificationChannel(CHANNEL, "Reminders", NotificationManager.IMPORTANCE_DEFAULT))
            val open = PendingIntent.getActivity(c, 7, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            runCatching {
                NotificationManagerCompat.from(c).notify(7, NotificationCompat.Builder(c, CHANNEL).setSmallIcon(R.drawable.ic_notification)
                    .setColor(0xFF1F4E79.toInt()).setContentTitle("Plainly").setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setContentIntent(open).setAutoCancel(true).build())
            }
            return Result.success()
        }
    }
}
