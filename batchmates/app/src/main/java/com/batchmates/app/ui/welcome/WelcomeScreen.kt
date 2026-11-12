package com.batchmates.app.ui.welcome

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batchmates.app.data.InvitePreview
import com.batchmates.app.ui.components.AuroraBackground
import com.batchmates.app.ui.components.Avatar
import com.batchmates.app.ui.components.GradientButton
import com.batchmates.app.ui.components.Pill
import com.batchmates.app.ui.theme.Brand
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class Slide(val emoji: String, val title: String, val body: String)

private val slides = listOf(
    Slide("🏫", "Find your school", "Browse World → Country → State → District, or simply tap it on the map."),
    Slide("📚", "Pick your batch", "10th or 12th, the year you passed out, and your section — A, B, C, D…"),
    Slide("🤝", "Bring everyone back", "Invite the gang on WhatsApp, Instagram or Facebook. Your reunion starts here."),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(vm: AuthViewModel, isDemo: Boolean, invite: InvitePreview? = null) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var showEmail by rememberSaveable { mutableStateOf(false) }
    val pager = rememberPagerState { slides.size }

    LaunchedEffect(pager) {
        while (true) {
            delay(3800)
            pager.animateScrollToPage((pager.currentPage + 1) % slides.size)
        }
    }

    AuroraBackground {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            if (isDemo) Pill("DEMO MODE · sample data", Brand.Mint)
            Spacer(Modifier.weight(0.4f))
            if (invite != null) InviteHero(invite) else OrbitHero()
            Spacer(Modifier.weight(0.3f))
            Text(
                "BatchMates",
                style = MaterialTheme.typography.displaySmall.copy(
                    brush = Brush.linearGradient(listOf(Color.White, Brand.Sun, Brand.Coral)),
                ),
            )
            Spacer(Modifier.height(4.dp))
            Text("Your school friends are one tap away", color = Brand.TextDim)
            Spacer(Modifier.height(20.dp))
            if (invite == null) HorizontalPager(pager, Modifier.fillMaxWidth().height(96.dp)) { i ->
                val s = slides[i]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${s.emoji}  ${s.title}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(s.body, color = Brand.TextDim, textAlign = TextAlign.Center)
                }
            }
            if (invite == null) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(slides.size) { i ->
                    val active = pager.currentPage == i
                    Box(
                        Modifier.height(6.dp).width(if (active) 22.dp else 6.dp).clip(CircleShape)
                            .background(if (active) Brand.Sun else Color(0x44FFFFFF)),
                    )
                }
            }
            Spacer(Modifier.weight(0.5f))
            GradientButton(
                if (invite != null) "Join with Google" else "Continue with Google",
                onClick = { vm.google(context) },
                modifier = Modifier.fillMaxWidth(),
                loading = state.busy && !showEmail,
                icon = null,
                brush = Brand.Primary,
            )
            Spacer(Modifier.height(12.dp))
            GradientButton(
                "Use email instead",
                onClick = { vm.clearMessages(); showEmail = true },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.Email,
                brush = Brush.linearGradient(listOf(Color(0x26FFFFFF), Color(0x14FFFFFF))),
            )
            if (!showEmail) state.error?.let {
                Spacer(Modifier.height(10.dp)); Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Free forever · No ads · Only your batch sees your posts",
                fontSize = 12.sp, color = Color(0x88FFFFFF), textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showEmail) {
        ModalBottomSheet(
            onDismissRequest = { showEmail = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Brand.Deep,
        ) { EmailSheet(vm) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailSheet(vm: AuthViewModel) {
    val state by vm.state.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPw by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
        SecondaryTabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
            Tab(tab == 0, { tab = 0; vm.clearMessages() }, text = { Text("Sign in") })
            Tab(tab == 1, { tab = 1; vm.clearMessages() }, text = { Text("Create account") })
        }
        Spacer(Modifier.height(16.dp))
        AnimatedContent(tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab") { t ->
            Column {
                if (t == 1) {
                    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Your name") }, singleLine = true, shape = RoundedCornerShape(16.dp))
                    Spacer(Modifier.height(10.dp))
                }
                OutlinedTextField(
                    email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true,
                    shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton({ showPw = !showPw }) {
                            Icon(if (showPw) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, "Show password")
                        }
                    },
                )
            }
        }
        state.error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        state.info?.let { Spacer(Modifier.height(8.dp)); Text(it, color = Brand.Mint) }
        Spacer(Modifier.height(16.dp))
        GradientButton(
            if (tab == 0) "Sign in" else "Create my account",
            onClick = { if (tab == 0) vm.signIn(email, password) else vm.signUp(name, email, password) },
            modifier = Modifier.fillMaxWidth(),
            loading = state.busy,
        )
        if (tab == 0) {
            TextButton({ vm.reset(email) }, Modifier.align(Alignment.CenterHorizontally)) { Text("Forgot password?") }
        }
    }
}

/** What a friend sees after tapping an invite link: who's waiting, before any sign-in. */
@Composable
private fun InviteHero(invite: InvitePreview) {
    val t = rememberInfiniteTransition(label = "inv")
    val bob by t.animateFloat(-4f, 4f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "b")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎉", fontSize = 56.sp, modifier = Modifier.offset(y = bob.dp))
        Text("YOU'RE INVITED", style = com.batchmates.app.ui.theme.Eyebrow)
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Brand.Violet, Brand.Orchid, Brand.Coral))).padding(22.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(invite.schoolName, style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.Center)
                if (invite.regionLabel.isNotBlank()) Text(invite.regionLabel, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(invite.title, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                    invite.faces.take(5).forEach { Avatar(it, null, 44.dp, ring = Brand.Orchid) }
                }
                Spacer(Modifier.height(10.dp))
                val names = invite.faces.take(2)
                val others = invite.memberCount - names.size
                Text(
                    when {
                        names.isEmpty() -> "Your batch is waiting for you"
                        others > 0 -> "${names.joinToString(", ")} and $others other${if (others == 1) "" else "s"} are already here"
                        else -> "${names.joinToString(" and ")} ${if (names.size == 1) "is" else "are"} already here"
                    },
                    color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** Classmates orbiting a graduation cap — the app's signature motion. */
@Composable
private fun OrbitHero() {
    val t = rememberInfiniteTransition(label = "orbit")
    val angle by t.animateFloat(0f, 360f, infiniteRepeatable(tween(24_000, easing = LinearEasing)), label = "a")
    val bob by t.animateFloat(-6f, 6f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "b")
    val names = listOf("Priya S", "Arjun K", "Meena R", "Karthik V", "Divya M", "Rahul P")
    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(250.dp).clip(CircleShape).background(Color(0x0DFFFFFF)))
        Box(Modifier.size(170.dp).clip(CircleShape).background(Color(0x12FFFFFF)))
        Box(
            Modifier.size(96.dp).offset(y = bob.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Brand.Violet, Brand.Coral))),
            contentAlignment = Alignment.Center,
        ) { Text("🎓", fontSize = 46.sp, modifier = Modifier.rotate(bob)) }
        names.forEachIndexed { i, n ->
            val ring = if (i % 2 == 0) 118f else 82f
            val dir = if (i % 2 == 0) 1 else -1
            val a = Math.toRadians((angle * dir + i * 60f).toDouble())
            Box(Modifier.offset((ring * cos(a)).dp, (ring * sin(a) * 0.92 + if (i % 2 == 0) 0.0 else bob * 0.5).dp)) {
                Avatar(n, null, if (i % 2 == 0) 46.dp else 38.dp)
            }
        }
        Text(
            "👋", fontSize = 22.sp,
            modifier = Modifier.offset((118 * cos(Math.toRadians(angle.toDouble()))).dp - 22.dp, (118 * sin(Math.toRadians(angle.toDouble())) * 0.92).dp - 26.dp),
            fontWeight = FontWeight.Bold,
        )
    }
}
