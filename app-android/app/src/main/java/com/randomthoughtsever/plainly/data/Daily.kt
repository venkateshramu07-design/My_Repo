package com.randomthoughtsever.plainly.data

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.IsoFields

/** A small real-world step for the Daily Run, tied to a goal. One per day, from the reader's goals. */
data class MicroAction(val text: String, val why: String, val goal: String, val guides: Regex)

val MICRO_ACTIONS = listOf(
    MicroAction("Check one subscription and cancel it if you don't use it", "Forgotten subscriptions are the easiest money to get back.", "save", Regex("(?i)subscription|budget")),
    MicroAction("Move a small amount to savings right now, even 1%", "Starting matters more than the amount.", "save", Regex("(?i)sav|emergency")),
    MicroAction("Look up the interest rate on your biggest debt", "You can't beat a rate you don't know.", "debt", Regex("(?i)debt|credit card|loan")),
    MicroAction("Set up autopay for at least the minimum on one card", "A missed payment costs fees and score points.", "debt", Regex("(?i)credit|debt")),
    MicroAction("Write down three things you spent on yesterday", "Noticing spending is the first step of every budget.", "save", Regex("(?i)budget|spend")),
    MicroAction("Unsubscribe from one shop's emails", "Fewer offers, fewer impulse buys.", "save", Regex("(?i)budget|spend|save")),
    MicroAction("Do one minute of slow breathing: in 4, out 6", "Longer out-breaths slow your heart.", "mind", Regex("(?i)stress|calm|anxiety")),
    MicroAction("Put your phone in another room for 30 minutes", "Distance makes focus easier than willpower.", "mind", Regex("(?i)focus|habit|deep work")),
    MicroAction("Go to bed 15 minutes earlier tonight", "Small sleep gains add up quickly.", "mind", Regex("(?i)sleep")),
    MicroAction("Drink a glass of water before your next coffee", "Tiny habits stick when tied to an existing one.", "mind", Regex("(?i)habit|health")),
    MicroAction("Write one sentence about something that went well today", "It trains attention towards what's working.", "mind", Regex("(?i)happy|mind|gratitude")),
    MicroAction("Add one result with a number to your CV", "Numbers make achievements believable.", "career", Regex("(?i)cv|resume|career|interview")),
    MicroAction("Message one former colleague just to say hi", "Most jobs come through people you already know.", "career", Regex("(?i)career|job|network")),
    MicroAction("Spend 15 minutes trying an AI tool used in your job", "People who use AI well are the ones it helps.", "career", Regex("(?i)ai |career|work")),
    MicroAction("Block one hour in your calendar for focused work", "Deep work needs protected time.", "career", Regex("(?i)deep work|focus|productiv")),
    MicroAction("Look up what your role pays in your city", "Knowing your market rate is half a raise conversation.", "career", Regex("(?i)salary|raise|pay")),
    MicroAction("Check the visa or residency rules for one country you'd like to live in", "Rules change often; checking early avoids surprises.", "abroad", Regex("(?i)visa|abroad|countr")),
    MicroAction("Compare what your money is worth in another currency today", "Exchange rates move more than people expect.", "abroad", Regex("(?i)currency|exchange|abroad")),
    MicroAction("Read the inflation rate for your country this month", "Your savings need to beat it to grow.", "learn", Regex("(?i)inflation")),
    MicroAction("Explain one money word to someone (or to yourself out loud)", "Teaching is the fastest way to understand.", "learn", Regex("(?i)explained|what is")),
)

fun actionOfTheDay(goals: Set<String>, day: Long = LocalDate.now().toEpochDay()): MicroAction {
    val pool = MICRO_ACTIONS.filter { it.goal in goals }.ifEmpty { MICRO_ACTIONS }
    return pool[(day % pool.size).toInt()]
}

/** A seasonal event: a few days a year with a theme, a banner, a related challenge and 50% more XP. */
data class Season(val key: String, val title: String, val blurb: String, val emoji: String, val from: MonthDay, val to: MonthDay,
                  val challenge: String?, val guides: Regex, val c1: Color, val c2: Color)

object Seasons {
    val ALL = listOf(
        Season("newyear", "New Year money reset", "Fresh start: set one money goal and a habit for the year.", "🎆", MonthDay.of(1, 1), MonthDay.of(1, 14),
            "nospend", Regex("(?i)budget|goal|habit|save"), Color(0xFF3B2F6B), Color(0xFFD9A75E)),
        Season("love", "Love & money week", "Talk money with someone you love, without a fight.", "💞", MonthDay.of(2, 8), MonthDay.of(2, 16),
            null, Regex("(?i)love|relationship|couple"), Color(0xFF7D2333), Color(0xFFCC4D63)),
        Season("tax", "Tax season sprint", "Get your papers in order and find the credits you're owed.", "🧾", MonthDay.of(3, 25), MonthDay.of(4, 15),
            null, Regex("(?i)tax|credit|refund"), Color(0xFF0F5F55), Color(0xFF2E9C88)),
        Season("calm", "Mental health week", "Seven days of small calm: breathing, sleep and screen breaks.", "🌿", MonthDay.of(5, 10), MonthDay.of(5, 18),
            "calm7", Regex("(?i)stress|sleep|calm|anxiety|burnout"), Color(0xFF145A6B), Color(0xFF3A9AAE)),
        Season("midyear", "Mid-year check-up", "Halfway through the year: check your money, mind and career.", "🧭", MonthDay.of(6, 25), MonthDay.of(7, 7),
            null, Regex("(?i)budget|career|goal"), Color(0xFF1F4E79), Color(0xFF3B7BB8)),
        Season("career", "Career boost fortnight", "Two weeks of small steps for your career.", "🚀", MonthDay.of(9, 1), MonthDay.of(9, 14),
            "career14", Regex("(?i)career|job|interview|raise"), Color(0xFF8A4B1E), Color(0xFFC77A43)),
        Season("wise", "Wise spending week", "Big sales are coming. Plan before you buy.", "🛍️", MonthDay.of(11, 20), MonthDay.of(12, 2),
            "nospend", Regex("(?i)budget|spend|debt|credit card"), Color(0xFF7A4E0C), Color(0xFFD69A2E)),
        Season("holiday", "Holiday budget", "Enjoy the season without a January money hangover.", "🎁", MonthDay.of(12, 10), MonthDay.of(12, 24),
            null, Regex("(?i)budget|gift|spend|save"), Color(0xFF2E5E2A), Color(0xFF5FA35A)),
        Season("review", "Year in review", "Look back at your year and plan the next one.", "✨", MonthDay.of(12, 26), MonthDay.of(12, 31),
            null, Regex("(?i)goal|habit|plan"), Color(0xFF0A1B2F), Color(0xFFD9A75E)),
    )
    fun current(d: LocalDate = LocalDate.now()): Season? = MonthDay.from(d).let { m -> ALL.firstOrNull { !m.isBefore(it.from) && !m.isAfter(it.to) } }
    fun next(d: LocalDate = LocalDate.now()): Season = MonthDay.from(d).let { m -> ALL.firstOrNull { it.from.isAfter(m) } ?: ALL.first() }
}

/** The community's question of the week: the same for everyone this week, answered as a comment on its guide. */
data class WeekQuestion(val text: String, val post: String)

/** Each question sits under the guide (a quiz's guide id, all verified) it's about. */
val WEEK_QUESTIONS: List<WeekQuestion> = listOf(
    "How many months of expenses do you keep in your emergency fund, and how did you build it?" to "efund",
    "What's one thing you'd tell your younger self about retiring early?" to "fire25",
    "Which habit has helped your sleep the most?" to "sleep",
    "What's the tiniest habit that actually stuck for you?" to "habit",
    "How do you explain a career gap? Share what worked." to "gap",
    "Did you get a raise this year? What helped you ask?" to "raise",
    "How has AI changed your job so far?" to "aijobs",
    "When do you do your best focused work?" to "deep",
    "Rent or buy: what did you choose, and would you do it again?" to "rentbuy",
    "Which card or loan was hardest to pay off, and how did you do it?" to "credit",
    "What surprised you most about the cost of living where you live?" to "keepoecd",
    "What's your love language, and does it affect how you spend?" to "love",
    "Inflation: what price rise have you noticed the most this year?" to "prices",
).map { (q, quiz) -> WeekQuestion(q, QUIZZES.first { it.id == quiz }.post) }

fun questionOfTheWeek(d: LocalDate = LocalDate.now()): WeekQuestion {
    val n = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + d.get(IsoFields.WEEK_BASED_YEAR) * 53
    return WEEK_QUESTIONS[n % WEEK_QUESTIONS.size]
}
