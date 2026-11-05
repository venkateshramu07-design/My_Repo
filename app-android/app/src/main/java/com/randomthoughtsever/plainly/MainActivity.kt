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

class MainActivity : ComponentActivity() {
    /** An article to open because the app was started from a link to one. */
    private var startArticle by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AppGraph.repo = Repository(applicationContext)
        AppGraph.library = Library(applicationContext)
        AppGraph.appContext = applicationContext
        AppGraph.profile = com.randomthoughtsever.plainly.data.Profile(applicationContext)
        Catalog.load(applicationContext)
        com.randomthoughtsever.plainly.data.Community.init(applicationContext)
        runCatching { PlainlyWidget.refresh(applicationContext) }
        if (AppGraph.profile.joined().isNotEmpty()) com.randomthoughtsever.plainly.data.Reminders.schedule(applicationContext)
        com.randomthoughtsever.plainly.data.Community.publishStats(AppGraph.profile.name, AppGraph.profile.avatar, AppGraph.library.streak(), AppGraph.library.reads())
        if (android.os.Build.FINGERPRINT != "robolectric") Images.install(applicationContext) // tests install their own
        NewPosts.schedule(applicationContext)
        if (savedInstanceState == null) { AppGraph.library.countOpen(); handle(intent) }
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

    override fun onDestroy() {
        Ads.clearBanners()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    /** Opens a blog link inside the app; anything else just opens the app. */
    private fun handle(intent: Intent?) {
        intent?.getStringExtra(NewPosts.EXTRA_POST)?.let { if (it.all(Char::isDigit)) { startArticle = it; return } }
        val uri = intent?.data ?: return
        if (uri.scheme != "https" || uri.host != Blog.HOST) return
        val path = uri.path ?: return
        if (!path.endsWith(".html") || path.startsWith("/p/")) return
        lifecycleScope.launch { AppGraph.repo.idForPath(path)?.let { startArticle = it } }
    }
}
