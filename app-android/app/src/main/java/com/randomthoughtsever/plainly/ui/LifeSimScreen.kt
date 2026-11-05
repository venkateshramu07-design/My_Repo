package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.LifeSim

/** "A Year of Money": intro, twelve months of events and choices, then a score and what you learned. */
@Composable
fun LifeSimScreen(nav: Nav) {
    val profile = AppGraph.profile
    val pr = AppGraph.progress
    LifeSim.symbol = symbolFor(profile.currency())
    var seed by remember { mutableStateOf(System.currentTimeMillis()) }
    var phase by remember { mutableIntStateOf(0) } // 0 intro, 1 playing, 2 result
    val start = remember(seed) { LifeSim.start(profile.numbers) }
    var state by remember(seed) { mutableStateOf(start) }
    val events = remember(seed) { LifeSim.events(seed) }
    val markets = remember(seed) { LifeSim.markets(seed) }
    var outcome by remember(seed) { mutableStateOf<String?>(null) }
    val worth = remember(seed) { mutableStateListOf(start.netWorth) }
    val index = rememberIndex()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            Text("💰 A Year of Money", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (phase == 1) Text("Month ${(state.month + 1).coerceAtMost(12)} of 12", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 12.dp))
        }
        if (phase == 1) LinearProgressIndicator(progress = { state.month / 12f }, Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(6.dp).clip(CircleShape), color = Gold)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            when (phase) {
                0 -> {
                    Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(28.dp))) { TopicScene("Personal Finance", Modifier.fillMaxSize()) }
                    Spacer(Modifier.height(16.dp))
                    Text("Live a year in five minutes", style = MaterialTheme.typography.headlineMedium)
                    Text("Each month brings pay, bills, interest and the markets, then a moment that needs a decision. Your choices change your money and how you feel. " +
                        "At the end you get a score and the guides behind each lesson.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(Modifier.padding(16.dp)) {
                            Text(if (profile.numbers.isSet) "Starting with your numbers" else "Starting with typical numbers", style = MaterialTheme.typography.titleSmall)
                            Text("Monthly pay ${LifeSim.fmt(start.income)} · spending ${LifeSim.fmt(start.spend)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Savings ${LifeSim.fmt(start.savings)} · debt ${LifeSim.fmt(start.debt)} at 22%", style = MaterialTheme.typography.bodyMedium)
                            if (!profile.numbers.isSet) Text("Add your numbers in You → My numbers to play your own life.", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    if (pr.simBest() >= 0) Text("Your best: ${pr.simBest()} (${LifeSim.grade(pr.simBest())}) · ${pr.simRuns()} years played",
                        style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
                    Spacer(Modifier.height(18.dp))
                    RunButton("Start the year") { phase = 1 }
                }
                1 -> {
                    StatsRow(state)
                    Spacer(Modifier.height(14.dp))
                    val e = events[state.month.coerceAtMost(11)]
                    AnimatedContent(state.month to outcome, transitionSpec = { (fadeIn() + slideInVertically { it / 6 }) togetherWith fadeOut() }, label = "month") { (_, out) ->
                        Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Text(e.emoji, style = MaterialTheme.typography.displayMedium)
                                Text(e.title, style = MaterialTheme.typography.headlineSmall)
                                Text(e.text(state), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 8.dp))
                                if (out == null) e.choices.forEach { c ->
                                    Surface(onClick = {
                                        val before = state
                                        val after = c.apply(state)
                                        val move = markets[state.month]
                                        state = LifeSim.month(after, move)
                                        worth += state.netWorth
                                        outcome = c.result + "\n\nThis month: markets ${if (move >= 0) "up" else "down"} ${"%.1f".format(kotlin.math.abs(move) * 100)}%, " +
                                            "net worth ${if (state.netWorth >= before.netWorth) "up" else "down"} ${LifeSim.fmt(kotlin.math.abs(state.netWorth - before.netWorth))}."
                                        Sounds.tap()
                                    }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Text(c.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(16.dp))
                                    }
                                } else {
                                    Text(out, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    val guide = remember(e.key, index) { index.orEmpty().firstOrNull { !it.isToolPage && e.learn.containsMatchIn(it.title) } }
                                    guide?.let { g ->
                                        TextButton(onClick = { nav.article(g.id) }) {
                                            Icon(Icons.AutoMirrored.Rounded.MenuBook, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Learn: ${g.title}", maxLines = 1)
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    RunButton(if (state.month >= 12) "See my year" else "Next month") {
                                        outcome = null
                                        if (state.month >= 12) { phase = 2; pr.saveSim(LifeSim.score(start, state)) }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> SimResult(start, state, worth, nav) { seed = System.currentTimeMillis(); phase = 1 }
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun StatsRow(s: LifeSim.State) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SimStat("Savings", LifeSim.fmt(s.savings), Color(0xFF2E9C88), Modifier.weight(1f))
        SimStat("Invested", LifeSim.fmt(s.invested), Color(0xFF3B79B5), Modifier.weight(1f))
        SimStat("Debt", LifeSim.fmt(s.debt), Color(0xFFC0392B), Modifier.weight(1f))
    }
    Spacer(Modifier.height(10.dp))
    val w by animateFloatAsState(s.wellbeing / 100f, tween(600), label = "wb")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(when { s.wellbeing >= 70 -> "😄"; s.wellbeing >= 50 -> "🙂"; s.wellbeing >= 30 -> "😕"; else -> "😫" }, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text("Wellbeing ${s.wellbeing}", style = MaterialTheme.typography.labelMedium)
            LinearProgressIndicator(progress = { w }, Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Gold)
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("Net worth", style = MaterialTheme.typography.labelMedium)
            Text(LifeSim.fmt(s.netWorth), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun SimStat(label: String, value: String, color: Color, modifier: Modifier) =
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = .14f)).padding(10.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }

@Composable
private fun SimResult(start: LifeSim.State, end: LifeSim.State, worth: List<Double>, nav: Nav, again: () -> Unit) {
    val score = LifeSim.score(start, end)
    val grade = LifeSim.grade(score)
    val ring by animateFloatAsState(score / 100f, tween(1200), label = "score")
    Box {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawArc(Color.Gray.copy(alpha = .2f), -90f, 360f, false, style = Stroke(22f))
                    drawArc(Brush.sweepGradient(listOf(Gold, Color(0xFF2E9C88), Gold)), -90f, 360f * ring, false, style = Stroke(22f, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(grade, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold)
                    Text("$score / 100", style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(LifeSim.verdict(score), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 10.dp))
            Text("Net worth ${LifeSim.fmt(start.netWorth)} → ${LifeSim.fmt(end.netWorth)} · wellbeing ${start.wellbeing} → ${end.wellbeing}",
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // the year as a line
            val lo = worth.min(); val hi = worth.max().coerceAtLeast(lo + 1)
            val line = MaterialTheme.colorScheme.primary
            Canvas(Modifier.fillMaxWidth().height(120.dp).padding(vertical = 14.dp)) {
                val pts = worth.mapIndexed { i, v -> Offset(size.width * i / (worth.size - 1).coerceAtLeast(1), size.height * (1 - ((v - lo) / (hi - lo)).toFloat())) }
                pts.zipWithNext().forEach { (a, b) -> drawLine(line, a, b, 6f, StrokeCap.Round) }
                pts.lastOrNull()?.let { drawCircle(Gold, 10f, it) }
            }
            Text("Your net worth, month by month", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                nav.context.startActivity(android.content.Intent.createChooser(android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(android.content.Intent.EXTRA_TEXT, "I scored $score ($grade) in A Year of Money on Plainly: ${LifeSim.verdict(score)} Can you beat it?\n" +
                        "https://play.google.com/store/apps/details?id=com.randomthoughtsever.plainly"), null))
            }, shape = RoundedCornerShape(50)) { Icon(Icons.Rounded.Share, null); Spacer(Modifier.width(8.dp)); Text("Challenge a friend") }
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = again, shape = RoundedCornerShape(50)) { Text("Play another year") }
                OutlinedButton(onClick = nav::back, shape = RoundedCornerShape(50)) { Text("Done") }
            }
            SupportNudge(nav, "Liked A Year of Money?")
            Text("Best so far: ${AppGraph.progress.simBest().coerceAtLeast(score)}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
        }
        if (score >= 70) Confetti(Modifier.matchParentSize())
    }
}

