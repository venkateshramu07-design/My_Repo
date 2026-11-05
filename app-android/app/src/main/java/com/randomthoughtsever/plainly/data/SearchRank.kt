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
}
