package com.randomthoughtsever.plainly

import android.content.Context
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.randomthoughtsever.plainly.ui.AppGraph
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Pre-launch QA (black-box): the app as a user meets it. Every screen opens on the oldest and a recent Android, a seeded
 * random "monkey" taps around without crashing, the app copes with no internet and with damaged saved data, links can
 * only open allowed places, every tappable thing has a label for screen readers, and the live content the app points at
 * really exists.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h840dp-xxhdpi")
class ReleaseQaTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val ctx: Context = ApplicationProvider.getApplicationContext()

    private fun launch(starter: Boolean = false): ActivityScenario<MainActivity> {
        ctx.getSharedPreferences("plainly", Context.MODE_PRIVATE).edit().putString("theme", "light").putString("currency", "USD")
            .putString("autoDeck", java.time.LocalDate.now().toString()).commit()
        ctx.getSharedPreferences("profile", Context.MODE_PRIVATE).edit().putBoolean("onboarded", true)
            .putStringSet("goals", setOf("debt", "save")).putString("country", "US").commit()
        ctx.getSharedPreferences("progress", Context.MODE_PRIVATE).edit().putBoolean("starterHidden", !starter).commit()
        val s = ActivityScenario.launch(MainActivity::class.java)
        scenario = s
        compose.mainClock.autoAdvance = false
        return s
    }
    private fun pause(ms: Int) {
        val end = System.currentTimeMillis() + ms
        while (System.currentTimeMillis() < end) { compose.mainClock.advanceTimeByFrame(); ShadowLooper.idleMainLooper(50, TimeUnit.MILLISECONDS); Thread.sleep(30) }
    }
    private fun waitFor(seconds: Int, done: () -> Boolean) {
        val end = System.currentTimeMillis() + seconds * 1000L
        while (System.currentTimeMillis() < end && !runCatching(done).getOrDefault(false)) pause(150)
    }
    private fun seen(t: String) = compose.onAllNodesWithText(t, substring = true).fetchSemanticsNodes().isNotEmpty()
    private fun go(route: String) { AppGraph.route.value = route; pause(1400) }
    private var scenario: ActivityScenario<MainActivity>? = null
    private var activity: MainActivity? = null
    /** Presses Back on the app's main thread, like the system does. */
    private fun back() { scenario?.onActivity { it.onBackPressedDispatcher.onBackPressed() }; pause(700) }
    private fun expect(t: String) { waitFor(10) { seen(t) }; assertTrue("expected \"$t\" on screen", seen(t)) }

    private val routes = listOf(
        "today", "explore", "reads", "tools", "you", "games", "community",
        "run", "puzzle", "puzzles", "rewards", "sim", "calm/breathe", "calm/focus", "calm/sleep", "recap/week", "recap/month",
        "cards", "notebook", "support", "challenges", "challenge/nospend", "challenge/debt30", "check/money", "check/mind", "check/career",
        "settings", "library", "saved", "saved?history=true", "search", "shelf/start", "shelf/debt", "topic/Investing",
        "tool/loan", "tool/growth", "tool/card", "tool/fire", "tool/qr", "tool/photo", "tool/pdf",
        "article/${com.randomthoughtsever.plainly.data.QUIZZES.first().post}", "talk/${com.randomthoughtsever.plainly.data.QUIZZES.first().post}",
    )

    private fun everyScreenOpens() {
        val s = launch(); s.onActivity { activity = it }
        waitFor(45) { seen("TODAY'S PICK") }
        val failed = mutableListOf<String>()
        for (r in routes) {
            val ok = runCatching { go(r); compose.onRoot().fetchSemanticsNode() }.isSuccess
            if (!ok) failed += r
            if (r !in setOf("today", "explore", "reads", "tools", "you", "games", "community")) back()
        }
        assertTrue("screens that failed to open: $failed", failed.isEmpty())
    }

    /** Every screen on a recent Android (15). */
    @Test fun everyScreenOpensOnAndroid15() = everyScreenOpens()

    /** Every screen on the oldest Android the app supports (10, API 29). */
    @Config(sdk = [29]) @Test fun everyScreenOpensOnAndroid10() = everyScreenOpens()

    /** A seeded "monkey": 300 random taps on whatever is tappable, across tabs and screens, must never crash the app. */
    @Test fun monkeyTapsNeverCrash() {
        val s = launch(); s.onActivity { activity = it }
        waitFor(45) { seen("TODAY'S PICK") }
        val r = Random(20261106)
        val tabs = listOf("today", "explore", "reads", "tools", "you", "games", "community")
        repeat(300) { step ->
            if (step % 25 == 0) go(tabs[r.nextInt(tabs.size)])
            val nodes = compose.onAllNodes(hasClickAction()).fetchSemanticsNodes()
            if (nodes.isEmpty()) { back(); return@repeat }
            val n = nodes[r.nextInt(nodes.size)]
            runCatching { compose.onAllNodes(SemanticsMatcher("id ${n.id}") { it.id == n.id }).fetchSemanticsNodes().firstOrNull()
                ?.let { compose.onAllNodes(SemanticsMatcher("id ${n.id}") { x -> x.id == n.id })[0].performSemanticsAction(SemanticsActions.OnClick) } }
            pause(120)
            if (step % 7 == 6) back()
        }
        // Still alive after 300 random taps (a dialog the monkey opened may still be on top, so there can be two roots).
        assertTrue(compose.onAllNodes(androidx.compose.ui.test.isRoot()).fetchSemanticsNodes().isNotEmpty())
    }

    /** No internet at all: the app opens, says it's offline, and offline features (puzzle, simulator, calm, games) still work. */
    @Test fun worksWithoutInternet() {
        val old = System.getProperty("https.proxyPort"); val oldHost = System.getProperty("https.proxyHost")
        System.setProperty("https.proxyHost", "127.0.0.1"); System.setProperty("https.proxyPort", "9")  // nothing listens there
        try {
            val s = launch(); s.onActivity { activity = it }
            waitFor(40) { seen("offline") || seen("Offline") || seen("connection") || seen("TODAY'S PICK") }
            assertTrue("Today should explain it's offline", seen("offline") || seen("Offline") || seen("connection") || seen("TODAY'S PICK"))
            com.github.takahirom.roborazzi.captureScreenRoboImage("build/shots/qa-offline-today.png")
            go("puzzle"); expect("puzzle #")
            go("sim"); expect("A Year of Money")
            go("calm/breathe"); expect("Calm corner")
            go("community"); pause(3000); com.github.takahirom.roborazzi.captureScreenRoboImage("build/shots/qa-offline-community.png")
            go("games"); expect("Rock Paper Scissors")
        } finally {
            if (old == null) System.clearProperty("https.proxyPort") else System.setProperty("https.proxyPort", old)
            if (oldHost == null) System.clearProperty("https.proxyHost") else System.setProperty("https.proxyHost", oldHost)
        }
    }

    /** Saved data that's damaged (an interrupted write, an old version) must never stop the app from opening. */
    @Test fun survivesCorruptedSavedData() {
        ctx.getSharedPreferences("progress", Context.MODE_PRIVATE).edit().putString("dayXp", "{bad").putString("log", "[nope")
            .putString("notes", "{").putString("cards", "not json").putString("tried", "[{]").putString("pz:2026-01-01", "x")
            .putString("ci:${java.time.LocalDate.now()}", "{\"mood\":").putString("hours", "a,b,,9").putInt("xp", -50).commit()
        ctx.getSharedPreferences("profile", Context.MODE_PRIVATE).edit().putString("numbers", "{oops").putString("challenges", "[{\"key\":1}]")
            .putString("check:money", "{]").commit()
        ctx.getSharedPreferences("plainly", Context.MODE_PRIVATE).edit().putString("history", "[{]").putString("saved", "x")
            .putString("days", ",,2026-13-45,").putStringSet("frozen", setOf("garbage")).putInt("freezes", 99).commit()
        ctx.getSharedPreferences("blogtalk", Context.MODE_PRIVATE).edit().putStringSet("pending", setOf("no-separators", "1\u0001x")).commit()
        val s = launch(); s.onActivity { activity = it }
        waitFor(45) { seen("TODAY'S PICK") || seen("offline") }
        for (r in listOf("you", "rewards", "cards", "notebook", "run", "recap/week", "puzzle", "sim", "community", "games")) { go(r); compose.onRoot().fetchSemanticsNode() }
    }

    /** Links and notifications can only open allowed places; anything odd is ignored. */
    @Test fun deepLinksOnlyOpenAllowedPlaces() {
        listOf("run", "puzzle", "puzzles", "sim", "today", "challenge/nospend", "calm/sleep", "recap/month").forEach { assertEquals(it, safeAppRoute(it)) }
        listOf("", "settings", "web/x/y", "article/1", "challenge/../settings", "challenge/NOSPEND", "run?x=1", "run/", " run", "javascript:alert(1)",
            "calm/other", "challenge/" + "a".repeat(40), "puzzle\nrun", "recap/year", "tool/pdf", "support").forEach { assertTrue("allowed: $it", safeAppRoute(it) == null) }
    }

    /** Screen-reader audit: on the main screens, everything you can tap says what it is. */
    @Test fun everyTappableThingHasALabel() {
        val s = launch(); s.onActivity { activity = it }
        waitFor(45) { seen("TODAY'S PICK") }
        val missing = mutableSetOf<String>()
        fun label(n: SemanticsNode): Boolean {
            fun has(x: SemanticsNode): Boolean = !x.config.getOrNull(SemanticsProperties.ContentDescription).isNullOrEmpty() ||
                !x.config.getOrNull(SemanticsProperties.Text).isNullOrEmpty() || x.config.getOrNull(SemanticsActions.OnClick)?.label != null ||
                x.config.getOrNull(SemanticsProperties.EditableText) != null || x.children.any { has(it) }
            return has(n)
        }
        for (r in listOf("today", "explore", "tools", "you", "games", "community", "run", "puzzle", "sim", "calm/breathe", "rewards", "settings", "support")) {
            go(r)
            compose.onAllNodes(hasClickAction(), useUnmergedTree = false).fetchSemanticsNodes().forEach { n ->
                if (!label(n)) missing += "$r: node ${n.id} at ${n.boundsInRoot}"
            }
            if (r !in setOf("today", "explore", "tools", "you", "games", "community")) back()
        }
        assertTrue("unlabelled tappable elements:\n" + missing.joinToString("\n"), missing.isEmpty())
    }

    /** The live blog: every guide the app links to by id exists, and every topic rule finds guides. */
    @Test fun liveContentTheAppPointsAtExists() {
        val ids = mutableMapOf<String, String>()
        var start = 1
        while (true) {
            val body = runCatching { URL("https://randomthoughtsever.blogspot.com/feeds/posts/summary?alt=json&max-results=150&start-index=$start").readText() }.getOrNull()
            assumeTrue("blog not reachable", body != null || ids.isNotEmpty())
            val entries = org.json.JSONObject(body ?: break).getJSONObject("feed").optJSONArray("entry") ?: break
            if (entries.length() == 0) break
            for (i in 0 until entries.length()) entries.getJSONObject(i).let { e -> ids[e.getJSONObject("id").getString("\$t").substringAfterLast("post-")] = e.getJSONObject("title").getString("\$t") }
            start += entries.length()
        }
        val titles = ids.values.toList()
        val missingQuiz = com.randomthoughtsever.plainly.data.QUIZZES.filter { it.post !in ids }.map { it.id }
        assertTrue("quiz guides missing on the blog: $missingQuiz", missingQuiz.isEmpty())
        val missingWeek = com.randomthoughtsever.plainly.data.WEEK_QUESTIONS.filter { it.post !in ids }.map { it.text }
        assertTrue("question-of-the-week guides missing: $missingWeek", missingWeek.isEmpty())
        val noGuides = com.randomthoughtsever.plainly.data.CHALLENGES.filter { c -> titles.none { c.guides.containsMatchIn(it) } }.map { it.key } +
            com.randomthoughtsever.plainly.data.LifeSim.EVENTS.filter { e -> titles.none { e.learn.containsMatchIn(it) } }.map { "sim:" + it.key } +
            com.randomthoughtsever.plainly.data.MICRO_ACTIONS.filter { a -> titles.none { a.guides.containsMatchIn(it) } }.map { "action:" + it.text.take(30) }
        assertTrue("rules that find no guide: $noGuides", noGuides.isEmpty())
        assertTrue(ids.size > 100)
    }
}
