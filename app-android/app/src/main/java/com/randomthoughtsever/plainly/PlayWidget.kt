package com.randomthoughtsever.plainly

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.randomthoughtsever.plainly.data.Library
import com.randomthoughtsever.plainly.data.Profile
import com.randomthoughtsever.plainly.data.Progress
import com.randomthoughtsever.plainly.data.Reminders
import com.randomthoughtsever.plainly.data.quizOfTheDay

/**
 * The play widget: today's quiz, answered right on the home screen (with XP), plus level, streak and Daily Run
 * progress. Tapping the header opens the Daily Run.
 */
class PlayWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = ids.forEach { update(context, manager, it) }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ANSWER) return
        val option = intent.getIntExtra("option", -1)
        val profile = Profile(context)
        val q = quizOfTheDay(profile.goals)
        if (option in q.options.indices && profile.answered(q.id) == null) {
            val right = option == q.correct
            profile.answer(q.id, right)
            Progress(context).award("quiz:${q.id}", if (right) 15 else 5, if (right) "Quiz: right!" else "Quiz answered", "quizzes")
            context.getSharedPreferences("progress", Context.MODE_PRIVATE).edit().putInt("widgetPick:${q.id}", option).apply()
        }
        refresh(context)
    }

    companion object {
        private const val ANSWER = "com.randomthoughtsever.plainly.WIDGET_ANSWER"

        fun refresh(context: Context) {
            val m = AppWidgetManager.getInstance(context)
            m.getAppWidgetIds(ComponentName(context, PlayWidget::class.java)).forEach { update(context, m, it) }
        }

        private fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val profile = Profile(context); val pr = Progress(context)
            val q = quizOfTheDay(profile.goals)
            val answered = profile.answered(q.id)
            val pick = context.getSharedPreferences("progress", Context.MODE_PRIVATE).getInt("widgetPick:${q.id}", -1)
            val streak = runCatching { Library(context).streak() }.getOrDefault(0)
            val run = pr.runDone().size
            val v = RemoteViews(context.packageName, R.layout.widget_play)
            v.setTextViewText(R.id.play_level, "LEVEL ${pr.level()} · ${pr.xp} XP" + if (streak > 0) "  🔥 $streak" else "")
            v.setTextViewText(R.id.play_run, if (run >= 5) "Run done ✓" else "Daily Run $run/5")
            v.setTextViewText(R.id.play_question, q.question)
            val ids = listOf(R.id.play_opt0, R.id.play_opt1, R.id.play_opt2)
            ids.forEachIndexed { i, vid ->
                val text = q.options.getOrNull(i)
                if (text == null) { v.setViewVisibility(vid, android.view.View.GONE); return@forEachIndexed }
                v.setViewVisibility(vid, android.view.View.VISIBLE)
                v.setTextViewText(vid, (if (answered != null && i == q.correct) "✓ " else if (answered != null && i == pick) "✗ " else "") + text)
                v.setInt(vid, "setBackgroundResource", when {
                    answered != null && i == q.correct -> R.drawable.widget_right
                    answered != null && i == pick -> R.drawable.widget_wrong
                    else -> R.drawable.widget_option
                })
                if (answered == null) {
                    val tap = Intent(context, PlayWidget::class.java).setAction(ANSWER).putExtra("option", i)
                    v.setOnClickPendingIntent(vid, PendingIntent.getBroadcast(context, 100 + i, tap, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
                } else v.setOnClickPendingIntent(vid, openApp(context, "run"))
            }
            v.setTextViewText(R.id.play_why, if (answered == null) "Tap an answer. +15 XP if you're right." else (if (answered) "Right! " else "Not quite. ") + q.why)
            v.setOnClickPendingIntent(R.id.play_root, openApp(context, "run"))
            v.setOnClickPendingIntent(R.id.play_run, openApp(context, "run"))
            manager.updateAppWidget(id, v)
        }

        private fun openApp(context: Context, route: String) = PendingIntent.getActivity(context, 77,
            Intent(context, MainActivity::class.java).putExtra(Reminders.EXTRA_ROUTE, route).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
