package com.randomthoughtsever.plainly

import com.randomthoughtsever.plainly.tools.Calc
import com.randomthoughtsever.plainly.tools.PdfWriter
import com.randomthoughtsever.plainly.tools.QrEngine
import com.randomthoughtsever.plainly.ui.Block
import com.randomthoughtsever.plainly.ui.Reader
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LogicTest {
    @Test fun loanMatchesTheStandardFormula() {
        val r = Calc.loan(300_000.0, 6.5, 30.0, 0.0)!!
        assertEquals(1896.20, r.payment, 0.01)
        assertEquals(360, r.months)
        assertEquals(382_633.47, r.interest, 1.0)
        val zero = Calc.loan(12_000.0, 0.0, 1.0, 0.0)!!
        assertEquals(1000.0, zero.payment, 1e-9)
    }

    @Test fun overpaymentsShortenTheLoan() {
        val r = Calc.loan(300_000.0, 6.5, 30.0, 300.0)!!
        assertTrue(r.months < 360)
        assertTrue(r.baseInterest - r.interest > 80_000)
    }

    @Test fun growthWithNoReturnIsJustContributions() {
        val g = Calc.growth(1000.0, 100.0, 0.0, 2, 0.0)
        assertEquals(3400.0, g.value, 1e-6)
        assertEquals(3400.0, g.contributed, 1e-6)
        assertEquals(2, g.yearly.size)
        assertTrue(Calc.growth(0.0, 500.0, 7.0, 20, 0.0).value > 240_000)
    }

    @Test fun cardPayoff() {
        val c = Calc.card(5000.0, 22.9, 200.0)!!
        assertEquals(35, c.months)
        assertEquals(1859.78, c.interest, 0.5)
        assertEquals(-1, Calc.card(5000.0, 22.9, 50.0)!!.months) // never clears
    }

    @Test fun fireNumberUsesTheWithdrawalRate() {
        val f = Calc.fire(40_000.0, 0.0, 2000.0, 6.0, 4.0)!!
        assertEquals(1_000_000.0, f.target, 1e-6)
        assertTrue(f.years in 15.0..25.0)
        assertEquals(0, Calc.monthsToFill(0.0, 100.0)); assertEquals(5, Calc.monthsToFill(450.0, 100.0)); assertEquals(-1, Calc.monthsToFill(10.0, 0.0))
    }

    @Test fun wifiCodesEscapeSpecialCharacters() {
        assertEquals("WIFI:T:WPA;S:My\\;Net;P:pa\\:ss;;", QrEngine.wifi("My;Net", "pa:ss", "WPA", false))
        assertEquals("WIFI:T:nopass;S:Cafe;H:true;;", QrEngine.wifi("Cafe", "x", "None", true))
    }

    @Test fun pdfWriterProducesAValidCrossReferenceTable() {
        val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 1, 2, 3, 0xFF.toByte(), 0xD9.toByte())
        val bytes = PdfWriter.write(listOf(PdfWriter.Page(jpeg, 10, 20, 595f, 842f), PdfWriter.Page(jpeg, 10, 20, 595f, 842f)))
        val text = String(bytes, Charsets.ISO_8859_1)
        assertTrue(text.startsWith("%PDF-1.4"))
        assertTrue(text.trimEnd().endsWith("%%EOF"))
        val startxref = text.substringAfterLast("startxref\n").substringBefore("\n").toInt()
        assertTrue(text.substring(startxref).startsWith("xref"))
        val entries = text.substring(startxref).lines().drop(3).takeWhile { it.endsWith(" n ") }
        assertEquals(8, entries.size) // catalog, pages, 2 × (page, contents, image)
        entries.forEachIndexed { i, e -> assertTrue(text.substring(e.take(10).toInt()).startsWith("${i + 1} 0 obj")) }
    }

    @Test fun readerTurnsHtmlIntoNativeBlocks() {
        val html = """
            <p><em>Updated 3 Nov 2026</em></p>
            <p><strong>Quick answer:</strong> Pay <a href="/2026/10/x.html">this</a> first.</p>
            <div class="bd-calc"><input><button>Go</button><p>widget text</p></div>
            <h2>Why it matters</h2><p>Line one<br>line two</p>
            <ul><li>A<ul><li>A1</li></ul></li><li>B</li></ul>
            <table><thead><tr><th>Plan</th><th>Cost</th></tr></thead><tbody><tr><td>Basic</td><td>$5</td></tr></tbody></table>
            <details><summary>Is it safe?</summary><p>Yes.</p></details>
            <script>alert(1)</script><style>p{}</style>""".trimIndent()
        val b = Reader.parse(html)
        assertTrue(b[0] is Block.QuickAnswer)
        assertEquals("Pay this first.", (b[0] as Block.QuickAnswer).text.text)
        assertTrue((b[0] as Block.QuickAnswer).text.getLinkAnnotations(0, 15).isNotEmpty())
        assertEquals("Why it matters", (b[1] as Block.Heading).text.text)
        assertEquals("Line one\nline two", (b[2] as Block.Para).text.text)
        assertEquals(listOf(0 to "A", 1 to "A1", 0 to "B"), (b[3] as Block.Bullets).items.map { it.first to it.second.text })
        val t = b[4] as Block.Table
        assertEquals(listOf("Plan", "Cost"), t.header!!.map { it.text }); assertEquals("$5", t.rows[0][1].text)
        assertEquals("Is it safe?", (b[5] as Block.Faq).question)
        assertEquals(6, b.size)
        assertTrue(b.none { it.toString().contains("widget") || it.toString().contains("alert") })
    }

    @Test fun articleAdsNeverFollowAHeadingAndShortArticlesGetFewOrNone() {
        fun para() = Block.Para(androidx.compose.ui.text.AnnotatedString("x"))
        fun head() = Block.Heading(2, androidx.compose.ui.text.AnnotatedString("h"))
        assertTrue(com.randomthoughtsever.plainly.ui.adSlots(List(8) { para() }).isEmpty())
        val long = listOf(Block.QuickAnswer(androidx.compose.ui.text.AnnotatedString("q"))) + List(29) { if (it % 4 == 3) head() else para() }
        val slots = com.randomthoughtsever.plainly.ui.adSlots(long)
        assertEquals(2, slots.size)
        slots.forEach { assertTrue("slot $it is a heading", long[it] !is Block.Heading) }
    }

    @Test fun searchPutsTheArticleAboutTheWordFirst() {
        fun rank(q: String, titles: Map<String, String>) = titles.entries.sortedByDescending {
            com.randomthoughtsever.plainly.data.SearchRank.score(q, it.key, emptyList(), "", it.value) }.map { it.key }
        val posts = mapOf(
            "Dental Implant Loans: Real Financing Options" to "Check your score on Credit Karma before you apply.",
            "What Is Karma? Meaning, Types and How It Works" to "Karma means action. Karma has three types. Good karma...",
            "Credit Karma vs FICO: Why Is My Credit Score Different?" to "Credit Karma shows VantageScore. Karma is free.",
        )
        assertEquals(listOf("What Is Karma? Meaning, Types and How It Works", "Credit Karma vs FICO: Why Is My Credit Score Different?",
            "Dental Implant Loans: Real Financing Options"), rank("karma", posts))
        assertEquals("Credit Karma vs FICO: Why Is My Credit Score Different?", rank("credit karma", posts).first())
    }

    /** The real search that went wrong on the phone: "karma" on the live blog. */
    @Test fun liveKarmaSearchShowsTheKarmaArticlesFirst() {
        val body = runCatching { URL("https://randomthoughtsever.blogspot.com/feeds/posts/default?alt=json&max-results=50&q=karma").readText() }.getOrNull()
        assumeTrue("network unavailable", body != null)
        val entries = JSONObject(body!!).getJSONObject("feed").getJSONArray("entry")
        val ranked = (0 until entries.length()).map { entries.getJSONObject(it) }.map { e ->
            val title = e.getJSONObject("title").getString("\$t")
            val labels = e.optJSONArray("category")?.let { a -> List(a.length()) { a.getJSONObject(it).getString("term") } } ?: emptyList()
            val text = e.getJSONObject("content").getString("\$t").replace(Regex("<[^>]+>"), " ")
            title to com.randomthoughtsever.plainly.data.SearchRank.score("karma", title, labels, "", text)
        }.filterNot { it.first.startsWith("Sitemap") }.sortedByDescending { it.second }.map { it.first }
        println("KARMA " + ranked.take(4))
        assertTrue(ranked[0].startsWith("What Is Karma"))
        assertTrue(ranked[1].startsWith("Credit Karma vs FICO"))
    }

    @Test fun motionCardsAreSpreadOutAndChangeDaily() {
        val a = com.randomthoughtsever.plainly.ui.motionSpots(200, 20000L).sorted()
        val b = com.randomthoughtsever.plainly.ui.motionSpots(200, 20001L).sorted()
        assertTrue(a.size in 14..26)
        a.zipWithNext().forEach { (x, y) -> assertTrue("gap ${y - x}", y - x in 7..13) }
        assertTrue(a.none { it % 7 == 0 })
        assertTrue(a != b)
    }

    @Test fun checkUpsScoreAndFindTheWeakestArea() {
        val money = com.randomthoughtsever.plainly.data.CHECKS.first { it.id == "money" }
        val best = money.questions.map { q -> q.options.indexOf(q.options.maxBy { it.points }) }
        assertEquals(100, money.score(best).first)
        val worst = money.questions.map { q -> q.options.indexOf(q.options.minBy { it.points }) }
        assertEquals(0, money.score(worst).first)
        val mixed = best.toMutableList().also { it[1] = 4 } // missed card payments
        val (score, areas) = money.score(mixed)
        assertTrue(score in 70..90)
        assertEquals("Debt", money.weakest(areas))
        assertEquals("Strong", com.randomthoughtsever.plainly.data.verdict(score))
        com.randomthoughtsever.plainly.data.CHECKS.forEach { c -> c.areas.forEach { a -> assertTrue("${c.id}/$a has no fix", c.fixes.containsKey(a)) } }
    }

    @Test fun labelsSayWhatKindOfGuideItIs() {
        val f = { t: String -> com.randomthoughtsever.plainly.data.intentOf(t) }
        assertEquals(com.randomthoughtsever.plainly.data.Intent.FIX, f("How to Pay Off Credit Card Debt Fast"))
        assertEquals(com.randomthoughtsever.plainly.data.Intent.DECIDE, f("SIP vs Lumpsum: Which Is Better in Mutual Funds?"))
        assertEquals(com.randomthoughtsever.plainly.data.Intent.LEARN, f("What Is Inflation? Causes, Types and Effects Explained"))
        assertEquals(com.randomthoughtsever.plainly.data.Intent.FACTS, f("Switzerland Income Tax 2026: Mortgages and Pensions"))
        assertEquals(com.randomthoughtsever.plainly.data.Intent.FACTS, f("Minimum Wage by Country 2026: Hourly and Monthly Rates in US Dollars"))
    }

    @Test fun planDailyQuizAndBadges() {
        fun e(id: String, title: String, label: String) = com.randomthoughtsever.plainly.data.Entry(id, title, "/$id", "", listOf(label), "2026-10-0$id", "", null)
        val index = listOf(e("1", "How to Pay Off Credit Card Debt Fast", "Personal Finance"), e("2", "Sleep Hacks", "Health & Wellness"),
            e("3", "Resize Image to 20KB Online Free", "Tools"), e("4", "Debt Consolidation Loans: When They Save Money", "Personal Finance"))
        val plan = com.randomthoughtsever.plainly.ui.planFor(setOf("debt", "mind"), index).map { it.id }
        assertTrue(plan.containsAll(listOf("1", "2")) && "3" !in plan)
        val q = com.randomthoughtsever.plainly.data.quizOfTheDay(setOf("mind"), 20000L)
        assertEquals("mind", q.goal)
        assertTrue(com.randomthoughtsever.plainly.data.QUIZZES.all { it.correct in it.options.indices && it.options.size in 2..4 })
        assertEquals(com.randomthoughtsever.plainly.data.QUIZZES.size, com.randomthoughtsever.plainly.data.QUIZZES.map { it.id }.toSet().size)
        val b = com.randomthoughtsever.plainly.data.badges(1, 3, 0, 0, 0, 0, 0, 0, 0)
        assertTrue(b.first { it.title == "First guide" }.earned && b.first { it.title == "On a roll" }.earned && !b.first { it.title == "Bookworm" }.earned)
    }

    @Test fun moreToReadMixesTopics() {
        fun e(id: String, label: String, date: String) = com.randomthoughtsever.plainly.data.Entry(id, id, "/$id", "", listOf(label), date, "", null)
        val feed = listOf(e("t1", "Tools", "2026-10-30"), e("t2", "Tools", "2026-10-30"), e("t3", "Tools", "2026-10-30"),
            e("m1", "Psychology", "2026-10-29"), e("f1", "Personal Finance", "2026-10-28"), e("t4", "Tools", "2026-10-27"))
        val mixed = com.randomthoughtsever.plainly.ui.mixTopics(feed).map { it.id }
        assertEquals(listOf("t1", "m1", "t2", "f1", "t3", "t4"), mixed)
        assertEquals(feed.size, mixed.toSet().size)
    }

    /** Runs the reader over every live post, so a real-world layout that breaks it is caught. */
    @Test fun readerHandlesEveryLivePost() {
        val body = runCatching { URL("https://randomthoughtsever.blogspot.com/feeds/posts/default?alt=json&max-results=150").readText() }.getOrNull()
        assumeTrue("network unavailable", body != null)
        val entries = JSONObject(body!!).getJSONObject("feed").getJSONArray("entry")
        var quick = 0; var tables = 0; var faqs = 0; var thin = 0
        for (i in 0 until entries.length()) {
            val e = entries.getJSONObject(i)
            val title = e.getJSONObject("title").getString("\$t")
            val blocks = Reader.parse(e.getJSONObject("content").getString("\$t"))
            val text = blocks.joinToString(" ") { it.toString() }
            val leak = Regex("function\\s*\\(|document\\.[a-z]+\\(|addEventListener|\\{\\s*display").find(text)
            assertTrue("$title leaks code: " + leak?.let { text.substring(maxOf(0, it.range.first - 200), minOf(text.length, it.range.last + 100)) }, leak == null)
            if (blocks.size < 8 && !title.contains("Privacy", true) && !title.contains("Terms", true)) { thin++; println("THIN: $title (${blocks.size})") }
            if (blocks.any { it is Block.QuickAnswer }) quick++
            tables += blocks.count { it is Block.Table }; faqs += blocks.count { it is Block.Faq }
        }
        println("posts=${entries.length()} quickAnswers=$quick tables=$tables faqs=$faqs thin=$thin")
        assertTrue(thin <= 3)
    }
}
