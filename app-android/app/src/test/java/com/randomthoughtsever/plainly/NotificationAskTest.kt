package com.randomthoughtsever.plainly

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.randomthoughtsever.plainly.data.NewPosts
import com.randomthoughtsever.plainly.ui.NotifyPrompt
import com.randomthoughtsever.plainly.ui.NotifyPromptDialog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
class NotificationAskTest {
    @get:Rule val compose = createComposeRule()
    private val c get() = ApplicationProvider.getApplicationContext<Context>()
    private val day = 86_400_000L

    @Test @Config(sdk = [35]) fun showsOnTodayThenEveryThreeDaysAtMostThreeTimes() {
        NewPosts.prefs(c).edit().clear().apply()
        assertFalse("not before onboarding", NotifyPrompt.shouldShow(c, false, "today", 0))
        assertFalse("not on other screens", NotifyPrompt.shouldShow(c, true, "settings", 0))
        assertTrue(NotifyPrompt.shouldShow(c, true, "today", 0))
        NotifyPrompt.markShown(c, 0)
        assertFalse("not again the next day", NotifyPrompt.shouldShow(c, true, "today", day))
        assertTrue("again after 3 days", NotifyPrompt.shouldShow(c, true, "today", 3 * day))
        NotifyPrompt.markShown(c, 3 * day); NotifyPrompt.markShown(c, 6 * day)
        assertFalse("at most 3 times", NotifyPrompt.shouldShow(c, true, "today", 30 * day))
    }

    @Test @Config(sdk = [35]) fun neverWhenAlreadyAllowed() {
        NewPosts.prefs(c).edit().clear().apply()
        shadowOf(c as android.app.Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        assertFalse(NotifyPrompt.shouldShow(c, true, "today", 0))
    }

    @Test @Config(sdk = [32]) fun notNeededBeforeAndroid13() {
        NewPosts.prefs(c).edit().clear().apply()
        assertFalse(NotifyPrompt.shouldShow(c, true, "today", 0))
    }

    @Test @Config(sdk = [35], qualifiers = "w400dp-h840dp-xxhdpi") fun invitationShowsItsPromise() {
        compose.setContent { com.randomthoughtsever.plainly.ui.PlainlyTheme("light") { NotifyPromptDialog("Form I-9 in 2026: Accepted Documents, Deadlines and the New Fine Rules") {} } }
        compose.onNodeWithText("Be the first to know").assertIsDisplayed()
        compose.onNodeWithText("Yes, keep me posted").assertIsDisplayed()
        compose.onNodeWithText("Not now").assertIsDisplayed()
        compose.onNodeWithText("Form I-9 in 2026: Accepted Documents, Deadlines and the New Fine Rules").assertIsDisplayed()
        com.github.takahirom.roborazzi.captureScreenRoboImage("build/notify-prompt.png")
    }
}
