package com.randomthoughtsever.plainly.data

import kotlin.random.Random

/**
 * A varied reading order, so lookalike guides don't come back to back (the blog has series such as
 * "US Minimum Wage 2026", "UK Minimum Wage 2026", "Switzerland Income Tax 2026"…).
 *
 * - Guides in the same **series** (same title once the country and year are taken out) are kept at least
 *   [seriesGap] cards apart, and country guides of any kind at least [countryGap] apart.
 * - Each **topic** is spaced by its share of all guides (big topics up to 4 cards apart) when there's any other choice.
 * - Otherwise the order is random (seeded, so it's stable for a day and changes with Shuffle), with the reader's
 *   favourite topics a little more likely to come early. Publish date plays no part.
 */
object Variety {
    private val COUNTRIES = listOf(
        "United States", "United Kingdom", "US", "USA", "UK", "India", "Canada", "Australia", "Germany", "France", "Spain", "Italy",
        "Netherlands", "Ireland", "Singapore", "United Arab Emirates", "UAE", "Dubai", "Saudi Arabia", "Japan", "South Korea", "Korea", "China",
        "Brazil", "Mexico", "South Africa", "Nigeria", "Kenya", "Egypt", "Philippines", "Malaysia", "Indonesia", "Pakistan", "Bangladesh",
        "Sri Lanka", "Nepal", "Vietnam", "Thailand", "New Zealand", "Switzerland", "Sweden", "Norway", "Denmark", "Finland", "Iceland",
        "Belgium", "Luxembourg", "Austria", "Portugal", "Poland", "Czech Republic", "Czechia", "Slovakia", "Slovenia", "Hungary", "Romania",
        "Bulgaria", "Greece", "Croatia", "Estonia", "Latvia", "Lithuania", "Turkey", "Türkiye", "Israel", "Qatar", "Kuwait", "Bahrain", "Oman",
        "Argentina", "Chile", "Colombia", "Peru", "Hong Kong", "Taiwan", "Russia", "Ukraine", "Ghana", "Morocco", "Malta", "Cyprus",
    ).sortedByDescending { it.length }
    private val YEAR = Regex("\\b(19|20)\\d{2}(-\\d{2,4})?\\b")
    private val STOP = setOf("the", "a", "an", "in", "of", "and", "for", "to", "how", "what", "is", "your", "my", "vs", "fy")

    /** Starts with a country name ("US Minimum Wage 2026…", "South Africa Minimum Wage…"). */
    fun isCountryGuide(title: String) = COUNTRIES.any { c -> title.startsWith("$c ", ignoreCase = false) || title.startsWith("$c's ") }

    /**
     * The series a guide belongs to: for titles with a year, the words just before the year with any country removed
     * ("US Minimum Wage 2026: Tax…" → "minimum wage"); otherwise the guide is its own series.
     */
    fun series(title: String, id: String): String {
        val y = YEAR.find(title) ?: return "id:$id"
        var head = title.substring(0, y.range.first).trim().trimEnd(':', '-', '–', ',')
        COUNTRIES.firstOrNull { head.startsWith("$it ") || head == it }?.let { head = head.removePrefix(it).trim() }
        val words = head.lowercase().split(Regex("[^\\p{L}\\p{N}']+")).filter { it.isNotBlank() && it !in STOP }
        return if (words.isEmpty()) "id:$id" else words.takeLast(2).joinToString(" ")
    }

    fun order(list: List<Entry>, seed: Long, favourites: Set<String> = emptySet(), seriesGap: Int = 8, countryGap: Int = 4): List<Entry> {
        if (list.size < 3) return list
        val r = Random(seed)
        // Random priority (publish date plays no part), nudged up only for the reader's favourite topics.
        val pool = list.map { e ->
            val fav = if (e.label in favourites) .35 else 0.0
            e to (r.nextDouble() + fav)   // random: newer guides get no head start
        }.sortedByDescending { it.second }.map { it.first }.toMutableList()
        val seriesOf = list.associate { it.id to series(it.title, it.id) }
        val country = list.filter { isCountryGuide(it.title) }.map { it.id }.toSet()
        // Spread each series evenly: a big series (35 countries) can't be 8 apart in 150 cards, so its gap is its fair share.
        val sizes = seriesOf.values.groupingBy { it }.eachCount()
        fun gapOf(s: String) = minOf(seriesGap, maxOf(2, list.size / sizes.getValue(s)))
        val cGap = if (country.isEmpty()) 0 else minOf(countryGap, maxOf(2, list.size / country.size))
        // Topics too: a big topic (a fifth of all guides) is spaced up to 4 cards apart, smaller ones further.
        val labelSizes = list.groupingBy { it.label }.eachCount()
        fun topicGap(l: String) = minOf(4, maxOf(2, list.size / labelSizes.getValue(l)))
        val out = ArrayList<Entry>(list.size)
        val lastSeries = HashMap<String, Int>()
        var lastCountry = -1_000
        while (pool.isNotEmpty()) {
            val i = out.size
            fun ok(e: Entry, strict: Int): Boolean {
                val s = seriesOf.getValue(e.id)
                if (i - (lastSeries[s] ?: -1_000) < gapOf(s)) return false
                if (e.id in country && i - lastCountry < cGap) return false
                // strict -1: the topic's own spacing; 1: just not the same topic as the last card; 0: any topic.
                val window = if (strict < 0) topicGap(e.label) - 1 else strict
                return out.takeLast(window).none { it.label == e.label }
            }
            val pick = pool.firstOrNull { ok(it, -1) } ?: pool.firstOrNull { ok(it, 1) } ?: pool.firstOrNull { ok(it, 0) }
                ?: pool.firstOrNull { out.isEmpty() || out.last().label != it.label } ?: pool.first()
            pool.remove(pick); out += pick
            lastSeries[seriesOf.getValue(pick.id)] = i
            if (pick.id in country) lastCountry = i
        }
        return out
    }


    /**
     * Keeps a list's order (Today's newest-first feed) but moves lookalike series apart: a guide whose series appeared in
     * the last [gap] cards, or a country guide right after another, waits for the next free spot.
     */
    fun spread(list: List<Entry>, gap: Int = 6): List<Entry> {
        val seriesOf = list.associate { it.id to series(it.title, it.id) }
        val pool = list.toMutableList(); val out = ArrayList<Entry>(list.size)
        while (pool.isNotEmpty()) {
            val recent = out.takeLast(gap).map { seriesOf.getValue(it.id) }.toSet()
            val lastCountry = out.lastOrNull()?.let { isCountryGuide(it.title) } == true
            val pick = pool.firstOrNull { seriesOf.getValue(it.id) !in recent && !(lastCountry && isCountryGuide(it.title)) }
                ?: pool.firstOrNull { seriesOf.getValue(it.id) !in recent } ?: pool.first()
            pool.remove(pick); out += pick
        }
        return out
    }
}
