package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.Entry
import kotlinx.coroutines.launch
import java.time.LocalDate

private val GROUPS = linkedMapOf(
    "For you" to emptySet(),
    "Money" to setOf("Personal Finance", "Investing", "Economics", "Cryptocurrency"),
    "Mind" to setOf("Psychology", "Self Improvement", "Zen & Mindfulness", "Health & Wellness", "Love & Relationships"),
    "Work" to setOf("Career & Workplace", "Leadership", "Technology & AI", "Education"),
    "World" to setOf("Countries & Living Abroad"),
)

/** "For you": newest first, with topics you read most moved up, as if they were a month newer. */
private fun forYou(list: List<Entry>): List<Entry> {
    val fav = AppGraph.library.history.groupingBy { it.label }.eachCount().entries.sortedByDescending { it.value }.take(3).map { it.key }.toSet()
    return list.sortedByDescending { e -> runCatching { LocalDate.parse(e.published).toEpochDay() }.getOrDefault(0) + if (e.label in fav) 30 else 0 }
}

/** Quick reads: one guide per screen, its quick answer up front. Swipe up for the next. */
@Composable
fun QuickReadsScreen(nav: Nav) {
    val index = rememberIndex()
    var group by rememberSaveable { mutableStateOf("For you") }
    val list = remember(index, group) {
        val usable = index.orEmpty().filter { it.quick.length > 60 && isReadingTopic(it.label) && !it.isToolPage }
        if (group == "For you") forYou(usable) else usable.filter { it.label in GROUPS[group]!! }
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.statusBarsPadding().padding(top = 12.dp)) {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Quick reads", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                if (list.isNotEmpty()) Text("${list.size} answers", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(GROUPS.keys.toList()) { g -> FilterChip(group == g, { group = g }, label = { Text(g) }, shape = RoundedCornerShape(50)) }
            }
        }
        if (index == null) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else {
            val pager = rememberPagerState { list.size }
            VerticalPager(pager, Modifier.weight(1f), key = { list[it].id }, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
                pageSpacing = 14.dp) { i ->
                QuickCard(list[i], i == 0 && pager.currentPage == 0, nav)
            }
        }
    }
}

@Composable
private fun QuickCard(e: Entry, hint: Boolean, nav: Nav) {
    val lib = AppGraph.library
    val scope = rememberCoroutineScope()
    val t = topicFor(e.label)
    Surface(shape = RoundedCornerShape(30.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp, modifier = Modifier.fillMaxSize()) {
        Column {
            Box {
                Cover(e.image, e.label, Modifier.fillMaxWidth().aspectRatio(COVER_RATIO), 56.dp, corner = 0.dp)
                Text(t.title.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(14.dp).background(t.from.copy(alpha = .85f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp))
            }
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp).weight(1f)) {
                Text(e.title, style = MaterialTheme.typography.headlineSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.weight(1f)) {
                    Box(Modifier.width(4.dp).fillMaxSize().background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(12.dp))
                    Text(e.quick, style = MaterialTheme.typography.bodyLarge, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = {
                        scope.launch { (AppGraph.repo.cached(e.id) ?: AppGraph.repo.post(e.id))?.let(lib::toggleSave) }
                    }) { Icon(if (lib.isSaved(e.id)) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, "Save") }
                    Spacer(Modifier.width(6.dp))
                    FilledTonalIconButton(onClick = { nav.share(e.title, e.url) }) { Icon(Icons.Rounded.Share, "Share") }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { nav.article(e.id) }, shape = RoundedCornerShape(50)) {
                        Text("Read the guide"); Spacer(Modifier.width(6.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp))
                    }
                }
                if (hint) SwipeHint()
            }
        }
    }
}

@Composable
private fun SwipeHint() {
    val y by rememberInfiniteTransition(label = "hint").animateFloat(0f, -6f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "y")
    Row(Modifier.fillMaxWidth().padding(top = 10.dp).offset(y = y.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.KeyboardArrowUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Swipe up for the next one", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
