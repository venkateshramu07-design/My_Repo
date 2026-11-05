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

    private fun launch(theme: String = "light", link: String? = null): ActivityScenario<MainActivity> {
        ctx.getSharedPreferences("plainly", Context.MODE_PRIVATE).edit().putString("theme", theme).putString("currency", "USD").commit()
        Coil.setImageLoader(ImageLoader.Builder(ctx).allowHardware(false).crossfade(false).components { add(LocalCovers()) }.build())
        val intent = Intent(ctx, MainActivity::class.java).apply { if (link != null) { action = Intent.ACTION_VIEW; data = Uri.parse(link) } }
        val s = ActivityScenario.launch<MainActivity>(intent)
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

    private fun seen(text: String) = compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    private fun shot(name: String) = compose.onRoot().captureRoboImage("build/shots/$name.png")
    private fun tab(label: String) = compose.onNode(hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).performClick()

    /** Finds a tool with the Tools search box and opens it. */
    private fun openTool(search: String, title: String) {
        compose.onNode(hasSetTextAction()).performTextReplacement(search); pause(1200)
        compose.onAllNodesWithText(title).onFirst().performClick(); pause(2500)
    }

    private fun scroll(times: Int) = repeat(times) {
        compose.onRoot().performTouchInput { swipeUp(startY = bottom * .8f, endY = bottom * .25f, durationMillis = 350) }; pause(700)
    }

    @Test fun today() {
        launch(); waitFor(settle = 5000) { seen("TODAY'S PICK") }
        shot("01-today")
        scroll(1); shot("01b-today-fresh")
        scroll(1); shot("01c-today-money")
        scroll(1); shot("01e-today-answers")
        scroll(6); pause(1500); shot("01d-today-end")
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
