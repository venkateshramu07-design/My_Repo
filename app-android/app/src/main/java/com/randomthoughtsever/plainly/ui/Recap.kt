package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.Progress
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** What a recap covers: a start date and a number of days, with a title. */
data class RecapRange(val from: LocalDate, val days: Int, val title: String, val kicker: String)

fun recapRange(kind: String, today: LocalDate = LocalDate.now()): RecapRange = if (kind == "month") {
    val m = if (today.dayOfMonth <= 5) today.minusMonths(1).withDayOfMonth(1) else today.withDayOfMonth(1)
    val days = if (m.month == today.month) today.dayOfMonth else m.lengthOfMonth()
    RecapRange(m, days, "Your ${m.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())} in Plainly", "Monthly recap")
} else RecapRange(today.minusDays(6), 7, "Your week in Plainly", "Weekly recap")

/** The numbers behind a recap, all from the phone. */
data class RecapStats(val xp: Int, val activeDays: Int, val reads: Int, val quizzes: Int, val puzzles: Int, val runs: Int, val calm: Int,
                      val cards: Int, val moods: List<Int?>, val spend: Int, val level: Int, val title: String)

fun recapStats(pr: Progress, r: RecapRange): RecapStats {
    val dates = (0 until r.days).map { r.from.plusDays(it.toLong()) }
    return RecapStats(dates.sumOf { pr.xpOn(it) }, pr.activeDays(r.from, r.days), pr.statIn("reads", r.from, r.days), pr.statIn("quizzes", r.from, r.days),
        pr.statIn("puzzles", r.from, r.days), pr.statIn("runs", r.from, r.days), pr.statIn("calm", r.from, r.days), pr.statIn("cards", r.from, r.days),
        dates.map { pr.checkIn(it)?.mood }, dates.mapNotNull { pr.checkIn(it)?.spend }.sum(), pr.level(), pr.title())
}

/** Swipe-through story slides for the week or month, ending with a picture to share. */
@Composable
fun RecapScreen(kind: String, nav: Nav) {
    val pr = AppGraph.progress
    val range = remember(kind) { recapRange(kind) }
    val s = remember(kind) { recapStats(pr, range) }
    val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    val slides = 6
    var page by remember { mutableIntStateOf(0) }
    val bar = remember { Animatable(0f) }
    LaunchedEffect(page) { bar.snapTo(0f); if (page < slides - 1) { bar.animateTo(1f, tween(6000, easing = LinearEasing)); page++ } else bar.snapTo(1f) }
    val bgs = listOf(listOf(NavyDeep, Color(0xFF3B2F6B)), listOf(Color(0xFF0F5F55), Color(0xFF2E9C88)), listOf(Color(0xFF8A4B1E), Color(0xFFD08844)),
        listOf(Color(0xFF145A6B), Color(0xFF3AA5BD)), listOf(Color(0xFF7A2E55), Color(0xFFC25A8C)), listOf(NavyDeep, NavyBright))
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(bgs[page])).pointerInput(Unit) {
        detectTapGestures { o -> page = if (o.x < size.width / 3) (page - 1).coerceAtLeast(0) else (page + 1).coerceAtMost(slides - 1) }
    }) {
        Column(Modifier.statusBarsPadding().padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(slides) { i ->
                    Box(Modifier.weight(1f).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .3f))) {
                        Box(Modifier.fillMaxWidth(when { i < page -> 1f; i == page -> bar.value; else -> 0f }).height(4.dp).background(Color.White))
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${range.kicker} · ${range.from.format(fmt)} – ${range.from.plusDays(range.days - 1L).format(fmt)}", color = Color.White.copy(alpha = .85f),
                    style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = nav::back) { Icon(Icons.Rounded.Close, "Close", tint = Color.White) }
            }
        }
        AnimatedContent(page, Modifier.align(Alignment.Center).padding(28.dp), transitionSpec = { (fadeIn() + scaleIn(initialScale = .9f)) togetherWith fadeOut() }, label = "slide") { p ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (p) {
                    0 -> Slide("✨", range.title, "${s.xp}", "XP earned", if (s.activeDays > 0) "You showed up on ${s.activeDays} of ${range.days} days." else "A quiet stretch. Every streak starts with one day.")
                    1 -> Slide("📚", "You learned", "${s.reads}", "guides read", "${s.quizzes} quiz questions answered · ${s.cards} new cards collected")
                    2 -> Slide("🧩", "You played", "${s.puzzles}", "daily puzzles", "${s.runs} Daily Runs finished · puzzle streak ${pr.puzzleStreak()}")
                    3 -> Slide("🌿", "You looked after yourself", "${s.calm}", "calm minutes", "Breathing, focus and sleep sessions add up.")
                    4 -> {
                        Text("🙂", style = MaterialTheme.typography.displayLarge)
                        Text("Your mood", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        Spacer(Modifier.height(16.dp))
                        MoodChart(s.moods, Modifier.fillMaxWidth().height(160.dp))
                        val logged = s.moods.filterNotNull()
                        Text(if (logged.isEmpty()) "Check in during your Daily Run to see your mood here." else
                            "Average ${MOODS[(logged.average().toInt() - 1).coerceIn(0, 4)]} over ${logged.size} check-ins · spending ${when { s.spend < 0 -> "below"; s.spend > 0 -> "above"; else -> "about" }} usual",
                            color = Color.White.copy(alpha = .9f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
                    }
                    else -> {
                        Slide("🏅", "Level ${s.level}", s.title, "", "Total ${pr.xp} XP · ${pr.cards().size} cards · best week ${pr.bestWeek().coerceAtLeast(pr.weekXp())} XP")
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = {
                            ShareCards.share(nav.context, ShareCards.recap(nav.context, range.kicker, range.title, "${s.xp}", "XP earned",
                                listOf("📚 ${s.reads} guides read", "🧩 ${s.puzzles} puzzles", "🌿 ${s.calm} calm minutes", "🏅 Level ${s.level} · ${s.title}")), "plainly-recap")
                        }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) {
                            Icon(Icons.Rounded.Share, null); Spacer(Modifier.width(8.dp)); Text("Share my recap", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Text("Tap left or right", color = Color.White.copy(alpha = .5f), style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp))
    }
}

@Composable
private fun Slide(emoji: String, title: String, big: String, label: String, sub: String) {
    Text(emoji, style = MaterialTheme.typography.displayLarge)
    Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center)
    Text(big, style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.4f), color = Gold,
        fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
    if (label.isNotBlank()) Text(label, style = MaterialTheme.typography.titleLarge, color = Color.White)
    Text(sub, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = .9f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 14.dp))
}

@Composable
private fun MoodChart(moods: List<Int?>, modifier: Modifier) = Canvas(modifier) {
    val n = moods.size.coerceAtLeast(1); val w = size.width / n
    moods.forEachIndexed { i, m ->
        val h = size.height * ((m ?: 0) / 5f)
        drawRoundRect(Color.White.copy(alpha = .15f), Offset(i * w + w * .2f, 0f), Size(w * .6f, size.height), CornerRadius(12f))
        if (m != null) drawRoundRect(Gold, Offset(i * w + w * .2f, size.height - h), Size(w * .6f, h), CornerRadius(12f))
    }
}

/** A card on Today offering the recap: weekly on Sunday and Monday, monthly in the first days of a month. */
@Composable
fun RecapCard(nav: Nav) {
    val today = LocalDate.now()
    val kind = when { today.dayOfMonth <= 3 -> "month"; today.dayOfWeek.value >= 6 || today.dayOfWeek.value == 1 -> "week"; else -> return }
    val s = remember { recapStats(AppGraph.progress, recapRange(kind)) }
    if (s.xp == 0) return
    Surface(onClick = { nav.recap(kind) }, shape = RoundedCornerShape(26.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(Modifier.background(Brush.linearGradient(listOf(Color(0xFF7A2E55), Color(0xFF3B2F6B)))).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("✨", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(if (kind == "month") "YOUR MONTH IN PLAINLY" else "YOUR WEEK IN PLAINLY", color = Gold, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                Text("${s.xp} XP · ${s.reads} guides · ${s.puzzles} puzzles", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text("Tap to watch your recap", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.bodySmall)
            }
            Box(Modifier.size(10.dp))
        }
    }
}
