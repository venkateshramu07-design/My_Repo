package com.batchmates.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.batchmates.app.data.AppUser
import com.batchmates.app.ui.finder.FinderScreen
import com.batchmates.app.ui.finder.FinderViewModel
import com.batchmates.app.ui.group.GroupScreen
import com.batchmates.app.ui.group.GroupViewModel
import com.batchmates.app.ui.home.HomeScreen
import com.batchmates.app.ui.home.HomeViewModel
import com.batchmates.app.ui.theme.BatchMatesTheme
import com.batchmates.app.ui.welcome.AuthViewModel
import com.batchmates.app.ui.welcome.WelcomeScreen
import com.batchmates.app.util.InviteCodes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {

    /** Invite code from a tapped link, held until the user is signed in. */
    private val pendingCode = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) readInvite(intent)
        val container = (application as BatchMatesApp).container

        setContent {
            BatchMatesTheme {
                val user by container.auth.currentUser.collectAsState()
                val code by pendingCode.collectAsState()
                Crossfade(user, label = "auth") { u ->
                    if (u == null) {
                        val vm = viewModel { AuthViewModel(container.auth) }
                        // Invited? Show who's waiting before asking anyone to sign in.
                        val invite by androidx.compose.runtime.produceState<com.batchmates.app.data.InvitePreview?>(null, code) {
                            value = code?.let { container.reunion.invitePreview(it) }
                        }
                        WelcomeScreen(vm, container.isDemo, invite)
                    } else {
                        AppNav(container, u, code, onCodeUsed = { pendingCode.value = null }) {
                            lifecycleScope.launch { container.auth.signOut() }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readInvite(intent)
    }

    private fun readInvite(intent: Intent?) {
        val data = intent?.data ?: return
        val raw = data.lastPathSegment ?: return
        if (InviteCodes.isPlausible(raw)) pendingCode.value = InviteCodes.normalize(raw)
    }
}

@Composable
private fun AppNav(
    container: AppContainer,
    user: AppUser,
    pendingCode: String?,
    onCodeUsed: () -> Unit,
    onSignOut: () -> Unit,
) {
    val nav = rememberNavController()
    NavHost(
        nav, startDestination = "home",
        enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() + scaleIn(initialScale = 0.96f) },
        popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() },
    ) {
        composable("home") {
            val vm = viewModel(key = user.uid) { HomeViewModel(container.reunion, user) }
            HomeScreen(
                vm, user, container.isDemo, pendingCode, onCodeUsed,
                onBrowse = { nav.navigate("find?map=false") },
                onMap = { nav.navigate("find?map=true") },
                onOpenGroup = { id, celebrate -> nav.navigate("group/$id?celebrate=$celebrate") },
                onSignOut = onSignOut,
            )
        }
        composable("find?map={map}", arguments = listOf(navArgument("map") { type = NavType.BoolType; defaultValue = false })) { entry ->
            val map = entry.arguments?.getBoolean("map") ?: false
            val vm = viewModel { FinderViewModel(container.geo, container.reunion, user, map) }
            FinderScreen(
                vm, onExit = { nav.popBackStack() },
                onJoined = { id ->
                    nav.navigate("group/$id?celebrate=true") { popUpTo("home") }
                },
            )
        }
        composable(
            "group/{id}?celebrate={celebrate}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("celebrate") { type = NavType.BoolType; defaultValue = false },
            ),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val vm = viewModel { GroupViewModel(container.reunion, user, id) }
            GroupScreen(vm, entry.arguments?.getBoolean("celebrate") ?: false, onBack = { nav.popBackStack() })
        }
    }
}
