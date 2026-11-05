package com.randomthoughtsever.plainly.ui

import com.randomthoughtsever.plainly.ads.Ads
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.review.ReviewManagerFactory
import com.randomthoughtsever.plainly.data.Library
import com.randomthoughtsever.plainly.data.Repository
import com.randomthoughtsever.plainly.tools.CalculatorScreen
import com.randomthoughtsever.plainly.tools.PdfScreen
import com.randomthoughtsever.plainly.tools.PhotoScreen
import com.randomthoughtsever.plainly.tools.QrScreen
import com.randomthoughtsever.plainly.tools.ToolsScreen

/** App-wide objects, created once in MainActivity. */
object AppGraph {
    lateinit var repo: Repository
    lateinit var library: Library
}

/** Navigation actions shared by every screen. */
class Nav(private val c: NavHostController, private val context: Context) {
    fun article(id: String) = c.navigate("article/$id")
    fun topic(label: String) = c.navigate("topic/${Uri.encode(label)}")
    fun tool(id: String) = c.navigate("tool/$id")
    fun search() = c.navigate("search")
    /** Switches to a bottom-bar tab the same way tapping it does. */
    fun tab(route: String) = c.navigate(route) {
        popUpTo(c.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true; restoreState = true
    }
    fun settings() = c.navigate("settings")
    fun back() { if (!c.popBackStack()) (context as? Activity)?.finish() }
    fun web(url: String) = runCatching {
        CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, Uri.parse(url))
    }
    fun share(title: String, url: String) = context.startActivity(Intent.createChooser(
        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT, title).putExtra(Intent.EXTRA_TEXT, "$title\n$url"), null))
    /** After a file is saved: maybe a full-screen ad (rarely, see Ads.afterTask); if none is shown, maybe the rating prompt. */
    fun afterTask() {
        val activity = context as? Activity ?: return
        if (!Ads.afterTask(activity)) maybeAskForReview()
    }
    /** Google Play's in-app rating prompt, once, after the reader has got real value from the app. */
    fun maybeAskForReview() {
        val lib = AppGraph.library
        val activity = context as? Activity ?: return
        if (!lib.readyForReview()) return
        val m = ReviewManagerFactory.create(activity)
        m.requestReviewFlow().addOnCompleteListener { r -> if (r.isSuccessful) { lib.reviewAsked(); m.launchReviewFlow(activity, r.result) } }
    }
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)
private val TABS = listOf(
    Tab("today", "Today", Icons.Rounded.Today), Tab("explore", "Explore", Icons.Rounded.Explore),
    Tab("tools", "Tools", Icons.Rounded.Handyman), Tab("saved", "Saved", Icons.Rounded.Bookmarks),
)

@Composable
fun PlainlyRoot(context: Context, startArticle: String?, onStartHandled: () -> Unit) {
    val controller = rememberNavController()
    val nav = androidx.compose.runtime.remember(controller) { Nav(controller, context) }
    val entry by controller.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    LaunchedEffect(startArticle) { if (startArticle != null) { nav.article(startArticle); onStartHandled() } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (route in TABS.map { it.route }) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                TABS.forEach { t ->
                    NavigationBarItem(
                        selected = route == t.route,
                        onClick = { nav.tab(t.route) },
                        icon = { Icon(t.icon, null) }, label = { Text(t.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer),
                    )
                }
            }
        },
    ) { pad ->
        NavHost(
            controller, startDestination = "today", modifier = Modifier.padding(bottom = pad.calculateBottomPadding()),
            enterTransition = { fadeIn() + slideInHorizontally { it / 10 } }, exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() }, popExitTransition = { fadeOut() + slideOutHorizontally { it / 10 } },
        ) {
            composable("today") { HomeScreen(nav) }
            composable("explore") { ExploreScreen(nav) }
            composable("tools") { ToolsScreen(nav) }
            composable("saved") { SavedScreen(nav) }
            composable("search") { SearchScreen(nav) }
            composable("settings") { SettingsScreen(nav) }
            composable("article/{id}") { ArticleScreen(it.arguments?.getString("id") ?: "", nav) }
            composable("topic/{label}") { TopicScreen(Uri.decode(it.arguments?.getString("label") ?: ""), nav) }
            composable("tool/{id}") {
                when (val id = it.arguments?.getString("id") ?: "") {
                    "photo" -> PhotoScreen(nav)
                    "pdf" -> PdfScreen(nav)
                    "qr" -> QrScreen(nav)
                    else -> CalculatorScreen(id, nav)
                }
            }
        }
    }
}
