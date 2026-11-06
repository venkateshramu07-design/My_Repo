package com.randomthoughtsever.plainly.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** A short reference to an article, for the Saved and Recently read lists. */
data class Ref(val id: String, val title: String, val image: String?, val label: String, val minutes: Int) {
    fun toJson(): JSONObject = JSONObject().put("id", id).put("title", title).put("image", image ?: "").put("label", label).put("minutes", minutes)
    companion object {
        fun of(p: Post) = Ref(p.id, p.title, p.image, p.label, p.minutes)
        fun fromJson(o: JSONObject) = Ref(o.getString("id"), o.getString("title"), o.optString("image").ifBlank { null }, o.optString("label"), o.optInt("minutes", 3))
    }
}

/** Everything the app keeps on the phone: saved articles (with full text, for offline reading), history and settings. */
class Library(context: Context) {
    private val prefs = context.getSharedPreferences("plainly", Context.MODE_PRIVATE)
    private val savedDir = File(context.filesDir, "saved").apply { mkdirs() }

    val saved = mutableStateListOf<Ref>().apply { addAll(read("saved")) }
    val history = mutableStateListOf<Ref>().apply { addAll(read("history")) }
    var textZoom by mutableIntStateOf(prefs.getInt("textZoom", 100))
        private set
    var theme by mutableStateOf(prefs.getString("theme", "system") ?: "system")
        private set

    fun isSaved(id: String) = saved.any { it.id == id }

    fun toggleSave(p: Post) {
        if (isSaved(p.id)) {
            saved.removeAll { it.id == p.id }
            File(savedDir, "${p.id}.json").delete()
        } else {
            saved.add(0, Ref.of(p))
            File(savedDir, "${p.id}.json").writeText(p.toJson().toString())
        }
        write("saved", saved)
    }

    fun savedPost(id: String): Post? = File(savedDir, "$id.json").takeIf { it.exists() }
        ?.let { runCatching { Post.fromJson(JSONObject(it.readText())) }.getOrNull() }

    fun opened(p: Post) {
        history.removeAll { it.id == p.id }
        history.add(0, Ref.of(p))
        while (history.size > 30) history.removeAt(history.lastIndex)
        write("history", history)
        prefs.edit().putInt("reads", prefs.getInt("reads", 0) + 1).putStringSet("readIds", readIds() + p.id).apply()
    }

    fun clearHistory() { history.clear(); write("history", history) }

    fun setZoom(z: Int) { textZoom = z.coerceIn(80, 150); prefs.edit().putInt("textZoom", textZoom).apply() }
    fun setThemeMode(t: String) { theme = t; prefs.edit().putString("theme", t).apply() }

    // Counters for asking for a Play Store rating at a good moment.
    fun countToolUse() = prefs.edit().putInt("toolUses", prefs.getInt("toolUses", 0) + 1).apply()
    /** Days in a row the app has been opened (or covered by a streak freeze), ending today. */
    fun streak(): Int {
        val days = keptDays()
        var d = java.time.LocalDate.now(); var n = 0
        while (d.toString() in days) { n++; d = d.minusDays(1) }
        return n
    }
    private fun markToday() {
        val today = java.time.LocalDate.now().toString()
        val days = (prefs.getString("days", "").orEmpty().split(',').filter { it.isNotBlank() } + today).distinct().takeLast(400)
        prefs.edit().putString("days", days.joinToString(",")).apply()
    }
    fun openDays(): Set<String> = prefs.getString("days", "").orEmpty().split(',').filter { it.isNotBlank() }.toSet()

    // ---------- streak freezes ----------
    /** Days missed but covered by a streak freeze. */
    fun frozenDays(): Set<String> = prefs.getStringSet("frozen", emptySet())!!.toSet()
    /** Days that count for the streak: opened, or frozen. */
    fun keptDays(): Set<String> = openDays() + frozenDays()
    fun freezes() = prefs.getInt("freezes", 0)
    /** The day a freeze last saved the streak (for a one-time "your streak was saved" note). */
    fun freezeUsedOn(): String? = prefs.getString("freezeUsed", null)
    fun clearFreezeNote() = prefs.edit().remove("freezeUsed").apply()

    /** On opening: if the reader missed a day or two and has freezes, they cover the gap so the streak survives. */
    private fun useFreezes() {
        val today = java.time.LocalDate.now()
        val (fill, _) = coverGap(keptDays(), freezes(), today)
        if (fill.isEmpty()) return
        prefs.edit().putStringSet("frozen", frozenDays() + fill.map { it.toString() }).putInt("freezes", freezes() - fill.size)
            .putString("freezeUsed", today.toString()).apply()
    }
    /** Every guide ever opened (ids), for the knowledge map and plan progress. */
    fun readIds(): Set<String> = prefs.getStringSet("readIds", emptySet())!!.toSet()
    fun toolUses() = prefs.getInt("toolUses", 0)
    fun firstOpen() = prefs.getLong("firstOpen", System.currentTimeMillis())
    fun reads() = prefs.getInt("reads", 0)

    fun countOpen() {
        useFreezes()
        markToday()
        prefs.edit().putInt("opens", prefs.getInt("opens", 0) + 1)
            .apply { if (!prefs.contains("firstOpen")) putLong("firstOpen", System.currentTimeMillis()) }.apply()
    }
    fun readyForReview(): Boolean {
        if (prefs.getBoolean("reviewAsked", false)) return false
        val days = (System.currentTimeMillis() - prefs.getLong("firstOpen", System.currentTimeMillis())) / 86_400_000L
        return prefs.getInt("toolUses", 0) >= 2 || prefs.getInt("reads", 0) >= 4 || (prefs.getInt("opens", 0) >= 5 && days >= 3)
    }
    fun reviewAsked() = prefs.edit().putBoolean("reviewAsked", true).apply()

    private fun read(key: String): List<Ref> = runCatching {
        val a = JSONArray(prefs.getString(key, "[]"))
        List(a.length()) { Ref.fromJson(a.getJSONObject(it)) }
    }.getOrDefault(emptyList())

    private fun write(key: String, list: List<Ref>) =
        prefs.edit().putString(key, JSONArray().apply { list.forEach { put(it.toJson()) } }.toString()).apply()

    companion object {
        const val MAX_FREEZES = 2
        /**
         * Which missed days a freeze should cover when the reader opens the app on [today]: the gap since the last kept
         * day, if it's no longer than the freezes they have and the streak before it was worth saving (2+ days). Pure.
         */
        fun coverGap(kept: Set<String>, freezes: Int, today: java.time.LocalDate): Pair<List<java.time.LocalDate>, Int> {
            if (freezes <= 0 || today.toString() in kept || today.minusDays(1).toString() in kept) return emptyList<java.time.LocalDate>() to 0
            val last = (2L..(freezes + 1L)).map { today.minusDays(it) }.firstOrNull { it.toString() in kept } ?: return emptyList<java.time.LocalDate>() to 0
            var run = 0; var d = last; while (d.toString() in kept) { run++; d = d.minusDays(1) }
            if (run < 2) return emptyList<java.time.LocalDate>() to 0
            val gap = generateSequence(last.plusDays(1)) { it.plusDays(1) }.takeWhile { it.isBefore(today) }.toList()
            return gap to run
        }
    }
}
