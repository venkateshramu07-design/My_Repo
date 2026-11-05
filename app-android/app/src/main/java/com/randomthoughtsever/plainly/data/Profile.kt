package com.randomthoughtsever.plainly.data

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Something the reader wants help with. It decides their plan, Today's picks, tips and quiz questions. */
data class Goal(val key: String, val title: String, val blurb: String, val icon: ImageVector, val from: Color, val to: Color,
                val labels: Set<String>, val words: Regex, val tool: String)

val GOALS = listOf(
    Goal("debt", "Pay off debt", "Cards, loans and a plan to be debt-free", Icons.Rounded.MoneyOff, Color(0xFF8E3B6E), Color(0xFFC25A8C),
        setOf("Personal Finance"), Regex("(?i)debt|credit card|loan|payoff|consolidat|credit score|cibil"), "n:card"),
    Goal("save", "Save & invest", "Grow your money and retire on your terms", Icons.AutoMirrored.Rounded.TrendingUp, Color(0xFF0F5F55), Color(0xFF2E9C88),
        setOf("Investing", "Personal Finance"), Regex("(?i)invest|saving|retire|fire|401|sip|lump|fund|portfolio|gold|rich|wealth|emergency"), "n:growth"),
    Goal("mind", "A calmer mind", "Stress, sleep, habits and focus", Icons.Rounded.Spa, Color(0xFF145A6B), Color(0xFF3A9AAE),
        setOf("Psychology", "Zen & Mindfulness", "Health & Wellness", "Self Improvement"), Regex("(?i)stress|sleep|habit|happy|anxiety|mind|calm|focus|burnout"), "w:bd-sleep"),
    Goal("career", "Grow my career", "Pay, interviews, AI and burnout", Icons.Rounded.Work, Color(0xFF8A4B1E), Color(0xFFC77A43),
        setOf("Career & Workplace", "Leadership", "Technology & AI"), Regex("(?i)career|job|interview|salary|raise|pay|work|ai |leader"), "w:bd-ry"),
    Goal("abroad", "Live or work abroad", "Money facts for 35+ countries", Icons.Rounded.Public, Color(0xFF0E4D64), Color(0xFF2B86A3),
        setOf("Countries & Living Abroad"), Regex("(?i)countr|abroad|visa|passport|wage|tax|retire abroad|move"), "w:bd-fx"),
    Goal("learn", "Understand the economy", "Inflation, rates, crypto and markets", Icons.Rounded.Insights, Color(0xFF4B3A8C), Color(0xFF7A5BC4),
        setOf("Economics", "Cryptocurrency"), Regex("(?i)inflation|rate|gdp|currency|bitcoin|crypto|economy|yield|price"), "w:bd-infl"),
)

/** The reader's own numbers (all optional, kept on the phone only). */
data class Numbers(val income: Double = 0.0, val spend: Double = 0.0, val savings: Double = 0.0, val invested: Double = 0.0,
                   val debt: Double = 0.0, val debtPayment: Double = 0.0) {
    val isSet get() = income > 0 || spend > 0 || savings > 0 || invested > 0 || debt > 0
    fun toJson(): JSONObject = JSONObject().put("income", income).put("spend", spend).put("savings", savings).put("invested", invested)
        .put("debt", debt).put("debtPayment", debtPayment)
    companion object {
        fun from(o: JSONObject?) = if (o == null) Numbers() else Numbers(o.optDouble("income", 0.0), o.optDouble("spend", 0.0),
            o.optDouble("savings", 0.0), o.optDouble("invested", 0.0), o.optDouble("debt", 0.0), o.optDouble("debtPayment", 0.0))
    }
}

/** A challenge the reader joined: which one, when, and the days they checked in. */
data class Joined(val key: String, val start: String, val days: Set<String>)

/**
 * Everything personal: goals, country, numbers, check-up scores, quiz answers, challenges, daily routine and the
 * community name. Stored on the phone only (SharedPreferences), never sent anywhere unless the reader posts in Community.
 */
class Profile(context: Context) {
    private val p = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
    private val shared = context.getSharedPreferences("plainly", Context.MODE_PRIVATE)

    var onboarded by mutableStateOf(p.getBoolean("onboarded", false)); private set
    var goals by mutableStateOf(p.getStringSet("goals", emptySet())!!.toSet()); private set
    var country by mutableStateOf(p.getString("country", null)); private set
    var numbers by mutableStateOf(Numbers.from(p.getString("numbers", null)?.let { runCatching { JSONObject(it) }.getOrNull() })); private set
    var name by mutableStateOf(p.getString("name", null) ?: randomName().also { p.edit().putString("name", it).apply() }); private set
    var avatar by mutableStateOf(p.getString("avatar", null) ?: AVATARS.random().also { p.edit().putString("avatar", it).apply() }); private set
    /** Bumps whenever check-ups, quizzes, challenges or the daily routine change, so screens that show them redraw. */
    var version by mutableIntStateOf(0); private set

    fun finishOnboarding(goals: Set<String>, country: String?, currency: String?, numbers: Numbers?) {
        this.goals = goals; this.country = country
        p.edit().putBoolean("onboarded", true).putStringSet("goals", goals).putString("country", country).apply()
        currency?.let { shared.edit().putString("currency", it).apply() }
        shared.edit().putString("autoDeck", LocalDate.now().toString()).apply() // no stories pop-up on the day you join
        numbers?.let { updateNumbers(it) }
        onboarded = true
    }
    fun updateGoals(g: Set<String>) { goals = g; p.edit().putStringSet("goals", g).apply() }
    fun updateNumbers(n: Numbers) { numbers = n; p.edit().putString("numbers", n.toJson().toString()).apply(); version++ }
    fun setIdentity(n: String, a: String) { name = n.trim().take(24).ifBlank { name }; avatar = a; p.edit().putString("name", name).putString("avatar", a).apply() }
    fun currency(): String = shared.getString("currency", null) ?: "USD"

    // ---- check-ups ----
    fun saveCheck(id: String, score: Int, weakest: String) {
        p.edit().putString("check:$id", JSONObject().put("score", score).put("weakest", weakest).put("date", LocalDate.now().toString()).toString()).apply(); version++
    }
    fun check(id: String): Triple<Int, String, String>? = p.getString("check:$id", null)?.let {
        runCatching { JSONObject(it).let { o -> Triple(o.getInt("score"), o.getString("weakest"), o.getString("date")) } }.getOrNull()
    }

    // ---- quizzes ----
    fun answer(quizId: String, correct: Boolean) { p.edit().putBoolean("quiz:$quizId", correct).apply(); version++ }
    fun answered(quizId: String): Boolean? = if (p.contains("quiz:$quizId")) p.getBoolean("quiz:$quizId", false) else null
    fun quizStats(): Pair<Int, Int> = p.all.filterKeys { it.startsWith("quiz:") }.values.let { v -> v.count { it == true } to v.size }

    // ---- daily routine ----
    fun dailyDone(): Set<String> = if (p.getString("dailyDate", "") == LocalDate.now().toString()) p.getStringSet("daily", emptySet())!!.toSet() else emptySet()
    fun markDaily(item: String) {
        val done = dailyDone() + item
        p.edit().putString("dailyDate", LocalDate.now().toString()).putStringSet("daily", done).apply()
        if (done.size == 3) p.edit().putInt("dailyCompleted", p.getInt("dailyCompleted", 0) + 1).apply()
        version++
    }
    fun dailyCompleted() = p.getInt("dailyCompleted", 0)
    fun breaths() = p.getInt("breaths", 0)
    fun addBreathing() { p.edit().putInt("breaths", breaths() + 1).apply() }

    // ---- challenges ----
    fun joined(): List<Joined> = runCatching {
        val a = JSONArray(p.getString("challenges", "[]"))
        List(a.length()) { i -> a.getJSONObject(i).let { o -> Joined(o.getString("key"), o.getString("start"), o.getJSONArray("days").let { d -> List(d.length()) { d.getString(it) }.toSet() }) } }
    }.getOrDefault(emptyList())
    private fun saveJoined(list: List<Joined>) {
        p.edit().putString("challenges", JSONArray(list.map { j -> JSONObject().put("key", j.key).put("start", j.start).put("days", JSONArray(j.days.toList())) }).toString()).apply(); version++
    }
    fun join(key: String) { if (joined().none { it.key == key }) saveJoined(joined() + Joined(key, LocalDate.now().toString(), emptySet())) }
    fun leave(key: String) = saveJoined(joined().filterNot { it.key == key })
    fun checkIn(key: String) = saveJoined(joined().map { if (it.key == key) it.copy(days = it.days + LocalDate.now().toString()) else it })
    fun finishedChallenges() = p.getInt("challengesDone", 0)
    fun completeChallenge(key: String) { p.edit().putInt("challengesDone", finishedChallenges() + 1).apply(); leave(key) }

    companion object {
        val AVATARS = listOf("🦊", "🐼", "🦉", "🐢", "🐬", "🦁", "🐨", "🐙", "🦋", "🐝", "🌵", "🍀", "🌻", "⭐", "🚀", "🎯")
        private val ADJ = listOf("Calm", "Bright", "Bold", "Clever", "Steady", "Lucky", "Swift", "Wise", "Brave", "Sunny", "Quiet", "Keen")
        private val NOUN = listOf("Otter", "Falcon", "Maple", "River", "Comet", "Fox", "Panda", "Owl", "Cedar", "Harbor", "Pebble", "Lark")
        fun randomName() = "${ADJ.random()} ${NOUN.random()} ${(10..99).random()}"
    }
}
