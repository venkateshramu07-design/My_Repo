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

    @Test fun gamesFollowTheirRules() {
        val g = "com.randomthoughtsever.plainly.ui"
        // rock 0, paper 1, scissors 2
        assertEquals(1, com.randomthoughtsever.plainly.ui.rpsOutcome(1, 0)); assertEquals(1, com.randomthoughtsever.plainly.ui.rpsOutcome(0, 2))
        assertEquals(1, com.randomthoughtsever.plainly.ui.rpsOutcome(2, 1)); assertEquals(-1, com.randomthoughtsever.plainly.ui.rpsOutcome(0, 1))
        assertEquals(0, com.randomthoughtsever.plainly.ui.rpsOutcome(2, 2))
        // Number Rush: every answer is right, and it's one of 4 different options.
        val r = kotlin.random.Random(7)
        repeat(500) {
            val q = com.randomthoughtsever.plainly.ui.nextSum(r)
            val pct = Regex("^(\\d+)% of (\\d+)$").find(q.text)
            val interest = Regex("^(\\d+) \\+ (\\d+)% interest$").find(q.text)
            val expected = when {
                pct != null -> pct.groupValues[2].toInt() * pct.groupValues[1].toInt() / 100
                interest != null -> interest.groupValues.let { it[1].toInt() + it[1].toInt() * it[2].toInt() / 100 }
                q.text.contains(" + ") -> q.text.split(" + ").sumOf { it.toInt() }
                q.text.contains(" − ") -> q.text.split(" − ").let { it[0].toInt() - it[1].toInt() }
                else -> q.text.split(" × ").let { it[0].toInt() * it[1].toInt() }
            }
            assertEquals(q.text, expected, q.answer)
            assertEquals(4, q.options.toSet().size); assertTrue(q.answer in q.options)
        }
        // Unbeatable tic-tac-toe never loses: try every possible game where X moves first.
        fun xCanWin(b: MutableList<Char>, xTurn: Boolean): Boolean {
            com.randomthoughtsever.plainly.ui.tttWinner(b)?.let { return it.first == 'X' }
            if (b.none { it == ' ' }) return false
            return if (xTurn) b.indices.filter { b[it] == ' ' }.any { i -> b[i] = 'X'; val w = xCanWin(b, false); b[i] = ' '; w }
            else { val m = com.randomthoughtsever.plainly.ui.tttMove(b, 'O', true); b[m] = 'O'; val w = xCanWin(b, true); b[m] = ' '; w }
        }
        assertTrue("the unbeatable phone lost a game", !xCanWin(MutableList(9) { ' ' }, true))
        // Easy mode still takes an obvious win.
        assertEquals(2, com.randomthoughtsever.plainly.ui.tttMove(listOf('O', 'O', ' ', 'X', 'X', ' ', ' ', ' ', ' '), 'O', false))
        assertEquals(5, com.randomthoughtsever.plainly.ui.GAMES.size); assertTrue(g.isNotEmpty())
    }

    @Test fun searchAnswersWhileTyping() {
        fun e(id: String, title: String, label: String, quick: String = "") = com.randomthoughtsever.plainly.data.Entry(id, title, "/$id", "", listOf(label), "2026-10-01", quick, null)
        val index = listOf(e("1", "What Is Karma? Meaning, Types and How It Works", "Psychology"),
            e("2", "Credit Karma vs FICO: Why Is My Credit Score Different on Every App?", "Personal Finance"),
            e("3", "What Is Inflation? Causes, Types and Effects Explained", "Economics"),
            e("4", "How Does the Fed Control Inflation?", "Economics"),
            e("5", "Dental Implant Loans", "Health & Wellness", "check your score on Credit Karma first"))
        val s = com.randomthoughtsever.plainly.data.SearchRank
        assertEquals(listOf("1", "2"), s.instant("kar", index).map { it.id }.take(2))   // partial word, titles first
        assertEquals("3", s.instant("infl", index).first().id)
        val withGroceries = index + e("6", "Why Are Groceries and Prices So Expensive When Inflation Is Down?", "Economics")
        assertEquals("3", s.instant("infl", withGroceries).first().id)
        assertEquals(listOf("3", "4"), s.instant("inflation", index).map { it.id })
        assertEquals("2", s.instant("credit kar", index).first().id)                      // both words, last one partial
        assertTrue(s.instant("zzzz", index).isEmpty())
        assertTrue(s.instant("what is k", index).first().id == "1")                       // stop words, then a partial word
        assertEquals(listOf(0 until 3), s.matches("Karma explained", "kar"))
    }

    /** The Firebase-free community: Blogger comment feeds parsed into conversations, spam dropped, your own posts recognised. */
    @Test fun blogCommentsBecomeConversations() {
        val t = com.randomthoughtsever.plainly.data.BlogTalk
        fun entry(id: String, post: String, path: String, name: String, html: String, at: String, photo: String = "//blogger.googleusercontent.com/img/a/x=s35") = JSONObject()
            .put("id", JSONObject().put("\$t", "tag:blogger.com,1999:blog-1.post-$id"))
            .put("published", JSONObject().put("\$t", at))
            .put("content", JSONObject().put("type", "html").put("\$t", html))
            .put("link", org.json.JSONArray().put(JSONObject().put("rel", "alternate").put("href", "https://randomthoughtsever.blogspot.com$path?showComment=1#c$id")))
            .put("author", org.json.JSONArray().put(JSONObject().put("name", JSONObject().put("\$t", name))
                .put("uri", JSONObject().put("\$t", "https://www.blogger.com/profile/$name")).put("gd\$image", JSONObject().put("src", photo))))
            .put("thr\$in-reply-to", JSONObject().put("href", "https://randomthoughtsever.blogspot.com$path").put("ref", "tag:blogger.com,1999:blog-1.post-$post"))
        val feed = JSONObject().put("feed", JSONObject().put("entry", org.json.JSONArray()
            .put(entry("c1", "111", "/2026/10/emergency-fund.html", "Asha", "Great guide!<br />I keep 4 months &amp; it helped.", "2026-11-01T10:00:00.000+05:30"))
            .put(entry("c2", "111", "/2026/10/emergency-fund.html", "Spammer", "Nice! <a href=\"https://cheap-loans.example\" rel=\"nofollow\">loans</a>", "2026-11-01T11:00:00.000+05:30"))
            .put(entry("c3", "222", "/p/free-tools.html", "Ben", "Where is the PDF tool?", "2026-11-02T10:00:00.000+05:30"))
            .put(entry("c4", "333", "/2026/09/karma.html", "Asha", "Visit www.mysite.example for more", "2026-11-02T12:00:00.000+05:30"))
            .put(entry("c5", "333", "/2026/09/karma.html", "Chen", "Day 3 was the hardest for me", "2026-11-03T09:00:00.000+05:30", "https://img1.blogblog.com/img/b16-rounded.gif")))).toString()
        val notes = t.parse(feed)
        assertEquals(listOf("c5", "c1"), notes.map { it.id })                          // newest first; link spam and page comments gone
        assertEquals("Great guide!\nI keep 4 months & it helped.", notes[1].text)
        assertEquals("111", notes[1].postId); assertEquals("https://blogger.googleusercontent.com/img/a/x=s35", notes[1].photo)
        assertEquals("", notes[0].photo)                                                 // Blogger's placeholder means "no photo"
        val v = t.voices(t.parse(feed) + notes.map { it.copy(id = it.id + "b", postId = "444") })
        assertEquals(2, v.first().notes); assertEquals(2, v.first().guides)
        // Your post is recognised even after Blogger or the paste changes spacing and punctuation.
        assertTrue(t.same("Great guide!\nI keep 4 months & it helped.", "Great guide! I keep 4 months & it helped"))
        assertTrue(!t.same("Day 3 was the hardest for me", "Great guide! I keep 4 months"))
        assertEquals("https://www.blogger.com/comment/fullpage/post/6417357851361576411/111", t.formUrl("111"))
    }

    /** The live blog: its comment feed parses, and Blogger's comment page for a guide is there to post on. */
    @Test fun liveBlogCommentsWork() {
        val posts = runCatching { URL("https://randomthoughtsever.blogspot.com/feeds/posts/summary?alt=json&max-results=1").readText() }.getOrNull()
        assumeTrue(posts != null)
        val id = JSONObject(posts!!).getJSONObject("feed").getJSONArray("entry").getJSONObject(0).getJSONObject("id").getString("\$t").substringAfterLast("post-")
        val comments = URL("https://randomthoughtsever.blogspot.com/feeds/comments/default?alt=json&max-results=200").readText()
        com.randomthoughtsever.plainly.data.BlogTalk.parse(comments).forEach { assertTrue(it.text.isNotBlank() && it.postId.isNotBlank()) }
        val form = URL(com.randomthoughtsever.plainly.data.BlogTalk.formUrl(id)).openConnection() as java.net.HttpURLConnection
        assertEquals(200, form.responseCode)
        assertTrue(form.inputStream.bufferedReader().readText().contains("Comment as"))
    }

    /** XP, levels, leagues, the Daily Run, cards, check-ins and "I tried this", on a controllable calendar. */
    @Test fun progressRewardsTheRightThings() {
        val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        ctx.getSharedPreferences("progress", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        var day = java.time.LocalDate.of(2026, 3, 2) // a Monday, no seasonal event
        val pr = com.randomthoughtsever.plainly.data.Progress(ctx) { day }
        assertEquals(1.0, pr.multiplier(), 0.0)
        assertTrue(pr.award("read:1", 20, "x")); assertTrue(!pr.award("read:1", 20, "x"))          // once per key
        assertEquals(20, pr.xp); assertEquals(1, pr.level())
        assertEquals(listOf(0, 100, 300, 600, 1000), (1..5).map { pr.need(it) })
        pr.award("big", 90, "x"); assertEquals(2, pr.level()); assertEquals(2, pr.levelUp)        // level-up flagged
        assertTrue(pr.avatars().containsAll(listOf("🐯", "🦄")))                                   // level 2 unlocks emojis
        assertEquals("navy", pr.accent.also { pr.accent = "plum" })                               // plum needs level 5
        assertEquals("navy", pr.accent)
        // Daily Run: five steps, a bonus once, and a streak across days
        com.randomthoughtsever.plainly.data.Progress.RUN_STEPS.forEach { pr.runStep(it) }
        assertEquals(1, pr.runs()); assertEquals(1, pr.runStreak())
        day = day.plusDays(1); com.randomthoughtsever.plainly.data.Progress.RUN_STEPS.forEach { pr.runStep(it) }
        assertEquals(2, pr.runStreak()); assertEquals(160 + 50, pr.weekXp())
        assertEquals("Silver", pr.league().name)
        // Seasonal events give 50% more
        day = java.time.LocalDate.of(2026, 1, 5); assertEquals(1.5, pr.multiplier(), 0.0)
        val before = pr.xp; pr.award("event-test", 20, "x"); assertEquals(before + 30, pr.xp)
        day = java.time.LocalDate.of(2026, 3, 4)
        // Cards only upgrade
        assertTrue(pr.collect("g1", "Guide", "Investing", 0) != null)
        assertTrue(pr.collect("g1", "Guide", "Investing", 0) == null)
        assertEquals(2, pr.collect("g1", "Guide", "Investing", 2)!!.rarity)
        assertTrue(pr.collect("g1", "Guide", "Investing", 1) == null)
        // I tried this: asked again after three days, snooze moves it
        pr.startTrying("g1", "Guide"); assertTrue(pr.followUp() == null)
        day = day.plusDays(3); assertEquals("g1", pr.followUp()!!.postId)
        pr.snoozeTried("g1"); assertTrue(pr.followUp() == null)
        day = day.plusDays(3); pr.answerTried("g1", "yes"); assertTrue(pr.followUp() == null)
        // Check-in, notebook, usual hour
        pr.saveCheckIn(com.randomthoughtsever.plainly.data.Progress.CheckIn(4, -1, "good")); assertEquals(4, pr.checkIn()!!.mood)
        pr.addNote("Pay yourself first", "g1", "Guide"); pr.addNote("Pay yourself first", "g1", "Guide"); assertEquals(1, pr.notes().size)
        assertEquals(19, pr.usualHour()); listOf(8, 22, 21, 20, 21).forEach { pr.recordOpen(it) }; assertEquals(21, pr.usualHour())
        val r = com.randomthoughtsever.plainly.ui.recapStats(pr, com.randomthoughtsever.plainly.ui.RecapRange(day.minusDays(10), 11, "t", "k"))
        assertTrue(r.xp > 0 && r.moods.contains(4))
    }

    /** Daily puzzles: the same for everyone on a day, always solvable, and scored correctly. */
    @Test fun dailyPuzzlesAreFairAndSolvable() {
        val P = com.randomthoughtsever.plainly.data.Puzzles
        assertTrue(P.WORDS.size >= 60 && P.WORDS.all { it.length == 5 && it.all { c -> c in 'A'..'Z' } })
        assertEquals(listOf(0, 2, 2, 0, 2), P.mark("NONNY", "MONEY"))          // repeated letters only count as often as they appear
        assertEquals(listOf(2, 2, 2, 2, 2), P.mark("money", "MONEY"))
        assertEquals(listOf(1, 1, 0, 0, 0), P.mark("OMAAA", "MONEY"))
        var d = java.time.LocalDate.of(2026, 11, 1)
        assertEquals(1, P.number(d)); assertEquals(P.word(d), P.word(d))
        assertEquals(3, (0L..2L).map { P.kind(d.plusDays(it)) }.toSet().size)
        repeat(400) {
            val b = P.budget(d)
            assertEquals(b.target, P.total(b, b.solution)); assertEquals(8, b.items.map { it.second }.toSet().size)
            val g = P.growth(d)
            assertEquals(4, P.rightPlaces(P.bestOrder(g), g)); assertEquals(4, g.map { it.end }.toSet().size)
            d = d.plusDays(1)
        }
        val s = com.randomthoughtsever.plainly.data.Puzzles.Saving("A", 1000, 0, 0.12, 1)
        assertEquals(1000 * Math.pow(1.01, 12.0), s.end, 0.01)
    }

    /** A Year of Money: deterministic per seed, every choice keeps the numbers sane, scores stay in range. */
    @Test fun moneyYearPlaysFair() {
        val S = com.randomthoughtsever.plainly.data.LifeSim
        val start = S.start(com.randomthoughtsever.plainly.data.Numbers())
        assertEquals(3000.0, start.income, 0.0)
        assertEquals(S.events(42).map { it.key }, S.events(42).map { it.key }); assertEquals(12, S.events(42).size)
        S.EVENTS.forEach { e -> e.choices.forEach { c ->
            val after = c.apply(start)
            assertTrue("${e.key}: ${c.label}", after.savings >= -0.01 && after.debt >= 0 && after.wellbeing in 0..100)
        } }
        for (seed in 1L..200L) {
            var st = start
            S.events(seed).forEachIndexed { i, e -> st = S.month(e.choices[(seed % e.choices.size).toInt()].apply(st), S.markets(seed)[i]) }
            assertEquals(12, st.month); assertTrue(S.score(start, st) in 0..100); assertTrue(st.savings >= 0 && st.debt >= 0)
        }
        // Paying down debt beats ignoring it
        val ignore = (1..12).fold(start) { s, _ -> S.month(s, 0.0) }
        val pay = (1..12).fold(start) { s, _ -> S.month(S.EVENTS.first { it.key == "card" }.choices[1].apply(s), 0.0) }
        assertTrue(pay.debt < ignore.debt)
    }

    @Test fun remindersSeasonsAndQuestions() {
        val R = com.randomthoughtsever.plainly.data.Reminders
        val ch = com.randomthoughtsever.plainly.data.CHALLENGES.first()
        assertEquals("run", R.message(false, 5, ch, false, null, "P")!!.second)            // a streak at risk comes first
        assertEquals("challenge/${ch.key}", R.message(true, 5, ch, false, null, "P")!!.second)
        assertEquals("run", R.message(true, 0, null, false, null, "P")!!.second)
        assertTrue(R.message(true, 0, null, true, "Guide", "P")!!.first.contains("Guide"))
        assertTrue(R.message(true, 9, null, true, null, "P") == null)                       // nothing to do, no nudge
        val Sz = com.randomthoughtsever.plainly.data.Seasons
        assertEquals("newyear", Sz.current(java.time.LocalDate.of(2027, 1, 3))!!.key)
        assertTrue(Sz.current(java.time.LocalDate.of(2026, 3, 1)) == null)
        assertEquals("review", Sz.current(java.time.LocalDate.of(2026, 12, 31))!!.key)
        val posts = com.randomthoughtsever.plainly.data.QUIZZES.map { it.post }.toSet()
        assertTrue(com.randomthoughtsever.plainly.data.WEEK_QUESTIONS.all { it.post in posts })
        assertEquals(com.randomthoughtsever.plainly.data.questionOfTheWeek(java.time.LocalDate.of(2026, 11, 2)),
            com.randomthoughtsever.plainly.data.questionOfTheWeek(java.time.LocalDate.of(2026, 11, 8)))   // same all week
        val m = com.randomthoughtsever.plainly.ui.recapRange("month", java.time.LocalDate.of(2026, 12, 3))
        assertEquals(java.time.LocalDate.of(2026, 11, 1), m.from); assertEquals(30, m.days)
        assertEquals(7, com.randomthoughtsever.plainly.ui.recapRange("week").days)
        assertTrue(com.randomthoughtsever.plainly.data.MICRO_ACTIONS.map { it.goal }.toSet().containsAll(com.randomthoughtsever.plainly.data.GOALS.map { it.key }))
    }

    @Test fun supportAsksPolitelyAndTranslationKeepsStructure() {
        val S = com.randomthoughtsever.plainly.data.Support
        val day = S.DAY; val now = 100 * day
        assertTrue(!S.shouldAsk(now, now - 1 * day, 0, 0, 10, 5, 0))                 // too new
        assertTrue(!S.shouldAsk(now, now - 9 * day, 0, 0, 2, 1, 0))                  // not enough value yet
        assertTrue(S.shouldAsk(now, now - 9 * day, 0, 0, 5, 0, 0))
        assertTrue(!S.shouldAsk(now, now - 9 * day, now - 2 * day, 0, 5, 0, 0))      // asked this week
        assertTrue(!S.shouldAsk(now, now - 60 * day, 0, now - 10 * day, 50, 9, 0))   // tipped this month
        assertTrue(!S.shouldAsk(now, now - 60 * day, 0, 0, 50, 9, 6))                // closed it six times: stop asking
        if (com.randomthoughtsever.plainly.BuildConfig.SUPPORT_LINKS) {
            assertTrue(S.upi(99).startsWith("upi://pay?pa=venkateshramu000@okhdfcbank&") && S.upi(99).contains("&am=99&") && S.upi(99).contains("cu=INR"))
            assertEquals("https://paypal.me/VenkateshramuM/5USD", S.paypal(5, "USD"))
        } else {
            // Normal (Play) builds carry no outside payment details at all.
            assertEquals("", com.randomthoughtsever.plainly.BuildConfig.SUPPORT_PAYPAL); assertEquals("", com.randomthoughtsever.plainly.BuildConfig.SUPPORT_UPI)
            assertTrue(!S.linksEnabled)
        }
        assertEquals("USD", S.paypalCurrency("INR")); assertEquals("EUR", S.paypalCurrency("EUR"))
        val T = com.randomthoughtsever.plainly.data.Translate
        assertEquals("Español", T.deviceLang(java.util.Locale("es", "MX"))!!.native)
        assertTrue(T.deviceLang(java.util.Locale.US) == null)
        assertTrue(T.LANGS.size >= 20 && T.LANGS.map { it.code }.toSet().size == T.LANGS.size)
        val blocks = com.randomthoughtsever.plainly.ui.Reader.parse("<h2>Save first</h2><p>Pay <a href='https://x.example'>yourself</a> first.</p>" +
            "<ul><li>One</li><li>Two</li></ul><table><tr><th>A</th></tr><tr><td>b</td></tr></table><img src='https://x.example/i.png'>")
        val out = kotlinx.coroutines.runBlocking { T.blocks(blocks, { it.uppercase() }) }
        assertEquals(blocks.map { it::class }, out.map { it::class })
        assertTrue(out.any { it is com.randomthoughtsever.plainly.ui.Block.Para && it.text.text == "PAY YOURSELF FIRST." })
        assertTrue(out.any { it is com.randomthoughtsever.plainly.ui.Block.Bullets && it.items.map { i -> i.second.text } == listOf("ONE", "TWO") })
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
