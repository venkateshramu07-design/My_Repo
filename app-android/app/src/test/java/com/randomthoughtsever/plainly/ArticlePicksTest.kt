package com.randomthoughtsever.plainly

import com.randomthoughtsever.plainly.data.ArticlePicks
import com.randomthoughtsever.plainly.data.ArticlePicks.Pick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ArticlePicksTest {
    private fun p(id: String, title: String = "Guide $id", labels: List<String> = listOf("Personal Finance")) = Triple(Pick(id, title, ""), labels, true)

    @Test fun quietAtNightOnly() {
        assertTrue((22..23).all { ArticlePicks.quiet(it) } && (0..7).all { ArticlePicks.quiet(it) })
        assertTrue((8..21).none { ArticlePicks.quiet(it) })
    }

    @Test fun neverSuggestsLegalPagesOrSitemap() {
        val posts = listOf(p("1", "Privacy Policy", emptyList()), p("2", "Sitemap: All Articles by Topic", listOf("Start Here")), p("3"))
        repeat(50) { assertEquals("3", ArticlePicks.choose(posts, emptySet(), Random(it))!!.id) }
        assertNull(ArticlePicks.choose(posts.take(2), emptySet(), Random(1)))
    }

    @Test fun avoidsRecentPicksAndIsRandom() {
        val posts = (1..10).map { p("$it") }
        val recent = (1..8).map { "$it" }.toSet()
        repeat(50) { assertFalse(ArticlePicks.choose(posts, recent, Random(it))!!.id in recent) }
        val seen = (0 until 60).map { ArticlePicks.choose(posts, emptySet(), Random(it))!!.id }.toSet()
        assertTrue(seen.size >= 8)
        // Everything recent: still suggests something rather than nothing.
        assertNotEquals(null, ArticlePicks.choose(posts, posts.map { it.first.id }.toSet(), Random(3)))
    }

    @Test fun teaserDropsDateLineAndLabels() {
        assertEquals("A single dental implant costs \$3,000 to \$6,000.",
            ArticlePicks.teaser("Updated 3 November 2026\n\nQuick answer: A single dental implant costs \$3,000 to \$6,000."))
        assertEquals("Telling the truth brings freedom.", ArticlePicks.teaser("Updated 24 October 2026 In short: Telling the truth brings freedom."))
        val long = ArticlePicks.teaser("word ".repeat(80))
        assertTrue(long.length <= 141 && long.endsWith("…"))
    }
}
