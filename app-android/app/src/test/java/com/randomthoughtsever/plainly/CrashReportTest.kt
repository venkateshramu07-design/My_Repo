package com.randomthoughtsever.plainly

import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A saved crash is shown once on the next start, with the error text; Continue clears it and starts the app. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class CrashReportTest {
    private val c get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    @After fun off() { CrashReportTesting.enabled = false; CrashReport.clear(c) }

    @Test fun startThatNeverReachedTheScreenIsReported() {
        val stages = java.io.File(c.filesDir, "start-stages.txt")
        stages.writeText("start\nsplits\napp\nactivity")
        CrashReport.beginRun(c)
        val r = CrashReport.pending(c)!!
        assertTrue(r, r.contains("Last start reached: start > splits > app > activity") && r.contains("Splits:"))
        assertEquals("start", stages.readText())
        // A start that reached the screen leaves no report.
        CrashReport.clear(c); CrashReport.shown(c); CrashReport.beginRun(c)
        assertNull(CrashReport.pending(c))
    }

    @Test fun savedCrashIsShownThenCleared() {
        val report = CrashReport.describe(c, "main", IllegalStateException("boom at start"))
        assertTrue(report.contains("Plainly ${BuildConfig.VERSION_NAME}") && report.contains("IllegalStateException: boom at start"))
        java.io.File(c.filesDir, "last-crash.txt").writeText(report)
        CrashReportTesting.enabled = true
        ActivityScenario.launch(MainActivity::class.java).use { s ->
            s.onActivity { a ->
                val texts = mutableListOf<String>()
                fun walk(v: android.view.View) { if (v is TextView) texts += v.text.toString(); if (v is android.view.ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i)) }
                walk(a.window.decorView)
                assertTrue(texts.any { it.contains("closed unexpectedly") })
                assertTrue(texts.any { it.contains("boom at start") })
                assertEquals(3, texts.count { it == "Copy" || it == "Share" || it == "Continue" })
            }
        }
        CrashReport.clear(c)
        assertNull(CrashReport.pending(c))
    }
}
