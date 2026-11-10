package com.randomthoughtsever.plainly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.randomthoughtsever.plainly.ads.Ads
import com.randomthoughtsever.plainly.data.Blog
import com.randomthoughtsever.plainly.data.Images
import com.randomthoughtsever.plainly.data.NewPosts
import com.randomthoughtsever.plainly.tools.Catalog
import com.randomthoughtsever.plainly.data.Library
import com.randomthoughtsever.plainly.data.Repository
import com.randomthoughtsever.plainly.ui.AppGraph
import com.randomthoughtsever.plainly.ui.PlainlyRoot
import com.randomthoughtsever.plainly.ui.PlainlyTheme
import kotlinx.coroutines.launch

/** Tests switch the crash screen on explicitly; other tests share one app folder and must start normally. */
internal object CrashReportTesting { @JvmField var enabled = false }

class MainActivity : ComponentActivity() {
    /** An article to open because the app was started from a link to one. */
    private var startArticle by mutableStateOf<String?>(null)
    /** False while the crash report screen is showing instead of the app. */
    private var started = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val diagnostics = android.os.Build.FINGERPRINT != "robolectric" || CrashReportTesting.enabled
        if (diagnostics) CrashReport.stage(applicationContext, "activity")
        // If the last run crashed, show the saved error once (plain views, before anything else starts, even the splash).
        val crash = if (diagnostics) CrashReport.pending(applicationContext) else null
        if (crash != null) {
            setTheme(android.R.style.Theme_Material_Light_NoActionBar)
            super.onCreate(savedInstanceState)
            CrashReport.show(this, crash) { recreate() }
            return
        }
        started = true
        installSplashScreen()
        if (diagnostics) CrashReport.stage(applicationContext, "splash")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AppGraph.repo = Repository(applicationContext)
        AppGraph.library = Library(applicationContext)
        AppGraph.appContext = applicationContext
        AppGraph.profile = com.randomthoughtsever.plainly.data.Profile(applicationContext)
        AppGraph.progress = com.randomthoughtsever.plainly.data.Progress(applicationContext)
        Catalog.load(applicationContext)
        com.randomthoughtsever.plainly.data.Community.init(applicationContext)
        com.randomthoughtsever.plainly.data.BlogTalk.init(applicationContext)
        com.randomthoughtsever.plainly.data.BlogTalk.onPublished = { n -> AppGraph.progress.award("comment:${n.id}", 40, "Your comment is live", "comments") }
        runCatching { PlainlyWidget.refresh(applicationContext); PlayWidget.refresh(applicationContext) }
        if (savedInstanceState == null) AppGraph.progress.recordOpen(java.time.LocalTime.now().hour)
        com.randomthoughtsever.plainly.data.Reminders.schedule(applicationContext)
        com.randomthoughtsever.plainly.data.Community.publishStats(AppGraph.profile.name, AppGraph.profile.avatar, AppGraph.library.streak(), AppGraph.library.reads())
        if (android.os.Build.FINGERPRINT != "robolectric") Images.install(applicationContext) // tests install their own
        NewPosts.schedule(applicationContext)
        com.randomthoughtsever.plainly.data.ArticlePicks.schedule(applicationContext)
        if (savedInstanceState == null) { AppGraph.library.countOpen(); handle(intent) }
        if (diagnostics) CrashReport.stage(applicationContext, "setup")
        Ads.start(this)

        setContent {
            val lib = AppGraph.library
            val dark = when (lib.theme) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark; isAppearanceLightNavigationBars = !dark
                }
            }
            PlainlyTheme(lib.theme) {
                PlainlyRoot(this, startArticle) { startArticle = null }
            }
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(newBase)
        com.google.android.play.core.splitcompat.SplitCompat.installActivity(this)
    }

    override fun onResume() {
        super.onResume()
        if (started) AppGraph.progress.reload()
        // Counts as a good start once the screen has been up for a moment (a start that dies right away doesn't get here).
        if (started && (android.os.Build.FINGERPRINT != "robolectric" || CrashReportTesting.enabled))
            window.decorView.postDelayed({ CrashReport.shown(applicationContext) }, 2_000)
    }

    override fun onPause() {
        if (started) runCatching { PlayWidget.refresh(applicationContext) }
        super.onPause()
    }

    override fun onDestroy() {
        if (started) Ads.clearBanners()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (started) handle(intent)
    }

    /** Opens a blog link inside the app; anything else just opens the app. */
    private fun handle(intent: Intent?) {
        intent?.getStringExtra(NewPosts.EXTRA_POST)?.let { if (it.all(Char::isDigit)) { startArticle = it; return } }
        intent?.getStringExtra(com.randomthoughtsever.plainly.data.Reminders.EXTRA_ROUTE)?.let { AppGraph.route.value = safeRoute(it); return }
        val uri = intent?.data ?: return
        if (uri.scheme != "https" || uri.host != Blog.HOST) return
        // Invites: https://randomthoughtsever.blogspot.com/?plainly=challenge/nospend opens that page in the app.
        uri.getQueryParameter("plainly")?.let { AppGraph.route.value = safeRoute(it); return }
        val path = uri.path ?: return
        if (!path.endsWith(".html") || path.startsWith("/p/")) return
        lifecycleScope.launch { AppGraph.repo.idForPath(path)?.let { startArticle = it } }
    }

    /** Only these in-app places can be opened from a link or notification. */
    private fun safeRoute(r: String): String? = safeAppRoute(r)
}

/** Only these in-app places can be opened from a link or notification; anything else is ignored. */
internal fun safeAppRoute(r: String): String? =
    r.takeIf { Regex("^(run|puzzle|puzzles|sim|today|games|rewards|cards|calm/(breathe|focus|sleep)|recap/(week|month)|challenge/[a-z0-9]{2,20})$").matches(it) }
