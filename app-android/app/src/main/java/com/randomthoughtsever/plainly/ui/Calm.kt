package com.randomthoughtsever.plainly.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private val CalmTop = Color(0xFF0E2A33)
private val CalmBottom = Color(0xFF145A6B)

/** Calm corner: breathing patterns, a focus timer and a sleep wind-down, with soft sounds made on the phone. */
@Composable
fun CalmScreen(mode: String, nav: Nav) {
    var tab by rememberSaveable { mutableIntStateOf(when (mode) { "focus" -> 1; "sleep" -> 2; else -> 0 }) }
    var sound by rememberSaveable { mutableStateOf("off") }
    val noise = remember { Ambient() }
    DisposableEffect(Unit) { onDispose { noise.stop() } }
    LaunchedEffect(sound) { if (sound == "off") noise.stop() else noise.play(sound) }
    val view = LocalView.current
    DisposableEffect(Unit) { view.keepScreenOn = true; onDispose { view.keepScreenOn = false } }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(CalmTop, if (tab == 2) Color(0xFF0A1420) else CalmBottom))).statusBarsPadding()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White) }
            Text("Calm corner", style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f))
            Text("${AppGraph.progress.calmMinutes()} calm min", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = .8f), modifier = Modifier.padding(end = 12.dp))
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            listOf("Breathe", "Focus", "Sleep").forEachIndexed { i, l -> SegmentedButton(tab == i, { tab = i }, SegmentedButtonDefaults.itemShape(i, 3)) { Text(l) } }
        }
        Row(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Sound", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelLarge)
            listOf("off" to "Off", "rain" to "Rain", "ocean" to "Ocean", "brown" to "Hum").forEach { (k, l) ->
                FilterChip(sound == k, { sound = k }, label = { Text(l) }, colors = FilterChipDefaults.filterChipColors(labelColor = Color.White,
                    selectedContainerColor = Gold, selectedLabelColor = NavyDeep))
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) { 0 -> Breathe(); 1 -> Focus(); else -> Sleep { sound = "off" } }
        }
    }
}

private data class Pattern(val name: String, val steps: List<Pair<String, Int>>)
private val PATTERNS = listOf(
    Pattern("Calm 4-6", listOf("Breathe in" to 4, "Breathe out" to 6)),
    Pattern("Box 4-4-4-4", listOf("Breathe in" to 4, "Hold" to 4, "Breathe out" to 4, "Hold" to 4)),
    Pattern("Sleepy 4-7-8", listOf("Breathe in" to 4, "Hold" to 7, "Breathe out" to 8)),
)

@Composable
private fun Breathe() {
    var pattern by rememberSaveable { mutableIntStateOf(0) }
    var minutes by rememberSaveable { mutableIntStateOf(1) }
    var running by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("Ready") }
    var left by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    val r = remember { Animatable(.45f) }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        val end = System.currentTimeMillis() + minutes * 60_000L
        while (System.currentTimeMillis() < end) {
            for ((what, sec) in PATTERNS[pattern].steps) {
                label = what
                left = ((end - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
                when (what) {
                    "Breathe in" -> r.animateTo(1f, tween(sec * 1000, easing = FastOutSlowInEasing))
                    "Breathe out" -> r.animateTo(.45f, tween(sec * 1000, easing = FastOutSlowInEasing))
                    else -> delay(sec * 1000L)
                }
            }
        }
        running = false; finished = true; label = "Well done"
        AppGraph.progress.addCalm(minutes); AppGraph.profile.addBreathing()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PATTERNS.forEachIndexed { i, p -> FilterChip(pattern == i, { if (!running) pattern = i }, label = { Text(p.name) },
                colors = FilterChipDefaults.filterChipColors(labelColor = Color.White, selectedContainerColor = Color.White.copy(alpha = .25f), selectedLabelColor = Color.White)) }
        }
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color.White.copy(alpha = .08f), size.minDimension / 2)
                drawCircle(Brush.radialGradient(listOf(Color(0xFF8FE3D3), Color(0xFF2E9C88))), size.minDimension / 2 * r.value)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                if (running) Text("${left / 60}:${"%02d".format(left % 60)}", color = Color.White.copy(alpha = .85f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 3, 5).forEach { m -> FilterChip(minutes == m, { if (!running) minutes = m }, label = { Text("$m min") },
                colors = FilterChipDefaults.filterChipColors(labelColor = Color.White, selectedContainerColor = Color.White.copy(alpha = .25f), selectedLabelColor = Color.White)) }
        }
        Spacer(Modifier.height(14.dp))
        Button(onClick = { finished = false; running = !running; if (!running) label = "Ready" }, shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) { Text(if (running) "Stop" else if (finished) "Again" else "Start", fontWeight = FontWeight.Bold) }
        if (finished) Text("Notice how you feel now compared with before.", color = Color.White.copy(alpha = .85f), modifier = Modifier.padding(top = 12.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun Focus() {
    var minutes by rememberSaveable { mutableIntStateOf(25) }
    var running by remember { mutableStateOf(false) }
    var leftMs by remember { mutableStateOf(minutes * 60_000L) }
    var done by remember { mutableStateOf(false) }
    LaunchedEffect(minutes) { if (!running) leftMs = minutes * 60_000L }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var last = System.currentTimeMillis()
        while (leftMs > 0) { delay(250); val now = System.currentTimeMillis(); leftMs -= now - last; last = now }
        running = false; done = true; leftMs = 0
        AppGraph.progress.addCalm(minutes); AppGraph.progress.count("focus", minutes)
        Sounds.fanfare()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("One task. Phone face down. Go.", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(20.dp))
        Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
            val frac = leftMs / (minutes * 60_000f)
            Canvas(Modifier.fillMaxSize()) {
                drawArc(Color.White.copy(alpha = .12f), -90f, 360f, false, style = Stroke(18f))
                drawArc(Gold, -90f, 360f * frac, false, style = Stroke(18f, cap = StrokeCap.Round))
            }
            val s = (leftMs / 1000).toInt()
            Text("${s / 60}:${"%02d".format(s % 60)}", style = MaterialTheme.typography.displayMedium, color = Color.White)
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(15, 25, 45).forEach { m -> FilterChip(minutes == m, { if (!running) { minutes = m; done = false } }, label = { Text("$m min") },
                colors = FilterChipDefaults.filterChipColors(labelColor = Color.White, selectedContainerColor = Color.White.copy(alpha = .25f), selectedLabelColor = Color.White)) }
        }
        Spacer(Modifier.height(14.dp))
        Button(onClick = { if (done) { done = false; leftMs = minutes * 60_000L }; running = !running }, shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) {
            Text(when { running -> "Pause"; done -> "Another round"; leftMs < minutes * 60_000L -> "Resume"; else -> "Start focusing" }, fontWeight = FontWeight.Bold)
        }
        if (done) Text("🎯 Focus session done. Take a 5-minute break.", color = Color.White, modifier = Modifier.padding(top = 12.dp))
    }
}

private val WIND_DOWN = listOf(
    "Dim the lights and put your phone on silent.", "Drop your shoulders. Unclench your jaw.", "Breathe in for 4, out for 6. Three times.",
    "Think of one thing that went well today.", "Notice where your body touches the bed.", "Let tomorrow's list wait. It will still be there.",
    "Relax your hands, then your feet.", "Breathe slowly. Let each breath out be longer.", "Picture a quiet place you love.", "Rest now. Goodnight. 🌙",
)

@Composable
private fun Sleep(stopSound: () -> Unit) {
    var minutes by rememberSaveable { mutableIntStateOf(10) }
    var running by remember { mutableStateOf(false) }
    var step by remember { mutableIntStateOf(0) }
    val fade = remember { Animatable(1f) }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        val per = minutes * 60_000L / WIND_DOWN.size
        step = 0
        while (step < WIND_DOWN.size - 1) { fade.snapTo(0f); fade.animateTo(1f, tween(2500, easing = LinearEasing)); delay(per - 2500); step++ }
        AppGraph.progress.addCalm(minutes); delay(20_000); stopSound(); running = false
    }
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        if (running) {
            Text(WIND_DOWN[step], style = MaterialTheme.typography.headlineMedium, color = Color.White.copy(alpha = .9f * fade.value), textAlign = TextAlign.Center)
            Spacer(Modifier.height(30.dp))
            Text("${step + 1} of ${WIND_DOWN.size}", color = Color.White.copy(alpha = .4f))
            Spacer(Modifier.height(30.dp))
            Button(onClick = { running = false }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = .15f))) { Text("Stop") }
        } else {
            Text("🌙", style = MaterialTheme.typography.displayLarge)
            Text("Sleep wind-down", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("Ten slow prompts to help you switch off. Pick a sound above; it fades out at the end.", color = Color.White.copy(alpha = .8f),
                textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(10, 20, 30).forEach { m -> FilterChip(minutes == m, { minutes = m }, label = { Text("$m min") },
                    colors = FilterChipDefaults.filterChipColors(labelColor = Color.White, selectedContainerColor = Color.White.copy(alpha = .25f), selectedLabelColor = Color.White)) }
            }
            Spacer(Modifier.height(14.dp))
            Button(onClick = { running = true }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) {
                Text("Begin", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

/** Soft background sound generated in real time (no audio files): rain (filtered noise), ocean (noise in slow waves) or a deep hum (brown noise). */
class Ambient {
    @Volatile private var kind: String? = null
    private var thread: Thread? = null
    fun play(k: String) {
        val wasPlaying = kind != null
        kind = k
        if (thread?.isAlive == true) { if (wasPlaying) return; thread?.join(600) }
        thread = Thread {
            runCatching {
                val rate = 22050
                val buf = ShortArray(2048)
                val track = AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(buf.size * 4).setTransferMode(AudioTrack.MODE_STREAM).build()
                track.play()
                val r = Random(1); var brown = 0.0; var lp = 0.0; var t = 0L; var vol = 0.0
                while (kind != null) {
                    val k = kind
                    for (i in buf.indices) {
                        val white = r.nextDouble() * 2 - 1
                        brown = (brown + white * 0.02).coerceIn(-1.0, 1.0) * 0.998
                        lp += (white - lp) * 0.08
                        val s = when (k) {
                            "rain" -> lp * 0.9 + white * 0.05
                            "ocean" -> brown * 3.0 * (0.35 + 0.65 * (0.5 + 0.5 * sin(2 * PI * t / (rate * 9.0))))
                            else -> brown * 3.5
                        }
                        vol = (vol + 0.00002).coerceAtMost(0.35) // fade in
                        buf[i] = (s.coerceIn(-1.0, 1.0) * vol * Short.MAX_VALUE).toInt().toShort(); t++
                    }
                    track.write(buf, 0, buf.size)
                }
                track.stop(); track.release()
            }
        }.apply { isDaemon = true; start() }
    }
    fun stop() { kind = null }
}
