package com.randomthoughtsever.plainly.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomthoughtsever.plainly.data.BlogTalk
import com.randomthoughtsever.plainly.data.Seasons
import com.randomthoughtsever.plainly.data.questionOfTheWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.IsoFields

/** The sky behind Today's header, following the time of day. */
fun skyBrush(hour: Int = LocalTime.now().hour, dark: Boolean): Brush {
    val a = if (dark) .35f else .22f
    val c = when (hour) {
        in 5..10 -> listOf(Color(0xFFF7D58A), Color(0xFFF2A65A))     // sunrise
        in 11..16 -> listOf(Color(0xFF8EC5E8), Color(0xFFDCE8F4))    // day
        in 17..20 -> listOf(Color(0xFFC25A8C), Color(0xFFF2A65A))    // sunset
        else -> listOf(Color(0xFF1B2A4E), Color(0xFF3B2F6B))          // night
    }
    return Brush.verticalGradient(listOf(c[0].copy(alpha = a), c[1].copy(alpha = a * .5f), Color.Transparent))
}

/** The seasonal event, while one runs: its theme, 50% more XP, and a way to take part. */
@Composable
fun SeasonBanner(nav: Nav) {
    val s = Seasons.current() ?: return
    val pr = AppGraph.progress
    pr.version
    val joined = pr.joinedEvent(s.key + LocalDate.now().year)
    Surface(shape = RoundedCornerShape(26.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.background(Brush.linearGradient(listOf(s.c1, s.c2))).padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.emoji, fontSize = 34.sp); Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("EVENT · 50% MORE XP", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                    Text(s.title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
            }
            Text(s.blurb, color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(onClick = {
                    pr.award("event:${s.key}${LocalDate.now().year}", 40, "Joined ${s.title}")
                    s.challenge?.let { nav.challenge(it) } ?: nav.search(s.guides.pattern.removePrefix("(?i)").substringBefore('|'))
                }, shape = RoundedCornerShape(50), color = Color.White) {
                    Text(if (joined) "Keep going" else if (s.challenge != null) "Join the event" else "Take part", color = s.c1, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp))
                }
                if (joined) Text("✓ You're in", color = Color.White, style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
    }
}

/** Three quick things beyond the run: the puzzle, the money year and the calm corner. Evening puts calm first. */
@Composable
fun PlayStrip(nav: Nav) {
    val evening = LocalTime.now().hour >= 20 || LocalTime.now().hour < 5
    data class Tile(val emoji: String, val title: String, val sub: String, val c1: Color, val c2: Color, val go: () -> Unit)
    val tiles = listOf(
        Tile("🧩", "Daily puzzle", if (AppGraph.progress.puzzle() != null) "Done ✓" else "New today", Color(0xFF3B2F6B), Color(0xFFB0472A)) { nav.puzzle() },
        Tile("💰", "A Year of Money", "Play a year in 5 min", Color(0xFF0F5F55), Color(0xFF2E9C88)) { nav.sim() },
        Tile(if (evening) "🌙" else "🌿", if (evening) "Wind down" else "Calm minute", if (evening) "Sleep sounds & prompts" else "Breathe, focus, rest",
            Color(0xFF145A6B), Color(0xFF3A9AAE)) { nav.calm(if (evening) "sleep" else "breathe") },
        Tile("🃏", "My cards", "${AppGraph.progress.cards().size} collected", Color(0xFF7A2E55), Color(0xFFC25A8C)) { nav.cards() },
    ).let { if (evening) listOf(it[2], it[0], it[1], it[3]) else it }
    Column(Modifier.padding(vertical = 6.dp)) {
        Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Play & unwind") }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tiles, key = { it.title }) { t ->
                Surface(onClick = t.go, shape = RoundedCornerShape(22.dp), color = Color.Transparent, modifier = Modifier.width(150.dp)) {
                    Column(Modifier.background(Brush.linearGradient(listOf(t.c1, t.c2))).padding(14.dp)) {
                        Text(t.emoji, fontSize = 30.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(t.title, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                        Text(t.sub, color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }
            }
        }
    }
}

/** The question of the week, the same for everyone; answers are comments on its guide. */
@Composable
fun QuestionOfWeekCard(nav: Nav, modifier: Modifier = Modifier) {
    val q = remember { questionOfTheWeek() }
    var compose by remember { mutableStateOf(false) }
    val answers by produceState<Int?>(null) {
        val weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        value = runCatching { BlogTalk.on(q.post).count { it.published >= weekStart } }.getOrNull()
    }
    if (compose) TalkComposer(q.post, "", nav) { compose = false; AppGraph.progress.award("qotw:${LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}", 10, "Answering the question of the week") }
    Surface(shape = RoundedCornerShape(26.dp), color = Color.Transparent, modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF8A4B1E), Color(0xFFD08844)))).padding(18.dp)) {
            Text("QUESTION OF THE WEEK", color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
            Text(q.text, color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = { compose = true }, shape = RoundedCornerShape(50), color = Color.White) {
                    Text("Answer", color = Color(0xFF8A4B1E), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp))
                }
                Spacer(Modifier.width(10.dp))
                TextButton(onClick = { nav.talk(q.post) }) {
                    Text(when (val a = answers) { null -> "See answers"; 0 -> "Be the first to answer"; 1 -> "1 answer this week"; else -> "$a answers this week" }, color = Color.White)
                }
            }
        }
    }
}

/** Guides people are talking about right now, from real comments on the blog (nothing shown if there are none). */
@Composable
fun HotDiscussions(nav: Nav) {
    val hot by produceState<List<Pair<String, Int>>>(emptyList()) {
        value = runCatching {
            val since = System.currentTimeMillis() - 30L * 86_400_000
            BlogTalk.recent().filter { it.published >= since && it.postId != BlogTalk.board }.groupingBy { it.postId }.eachCount().entries
                .sortedByDescending { it.value }.take(5).map { it.key to it.value }
        }.getOrDefault(emptyList())
    }
    if (hot.isEmpty()) return
    Column(Modifier.padding(vertical = 6.dp)) {
        Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Hot discussions", "Community") { nav.tab("community") } }
        hot.forEach { (id, n) ->
            val e = AppGraph.repo.preview(id) ?: return@forEach
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surface)
                .clickableNoRipple { nav.talk(id) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", fontSize = 22.sp); Spacer(Modifier.width(10.dp))
                Text(e.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Text("💬 $n", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}


/** "A streak freeze saved your streak": shown on the day a freeze covered a missed day. */
@Composable
fun FreezeNote() {
    val lib = AppGraph.library
    var shown by remember { mutableStateOf(lib.freezeUsedOn() == LocalDate.now().toString()) }
    if (!shown) return
    Surface(onClick = { lib.clearFreezeNote(); shown = false }, shape = RoundedCornerShape(22.dp), color = Color(0xFFDDF2FB),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🧊", fontSize = 28.sp); Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("A streak freeze saved your ${lib.streak()}-day streak", style = MaterialTheme.typography.titleSmall, color = Color(0xFF0E3A4F))
                Text("You missed a day, so one freeze covered it. ${lib.freezes()} left. Tap to close.", style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0E3A4F).copy(alpha = .8f))
            }
        }
    }
}
