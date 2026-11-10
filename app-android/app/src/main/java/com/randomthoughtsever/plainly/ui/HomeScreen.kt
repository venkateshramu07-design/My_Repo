package com.randomthoughtsever.plainly.ui

import com.randomthoughtsever.plainly.data.intentOf
import androidx.compose.material.icons.rounded.AutoStories
import com.randomthoughtsever.plainly.data.Quiz
import com.randomthoughtsever.plainly.data.QUIZZES
import com.randomthoughtsever.plainly.data.Images
import com.randomthoughtsever.plainly.data.Entry
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.lazy.LazyListScope
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.FiberNew
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.randomthoughtsever.plainly.ads.BannerAd
import com.randomthoughtsever.plainly.data.NewPosts
import com.randomthoughtsever.plainly.data.Post
import com.randomthoughtsever.plainly.tools.Calc
import com.randomthoughtsever.plainly.tools.Money
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue

/** Keeps Today's posts across tab switches. */
private object HomeCache { var posts: List<Post> = emptyList(); var loadedAt = 0L; var newestId: String? = null }

/**
 * Today: stories by topic, a daily pick, what's just been published, a live
 * money widget, flip-card quick answers and a topic spotlight (articles only; tools have their own tab), followed by "More to read": every guide, newest first.
 * New posts show up the moment Today opens or comes back to the screen, and it checks every minute while visible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: Nav) {
    val repo = AppGraph.repo
    val scope = rememberCoroutineScope()
    var posts by remember { mutableStateOf(HomeCache.posts.ifEmpty { repo.today.filterNot { it.isToolPage } }) }
    var loading by remember { mutableStateOf(posts.isEmpty()) }
    var refreshing by remember { mutableStateOf(false) }
    var fresh by remember { mutableStateOf<Post?>(null) }
    var story by remember { mutableStateOf<String?>(null) }
    val list = rememberLazyListState()
    val profile = AppGraph.profile
    profile.version
    // Quiz cards for the feed: the reader's goals first, unanswered ones first.
    val quizzes = remember(profile.version) {
        QUIZZES.sortedWith(compareBy<Quiz>({ profile.answered(it.id) != null }, { it.goal !in profile.goals }))
    }
    // "Today in 60 seconds": the newest guides as stories, played once a day when Today first opens.
    val prefs = remember { NewPosts.prefs(nav.context) }
    LaunchedEffect(posts.isNotEmpty()) {
        val today = LocalDate.now().toString()
        if (posts.isNotEmpty() && profile.onboarded && prefs.getString("autoDeck", "") != today) {
            prefs.edit().putString("autoDeck", today).apply(); delay(700); story = "new"
        }
    }
    // "More to read": every guide on the blog, newest first, after Today's sections. Loads 20 more as you reach the end.
    val index by produceState<List<Entry>>(emptyList(), posts.firstOrNull()?.id) { value = AppGraph.repo.index(force = false) }
    var shown by rememberSaveable { mutableIntStateOf(20) }
    val more = remember(posts, index) {
        (posts.map { it.toEntry() } + index.filterNot { it.isToolPage }).distinctBy { it.id }.sortedByDescending { it.published }
            .let { all -> if (posts.isEmpty()) all else all.filter { it.id != dailyPick(posts).id } }
            .let(::mixTopics)
            .let { com.randomthoughtsever.plainly.data.Variety.spread(it) }   // no "minimum wage" after "minimum wage"
    }
    val context = LocalContext.current
    LaunchedEffect(more.size, shown) { Images.prefetch(context, more.take(shown + 20).drop((shown - 20).coerceAtLeast(0)).map { it.image }) }

    // Today shows articles only; tool pages are in the Tools tab (new ones appear there under "New").
    fun show(all: List<Post>, live: Boolean) {
        val p = all.filterNot { it.isToolPage }
        if (p.isEmpty()) return
        val before = posts.firstOrNull()?.id
        posts = p; HomeCache.posts = p; repo.rememberToday(all); loading = false
        if (live) { HomeCache.newestId = all.first().id; HomeCache.loadedAt = System.currentTimeMillis() }
        if (live && before != null && p.first().id != before) fresh = p.first()
    }
    suspend fun load() {
        // Nothing on screen yet (a first-ever open): a small request first so the page fills in about a second,
        // then the full set. Otherwise the full set refreshes quietly behind what's already shown.
        if (posts.isEmpty()) runCatching { repo.latest(1, com.randomthoughtsever.plainly.data.TODAY_FIRST) }.onSuccess { show(it, live = true) }
        runCatching { repo.latest(1, com.randomthoughtsever.plainly.data.TODAY_COUNT) }.onSuccess { show(it, live = true) }
    }
    LaunchedEffect(Unit) {
        // Instant: the copy saved last time (read off the main thread), then the network behind it.
        if (posts.isEmpty()) repo.cachedLatest()?.let { show(it, live = false) }
        if (posts.isEmpty() || System.currentTimeMillis() - HomeCache.loadedAt > 30_000) load()
        loading = false
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (HomeCache.loadedAt != 0L && System.currentTimeMillis() - HomeCache.loadedAt > 30_000) scope.launch { load() }
    }
    LaunchedEffect(Unit) { // live check while Today is on screen: one tiny request a minute
        while (true) {
            delay(60_000)
            val newest = repo.newest()
            if (newest != null && posts.isNotEmpty() && newest.first != HomeCache.newestId) load()
        }
    }
    LaunchedEffect(fresh) { if (fresh != null) { delay(10_000); fresh = null } }

    Box(Modifier.fillMaxSize()) {
        PullToRefreshBox(refreshing, { scope.launch { refreshing = true; load(); refreshing = false } }, Modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize(), state = list, contentPadding = PaddingValues(bottom = 28.dp)) {
                item { HomeHeader(nav, posts) }
                when {
                    loading -> item { HomeSkeleton() }
                    posts.isEmpty() -> item { Offline(nav) { scope.launch { loading = true; load(); loading = false } } }
                    else -> {
                        val pick = dailyPick(posts)
                        item { Stories(posts) { story = it } }
                        item { FreezeNote() }
                        // New readers get one clear path first; the Daily Run and check-up cards wait until it's done.
                        val starter = AppGraph.progress.version.let { AppGraph.progress.starterActive(AppGraph.library.firstOpen()) }
                        item { StarterPath(nav) }
                        if (!starter) item { DailyRunCard(nav) }
                        item { SeasonBanner(nav) }
                        item { TriedFollowUpCard() }
                        item { RecapCard(nav) }
                        if (!starter) item { ChecksCard(nav) }
                        if (profile.numbers.isSet) item { MyNumbersCard() }
                        item { PlanCard(index, nav) }
                        item { TodaysPick(pick, nav) }
                        item { PlayStrip(nav) }
                        item { ChallengeCards(nav) }
                        if (AppGraph.library.history.isNotEmpty()) item { ContinueReading(nav) }
                        item { JustPublished(posts.filter { it.id != pick.id }.take(10), nav) }
                        item { MoneyIn10Seconds(nav) }
                        if (!profile.numbers.isSet) item { MyNumbersCard() }
                        item { QuickAnswers(posts.filter { it.id != pick.id && it.excerpt.length > 60 }.drop(3).take(6), nav) }
                        item { BannerAd("home:1", Modifier.padding(horizontal = 20.dp)) }
                        item { TopicSpotlight(posts, nav) }
                        item { HotDiscussions(nav) }
                        item { QuestionOfWeekCard(nav) }
                        item { NotifyCard() }
                        moreToRead(this, more, shown, nav, quizzes) { shown += 20 }
                    }
                }
            }
        }
        // "New guide" pill when something is published while Today is open
        AnimatedVisibility(fresh != null, Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 8.dp),
            enter = slideInVertically { -it } + fadeIn(), exit = slideOutVertically { -it } + fadeOut()) {
            val f = fresh
            Surface(onClick = { f?.let { nav.article(it.id) }; fresh = null }, shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary, shadowElevation = 6.dp) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.FiberNew, null, tint = Gold)
                    Spacer(Modifier.width(8.dp))
                    Text("Just published: ${f?.title ?: ""}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(260.dp))
                }
            }
        }
    }
    story?.let { s -> StoryViewer(s, posts, nav) { story = null } }
}

/** Today's pick changes every day (the same for everyone that day), chosen from the 12 newest guides. */
private fun dailyPick(posts: List<Post>): Post {
    val pool = posts.filter { isReadingTopic(it.label) }.take(12).ifEmpty { posts.take(12) }
    return pool[LocalDate.now().dayOfYear % pool.size]
}

private fun age(p: Post): Long = runCatching { ChronoUnit.DAYS.between(LocalDate.parse(p.published), LocalDate.now()) }.getOrDefault(99)
private fun ageText(published: String): String {
    val d = runCatching { ChronoUnit.DAYS.between(LocalDate.parse(published), LocalDate.now()) }.getOrDefault(999)
    return when { d == 0L -> "Today"; d == 1L -> "Yesterday"; d < 14 -> "$d days ago"; d < 60 -> "${d / 7} weeks ago"
        else -> runCatching { LocalDate.parse(published).format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())) }.getOrDefault(published) }
}
private fun ageText(p: Post) = when (val d = age(p)) { 0L -> "Today"; 1L -> "Yesterday"; in 2..13 -> "$d days ago"; else -> "${d / 7} weeks ago" }

@Composable
private fun HomeHeader(nav: Nav, posts: List<Post>) {
    val hour = LocalTime.now().hour
    val greeting = when (hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; in 17..21 -> "Good evening"; else -> "Hello, night owl" }
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern(android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEEMMMMd"), Locale.getDefault()))
    val newThisWeek = posts.count { age(it) <= 7 }
    val read = AppGraph.library.history.size
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    Column(Modifier.fillMaxWidth().background(skyBrush(hour, dark)).statusBarsPadding().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Logo(40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(date.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(greeting, style = MaterialTheme.typography.headlineMedium)
            }
            LevelChip(nav)
            IconButton(onClick = nav::search) { Icon(Icons.Rounded.Search, "Search") }
        }
        if (newThisWeek > 0 || read > 0) Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (newThisWeek > 0) Pill("$newThisWeek new this week", Gold.copy(alpha = .22f), MaterialTheme.colorScheme.onBackground)
            if (read > 0) Pill("You've read $read", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun Pill(text: String, bg: Color, fg: Color) =
    Text(text, style = MaterialTheme.typography.labelMedium, color = fg, fontWeight = FontWeight.Bold,
        modifier = Modifier.background(bg, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp))

// ---------- Stories ----------

@Composable
private fun Stories(posts: List<Post>, onOpen: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { NewPosts.prefs(context) }
    var seenVersion by remember { mutableIntStateOf(0) }
    val labels = listOf("new") + posts.map { it.label }.filter(::isReadingTopic).distinct().take(9)
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(labels, key = { it }) { label ->
            val newest = if (label == "new") posts.firstOrNull() else posts.firstOrNull { it.label == label }
            val unseen = remember(seenVersion, newest?.id) { newest != null && prefs.getString("story:$label", null) != newest.id }
            val t = topicFor(label)
            val ring = if (unseen) Brush.sweepGradient(listOf(Gold, Color(0xFFE2462C), NavyBright, Gold))
                else Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp).clickable {
                newest?.let { prefs.edit().putString("story:$label", it.id).apply() }; seenVersion++; onOpen(label)
            }) {
                Box(Modifier.size(68.dp).border(BorderStroke(3.dp, ring), CircleShape).padding(5.dp).clip(CircleShape)
                    .background(if (label == "new") Brush.linearGradient(listOf(Gold, Color(0xFFC0602A))) else Brush.linearGradient(listOf(t.from, t.to))),
                    contentAlignment = Alignment.Center) {
                    if (label == "new") Icon(Icons.Rounded.FiberNew, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    else Icon(t.icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(if (label == "new") "Latest" else t.title.substringBefore(" &").substringBefore(" and"), style = MaterialTheme.typography.labelMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            }
        }
    }
}

/** Full-screen stories: one quick answer per page, moving on by itself; tap the right side for next, left for back. */
@Composable
private fun StoryViewer(label: String, posts: List<Post>, nav: Nav, onClose: () -> Unit) {
    val items = (if (label == "new") posts else posts.filter { it.label == label }).take(8)
    if (items.isEmpty()) { onClose(); return }
    var index by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(index) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(8000, easing = LinearEasing))
        if (index < items.lastIndex) index++ else onClose()
    }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().background(NavyDeep).pointerInput(items.size) {
            detectTapGestures { o -> if (o.x < size.width / 3f) { if (index > 0) index-- } else if (index < items.lastIndex) index++ else onClose() }
        }) {
            AnimatedContent(index, transitionSpec = {
                if (targetState > initialState) (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                else (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it / 3 } + fadeOut())
            }, label = "story") { i ->
                val p = items[i]
                Box(Modifier.fillMaxSize()) {
                    // A soft, blurred copy of the cover fills the background; the cover itself is shown whole, never cropped.
                    Cover(p.image, p.label, Modifier.fillMaxWidth().fillMaxHeight(.6f).blur(28.dp).graphicsLayer { alpha = .55f }, 72.dp, corner = 0.dp)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to NavyDeep.copy(alpha = .35f), .45f to NavyDeep.copy(alpha = .75f), .62f to NavyDeep, 1f to NavyDeep)))
                    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(start = 20.dp, end = 20.dp, top = 76.dp, bottom = 24.dp)) {
                        Cover(p.image, p.label, Modifier.fillMaxWidth().aspectRatio(COVER_RATIO).shadow(16.dp, RoundedCornerShape(22.dp)), 72.dp, corner = 22.dp)
                        Spacer(Modifier.height(24.dp))
                        Kicker(topicFor(p.label).title, Gold)
                        Spacer(Modifier.height(8.dp))
                        Text(p.title, style = MaterialTheme.typography.headlineMedium, color = Color.White, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(12.dp))
                        Text(p.excerpt, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = .88f), maxLines = 6, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.weight(1f).heightIn(min = 16.dp))
                        Button(onClick = { onClose(); nav.article(p.id) }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep),
                            shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Text("Read the full guide", fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
                        }
                    }
                }
            }
            Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items.indices.forEach { i ->
                        val f = when { i < index -> 1f; i == index -> progress.value; else -> 0f }
                        Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = .3f))) {
                            Box(Modifier.fillMaxWidth(f).fillMaxHeight().background(Color.White))
                        }
                    }
                }
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Logo(28.dp); Spacer(Modifier.width(8.dp))
                    Text(if (label == "new") "Latest guides" else topicFor(label).title, color = Color.White, style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Close", tint = Color.White) }
                }
            }
        }
    }
}

// ---------- Today's pick ----------

@Composable
private fun TodaysPick(p: Post, nav: Nav) {
    val shown = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(shown, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 5 }) {
        Surface(onClick = { nav.article(p.id) }, shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column {
                Box {
                    Cover(p.image, p.label, Modifier.fillMaxWidth().aspectRatio(COVER_RATIO), 56.dp, corner = 0.dp, motion = true)
                    Row(Modifier.padding(14.dp).background(NavyDeep.copy(alpha = .78f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(Gold)); Spacer(Modifier.width(6.dp))
                        Text("TODAY'S PICK", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Column(Modifier.padding(18.dp)) {
                    Kicker(topicFor(p.label).title)
                    Spacer(Modifier.height(6.dp))
                    Text(p.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(p.excerpt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${p.minutes} min read", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("Read now", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueReading(nav: Nav) {
    Column(Modifier.padding(top = 8.dp)) {
        Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Continue reading") }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(AppGraph.library.history.take(8), key = { it.id }) { r ->
                Column(Modifier.width(200.dp).clip(RoundedCornerShape(20.dp)).clickable { nav.article(r.id) }) {
                    Cover(r.image, r.label, Modifier.fillMaxWidth().height(112.dp), 28.dp, alignment = Alignment.CenterEnd)
                    Spacer(Modifier.height(8.dp))
                    Text(r.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

// ---------- Just published ----------

@Composable
private fun JustPublished(posts: List<Post>, nav: Nav) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(.35f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "dot")
    Column(Modifier.padding(top = 8.dp)) {
        Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).graphicsLayer { alpha = pulse }.clip(CircleShape).background(Color(0xFFE2462C)))
            Spacer(Modifier.width(8.dp))
            Text("Just published", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(posts, key = { it.id }) { p ->
                Surface(onClick = { nav.article(p.id) }, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .6f)), modifier = Modifier.width(250.dp)) {
                    Column {
                        Box {
                            Cover(p.image, p.label, Modifier.fillMaxWidth().aspectRatio(COVER_RATIO), 40.dp, corner = 0.dp)
                            if (age(p) <= 3) Text("NEW", style = MaterialTheme.typography.labelSmall, color = NavyDeep, fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(10.dp).background(Gold, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Column(Modifier.padding(14.dp)) {
                            Text("${ageText(p)} · ${topicFor(p.label).title}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(4.dp))
                            Text(p.title, style = MaterialTheme.typography.titleMedium, maxLines = 3, minLines = 3, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

// ---------- Money in 10 seconds ----------

@Composable
private fun MoneyIn10Seconds(nav: Nav) {
    val context = LocalContext.current
    val cur = remember { Money.get(context) }
    var monthly by rememberSaveable { mutableFloatStateOf(300f) }
    val m = (monthly / 50f).toInt() * 50.0
    val years = listOf(10, 20, 30)
    val values = years.map { Calc.growth(0.0, m, 7.0, it, 0.0).value }
    val max = values.max().coerceAtLeast(1.0)
    Surface(shape = RoundedCornerShape(28.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 26.dp)) {
        Column(Modifier.background(Brush.linearGradient(listOf(Navy, NavyDeep))).padding(22.dp)) {
            Text("MONEY IN 10 SECONDS", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text("Invest ${Money.fmt(cur, m)} a month", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text("Drag to see what it could grow to at 7% a year.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .75f))
            Slider(monthly, { monthly = it }, valueRange = 50f..2000f, modifier = Modifier.padding(top = 6.dp),
                colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold, inactiveTrackColor = Color.White.copy(alpha = .25f)))
            Row(Modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Bottom) {
                years.forEachIndexed { i, y ->
                    val h by animateFloatAsState((values[i] / max).toFloat(), tween(450), label = "bar")
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(compact(cur, values[i]), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        Spacer(Modifier.height(6.dp))
                        Box(Modifier.fillMaxWidth().height((100 * h).coerceAtLeast(6f).dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(Brush.verticalGradient(listOf(Gold, GoldLight.copy(alpha = .55f)))))
                        Spacer(Modifier.height(6.dp))
                        Text("$y years", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = .8f))
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = { nav.tool("growth") }, shape = RoundedCornerShape(50), border = BorderStroke(1.dp, Color.White.copy(alpha = .5f)),
                modifier = Modifier.fillMaxWidth()) { Text("Try your own numbers", color = Color.White) }
        }
    }
}

private fun compact(code: String, v: Double): String {
    val sym = Money.fmt(code, 0.0).replace("0", "").trim()
    return when {
        v >= 1e9 -> "$sym%.1fB".format(v / 1e9); v >= 1e6 -> "$sym%.2fM".format(v / 1e6); v >= 1e4 -> "$sym%.0fk".format(v / 1e3)
        else -> Money.fmt(code, v)
    }
}

// ---------- 30-second answers (flip cards) ----------

@Composable
private fun QuickAnswers(posts: List<Post>, nav: Nav) {
    if (posts.isEmpty()) return
    Column(Modifier.padding(top = 26.dp)) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("30-second answers", style = MaterialTheme.typography.headlineSmall)
            Text("Tap a card to flip it.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        val pager = rememberPagerState { posts.size }
        HorizontalPager(pager, contentPadding = PaddingValues(horizontal = 32.dp), pageSpacing = 14.dp) { i ->
            val off = ((pager.currentPage - i) + pager.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
            FlipCard(posts[i], nav, Modifier.graphicsLayer { scaleX = 1f - off * .06f; scaleY = 1f - off * .06f })
        }
    }
}

@Composable
private fun FlipCard(p: Post, nav: Nav, modifier: Modifier) {
    var flipped by rememberSaveable(p.id) { mutableStateOf(false) }
    val rot by animateFloatAsState(if (flipped) 180f else 0f, tween(550), label = "flip")
    val density = LocalDensity.current.density
    val t = topicFor(p.label)
    Box(modifier.fillMaxWidth().height(270.dp).graphicsLayer { rotationY = rot; cameraDistance = 14 * density }
        .clip(RoundedCornerShape(26.dp)).clickable { flipped = !flipped }) {
        if (rot <= 90f) Column(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(t.from, t.to))).padding(22.dp)) {
            Text("Q", style = MaterialTheme.typography.displaySmall, color = Color.White.copy(alpha = .35f))
            Spacer(Modifier.weight(1f))
            Text(p.title.substringBefore(":").substringBefore(" ("), style = MaterialTheme.typography.headlineSmall, color = Color.White,
                maxLines = 4, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.TouchApp, null, tint = Color.White.copy(alpha = .8f), modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                Text("Tap for the answer", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = .85f))
            }
        } else Column(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }.background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(26.dp)).padding(22.dp)) {
            Text("THE SHORT ANSWER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Text(p.excerpt, style = MaterialTheme.typography.bodyMedium, maxLines = 7, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            TextButton(onClick = { nav.article(p.id) }, contentPadding = PaddingValues(0.dp)) {
                Text("Read the guide"); Spacer(Modifier.width(4.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp))
            }
        }
    }
}

// ---------- Tools and topics ----------

@Composable
private fun TopicSpotlight(posts: List<Post>, nav: Nav) {
    val labels = Topics.map { it.label }
    var selected by rememberSaveable { mutableStateOf(posts.map { it.label }.firstOrNull { it in labels } ?: labels.first()) }
    Column(Modifier.padding(top = 10.dp)) {
        Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Explore by topic", "All topics") { nav.tab("explore") } }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Topics) { t ->
                FilterChip(selected == t.label, { selected = t.label }, label = { Text(t.title) }, shape = RoundedCornerShape(50),
                    leadingIcon = { Icon(t.icon, null, Modifier.size(18.dp), tint = t.to) })
            }
        }
        AnimatedContent(selected, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) }, label = "topic") { label ->
            val local = posts.filter { it.label == label }.take(3)
            val shown by produceState(local, label) {
                if (local.size < 3) value = runCatching { AppGraph.repo.topic(label, 1, 8).filterNot { it.isToolPage }.take(3) }.getOrDefault(local)
            }
            Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                shown.forEach { p -> PostRow(p) { nav.article(p.id) } }
                TextButton(onClick = { nav.topic(label) }, modifier = Modifier.padding(start = 4.dp)) {
                    Text("More in ${topicFor(label).title}"); Spacer(Modifier.width(4.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp))
                }
            }
        }
    }
}

// ---------- Notifications, end of page, empty states ----------

@Composable
private fun NotifyCard() {
    if (Build.VERSION.SDK_INT < 33) return
    val context = LocalContext.current
    val prefs = remember { NewPosts.prefs(context) }
    var hidden by remember { mutableStateOf(NewPosts.canNotify(context) || prefs.getBoolean("notifyCardDismissed", false)) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) NewPosts.setEnabled(context, true)
        prefs.edit().putBoolean("notifyCardDismissed", true).apply(); hidden = true
    }
    if (hidden) return
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Never miss a new guide", style = MaterialTheme.typography.titleMedium)
                Text("Get a notification when one is published.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.padding(top = 8.dp)) {
                    Button(onClick = { ask.launch(Manifest.permission.POST_NOTIFICATIONS) }, shape = RoundedCornerShape(50)) { Text("Turn on") }
                    TextButton(onClick = { prefs.edit().putBoolean("notifyCardDismissed", true).apply(); hidden = true }) { Text("Not now") }
                }
            }
        }
    }
}

/**
 * Keeps the feed varied: newest first, but the next guide is never from the same topic as the one before it when
 * another topic is waiting nearby (so ten "Resize image to N KB" pages don't come in a row).
 */
internal fun mixTopics(list: List<Entry>): List<Entry> {
    val left = list.toMutableList()
    val out = ArrayList<Entry>(list.size)
    while (left.isNotEmpty()) {
        val last = out.lastOrNull()?.label
        val i = left.take(40).indexOfFirst { it.label != last }.let { if (it < 0) 0 else it }
        out += left.removeAt(i)
    }
    return out
}

private fun Post.toEntry() = Entry(id, title, path, url, labels, published, excerpt, image)

/** Where animated cards go in a feed of [n] items: every 8 to 12 items, at spots that change each day. */
internal fun motionSpots(n: Int, day: Long = LocalDate.now().toEpochDay()): Set<Int> {
    val r = kotlin.random.Random(day)
    val out = HashSet<Int>(); var i = 3 + r.nextInt(3)
    while (i < n) { if (i % 7 != 0) out += i else out += i + 1; i += 8 + r.nextInt(5) }
    return out
}

/** The never-ending part of Today: every guide, newest first, a big card every 7th, an ad after every 10th. */
private fun moreToRead(scope: LazyListScope, more: List<Entry>, shown: Int, nav: Nav, quizzes: List<Quiz>, loadMore: () -> Unit) = with(scope) {
    if (more.isEmpty()) return@with
    item(key = "more-header") {
        Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp), verticalAlignment = Alignment.Bottom) {
            Text("More to read", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            Text("${more.size} guides", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    val page = more.take(shown)
    val motion = motionSpots(page.size)
    val quizAt = motion.map { it + 5 }.filter { it < page.size && it !in motion }
    page.forEachIndexed { i, e ->
        val qi = quizAt.indexOf(i)
        if (qi >= 0 && qi < quizzes.size) item(key = "quiz-${quizzes[qi].id}") { QuizCard(quizzes[qi], nav, Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) }
        item(key = "more-${e.id}") {
            if (i in motion) MotionCard(e, "IN 30 SECONDS", nav, Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
            else if (i % 7 == 0) BigEntryCard(e, nav)
            else Box(Modifier.padding(horizontal = 16.dp)) { PostRow(e.ref, { nav.article(e.id) }) }
        }
        if (i % 10 == 9) item(key = "more-ad-$i") { BannerAd("home:feed:${i / 10}", Modifier.padding(horizontal = 20.dp)) }
    }
    if (shown < more.size) item(key = "more-loader-$shown") {
        LaunchedEffect(shown) { delay(120); loadMore() }
        Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp) }
    }
}

@Composable
private fun BigEntryCard(e: Entry, nav: Nav) {
    Surface(onClick = { nav.article(e.id) }, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Column {
            Cover(e.image, e.label, Modifier.fillMaxWidth().aspectRatio(COVER_RATIO), 48.dp, corner = 0.dp)
            Column(Modifier.padding(16.dp)) {
                Text("${intentOf(e.title).label} · ${topicFor(e.label).title.uppercase()} · ${ageText(e.published)}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(e.title, style = MaterialTheme.typography.titleLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
                if (e.quick.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(e.quick, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
                        overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun Offline(nav: Nav, retry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
        EmptyState(Icons.Rounded.CloudOff, "You're offline", "Connect to the internet to see today's guides. Your saved guides and the app's own tools still work.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = retry) { Text("Try again") }
            OutlinedButton(onClick = { nav.saved() }) { Text("Saved guides") }
        }
    }
}

@Composable
private fun HomeSkeleton() = Column(Modifier.padding(horizontal = 20.dp)) {
    Row(Modifier.padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { repeat(5) { Shimmer(Modifier.size(64.dp).clip(CircleShape)) } }
    Shimmer(Modifier.fillMaxWidth().height(330.dp).clip(RoundedCornerShape(28.dp)))
    Spacer(Modifier.height(20.dp))
    Shimmer(Modifier.fillMaxWidth(.6f).height(26.dp))
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { repeat(2) { Shimmer(Modifier.width(240.dp).height(200.dp).clip(RoundedCornerShape(22.dp))) } }
}

/** The Plainly mark: navy tile, white P, gold needle dot. */
@Composable
fun Logo(size: Dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(size * .3f)).background(Brush.linearGradient(listOf(NavyBright, Navy, NavyDeep))),
        contentAlignment = Alignment.Center) {
        Text("P", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Box(Modifier.align(Alignment.TopEnd).padding(size * .16f).size(size * .16f).clip(CircleShape).background(Gold))
    }
}

@Composable
fun rememberPaging(): androidx.compose.runtime.MutableIntState = remember { mutableIntStateOf(1) }
