package com.randomthoughtsever.plainly

import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompatApplication

/** Lets the app use on-demand modules (the translator) right after Google Play installs them, without a restart. */
class PlainlyApp : SplitCompatApplication() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Before any library starts (their start-up providers run after this), so even a start-up crash is recorded.
        CrashReport.install(this)
    }
}
