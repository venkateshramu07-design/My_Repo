package com.randomthoughtsever.plainly.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Everything that makes Plainly feel like a game, kept on the phone only: XP and levels, the rewards they unlock, the
 * Daily Run, daily check-ins, the notebook, "I tried this", collectible guide cards, daily puzzle results, the money
 * simulator's best run and the weekly league. No server: every number here is the reader's own.
 */
class Progress(context: Context, private val today: () -> LocalDate = { LocalDate.now() }) {
    private val p: SharedPreferences = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    /** Bumps on every change so screens redraw. */
    var version by mutableIntStateOf(0); private set
    var xp by mutableIntStateOf(p.getInt("xp", 0)); private set
    /** The last XP award, for the little "+20 XP" toast. Cleared by the UI after showing it. */
    var lastAward by mutableStateOf<Award?>(null)
    /** Set when an award takes the reader to a new level, for the level-up celebration. */
    var levelUp by mutableStateOf<Int?>(null)

    /** Picks up XP added outside the app (the widget). */
    fun reload() { val x = p.getInt("xp", 0); if (x != xp) { xp = x; version++ } }

    data class Award(val amount: Int, val reason: String, val at: Long = System.currentTimeMillis())

    // ---------- XP ----------

    /**
     * Gives [amount] XP once per [key] (a key with today's date in it can be earned again tomorrow). Returns whether it
     * was awarded. [stat] also counts the action in today's log, for recaps.
     */
    fun award(key: String, amount: Int, reason: String, stat: String? = null): Boolean {
        val keys = p.getStringSet("keys", emptySet())!!
        if (key in keys) return false
        val before = level()
        val date = today().toString()
        val gain = (amount * multiplier()).toInt()
        xp = p.getInt("xp", 0) + gain // read fresh: the home-screen widget may have added XP meanwhile
        val days = dayXp().apply { put(date, optInt(date, 0) + gain) }
        p.edit().putInt("xp", xp).putStringSet("keys", (keys + key).let { if (it.size > 4000) it.drop(it.size - 4000).toSet() else it })
            .putString("dayXp", trim(days).toString()).apply()
        stat?.let { count(it) }
        lastAward = Award(gain, reason)
        if (level() > before) levelUp = level()
        version++
        return true
    }
    fun has(key: String) = key in p.getStringSet("keys", emptySet())!!
    /** Seasonal events give 50% more XP while they run. */
    fun multiplier(): Double = if (Seasons.current(today()) != null) 1.5 else 1.0

    fun level(x: Int = xp): Int { var l = 1; while (x >= need(l + 1)) l++; return l }
    /** XP needed to reach level [l]: 0, 100, 300, 600, 1000, 1500… */
    fun need(l: Int) = 50 * l * (l - 1)
    fun levelProgress(): Float { val l = level(); return ((xp - need(l)).toFloat() / (need(l + 1) - need(l))).coerceIn(0f, 1f) }
    fun title(l: Int = level()) = TITLES[(l - 1).coerceIn(0, TITLES.lastIndex)]

    private fun dayXp() = runCatching { JSONObject(p.getString("dayXp", "{}")!!) }.getOrDefault(JSONObject())
    fun xpOn(d: LocalDate) = dayXp().optInt(d.toString(), 0)
    fun weekStart(d: LocalDate = today()): LocalDate = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    fun weekXp(start: LocalDate = weekStart()) = (0L until 7).sumOf { xpOn(start.plusDays(it)) }

    // ---------- leagues (personal: against your own weeks) ----------

    data class League(val name: String, val emoji: String, val from: Int)
    fun league(weekXp: Int = weekXp()) = LEAGUES.last { weekXp >= it.from }
    fun nextLeague(weekXp: Int = weekXp()) = LEAGUES.firstOrNull { it.from > weekXp }
    var weeklyGoal: Int
        get() = p.getInt("weeklyGoal", 300)
        set(v) { p.edit().putInt("weeklyGoal", v).apply(); version++ }
    /** Best full week before this one. */
    fun bestWeek(): Int {
        val start = weekStart()
        return (1L..52L).maxOfOrNull { weekXp(start.minusWeeks(it)) } ?: 0
    }
    fun lastWeek() = weekXp(weekStart().minusWeeks(1))

    // ---------- rewards ----------

    data class Reward(val level: Int, val kind: String, val key: String, val title: String)
    fun rewards() = REWARDS
    fun unlocked(r: Reward) = level() >= r.level
    fun avatars(): List<String> = Profile.AVATARS + REWARDS.filter { it.kind == "avatars" && unlocked(it) }.flatMap { AVATAR_PACKS[it.key].orEmpty() }
    var accent: String
        get() = p.getString("accent", "navy")!!.takeIf { a -> a == "navy" || REWARDS.any { it.key == a && unlocked(it) } } ?: "navy"
        set(v) { p.edit().putString("accent", v).apply(); version++ }
    var frame: String
        get() = p.getString("frame", "")!!.takeIf { f -> REWARDS.any { it.key == f && unlocked(it) } } ?: ""
        set(v) { p.edit().putString("frame", v).apply(); version++ }

    // ---------- the daily log (for recaps) ----------

    private fun log() = runCatching { JSONObject(p.getString("log", "{}")!!) }.getOrDefault(JSONObject())
    fun count(stat: String, by: Int = 1, d: LocalDate = today()) {
        val l = log(); val day = l.optJSONObject(d.toString()) ?: JSONObject()
        day.put(stat, day.optInt(stat, 0) + by); l.put(d.toString(), day)
        p.edit().putString("log", trim(l).toString()).apply(); version++
    }
    fun stat(stat: String, d: LocalDate) = log().optJSONObject(d.toString())?.optInt(stat, 0) ?: 0
    fun statIn(stat: String, from: LocalDate, days: Int) = (0L until days).sumOf { stat(stat, from.plusDays(it)) }
    fun activeDays(from: LocalDate, days: Int) = (0L until days).count { xpOn(from.plusDays(it)) > 0 }

    // ---------- Daily Run ----------

    fun runDone(d: LocalDate = today()): Set<String> = p.getStringSet("run:$d", emptySet())!!.toSet()
    fun runStep(step: String) {
        val done = runDone() + step
        p.edit().putStringSet("run:${today()}", done).apply(); version++
        if (done.containsAll(RUN_STEPS) && award("run:${today()}", 50, "Daily Run complete", "runs")) {
            p.edit().putInt("runs", runs() + 1).apply()
        }
    }
    fun runs() = p.getInt("runs", 0)
    /** Days in a row with a finished Daily Run, ending today or yesterday. */
    fun runStreak(): Int {
        var d = today(); if (!runDone(d).containsAll(RUN_STEPS)) d = d.minusDays(1)
        var n = 0; while (runDone(d).containsAll(RUN_STEPS)) { n++; d = d.minusDays(1) }
        return n
    }

    // ---------- daily check-in ----------

    data class CheckIn(val mood: Int, val spend: Int, val note: String)
    fun checkIn(d: LocalDate = today()): CheckIn? = p.getString("ci:$d", null)?.let {
        runCatching { JSONObject(it).let { o -> CheckIn(o.getInt("mood"), o.getInt("spend"), o.optString("note")) } }.getOrNull()
    }
    fun saveCheckIn(c: CheckIn) {
        p.edit().putString("ci:${today()}", JSONObject().put("mood", c.mood).put("spend", c.spend).put("note", c.note.take(80)).toString()).apply()
        award("checkin:${today()}", 10, "Daily check-in", "checkins"); version++
    }

    // ---------- notebook ----------

    data class Note(val text: String, val postId: String, val title: String, val date: String)
    fun notes(): List<Note> = arr("notes").map { Note(it.getString("text"), it.getString("post"), it.getString("title"), it.getString("date")) }
    fun addNote(text: String, postId: String, title: String) {
        val t = text.trim().take(600); if (t.length < 3 || notes().any { it.text == t }) return
        put("notes", listOf(JSONObject().put("text", t).put("post", postId).put("title", title).put("date", today().toString())) + arr("notes"))
        award("note:${t.hashCode()}", 5, "Saved to your notebook", "notes")
    }
    fun removeNote(n: Note) = put("notes", arr("notes").filterNot { it.getString("text") == n.text })

    // ---------- I tried this ----------

    data class Tried(val postId: String, val title: String, val date: String, val result: String)
    fun tried(): List<Tried> = arr("tried").map { Tried(it.getString("post"), it.getString("title"), it.getString("date"), it.optString("result")) }
    fun isTrying(postId: String) = tried().any { it.postId == postId }
    fun startTrying(postId: String, title: String) {
        if (isTrying(postId)) return
        put("tried", listOf(JSONObject().put("post", postId).put("title", title).put("date", today().toString()).put("result", "")) + arr("tried"))
        award("try:$postId", 15, "You're trying it", "tries")
    }
    /** Something tried at least 3 days ago that hasn't been answered yet: "Did it work?" */
    fun followUp(): Tried? = tried().firstOrNull { it.result.isBlank() && runCatching { LocalDate.parse(it.date).plusDays(3) <= today() }.getOrDefault(false) }
    fun snoozeTried(postId: String) = put("tried", arr("tried").map { if (it.getString("post") == postId) it.put("date", today().toString()) else it })
    fun answerTried(postId: String, result: String) {
        put("tried", arr("tried").map { if (it.getString("post") == postId) it.put("result", result) else it })
        award("tried:$postId", 20, if (result == "yes") "It worked!" else "Thanks for checking in")
    }

    // ---------- collectible guide cards ----------

    data class Card(val postId: String, val title: String, val label: String, val rarity: Int, val date: String)
    fun cards(): List<Card> = arr("cards").map { Card(it.getString("post"), it.getString("title"), it.getString("label"), it.getInt("rarity"), it.getString("date")) }
    fun card(postId: String) = cards().firstOrNull { it.postId == postId }
    /** Collects (or upgrades) a guide's card. Returns the card if it's new or rarer than before. */
    fun collect(postId: String, title: String, label: String, rarity: Int): Card? {
        val old = card(postId)
        if (old != null && old.rarity >= rarity) return null
        val c = Card(postId, title, label, rarity, today().toString())
        put("cards", listOf(JSONObject().put("post", postId).put("title", title).put("label", label).put("rarity", rarity).put("date", c.date)) +
            arr("cards").filterNot { it.getString("post") == postId })
        award("card:$postId:$rarity", listOf(15, 30, 60)[rarity.coerceIn(0, 2)], "${RARITY[rarity]} card", "cards")
        return c
    }

    // ---------- daily puzzle ----------

    data class PuzzleResult(val solved: Boolean, val score: Int, val grid: String)
    fun puzzle(d: LocalDate = today()): PuzzleResult? = p.getString("pz:$d", null)?.let {
        runCatching { JSONObject(it).let { o -> PuzzleResult(o.getBoolean("solved"), o.getInt("score"), o.getString("grid")) } }.getOrNull()
    }
    fun savePuzzle(r: PuzzleResult) {
        if (puzzle() != null) return
        p.edit().putString("pz:${today()}", JSONObject().put("solved", r.solved).put("score", r.score).put("grid", r.grid).toString()).apply()
        award("puzzle:${today()}", if (r.solved) 30 else 10, if (r.solved) "Daily puzzle solved" else "Daily puzzle played", "puzzles")
        runStep("puzzle")
    }
    fun puzzleStreak(): Int {
        var d = today(); if (puzzle(d)?.solved != true) d = d.minusDays(1)
        var n = 0; while (puzzle(d)?.solved == true) { n++; d = d.minusDays(1) }
        return n
    }

    // ---------- money simulator ----------

    fun simBest() = p.getInt("simBest", -1)
    fun simRuns() = p.getInt("simRuns", 0)
    fun saveSim(score: Int) {
        p.edit().putInt("simRuns", simRuns() + 1).apply { if (score > simBest()) putInt("simBest", score) }.apply()
        award("sim:${today()}", 30, "Finished a simulated year", "sims"); version++
    }

    // ---------- when the reader usually opens the app (for reminders) ----------

    fun recordOpen(hour: Int) {
        val hours = (p.getString("hours", "").orEmpty().split(',').filter { it.isNotBlank() } + hour.toString()).takeLast(30)
        p.edit().putString("hours", hours.joinToString(",")).apply()
    }
    /** The middle of the reader's recent opening hours, kept between 9:00 and 21:00; 19:00 until we know. */
    fun usualHour(): Int {
        val h = p.getString("hours", "").orEmpty().split(',').mapNotNull { it.toIntOrNull() }.sorted()
        return if (h.size < 3) 19 else h[h.size / 2].coerceIn(9, 21)
    }
    var lastNudge: String
        get() = p.getString("lastNudge", "")!!
        set(v) { p.edit().putString("lastNudge", v).apply() }

    // ---------- calm ----------

    fun calmMinutes() = p.getInt("calmMin", 0)
    fun addCalm(minutes: Int) {
        if (minutes <= 0) return
        p.edit().putInt("calmMin", calmMinutes() + minutes).apply(); count("calm", minutes)
        award("calm:${today()}:${calmMinutes() / 5}", 10, "Calm minutes")
    }

    // ---------- seasonal events ----------

    fun joinedEvent(key: String) = has("event:$key")
    fun events(): Int = p.getStringSet("keys", emptySet())!!.count { it.startsWith("event:") }

    // ---------- helpers ----------

    private fun arr(k: String): List<JSONObject> = runCatching { JSONArray(p.getString(k, "[]")).let { a -> List(a.length()) { a.getJSONObject(it) } } }.getOrDefault(emptyList())
    private fun put(k: String, list: List<JSONObject>) { p.edit().putString(k, JSONArray(list.take(500)).toString()).apply(); version++ }
    private fun trim(o: JSONObject): JSONObject {
        val keep = o.keys().asSequence().toList().sorted().takeLast(400).toSet()
        return JSONObject().apply { keep.forEach { put(it, o.get(it)) } }
    }

    companion object {
        val TITLES = listOf("Newcomer", "Curious", "Explorer", "Learner", "Saver", "Planner", "Strategist", "Navigator", "Expert", "Mentor", "Master", "Legend")
        val RARITY = listOf("Common", "Rare", "Legendary")
        val RUN_STEPS = listOf("story", "quiz", "action", "puzzle", "checkin")
        val LEAGUES = listOf(League("Bronze", "🥉", 0), League("Silver", "🥈", 200), League("Gold", "🥇", 500),
            League("Platinum", "💠", 1000), League("Diamond", "💎", 2000))
        val AVATAR_PACKS = mapOf("pack1" to listOf("🐯", "🦄", "🐳", "🦜"), "pack2" to listOf("👑", "🧠", "💎", "🌈"), "pack3" to listOf("🐉", "🪐", "🔥", "🏆"))
        val REWARDS = listOf(
            Reward(2, "avatars", "pack1", "New emojis: 🐯 🦄 🐳 🦜"), Reward(3, "accent", "emerald", "Emerald colour theme"),
            Reward(4, "frame", "gold", "Gold profile ring"), Reward(5, "accent", "plum", "Plum colour theme"),
            Reward(6, "avatars", "pack2", "New emojis: 👑 🧠 💎 🌈"), Reward(7, "accent", "sunset", "Sunset colour theme"),
            Reward(8, "frame", "aurora", "Aurora profile ring"), Reward(9, "avatars", "pack3", "New emojis: 🐉 🪐 🔥 🏆"),
            Reward(10, "accent", "midnight", "Midnight gold theme"), Reward(12, "frame", "legend", "Legend profile ring"),
        )
    }
}
