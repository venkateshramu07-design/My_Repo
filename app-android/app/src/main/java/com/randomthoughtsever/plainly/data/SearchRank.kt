package com.randomthoughtsever.plainly.data

/**
 * Orders search results by relevance. Blogger's search returns every post that mentions the words anywhere, newest
 * first, so a new post that mentions "Credit Karma" once would come before "What Is Karma?". Here the title counts most,
 * then the topic and the quick answer, then how often the words appear in the text; the date only breaks ties.
 */
object SearchRank {
    private val STOP = setOf("a", "an", "the", "of", "to", "in", "on", "for", "and", "or", "is", "are", "what", "how", "do", "i", "my", "with", "vs")

    fun words(query: String): List<String> {
        val all = query.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotBlank() }
        return all.filterNot { it in STOP }.ifEmpty { all }
    }

    fun score(query: String, title: String, labels: List<String>, excerpt: String, body: String): Int {
        val q = query.trim().lowercase()
        val ws = words(q)
        if (ws.isEmpty()) return 0
        val t = title.lowercase()
        var s = 0
        if (q.length >= 3 && t.contains(q)) s += 120
        var inTitle = 0
        for (w in ws) {
            val word = Regex("(?<![\\p{L}\\p{N}])" + Regex.escape(w))
            if (word.containsMatchIn(t)) {
                inTitle++; s += 40
                if (Regex("^(what is |what are |how |the )?" + Regex.escape(w)).containsMatchIn(t)) s += 25 // the guide is about this
            }
            if (labels.any { word.containsMatchIn(it.lowercase()) }) s += 12
            if (word.containsMatchIn(excerpt.lowercase())) s += 10
            s += minOf(word.findAll(body.lowercase()).count(), 15) * 2
        }
        if (ws.size > 1 && inTitle == ws.size) s += 30
        return s
    }

    /** Ranks posts for [query]; drops index pages such as the sitemap. */
    fun rank(query: String, posts: List<Post>): List<Post> {
        val text = posts.associate { p -> p.id to p.html.replace(Regex("(?is)<(script|style)[^>]*>.*?</\\1>"), " ").replace(Regex("<[^>]+>"), " ") }
        return posts.filterNot { it.title.startsWith("Sitemap", true) }
            .map { it to score(query, it.title, it.labels, it.excerpt, text[it.id] ?: "") }
            .sortedWith(compareByDescending<Pair<Post, Int>> { it.second }.thenByDescending { it.first.published })
            .map { it.first }
    }

    private val SPLIT = Regex("[^\\p{L}\\p{N}]+")

    /** The words to look for while typing: stop-words are dropped, except the word being typed (it may become "inflation"). */
    fun typed(query: String): List<String> {
        val raw = query.lowercase().split(SPLIT).filter { it.isNotBlank() }
        return raw.filterIndexed { i, w -> w !in STOP || i == raw.lastIndex }.ifEmpty { raw }
    }

    /**
     * Instant results for what's typed so far, from the full index on the phone (no waiting for the network, no need to
     * finish a word): every word must match the title, topic or quick answer, and the last word may be just its first
     * letters ("infl" finds "Inflation"). Title matches rank highest; whole words beat beginnings of words.
     */
    fun instant(query: String, entries: List<Entry>, limit: Int = 40): List<Entry> {
        val tokens = typed(query)
        if (tokens.isEmpty()) return emptyList()
        val phrase = query.trim().lowercase()
        return entries.mapNotNull { e ->
            val title = e.title.lowercase()
            val words = title.split(SPLIT)
            val labels = e.labels.joinToString(" ").lowercase().split(SPLIT)
            val quick = e.quick.lowercase()
            var score = 0
            for (t in tokens) score += when {
                words.any { it == t } -> 50
                words.any { it.startsWith(t) } -> 35
                t.length >= 3 && title.contains(t) -> 18
                labels.any { it.startsWith(t) } -> 12
                t.length >= 3 && quick.split(SPLIT).any { it.startsWith(t) } -> 6
                else -> return@mapNotNull null
            }
            if (words.firstOrNull()?.startsWith(tokens.first()) == true) score += 10
            // A match near the start of the title means the guide is about it ("What Is Inflation?" before "... When Inflation Is Down").
            val at = words.filter { it.isNotEmpty() && it !in STOP }.indexOfFirst { w -> tokens.any { w.startsWith(it) } }
            if (at >= 0) score += (12 - at * 2).coerceAtLeast(0)
            if (phrase.length >= 3 && title.contains(phrase)) score += 30
            if (e.isToolPage) score -= 5
            e to score
        }.sortedWith(compareByDescending<Pair<Entry, Int>> { it.second }.thenByDescending { it.first.published }).take(limit).map { it.first }
    }

    /** Where in [text] the typed words match (word beginnings), for highlighting. */
    fun matches(text: String, query: String): List<IntRange> {
        val tokens = typed(query).filter { it.isNotEmpty() }
        val lower = text.lowercase()
        return Regex("[\\p{L}\\p{N}]+").findAll(lower).flatMap { m ->
            tokens.filter { m.value.startsWith(it) }.map { t -> m.range.first until m.range.first + t.length }
        }.toList()
    }
}
