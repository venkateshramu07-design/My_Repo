package com.batchmates.app.util

import com.batchmates.app.data.School
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * School names are written a dozen ways: "Vallalar", "Vallalar HSS", "Vallalar Hr. Sec. School",
 * "St. Joseph's" / "St Josephs Matric". We compare only the *distinctive* words and ignore the
 * generic ones, which is what humans do when they say "it's the same school".
 */
object SchoolNames {

    /** Words that describe *a kind of* school rather than *which* school. */
    private val generic = setOf(
        "school", "schools", "higher", "hr", "hsc", "hss", "hs", "sec", "secondary", "senior", "sr", "junior", "jr",
        "high", "matric", "matriculation", "mat", "primary", "middle", "elementary", "nursery", "kg", "pre",
        "and", "the", "of", "for", "in", "at", "english", "medium", "eng", "govt", "government", "gov", "aided",
        "panchayat", "union", "municipal", "corporation", "model", "public", "cbse", "icse", "state", "board",
        "st", "saint", "sri", "shri", "shree", "thiru", "co", "ed", "coed", "education", "educational",
        "institution", "institute", "campus", "branch", "main", "new", "old", "ps", "ms", "ghss", "gbhss", "gghss",
    )

    /** Distinctive words, lower-cased, with possessives and punctuation removed. */
    fun core(name: String): Set<String> = name.lowercase()
        .replace("&", " and ")
        .replace(Regex("['’`]s\\b"), "")
        .split(Regex("[^\\p{L}\\p{N}]+"))
        .map { it.trim() }
        .filter { it.length >= 2 && it !in generic }
        .toSet()

    /** True when two names very likely mean the same school. */
    fun similar(a: String, b: String): Boolean {
        val ca = core(a)
        val cb = core(b)
        if (ca.isEmpty() || cb.isEmpty()) {
            // Purely generic names ("Government Higher Secondary School") only match exactly.
            return normalize(a) == normalize(b)
        }
        val common = ca.count { x -> cb.any { y -> close(x, y) } }
        return common >= minOf(ca.size, cb.size) && common * 3 >= maxOf(ca.size, cb.size)
    }

    /** Search: every typed word must start a word of the name or one of its aliases. */
    fun matches(query: String, school: School): Boolean {
        val words = query.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return true
        val haystack = (listOf(school.name) + school.aliases + school.locality).joinToString(" ").lowercase()
            .split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotBlank() }
        return words.all { w -> haystack.any { it.startsWith(w) } || (w in generic) }
    }

    /** Existing schools that look like the one being created (by name, or by being on the same spot). */
    fun duplicates(name: String, lat: Double?, lon: Double?, candidates: List<School>): List<School> =
        candidates.filter { s ->
            (listOf(s.name) + s.aliases).any { similar(name, it) } ||
                (lat != null && lon != null && s.lat != null && s.lon != null && km(lat, lon, s.lat, s.lon) < 0.25)
        }.distinctBy { it.id }

    /**
     * Is "Karthik" (typed from memory) the person who just joined as "Karthikeyan S"?
     * First names must match closely; initials and surnames may differ.
     */
    fun samePerson(remembered: String, joined: String): Boolean {
        fun parts(n: String) = n.lowercase().split(Regex("[^\\p{L}]+")).filter { it.isNotEmpty() }
        val a = parts(remembered)
        val b = parts(joined)
        val first = a.firstOrNull()?.takeIf { it.length >= 2 } ?: return false
        fun sameFirst(p: String) = p == first || (first.length >= 5 && p.startsWith(first)) || (p.length >= 5 && first.startsWith(p)) ||
            (minOf(p.length, first.length) >= 5 && editDistance(p, first) <= 1)
        val match = b.indexOfFirst(::sameFirst).takeIf { it >= 0 } ?: return false
        val rest = b.filterIndexed { i, _ -> i != match }
        // Any surname / initial they remembered must fit one of the joiner's other names.
        return a.drop(1).all { s -> rest.any { it.startsWith(s) || s.startsWith(it) } }
    }

    fun km(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(a))
    }

    private val synonyms = mapOf(
        "govt" to "government", "gov" to "government", "hr" to "higher", "sec" to "secondary",
        "hss" to "higher secondary school", "ghss" to "government higher secondary school",
        "hs" to "high school", "matric" to "matriculation", "mat" to "matriculation", "sr" to "senior",
        "jr" to "junior", "st" to "saint", "eng" to "english", "ps" to "primary school", "ms" to "middle school",
    )

    /** Spells out common abbreviations so "Govt. HSS" == "Government Higher Secondary School". */
    private fun normalize(s: String) = s.lowercase().split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.isNotBlank() }
        .flatMap { (synonyms[it] ?: it).split(' ') }
        .joinToString("")

    /** Tolerates small spelling differences: "vallalar" ≈ "valalar", "joseph" ≈ "josef". */
    private fun close(a: String, b: String): Boolean {
        if (a == b) return true
        if (a.length >= 4 && b.length >= 4 && (a.startsWith(b) || b.startsWith(a))) return true
        if (minOf(a.length, b.length) < 5) return false
        return editDistance(a, b) <= 1 + minOf(a.length, b.length) / 8
    }

    private fun editDistance(a: String, b: String): Int {
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val tmp = dp[j]
                dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + if (a[i - 1] == b[j - 1]) 0 else 1)
                prev = tmp
            }
        }
        return dp[b.length]
    }
}
