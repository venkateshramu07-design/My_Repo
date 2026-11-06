package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.time.LocalDate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomthoughtsever.plainly.data.LifeSim
import com.randomthoughtsever.plainly.data.Progress
import com.randomthoughtsever.plainly.data.Puzzles
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

private val RIGHT = Color(0xFFD9A75E)   // gold: right letter, right place
private val NEAR = Color(0xFF2E9C88)    // teal: in the word, elsewhere

/** A daily puzzle (today's, or a past one from the archive), then the result with an emoji grid to share. */
@Composable
fun PuzzleScreen(nav: Nav, date: LocalDate = LocalDate.now()) {
    val pr = AppGraph.progress
    pr.version
    val today = date == LocalDate.now()
    val kind = Puzzles.kind(date)
    val result = pr.puzzleOn(date)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) {
                Text("${kind.emoji} ${kind.title}", style = MaterialTheme.typography.titleLarge)
                Text(if (today) "Daily puzzle #${Puzzles.number(date)} · the same for everyone today" else "Past puzzle #${Puzzles.number(date)} · " +
                    date.format(java.time.format.DateTimeFormatter.ofPattern("d MMM")), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val streak = pr.puzzleStreak()
            if (today && streak > 0) Text("🔥 $streak", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 12.dp).semantics { contentDescription = "Puzzle streak $streak days" })
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            if (result != null) PuzzleResult(kind, date, result, nav)
            else {
                Text(kind.how, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                val save: (Progress.PuzzleResult) -> Unit = { pr.savePuzzleOn(date, it) }
                when (kind) {
                    Puzzles.Kind.WORD -> WordVault(date, save)
                    Puzzles.Kind.BUDGET -> BudgetBalance(date, save)
                    Puzzles.Kind.GROWTH -> GrowthOrder(date, save)
                    Puzzles.Kind.SCAM -> SpotTheScam(date, save)
                    Puzzles.Kind.DOLLAR -> ShrinkingDollar(date, save)
                }
            }
        }
    }
}

@Composable
private fun PuzzleResult(kind: Puzzles.Kind, date: LocalDate, r: Progress.PuzzleResult, nav: Nav) {
    val ctx = nav.context
    val today = date == LocalDate.now()
    var left by remember { mutableStateOf(untilMidnight()) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1000); left = untilMidnight() } }
    Box {
        Column(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (r.solved) "🎉 Solved!" else "Not this time", style = MaterialTheme.typography.headlineLarge)
            Text(when (kind) { Puzzles.Kind.WORD -> if (r.solved) "In ${r.score} of 6 tries" else "The word was ${Puzzles.word(date)}"
                Puzzles.Kind.BUDGET -> if (r.solved) "In ${r.score} of 5 checks" else "So close. Try another one."
                Puzzles.Kind.GROWTH -> if (r.solved) "In ${r.score} of 3 tries" else "Compound growth is tricky!"
                Puzzles.Kind.SCAM -> "${r.score} of 5 scams spotted"
                Puzzles.Kind.DOLLAR -> "${r.score} of 5 right" },
                style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(r.grid, fontSize = 26.sp, lineHeight = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 16.dp))
            Button(onClick = {
                ctx.startActivity(android.content.Intent.createChooser(android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(android.content.Intent.EXTRA_TEXT, "Plainly ${kind.title} #${Puzzles.number(date)} ${if (r.solved) "✅" else "❌"}\n${r.grid}\n" +
                        "Play the daily puzzle: https://play.google.com/store/apps/details?id=com.randomthoughtsever.plainly"), null))
            }, shape = RoundedCornerShape(50)) { Icon(Icons.Rounded.Share, null); Spacer(Modifier.width(8.dp)); Text("Share my result") }
            if (today) {
                Text("Next puzzle in $left", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 18.dp))
                Text("Puzzle streak: ${AppGraph.progress.puzzleStreak()} day(s)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { nav.puzzles() }, shape = RoundedCornerShape(50)) { Text("Past puzzles") }
                OutlinedButton(onClick = { nav.games() }, shape = RoundedCornerShape(50)) { Text("More games") }
            }
        }
        if (r.solved) Confetti(Modifier.matchParentSize())
    }
}

/** The last 30 days of puzzles, to catch up on or replay the ones you missed. */
@Composable
fun PuzzleArchiveScreen(nav: Nav) {
    val pr = AppGraph.progress
    pr.version
    val days = remember { Puzzles.archive() }
    androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
        item {
            Row(Modifier.statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Column {
                    Text("Past puzzles", style = MaterialTheme.typography.titleLarge)
                    Text("Catch up on the last 30 days. They don't change your puzzle streak.", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { PuzzleCard(nav) }
        if (days.isEmpty()) item { EmptyCentered("The archive fills up day by day. Come back tomorrow for yesterday's puzzle.") }
        days.forEach { d ->
            item(key = d.toString()) {
                val k = Puzzles.kind(d); val r = pr.puzzleOn(d)
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surface)
                    .clickable(onClickLabel = "Play") { nav.puzzle(d) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(k.emoji, fontSize = 26.sp); Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("#${Puzzles.number(d)} · ${k.title}", style = MaterialTheme.typography.titleSmall)
                        Text(d.format(java.time.format.DateTimeFormatter.ofPattern("EEEE d MMMM")), style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(when { r == null -> "Play"; r.solved -> "✓ Solved"; else -> "Played" }, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                        color = if (r == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun untilMidnight(): String {
    val now = LocalDateTime.now(); val d = Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay())
    return "%d:%02d:%02d".format(d.toHours(), d.toMinutesPart(), d.toSecondsPart())
}

// ---------- Word Vault ----------

@Composable
private fun WordVault(date: LocalDate, save: (Progress.PuzzleResult) -> Unit) {
    val answer = remember { Puzzles.word(date) }
    val guesses = remember { mutableStateListOf<String>() }
    var typing by remember { mutableStateOf("") }
    var shake by remember { mutableIntStateOf(0) }
    val offset = remember { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(shake) { if (shake > 0) { repeat(3) { offset.animateTo(12f, tween(40)); offset.animateTo(-12f, tween(40)) }; offset.animateTo(0f, tween(40)) } }
    val marks = guesses.map { Puzzles.mark(it, answer) }
    fun finish(solved: Boolean) = save(Progress.PuzzleResult(solved, guesses.size, Puzzles.wordGrid(guesses.map { Puzzles.mark(it, answer) })))
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        (0 until 6).forEach { row ->
            val word = guesses.getOrNull(row) ?: if (row == guesses.size) typing else ""
            val m = marks.getOrNull(row)
            Row(Modifier.padding(vertical = 3.dp).graphicsLayer { translationX = if (row == guesses.size) offset.value else 0f }, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0 until 5).forEach { i ->
                    val ch = word.getOrNull(i)?.toString() ?: ""
                    val bg = when (m?.get(i)) { 2 -> RIGHT; 1 -> NEAR; 0 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .55f); else -> Color.Transparent }
                    val mark = m?.get(i)
                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(bg)
                        .border(BorderStroke(2.dp, if (m == null) MaterialTheme.colorScheme.outline else Color.Transparent), RoundedCornerShape(12.dp))
                        .semantics { contentDescription = if (ch.isBlank()) "Empty" else ch + when (mark) { 2 -> ", right place"; 1 -> ", in the word, wrong place"; 0 -> ", not in the word"; else -> "" } },
                        contentAlignment = Alignment.Center) {
                        Text(ch, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = if (m != null) Color.White else MaterialTheme.colorScheme.onBackground)
                        // A shape as well as a colour, so the marks read without colour vision.
                        if (mark != null) Text(when (mark) { 2 -> "✓"; 1 -> "↔"; else -> "✗" }, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 1.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        val used = guesses.flatMapIndexed { gi, g -> g.mapIndexed { i, c -> c to marks[gi][i] } }.groupBy({ it.first }, { it.second }).mapValues { it.value.max() }
        listOf("QWERTYUIOP", "ASDFGHJKL", "+ZXCVBNM-").forEach { row ->
            Row(Modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { c ->
                    val wide = c == '+' || c == '-'
                    val state = used[c]
                    Surface(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        when (c) {
                            '-' -> typing = typing.dropLast(1)
                            '+' -> if (typing.length == 5) {
                                guesses += typing; typing = ""
                                val last = Puzzles.mark(guesses.last(), answer)
                                if (last.all { it == 2 }) finish(true) else if (guesses.size == 6) finish(false)
                            } else shake++
                            else -> if (typing.length < 5) typing += c
                        }
                    }, shape = RoundedCornerShape(8.dp), color = when (state) { 2 -> RIGHT; 1 -> NEAR; 0 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .4f)
                        else -> MaterialTheme.colorScheme.surfaceVariant }, modifier = Modifier.width(if (wide) 50.dp else 31.dp).height(46.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            when (c) {
                                '-' -> Icon(Icons.AutoMirrored.Rounded.Backspace, "Delete", Modifier.size(18.dp))
                                '+' -> Text("ENTER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                else -> Text("$c", fontWeight = FontWeight.Bold, color = if (state != null) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.semantics { contentDescription = "$c" + when (state) { 2 -> ", right place"; 1 -> ", in the word"; 0 -> ", not in the word"; else -> "" } })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Budget Balance ----------

@Composable
private fun BudgetBalance(date: LocalDate, save: (Progress.PuzzleResult) -> Unit) {
    val b = remember { Puzzles.budget(date) }
    val sym = remember { symbolFor(AppGraph.profile.currency()) }
    var picked by remember { mutableStateOf(setOf<Int>()) }
    val checks = remember { mutableStateListOf<Int>() }
    val total = Puzzles.total(b, picked)
    Surface(shape = RoundedCornerShape(24.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.background(Brush.linearGradient(listOf(Color(0xFF0F5F55), Color(0xFF2E9C88)))).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("BUDGET", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("$sym${b.target}", color = Color.White, style = MaterialTheme.typography.displaySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("PICKED", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("$sym$total", color = if (total == b.target) Gold else Color.White, style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    b.items.chunked(2).forEachIndexed { r, pair ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 5.dp)) {
            pair.forEachIndexed { c, (name, price) ->
                val i = r * 2 + c; val on = i in picked
                Surface(onClick = { picked = if (on) picked - i else picked + i; Sounds.tap() }, shape = RoundedCornerShape(18.dp),
                    color = if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(if (on) 2.dp else 1.dp, if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline), modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                        Text("$sym$price", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    if (checks.isNotEmpty()) Text(checks.joinToString("  ") { when { it == b.target -> "✅"; it > b.target -> "🔼 $sym${it - b.target} over"; else -> "🔽 $sym${b.target - it} under" } },
        style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 6.dp))
    RunButton("Check (${5 - checks.size} left)", enabled = picked.isNotEmpty()) {
        checks += total
        val grid = checks.joinToString("") { when { it == b.target -> "✅"; it > b.target -> "🔼"; else -> "🔽" } }
        if (total == b.target) save(Progress.PuzzleResult(true, checks.size, grid))
        else if (checks.size == 5) save(Progress.PuzzleResult(false, 5, grid))
    }
}

// ---------- Growth Order ----------

@Composable
private fun GrowthOrder(date: LocalDate, save: (Progress.PuzzleResult) -> Unit) {
    val list = remember { Puzzles.growth(date) }
    val sym = remember { symbolFor(AppGraph.profile.currency()) }
    var order by remember { mutableStateOf(listOf<String>()) }
    val tries = remember { mutableStateListOf<Int>() }
    val scope = rememberCoroutineScope()
    list.forEach { s ->
        val pos = order.indexOf(s.label)
        Surface(onClick = { order = if (pos >= 0) order - s.label else order + s.label }, shape = RoundedCornerShape(18.dp),
            color = if (pos >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(if (pos >= 0) Gold else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Text(if (pos >= 0) "${pos + 1}" else s.label, fontWeight = FontWeight.ExtraBold, color = if (pos >= 0) NavyDeep else MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(12.dp))
                Column { Text("Saving ${s.label}", style = MaterialTheme.typography.titleSmall); Text(s.describe(sym), style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
    Text(if (order.size < 4) "Tap them from most money at the end (1) to least (4)." else "Your order: ${order.joinToString(" > ")}",
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
    if (tries.isNotEmpty()) Text(tries.joinToString("   ") { "$it/4 in place" }, style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))
    RunButton("Check (${3 - tries.size} left)", enabled = order.size == 4) {
        val n = Puzzles.rightPlaces(order, list); tries += n
        val grid = tries.joinToString("\n") { k -> "🟩".repeat(k) + "⬛".repeat(4 - k) }
        if (n == 4) save(Progress.PuzzleResult(true, tries.size, grid + "\n" + list.sortedByDescending { it.end }
            .joinToString(" > ") { "${it.label} ${LifeSim.fmt(it.end).replace("$", sym)}" }))
        else if (tries.size == 3) save(Progress.PuzzleResult(false, 3, grid))
        else scope.launch { order = emptyList() }
    }
}

fun symbolFor(code: String) = runCatching { java.util.Currency.getInstance(code).getSymbol(java.util.Locale.getDefault()) }.getOrDefault("$").let { if (it.length > 2) "$code " else it }

/** The daily puzzle hero card, for Games and Today. */
@Composable
fun PuzzleCard(nav: Nav, modifier: Modifier = Modifier) {
    val pr = AppGraph.progress
    pr.version
    val k = Puzzles.kind(); val r = pr.puzzle()
    Surface(onClick = { nav.puzzle() }, shape = RoundedCornerShape(26.dp), color = Color.Transparent, modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 7.dp)) {
        Row(Modifier.background(Brush.linearGradient(listOf(Color(0xFF3B2F6B), Color(0xFFB0472A)))).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).aspectRatio(1f).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                Text(k.emoji, fontSize = 34.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("DAILY PUZZLE #${Puzzles.number()}", color = Gold, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                Text(k.title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(when { r == null -> "New today · the same for everyone"; r.solved -> "Solved ✓ · come back tomorrow"; else -> "Played · new one tomorrow" },
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .85f))
            }
            val s = pr.puzzleStreak()
            if (s > 0) Text("🔥$s", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

// ---------- Spot the Scam ----------

@Composable
private fun SpotTheScam(date: LocalDate, save: (Progress.PuzzleResult) -> Unit) {
    val rounds = remember { Puzzles.scamRounds(date) }
    var round by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<Int?>(null) }
    val results = remember { mutableStateListOf<Boolean>() }
    val r = rounds[round]
    Text("Round ${round + 1} of 5 · Which one is the scam?", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    r.options.forEachIndexed { i, m ->
        val state = when { picked == null -> 0; i == r.answer -> 1; i == picked -> 2; else -> 3 }
        Surface(onClick = { if (picked == null) { picked = i; results += i == r.answer; Sounds.tap() } }, shape = RoundedCornerShape(18.dp),
            color = when (state) { 1 -> Color(0xFFC0392B).copy(alpha = .14f); 2 -> MaterialTheme.colorScheme.surfaceVariant; else -> MaterialTheme.colorScheme.surface },
            border = BorderStroke(if (state == 1) 2.dp else 1.dp, if (state == 1) Color(0xFFC0392B) else MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).semantics { contentDescription = "Message ${i + 1}: ${m.text}" +
                when (state) { 1 -> ". This is the scam."; 2 -> ". You picked this; it's genuine."; else -> "" } }) {
            Row(Modifier.padding(14.dp)) {
                Text(when (state) { 1 -> "🚩"; 2 -> "✗"; else -> "💬" }, fontSize = 20.sp); Spacer(Modifier.width(10.dp))
                Text(m.text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
    if (picked != null) {
        val right = picked == r.answer
        Text((if (right) "✅ Spotted it! " else "❌ Not quite. ") + r.options[r.answer].why, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 10.dp))
        RunButton(if (round < 4) "Next round" else "See my result") {
            if (round < 4) { round++; picked = null }
            else { val n = results.count { it }; save(Progress.PuzzleResult(n >= 4, n, results.joinToString("") { if (it) "✅" else "❌" })) }
        }
    }
    Text("Tip: urgency, secrecy, gift cards, upfront fees and requests for codes or PINs are the usual warning signs.",
        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 14.dp))
}

// ---------- Shrinking Dollar ----------

@Composable
private fun ShrinkingDollar(date: LocalDate, save: (Progress.PuzzleResult) -> Unit) {
    val rounds = remember { Puzzles.dollarRounds(date) }
    val sym = remember { symbolFor(AppGraph.profile.currency()) }
    var round by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<Int?>(null) }
    val results = remember { mutableStateListOf<Boolean>() }
    val q = rounds[round]
    Text("Round ${round + 1} of 5", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
    Text("Prices rise ${q.rate}% a year. Shopping that costs ${sym}100 today will cost about how much in ${q.years} years?",
        style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
    q.options.forEachIndexed { i, v ->
        val state = when { picked == null -> 0; i == q.answer -> 1; i == picked -> 2; else -> 3 }
        Surface(onClick = { if (picked == null) { picked = i; results += i == q.answer; Sounds.tap() } }, shape = RoundedCornerShape(16.dp),
            color = when (state) { 1 -> Color(0xFF2E7D5B); 2 -> Color(0xFFC0392B); else -> MaterialTheme.colorScheme.surfaceVariant },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("$sym$v", style = MaterialTheme.typography.titleLarge, color = if (state == 1 || state == 2) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f))
                if (state == 1) Text("✓ right", color = Color.White, fontWeight = FontWeight.Bold)
                if (state == 2) Text("✗", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (picked != null) {
        val right = q.options[q.answer]
        Text((if (picked == q.answer) "✅ Right! " else "❌ Not quite. ") + "Price rises compound: each year's ${q.rate}% is added on top of last year's higher price, " +
            "so ${sym}100 becomes about $sym$right after ${q.years} years. In other words, ${sym}100 then buys what about $sym${Math.round(10000.0 / right)} buys today.",
            style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 10.dp))
        RunButton(if (round < 4) "Next round" else "See my result") {
            if (round < 4) { round++; picked = null }
            else { val n = results.count { it }; save(Progress.PuzzleResult(n >= 4, n, results.joinToString("") { if (it) "✅" else "❌" })) }
        }
    }
}
