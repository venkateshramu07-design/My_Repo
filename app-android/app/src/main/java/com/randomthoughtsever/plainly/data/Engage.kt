package com.randomthoughtsever.plainly.data

import java.time.LocalDate

/**
 * A quiz question taken from a guide's own answer, so tapping "Read why" shows where the answer comes from.
 * [goal] links it to a reader goal (for Daily 3 and Today); [post] is the guide's id.
 */
data class Quiz(val id: String, val question: String, val options: List<String>, val correct: Int, val why: String, val post: String, val goal: String)

val QUIZZES = listOf(
    Quiz("efund", "How much do most planners suggest keeping in an emergency fund?", listOf("1 month of spending", "3 to 6 months of essentials", "2 years of salary"), 1,
        "Three to six months of essential costs; more if your income is irregular or others depend on you.", "9189829005013563974", "save"),
    Quiz("fire25", "FIRE's rule of thumb: how big should your portfolio be to retire early?", listOf("10 × yearly spending", "25 × yearly spending", "100 × monthly spending"), 1,
        "About 25 times your annual spending, based on the 4% withdrawal rule.", "8447404837524559491", "save"),
    Quiz("fdic", "How much of your money does US deposit insurance protect at one bank?", listOf("$100,000", "$250,000", "Everything"), 1,
        "$250,000 per person, per bank, per ownership category.", "4802572834537200994", "save"),
    Quiz("ukdeposit", "Since December 2025, how much does UK deposit protection cover per person, per bank?", listOf("£85,000", "£120,000", "£250,000"), 1,
        "£120,000, raised from £85,000.", "654896823025164438", "save"),
    Quiz("cash", "Flying out of the US with cash, above what amount must you declare it?", listOf("$3,000", "$10,000", "There is no rule"), 1,
        "There's no limit on how much you carry, but above US$10,000 you must declare it.", "2966849922473946170", "abroad"),
    Quiz("gift", "Your parent sends you money from abroad. In most countries, is it taxed as income?", listOf("Yes, always", "Usually no: it's a gift", "Only if it's over $100"), 1,
        "In most countries money from a parent, child or spouse is a gift, and gifts aren't income.", "62782047927845160", "abroad"),
    Quiz("japanmeds", "Which of these does Japan ban outright for travellers?", listOf("Paracetamol", "Adderall and Sudafed", "Antibiotics"), 1,
        "Japan bans amphetamine ADHD medicines like Adderall, and pseudoephedrine cold tablets like Sudafed.", "6783406381369311805", "abroad"),
    Quiz("notax", "Which of these countries has no personal income tax on salaries?", listOf("United Arab Emirates", "Germany", "Japan"), 0,
        "The UAE, Qatar, Kuwait and Bahrain are among the countries that don't tax employment income.", "5926589948389203389", "abroad"),
    Quiz("retireage", "In 2026, what is the full state retirement age in the US for anyone born in 1960 or later?", listOf("62", "65", "67"), 2,
        "67 in the US, as in Australia, Italy, the Netherlands and the Nordic countries.", "2818149266725416804", "abroad"),
    Quiz("keepoecd", "On average, how much of their gross pay does a single average worker keep across OECD countries?", listOf("About 55%", "About 75%", "About 90%"), 1,
        "About 75%, after income tax and employee social security. Belgium is lowest at 60.5%.", "8606503102249681444", "abroad"),
    Quiz("minwage", "Which country has the world's highest minimum wage in 2026?", listOf("United States", "Luxembourg", "Japan"), 1,
        "Luxembourg, at about €2,771 a month (around $3,100).", "979474773097769668", "abroad"),
    Quiz("prices", "Inflation fell from 9% to 3%. What happened to prices?", listOf("They fell", "They're still rising, just more slowly", "They stayed the same"), 1,
        "Inflation measures how fast prices rise, not how high they are. At 3% they're still going up.", "4939854309846248923", "learn"),
    Quiz("fedtarget", "What inflation rate does the US Federal Reserve aim for?", listOf("0%", "2%", "5%"), 1,
        "A 2% target. Above it, the Fed raises its key rate to cool demand.", "7343017972988870999", "learn"),
    Quiz("gdp", "What does GDP measure?", listOf("Government debt", "The value of all final goods and services a country produces", "The stock market's value"), 1,
        "The market value of all final goods and services produced in a country over a period.", "322541548933063666", "learn"),
    Quiz("reserve", "Why is the US dollar the world's main reserve currency?", listOf("It's backed by gold", "Largest economy and deepest markets", "A United Nations rule"), 1,
        "The world's largest economy, deep financial markets, a convertible currency and Treasuries seen as among the safest assets.", "8432941315446354874", "learn"),
    Quiz("stable", "What is a stablecoin designed to do?", listOf("Rise steadily in value", "Stay worth exactly one unit of a currency, usually $1", "Pay a fixed 10% a year"), 1,
        "Stay worth one unit of a currency, backed by cash and short-term government debt.", "5606869079880760746", "learn"),
    Quiz("gold", "From its January 2026 record, roughly how far had gold fallen by late October?", listOf("About 5%", "About 28%", "About 60%"), 1,
        "From about $5,595 an ounce to about $4,027, roughly 28%.", "4706352494431399329", "learn"),
    Quiz("credit", "Why does your credit score differ from app to app?", listOf("One of them is wrong", "Different bureaus and scoring models", "Apps add a random number"), 1,
        "There's no single score: each bureau has its own file and dozens of models turn files into numbers.", "5740472520363100795", "debt"),
    Quiz("cibil", "In India, which CIBIL score is generally considered good?", listOf("500 or above", "650 or above", "750 or above"), 2,
        "Scores run from 300 to 900; 750 or above is generally seen as good.", "3529972167132428793", "debt"),
    Quiz("carloan", "Average used-car loan rate for borrowers with scores of 300 to 500 (mid-2026)?", listOf("About 8.8%", "About 21.6%", "About 35%"), 1,
        "About 21.6%, against 8.8% for prime borrowers, per Experian.", "3020803474340550917", "debt"),
    Quiz("subsid", "What's the difference between subsidized and unsubsidized student loans?", listOf("The interest rate", "Who pays the interest while you study", "How much you can borrow"), 1,
        "Same rate; on a subsidized loan the government pays the interest while you're enrolled at least half-time.", "8366928729163185451", "debt"),
    Quiz("aotc", "The American Opportunity Tax Credit is worth up to how much per student per year?", listOf("$1,000", "$2,500", "$5,000"), 1,
        "Up to $2,500: all of the first $2,000 of costs plus 25% of the next $2,000.", "4005128226220285163", "debt"),
    Quiz("rentbuy", "With mortgage rates above 7%, buying usually only makes sense if you stay for at least...", listOf("1 to 2 years", "3 years", "7 to 10 years"), 2,
        "Usually at least seven to ten years in the US, and only if the monthly cost fits your budget today.", "1035870912250163605", "debt"),
    Quiz("gap", "How long should your answer about a career gap or layoff be in an interview?", listOf("5 seconds", "30 to 60 seconds", "5 minutes"), 1,
        "Thirty to sixty seconds: the fact, what you did with the time, then why you fit the job.", "7951744000938487213", "career"),
    Quiz("raise", "A typical 2026 raise for staying in the same job in the US, UK or Canada?", listOf("1%", "3% to 4%", "10%"), 1,
        "About 3% to 4%; around 2.5% in the euro area and about 9% in India.", "7983108608663736153", "career"),
    Quiz("aijobs", "Who has AI affected most at work so far?", listOf("Senior managers", "People starting out, in routine tasks", "Nobody yet"), 1,
        "AI is taking routine tasks inside jobs, and those starting out feel it most.", "7495590773123088922", "career"),
    Quiz("deep", "Which of these is 'deep work'?", listOf("Answering email", "Writing or coding without distraction", "A routine meeting"), 1,
        "Focused, distraction-free work on demanding tasks that create real value.", "3602549798644502798", "career"),
    Quiz("sleep", "Which habit does the most for better sleep?", listOf("Sleeping in at weekends", "The same bed and wake time every day", "A nightcap"), 1,
        "Consistent times, morning daylight, a dark cool room and no screens before bed.", "1649974056115307315", "mind"),
    Quiz("habit", "Best way to make a habit stick?", listOf("Start big and go hard", "Make it tiny and tie it to an existing routine", "Rely on motivation"), 1,
        "Make it small enough for a bad day and anchor it to something you already do.", "3172134042728014477", "mind"),
    Quiz("love", "How many love languages are there in Gary Chapman's model?", listOf("3", "5", "7"), 1,
        "Five: words of affirmation, acts of service, gifts, quality time and physical touch.", "4818210939017191354", "mind"),
    Quiz("karma", "Which traditions share the idea of karma?", listOf("Only Buddhism", "Hinduism, Buddhism, Jainism and Sikhism", "Christianity and Islam"), 1,
        "Karma, that intentional actions shape the future, is found in Hinduism, Buddhism, Jainism and Sikhism.", "3174627791129954978", "mind"),
)

/** Today's quiz for Daily 3: from the reader's goals when possible, changing every day. */
fun quizOfTheDay(goals: Set<String>, day: Long = LocalDate.now().toEpochDay()): Quiz {
    val pool = QUIZZES.filter { it.goal in goals }.ifEmpty { QUIZZES }
    return pool[(day % pool.size).toInt()]
}

/** A challenge: [days] long, one small action to tick each day. */
data class Challenge(val key: String, val title: String, val blurb: String, val days: Int, val action: String, val emoji: String, val goal: String)

val CHALLENGES = listOf(
    Challenge("nospend", "7-day no-spend", "Spend only on essentials for a week and watch what you save.", 7, "No non-essential spending today", "💸", "save"),
    Challenge("debt30", "30-day debt sprint", "Pay something extra on your highest-interest debt every day you can.", 30, "Put something extra toward my debt", "🎯", "debt"),
    Challenge("calm7", "7-day calm", "One minute of slow breathing a day.", 7, "Do one minute of breathing", "🌿", "mind"),
    Challenge("read21", "21-day reading habit", "Read one guide a day and build the habit.", 21, "Read one guide", "📚", "learn"),
    Challenge("career14", "14-day career boost", "One small step a day: update your CV, learn, or reach out to someone.", 14, "Do one career step", "🚀", "career"),
    Challenge("habit30", "30-day tiny habit", "Pick one tiny habit and do it every day for a month.", 30, "Do my tiny habit", "✅", "mind"),
)

/** Badges, each with whether it's earned and the progress towards it. */
data class Badge(val emoji: String, val title: String, val how: String, val earned: Boolean, val progress: Float)

fun badges(reads: Int, streak: Int, saved: Int, tools: Int, quizRight: Int, checks: Int, dailies: Int, challengesDone: Int, breaths: Int): List<Badge> {
    fun b(e: String, t: String, how: String, have: Int, need: Int) = Badge(e, t, how, have >= need, (have / need.toFloat()).coerceAtMost(1f))
    return listOf(
        b("📖", "First guide", "Read a guide", reads, 1), b("📚", "Bookworm", "Read 25 guides", reads, 25),
        b("🔥", "On a roll", "A 3-day streak", streak, 3), b("⚡", "Unstoppable", "A 14-day streak", streak, 14),
        b("🔖", "Collector", "Save 5 guides", saved, 5), b("🛠️", "Maker", "Make 3 files with the tools", tools, 3),
        b("🧠", "Quiz whiz", "Get 10 quiz answers right", quizRight, 10), b("🩺", "Checked up", "Finish a check-up", checks, 1),
        b("☀️", "Daily 3", "Finish Daily 3 on 5 days", dailies, 5), b("🏆", "Finisher", "Complete a challenge", challengesDone, 1),
        b("🌿", "Breather", "Do 10 breathing minutes", breaths, 10),
    )
}

/** What kind of guide this is, from its title: something to fix, to decide, to learn, or facts and figures. */
enum class Intent(val label: String) { FIX("FIX"), DECIDE("DECIDE"), LEARN("LEARN"), FACTS("FACTS"), GUIDE("GUIDE") }

fun intentOf(title: String): Intent {
    val t = title.lowercase()
    return when {
        Regex("by country|in numbers|minimum wage|rates by|limits|prices by|(wage|tax|rates?|prices?|pension) 202\\d").containsMatchIn(t) -> Intent.FACTS
        Regex("\\bvs\\b|or |which|should you|is it|better\\?|worth").containsMatchIn(t) -> Intent.DECIDE
        Regex("^how to|how do|fix|pay off|get out|improve|explain a gap|stop|beat|handle|deal with|can i|do i").containsMatchIn(t) -> Intent.FIX
        Regex("^what|^why|explained|meaning|guide to|works|how does").containsMatchIn(t) -> Intent.LEARN
        else -> Intent.GUIDE
    }
}
