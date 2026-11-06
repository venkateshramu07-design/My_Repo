package com.randomthoughtsever.plainly.ui

import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.randomthoughtsever.plainly.data.GOALS
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.BuildConfig
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** You: your streak and numbers, the topics you read most, your saved guides and history, and the settings that matter. */
@Composable
fun YouScreen(nav: Nav) {
    val lib = AppGraph.library
    val context = LocalContext.current
    val streak = remember { lib.streak() }
    var goals by remember { mutableStateOf(false) }
    var numbers by remember { mutableStateOf(false) }
    var identity by remember { mutableStateOf(false) }
    if (goals) GoalsDialog(AppGraph.profile.goals) { g -> g?.let { AppGraph.profile.updateGoals(it) }; goals = false }
    if (numbers) NumbersDialog(AppGraph.profile.numbers, AppGraph.profile.currency()) { n -> n?.let { AppGraph.profile.updateNumbers(it) }; numbers = false }
    if (identity) IdentityDialog { identity = false }
    val days = remember { lib.openDays() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            Row(Modifier.statusBarsPadding().padding(start = 20.dp, end = 8.dp, top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("You", style = MaterialTheme.typography.displaySmall, modifier = Modifier.weight(1f))
                IconButton(onClick = nav::settings) { Icon(Icons.Rounded.Settings, "Settings") }
            }
        }
        item { LevelCard(nav) }
        item { Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) { WeeklyGoal() } }
        item {
            val pr = AppGraph.progress
            Column(Modifier.padding(horizontal = 12.dp)) {
                LinkRow(Icons.Rounded.Style, "My collection", "${pr.cards().size} guide cards · albums by topic") { nav.cards() }
                LinkRow(Icons.Rounded.EditNote, "My notebook", "${pr.notes().size} saved lines") { nav.notebook() }
                LinkRow(Icons.Rounded.AutoAwesome, "Weekly recap", "Your last 7 days as a story") { nav.recap("week") }
                LinkRow(Icons.Rounded.SelfImprovement, "Calm corner", "${pr.calmMinutes()} calm minutes so far") { nav.calm() }
                if (pr.tried().isNotEmpty()) LinkRow(Icons.Rounded.TaskAlt, "Things I'm trying",
                    "${pr.tried().size} tried · ${pr.tried().count { it.result == "yes" }} worked") { nav.cards() }
            }
        }
        item { StreakCard(streak, days) }
        item {
            Row(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat(Icons.AutoMirrored.Rounded.MenuBook, "${lib.reads()}", "guides read", Modifier.weight(1f))
                Stat(Icons.Rounded.Bookmarks, "${lib.saved.size}", "saved", Modifier.weight(1f))
                Stat(Icons.Rounded.Handyman, "${lib.toolUses()}", "files made", Modifier.weight(1f))
            }
        }
        item { ChecksCard(nav) }
        item { Badges() }
        item { KnowledgeMap(nav) }
        item { YourTopics() }
        item {
            Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Saved", if (lib.saved.isNotEmpty()) "See all ${lib.saved.size}" else null) { nav.saved() } }
            if (lib.saved.isEmpty()) Text("Tap the bookmark on any guide to keep it here. Saved guides open even without internet.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp))
            else Column(Modifier.padding(horizontal = 16.dp)) { lib.saved.take(3).forEach { r -> PostRow(r, { nav.article(r.id) }) } }
        }
        if (lib.history.isNotEmpty()) item {
            Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Recently read", "See all") { nav.saved(history = true) } }
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(lib.history.take(10), key = { it.id }) { r ->
                    Column(Modifier.width(170.dp).clip(RoundedCornerShape(18.dp)).clickable { nav.article(r.id) }) {
                        Cover(r.image, r.label, Modifier.fillMaxWidth().height(96.dp), 26.dp, alignment = Alignment.CenterEnd)
                        Spacer(Modifier.height(6.dp))
                        Text(r.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        item {
            Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Look and feel") }
            val modes = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                modes.forEachIndexed { i, (k, l) -> SegmentedButton(lib.theme == k, { lib.setThemeMode(k) }, SegmentedButtonDefaults.itemShape(i, modes.size)) { Text(l) } }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
                LinkRow(Icons.Rounded.EmojiEvents, "Challenges", "7-day no-spend, 30-day debt sprint and more") { nav.challenges() }
                LinkRow(Icons.Rounded.Flag, "My goals", GOALS.filter { it.key in AppGraph.profile.goals }.joinToString { it.title }.ifBlank { "Choose what you want help with" }) { goals = true }
                LinkRow(Icons.Rounded.Calculate, "My numbers", "Pay, spending, savings and debt (on your phone only)") { numbers = true }
                LinkRow(Icons.Rounded.Groups, "Community profile", "${AppGraph.profile.avatar} ${AppGraph.profile.name}") { identity = true }
                LinkRow(Icons.Rounded.Settings, "Settings", "Text size, notifications, privacy") { nav.settings() }
                LinkRow(Icons.Rounded.Favorite, "Support Plainly", "Plainly is free. A small tip keeps it going") { nav.support() }
                LinkRow(Icons.Rounded.Star, "Rate Plainly", "It helps other people find it") {
                    runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}"))) }
                        .onFailure { nav.web("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}") }
                }
                LinkRow(Icons.Rounded.Share, "Share Plainly", "Send it to a friend") {
                    nav.share("Plainly: Money, Mind & Work", "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                }
            }
        }
        item {
            Text("Plainly ${BuildConfig.VERSION_NAME} · No account needed", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun Badges() {
    val lib = AppGraph.library; val p = AppGraph.profile
    p.version
    val list = remember(p.version) {
        com.randomthoughtsever.plainly.data.badges(lib.reads(), lib.streak(), lib.saved.size, lib.toolUses(), p.quizStats().first,
            com.randomthoughtsever.plainly.data.CHECKS.count { p.check(it.id) != null }, p.dailyCompleted() + AppGraph.progress.runs(), p.finishedChallenges(), p.breaths())
    }
    Column(Modifier.padding(top = 6.dp)) {
        Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Badges · ${list.count { it.earned }} of ${list.size}") }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(list) { b ->
                Column(Modifier.width(84.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = { b.progress }, modifier = Modifier.fillMaxSize(), strokeWidth = 4.dp,
                            color = if (b.earned) Gold else MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                        Text(b.emoji, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.graphicsLayer { alpha = if (b.earned) 1f else .35f })
                    }
                    Text(b.title, style = MaterialTheme.typography.labelMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1)
                    Text(b.how, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2)
                }
            }
        }
    }
}

/** How much of each topic the reader has explored: guides read out of all guides in that topic. */
@Composable
private fun KnowledgeMap(nav: Nav) {
    val index = rememberIndex() ?: return
    val read = remember { AppGraph.library.readIds() }
    val rows = remember(index) {
        Topics.map { t -> val all = index.filter { it.label == t.label && !it.isToolPage }; Triple(t, all.count { it.id in read }, all.size) }
            .filter { it.third > 0 }.sortedByDescending { it.second.toFloat() / it.third }
    }
    Column(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
        SectionTitle("Knowledge map")
        Text("Guides you've read in each topic. Tap one to keep going.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { (t, n, total) ->
                val f = n / total.toFloat()
                Surface(onClick = { nav.topic(t.label) }, shape = RoundedCornerShape(16.dp), color = Color.Transparent) {
                    Box(Modifier.background(Brush.linearGradient(listOf(t.from.copy(alpha = .25f + .75f * f), t.to.copy(alpha = .25f + .75f * f)))).padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Column {
                            Text(t.title, style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("$n of $total", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = .85f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakCard(streak: Int, days: Set<String>) {
    val today = LocalDate.now()
    val lib = AppGraph.library
    AppGraph.progress.version
    val frozen = remember { lib.frozenDays() }
    val freezes = lib.freezes()
    Surface(shape = RoundedCornerShape(28.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Column(Modifier.background(Brush.linearGradient(listOf(Navy, NavyDeep))).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(52.dp).clip(CircleShape).background(Gold.copy(alpha = .2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.LocalFireDepartment, null, tint = Gold, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (streak <= 1) "Day 1" else "$streak-day streak", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text(if (streak <= 1) "Come back tomorrow to start a streak." else "Open Plainly tomorrow to keep it going.",
                        style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .78f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.semantics(mergeDescendants = true) {}) {
                    Text("🧊".repeat(freezes.coerceAtLeast(0)).ifBlank { "🧊" }, style = MaterialTheme.typography.titleLarge,
                        color = if (freezes > 0) Color.Unspecified else Color.White.copy(alpha = .3f), modifier = Modifier.graphicsLayer { alpha = if (freezes > 0) 1f else .35f })
                    Text("$freezes freeze${if (freezes == 1) "" else "s"}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = .8f))
                }
            }
            Text(if (freezes > 0) "A streak freeze covers a missed day automatically. Earn one with every 5th Daily Run or level-up (hold up to 2)."
                else "Earn a streak freeze with every 5th Daily Run or level-up. It covers a missed day so your streak survives.",
                style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .7f), modifier = Modifier.padding(top = 10.dp))
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (6 downTo 0).map { today.minusDays(it.toLong()) }.forEach { d ->
                    val on = d.toString() in days
                    val ice = d.toString() in frozen
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(30.dp).clip(CircleShape).background(when { on -> Gold; ice -> Color(0xFF8FD3F0); else -> Color.White.copy(alpha = .12f) }),
                            contentAlignment = Alignment.Center) {
                            if (on) Icon(Icons.Rounded.LocalFireDepartment, null, tint = NavyDeep, modifier = Modifier.size(16.dp))
                            else if (ice) Text("🧊", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(d.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()), style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = if (d == today) 1f else .65f), fontWeight = if (d == today) FontWeight.ExtraBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(icon: ImageVector, value: String, label: String, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun YourTopics() {
    val counts = AppGraph.library.history.groupingBy { it.label }.eachCount().entries.filter { isReadingTopic(it.key) }
        .sortedByDescending { it.value }.take(5)
    Column(Modifier.padding(horizontal = 20.dp)) {
        SectionTitle("What you read")
        if (counts.isEmpty()) {
            Text("Read a few guides and your favourite topics show up here.", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }
        val max = counts.first().value.toFloat()
        counts.forEach { (label, n) ->
            val t = topicFor(label)
            val f by animateFloatAsState(n / max, tween(700), label = "topic")
            Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(t.icon, null, tint = t.to, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(t.title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(110.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(Modifier.fillMaxWidth(f).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(Brush.horizontalGradient(listOf(t.from, t.to))))
                }
                Spacer(Modifier.width(10.dp))
                Text("$n", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}
