package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.randomthoughtsever.plainly.data.Reminders
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.randomthoughtsever.plainly.data.CHALLENGES
import com.randomthoughtsever.plainly.data.Community
import com.randomthoughtsever.plainly.data.CHECKS
import com.randomthoughtsever.plainly.data.Entry
import com.randomthoughtsever.plainly.data.GOALS
import com.randomthoughtsever.plainly.data.Numbers
import com.randomthoughtsever.plainly.data.Quiz
import com.randomthoughtsever.plainly.data.quizOfTheDay
import com.randomthoughtsever.plainly.data.verdict
import com.randomthoughtsever.plainly.tools.Calc
import com.randomthoughtsever.plainly.tools.Money
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

// ---------- Check-ups ----------

/** The Money Health Check invitation, or the reader's last scores once taken. */
@Composable
fun ChecksCard(nav: Nav) {
    val profile = AppGraph.profile
    profile.version
    val money = profile.check("money")
    Surface(shape = RoundedCornerShape(26.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.background(Brush.linearGradient(listOf(Navy, NavyDeep))).padding(18.dp)) {
            if (money == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("60-SECOND CHECK-UP", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.ExtraBold)
                        Text("What's your money score?", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text("Six quick questions. Your score out of 100 and the one thing to fix first.", style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = .78f))
                    }
                    TopicScene("Personal Finance", Modifier.size(84.dp).clip(RoundedCornerShape(20.dp)), background = false)
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { nav.check("money") }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) {
                    Text("Take the Money Health Check", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("YOUR CHECK-UPS", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CHECKS.forEach { c ->
                        val r = profile.check(c.id)
                        Surface(onClick = { nav.check(c.id) }, shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = .1f), modifier = Modifier.weight(1f)) {
                            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                ScoreRing(r?.first, 64)
                                Spacer(Modifier.height(6.dp))
                                Text(c.title.removeSuffix(" Check").replace("Money Health", "Money"), style = MaterialTheme.typography.labelLarge, color = Color.White)
                                Text(r?.let { verdict(it.first) } ?: "Take it", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = .7f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Working on: ${money.second}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .8f))
            }
        }
    }
}

@Composable
fun ScoreRing(score: Int?, sizeDp: Int) {
    val a = remember { Animatable(0f) }
    LaunchedEffect(score) { a.animateTo((score ?: 0) / 100f, tween(1000)) }
    Box(Modifier.size(sizeDp.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val st = size.minDimension * .12f
            drawArc(Color.White.copy(alpha = .18f), 0f, 360f, false, Offset(st / 2, st / 2), androidx.compose.ui.geometry.Size(size.width - st, size.height - st),
                style = androidx.compose.ui.graphics.drawscope.Stroke(st))
            drawArc(Gold, -90f, 360f * a.value, false, Offset(st / 2, st / 2), androidx.compose.ui.geometry.Size(size.width - st, size.height - st),
                style = androidx.compose.ui.graphics.drawscope.Stroke(st, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        Text(score?.toString() ?: "?", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
    }
}

// ---------- My numbers ----------

private fun months(a: Double, b: Double) = if (b <= 0) 0.0 else a / b

/** The reader's own numbers turned into answers: FIRE number, safety net, savings rate, net worth and debt-free date. */
@Composable
fun MyNumbersCard() {
    val profile = AppGraph.profile
    val n = profile.numbers
    val cur = profile.currency()
    var editing by remember { mutableStateOf(false) }
    if (editing) NumbersDialog(n, cur) { new -> new?.let { profile.updateNumbers(it) }; editing = false }
    Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.padding(18.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("MY NUMBERS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
                    Text(if (n.isSet) "Your money, at a glance" else "See your own numbers", style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = { editing = true }) { Icon(Icons.Rounded.Edit, "Edit my numbers") }
            }
            if (!n.isSet) {
                Text("Add your pay and spending (kept on your phone) to see your FIRE number, safety net and debt-free date.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                FilledTonalButton(onClick = { editing = true }, shape = RoundedCornerShape(50)) { Text("Add my numbers") }
                return@Column
            }
            Spacer(Modifier.height(10.dp))
            val yearly = n.spend * 12
            val fire = Calc.fire(yearly, n.invested + n.savings, (n.income - n.spend).coerceAtLeast(0.0), 6.0, 4.0)
            val safety = months(n.savings, n.spend)
            val rate = if (n.income > 0) (n.income - n.spend) / n.income * 100 else 0.0
            val net = n.savings + n.invested - n.debt
            val pay = if (n.debtPayment > 0) n.debtPayment else (n.income - n.spend).coerceAtLeast(0.0)
            val debtMonths = if (n.debt > 0 && pay > 0) ceil(n.debt / pay).toInt() else 0
            val tiles = buildList {
                if (fire != null && yearly > 0) add(Triple("FIRE number", Money.fmt(cur, fire.target),
                    if (fire.years >= 0) "about ${"%.0f".format(fire.years)} years away" else "add savings to see when"))
                if (n.spend > 0) add(Triple("Safety net", "${"%.1f".format(safety)} months", if (safety >= 6) "you're well covered" else if (safety >= 3) "solid; 6 is ideal" else "aim for 3 to 6"))
                if (n.income > 0) add(Triple("Savings rate", "${rate.toInt()}%", if (rate >= 20) "excellent" else if (rate >= 10) "good" else "try for 10% or more"))
                add(Triple("Net worth", Money.fmt(cur, net), "savings + investments − debt"))
                if (debtMonths > 0) add(Triple("Debt-free", LocalDate.now().plusMonths(debtMonths.toLong()).let { "${it.month.name.take(3).lowercase().replaceFirstChar(Char::uppercase)} ${it.year}" },
                    "at ${Money.fmt(cur, pay)} a month"))
            }
            tiles.chunked(2).forEach { row ->
                Row(Modifier.padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (k, v, s) ->
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .6f)).padding(12.dp)) {
                            Text(k, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(v, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                            Text(s, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun NumbersDialog(n: Numbers, cur: String, done: (Numbers?) -> Unit) {
    fun f(d: Double) = if (d == 0.0) "" else d.toLong().toString()
    val fields = remember { mutableStateOf(listOf(f(n.income), f(n.spend), f(n.savings), f(n.invested), f(n.debt), f(n.debtPayment))) }
    val labels = listOf("Monthly take-home pay", "Monthly spending", "Savings in the bank", "Investments", "Debt (cards and loans)", "Monthly debt payment")
    AlertDialog(onDismissRequest = { done(null) }, title = { Text("My numbers") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Kept on this phone only.", style = MaterialTheme.typography.bodySmall)
            labels.forEachIndexed { i, l ->
                OutlinedTextField(fields.value[i], { v -> fields.value = fields.value.toMutableList().also { it[i] = v.filter { c -> c.isDigit() || c == '.' }.take(12) } },
                    label = { Text(l) }, singleLine = true, suffix = { Text(cur) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        }
    }, confirmButton = {
        TextButton(onClick = { val v = fields.value.map { it.toDoubleOrNull() ?: 0.0 }; done(Numbers(v[0], v[1], v[2], v[3], v[4], v[5])) }) { Text("Save") }
    }, dismissButton = { TextButton(onClick = { done(null) }) { Text("Cancel") } })
}

// ---------- Your plan ----------

@Composable
fun PlanCard(index: List<Entry>, nav: Nav) {
    val profile = AppGraph.profile
    val plan = remember(index, profile.goals) { planFor(profile.goals, index) }
    val tools = remember(profile.goals) { toolsFor(profile.goals).take(3) }
    val read = AppGraph.library.history.map { it.id }.toSet()
    var editing by remember { mutableStateOf(false) }
    if (editing) GoalsDialog(profile.goals) { g -> g?.let { profile.updateGoals(it) }; editing = false }
    if (plan.isEmpty()) return
    Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("YOUR PLAN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
                    Text(GOALS.filter { it.key in profile.goals }.joinToString(" · ") { it.title }.ifBlank { "Pick your goals" }, style = MaterialTheme.typography.titleMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                TextButton(onClick = { editing = true }) { Text("Change") }
            }
            val doneCount = plan.count { it.id in read }
            LinearProgressIndicator(progress = { doneCount / plan.size.toFloat() }, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(6.dp).clip(CircleShape))
            plan.forEachIndexed { i, e ->
                val done = e.id in read
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { nav.article(e.id) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center) {
                        if (done) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        else Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(e.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis,
                        color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                }
            }
            if (tools.isNotEmpty()) LazyRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tools) { t ->
                    Surface(onClick = { t.open(nav) }, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(t.icon, null, tint = t.color, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                            Text(t.title, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalsDialog(current: Set<String>, done: (Set<String>?) -> Unit) {
    var sel by remember { mutableStateOf(current) }
    AlertDialog(onDismissRequest = { done(null) }, title = { Text("What do you want help with?") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            GOALS.forEach { g ->
                val on = g.key in sel
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { sel = if (on) sel - g.key else sel + g.key }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(g.icon, null, tint = g.to); Spacer(Modifier.width(12.dp))
                    Text(g.title, modifier = Modifier.weight(1f))
                    if (on) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }, confirmButton = { TextButton(onClick = { done(sel.ifEmpty { current }) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = { done(null) }) { Text("Cancel") } })
}

// ---------- Daily 3 ----------

/** Three small things a day: a tip, a minute of breathing, a quiz question. Finishing all three counts towards badges. */
@Composable
fun Daily3Card(index: List<Entry>, nav: Nav) {
    val profile = AppGraph.profile
    profile.version
    val done = profile.dailyDone()
    val tip = remember(index) {
        val goals = GOALS.filter { it.key in profile.goals }
        val pool = index.filterNot { it.isToolPage }.filter { e -> e.quick.length > 80 && (goals.isEmpty() || goals.any { g -> e.label in g.labels }) }
        pool.getOrNull((LocalDate.now().toEpochDay() % pool.size.coerceAtLeast(1)).toInt())
    }
    val quiz = remember { quizOfTheDay(profile.goals) }
    var breathing by remember { mutableStateOf(false) }
    var showTip by remember { mutableStateOf(false) }
    var showQuiz by remember { mutableStateOf(false) }
    if (breathing) BreathingDialog { finished -> breathing = false; if (finished) { profile.addBreathing(); profile.markDaily("breathe") } }
    Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.padding(18.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("DAILY 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
                    Text(if (done.size == 3) "All done for today. See you tomorrow!" else "Three small things, two minutes", style = MaterialTheme.typography.titleMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("tip", "breathe", "quiz").forEach { k ->
                        Box(Modifier.size(12.dp).clip(CircleShape).background(if (k in done) Gold else MaterialTheme.colorScheme.surfaceVariant))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            DailyRow(Icons.Rounded.Lightbulb, "Tip of the day", tip?.title ?: "Loading…", "tip" in done) { showTip = !showTip; if (tip != null) profile.markDaily("tip") }
            AnimatedVisibility(showTip && tip != null) {
                Column(Modifier.padding(start = 44.dp, bottom = 8.dp)) {
                    Text(tip?.quick ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = 6, overflow = TextOverflow.Ellipsis)
                    TextButton(onClick = { tip?.let { nav.article(it.id) } }, contentPadding = PaddingValues(0.dp)) { Text("Read the guide") }
                }
            }
            DailyRow(Icons.Rounded.Spa, "One minute of calm", "Breathe with the circle", "breathe" in done) { breathing = true }
            DailyRow(Icons.Rounded.Quiz, "Quiz of the day", quiz.question, "quiz" in done) { showQuiz = !showQuiz }
            AnimatedVisibility(showQuiz) { Box(Modifier.padding(top = 4.dp)) { QuizBody(quiz, nav) { profile.markDaily("quiz") } } }
        }
    }
}

@Composable
private fun DailyRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String, done: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(if (done) Gold else MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(if (done) Icons.Rounded.Check else icon, null, tint = if (done) NavyDeep else MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** One minute of slow breathing: 4 seconds in, 6 seconds out, six times, with a growing and shrinking circle. */
@Composable
fun BreathingDialog(done: (Boolean) -> Unit) {
    val total = 60
    var second by remember { mutableIntStateOf(0) }
    val r = remember { Animatable(.45f) }
    LaunchedEffect(Unit) {
        repeat(6) {
            launch { r.animateTo(1f, tween(4000, easing = LinearEasing)); r.animateTo(.45f, tween(6000, easing = LinearEasing)) }
            repeat(10) { kotlinx.coroutines.delay(1000); second++ }
        }
        done(true)
    }
    val inhale = (second % 10) < 4
    Dialog(onDismissRequest = { done(false) }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0E4D64), NavyDeep))), contentAlignment = Alignment.Center) {
            TopicScene("Zen & Mindfulness", Modifier.fillMaxSize(), background = false, seconds = 10)
            Box(Modifier.size((260 * r.value).dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color.White.copy(alpha = .9f), Gold.copy(alpha = .5f)))))
            Column(Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (inhale) "Breathe in" else "Breathe out", style = MaterialTheme.typography.displaySmall, color = Color.White)
                Text("${total - second} seconds", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = .75f))
            }
            TextButton(onClick = { done(false) }, modifier = Modifier.align(Alignment.BottomCenter).padding(40.dp)) { Text("Stop", color = Color.White) }
        }
    }
}

// ---------- Quiz ----------

/** A quiz question: tap an answer, see right or wrong and why, then read the guide it comes from. */
@Composable
fun QuizBody(q: Quiz, nav: Nav, onAnswered: () -> Unit = {}) {
    val profile = AppGraph.profile
    var picked by remember(q.id) { mutableStateOf<Int?>(null) }
    Column {
        Text(q.question, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        q.options.forEachIndexed { i, o ->
            val state = when { picked == null -> 0; i == q.correct -> 1; i == picked -> 2; else -> 3 }
            val bg = when (state) { 1 -> Color(0xFF2E7D5B); 2 -> Color(0xFFC0392B); else -> MaterialTheme.colorScheme.surfaceVariant }
            val fg = if (state == 1 || state == 2) Color.White else MaterialTheme.colorScheme.onSurface
            Surface(onClick = {
                if (picked == null) { picked = i; profile.answer(q.id, i == q.correct); onAnswered(); Community.vote(q.id, i) }
            }, shape = RoundedCornerShape(14.dp), color = bg, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(o, style = MaterialTheme.typography.titleSmall, color = fg, modifier = Modifier.weight(1f))
                    if (state == 1) Icon(Icons.Rounded.Check, null, tint = Color.White)
                    if (state == 2) Icon(Icons.Rounded.Close, null, tint = Color.White)
                    if (picked != null) PollShare(q.id, i, fg)
                }
            }
        }
        AnimatedVisibility(picked != null) {
            Column(Modifier.padding(top = 6.dp)) {
                Text(if (picked == q.correct) "Right! " + q.why else "Not quite. " + q.why, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { nav.article(q.post) }, contentPadding = PaddingValues(0.dp)) {
                    Text("Read why"); Spacer(Modifier.width(4.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(16.dp))
                }
            }
        }
    }
}

/** A quiz card for feeds. */
@Composable
fun QuizCard(q: Quiz, nav: Nav, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(2.dp, Gold.copy(alpha = .6f)),
        modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Quiz, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                Text("TEST YOURSELF", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(8.dp))
            QuizBody(q, nav)
        }
    }
}

// ---------- Challenges ----------

@Composable
fun ChallengeCards(nav: Nav) {
    val profile = AppGraph.profile
    profile.version
    val joined = profile.joined()
    if (joined.isEmpty()) {
        Surface(onClick = { nav.challenges() }, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🏁", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Start a challenge", style = MaterialTheme.typography.titleMedium)
                    Text("7-day no-spend, 30-day debt sprint, 7-day calm and more", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }
    joined.forEach { j -> CHALLENGES.firstOrNull { it.key == j.key }?.let { c -> ActiveChallenge(c, j.start, j.days) { nav.challenge(c.key) } } }
}

@Composable
private fun ActiveChallenge(c: com.randomthoughtsever.plainly.data.Challenge, start: String, days: Set<String>, open: () -> Unit) {
    val profile = AppGraph.profile
    val dayNo = (ChronoUnit.DAYS.between(LocalDate.parse(start), LocalDate.now()) + 1).toInt().coerceAtMost(c.days)
    val today = LocalDate.now().toString() in days
    val finished = days.size >= c.days
    Surface(onClick = open, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(c.emoji, style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.title, style = MaterialTheme.typography.titleMedium)
                    Text("Day $dayNo of ${c.days} · ${days.size} check-ins", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            LinearProgressIndicator(progress = { days.size / c.days.toFloat() }, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(6.dp).clip(CircleShape),
                color = Gold)
            when {
                finished -> Button(onClick = { profile.completeChallenge(c.key) }, shape = RoundedCornerShape(50)) { Text("🏆 Claim your badge") }
                today -> Text("✓ Done for today: ${c.action.lowercase()}", style = MaterialTheme.typography.labelLarge, color = Color(0xFF2E7D5B))
                else -> Button(onClick = { profile.checkIn(c.key) }, shape = RoundedCornerShape(50)) { Text(c.action) }
            }
        }
    }
}

@Composable
fun ChallengesScreen(nav: Nav) {
    val profile = AppGraph.profile
    profile.version
    val joined = profile.joined().map { it.key }.toSet()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Row(Modifier.statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Text("Challenges", style = MaterialTheme.typography.headlineMedium)
            }
            Text("Small daily actions, a check-in each day, and a badge at the end. We'll remind you each evening.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp))
        }
        items(CHALLENGES, key = { it.key }) { c ->
            val g = GOALS.firstOrNull { it.key == c.goal }
            Surface(onClick = { nav.challenge(c.key) }, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(g?.from ?: Navy, g?.to ?: NavyBright))),
                        contentAlignment = Alignment.Center) { Text(c.emoji, style = MaterialTheme.typography.headlineSmall) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.title, style = MaterialTheme.typography.titleMedium)
                        Text(c.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (c.key in joined) TextButton(onClick = { profile.leave(c.key) }) { Text("Leave") }
                    else Button(onClick = { profile.join(c.key); Reminders.schedule(nav.context); nav.challenge(c.key) }, shape = RoundedCornerShape(50)) { Text("Join") }
                }
            }
        }
    }
}

@Composable
fun EmptyCentered(text: String) = Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
    Text(text, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
