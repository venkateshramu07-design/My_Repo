package com.randomthoughtsever.plainly.ui

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.Entry

private val TRENDING = listOf("credit score", "passport photo", "minimum wage", "karma", "burnout", "retire early", "inflation",
    "AI jobs", "sleep", "compound interest", "student loans", "tax free countries")

/**
 * Explore: search with suggestions, collections (shelves that fill themselves from the full index), countries with
 * flags, every topic with its real count and newest guide, a "surprise me" button and the full A-to-Z library.
 */
@Composable
fun ExploreScreen(nav: Nav) {
    val index = rememberIndex()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
        item { ExploreHeader(index, nav) }
        if (index == null) item { ExploreSkeleton() } else {
            val shelves = Shelves.map { it to index.count(it.match) }.filter { it.second >= 3 }
            item {
                Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Collections") }
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(shelves, key = { it.first.key }) { (s, n) -> ShelfCard(s, n) { nav.shelf(s.key) } }
                }
            }
            val cs = countries(index)
            if (cs.isNotEmpty()) item {
                val compare = index.firstOrNull { it.title.startsWith("Compare Countries") }
                Box(Modifier.padding(horizontal = 20.dp)) {
                    SectionTitle("${cs.size} countries in numbers", if (compare != null) "Compare" else null) { compare?.let { nav.article(it.id) } }
                }
                Text("Minimum wage, tax, mortgages and pensions for each country.", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp))
                LazyHorizontalGrid(GridCells.Fixed(2), Modifier.height(116.dp), contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(cs, key = { it.name }) { c -> CountryChip(c) { nav.article(c.id) } }
                }
            }
            item { Box(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) { SectionTitle("Topics") } }
            val topics = Topics.map { t -> Triple(t, index.count { it.label == t.label || t.label in it.labels }, index.firstOrNull { it.label == t.label }) }
                .filter { it.second > 0 }.sortedByDescending { it.second }
            topics.chunked(2).forEach { row ->
                item(key = "t-" + row.first().first.label) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { (t, n, newest) -> TopicCard(t, n, newest, Modifier.weight(1f)) { nav.topic(t.label) } }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            item {
                Surface(onClick = nav::library, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Rounded.LibraryBooks, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Browse all ${index.size} guides", style = MaterialTheme.typography.titleMedium)
                            Text("Newest first or A to Z, by topic", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreHeader(index: List<Entry>?, nav: Nav) {
    var spins by remember { mutableFloatStateOf(0f) }
    val turn by animateFloatAsState(spins, tween(500), label = "dice")
    Column(Modifier.statusBarsPadding().padding(top = 16.dp)) {
        Row(Modifier.padding(start = 20.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Explore", style = MaterialTheme.typography.displaySmall)
                Text(if (index != null) "${index.size} guides on money, mind, work and the world" else "Guides on money, mind, work and the world",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(onClick = {
                spins += 360f
                index?.filter { isReadingTopic(it.label) }?.randomOrNull()?.let { nav.article(it.id) }
            }, shape = RoundedCornerShape(18.dp), color = Gold.copy(alpha = .22f)) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Casino, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.rotate(turn))
                    Text("Surprise me", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
        Surface(onClick = { nav.search() }, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(10.dp))
                Text("Search guides and tools", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Text("PEOPLE SEARCH FOR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TRENDING) { q ->
                Surface(onClick = { nav.search(q) }, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Search, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(6.dp))
                        Text(q, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ShelfCard(s: Shelf, count: Int, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(26.dp), color = Color.Transparent, modifier = Modifier.width(230.dp).height(160.dp)) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(s.from, s.to)))) {
            Icon(s.icon, null, tint = Color.White.copy(alpha = .14f), modifier = Modifier.size(120.dp).align(Alignment.BottomEnd).padding(start = 16.dp, top = 16.dp))
            Column(Modifier.padding(18.dp).fillMaxSize()) {
                Box(Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                    Icon(s.icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(s.title, style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$count guides · ${s.blurb}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .85f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun TopicCard(t: Topic, count: Int, newest: Entry?, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(24.dp), color = Color.Transparent, modifier = modifier.height(150.dp)) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(t.from, t.to)))) {
            TopicScene(t.label, Modifier.fillMaxWidth(.62f).fillMaxHeight(.62f).align(Alignment.TopEnd).graphicsLayer { alpha = .42f }, background = false, seconds = 6)
            Column(Modifier.padding(16.dp).fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(t.icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.weight(1f))
                    Text("$count", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.background(Color.White.copy(alpha = .2f), RoundedCornerShape(50)).padding(horizontal = 9.dp, vertical = 2.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(t.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                newest?.let {
                    Text("New: ${it.title}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .82f), maxLines = 2,
                        overflow = TextOverflow.Ellipsis)
                } ?: Text(t.blurb, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .82f), maxLines = 2)
            }
        }
    }
}

@Composable
private fun ExploreSkeleton() = Column(Modifier.padding(20.dp)) {
    Shimmer(Modifier.width(160.dp).height(24.dp)); Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { repeat(2) { Shimmer(Modifier.width(230.dp).height(160.dp).clip(RoundedCornerShape(26.dp))) } }
    Spacer(Modifier.height(24.dp))
    repeat(3) { Row(Modifier.padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { repeat(2) { Shimmer(Modifier.weight(1f).height(150.dp).clip(RoundedCornerShape(24.dp))) } } }
}
