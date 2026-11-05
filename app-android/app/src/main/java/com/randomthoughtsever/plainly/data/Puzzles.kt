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
        WORD("Word Vault", "🔐", "Find the 5-letter word in 6 tries. Gold = right letter, right place. Teal = in the word, wrong place."),
        BUDGET("Budget Balance", "⚖️", "Pick items that add up to exactly the budget. You have 5 checks; each tells you if you're over or under."),
        GROWTH("Growth Order", "📈", "Order the four savings from the most money at the end to the least. 3 tries; we show how many are in the right place."),
    }
    fun number(d: LocalDate = LocalDate.now()) = (d.toEpochDay() - FIRST.toEpochDay() + 1).toInt()
    fun kind(d: LocalDate = LocalDate.now()) = Kind.entries[Math.floorMod(d.toEpochDay(), 3L).toInt()]
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
}
