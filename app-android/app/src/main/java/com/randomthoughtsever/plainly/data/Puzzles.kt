package com.randomthoughtsever.plainly.data

import java.time.LocalDate
import kotlin.math.pow
import kotlin.random.Random

/**
 * The daily puzzle: the same for every player on a given day (seeded by the date, so no server is needed), one of three
 * kinds in turn. Results can be shared as an emoji grid without giving the answer away.
 */
object Puzzles {
    val FIRST: LocalDate = LocalDate.of(2026, 11, 1)
    enum class Kind(val title: String, val emoji: String, val how: String) {
        WORD("Word Vault", "🔐", "Find the 5-letter word in 6 tries. Gold ✓ = right letter, right place. Teal ↔ = in the word, wrong place. Grey ✗ = not in it."),
        BUDGET("Budget Balance", "⚖️", "Pick items that add up to exactly the budget. You have 5 checks; each tells you if you're over or under."),
        GROWTH("Growth Order", "📈", "Order the four savings from the most money at the end to the least. 3 tries; we show how many are in the right place."),
        SCAM("Spot the Scam", "🕵️", "Five rounds. Each has three messages; one is a scam. Find it, then see the warning sign. 4 of 5 right solves it."),
        DOLLAR("Shrinking Dollar", "🛒", "Five rounds about inflation: how much will today's shopping cost in the future? 4 of 5 right solves it."),
    }
    fun number(d: LocalDate = LocalDate.now()) = (d.toEpochDay() - FIRST.toEpochDay() + 1).toInt()
    fun kind(d: LocalDate = LocalDate.now()) = Kind.entries[Math.floorMod(d.toEpochDay(), Kind.entries.size.toLong()).toInt()]
    /** Past puzzles that can still be played (the last 30 days, newest first). */
    fun archive(today: LocalDate = LocalDate.now()) = (1L..30L).map { today.minusDays(it) }.filter { !it.isBefore(FIRST) }
    private fun rng(d: LocalDate, salt: Int) = Random(d.toEpochDay() * 7919 + salt)

    // ---------- Word Vault ----------

    val WORDS = listOf("MONEY", "BONDS", "STOCK", "SHARE", "YIELD", "TAXES", "RATES", "LOANS", "DEBTS", "ASSET", "PRICE", "VALUE", "WAGES",
        "BANKS", "FUNDS", "SAVER", "TRADE", "AUDIT", "BONUS", "CLAIM", "DEBIT", "GRANT", "HEDGE", "INDEX", "LEASE", "OWNER", "PENNY", "POUND",
        "QUOTA", "RALLY", "RENTS", "SALES", "SPEND", "TERMS", "TOKEN", "TRUST", "VAULT", "WORTH", "RUPEE", "PESOS", "FRANC", "KRONA", "DINAR",
        "RIYAL", "SLEEP", "HABIT", "FOCUS", "PEACE", "SMILE", "DREAM", "GOALS", "CHART", "PLANS", "BUDGE", "COINS", "NOTES", "PURSE", "LUCKY",
        "STAKE", "CYCLE", "MERIT", "TRAIN", "SKILL", "LEARN", "MONTH").filter { it.length == 5 && it.all { c -> c in 'A'..'Z' } }.distinct()
    fun word(d: LocalDate = LocalDate.now()) = WORDS[rng(d, 1).nextInt(WORDS.size)]

    /** For each letter: 2 = right place, 1 = in the word elsewhere, 0 = not in it. Repeated letters are counted properly. */
    fun mark(guess: String, answer: String): List<Int> {
        val g = guess.uppercase(); val res = MutableList(5) { 0 }
        val left = answer.groupingBy { it }.eachCount().toMutableMap()
        for (i in 0 until 5) if (g[i] == answer[i]) { res[i] = 2; left[g[i]] = left.getValue(g[i]) - 1 }
        for (i in 0 until 5) if (res[i] == 0 && (left[g[i]] ?: 0) > 0) { res[i] = 1; left[g[i]] = left.getValue(g[i]) - 1 }
        return res
    }
    fun wordGrid(marks: List<List<Int>>) = marks.joinToString("\n") { row -> row.joinToString("") { listOf("⬛", "🟦", "🟧")[it] } }

    // ---------- Budget Balance ----------

    data class Budget(val items: List<Pair<String, Int>>, val target: Int, val solution: Set<Int>)
    private val THINGS = listOf("🍕 Pizza", "🎧 Headphones", "📚 Books", "👟 Trainers", "🎬 Cinema", "☕ Coffees", "🧴 Skincare", "🎮 Game",
        "🪴 Plant", "🧥 Jacket", "🎫 Concert", "🍣 Sushi", "🧢 Cap", "🎒 Backpack", "🕯️ Candles", "🧩 Puzzle", "🍫 Chocolate", "🚲 Bike repair")
    fun budget(d: LocalDate = LocalDate.now()): Budget {
        val r = rng(d, 2)
        val names = THINGS.shuffled(r).take(8)
        val prices = mutableListOf<Int>()
        while (prices.size < 8) { val p = 5 + r.nextInt(19) * 5; if (p !in prices) prices += p }
        val solution = (0 until 8).shuffled(r).take(3 + r.nextInt(2)).toSet()
        return Budget(names.zip(prices), solution.sumOf { prices[it] }, solution)
    }
    fun total(b: Budget, picked: Set<Int>) = picked.sumOf { b.items[it].second }

    // ---------- Growth Order ----------

    data class Saving(val label: String, val start: Int, val monthly: Int, val rate: Double, val years: Int) {
        /** Future value: the lump sum plus monthly additions, compounded monthly. */
        val end: Double get() {
            val i = rate / 12; val n = years * 12
            return if (i == 0.0) start + monthly.toDouble() * n else start * (1 + i).pow(n) + monthly * (((1 + i).pow(n) - 1) / i)
        }
        fun describe(sym: String) = "$sym${"%,d".format(start)} now + $sym$monthly a month, ${"%.1f".format(rate * 100)}% a year for $years years"
    }
    fun growth(d: LocalDate = LocalDate.now()): List<Saving> {
        val r = rng(d, 3)
        while (true) {
            val list = listOf("A", "B", "C", "D").map { l ->
                Saving(l, (r.nextInt(1, 21)) * 500, (r.nextInt(0, 9)) * 25, (r.nextInt(2, 17)) / 200.0, r.nextInt(3, 26))
            }
            val ends = list.map { it.end }.sorted()
            if (ends.zipWithNext().all { (a, b) -> b - a > b * 0.04 }) return list
        }
    }
    fun bestOrder(list: List<Saving>) = list.sortedByDescending { it.end }.map { it.label }
    fun rightPlaces(order: List<String>, list: List<Saving>) = order.zip(bestOrder(list)).count { (a, b) -> a == b }

    // ---------- Spot the Scam ----------

    data class Message(val text: String, val scam: Boolean, val why: String)
    val SCAMS = listOf(
        Message("Your bank account is locked. Reply with the one-time code we just sent to unlock it now.", true, "Banks never ask for your one-time code. Anyone who has it can get into your account."),
        Message("Congratulations! You've won \$5,000. Pay a \$50 processing fee to claim your prize.", true, "Real prizes never ask you to pay a fee first."),
        Message("Invest \$500 today for a guaranteed 30% return every month, with no risk.", true, "Nobody can guarantee high returns. \"No risk, big profit\" is the classic investment scam."),
        Message("This is the tax office. Pay your overdue tax today in gift cards or you will be arrested.", true, "No government agency takes gift cards or threatens instant arrest."),
        Message("Hi Mum, I dropped my phone. This is my new number. Can you send me money urgently?", true, "A \"new number\" plus an urgent money request: call the person on their old number first."),
        Message("Your parcel couldn't be delivered. Pay \$1.99 at this link to rebook it.", true, "Small \"redelivery fees\" by text are used to steal card details."),
        Message("Work from home: earn \$300 a day liking videos. Just pay a \$40 starter fee.", true, "Real jobs don't make you pay to start."),
        Message("We noticed a suspicious login. Confirm your card number and PIN here.", true, "No real company asks for your PIN, ever."),
        Message("Send 0.1 BTC to this wallet and we'll send 0.2 BTC back. Today only!", true, "\"Send crypto, get double back\" is always a scam."),
        Message("Your electricity will be cut off tonight. Call this mobile number now to pay.", true, "Pressure to pay right now, to a personal number: hang up and call the company's official number."),
        Message("Loan approved instantly, no credit check! Just pay the insurance fee first.", true, "Paying a fee before a loan is paid out is an advance-fee scam."),
        Message("I'm working overseas and need money for a flight so we can finally meet.", true, "An online partner you've never met asking for money is a romance scam."),
        Message("Refund available! Install this screen-sharing app so we can process it for you.", true, "Screen-sharing apps let scammers see and control your banking."),
        Message("Join our private trading group: copy our trades, 100% win rate. Only \$199 to join.", true, "No trader wins every time. Paid \"signal groups\" are a common trap."),
        Message("Your account closes in 1 hour unless you verify at bank-secure-login.co.", true, "A deadline plus a look-alike web address is phishing."),
        Message("A recruiter offers you a job with no interview and asks for your bank login to set up pay.", true, "Employers need your account number for pay, never your login."),
        Message("Designer bag 90% off! Pay only by bank transfer to a personal account.", true, "Huge discounts and payment to a personal account: you'll get nothing back."),
    )
    val SAFE = listOf(
        Message("Your card was used for \$42.10 at City Grocer. If this wasn't you, call the number on the back of your card.", false, ""),
        Message("Your October statement is ready. Log in through our app to see it.", false, ""),
        Message("Reminder: your phone bill of \$35 is due on the 12th. Pay in your account as usual.", false, ""),
        Message("Your package arrives tomorrow between 9 and 12. Track it in the store's app.", false, ""),
        Message("Your one-time code is 482913. Never share this code with anyone, including us.", false, ""),
        Message("Your password was changed. If this wasn't you, reset it from the official app.", false, ""),
        Message("Thanks, we received your payment of \$120 to your credit card.", false, ""),
        Message("Your salary of \$2,950 has been paid into your account.", false, ""),
        Message("Your appointment is confirmed for Tuesday at 10:30.", false, ""),
        Message("Your subscription renews on the 1st for \$9.99. Manage it in your account settings.", false, ""),
        Message("Savings rates change next month. The details are on our website and in the app.", false, ""),
        Message("We've received your tax return. You can check its status on the official government website.", false, ""),
        Message("Your new debit card is in the post. Activate it in the app when it arrives.", false, ""),
        Message("Your flight is delayed by 40 minutes. New departure time: 18:10.", false, ""),
        Message("Low balance alert: your account is below \$50.", false, ""),
        Message("Your loan application needs one more document. Upload it in online banking.", false, ""),
    )
    data class ScamRound(val options: List<Message>) { val answer get() = options.indexOfFirst { it.scam } }
    fun scamRounds(d: LocalDate = LocalDate.now()): List<ScamRound> {
        val r = rng(d, 4)
        val scams = SCAMS.shuffled(r).take(5); val safe = SAFE.shuffled(r).take(10)
        return scams.mapIndexed { i, s -> ScamRound((listOf(s) + safe.subList(i * 2, i * 2 + 2)).shuffled(r)) }
    }

    // ---------- Shrinking Dollar ----------

    data class Inflation(val rate: Int, val years: Int, val options: List<Int>, val answer: Int)
    /** What [start] of shopping costs after [years] of [rate]% inflation (compounded yearly). */
    fun future(start: Int, rate: Int, years: Int) = Math.round(start * (1 + rate / 100.0).pow(years)).toInt()
    fun dollarRounds(d: LocalDate = LocalDate.now()): List<Inflation> {
        val r = rng(d, 5)
        val out = mutableListOf<Inflation>()
        while (out.size < 5) {
            val rate = r.nextInt(2, 11); val years = listOf(5, 10, 15, 20, 25)[r.nextInt(5)]
            val right = future(100, rate, years)
            val simple = 100 + rate * years                       // forgets compounding
            val half = future(100, rate, years / 2)               // half the time
            val opts = listOf(right, simple, half).distinct()
            if (opts.size < 3 || opts.zipWithNext().any { (a, b) -> kotlin.math.abs(a - b) < 3 } || kotlin.math.abs(right - simple) < 3) continue
            if (out.any { it.rate == rate && it.years == years }) continue
            val shuffled = opts.shuffled(r)
            out += Inflation(rate, years, shuffled, shuffled.indexOf(right))
        }
        return out
    }
}
