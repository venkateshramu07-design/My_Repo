package com.randomthoughtsever.plainly.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.Post
import com.randomthoughtsever.plainly.tools.QuickTools
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

/** Remembers the Today feed across tab switches. */
private object HomeCache { var posts: List<Post> = emptyList(); var next = 1; var end = false }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: Nav) {
    val repo = AppGraph.repo
    val lib = AppGraph.library
    val scope = rememberCoroutineScope()
    var posts by remember { mutableStateOf(HomeCache.posts) }
    var loading by remember { mutableStateOf(posts.isEmpty()) }
    var failed by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }

    suspend fun load(reset: Boolean) {
        try {
            val start = if (reset) 1 else HomeCache.next
            val page = repo.latest(start, 20)
            posts = if (reset) page else (posts + page).distinctBy { it.id }
            HomeCache.posts = posts; HomeCache.next = start + page.size; HomeCache.end = page.size < 20
            failed = false
        } catch (e: Exception) { failed = true }
    }
    LaunchedEffect(Unit) { if (posts.isEmpty()) { load(true); loading = false } }

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = { scope.launch { refreshing = true; load(true); refreshing = false } },
        modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
            item { HomeHeader(nav) }
            when {
                loading -> {
                    item { Shimmer(Modifier.fillMaxWidth().height(320.dp).padding(horizontal = 20.dp)) }
                    items(5) { Box(Modifier.padding(horizontal = 16.dp)) { RowSkeleton() } }
                }
                posts.isEmpty() -> item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        EmptyState(Icons.Rounded.CloudOff, "You're offline", "Connect to the internet to load the latest guides. Your saved articles still open.")
                        FilledTonalButton(onClick = { scope.launch { loading = true; load(true); loading = false } }) { Text("Try again") }
                    }
                }
                else -> {
                    item { Featured(posts.take(5), nav) }
                    if (lib.history.isNotEmpty()) item {
                        Column(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Continue reading") }
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(lib.history.take(8), key = { it.id }) { r ->
                                Column(Modifier.width(200.dp).clip(RoundedCornerShape(20.dp)).clickable { nav.article(r.id) }) {
                                    Cover(r.image, r.label, Modifier.fillMaxWidth().height(112.dp), 28.dp, alignment = Alignment.CenterEnd)
                                    Spacer(Modifier.height(8.dp))
                                    Text(r.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    item {
                        Column(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Quick tools", "All tools") { nav.tab("tools") } }
                        QuickTools(nav)
                    }
                    item {
                        Column(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Topics", "Explore") { nav.tab("explore") } }
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(Topics) { t ->
                                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface, onClick = { nav.topic(t.label) },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                                    Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(t.icon, null, tint = t.to, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                                        Text(t.title, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }
                    item { Column(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Latest") } }
                    items(posts.drop(5), key = { it.id }) { p -> Box(Modifier.padding(horizontal = 16.dp)) { PostRow(p) { nav.article(p.id) } } }
                    if (!HomeCache.end) item {
                        LaunchedEffect(posts.size) { if (!loadingMore) { loadingMore = true; load(false); loadingMore = false } }
                        Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(nav: Nav) {
    val hour = LocalTime.now().hour
    val greeting = when (hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; in 17..21 -> "Good evening"; else -> "Hello, night owl" }
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern(android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEEMMMMd"), Locale.getDefault()))
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Logo(40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(date.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(greeting, style = MaterialTheme.typography.headlineMedium)
        }
        IconButton(onClick = nav::search) { Icon(Icons.Rounded.Search, "Search") }
        IconButton(onClick = nav::settings) { Icon(Icons.Rounded.Settings, "Settings") }
    }
}

/** The Plainly mark: navy tile, white P, gold needle dot. */
@Composable
fun Logo(size: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(size * .3f)).background(Brush.linearGradient(listOf(NavyBright, Navy, NavyDeep))),
        contentAlignment = Alignment.Center) {
        Text("P", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Box(Modifier.align(Alignment.TopEnd).padding(size * .16f).size(size * .16f).clip(CircleShape).background(Gold))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Featured(posts: List<Post>, nav: Nav) {
    if (posts.isEmpty()) return
    val state = rememberPagerState { posts.size }
    var userTouched by rememberSaveable { mutableStateOf(false) }
    val dragged by state.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(dragged) { if (dragged) userTouched = true } // auto-advance stops once the reader swipes
    LaunchedEffect(userTouched) {
        while (!userTouched) { delay(5500); state.animateScrollToPage((state.currentPage + 1) % posts.size) }
    }
    Column {
        HorizontalPager(state, contentPadding = PaddingValues(horizontal = 20.dp), pageSpacing = 12.dp) { i ->
            val offset = ((state.currentPage - i) + state.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
            FeatureCard(posts[i], Modifier.fillMaxWidth().graphicsLayer {
                scaleY = 1f - offset * .06f; alpha = 1f - offset * .3f
            }) { nav.article(posts[i].id) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center) {
            repeat(posts.size) { i ->
                val on = state.currentPage == i
                Box(Modifier.padding(3.dp).height(6.dp).width(if (on) 22.dp else 6.dp).clip(CircleShape)
                    .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline))
            }
        }
    }
}

@Composable
fun rememberPaging(): androidx.compose.runtime.MutableIntState = remember { mutableIntStateOf(1) }
