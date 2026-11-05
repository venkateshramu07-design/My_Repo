package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.Entry
import com.randomthoughtsever.plainly.data.GOALS
import com.randomthoughtsever.plainly.data.Progress
import com.randomthoughtsever.plainly.data.Puzzles
import com.randomthoughtsever.plainly.data.actionOfTheDay
import com.randomthoughtsever.plainly.data.quizOfTheDay
import java.time.LocalDate

private val STEP_INFO = listOf("story" to "📖 Story", "quiz" to "🧠 Quiz", "action" to "✅ Do one thing", "puzzle" to "🧩 Puzzle", "checkin" to "🙂 Mood check-in")

/** Today's guide for the Daily Run: one with a good quick answer, from the reader's goals when possible, new each day. */
fun runStory(index: List<Entry>, goals: Set<String>, day: Long = LocalDate.now().toEpochDay()): Entry? {
    val good = index.filter { !it.isToolPage && it.quick.length > 80 }
    val labels = GOALS.filter { it.key in goals }.flatMap { it.labels }.toSet()
    val pool = good.filter { e -> e.labels.any { it in labels } }.take(40).ifEmpty { good.take(40) }
    return pool.getOrNull((day % pool.size.coerceAtLeast(1)).toInt())
}

/** The Daily Run card at the top of Today: five steps, about five minutes, then "done for today". */
@Composable
fun DailyRunCard(nav: Nav) {
    val pr = AppGraph.progress
    pr.version
    val done = pr.runDone()
    val finished = done.containsAll(Progress.RUN_STEPS)
    Surface(onClick = { nav.run() }, shape = RoundedCornerShape(28.dp), color = Color.Transparent, shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.background(Brush.linearGradient(if (finished) listOf(Color(0xFF0F5F55), Color(0xFF2E9C88)) else listOf(NavyDeep, Color(0xFF3B2F6B)))).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (finished) "DONE FOR TODAY" else "YOUR DAILY RUN · 5 MINUTES", color = Gold, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                    Text(when { finished -> "See you tomorrow ✓"; done.isEmpty() -> "Start today's run"; else -> "Keep going: ${done.size} of 5" },
                        style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
                val streak = pr.runStreak()
                if (streak > 0) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥", style = MaterialTheme.typography.headlineSmall); Text("$streak day${if (streak == 1) "" else "s"}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                STEP_INFO.forEach { (k, label) ->
                    val on = k in done
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(if (on) Gold else Color.White.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                            if (on) Icon(Icons.Rounded.Check, null, tint = NavyDeep, modifier = Modifier.size(20.dp)) else Text(label.substringBefore(" "))
                        }
                        Text(label.substringAfter(" ").substringBefore(" "), color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.labelSmall, maxLines = 1,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
            if (!finished) {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("+${50 + 75} XP today", color = Gold, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Box(Modifier.clip(RoundedCornerShape(50)).background(Gold).padding(horizontal = 18.dp, vertical = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (done.isEmpty()) "Start" else "Continue", color = NavyDeep, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = NavyDeep, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

/** The run itself: one step per screen, with a bar of five segments, and a finish screen with a natural end. */
@Composable
fun DailyRunScreen(nav: Nav) {
    val pr = AppGraph.progress
    pr.version
    val done = pr.runDone()
    val firstOpen = Progress.RUN_STEPS.indexOfFirst { it !in done }.let { if (it < 0) 5 else it }
    var step by rememberSaveable { mutableIntStateOf(firstOpen) }
    fun next() { step = Progress.RUN_STEPS.indexOfFirst { it !in pr.runDone() }.let { if (it < 0) 5 else it } }
    val index = rememberIndex()
    val profile = AppGraph.profile
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = nav::back) { Icon(Icons.Rounded.Close, "Close") }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Progress.RUN_STEPS.forEachIndexed { i, k ->
                    Box(Modifier.weight(1f).height(6.dp).clip(CircleShape).background(when { k in done -> Gold; i == step -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant }).clickableNoRipple { step = i })
                }
            }
            Spacer(Modifier.width(16.dp))
        }
        AnimatedContent(step, Modifier.weight(1f), transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut()) },
            label = "run") { s ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp)) {
                if (s < 5) Text(STEP_INFO[s].second.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                when (s) {
                    0 -> {
                        val e = remember(index) { index?.let { runStory(it, profile.goals) } }
                        if (e == null) Text("Loading today's story…") else {
                            TopicScene(e.label, Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(26.dp)))
                            Spacer(Modifier.height(14.dp))
                            Text(e.title, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(10.dp))
                            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(e.quick, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
                            }
                            TextButton(onClick = { nav.article(e.id) }) { Text("Read the full guide") }
                            Spacer(Modifier.height(10.dp))
                            RunButton("Got it") { pr.award("runstory:${LocalDate.now()}", 10, "Story read", "stories"); pr.runStep("story"); next() }
                        }
                    }
                    1 -> {
                        val q = quizOfTheDay(profile.goals)
                        var answered by remember { mutableStateOf(profile.answered(q.id) != null && "quiz" in done) }
                        QuizBody(q, nav) { answered = true }
                        Spacer(Modifier.height(16.dp))
                        RunButton("Next", enabled = answered || profile.answered(q.id) != null) { pr.runStep("quiz"); next() }
                    }
                    2 -> {
                        val a = actionOfTheDay(profile.goals)
                        Text(a.text, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(a.why, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val guide = remember(index) { index.orEmpty().firstOrNull { !it.isToolPage && a.guides.containsMatchIn(it.title) } }
                        guide?.let { g -> TextButton(onClick = { nav.article(g.id) }) { Text("How: ${g.title}", maxLines = 1) } }
                        Spacer(Modifier.height(16.dp))
                        RunButton("I did it") { pr.award("action:${LocalDate.now()}", 15, "Did one thing", "actions"); pr.runStep("action"); next() }
                        TextButton(onClick = { pr.runStep("action"); next() }, modifier = Modifier.fillMaxWidth()) { Text("I'll do it later today") }
                    }
                    3 -> {
                        val k = Puzzles.kind(); val res = pr.puzzle()
                        Text("${k.emoji} ${k.title} #${Puzzles.number()}", style = MaterialTheme.typography.headlineSmall)
                        Text(k.how, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                        if (res != null) {
                            Text(if (res.solved) "Solved! 🎉" else "Played. New puzzle tomorrow.", style = MaterialTheme.typography.titleLarge)
                            Text(res.grid, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                            RunButton("Next") { pr.runStep("puzzle"); next() }
                        } else RunButton("Play today's puzzle") { nav.puzzle() }
                    }
                    4 -> CheckInBody { next() }
                    else -> RunFinished(nav)
                }
            }
        }
    }
}

/** Mood, spending and a one-line note: ten seconds, and it powers the weekly recap. */
@Composable
fun CheckInBody(saved: () -> Unit) {
    val pr = AppGraph.progress
    val old = pr.checkIn()
    var mood by rememberSaveable { mutableIntStateOf(old?.mood ?: 0) }
    var spend by rememberSaveable { mutableIntStateOf(old?.spend ?: 9) }
    var note by rememberSaveable { mutableStateOf(old?.note ?: "") }
    Text("How are you today?", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        MOODS.forEachIndexed { i, m ->
            val on = mood == i + 1
            val k by animateFloatAsState(if (on) 1.25f else 1f, spring(dampingRatio = .4f), label = "m$i")
            Text(m, style = MaterialTheme.typography.displaySmall, modifier = Modifier.scale(k).clip(CircleShape)
                .background(if (on) Gold.copy(alpha = .3f) else Color.Transparent).clickableNoRipple { mood = i + 1 }.padding(6.dp))
        }
    }
    Spacer(Modifier.height(20.dp))
    Text("Spending today", style = MaterialTheme.typography.titleMedium)
    Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(-1 to "Less than usual", 0 to "Normal", 1 to "More").forEach { (v, l) ->
            Surface(onClick = { spend = v }, shape = RoundedCornerShape(50), color = if (spend == v) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(if (spend == v) 2.dp else 1.dp, if (spend == v) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)) {
                Text(l, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
            }
        }
    }
    OutlinedTextField(note, { note = it.take(80) }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("One word or line (optional)") },
        shape = RoundedCornerShape(16.dp))
    Text("Stays on your phone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
    Spacer(Modifier.height(16.dp))
    RunButton("Save check-in", enabled = mood > 0 && spend != 9) {
        pr.saveCheckIn(Progress.CheckIn(mood, spend, note)); pr.runStep("checkin"); saved()
    }
}

val MOODS = listOf("😞", "😕", "😐", "🙂", "😄")

@Composable
private fun RunFinished(nav: Nav) {
    val pr = AppGraph.progress
    Box {
        Column(Modifier.fillMaxWidth().padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏁", style = MaterialTheme.typography.displayLarge)
            Text("Done for today!", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
            Text("That's your Daily Run. Come back tomorrow for a new story, quiz and puzzle #${Puzzles.number() + 1}.",
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 10.dp))
            Row(Modifier.padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RunStat("${pr.xpOn(LocalDate.now())}", "XP today"); RunStat("🔥 ${pr.runStreak()}", "run streak"); RunStat("Lv ${pr.level()}", pr.title())
            }
            RunButton("Back to Today") { nav.tab("today") }
            Spacer(Modifier.height(10.dp))
            Text("Got a few more minutes?", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { nav.sim() }, shape = RoundedCornerShape(50)) { Text("💰 Money year") }
                OutlinedButton(onClick = { nav.calm() }, shape = RoundedCornerShape(50)) { Text("🌿 Calm minute") }
            }
            Spacer(Modifier.navigationBarsPadding())
        }
        Confetti(Modifier.matchParentSize())
    }
}

@Composable
private fun RunStat(v: String, l: String) = Column(Modifier.clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
    .padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(v, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold); Text(l, style = MaterialTheme.typography.labelMedium)
}
