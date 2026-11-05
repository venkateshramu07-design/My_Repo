package com.randomthoughtsever.plainly.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import coil.compose.AsyncImage
import com.randomthoughtsever.plainly.data.BlogTalk
import com.randomthoughtsever.plainly.data.Community
import com.randomthoughtsever.plainly.data.SearchRank
import com.randomthoughtsever.plainly.data.quizOfTheDay
import kotlinx.coroutines.launch

// The community without Firebase: conversations are the blog's own comments (data/BlogTalk.kt).

/** The member's community page: first steps, how posting works, the latest conversations across every guide, and top voices. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogCommunityScreen(nav: Nav) {
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf<List<BlogTalk.Note>?>(null) }
    var error by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    var compose by remember { mutableStateOf<Pair<String, String>?>(null) }
    var tick by remember { mutableIntStateOf(0) }
    suspend fun load(force: Boolean) {
        runCatching { BlogTalk.recent(force) }.onSuccess { notes = it; error = false }.onFailure { error = true; if (notes == null) notes = emptyList() }
        tick++
    }
    LaunchedEffect(Unit) { load(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { scope.launch { load(true) } }
    compose?.let { (post, preset) -> TalkComposer(post, preset, nav) { compose = null; tick++ } }
    val board = BlogTalk.board

    Column(Modifier.fillMaxSize()) {
        MemberHeader(nav)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            SegmentedButton(tab == 0, { tab = 0 }, SegmentedButtonDefaults.itemShape(0, 2)) { Text("Conversations") }
            SegmentedButton(tab == 1, { tab = 1 }, SegmentedButtonDefaults.itemShape(1, 2)) { Text("Top voices") }
        }
        PullToRefreshBox(refreshing, { scope.launch { refreshing = true; load(true); refreshing = false } }, Modifier.fillMaxSize()) {
            if (tab == 1) { TopVoices(notes); return@PullToRefreshBox }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                item(key = "steps$tick") { FirstStepsCard(nav) { compose = board to helloPrompts().first() } }
                item {
                    Surface(onClick = { compose = board to "" }, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(AppGraph.profile.avatar, 36); Spacer(Modifier.width(12.dp))
                            Text(if (board.isNotBlank()) "Share a tip, a win or a question…" else "Start a conversation on a guide…",
                                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Icon(Icons.Rounded.Edit, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                item { HowPostingWorks() }
                item { QuestionOfWeekCard(nav) }
                item(key = "waiting$tick") { WaitingBanner(nav) { tick++ } }
                item { Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Latest conversations") } }
                val list = notes
                when {
                    list == null -> item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                    error && list.isEmpty() -> item { EmptyCentered("Couldn't load conversations. Check your connection and pull to refresh.") }
                    list.isEmpty() -> { item { QuietStart(nav) { compose = it to "" } }; item { ChallengesStrip(nav) }; item { TodaysQuiz(nav) } }
                    else -> itemsIndexed(list, key = { _, n -> n.id }) { i, n ->
                        NoteCard(n, nav, onReply = { compose = n.postId to "@${n.name} " }, onRemoved = { notes = notes?.filterNot { it.id == n.id }; scope.launch { load(true) } })
                        if (i == 2 || i == list.size - 1 && i < 2) ChallengesStrip(nav)
                        if (i == 6 || i == list.size - 1 && i < 6) TodaysQuiz(nav)
                        if (i % 8 == 5) com.randomthoughtsever.plainly.ads.BannerAd("community:${i / 8}", Modifier.padding(horizontal = 20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TodaysQuiz(nav: Nav) {
    Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text("TODAY'S QUIZ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            QuizBody(quizOfTheDay(AppGraph.profile.goals), nav)
        }
    }
}

/** Three steps, said once at the top, so posting through the blog never surprises anyone. */
@Composable
private fun HowPostingWorks() {
    var open by rememberSaveable { mutableStateOf(BlogTalk.pending().isEmpty() && !Community.posted) }
    Surface(onClick = { open = !open }, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💬", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(10.dp))
                Text("How posting works", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(if (open) "Hide" else "Show", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            if (open) {
                Spacer(Modifier.height(8.dp))
                listOf("Write your post or reply here in the app.",
                    "Tap Copy and continue: we copy your text and open the blog's comment box for that guide.",
                    "Paste it, sign in with your Google account if asked, and tap Publish.",
                    "Come back: your post shows up here, marked as yours.").forEachIndexed { i, t ->
                    Row(Modifier.padding(vertical = 4.dp)) {
                        Box(Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp)); Text(t, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text("Conversations are the blog's comments, so everyone sees the same thing on the website and in the app. On posts, Blogger shows the name of the Google account you use.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

/** Posts the reader sent to the blog that haven't appeared yet, with what to do about it. */
@Composable
private fun WaitingBanner(nav: Nav, changed: () -> Unit) {
    val waiting = BlogTalk.pending().filter { System.currentTimeMillis() - it.at < 3 * 86_400_000L }
    val w = waiting.lastOrNull() ?: return
    val title = AppGraph.repo.preview(w.postId)?.title ?: if (w.postId == BlogTalk.board) "the community board" else "a guide"
    Surface(shape = RoundedCornerShape(22.dp), color = Gold.copy(alpha = .16f), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.HourglassTop, null, tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(10.dp))
                Text("Waiting for your post on $title", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text("“${w.text.take(90)}${if (w.text.length > 90) "…" else ""}”", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
            Text("If you tapped Publish, it appears within a minute or two. If the blog checks comments first, it appears once it's approved.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { copy(nav.context, w.text); nav.web(BlogTalk.formUrl(w.postId)) }) { Text("Copy and open the box again") }
                TextButton(onClick = { BlogTalk.forget(w); changed() }) { Text("Forget it") }
            }
        }
    }
}

/** No conversations yet: suggest guides to start on. */
@Composable
private fun QuietStart(nav: Nav, start: (String) -> Unit) {
    val index = rememberIndex()
    val picks = remember(index) { index.orEmpty().filterNot { it.isToolPage }.take(3) }
    Column(Modifier.padding(horizontal = 20.dp)) {
        Text("It's quiet here. Be the first: pick a guide and say what you think, ask a question, or share what worked for you.",
            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        picks.forEach { e -> PostRow(e.ref, { start(e.id) }, trailing = { Icon(Icons.AutoMirrored.Rounded.Chat, "Start", tint = MaterialTheme.colorScheme.primary) }) }
    }
}

/** Someone's photo from their Google profile, or their initial. */
@Composable
fun Face(photo: String, name: String, size: Int = 40) {
    if (photo.isNotBlank()) AsyncImage(photo, null, Modifier.size(size.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentScale = ContentScale.Crop)
    else Box(Modifier.size(size.dp).clip(CircleShape).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, Gold.copy(alpha = .35f)))),
        contentAlignment = Alignment.Center) {
        Text(name.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "?", style = if (size >= 40) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold)
    }
}

/** One comment: who, when, which guide, the text, and reply / see the conversation / hide / block. */
@Composable
fun NoteCard(n: BlogTalk.Note, nav: Nav, onReply: () -> Unit, onRemoved: () -> Unit, showGuide: Boolean = true) {
    var menu by remember { mutableStateOf(false) }
    var reporting by remember { mutableStateOf(false) }
    var reported by remember { mutableStateOf(false) }
    val mine = BlogTalk.mine(n)
    if (reporting) ReportDialog { reason -> reporting = false
        if (reason != null) {
            // The report goes to the blog's owner through the blog's contact form; the post is hidden here right away.
            copy(nav.context, "Report: $reason\nComment by ${n.name}: ${n.url.ifBlank { n.postUrl }}\n\"${n.text.take(200)}\"")
            BlogTalk.hide(n); reported = true
        }
    }
    if (reported) AlertDialog(onDismissRequest = { reported = false; onRemoved() }, title = { Text("Thanks for reporting") },
        text = { Text("We've hidden it for you. To tell the blog's owner, who removes posts that break the rules, paste the copied report into the contact form that opens next.") },
        confirmButton = { Button(onClick = { reported = false; nav.web(com.randomthoughtsever.plainly.data.Blog.BASE + "/2021/06/contact-us.html"); onRemoved() }, shape = RoundedCornerShape(50)) {
            Text("Open the contact form") } },
        dismissButton = { TextButton(onClick = { reported = false; onRemoved() }) { Text("Just hide it") } })
    Surface(shape = RoundedCornerShape(24.dp), color = if (mine) MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f) else MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)
            .then(if (showGuide) Modifier.clickable { nav.talk(n.postId) } else Modifier)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Face(n.photo, n.name); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(n.name + if (mine) " (you)" else "", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(ago(n.published), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, "More") }
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem(text = { Text("See it on the blog") }, onClick = { menu = false; nav.web(n.url.ifBlank { n.postUrl }) })
                        if (!mine) {
                            DropdownMenuItem(text = { Text("Report") }, onClick = { menu = false; reporting = true })
                            DropdownMenuItem(text = { Text("Hide this") }, onClick = { menu = false; BlogTalk.hide(n); onRemoved() })
                            DropdownMenuItem(text = { Text("Block ${n.name}") }, onClick = { menu = false; BlogTalk.block(n); onRemoved() })
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(n.text, style = MaterialTheme.typography.bodyLarge, maxLines = if (showGuide) 8 else Int.MAX_VALUE, overflow = TextOverflow.Ellipsis)
            if (showGuide && n.postId != BlogTalk.board) {
                val e = remember(n.postId) { AppGraph.repo.preview(n.postId) }
                Surface(onClick = { nav.article(n.postId) }, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(6.dp))
                        Text(e?.title ?: "Open the guide", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onReply) { Icon(Icons.AutoMirrored.Rounded.Reply, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Reply") }
                if (showGuide) TextButton(onClick = { nav.talk(n.postId) }) {
                    Icon(Icons.AutoMirrored.Rounded.Chat, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Conversation")
                }
            }
        }
    }
}

/** The people who talk most, from the same comments everyone sees. */
@Composable
private fun TopVoices(notes: List<BlogTalk.Note>?) {
    val mine = BlogTalk.myUids()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Text("The most active readers in recent conversations. Every comment on a guide counts.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        }
        val v = notes?.let { BlogTalk.voices(it) }
        when {
            v == null -> item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            v.isEmpty() -> item { EmptyCentered("No one here yet. Comment on a guide and you'll be first.") }
            else -> itemsIndexed(v.take(30), key = { _, x -> x.uid }) { i, x ->
                val me = x.uid in mine
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).clip(RoundedCornerShape(18.dp))
                    .background(if (me) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(when (i) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "${i + 1}" }, style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                    Face(x.photo, x.name, 36); Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(x.name + if (me) " (you)" else "", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("on ${x.guides} guide${if (x.guides == 1) "" else "s"} · last ${ago(x.last)}", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("💬 ${x.notes}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/** A guide's whole conversation, oldest first, with a bar to add to it. */
@Composable
fun TalkScreen(postId: String, nav: Nav) {
    val scope = rememberCoroutineScope()
    var notes by remember { mutableStateOf<List<BlogTalk.Note>?>(null) }
    var compose by remember { mutableStateOf<String?>(null) }
    var tick by remember { mutableIntStateOf(0) }
    suspend fun load() { notes = runCatching { BlogTalk.on(postId) }.getOrDefault(notes ?: emptyList()); tick++ }
    LaunchedEffect(postId) { load() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { scope.launch { load() } }
    compose?.let { TalkComposer(postId, it, nav) { compose = null; tick++ } }
    val guide = remember(postId) { AppGraph.repo.preview(postId) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) {
                Text("Conversation", style = MaterialTheme.typography.titleLarge)
                Text(guide?.title ?: if (postId == BlogTalk.board) "Community board" else "", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (guide != null) TextButton(onClick = { nav.article(postId) }) { Text("Read") }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 12.dp)) {
            item(key = "w$tick") { BlogTalk.waitingOn(postId)?.let { WaitingBanner(nav) { tick++ } } }
            val list = notes
            when {
                list == null -> item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                list.isEmpty() -> item { EmptyCentered("No comments yet. Start the conversation: what did you think, or what would you add?") }
                else -> itemsIndexed(list, key = { _, n -> n.id }) { _, n ->
                    NoteCard(n, nav, onReply = { compose = "@${n.name} " }, onRemoved = { notes = notes?.filterNot { it.id == n.id } }, showGuide = false)
                }
            }
        }
        Surface(onClick = { compose = "" }, shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(12.dp)) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(AppGraph.profile.avatar, 32); Spacer(Modifier.width(10.dp))
                Text("Add to the conversation…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Icon(Icons.Rounded.Edit, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** Under an article: what readers said about it, and a button to add yours. */
@Composable
fun BlogArticleDiscussion(guideId: String, nav: Nav) {
    var notes by remember { mutableStateOf<List<BlogTalk.Note>?>(null) }
    var compose by remember { mutableStateOf(false) }
    LaunchedEffect(guideId) { notes = runCatching { BlogTalk.on(guideId) }.getOrDefault(emptyList()) }
    if (compose) TalkComposer(guideId, "", nav) { compose = false }
    Column(Modifier.padding(vertical = 8.dp)) {
        Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Groups, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp))
            Text("Discussion" + (notes?.takeIf { it.isNotEmpty() }?.let { " (${it.size})" } ?: ""), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { compose = true }) { Text("Join in") }
        }
        val list = notes
        if (list != null && list.isEmpty()) Text("No comments yet. What did you think, or what would you add?", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp))
        list?.takeLast(3)?.forEach { n -> NoteCard(n, nav, onReply = { compose = true }, onRemoved = { notes = notes?.filterNot { it.id == n.id } }, showGuide = false) }
        if ((list?.size ?: 0) > 3) TextButton(onClick = { nav.talk(guideId) }, modifier = Modifier.padding(horizontal = 16.dp)) { Text("See all ${list!!.size} comments") }
    }
}

/**
 * Write a post or reply, then hand it to the blog: the text is copied and Blogger's comment box for that guide opens.
 * With no guide (and no board), the reader first picks the guide their post is about.
 */
@Composable
fun TalkComposer(postId: String, preset: String, nav: Nav, done: () -> Unit) {
    var target by rememberSaveable { mutableStateOf(postId) }
    var text by rememberSaveable { mutableStateOf(preset) }
    var problem by remember { mutableStateOf<String?>(null) }
    var rules by remember { mutableStateOf(!Community.acceptedRules) }
    if (rules) { RulesDialog { ok -> rules = false; if (ok) Community.acceptedRules = true else done() }; return }
    if (target.isBlank()) { GuidePicker({ target = it }, done); return }
    val guide = remember(target) { AppGraph.repo.preview(target) }
    AlertDialog(onDismissRequest = done, title = { Text(if (preset.startsWith("@")) "Reply" else if (target == BlogTalk.board) "New post" else "Join the discussion") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            if (guide != null) Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Rounded.MenuBook, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(6.dp))
                Text(guide.title, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (postId.isBlank()) TextButton(onClick = { target = "" }) { Text("Change") }
            }
            if (text.isBlank() && target == BlogTalk.board) {
                Text("Ideas to start", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                helloPrompts().forEach { idea ->
                    Text(idea, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant).clickable { text = idea }.padding(10.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(text, { text = it.take(1000); problem = null }, Modifier.fillMaxWidth().heightIn(min = 140.dp),
                placeholder = { Text("What did you think? A tip that worked for you, or a question for everyone") }, shape = RoundedCornerShape(16.dp))
            problem?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text("Next: we copy this and open the blog's comment box. Paste it there and tap Publish (it asks you to sign in with Google the first time).",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        }
    }, confirmButton = {
        Button(enabled = text.isNotBlank(), onClick = {
            problem = Community.problem(text); if (problem != null) return@Button
            BlogTalk.expect(target, text.trim())
            copy(nav.context, text.trim())
            nav.web(BlogTalk.formUrl(target))
            done()
        }, shape = RoundedCornerShape(50)) { Text("Copy and continue") }
    }, dismissButton = { TextButton(onClick = done) { Text("Cancel") } })
}

/** Which guide is this about? Search as you type; the newest guides first. */
@Composable
private fun GuidePicker(pick: (String) -> Unit, cancel: () -> Unit) {
    val index = rememberIndex()
    var q by rememberSaveable { mutableStateOf("") }
    val guides = remember(index) { index.orEmpty().filterNot { it.isToolPage } }
    val shown = remember(guides, q) { if (q.isBlank()) guides.take(12) else SearchRank.instant(q, guides, 12) }
    AlertDialog(onDismissRequest = cancel, title = { Text("Which guide is it about?") }, text = {
        Column {
            Text("Conversations live under guides, so people who read it can join in.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth().padding(vertical = 8.dp), singleLine = true, placeholder = { Text("Search guides") },
                shape = RoundedCornerShape(16.dp))
            Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                if (index == null) Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                shown.forEach { e ->
                    Text(e.title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { pick(e.id) }.padding(vertical = 10.dp, horizontal = 6.dp))
                }
            }
        }
    }, confirmButton = {}, dismissButton = { TextButton(onClick = cancel) { Text("Cancel") } })
}

fun copy(context: Context, text: String) {
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(ClipData.newPlainText("Your post", text))
}

