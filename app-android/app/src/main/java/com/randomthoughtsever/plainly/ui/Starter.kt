package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.randomthoughtsever.plainly.data.Puzzles

/** One step of the first session: what to do, how long, the reward, and whether it's done. */
data class StarterStep(val key: String, val emoji: String, val title: String, val time: String, val xp: Int, val done: Boolean)

fun starterSteps(): List<StarterStep> {
    val p = AppGraph.profile; val pr = AppGraph.progress
    val puzzled = pr.puzzle() != null || Puzzles.archive().any { pr.archived(it) != null }
    return listOf(
        StarterStep("score", "💯", "Get your money score", "60 seconds", 25, p.check("money") != null),
        StarterStep("puzzle", "🧩", "Play today's puzzle", "1–2 minutes", 30, puzzled),
        StarterStep("card", "🃏", "Read a guide, collect its card", "2 minutes", 35, pr.cards().isNotEmpty()),
    )
}

/**
 * "Your first 3 minutes": the first thing a new reader sees on Today. One clear next step at a time, each done in a
 * minute or two, then a celebration, 100 XP and a starter streak freeze. Hidden after that (or if closed).
 */
@Composable
fun StarterPath(nav: Nav) {
    val pr = AppGraph.progress
    pr.version; AppGraph.profile.version
    if (!pr.starterActive(AppGraph.library.firstOpen())) return
    val steps = starterSteps()
    val done = steps.count { it.done }
    var celebrate by remember { mutableStateOf(false) }
    LaunchedEffect(done) { if (done == steps.size && pr.finishStarter()) celebrate = true }
    if (celebrate) StarterDone(nav) { celebrate = false }
    if (done == steps.size) return
    val next = steps.first { !it.done }
    val index = rememberIndex()
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(1f, 1.04f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "p")
    fun go(s: StarterStep) = when (s.key) {
        "score" -> nav.check("money")
        "puzzle" -> nav.puzzle()
        else -> (planFor(AppGraph.profile.goals, index.orEmpty()).firstOrNull() ?: runStory(index.orEmpty(), AppGraph.profile.goals))?.let { nav.article(it.id) } ?: nav.tab("explore")
    }
    Surface(shape = RoundedCornerShape(28.dp), color = Color.Transparent, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.background(Brush.linearGradient(listOf(NavyDeep, Color(0xFF3B2F6B), Color(0xFF7A2E55)))).padding(20.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("WELCOME · YOUR FIRST 3 MINUTES", color = Gold, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                    Text(if (done == 0) "Start here" else "Nice! ${steps.size - done} to go", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
                Box(Modifier.size(54.dp).semantics { contentDescription = "$done of ${steps.size} steps done" }, contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawArc(Color.White.copy(alpha = .18f), -90f, 360f, false, style = Stroke(10f))
                        drawArc(Gold, -90f, 360f * done / steps.size, false, style = Stroke(10f, cap = StrokeCap.Round))
                    }
                    Text("$done/${steps.size}", color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(14.dp))
            steps.forEachIndexed { i, s ->
                val current = s == next
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(18.dp))
                    .background(if (current) Color.White.copy(alpha = .14f) else Color.Transparent)
                    .clickable(enabled = !s.done, role = Role.Button, onClickLabel = s.title) { go(s) }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(if (s.done) Gold else Color.White.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                        if (s.done) Icon(Icons.Rounded.Check, "Done", tint = NavyDeep) else Text(s.emoji, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${i + 1}. ${s.title}", color = Color.White.copy(alpha = if (s.done) .6f else 1f), style = MaterialTheme.typography.titleSmall)
                        Text(if (s.done) "Done" else "${s.time} · +${s.xp} XP", color = Color.White.copy(alpha = .75f), style = MaterialTheme.typography.labelMedium)
                    }
                    if (current) Box(Modifier.scale(pulse).clip(RoundedCornerShape(50)).background(Gold).padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Go", color = NavyDeep, fontWeight = FontWeight.Bold); Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = NavyDeep, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Text("🎁 Finish all three: +100 XP and a streak freeze", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = { pr.starterHidden = true }) { Text("Skip", color = Color.White.copy(alpha = .7f)) }
            }
        }
    }
}

@Composable
private fun StarterDone(nav: Nav, close: () -> Unit) {
    Dialog(onDismissRequest = close) {
        Box {
            Surface(shape = RoundedCornerShape(32.dp), color = NavyDeep) {
                Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 64.sp)
                    Text("You're all set!", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("Money score, first puzzle and first card: done. Here's +100 XP and a streak freeze 🧊 to start your streak.",
                        color = Color.White.copy(alpha = .85f), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                    Text("From tomorrow, your 5-minute Daily Run is the best way to keep going.", color = Gold, textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = { close(); nav.run() }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) {
                        Text("Try today's Daily Run", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = close) { Text("Explore on my own", color = Color.White.copy(alpha = .8f)) }
                }
            }
            Confetti(Modifier.matchParentSize())
        }
    }
}

