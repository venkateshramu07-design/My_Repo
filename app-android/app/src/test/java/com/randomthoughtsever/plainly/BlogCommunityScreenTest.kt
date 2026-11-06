package com.randomthoughtsever.plainly

import android.content.Context
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

/** The blog-comments community on sample data, in its own test class so it runs in a fresh JVM (see build.gradle.kts). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h840dp-xxhdpi")
class BlogCommunityScreenTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private fun launch(): ActivityScenario<MainActivity> {
        ctx.getSharedPreferences("progress", Context.MODE_PRIVATE).edit().putBoolean("starterHidden", true).commit()
        ctx.getSharedPreferences("plainly", Context.MODE_PRIVATE).edit().putString("theme", "light").putString("currency", "USD")
            .putString("autoDeck", java.time.LocalDate.now().toString()).commit()
        ctx.getSharedPreferences("profile", Context.MODE_PRIVATE).edit().putBoolean("onboarded", true)
            .putStringSet("goals", setOf("debt", "save")).putString("country", "US").commit()
        val s = ActivityScenario.launch(MainActivity::class.java)
        compose.mainClock.autoAdvance = false
        return s
    }
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
        while (System.currentTimeMillis() < end) { compose.mainClock.advanceTimeByFrame(); ShadowLooper.idleMainLooper(50, TimeUnit.MILLISECONDS); Thread.sleep(40) }
    }
    private fun seen(text: String) = compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    private fun tab(label: String) = compose.onNode(hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).performClick()
    private fun tapText(t: String) = compose.onAllNodes(androidx.compose.ui.test.hasClickAction() and hasText(t, substring = true)).onFirst()
        .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick)
    private fun scroll(times: Int) = repeat(times) {
        compose.onRoot().performTouchInput { swipeUp(startY = bottom * .8f, endY = bottom * .25f, durationMillis = 350) }; pause(700)
    }
    private fun shot(name: String) {
        if (runCatching { com.randomthoughtsever.plainly.ui.AppGraph.progress.lastAward != null }.getOrDefault(false)) {
            com.randomthoughtsever.plainly.ui.AppGraph.progress.lastAward = null; pause(900)
        }
        compose.onRoot().captureRoboImage("build/shots/$name.png")
    }

    /**
     * The community without Firebase, on sample blog comments served from this machine (the live blog has few comments).
     * Checks the feed, Top voices, a guide's conversation, and that a post you sent is recognised as yours when it appears.
     */
    @Test fun blogCommunity() {
        fun c(id: String, post: String, name: String, text: String, hoursAgo: Long) = org.json.JSONObject()
            .put("id", org.json.JSONObject().put("\$t", "tag:blogger.com,1999:blog-1.post-$id"))
            .put("published", org.json.JSONObject().put("\$t", java.time.OffsetDateTime.now().minusHours(hoursAgo).toString()))
            .put("content", org.json.JSONObject().put("\$t", text))
            .put("author", org.json.JSONArray().put(org.json.JSONObject().put("name", org.json.JSONObject().put("\$t", name))
                .put("uri", org.json.JSONObject().put("\$t", "https://www.blogger.com/profile/$name"))))
            .put("thr\$in-reply-to", org.json.JSONObject().put("href", "https://randomthoughtsever.blogspot.com/2026/01/g$post.html").put("ref", "tag:blogger.com,1999:blog-1.post-$post"))
        val efund = "9189829005013563974"; val fire = "8447404837524559491"
        val mineText = "Sample: I set up a separate account just for emergencies and it stopped me dipping into it."
        val all = listOf(c("n1", efund, "Asha (sample)", "Sample: Three months felt impossible, so I started with one. Took me 5 months but I got there.", 2),
            c("n2", fire, "Ben (sample)", "Sample: Does the 25x rule still hold if I retire at 40, not 65?", 5),
            c("n3", efund, "Chen (sample)", "Sample: @Asha same here! Small automatic transfers on payday made it painless.", 9),
            c("n4", efund, "You (sample)", mineText, 0))
        fun feed(list: List<org.json.JSONObject>) = org.json.JSONObject().put("feed", org.json.JSONObject().put("entry", org.json.JSONArray(list))).toString()
        // A tiny HTTP server on a plain socket (the JDK's HttpServer isn't on the Android test classpath).
        val server = java.net.ServerSocket(0, 50, java.net.InetAddress.getByName("127.0.0.1"))
        Thread {
            while (!server.isClosed) runCatching {
                server.accept().use { sock ->
                    val path = sock.getInputStream().bufferedReader().readLine().orEmpty().split(' ').getOrElse(1) { "/" }.substringBefore('?')
                    val body = (if (path.startsWith("/feeds/comments")) feed(all) else feed(all.filter { path.contains(it.getJSONObject("thr\$in-reply-to").getString("ref").substringAfterLast("post-")) })).toByteArray()
                    sock.getOutputStream().apply { write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${body.size}\r\nConnection: close\r\n\r\n".toByteArray()); write(body); flush() }
                }
            }
        }.apply { isDaemon = true }.start()
        try {
            val t = com.randomthoughtsever.plainly.data.BlogTalk
            t.base = "http://127.0.0.1:${server.localPort}"
            ctx.getSharedPreferences("community", Context.MODE_PRIVATE).edit().clear().putBoolean("joined", true).putBoolean("welcomed", true).putBoolean("rules", true).commit()
            ctx.getSharedPreferences("blogtalk", Context.MODE_PRIVATE).edit().clear().commit()
            t.init(ctx); t.expect(efund, mineText.replace(".", ""))
            launch(); waitFor { seen("TODAY'S PICK") }
            tab("Community"); waitFor(30, settle = 2000) { seen("Three months felt") }
            org.junit.Assert.assertTrue(seen("How posting works"))
            shot("48-community-blog")
            repeat(6) { if (!seen("You (sample) (you)")) scroll(1) }
            shot("48b-community-blog-mine")
            org.junit.Assert.assertTrue("your post should be recognised", seen("You (sample) (you)"))
            org.junit.Assert.assertTrue(com.randomthoughtsever.plainly.data.Community.discussed)
            compose.onNode(hasText("Top voices") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)).performClick(); pause(1500)
            org.junit.Assert.assertTrue(seen("You (sample) (you)") && seen("💬 1"))
            shot("49-community-top-voices")
            compose.onNode(hasText("Conversations") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)).performClick(); pause(1200)
            repeat(6) { if (compose.onAllNodesWithText("Conversation").fetchSemanticsNodes().isEmpty()) scroll(1) }
            compose.onAllNodes(hasText("Conversation") and androidx.compose.ui.test.hasClickAction()).onFirst()
                .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick); waitFor(15, settle = 1500) { seen("Add to the conversation") }
            shot("50-community-conversation")
            com.randomthoughtsever.plainly.ui.AppGraph.progress.levelUp = null; pause(600)   // the comment's XP can bring a level-up dialog
            tapText("Add to the conversation"); pause(800)
            waitFor(10) { seen("Copy and continue") }
            org.junit.Assert.assertTrue(seen("Copy and continue"))
            shot("51-community-compose")
        } finally { server.close(); com.randomthoughtsever.plainly.data.BlogTalk.base = "https://randomthoughtsever.blogspot.com" }
    }

}
