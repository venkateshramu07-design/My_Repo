package com.randomthoughtsever.plainly.data

/** A one-minute check-up: a few questions, each scoring 0-4 for one area, giving a score out of 100 and the weakest area. */
data class Option(val text: String, val points: Int)
data class Question(val area: String, val text: String, val options: List<Option>)
/** What to read and use for an area: a pattern for guide titles, and a tool key from the catalogue. */
data class Fix(val words: Regex, val tool: String?)
data class Check(val id: String, val title: String, val blurb: String, val questions: List<Question>, val fixes: Map<String, Fix>) {
    val areas get() = questions.map { it.area }.distinct()

    /** Score out of 100 and each area's share (0-1), from the chosen option index per question. */
    fun score(answers: List<Int>): Pair<Int, Map<String, Float>> {
        val byArea = questions.mapIndexed { i, q -> q.area to q.options[answers[i]].points }.groupBy({ it.first }, { it.second })
        val areas = byArea.mapValues { (_, pts) -> pts.sum() / (pts.size * 4f) }
        val total = questions.indices.sumOf { questions[it].options[answers[it]].points }
        return Math.round(total * 100f / (questions.size * 4)) to areas
    }

    fun weakest(areas: Map<String, Float>): String = areas.minByOrNull { it.value }?.key ?: this.areas.first()
}

fun verdict(score: Int) = when { score >= 90 -> "Excellent"; score >= 70 -> "Strong"; score >= 40 -> "Getting there"; else -> "Needs care" }

val CHECKS = listOf(
    Check("money", "Money Health Check", "Your money score out of 100, and the one thing to fix first", listOf(
        Question("Safety net", "If your income stopped today, how long could you cover your essentials?",
            listOf(Option("Less than a month", 0), Option("1 to 2 months", 1), Option("3 to 5 months", 3), Option("6 months or more", 4))),
        Question("Debt", "How do you deal with your credit card bill?",
            listOf(Option("I pay it in full every month", 4), Option("I don't use credit cards", 4), Option("More than the minimum", 2),
                Option("Only the minimum", 1), Option("I've missed payments", 0))),
        Question("Saving", "How much of your income do you save each month?",
            listOf(Option("Nothing yet", 0), Option("Under 5%", 1), Option("5% to 15%", 3), Option("More than 15%", 4))),
        Question("Investing", "Do you invest for the long term (funds, a pension, a 401(k))?",
            listOf(Option("No", 0), Option("I'm planning to", 1), Option("Now and then", 2), Option("Every month, automatically", 4))),
        Question("Budget", "Do you know where your money goes each month?",
            listOf(Option("No idea", 0), Option("Roughly", 2), Option("Yes, I track it", 4))),
        Question("Protection", "If you fell ill, or someone depends on your income, are you covered?",
            listOf(Option("Not really", 0), Option("Partly", 2), Option("Yes: health and life cover", 4))),
    ), mapOf(
        "Safety net" to Fix(Regex("(?i)emergency fund|keep cash|bank fails"), "n:emergency"),
        "Debt" to Fix(Regex("(?i)credit card debt|pay off|debt|consolidat"), "n:card"),
        "Saving" to Fix(Regex("(?i)save|saving|rich|budget|wealth"), "n:growth"),
        "Investing" to Fix(Regex("(?i)invest|sip|lump|401|portfolio|fire"), "n:growth"),
        "Budget" to Fix(Regex("(?i)budget|subscription|groceries|money moves|beginners"), "w:bd-sub"),
        "Protection" to Fix(Regex("(?i)insurance|annuit"), "w:bd-li"),
    )),
    Check("mind", "Stress Check", "How you're really doing, and one small thing that helps", listOf(
        Question("Sleep", "How do you usually sleep?",
            listOf(Option("Badly, most nights", 0), Option("It's hit and miss", 2), Option("Well, most nights", 4))),
        Question("Stress", "How often do you feel overwhelmed?",
            listOf(Option("Almost every day", 0), Option("A few times a week", 1), Option("Now and then", 3), Option("Rarely", 4))),
        Question("Focus", "Can you focus on one thing for 30 minutes?",
            listOf(Option("Hardly ever", 0), Option("Sometimes", 2), Option("Usually", 4))),
        Question("Habits", "Do you move your body or get outside most days?",
            listOf(Option("Rarely", 0), Option("A couple of days a week", 2), Option("Most days", 4))),
        Question("Connection", "Do you have someone you can talk to when things are hard?",
            listOf(Option("Not really", 0), Option("Sort of", 2), Option("Yes", 4))),
    ), mapOf(
        "Sleep" to Fix(Regex("(?i)sleep"), "w:bd-sleep"),
        "Stress" to Fix(Regex("(?i)stress|anxiety|calm|burnout|happy|zen"), null),
        "Focus" to Fix(Regex("(?i)focus|deep work|time management|multitask"), "w:bd-eis"),
        "Habits" to Fix(Regex("(?i)habit|consistent|routine|exercise|healthy"), "w:bd-habit"),
        "Connection" to Fix(Regex("(?i)relationship|love|friend|loneli"), null),
    )),
    Check("career", "Career Check", "Where your career is strong, and what to work on next", listOf(
        Question("Pay", "When did your pay last rise more than inflation?",
            listOf(Option("Never, or I don't know", 0), Option("Over 2 years ago", 1), Option("In the last 2 years", 3), Option("This year", 4))),
        Question("AI", "How ready are you for AI in your job?",
            listOf(Option("I haven't thought about it", 0), Option("I'm a bit worried", 1), Option("I use AI tools sometimes", 3), Option("I use them every week", 4))),
        Question("Energy", "How do you feel at the end of a work week?",
            listOf(Option("Burnt out", 0), Option("Drained", 1), Option("Tired but fine", 3), Option("Good", 4))),
        Question("Growth", "Are you learning something that makes you more valuable?",
            listOf(Option("No", 0), Option("Now and then", 2), Option("Yes, steadily", 4))),
        Question("Options", "If you lost your job tomorrow, how ready would you be?",
            listOf(Option("Not at all", 0), Option("Somewhat", 2), Option("Ready: CV, contacts and savings", 4))),
    ), mapOf(
        "Pay" to Fix(Regex("(?i)raise|salary|pay|wage"), "w:bd-ry"),
        "AI" to Fix(Regex("(?i)\\bai\\b|artificial|jobs will"), "w:bd-aix"),
        "Energy" to Fix(Regex("(?i)burnout|stress|pressure|balance"), null),
        "Growth" to Fix(Regex("(?i)career|skill|learn|leader"), null),
        "Options" to Fix(Regex("(?i)layoff|gap|interview|resume|emergency"), "w:bd-cv"),
    )),
)
