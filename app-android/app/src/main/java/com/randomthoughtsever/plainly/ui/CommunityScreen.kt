package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.Community
import com.randomthoughtsever.plainly.data.Profile
import kotlinx.coroutines.launch

private val TOPICS = listOf("Money", "Investing", "Mind", "Career", "Abroad", "Economy", "Ask anything")

fun ago(ms: Long): String {
    val s = (System.currentTimeMillis() - ms) / 1000
    return when { ms == 0L || s < 60 -> "now"; s < 3600 -> "${s / 60}m"; s < 86400 -> "${s / 3600}h"; s < 604800 -> "${s / 86400}d"; else -> "${s / 604800}w" }
}

@Composable
fun Avatar(emoji: String, size: Int = 40) = Box(Modifier.size(size.dp).clip(CircleShape)
    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, Gold.copy(alpha = .35f)))), contentAlignment = Alignment.Center) {
    Text(emoji, style = if (size >= 40) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall)
}

/** Community: what people are sharing and asking, and the streak leaderboard. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(nav: Nav) {
    val profile = AppGraph.profile
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var posts by remember { mutableStateOf<List<Community.Post>?>(null) }
    var liked by remember { mutableStateOf(setOf<String>()) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var composing by remember { mutableStateOf(false) }
    var identity by remember { mutableStateOf(false) }
    suspend fun load() {
        runCatching { Community.feed() }.onSuccess { p -> posts = p; error = null; liked = runCatching { Community.liked(p.map { it.id }) }.getOrDefault(emptySet()) }
            .onFailure { error = "Couldn't load the community. Check your connection." ; if (posts == null) posts = emptyList() }
    }
    LaunchedEffect(Unit) { load() }
    if (composing) ComposeDialog("", "") { created -> composing = false; if (created != null) posts = listOf(created) + posts.orEmpty() }
    val firstStepsVisible = firstSteps().any { !it.done }
    if (identity) IdentityDialog { identity = false }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.statusBarsPadding().padding(start = 20.dp, end = 12.dp, top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Community", style = MaterialTheme.typography.displaySmall)
                Text("Tips, questions and wins from Plainly readers", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(onClick = { identity = true }, shape = CircleShape, color = Color.Transparent) { Avatar(profile.avatar, 44) }
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            SegmentedButton(tab == 0, { tab = 0 }, SegmentedButtonDefaults.itemShape(0, 2)) { Text("Feed") }
            SegmentedButton(tab == 1, { tab = 1 }, SegmentedButtonDefaults.itemShape(1, 2)) { Text("Streak leaders") }
        }
        if (tab == 1) { Leaders(); return@Column }
        PullToRefreshBox(refreshing, { scope.launch { refreshing = true; load(); refreshing = false } }, Modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                if (firstStepsVisible) item { FirstStepsCard(nav) { composing = true } }
                item { QuestionOfWeekCard(nav) }
                item { ChallengesStrip(nav) }
                item {
                    Surface(onClick = { composing = true }, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(profile.avatar, 36); Spacer(Modifier.width(12.dp))
                            Text("Share a tip, a win or a question…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f))
                            Icon(Icons.Rounded.Edit, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                error?.let { item { EmptyCentered(it) } }
                val list = posts
                when {
                    list == null -> item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                    list.isEmpty() && error == null -> item { EmptyCentered("No posts yet. Be the first: share a money tip, a small win, or ask the community anything.") }
                    else -> itemsIndexed(list, key = { _, p -> p.id }) { i, p ->
                        PostCard(p, p.id in liked, nav, onLike = { on ->
                            liked = if (on) liked + p.id else liked - p.id
                            posts = posts?.map { if (it.id == p.id) it.copy(likes = it.likes + if (on) 1 else -1) else it }
                            scope.launch { runCatching { Community.like(p.id, on) } }
                        }, onRemoved = { posts = posts?.filterNot { it.id == p.id || it.uid == p.uid && it.uid != Community.uid } })
                        if (i % 8 == 5) com.randomthoughtsever.plainly.ads.BannerAd("community:${i / 8}", Modifier.padding(horizontal = 20.dp))
                    }
                }
            }
        }
    }
}

/** One post: who, when, topic, text, its guide, likes and comments, and report/block/delete. */
@Composable
fun PostCard(p: Community.Post, liked: Boolean, nav: Nav, onLike: (Boolean) -> Unit, onRemoved: () -> Unit, inThread: Boolean = false) {
    val scope = rememberCoroutineScope()
    var menu by remember { mutableStateOf(false) }
    var reporting by remember { mutableStateOf(false) }
    val mine = p.uid == Community.uid
    val pop by animateFloatAsState(if (liked) 1.15f else 1f, spring(dampingRatio = .35f, stiffness = 600f), label = "like")
    if (reporting) ReportDialog { reason -> reporting = false; if (reason != null) { scope.launch { runCatching { Community.report("posts/${p.id}", reason) } }; onRemoved() } }
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).then(if (inThread) Modifier else Modifier.clickable { nav.thread(p.id) })) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(p.avatar); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.name + if (mine) " (you)" else "", style = MaterialTheme.typography.titleSmall)
                    Text("${ago(p.createdAt)} · ${p.topic}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, "More") }
                    DropdownMenu(menu, { menu = false }) {
                        if (mine) DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; scope.launch { runCatching { Community.deletePost(p.id) } }; onRemoved() })
                        else {
                            DropdownMenuItem(text = { Text("Report") }, onClick = { menu = false; reporting = true })
                            DropdownMenuItem(text = { Text("Block ${p.name}") }, onClick = { menu = false; Community.block(p.uid); onRemoved() })
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(p.text, style = MaterialTheme.typography.bodyLarge, maxLines = if (inThread) Int.MAX_VALUE else 8, overflow = TextOverflow.Ellipsis)
            if (p.guide.isNotBlank()) {
                val e = remember(p.guide) { AppGraph.repo.preview(p.guide) }
                Surface(onClick = { nav.article(p.guide) }, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(6.dp))
                        Text(e?.title ?: "Open the guide", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onLike(!liked) }) {
                    Icon(if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Like", Modifier.scale(pop),
                        tint = if (liked) Color(0xFFE2462C) else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${p.likes.coerceAtLeast(0)}", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(14.dp))
                IconButton(onClick = { nav.thread(p.id) }) { Icon(Icons.AutoMirrored.Rounded.Chat, "Comments", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text("${p.comments}", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** A post and its comments, with a box to reply. */
@Composable
fun ThreadScreen(postId: String, nav: Nav) {
    val profile = AppGraph.profile
    val scope = rememberCoroutineScope()
    var post by remember { mutableStateOf<Community.Post?>(null) }
    var comments by remember { mutableStateOf<List<Community.Comment>>(emptyList()) }
    var liked by remember { mutableStateOf(false) }
    var reply by rememberSaveable { mutableStateOf("") }
    var problem by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }
    var rules by remember { mutableStateOf(false) }
    suspend fun load() {
        post = runCatching { Community.getPost(postId) }.getOrNull()
        comments = runCatching { Community.comments(postId) }.getOrDefault(emptyList())
        liked = runCatching { Community.liked(listOf(postId)).isNotEmpty() }.getOrDefault(false)
    }
    LaunchedEffect(postId) { load() }
    if (rules) RulesDialog { ok -> rules = false; if (ok) Community.acceptedRules = true }
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            Text("Conversation", style = MaterialTheme.typography.titleLarge)
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 12.dp)) {
            post?.let { p -> item { PostCard(p, liked, nav, onLike = { on -> liked = on; post = p.copy(likes = p.likes + if (on) 1 else -1)
                scope.launch { runCatching { Community.like(p.id, on) } } }, onRemoved = { nav.back() }, inThread = true) } }
            if (post == null) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            items(comments, key = { it.id }) { c ->
                Row(Modifier.padding(start = 36.dp, end = 20.dp, top = 8.dp)) {
                    Avatar(c.avatar, 30); Spacer(Modifier.width(10.dp))
                    Column(Modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .7f)).padding(12.dp).weight(1f)) {
                        Text("${c.name} · ${ago(c.createdAt)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(c.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        problem?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 20.dp)) }
        Row(Modifier.navigationBarsPadding().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(profile.avatar, 34); Spacer(Modifier.width(8.dp))
            OutlinedTextField(reply, { reply = it.take(500) }, Modifier.weight(1f), placeholder = { Text("Add a kind, helpful reply") }, shape = RoundedCornerShape(24.dp), maxLines = 4)
            IconButton(enabled = !sending && reply.isNotBlank(), onClick = {
                if (!Community.acceptedRules) { rules = true; return@IconButton }
                problem = Community.problem(reply); if (problem != null) return@IconButton
                sending = true
                scope.launch {
                    runCatching { Community.comment(postId, profile.name, profile.avatar, reply) }
                        .onSuccess { reply = ""; load() }.onFailure { problem = it.message?.takeIf { m -> !m.startsWith("Community request") } ?: "Couldn't send. Try again." }
                    sending = false
                }
            }) { Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = MaterialTheme.colorScheme.primary) }
        }
    }
}

/** Write a post: topic, text, an optional guide it's about. Shows the community rules the first time. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComposeDialog(guide: String, preset: String, done: (Community.Post?) -> Unit) {
    val profile = AppGraph.profile
    val scope = rememberCoroutineScope()
    var text by rememberSaveable { mutableStateOf(preset) }
    var topic by rememberSaveable { mutableStateOf(if (guide.isNotBlank()) "Ask anything" else "Money") }
    var problem by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }
    var rules by remember { mutableStateOf(!Community.acceptedRules) }
    if (rules) { RulesDialog { ok -> rules = false; if (ok) Community.acceptedRules = true else done(null) }; return }
    AlertDialog(onDismissRequest = { done(null) }, title = { Text(if (guide.isNotBlank()) "Join the discussion" else "New post") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) { Avatar(profile.avatar, 32); Spacer(Modifier.width(8.dp)); Text(profile.name, style = MaterialTheme.typography.titleSmall) }
            Spacer(Modifier.height(10.dp))
            if (guide.isBlank()) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TOPICS.forEach { t -> FilterChip(topic == t, { topic = t }, label = { Text(t) }) }
            }
            if (guide.isBlank() && text.isBlank()) {
                Text("Ideas to start", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                helloPrompts().forEach { idea ->
                    Text(idea, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant).clickable { text = idea }.padding(10.dp))
                }
                Spacer(Modifier.height(6.dp))
            }
            OutlinedTextField(text, { text = it.take(500); problem = null }, Modifier.fillMaxWidth().height(160.dp),
                placeholder = { Text("A tip that worked for you, a small win, or a question for everyone") }, shape = RoundedCornerShape(16.dp))
            Text("${text.length}/500", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.End))
            problem?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = {
        Button(enabled = !sending, onClick = {
            problem = Community.problem(text); if (problem != null) return@Button
            sending = true
            scope.launch {
                runCatching { Community.createPost(profile.name, profile.avatar, text, topic, guide) }
                    .onSuccess { done(it) }.onFailure { problem = it.message?.takeIf { m -> !m.startsWith("Community request") } ?: "Couldn't post. Try again."; sending = false }
            }
        }, shape = RoundedCornerShape(50)) { Text(if (sending) "Posting…" else "Post") }
    }, dismissButton = { TextButton(onClick = { done(null) }) { Text("Cancel") } })
}

@Composable
fun RulesDialog(done: (Boolean) -> Unit) = AlertDialog(onDismissRequest = { done(false) }, title = { Text("Community rules") }, text = {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("Be kind. No insults, hate or harassment.", "Share experience, not financial advice. Nothing here is professional advice.",
            "No spam, selling or links (guides on the blog are fine).", "Keep yourself safe: no phone numbers, addresses or account details.",
            if (Community.enabled) "Report anything that breaks the rules. Posts that do are removed, and repeat offenders are blocked."
            else "Posts are comments on the blog: they're public, and the blog's owner removes any that break the rules.").forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
    }
}, confirmButton = { Button(onClick = { done(true) }, shape = RoundedCornerShape(50)) { Text("I agree") } },
    dismissButton = { TextButton(onClick = { done(false) }) { Text("Not now") } })

@Composable
fun ReportDialog(done: (String?) -> Unit) {
    val reasons = listOf("Spam or selling", "Harassment or hate", "Dangerous or misleading advice", "Personal information", "Something else")
    AlertDialog(onDismissRequest = { done(null) }, title = { Text("Report this post") }, text = {
        Column { reasons.forEach { r -> Text(r, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { done(r) }.padding(12.dp)) } }
    }, confirmButton = {}, dismissButton = { TextButton(onClick = { done(null) }) { Text("Cancel") } })
}

/** Change your community name and emoji. */
@Composable
fun IdentityDialog(done: () -> Unit) {
    val profile = AppGraph.profile
    var name by remember { mutableStateOf(profile.name) }
    var avatar by remember { mutableStateOf(profile.avatar) }
    var board by remember { mutableStateOf(Community.onLeaderboard) }
    val bad = name.trim().length < 2 || Community.problem(name) != null
    AlertDialog(onDismissRequest = done, title = { Text("Your community profile") }, text = {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) { Avatar(avatar, 52); Spacer(Modifier.width(12.dp))
                OutlinedTextField(name, { name = it.take(24) }, singleLine = true, label = { Text("Name") }, isError = bad) }
            TextButton(onClick = { name = Profile.randomName() }) { Text("Random name") }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(AppGraph.progress.avatars()) { a ->
                    Surface(onClick = { avatar = a }, shape = CircleShape, color = if (a == avatar) MaterialTheme.colorScheme.primaryContainer else Color.Transparent) {
                        Text(a, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(6.dp))
                    }
                }
            }
            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Show me on the streak leaderboard", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(board, { board = it })
            }
            Text("No account, email or phone number. Your name and emoji are all others see.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }, confirmButton = { TextButton(enabled = !bad, onClick = { profile.setIdentity(name, avatar); Community.onLeaderboard = board; done() }) { Text("Save") } },
        dismissButton = { TextButton(onClick = done) { Text("Cancel") } })
}

@Composable
private fun Leaders() {
    val leaders by produceState<List<Community.Leader>?>(null) { value = runCatching { Community.leaders() }.getOrDefault(emptyList()) }
    val me = Community.uid
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Text("Days in a row people have opened Plainly. Your streak shows here if you've chosen to be on the board (tap your emoji).",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        }
        val l = leaders
        if (l == null) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else if (l.isEmpty()) item { EmptyCentered("No one on the board yet. Keep your streak going and you'll be first.") }
        else itemsIndexed(l, key = { _, x -> x.uid }) { i, x ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).clip(RoundedCornerShape(18.dp))
                .background(if (x.uid == me) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(when (i) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "${i + 1}" }, style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                Avatar(x.avatar, 36); Spacer(Modifier.width(10.dp))
                Text(x.name + if (x.uid == me) " (you)" else "", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("🔥 ${x.streak}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

/** At the end of an article: what readers say about it, and a button to join in. */
@Composable
fun ArticleDiscussion(guideId: String, nav: Nav) {
    if (!Community.enabled) { BlogArticleDiscussion(guideId, nav); return }
    var posts by remember { mutableStateOf<List<Community.Post>?>(null) }
    var composing by remember { mutableStateOf(false) }
    LaunchedEffect(guideId) { posts = runCatching { Community.discussion(guideId) }.getOrDefault(emptyList()) }
    if (composing) ComposeDialog(guideId, "") { created -> composing = false; if (created != null) posts = listOf(created) + posts.orEmpty() }
    Column(Modifier.padding(vertical = 8.dp)) {
        Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Groups, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp))
            Text("Discussion" + (posts?.let { " (${it.size})" } ?: ""), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { composing = true }) { Text("Join in") }
        }
        val p = posts
        if (p != null && p.isEmpty()) Text("No comments yet. What did you think, or what would you add?", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp))
        p?.take(3)?.forEach { post -> PostCard(post, false, nav, onLike = {}, onRemoved = { posts = posts?.filterNot { it.id == post.id } }) }
    }
}

/** After answering a quiz: what share of people chose this option. */
@Composable
fun PollShare(quizId: String, option: Int, color: Color) {
    if (!Community.enabled) return
    val counts by produceState<IntArray?>(null, quizId) { kotlinx.coroutines.delay(600); value = runCatching { Community.poll(quizId) }.getOrNull() }
    val c = counts ?: return
    val total = c.sum()
    if (total < 3) return
    Text(" ${Math.round(c[option] * 100f / total)}%", style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
}
