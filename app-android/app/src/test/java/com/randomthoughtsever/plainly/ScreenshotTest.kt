package com.randomthoughtsever.plainly

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.onFirst
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import java.io.File
import java.security.MessageDigest
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

/**
 * Renders the real app (live content from the blog) on a virtual phone and saves each screen as a PNG
 * in app/build/shots. Used to check layouts and to make the Play Store screenshots.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h840dp-xxhdpi")
class ScreenshotTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private var scenario: ActivityScenario<MainActivity>? = null
    /** Closes the app after each test, so one test's screens and timers can't keep running into the next. */
    @org.junit.After fun closeApp() { runCatching { scenario?.close() }; scenario = null }

    private fun launch(theme: String = "light", link: String? = null, onboarded: Boolean = true, autoDeck: Boolean = false, starter: Boolean = false): ActivityScenario<MainActivity> {
        ctx.getSharedPreferences("progress", Context.MODE_PRIVATE).edit().putBoolean("starterHidden", !starter).commit()
        ctx.getSharedPreferences("plainly", Context.MODE_PRIVATE).edit().putString("theme", theme).putString("currency", "USD")
            .putString("autoDeck", if (autoDeck) "" else java.time.LocalDate.now().toString())
            .putInt("notifyPromptShown", 3).commit() // the notification invitation has its own test (NotificationAskTest)
        ctx.getSharedPreferences("profile", Context.MODE_PRIVATE).edit().putBoolean("onboarded", onboarded)
            .putStringSet("goals", setOf("debt", "save")).putString("country", "US").commit()
        Coil.setImageLoader(ImageLoader.Builder(ctx).allowHardware(false).crossfade(false).components { add(LocalCovers()) }.build())
        val intent = Intent(ctx, MainActivity::class.java).apply { if (link != null) { action = Intent.ACTION_VIEW; data = Uri.parse(link) } }
        val s = ActivityScenario.launch<MainActivity>(intent)
        scenario = s
        compose.mainClock.autoAdvance = false
        return s
    }

    /**
     * Serves cover art from PNG copies in build/test-covers (made from the live WebP files before the run), because the
     * test machine's image pipeline can't fetch and decode WebP. Anything not found there loads normally.
     */
    private inner class LocalCovers : Interceptor {
        override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
            val url = chain.request.data.toString()
            val sha = MessageDigest.getInstance("SHA-1").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
            val f = File("build/test-covers/$sha.png")
            if (!f.exists()) return chain.proceed(chain.request)
            return SuccessResult(BitmapDrawable(ctx.resources, BitmapFactory.decodeFile(f.path)), chain.request, DataSource.DISK)
        }
    }

    /** Lets real time pass (network, image decoding) while stepping the UI clock, until [done] is true. */
    private fun waitFor(seconds: Int = 45, settle: Int = 0, done: () -> Boolean) {
        val end = System.currentTimeMillis() + seconds * 1000L
        while (System.currentTimeMillis() < end) {
            compose.mainClock.advanceTimeByFrame(); ShadowLooper.idleMainLooper(50, TimeUnit.MILLISECONDS)
            if (runCatching(done).getOrDefault(false)) break
            Thread.sleep(80)
        }
        pause(settle)
    }

    private fun pause(ms: Int) {
        val end = System.currentTimeMillis() + ms
        // One UI frame per step (so the featured carousel doesn't auto-advance mid-shot) while Android's clock moves on
        // in 50 ms steps, so image crossfades finish.
        while (System.currentTimeMillis() < end) { compose.mainClock.advanceTimeByFrame(); ShadowLooper.idleMainLooper(50, TimeUnit.MILLISECONDS); Thread.sleep(40) }
    }

    /** Clicks the first clickable element whose (merged) text contains [t]. */
    private fun tapText(t: String) = compose.onAllNodes(androidx.compose.ui.test.hasClickAction() and hasText(t, substring = true)).onFirst()
        .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick)
    private fun seen(text: String) = compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    /** Takes a picture of the screen, without a "+XP" pop-up caught halfway through its animation. */
    private fun shot(name: String) {
        if (runCatching { com.randomthoughtsever.plainly.ui.AppGraph.progress.lastAward != null }.getOrDefault(false)) {
            com.randomthoughtsever.plainly.ui.AppGraph.progress.lastAward = null; pause(900)
        }
        compose.onRoot().captureRoboImage("build/shots/$name.png")
    }
    private fun tab(label: String) = compose.onNode(hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).performClick()

    /** Finds a tool with the Tools search box and opens it. */
    private fun openTool(search: String, title: String) {
        compose.onNode(hasSetTextAction()).performTextReplacement(search); pause(1200)
        compose.onAllNodesWithText(title).onFirst().performClick(); pause(2500)
    }

    private fun scroll(times: Int) = repeat(times) {
        compose.onRoot().performTouchInput { swipeUp(startY = bottom * .8f, endY = bottom * .25f, durationMillis = 350) }; pause(700)
    }

    @Test fun onboarding() {
        ctx.getSharedPreferences("profile", Context.MODE_PRIVATE).edit().clear().commit()
        launch(onboarded = false); waitFor(20, settle = 1500) { seen("Make it mine") }
        shot("20-onboarding-welcome")
        compose.onAllNodesWithText("Make it mine").onFirst().performClick(); pause(1200)
        compose.onAllNodesWithText("Pay off debt").onFirst().performClick()
        compose.onAllNodesWithText("A calmer mind").onFirst().performClick(); pause(800)
        shot("21-onboarding-goals")
        compose.onAllNodesWithText("Continue").onFirst().performClick(); pause(1200)
        shot("22-onboarding-country")
        compose.onAllNodesWithText("Continue with", substring = true).onFirst().performClick(); pause(1200)
        compose.onAllNodesWithText("See my plan").onFirst().performClick()
        waitFor(40, settle = 1500) { seen("Your plan is ready") }
        shot("23-onboarding-plan")
    }

    @Test fun healthCheck() {
        launch(); waitFor { seen("TODAY'S PICK") }
        compose.onAllNodesWithText("Take the Money Health Check").onFirst().performClick(); pause(1500)
        shot("24-check-question")
        listOf("3 to 5 months", "More than the minimum", "5% to 15%", "Now and then", "Roughly", "Partly").forEach {
            waitFor(10) { seen(it) }; pause(900); compose.onAllNodesWithText(it).onFirst().performClick(); pause(600)
        }
        pause(2500); shot("25-check-result")
    }

    @Test fun personalToday() {
        ctx.getSharedPreferences("profile", Context.MODE_PRIVATE).edit()
            .putString("numbers", "{\"income\":4200,\"spend\":3100,\"savings\":9000,\"invested\":24000,\"debt\":3500,\"debtPayment\":300}").commit()
        launch(); waitFor(settle = 3000) { seen("TODAY'S PICK") }
        scroll(1); shot("26-today-numbers")
        scroll(1); shot("27-today-plan-daily")
        shot("28-today-play-strip")
    }

    @Test fun communityFeed() {
        // Points the app at the local Firebase emulators (see CommunityTest) and adds a few posts.
        val up = runCatching { (java.net.URL("http://127.0.0.1:8080/").openConnection() as java.net.HttpURLConnection).responseCode == 200 }.getOrDefault(false)
        org.junit.Assume.assumeTrue(up)
        val C = com.randomthoughtsever.plainly.data.Community
        C.projectId = "demo-plainly"; C.apiKey = "fake-api-key"
        C.authBase = "http://127.0.0.1:9099/identitytoolkit.googleapis.com"; C.tokenBase = "http://127.0.0.1:9099/securetoken.googleapis.com"
        C.firestoreBase = "http://127.0.0.1:8080"
        ctx.getSharedPreferences("community", Context.MODE_PRIVATE).edit().putBoolean("joined", true).putBoolean("welcomed", true).commit()
        C.init(ctx); C.acceptedRules = true
        kotlinx.coroutines.runBlocking {
            C.resetSession(); C.createPost("Steady Cedar 31", "🌵", "Tip: I moved my savings transfer to payday. I don't miss money I never see.", "Money")
            C.resetSession(); val q = C.createPost("Sunny Lark 58", "🌻", "Has anyone tried the 7-day no-spend challenge? How did day 3 go?", "Ask anything")
            C.comment(q.id, "Sunny Lark 58", "🌻", "Day 3 was the hardest for me!")
            C.like(q.id, true)
            C.resetSession()
        }
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Community"); waitFor(30, settle = 2000) { seen("no-spend") }
        shot("29-community")
    }

    @Test fun today() {
        launch(); waitFor(settle = 5000) { seen("TODAY'S PICK") }
        shot("01-today")
        scroll(1); shot("01b-today-fresh")
        scroll(1); shot("01c-today-money")
        scroll(1); shot("01e-today-answers")
        scroll(6); pause(1500); shot("01d-today-end")
    }

    @Test fun dailyDeckOpensOnce() {
        launch(autoDeck = true); waitFor(30, settle = 1500) { seen("Read the full guide") }
        org.junit.Assert.assertTrue("Today in 60 seconds didn't open", seen("Read the full guide"))
    }

    @Test fun story() {
        launch(); waitFor(settle = 3000) { seen("TODAY'S PICK") }
        compose.onAllNodesWithText("Latest").onFirst().performClick()
        waitFor(20, settle = 1500) { seen("Read the full guide") }
        org.junit.Assert.assertTrue("story viewer did not open", seen("Read the full guide"))
        com.github.takahirom.roborazzi.captureScreenRoboImage("build/shots/11-story.png")
    }

    @Test fun todayDark() {
        launch("dark"); waitFor(settle = 5000) { seen("TODAY'S PICK") }
        shot("09-today-dark")
    }

    @Test fun article() {
        launch(link = "https://randomthoughtsever.blogspot.com/2026/11/american-opportunity-tax-credit-2026.html")
        waitFor(60, settle = 5000) { seen("QUICK ANSWER") }
        shot("02-article")
        repeat(3) { compose.onRoot().performTouchInput { swipeUp(startY = bottom * .85f, endY = top + bottom * .1f, durationMillis = 400) }; pause(600) }
        pause(1500); shot("03-article-body")
    }

    @Test fun explore() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Explore"); waitFor(40, settle = 2500) { seen("Collections") }
        shot("04-explore")
        scroll(1); shot("04b-explore-countries")
        scroll(2); shot("04c-explore-topics")
    }

    @Test fun shelfAndLibrary() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Explore"); waitFor(40, settle = 1500) { seen("Collections") }
        compose.onAllNodesWithText("Start here").onFirst().performClick(); waitFor(20, settle = 2000) { seen("COLLECTION") }
        shot("12-shelf")
    }

    @Test fun quickReads() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Reads"); waitFor(40, settle = 3000) { seen("Swipe up for the next one") }
        shot("13-quick-reads")
    }

    /** The six-tab bar on a small (360 dp) phone: every label must fit. */
    @Test @Config(qualifiers = "w360dp-h740dp-xxhdpi") fun smallPhoneBar() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Community"); pause(1500)
        listOf("Today", "Explore", "Reads", "Community", "Tools", "You", "Games").forEach { l ->
            val n = compose.onAllNodesWithText(l, useUnmergedTree = true).fetchSemanticsNodes().last()
            val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            n.config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
            val r = layouts.first()
            val needs = r.multiParagraph.intrinsics.maxIntrinsicWidth
            println("BAR $l needs=${needs}px has=${n.size.width}px")
            org.junit.Assert.assertTrue("$l is cut off: needs ${needs}px, has ${n.size.width}px", needs <= n.size.width + 1)
        }
        shot("30-small-phone-bar")
    }

    @Test fun joinCommunityAndAChallenge() {
        ctx.getSharedPreferences("community", Context.MODE_PRIVATE).edit().clear().commit()
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Community"); waitFor(10, settle = 1200) { seen("Join the community") }
        shot("40-community-join")
        compose.onAllNodesWithText("Join the community").onFirst().performClick(); pause(1500)
        shot("41-join-step1")
        compose.onAllNodesWithText("Next").onFirst().performClick(); pause(1200)
        compose.onAllNodesWithText("Next").onFirst().performClick(); pause(1200)
        shot("42-join-rules")
        compose.onAllNodesWithText("I agree, join").onFirst().performClick(); waitFor(10, settle = 1500) { seen("You're in", ) }
        org.junit.Assert.assertTrue(seen("You're in"))
        shot("43-community-welcome")
        compose.onAllNodesWithText("Show me my first steps").onFirst().performClick(); pause(1500)
        org.junit.Assert.assertTrue(seen("YOUR FIRST STEPS"))
        shot("44-community-first-steps")
        repeat(6) { if (!seen("7-day no-spend")) scroll(1) }
        scroll(1)   // off the bottom bar
        compose.onAllNodesWithText("7-day no-spend").onFirst().performClick(); pause(1500)
        compose.onAllNodesWithText("Join and start day 1").onFirst().performClick(); pause(1200)
        org.junit.Assert.assertTrue(seen("TODAY'S TASK"))
        shot("45-challenge-day1")
        compose.onAllNodesWithText("Check in for day 1").onFirst().performClick(); pause(1200)
        org.junit.Assert.assertTrue(seen("Done for today"))
        shot("46-challenge-done")
    }

    /** The play layer: Daily Run, daily puzzle, A Year of Money, Calm corner, levels, collection and recap. */
    @Test fun playLayer() {
        ctx.getSharedPreferences("progress", Context.MODE_PRIVATE).edit().clear().commit()
        launch(); waitFor { seen("TODAY'S PICK") }
        waitFor(10, settle = 1500) { seen("DAILY RUN") }
        org.junit.Assert.assertTrue(seen("Start today's run"))
        shot("60-today-daily-run")
        tapText("Start today's run"); waitFor(20, settle = 2500) { seen("Got it") }
        shot("61-run-story")
        tapText("Got it"); pause(1500)
        org.junit.Assert.assertTrue("XP toast", seen("XP"))
        shot("62-run-quiz")
        compose.onNode(androidx.compose.ui.test.hasContentDescription("Close")).performClick(); pause(1200)
        tab("Games"); waitFor(10, settle = 1500) { seen("DAILY PUZZLE") }
        shot("63-games-hub")
        tapText("DAILY PUZZLE"); pause(1800)
        shot("64-daily-puzzle")
        compose.onNode(androidx.compose.ui.test.hasContentDescription("Back")).performClick(); pause(1000)
        tapText("A Year of Money"); pause(1500)
        shot("65-sim-intro")
        tapText("Start the year"); pause(1500)
        shot("66-sim-month")
        val choice = compose.onAllNodes(androidx.compose.ui.test.hasClickAction()).fetchSemanticsNodes().size
        org.junit.Assert.assertTrue(choice > 2)
        compose.onNode(androidx.compose.ui.test.hasContentDescription("Back")).performClick(); pause(1000)
        tab("You"); pause(2000)
        org.junit.Assert.assertTrue(seen("LEVEL"))
        shot("67-you-level")
        tapText("LEVEL"); pause(1500)
        shot("68-rewards")
        compose.onNode(androidx.compose.ui.test.hasContentDescription("Back")).performClick(); pause(1000)
        tapText("Calm corner"); pause(1500)
        shot("69-calm")
        compose.onNode(androidx.compose.ui.test.hasContentDescription("Back")).performClick(); pause(1000)
        tapText("Weekly recap"); pause(1500)
        shot("70-recap")
    }

    @Test fun supportAndLanguages() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("You"); pause(2000)
        repeat(8) { if (!seen("Support Plainly")) scroll(1) }
        tapText("Support Plainly"); pause(2500)
        org.junit.Assert.assertTrue(seen("made by one person"))
        shot("71-support")
    }

    @Test fun translatePicker() {
        launch(link = "https://randomthoughtsever.blogspot.com/2026/11/american-opportunity-tax-credit-2026.html")
        val translate = androidx.compose.ui.test.hasContentDescription("Translate")
        waitFor(90, settle = 2500) { compose.onAllNodes(translate).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(translate).performClick(); pause(1500)
        com.randomthoughtsever.plainly.ui.AppGraph.progress.lastAward = null; pause(900)
        com.github.takahirom.roborazzi.captureScreenRoboImage("build/shots/72-translate-picker.png")
        org.junit.Assert.assertTrue("picker", seen("Read articles in"))
        org.junit.Assert.assertTrue("languages", seen("Español"))
    }

    /** A brand-new reader's first session: the starter path leads, one step at a time, and Today stays light. */
    @Test fun firstSession() {
        launch(starter = true); waitFor { seen("TODAY'S PICK") }
        waitFor(10, settle = 1500) { seen("YOUR FIRST 3 MINUTES") }
        org.junit.Assert.assertTrue(seen("Get your money score") && seen("Play today's puzzle") && seen("collect its card"))
        org.junit.Assert.assertTrue("no Daily Run card on day one", !seen("YOUR DAILY RUN"))
        shot("73-first-session")
        tapText("Get your money score"); pause(2000)
        shot("74-first-step-check")
    }

    /** Large text (160%, as with Android's biggest font setting plus display size): key screens must still fit. */
    @Test fun largeText() {
        org.robolectric.RuntimeEnvironment.setFontScale(1.6f)
        try {
            launch(starter = true); waitFor { seen("TODAY'S PICK") }
            waitFor(10, settle = 1500) { seen("YOUR FIRST 3 MINUTES") }
            shot("80-large-today")
            tapText("Play today's puzzle"); pause(2000)
            shot("81-large-puzzle")
            compose.onNode(androidx.compose.ui.test.hasContentDescription("Back")).performClick(); pause(1000)
            tab("You"); pause(2000)
            shot("82-large-you")
            tab("Games"); pause(1500)
            shot("83-large-games")
        } finally { org.robolectric.RuntimeEnvironment.setFontScale(1f) }
    }

    @Test fun searchWhileTyping() {
        launch(); waitFor { seen("TODAY'S PICK") }
        compose.onNode(androidx.compose.ui.test.hasContentDescription("Search")).performClick(); pause(800)
        compose.onNode(hasSetTextAction()).performTextReplacement("infl"); waitFor(30, settle = 800) { seen("Inflation", ) }
        org.junit.Assert.assertTrue("no instant results for 'infl'", seen("Inflation"))
        shot("47-search-infl")
    }

    @Test fun games() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Games"); waitFor(10, settle = 1500) { seen("Rock Paper Scissors") }
        shot("31-games")
        compose.onAllNodesWithText("Money Match").onFirst().performClick(); pause(1500)
        shot("32-game-match")
    }

    @Test fun you() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("You"); pause(2500)
        shot("14-you")
    }

    @Test fun tools() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Tools"); pause(2500)
        shot("05-tools")
        openTool("loan", "Loan & mortgage calculator")
        shot("06-loan")
    }

    @Test fun growth() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Tools"); pause(2500)
        openTool("investment growth", "Investment growth calculator")
        shot("07-growth")
    }

    @Test fun qr() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Tools"); pause(2500)
        openTool("qr code maker", "QR code maker")
        compose.onNode(hasSetTextAction()).performTextReplacement("randomthoughtsever.blogspot.com"); pause(1500)
        shot("08-qr")
    }

    @Test fun photo() {
        launch(); waitFor { seen("TODAY'S PICK") }
        tab("Tools"); pause(2500)
        openTool("photo resizer", "Photo resizer")
        shot("10-photo")
    }
}
