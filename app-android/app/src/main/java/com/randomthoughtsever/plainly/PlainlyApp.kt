package com.randomthoughtsever.plainly

import android.content.Context
import android.os.Build
import com.google.android.play.core.splitcompat.SplitCompatApplication

/** Lets the app use on-demand modules (the translator) right after Google Play installs them, without a restart. */
class PlainlyApp : SplitCompatApplication() {
    private val diagnostics get() = Build.FINGERPRINT != "robolectric" || CrashReportTesting.enabled

    override fun attachBaseContext(base: Context) {
        // Before anything else, even Play's split loader, so a failure anywhere in start-up is recorded.
        if (diagnostics) { CrashReport.beginRun(base); CrashReport.install(base) }
        super.attachBaseContext(base)
        if (diagnostics) CrashReport.stage(base, "splits")
    }

    override fun onCreate() {
        super.onCreate()
        if (diagnostics) CrashReport.stage(this, "app")
    }
}
