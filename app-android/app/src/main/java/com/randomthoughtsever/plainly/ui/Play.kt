package com.randomthoughtsever.plainly.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.randomthoughtsever.plainly.data.NewPosts
import com.randomthoughtsever.plainly.data.Progress
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ---------- feedback: the XP toast, level-up, confetti, sounds ----------

/** The "+20 XP · Daily puzzle solved" pill that drops in after any award, and the level-up celebration. */
@Composable
fun PlayOverlay(nav: Nav) {
    val pr = AppGraph.progress
    val award = pr.lastAward
    LaunchedEffect(award?.at) { if (award != null) { Sounds.chime(); delay(2200); if (pr.lastAward == award) pr.lastAward = null } }
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(award != null, Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 64.dp),
            enter = slideInVertically { -it } + fadeIn(), exit = slideOutVertically { -it } + fadeOut()) {
            val a = remember(award?.at) { award }
            // A plain box (not a Surface) so taps go straight through to whatever is underneath.
            Box(Modifier.shadow(8.dp, RoundedCornerShape(50)).background(NavyDeep, RoundedCornerShape(50)).border(1.dp, Gold.copy(alpha = .6f), RoundedCornerShape(50))) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    if ((a?.amount ?: 0) > 0) { Text("+${a?.amount ?: 0} XP", color = Gold, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.width(8.dp)) }
                    Text(a?.reason.orEmpty(), color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
    pr.levelUp?.let { l -> LevelUpDialog(l, nav) { pr.levelUp = null } }
}

@Composable
fun LevelUpDialog(level: Int, nav: Nav, done: () -> Unit) {
    val pr = AppGraph.progress
    val k = remember { Animatable(.3f) }
    LaunchedEffect(Unit) { Sounds.fanfare(); k.animateTo(1f, spring(dampingRatio = .35f, stiffness = 220f)) }
    val unlocked = pr.rewards().filter { it.level == level }
    Dialog(onDismissRequest = done) {
        Box {
            Surface(shape = RoundedCornerShape(32.dp), color = NavyDeep) {
                Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LEVEL UP", color = Gold, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                    Box(Modifier.padding(16.dp).size(120.dp).scale(k.value).clip(CircleShape).background(Brush.linearGradient(listOf(Gold, Color(0xFFC0602A)))),
                        contentAlignment = Alignment.Center) { Text("$level", style = MaterialTheme.typography.displayLarge, color = Color.White) }
                    Text("You're a ${pr.title(level)}!", style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.Center)
                    unlocked.forEach { r ->
                        Text("Unlocked: ${r.title}", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp))
                    }
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = { done(); if (unlocked.isNotEmpty()) nav.rewards() }, shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) {
                        Text(if (unlocked.isNotEmpty()) "See my rewards" else "Keep going", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Confetti(Modifier.matchParentSize())
        }
    }
}

/** A burst of confetti that falls once (about two seconds). */
@Composable
fun Confetti(modifier: Modifier = Modifier, pieces: Int = 70) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(2200, easing = LinearEasing)) }
    val parts = remember {
        val r = Random(System.nanoTime())
        List(pieces) { floatArrayOf(r.nextFloat(), r.nextFloat() * .3f, (r.nextFloat() - .5f) * 2f, r.nextFloat() * 360f, r.nextInt(5).toFloat()) }
    }
    val colors = listOf(Gold, Color(0xFFE2462C), Color(0xFF2E9C88), Color(0xFF3B79B5), Color(0xFFC25A8C))
    if (t.value < 1f) Canvas(modifier) {
        val p = t.value
        parts.forEach { (x, y0, drift, rot, c) ->
            val px = size.width * (x + drift * p * .25f)
            val py = size.height * (y0 - .3f + p * 1.3f)
            rotate(rot + p * 720f, Offset(px, py)) {
                drawRect(colors[c.toInt()].copy(alpha = (1.2f - p).coerceIn(0f, 1f)), Offset(px - 6f, py - 10f), Size(12f, 20f))
            }
        }
    }
}

/** Short sounds made on the phone (no audio files), only when the reader turned sounds on in Settings. */
object Sounds {
    private val on get() = runCatching { NewPosts.prefs(AppGraph.appContext).getBoolean("sounds", false) }.getOrDefault(false)
    fun chime() = play(listOf(880.0 to 90, 1320.0 to 160))
    fun fanfare() = play(listOf(523.0 to 110, 659.0 to 110, 784.0 to 110, 1047.0 to 280))
    fun tap() = play(listOf(660.0 to 40))
    private fun play(notes: List<Pair<Double, Int>>) {
        if (!on) return
        Thread {
            runCatching {
                val rate = 22050
                val pcm = notes.flatMap { (f, ms) ->
                    val n = rate * ms / 1000
                    List(n) { i -> (sin(2 * PI * f * i / rate) * 0.25 * (1 - i.toDouble() / n) * Short.MAX_VALUE).toInt().toShort() }
                }.toShortArray()
                val track = AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(pcm.size * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
                track.write(pcm, 0, pcm.size); track.play()
                Thread.sleep(notes.sumOf { it.second }.toLong() + 100); track.release()
            }
        }.start()
    }
}

// ---------- avatar with a reward frame ----------

@Composable
fun FramedAvatar(emoji: String, size: Int = 56) {
    val frame = AppGraph.progress.frame
    val ring = when (frame) {
        "gold" -> Brush.sweepGradient(listOf(Gold, GoldLight, Gold))
        "aurora" -> Brush.sweepGradient(listOf(Color(0xFF2E9C88), Color(0xFF3B79B5), Color(0xFFC25A8C), Color(0xFF2E9C88)))
        "legend" -> Brush.sweepGradient(listOf(Color(0xFFE2462C), Gold, Color(0xFF7B67D6), Color(0xFFE2462C)))
        else -> null
    }
    if (ring == null) Avatar(emoji, size)
    else {
        val spin by rememberInfiniteTransition(label = "ring").animateFloat(0f, 360f, infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "spin")
        Box(Modifier.size((size + 8).dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.matchParentSize()) { rotate(spin) { drawCircle(ring, radius = this.size.minDimension / 2) } }
            Box(Modifier.size((size + 2).dp).clip(CircleShape).background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) { Avatar(emoji, size) }
        }
    }
}

// ---------- level card (You, Community) ----------

@Composable
fun LevelCard(nav: Nav, modifier: Modifier = Modifier) {
    val pr = AppGraph.progress
    pr.version
    val l = pr.level()
    val week = pr.weekXp()
    val league = pr.league(week)
    val next = pr.nextLeague(week)
    Surface(onClick = { nav.rewards() }, shape = RoundedCornerShape(28.dp), color = Color.Transparent, modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.background(Brush.linearGradient(listOf(NavyDeep, NavyBright))).padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FramedAvatar(AppGraph.profile.avatar, 52); Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("LEVEL $l · ${pr.title(l).uppercase()}", color = Gold, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                    Text("${pr.xp} XP", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(league.emoji, style = MaterialTheme.typography.headlineMedium)
                    Text("${league.name} week", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { pr.levelProgress() }, Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Gold, trackColor = Color.White.copy(alpha = .18f))
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${pr.need(l + 1) - pr.xp} XP to level ${l + 1}", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Text(next?.let { "${it.from - week} XP to ${it.name}" } ?: "Top league this week!", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Levels, what each unlocks, the weekly league, and pickers for colour theme and profile ring. */
@Composable
fun RewardsScreen(nav: Nav) {
    val pr = AppGraph.progress
    pr.version
    val week = pr.weekXp()
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            Row(Modifier.statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Text("Levels & rewards", style = MaterialTheme.typography.titleLarge)
            }
        }
        item { LevelCard(nav) }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                SectionTitle("This week")
                Text("Leagues are you against your own weeks: every XP counts, and a new week starts each Monday.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Progress.LEAGUES.forEach { lg ->
                        val reached = week >= lg.from
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(if (reached) Gold.copy(alpha = .22f) else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(lg.emoji, style = MaterialTheme.typography.titleLarge)
                            Text(lg.name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            Text("${lg.from}+", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                WeeklyGoal()
            }
        }
        item { Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Colour theme") } }
        item {
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (listOf(Progress.Reward(1, "accent", "navy", "Navy")) + pr.rewards().filter { it.kind == "accent" }).forEach { r ->
                    val open = pr.unlocked(r); val on = pr.accent == r.key
                    val c = ACCENTS[r.key] ?: ACCENTS.getValue("navy")
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Surface(onClick = { if (open) pr.accent = r.key }, shape = CircleShape, color = Color.Transparent,
                            border = BorderStroke(if (on) 3.dp else 0.dp, MaterialTheme.colorScheme.onBackground)) {
                            Box(Modifier.size(48.dp).semantics { contentDescription = r.title.substringBefore(" colour") + " colour theme" +
                                when { !open -> ", unlocks at level ${r.level}"; on -> ", selected"; else -> "" } }
                                .background(Brush.linearGradient(listOf(c.first, c.second))), contentAlignment = Alignment.Center) {
                                if (!open) Icon(Icons.Rounded.Lock, null, tint = Color.White) else if (on) Icon(Icons.Rounded.Check, null, tint = Color.White)
                            }
                        }
                        Text(if (open) r.title.substringBefore(" ") else "Lv ${r.level}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
        item { Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Profile ring") } }
        item {
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (listOf(Progress.Reward(1, "frame", "", "None")) + pr.rewards().filter { it.kind == "frame" }).forEach { r ->
                    val open = pr.unlocked(r)
                    Surface(onClick = { if (open) pr.frame = r.key }, shape = RoundedCornerShape(16.dp),
                        color = if (pr.frame == r.key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (open) r.title.removeSuffix(" profile ring") else "🔒", style = MaterialTheme.typography.labelLarge)
                            if (!open) Text("Level ${r.level}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item { Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Every level") } }
        items((2..12).toList()) { l ->
            val rs = pr.rewards().filter { it.level == l }
            val reached = pr.level() >= l
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp).clip(RoundedCornerShape(16.dp))
                .background(if (reached) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(if (reached) Gold else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Text("$l", fontWeight = FontWeight.ExtraBold, color = if (reached) NavyDeep else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(pr.title(l), style = MaterialTheme.typography.titleSmall)
                    Text(rs.joinToString { it.title }.ifBlank { "A new title" } + " · ${pr.need(l)} XP", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (reached) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                SectionTitle("How to earn XP")
                listOf("Finish the Daily Run" to 50, "Solve the daily puzzle" to 30, "Finish a guide (and collect its card)" to 15,
                    "Get a quiz right" to 15, "Check in on a challenge" to 20, "Comment on a guide" to 40, "Finish a simulated year" to 30,
                    "Daily check-in" to 10, "Calm minutes" to 10).forEach { (t, x) ->
                    Row(Modifier.padding(vertical = 4.dp)) {
                        Text(t, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f)); Text("+$x", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Text("Seasonal events give 50% more XP while they run.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

/** Your weekly XP target, this week against it, last week and your best week. */
@Composable
fun WeeklyGoal() {
    val pr = AppGraph.progress
    pr.version
    val week = pr.weekXp(); val goal = pr.weeklyGoal
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Column(Modifier.padding(16.dp)) {
            Text("Weekly goal", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Text("XP:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf(150, 300, 600, 1000).forEach { g ->
                    Text("$g", style = MaterialTheme.typography.labelLarge, fontWeight = if (g == goal) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (g == goal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (g == goal) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable(role = androidx.compose.ui.semantics.Role.RadioButton, onClickLabel = "Set weekly goal to $g XP") { pr.weeklyGoal = g }
                            .padding(horizontal = 10.dp, vertical = 8.dp))
                }
            }
            LinearProgressIndicator(progress = { (week / goal.toFloat()).coerceIn(0f, 1f) }, Modifier.fillMaxWidth().padding(vertical = 10.dp).height(10.dp).clip(CircleShape), color = Gold)
            Text(if (week >= goal) "🎯 Goal reached: $week XP this week!" else "$week of $goal XP this week", style = MaterialTheme.typography.bodyMedium)
            Text("Last week ${pr.lastWeek()} XP · Your best week ${pr.bestWeek()} XP" + if (week > pr.bestWeek() && pr.bestWeek() > 0) " · New personal best! 🏅" else "",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun Modifier.clickableNoRipple(onClick: () -> Unit) =
    this.clickable(interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(), indication = null, onClick = onClick)

/** Accent colours unlocked with levels (light primary, its container), used by the theme. */
val ACCENTS = mapOf(
    "navy" to (Color(0xFF1F4E79) to Color(0xFF3B7BB8)), "emerald" to (Color(0xFF0F6B5C) to Color(0xFF2E9C88)),
    "plum" to (Color(0xFF7A2E6B) to Color(0xFFC25A8C)), "sunset" to (Color(0xFFB0472A) to Color(0xFFE0883C)),
    "midnight" to (Color(0xFF2A2350) to Color(0xFFD9A75E)),
)

/** A step-by-step arrow button used across play screens. */
@Composable
fun RunButton(text: String, enabled: Boolean = true, onClick: () -> Unit) =
    Button(onClick = onClick, enabled = enabled, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(54.dp)) {
        Text(text, fontWeight = FontWeight.Bold); Spacer(Modifier.width(6.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
    }

/** Points evenly around a circle (for simple radial decorations). */
internal fun ring(n: Int, r: Float, c: Offset) = List(n) { i -> val a = 2 * PI * i / n; Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat()) }

/** A small "Lv 3 · 240 XP" chip for headers; opens Levels & rewards. */
@Composable
fun LevelChip(nav: Nav) {
    val pr = AppGraph.progress
    pr.version
    Surface(onClick = { nav.rewards() }, shape = RoundedCornerShape(50), color = Gold.copy(alpha = .22f)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Lv ${pr.level()}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(6.dp))
            Box(Modifier.width(34.dp).height(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline)) {
                Box(Modifier.fillMaxWidth(pr.levelProgress()).height(5.dp).background(Gold))
            }
        }
    }
}
