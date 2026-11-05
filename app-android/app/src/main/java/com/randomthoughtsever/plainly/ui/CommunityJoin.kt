package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.EmojiPeople
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.BlogTalk
import com.randomthoughtsever.plainly.data.CHALLENGES
import com.randomthoughtsever.plainly.data.Community
import com.randomthoughtsever.plainly.data.GOALS
import com.randomthoughtsever.plainly.data.Profile
import com.randomthoughtsever.plainly.data.Reminders
import com.randomthoughtsever.plainly.data.quizOfTheDay
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ---------- Before joining ----------

/** What the community is and why to join, then a three-step join (how you appear, what you'd like to talk about, the rules). */
@Composable
fun JoinCommunity(nav: Nav) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val profile = AppGraph.profile
    var name by rememberSaveable { mutableStateOf(profile.name) }
    var avatar by rememberSaveable { mutableStateOf(profile.avatar) }
    var topics by rememberSaveable { mutableStateOf(profile.goals) }
    AnimatedContent(step, transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut()) },
        label = "join") { s ->
        when (s) {
            0 -> JoinLanding { step = 1 }
            1 -> JoinStep(1, "How do you want to appear?", if (Community.enabled) "Pick a name and an emoji. No real name, email or phone number, ever."
                else "Pick a name and an emoji for your profile in the app. On posts, the blog shows the name of the Google account you comment with.",
                "Next", Community.problem(name) == null && name.trim().length >= 2, back = { step = 0 }, next = { step = 2 }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(avatar, 64); Spacer(Modifier.width(14.dp))
                    OutlinedTextField(name, { name = it.take(24) }, singleLine = true, label = { Text("Display name") }, modifier = Modifier.weight(1f))
                }
                TextButton(onClick = { name = Profile.randomName() }) { Text("🎲  Give me a random name") }
                Spacer(Modifier.height(8.dp))
                Text("Your emoji", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppGraph.progress.avatars().forEach { a ->
                        Surface(onClick = { avatar = a }, shape = CircleShape, color = if (a == avatar) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(if (a == avatar) 2.dp else 1.dp, if (a == avatar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)) {
                            Text(a, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
            2 -> JoinStep(2, "What would you like to talk about?", "We'll show you these first. You can change them any time.",
                "Next", topics.isNotEmpty(), back = { step = 1 }, next = { step = 3 }) {
                GOALS.forEach { g ->
                    val on = g.key in topics
                    Surface(onClick = { topics = if (on) topics - g.key else topics + g.key }, shape = RoundedCornerShape(18.dp),
                        color = if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(if (on) 2.dp else 1.dp, if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(g.from, g.to))), contentAlignment = Alignment.Center) {
                                Icon(g.icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(g.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            if (on) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            3 -> JoinStep(3, "Our community rules", "Short and simple, so it stays a kind, useful place.", "I agree, join", true, back = { step = 2 }, next = {
                profile.setIdentity(name, avatar); profile.updateGoals(topics)
                Community.acceptedRules = true; Community.joinedState = true
            }) {
                listOf("🤝" to "Be kind. No insults, hate or harassment.", "💬" to "Share experience, not financial advice. Nothing here is professional advice.",
                    "🚫" to "No spam, selling or links (guides on the blog are fine).", "🔒" to "Keep yourself safe: no phone numbers, addresses or account details.",
                    "🚩" to if (Community.enabled) "Report anything that breaks these rules. We remove it, and repeat offenders are blocked."
                        else "Report anything that breaks these rules (or hide and block it). The blog's owner removes it, and repeat offenders are blocked.").forEach { (e, t) ->
                    Row(Modifier.padding(vertical = 7.dp)) { Text(e, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(12.dp)); Text(t, style = MaterialTheme.typography.bodyLarge) }
                }
            }
        }
    }
}

@Composable
private fun JoinLanding(join: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(NavyDeep, Navy))).verticalScroll(rememberScrollState()).statusBarsPadding().padding(24.dp)) {
        Box(Modifier.fillMaxWidth().height(170.dp)) {
            listOf("Love & Relationships" to Alignment.CenterStart, "Career & Workplace" to Alignment.Center, "Personal Finance" to Alignment.CenterEnd).forEachIndexed { i, (l, a) ->
                val shown = remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { delay(150L * i); shown.value = true }
                val k by animateFloatAsState(if (shown.value) 1f else .5f, spring(dampingRatio = .45f), label = "pop$i")
                TopicScene(l, Modifier.align(a).size(if (i == 1) 150.dp else 110.dp).scale(k).clip(RoundedCornerShape(30.dp)))
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Join the Plainly community", style = MaterialTheme.typography.displaySmall, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("Real people working on the same things you are: money, a calmer mind, their careers. " +
            if (Community.enabled) "Free, friendly, and no account." else "Free and friendly. Reading is open to all; to post, you use your Google account, like any comment on the blog.",
            style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = .82f))
        Spacer(Modifier.height(18.dp))
        listOf(Icons.Rounded.EmojiPeople to "Share tips and small wins", Icons.AutoMirrored.Rounded.Chat to "Ask questions and get answers from readers",
            Icons.Rounded.Forum to "Discuss every guide with others",
            Icons.Rounded.HowToVote to if (Community.enabled) "See how everyone answered the quizzes" else "Test yourself with a quiz every day",
            Icons.Rounded.EmojiEvents to if (Community.enabled) "Do challenges together and climb the streak board" else "Do challenges and become a top voice").forEach { (icon, t) ->
            Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(CircleShape).background(Gold.copy(alpha = .2f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Gold, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(12.dp))
                Text(t, style = MaterialTheme.typography.titleSmall, color = Color.White)
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = join, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) {
            Text("Join the community", fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
        }
        Text("Takes 30 seconds · 3 quick steps", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = .7f),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun JoinStep(n: Int, title: String, sub: String, cta: String, enabled: Boolean, back: () -> Unit, next: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            LinearProgressIndicator(progress = { n / 3f }, modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape))
            Text("  $n of 3  ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(sub, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            content()
        }
        Button(onClick = next, enabled = enabled, modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(24.dp).height(56.dp),
            shape = RoundedCornerShape(50)) { Text(cta, fontWeight = FontWeight.Bold) }
    }
}

// ---------- After joining: welcome and first steps ----------

/** One of the first things a new member should do, with whether it's done and where it leads. */
data class FirstStep(val key: String, val emoji: String, val title: String, val how: String, val done: Boolean)

/** On the blog-comments community, "Say hello" needs a board post to say it on; without one the step is left out. */
fun firstSteps(): List<FirstStep> {
    val p = AppGraph.profile; val lib = AppGraph.library
    val hello = Community.enabled || BlogTalk.board.isNotBlank()
    return listOfNotNull(
        FirstStep("profile", "🙂", "Set up your profile", "Done when you joined: ${p.avatar} ${p.name}", true),
        if (hello) FirstStep("hello", "👋", "Say hello", "Post a short introduction: what you're working on.", Community.posted) else null,
        FirstStep("poll", "🗳️", if (Community.enabled) "Answer today's poll" else "Answer today's quiz",
            if (Community.enabled) "One quick question. See how others answered." else "One quick question, and why the answer is right.", p.quizStats().second > 0),
        FirstStep("challenge", "🏁", "Join a challenge", "Pick one and check in each day.", p.joined().isNotEmpty() || p.finishedChallenges() > 0),
        FirstStep("discuss", "💬", "Comment on a guide", "Open any guide and add your thoughts under Discussion.", Community.discussed),
        FirstStep("streak", "🔥", "Keep a 3-day streak", "Open Plainly three days in a row. You're on day ${lib.streak().coerceAtLeast(1)}.", lib.streak() >= 3),
    )
}

/** The community home for a member: a welcome the first time, the first-steps checklist until it's done, then the feed. */
@Composable
fun CommunityHome(nav: Nav) {
    var welcomed by remember { mutableStateOf(Community.welcomed) }
    if (!welcomed) { Welcome { Community.welcomed = true; welcomed = true }; return }
    if (Community.enabled) CommunityScreen(nav) else BlogCommunityScreen(nav)
}

@Composable
private fun Welcome(go: () -> Unit) {
    val p = AppGraph.profile
    val k = remember { Animatable(.2f) }
    LaunchedEffect(Unit) { k.animateTo(1f, spring(dampingRatio = .4f, stiffness = 250f)) }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(NavyDeep, Navy))).statusBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Box(contentAlignment = Alignment.Center) {
            TopicScene("Love & Relationships", Modifier.size(220.dp).clip(CircleShape), background = false)
            Box(Modifier.size(120.dp).scale(k.value).clip(CircleShape).background(Brush.linearGradient(listOf(Gold, Color(0xFFC0602A)))), contentAlignment = Alignment.Center) {
                Text(p.avatar, style = MaterialTheme.typography.displayMedium)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("You're in, ${p.name}!", style = MaterialTheme.typography.headlineLarge, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Welcome to the community. Here's what to do first. We'll tick each step off as you go.", style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = .82f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        firstSteps().take(4).forEach { s ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(s.emoji, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(12.dp))
                Text(s.title, style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
                if (s.done) Icon(Icons.Rounded.Check, null, tint = Gold)
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = go, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) { Text("Show me my first steps", fontWeight = FontWeight.Bold) }
    }
}

/** The checklist card: each step says what to do and has a button that goes there. */
@Composable
fun FirstStepsCard(nav: Nav, onSayHello: () -> Unit) {
    AppGraph.profile.version
    val steps = firstSteps()
    val done = steps.count { it.done }
    if (done == steps.size) return
    var poll by remember { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text("YOUR FIRST STEPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$done of ${steps.size} done", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(steps.firstOrNull { !it.done }?.let { "Next: ${it.title}" } ?: "", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(progress = { done / steps.size.toFloat() }, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(6.dp).clip(CircleShape), color = Gold)
            steps.forEach { s ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(enabled = !s.done) {
                    when (s.key) {
                        "hello" -> onSayHello(); "poll" -> poll = !poll; "challenge" -> nav.challenges(); "discuss" -> nav.tab("explore")
                    }
                }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(if (s.done) Gold else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        if (s.done) Icon(Icons.Rounded.Check, null, tint = NavyDeep, modifier = Modifier.size(18.dp)) else Text(s.emoji)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.title, style = MaterialTheme.typography.titleSmall,
                            color = if (s.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                        Text(s.how, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    if (!s.done && s.key != "streak") Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                if (s.key == "poll" && poll && !s.done) Box(Modifier.padding(start = 48.dp, bottom = 8.dp)) { QuizBody(quizOfTheDay(AppGraph.profile.goals), nav) }
            }
        }
    }
}

/** Ready-made first posts, so nobody stares at an empty box. */
fun helloPrompts(): List<String> {
    val p = AppGraph.profile
    val country = WORLD.firstOrNull { it.second == p.country }?.first
    val goal = GOALS.firstOrNull { it.key in p.goals }
    return listOfNotNull(
        "Hi everyone! I'm here to ${goal?.title?.lowercase() ?: "learn more about money"}." + (country?.let { " Saying hello from $it." } ?: ""),
        "Hello! My goal this month: ${when (goal?.key) { "debt" -> "pay an extra $50 off my card"; "save" -> "save 10% of my pay"; "mind" -> "sleep before 11 every night"
            "career" -> "ask for a raise"; "abroad" -> "plan my move abroad"; else -> "read one guide a day" }}. Who's with me?",
        "Hi all! What's one money habit that changed things for you?",
    )
}

@Composable
fun MemberHeader(nav: Nav? = null) {
    val p = AppGraph.profile
    var identity by remember { mutableStateOf(false) }
    if (identity) IdentityDialog { identity = false }
    Row(Modifier.statusBarsPadding().padding(start = 20.dp, end = 12.dp, top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Community", style = MaterialTheme.typography.displaySmall)
            Text("Signed in as ${p.avatar} ${p.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (nav != null) { LevelChip(nav); Spacer(Modifier.width(8.dp)) }
        Surface(onClick = { identity = true }, shape = CircleShape, color = Color.Transparent) { FramedAvatar(p.avatar, 44) }
    }
}

/** Challenges to join, as a strip. */
@Composable
fun ChallengesStrip(nav: Nav) {
    val joined = AppGraph.profile.joined().map { it.key }.toSet()
    Column(Modifier.padding(vertical = 6.dp)) {
        Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Challenges", "See all") { nav.challenges() } }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(CHALLENGES, key = { it.key }) { c ->
                val g = GOALS.firstOrNull { it.key == c.goal }
                Surface(onClick = { nav.challenge(c.key) }, shape = RoundedCornerShape(22.dp), color = Color.Transparent, modifier = Modifier.width(170.dp)) {
                    Column(Modifier.background(Brush.linearGradient(listOf(g?.from ?: Navy, g?.to ?: NavyBright))).padding(14.dp)) {
                        Text(c.emoji, style = MaterialTheme.typography.headlineMedium)
                        Text(c.title, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (c.key in joined) "Joined ✓" else "${c.days} days", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = .85f))
                    }
                }
            }
        }
    }
}

// ---------- A challenge's own page ----------

/** What a challenge is, today's task and check-in, how it works, a tip for today, and guides that help. Opened right after Join. */
@Composable
fun ChallengeScreen(key: String, nav: Nav) {
    val c = CHALLENGES.firstOrNull { it.key == key } ?: CHALLENGES.first()
    val profile = AppGraph.profile
    profile.version
    val j = profile.joined().firstOrNull { it.key == key }
    val g = GOALS.firstOrNull { it.key == c.goal }
    val index = rememberIndex()
    val guides = remember(index) { index.orEmpty().filterNot { it.isToolPage }.filter { c.guides.containsMatchIn(it.title) }.take(3) }
    val dayNo = j?.let { (ChronoUnit.DAYS.between(LocalDate.parse(it.start), LocalDate.now()) + 1).toInt().coerceIn(1, c.days) } ?: 1
    val today = j != null && LocalDate.now().toString() in j.days
    val finished = j != null && j.days.size >= c.days
    val pop = remember { Animatable(1f) }
    LaunchedEffect(today) { if (today) { pop.snapTo(.6f); pop.animateTo(1f, spring(dampingRatio = .35f)) } }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(g?.from ?: Navy, g?.to ?: NavyBright)))) {
                Column(Modifier.statusBarsPadding().padding(start = 8.dp, end = 20.dp, bottom = 22.dp)) {
                    IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White) }
                    Row(Modifier.padding(start = 12.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(c.emoji, style = MaterialTheme.typography.displayMedium, modifier = Modifier.scale(pop.value))
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(if (j == null) "CHALLENGE · ${c.days} DAYS" else "DAY $dayNo OF ${c.days}", style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = .8f), fontWeight = FontWeight.ExtraBold)
                            Text(c.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        }
                    }
                    Text(c.blurb, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = .9f), modifier = Modifier.padding(start = 12.dp, top = 8.dp))
                    if (j != null) {
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.padding(start = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (0 until c.days.coerceAtMost(30)).forEach { d ->
                                val day = LocalDate.parse(j.start).plusDays(d.toLong()).toString()
                                Box(Modifier.weight(1f).height(8.dp).clip(CircleShape).background(when {
                                    day in j.days -> Gold; d < dayNo - 1 -> Color.White.copy(alpha = .15f); else -> Color.White.copy(alpha = .35f) }))
                            }
                        }
                        Text("${j.days.size} of ${c.days} check-ins", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = .85f),
                            modifier = Modifier.padding(start = 12.dp, top = 6.dp))
                    }
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Column(Modifier.padding(18.dp)) {
                    when {
                        j == null -> {
                            Text("Ready to start?", style = MaterialTheme.typography.titleLarge)
                            Text("Day 1 begins as soon as you join. One check-in a day; we'll remind you in the evening if you haven't.",
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { profile.join(c.key); Reminders.schedule(nav.context) }, shape = RoundedCornerShape(50)) { Text("Join and start day 1", fontWeight = FontWeight.Bold) }
                        }
                        finished -> {
                            Text("🏆 You did it!", style = MaterialTheme.typography.headlineSmall)
                            Text("All ${c.days} days complete. Claim your badge, then pick your next challenge.", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { profile.completeChallenge(c.key); AppGraph.progress.award("challenge:${c.key}:${java.time.LocalDate.now()}", 100, "Challenge complete!", "challenges"); nav.challenges() }, shape = RoundedCornerShape(50)) { Text("Claim my badge") }
                        }
                        today -> {
                            Text("✅ Done for today", style = MaterialTheme.typography.titleLarge, color = Color(0xFF2E7D5B))
                            Text("Great work. Come back tomorrow for day ${dayNo + 1}: we'll remind you in the evening.", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        else -> {
                            Text("TODAY'S TASK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
                            Text(c.action, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { profile.checkIn(c.key); AppGraph.progress.award("ci:${c.key}:${java.time.LocalDate.now()}", 20, "Challenge check-in", "checkins_c") }, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                                Icon(Icons.Rounded.Check, null); Spacer(Modifier.width(6.dp)); Text("Check in for day $dayNo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 24.dp)) {
                Text("Tip for today", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.clip(RoundedCornerShape(18.dp)).background(Gold.copy(alpha = .16f)).padding(14.dp)) {
                    Text("💡", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(10.dp))
                    Text(c.tips.getOrElse((dayNo - 1) % c.tips.size.coerceAtLeast(1)) { c.blurb }, style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(Modifier.height(18.dp))
                Text("How it works", style = MaterialTheme.typography.titleLarge)
                c.how.forEachIndexed { i, h ->
                    Row(Modifier.padding(vertical = 6.dp)) {
                        Box(Modifier.size(26.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(Modifier.width(10.dp)); Text(h, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        if (guides.isNotEmpty()) item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Box(Modifier.padding(horizontal = 8.dp)) { SectionTitle("Guides that help") }
                guides.forEach { e -> PostRow(e.ref, { nav.article(e.id) }) }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { nav.share("Join me on the ${c.title} challenge on Plainly! Day ${dayNo} for me. Open it here:", "https://randomthoughtsever.blogspot.com/?plainly=challenge/${c.key}\n(Get Plainly free: https://play.google.com/store/apps/details?id=com.randomthoughtsever.plainly)") },
                    shape = RoundedCornerShape(50)) { Icon(Icons.Rounded.Share, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Invite a friend") }
                if (j != null && !finished) TextButton(onClick = { profile.leave(c.key) }) { Text("Leave challenge") }
            }
        }
    }
}
