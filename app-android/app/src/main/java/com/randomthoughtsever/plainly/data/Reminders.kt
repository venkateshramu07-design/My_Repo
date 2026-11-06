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
 * One gentle nudge a day at most, at the hour the reader usually opens the app, and only when there's a reason:
 * a streak about to break, a challenge check-in, today's puzzle and Daily Run, or "did it work?". Nothing otherwise.
 */
object Reminders {
    private const val WORK = "daily-nudge"
    private const val CHANNEL = "reminders"
    const val EXTRA_ROUTE = "route"

    /** (Re)schedules the daily check for the reader's usual hour. Cheap to call on every open. */
    fun schedule(c: Context) = runCatching {
        val hour = Progress(c).usualHour()
        val prefs = NewPosts.prefs(c)
        val same = prefs.getInt("nudgeHour", -1) == hour
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(hour, 5); if (!next.isAfter(now)) next = next.plusDays(1)
        val req = PeriodicWorkRequestBuilder<Worker>(1, TimeUnit.DAYS).setInitialDelay(Duration.between(now, next).toMinutes(), TimeUnit.MINUTES).build()
        WorkManager.getInstance(c).enqueueUniquePeriodicWork(WORK, if (same) ExistingPeriodicWorkPolicy.KEEP else ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, req)
        prefs.edit().putInt("nudgeHour", hour).apply()
    }

    /** What to say today, or null for nothing. Pure, so it's tested. */
    fun message(openedToday: Boolean, streak: Int, pendingChallenge: Challenge?, runDone: Boolean, followUp: String?, puzzle: String): Pair<String, String>? = when {
        !openedToday && streak >= 2 -> "🔥 Your $streak-day streak is safe until midnight. A 5-minute Daily Run keeps it going." to "run"
        pendingChallenge != null -> "${pendingChallenge.emoji} ${pendingChallenge.title}: ${pendingChallenge.action.lowercase()} to keep going." to "challenge/${pendingChallenge.key}"
        !runDone -> "🧩 $puzzle is ready. Your Daily Run takes 5 minutes." to "run"
        followUp != null -> "💪 You were trying \"$followUp\". Did it work?" to "today"
        else -> null
    }

    class Worker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val c = applicationContext
            if (!NewPosts.canNotify(c) || !NewPosts.prefs(c).getBoolean("reminders", true)) return Result.success()
            val pr = Progress(c); val profile = Profile(c); val lib = Library(c)
            val today = LocalDate.now()
            if (pr.lastNudge == today.toString()) return Result.success()
            val opened = today.toString() in lib.openDays()
            val kept = lib.keptDays()
            var streak = 0; var d = today.minusDays(1); while (d.toString() in kept) { streak++; d = d.minusDays(1) }
            val pending = profile.joined().filter { today.toString() !in it.days }.firstNotNullOfOrNull { j -> CHALLENGES.firstOrNull { it.key == j.key } }
            val (text, route) = message(opened, streak, pending, pr.runDone().containsAll(Progress.RUN_STEPS), pr.followUp()?.title,
                "${Puzzles.kind().title} #${Puzzles.number()}") ?: return Result.success()
            val nm = c.getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL) == null) nm.createNotificationChannel(NotificationChannel(CHANNEL, "Reminders", NotificationManager.IMPORTANCE_DEFAULT))
            val open = PendingIntent.getActivity(c, 7, Intent(c, MainActivity::class.java).putExtra(EXTRA_ROUTE, route).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            runCatching {
                NotificationManagerCompat.from(c).notify(7, NotificationCompat.Builder(c, CHANNEL).setSmallIcon(R.drawable.ic_notification)
                    .setColor(0xFF1F4E79.toInt()).setContentTitle("Plainly").setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setContentIntent(open).setAutoCancel(true).build())
                pr.lastNudge = today.toString()
            }
            return Result.success()
        }
    }
}
