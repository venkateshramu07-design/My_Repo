package com.randomthoughtsever.plainly.ui

import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Groups
import com.randomthoughtsever.plainly.data.Profile
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.navArgument
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
import com.randomthoughtsever.plainly.tools.WebToolScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

/** App-wide objects, created once in MainActivity. */
object AppGraph {
    lateinit var repo: Repository
    lateinit var library: Library
    lateinit var profile: Profile
    lateinit var appContext: android.content.Context
}

/** Navigation actions shared by every screen. */
class Nav(private val c: NavHostController, val context: Context, private val scope: CoroutineScope) {
    fun article(id: String) = c.navigate("article/$id")
    fun topic(label: String) = c.navigate("topic/${Uri.encode(label)}")
    fun tool(id: String) = c.navigate("tool/$id")
    fun webTool(post: String, widget: String) = c.navigate("web/$post/$widget")
    /** Opens another page of the blog inside the app: an article, a topic, or (for anything else) the website in a Custom Tab. */
    fun blogLink(uri: Uri) {
        val path = uri.path ?: ""
        when {
            Regex("^/\\d{4}/\\d{2}/.+\\.html$").matches(path) -> scope.launch {
                val id = AppGraph.repo.idForPath(path)
                if (id != null) article(id) else web(uri.toString())
            }
            path.startsWith("/search/label/") -> topic(Uri.decode(path.removePrefix("/search/label/")))
            else -> web(uri.toString())
        }
    }
    fun external(uri: Uri) = runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    fun search() = c.navigate("search")
    fun search(q: String) = c.navigate("search?q=${Uri.encode(q)}")
    fun shelf(key: String) = c.navigate("shelf/$key")
    fun library() = c.navigate("library")
    fun check(id: String) = c.navigate("check/$id")
    fun thread(id: String) = c.navigate("post/$id")
    /** A guide's conversation in the blog-comments community. */
    fun talk(id: String) = c.navigate("talk/$id")
    fun challenges() = c.navigate("challenges")
    fun challenge(key: String) = c.navigate("challenge/$key")
    fun reads() = c.navigate("reads")
    fun game(id: String) = c.navigate("game/$id")
    fun saved(history: Boolean = false) = c.navigate(if (history) "saved?history=true" else "saved")
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
    Tab("reads", "Reads", Icons.Rounded.AutoStories), Tab("tools", "Tools", Icons.Rounded.Handyman),
    Tab("you", "You", Icons.Rounded.Person), Tab("games", "Games", Icons.Rounded.SportsEsports), Tab("community", "Community", Icons.Rounded.Groups),
)

/** Floating bottom bar: the selected tab's pill grows and fills, its icon pops, with a light haptic tick. */
@Composable
private fun FloatingBar(route: String?, onSelect: (String) -> Unit) {
    val haptics = LocalHapticFeedback.current
    Surface(shape = RoundedCornerShape(30.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp,
        modifier = Modifier.navigationBarsPadding().padding(start = 8.dp, end = 8.dp, bottom = 8.dp, top = 4.dp).fillMaxWidth().height(70.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            TABS.forEach { t ->
                val selected = route == t.route
                val width by animateDpAsState(if (selected) 44.dp else 30.dp, spring(dampingRatio = .55f, stiffness = 500f), label = "pill")
                val scale by animateFloatAsState(if (selected) 1.12f else 1f, spring(dampingRatio = .4f, stiffness = 600f), label = "icon")
                val bg by animateColorAsState(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, label = "bg")
                val tint by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, label = "tint")
                Column(Modifier.weight(if (t.label.length > 7) 1.3f else 1f).fillMaxHeight().selectable(selected, role = Role.Tab, interactionSource = remember { MutableInteractionSource() },
                    indication = null) { if (!selected) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSelect(t.route) },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(Modifier.width(width).height(32.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
                        Icon(t.icon, null, tint = tint, modifier = Modifier.size(21.dp).graphicsLayer { scaleX = scale; scaleY = scale })
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(t.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.sp), color = tint, maxLines = 1,
                        softWrap = false,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PlainlyRoot(context: Context, startArticle: String?, onStartHandled: () -> Unit) {
    val controller = rememberNavController()
    val nav = androidx.compose.runtime.rememberCoroutineScope().let { sc -> remember(controller) { Nav(controller, context, sc) } }
    val entry by controller.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    LaunchedEffect(startArticle) { if (startArticle != null) { nav.article(startArticle); onStartHandled() } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { if (route in TABS.map { it.route }) FloatingBar(route) { nav.tab(it) } },
    ) { pad ->
        NavHost(
            controller, startDestination = if (AppGraph.profile.onboarded) "today" else "onboarding", modifier = Modifier.padding(bottom = pad.calculateBottomPadding()),
            enterTransition = { fadeIn() + slideInHorizontally { it / 10 } }, exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() }, popExitTransition = { fadeOut() + slideOutHorizontally { it / 10 } },
        ) {
            composable("today") { HomeScreen(nav) }
            composable("onboarding") {
                OnboardingScreen(nav) { health ->
                    controller.navigate("today") { popUpTo("onboarding") { inclusive = true } }
                    if (health) nav.check("money")
                }
            }
            composable("community") { if (com.randomthoughtsever.plainly.data.Community.joinedState) CommunityHome(nav) else JoinCommunity(nav) }
            composable("challenge/{key}") { ChallengeScreen(it.arguments?.getString("key") ?: "", nav) }
            composable("games") { GamesScreen(nav) }
            composable("game/{id}") { GameScreen(it.arguments?.getString("id") ?: "rps", nav) }
            composable("post/{id}") { ThreadScreen(it.arguments?.getString("id") ?: "", nav) }
            composable("talk/{id}") { TalkScreen(it.arguments?.getString("id") ?: "", nav) }
            composable("challenges") { ChallengesScreen(nav) }
            composable("check/{id}") { CheckScreen(it.arguments?.getString("id") ?: "money", nav) }
            composable("explore") { ExploreScreen(nav) }
            composable("reads") { QuickReadsScreen(nav) }
            composable("tools") { ToolsScreen(nav) }
            composable("you") { YouScreen(nav) }
            composable("saved?history={history}", listOf(navArgument("history") { defaultValue = "false" })) {
                SavedScreen(nav, it.arguments?.getString("history") == "true")
            }
            composable("search?q={q}", listOf(navArgument("q") { defaultValue = "" })) { SearchScreen(nav, Uri.decode(it.arguments?.getString("q") ?: "")) }
            composable("shelf/{key}") { ShelfScreen(it.arguments?.getString("key") ?: "", nav) }
            composable("library") { LibraryScreen(nav) }
            composable("settings") { SettingsScreen(nav) }
            composable("article/{id}") { ArticleScreen(it.arguments?.getString("id") ?: "", nav) }
            composable("topic/{label}") { TopicScreen(Uri.decode(it.arguments?.getString("label") ?: ""), nav) }
            composable("web/{post}/{widget}") { WebToolScreen(it.arguments?.getString("post") ?: "", it.arguments?.getString("widget") ?: "all", nav) }
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
