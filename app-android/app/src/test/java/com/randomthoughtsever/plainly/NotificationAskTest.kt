package com.randomthoughtsever.plainly

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.randomthoughtsever.plainly.data.NewPosts
import com.randomthoughtsever.plainly.ui.shouldAskNotifications
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class NotificationAskTest {
    private val c get() = ApplicationProvider.getApplicationContext<Context>()

    @Test @Config(sdk = [35]) fun asksOnceOnTodayAfterOnboarding() {
        NewPosts.prefs(c).edit().clear().apply()
        assertFalse("not before onboarding", shouldAskNotifications(c, onboarded = false, route = "today"))
        assertFalse("not on other screens", shouldAskNotifications(c, onboarded = true, route = "settings"))
        assertTrue(shouldAskNotifications(c, onboarded = true, route = "today"))
        NewPosts.prefs(c).edit().putBoolean("notifyAsked", true).apply()
        assertFalse("only once", shouldAskNotifications(c, onboarded = true, route = "today"))
    }

    @Test @Config(sdk = [35]) fun notWhenAlreadyGranted() {
        NewPosts.prefs(c).edit().clear().apply()
        shadowOf(c as android.app.Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        assertFalse(shouldAskNotifications(c, onboarded = true, route = "today"))
    }

    @Test @Config(sdk = [32]) fun notNeededBeforeAndroid13() {
        NewPosts.prefs(c).edit().clear().apply()
        assertFalse(shouldAskNotifications(c, onboarded = true, route = "today"))
    }
}
