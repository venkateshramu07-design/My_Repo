package com.randomthoughtsever.plainly

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.randomthoughtsever.plainly.data.Library
import com.randomthoughtsever.plainly.data.NewPosts
import org.json.JSONArray
import java.io.File
import java.time.LocalDate

/** Home-screen widget: a tip of the day (a guide's quick answer, changing daily) and the reader's streak. Tap to read. */
class PlainlyWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = ids.forEach { update(context, manager, it) }

    companion object {
        /** Refreshes every Plainly widget on the home screen (called when the app opens). */
        fun refresh(context: Context) {
            val m = AppWidgetManager.getInstance(context)
            m.getAppWidgetIds(ComponentName(context, PlainlyWidget::class.java)).forEach { update(context, m, it) }
        }

        private fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val tip = tipOfTheDay(context)
            val streak = runCatching { Library(context).streak() }.getOrDefault(0)
            val v = RemoteViews(context.packageName, R.layout.widget_plainly)
            v.setTextViewText(R.id.widget_title, tip?.second ?: "Plainly: Money, Mind & Work")
            v.setTextViewText(R.id.widget_text, tip?.third ?: "Open Plainly for today's guides, tools and your Daily 3.")
            v.setTextViewText(R.id.widget_streak, if (streak > 0) "🔥 $streak" else "")
            val open = Intent(context, MainActivity::class.java).apply { tip?.first?.let { putExtra(NewPosts.EXTRA_POST, it) }; addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            v.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, 42, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            manager.updateAppWidget(id, v)
        }

        /** From the guide index saved by the app (id, title, quick answer): an article, different each day. */
        private fun tipOfTheDay(context: Context): Triple<String, String, String>? = runCatching {
            val a = JSONArray(File(context.filesDir, "index.json").readText())
            val usable = (0 until a.length()).map { a.getJSONObject(it) }.filter { o ->
                val labels = o.getJSONArray("labels").let { l -> List(l.length()) { l.getString(it) } }
                o.getString("quick").length > 80 && labels.none { it == "Tools" || it == "Calculators" } && !Regex("(?i)calculator|resize|compress").containsMatchIn(o.getString("title"))
            }
            val o = usable[(LocalDate.now().toEpochDay() % usable.size).toInt()]
            Triple(o.getString("id"), o.getString("title"), o.getString("quick"))
        }.getOrNull()
    }
}
