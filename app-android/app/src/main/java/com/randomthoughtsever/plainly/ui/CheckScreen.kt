package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.CHECKS
import com.randomthoughtsever.plainly.data.verdict
import com.randomthoughtsever.plainly.tools.Catalog

/** A one-minute check-up: one question per screen, then the score, the weakest area and what to do about it. */
@Composable
fun CheckScreen(id: String, nav: Nav) {
    val check = CHECKS.firstOrNull { it.id == id } ?: CHECKS.first()
    var answers by rememberSaveable { mutableStateOf(listOf<Int>()) }
    val done = answers.size == check.questions.size
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (answers.isNotEmpty() && !done) answers = answers.dropLast(1) else nav.back() }) {
                Icon(if (answers.isNotEmpty() && !done) Icons.AutoMirrored.Rounded.ArrowBack else Icons.Rounded.Close, "Back")
            }
            Text(check.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (!done) Text("${answers.size + 1} of ${check.questions.size}", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 12.dp))
        }
        val progress by animateFloatAsState(answers.size / check.questions.size.toFloat(), tween(400), label = "p")
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(6.dp).clip(CircleShape))
        AnimatedContent(answers.size, transitionSpec = {
            (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
        }, label = "q") { i ->
            if (i < check.questions.size) {
                val q = check.questions[i]
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                    Text(q.area.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(8.dp))
                    Text(q.text, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(20.dp))
                    q.options.forEachIndexed { oi, o ->
                        Surface(onClick = { answers = answers + oi }, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(o.text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            } else CheckResult(check.id, answers, nav) { answers = emptyList() }
        }
    }
}

@Composable
private fun CheckResult(id: String, answers: List<Int>, nav: Nav, retake: () -> Unit) {
    val check = CHECKS.first { it.id == id }
    val context = LocalContext.current
    val (score, areas) = remember(answers) { check.score(answers) }
    val weakest = remember(areas) { check.weakest(areas) }
    LaunchedEffect(score) { AppGraph.profile.saveCheck(id, score, weakest) }
    val ring = remember { Animatable(0f) }
    LaunchedEffect(score) { ring.animateTo(score / 100f, tween(1400)) }
    val index = rememberIndex()
    val fix = check.fixes[weakest]
    val guides = remember(index, weakest) { index.orEmpty().filterNot { it.isToolPage }.filter { fix?.words?.containsMatchIn(it.title) == true }.take(2) }
    val tool = remember(weakest) { fix?.tool?.let { k -> Catalog.all().firstOrNull { it.key == k } } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            val track = MaterialTheme.colorScheme.surfaceVariant
            Canvas(Modifier.fillMaxSize()) {
                val st = 22.dp.toPx()
                drawArc(track, 0f, 360f, false, Offset(st / 2, st / 2), Size(size.width - st, size.height - st), style = Stroke(st))
                drawArc(Brush.sweepGradient(listOf(Gold, Color(0xFFE2462C), Gold)), -90f, 360f * ring.value, false, Offset(st / 2, st / 2),
                    Size(size.width - st, size.height - st), style = Stroke(st, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(ring.value * 100).toInt()}", style = MaterialTheme.typography.displayMedium)
                Text("out of 100", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(verdict(score), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(18.dp))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            check.areas.forEach { a ->
                val v by animateFloatAsState(areas[a] ?: 0f, tween(900), label = a)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(a, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(100.dp))
                    Box(Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(Modifier.fillMaxWidth(v).fillMaxHeight().clip(RoundedCornerShape(6.dp))
                            .background(if (a == weakest) Color(0xFFE2462C) else MaterialTheme.colorScheme.primary))
                    }
                    Text(" ${(v * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(48.dp))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Surface(shape = RoundedCornerShape(24.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.background(Brush.linearGradient(listOf(Navy, NavyDeep))).padding(18.dp)) {
                Text("FIX THIS FIRST", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.ExtraBold)
                Text(weakest, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Spacer(Modifier.height(10.dp))
                guides.forEach { g ->
                    Surface(onClick = { nav.article(g.id) }, shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = .1f), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(g.title, style = MaterialTheme.typography.titleSmall, color = Color.White, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Gold)
                        }
                    }
                }
                tool?.let { t ->
                    Button(onClick = { t.open(nav) }, shape = RoundedCornerShape(50), modifier = Modifier.padding(top = 8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) {
                        Icon(t.icon, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Open: ${t.title}")
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { ShareCards.share(context, ShareCards.score(context, check.title, score, verdict(score), weakest), "plainly-${check.id}-score") },
                shape = RoundedCornerShape(50)) { Icon(Icons.Rounded.Share, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Share my score") }
            OutlinedButton(onClick = retake, shape = RoundedCornerShape(50)) { Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Retake") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { nav.tab("today") }, shape = RoundedCornerShape(50), modifier = Modifier.navigationBarsPadding()) { Text("Go to Today") }
        Spacer(Modifier.height(16.dp))
    }
}
