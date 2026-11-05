package com.randomthoughtsever.plainly.ui

import com.randomthoughtsever.plainly.data.Entry
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch
import com.randomthoughtsever.plainly.tools.Catalog
import com.randomthoughtsever.plainly.data.SearchRank
import androidx.compose.material3.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import com.randomthoughtsever.plainly.data.NewPosts
import com.randomthoughtsever.plainly.ads.Ads
import com.randomthoughtsever.plainly.ads.BannerAd
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.BuildConfig
import com.randomthoughtsever.plainly.data.Blog
import com.randomthoughtsever.plainly.data.Post
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicScreen(label: String, nav: Nav) {
    val t = topicFor(label)
    val repo = AppGraph.repo
    var posts by remember { mutableStateOf<List<Post>?>(null) }
    var next by remember { mutableIntStateOf(1) }
    var end by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    suspend fun more() {
        if (busy || end) return
        busy = true
        runCatching { repo.topic(label, next) }.onSuccess { page ->
            posts = ((posts ?: emptyList()) + page).distinctBy { it.id }; next += page.size; end = page.size < 20
        }.onFailure { if (posts == null) posts = emptyList(); end = true }
        busy = false
    }
    LaunchedEffect(label) { more() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(t.from, t.to)))) {
                Icon(t.icon, null, tint = Color.White.copy(alpha = .12f), modifier = Modifier.size(180.dp).align(Alignment.BottomEnd))
                Column(Modifier.statusBarsPadding().padding(start = 8.dp, end = 20.dp, bottom = 26.dp)) {
                    IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White) }
                    Column(Modifier.padding(start = 12.dp)) {
                        Icon(t.icon, null, tint = Color.White, modifier = Modifier.size(34.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(t.title, style = MaterialTheme.typography.displaySmall, color = Color.White)
                        Text(t.blurb, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = .85f))
                    }
                }
            }
        }
        val list = posts
        if (list == null) items(6) { Box(Modifier.padding(horizontal = 16.dp)) { RowSkeleton() } }
        else if (list.isEmpty()) item { EmptyState(Icons.Rounded.SearchOff, "Nothing here yet", "Pull down on Today to refresh, or check your connection.") }
        else {
            itemsIndexed(list, key = { _, p -> p.id }) { i, p ->
                Box(Modifier.padding(horizontal = 16.dp)) { PostRow(p) { nav.article(p.id) } }
                if (i == 5) BannerAd("topic:$label:1", Modifier.padding(horizontal = 20.dp))
            }
            if (!end) item {
                LaunchedEffect(list.size) { more() }
                Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(26.dp), strokeWidth = 3.dp) }
            }
        }
    }
}

/**
 * Search that answers while you type: results come from the index of every guide on the phone (no network wait, partial
 * words count), with the matching letters highlighted. Blogger's full-text search runs in the background for deeper
 * matches and adds them underneath. Tools that match are shown first.
 */
@Composable
fun SearchScreen(nav: Nav, initial: String = "") {
    val context = LocalContext.current
    val prefs = remember { NewPosts.prefs(context) }
    var query by rememberSaveable { mutableStateOf(initial) }
    val index = rememberIndex()
    val q = query.trim()
    val instant = remember(q, index) { if (q.isEmpty() || index == null) emptyList() else SearchRank.instant(q, index) }
    var deep by remember { mutableStateOf<List<Post>>(emptyList()) }
    var deepLoading by remember { mutableStateOf(false) }
    var recent by remember { mutableStateOf(prefs.getString("recentSearches", "").orEmpty().split('\n').filter { it.isNotBlank() }) }
    fun saveRecent(term: String) {
        val t = term.trim(); if (t.length < 2) return
        recent = (listOf(t) + recent.filterNot { it.equals(t, true) }).take(8)
        prefs.edit().putString("recentSearches", recent.joinToString("\n")).apply()
    }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(q) {
        deep = emptyList()
        if (q.length < 3) return@LaunchedEffect
        delay(450); deepLoading = true
        deep = runCatching { AppGraph.repo.search(q) }.getOrDefault(emptyList()); deepLoading = false
    }
    val tokens = SearchRank.typed(q)
    val tools = remember(q) {
        if (tokens.isEmpty()) emptyList() else Catalog.all().filter { t ->
            val words = "${t.title} ${t.blurb}".lowercase().split(Regex("[^\\p{L}\\p{N}]+"))
            tokens.all { tok -> words.any { it.startsWith(tok) } }
        }.take(4)
    }
    val extra = deep.filter { p -> instant.none { it.id == p.id } }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            OutlinedTextField(query, { query = it }, Modifier.weight(1f).focusRequester(focus), placeholder = { Text("Search guides and tools") },
                singleLine = true, shape = RoundedCornerShape(50), leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, "Clear") } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { saveRecent(q) }))
        }
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
            when {
                q.isEmpty() -> {
                    if (recent.isNotEmpty()) {
                        item { Text("Recent searches", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp)) }
                        items(recent) { r ->
                            ListItem(headlineContent = { Text(r) }, leadingContent = { Icon(Icons.Rounded.History, null) },
                                modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { query = r },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                        }
                    }
                    item { Text("Popular topics", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp)) }
                    items(Topics.take(8)) { t ->
                        ListItem(headlineContent = { Text(t.title) }, supportingContent = { Text(t.blurb) },
                            leadingContent = { Icon(t.icon, null, tint = t.to) },
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { nav.topic(t.label) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                    }
                }
                index == null -> items(5) { RowSkeleton() }
                else -> {
                    if (tools.isNotEmpty()) {
                        item { Text("Tools", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp)) }
                        items(tools, key = { it.key }) { t ->
                            ListItem(headlineContent = { Text(highlighted(t.title, q)) }, supportingContent = { Text(t.blurb) },
                                leadingContent = { Box(Modifier.size(40.dp).background(t.color, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                                    Icon(t.icon, null, tint = Color.White) } },
                                modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { saveRecent(q); t.open(nav) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                        }
                    }
                    if (instant.isNotEmpty()) {
                        item { Text(if (instant.size == 1) "1 guide" else "${instant.size} guides", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp)) }
                        items(instant, key = { it.id }) { e -> SearchRow(e, q) { saveRecent(q); nav.article(e.id) } }
                    }
                    if (extra.isNotEmpty()) {
                        item { Text("More guides that mention it", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp)) }
                        items(extra, key = { "d" + it.id }) { p -> PostRow(p) { saveRecent(q); nav.article(p.id) } }
                    }
                    if (deepLoading && instant.isEmpty()) items(3) { RowSkeleton() }
                    if (instant.isEmpty() && tools.isEmpty() && extra.isEmpty() && !deepLoading)
                        item { EmptyState(Icons.Rounded.SearchOff, "Nothing for \"$q\" yet", "Try fewer letters or another word, like \"credit\" or \"sleep\".") }
                }
            }
        }
    }
}

/** The typed letters in bold and colour wherever a word starts with them. */
@Composable
fun highlighted(text: String, query: String): AnnotatedString {
    val color = MaterialTheme.colorScheme.primary
    return remember(text, query, color) {
        buildAnnotatedString {
            append(text)
            SearchRank.matches(text, query).forEach { r -> addStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = color, background = color.copy(alpha = .12f)), r.first, r.last + 1) }
        }
    }
}

@Composable
private fun SearchRow(e: Entry, q: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Cover(e.image, e.label, Modifier.size(width = 96.dp, height = 72.dp), 24.dp, alignment = Alignment.CenterEnd)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Kicker("${com.randomthoughtsever.plainly.data.intentOf(e.title).label} · ${topicFor(e.label).title}")
            Text(highlighted(e.title, q), style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SavedScreen(nav: Nav, history: Boolean = false) {
    val lib = AppGraph.library
    var tab by rememberSaveable { mutableIntStateOf(if (history) 1 else 0) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.statusBarsPadding().padding(start = 4.dp, end = 8.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            Text("Your library", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = nav::settings) { Icon(Icons.Rounded.Settings, "Settings") }
        }
        TabRow(tab, containerColor = Color.Transparent, modifier = Modifier.padding(horizontal = 8.dp)) {
            Tab(tab == 0, { tab = 0 }, text = { Text("Saved (${lib.saved.size})") })
            Tab(tab == 1, { tab = 1 }, text = { Text("Recently read") })
        }
        val list = if (tab == 0) lib.saved else lib.history
        if (list.isEmpty()) {
            if (tab == 0) EmptyState(Icons.Rounded.BookmarkBorder, "Nothing saved yet", "Tap the bookmark on any article to keep it here. Saved articles open even without internet.")
            else EmptyState(Icons.Rounded.History, "No reading history", "Articles you open will show up here.")
        } else LazyColumn(contentPadding = PaddingValues(16.dp)) {
            if (tab == 1) item { TextButton(onClick = lib::clearHistory) { Text("Clear history") } }
            items(list, key = { it.id }) { r ->
                PostRow(r, { nav.article(r.id) }, trailing = if (tab == 0) {
                    { IconButton(onClick = { AppGraph.repo.cached(r.id)?.let(lib::toggleSave) ?: lib.savedPost(r.id)?.let(lib::toggleSave) }) {
                        Icon(Icons.Rounded.DeleteOutline, "Remove") } }
                } else null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: Nav) {
    val lib = AppGraph.library
    val context = LocalContext.current
    var notify by remember { mutableStateOf(NewPosts.enabled(context) && NewPosts.canNotify(context)) }
    val askNotify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> NewPosts.setEnabled(context, ok); notify = ok }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = { IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Appearance", style = MaterialTheme.typography.titleMedium) }
            item {
                val modes = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    modes.forEachIndexed { i, (k, l) ->
                        SegmentedButton(lib.theme == k, { lib.setThemeMode(k) }, SegmentedButtonDefaults.itemShape(i, modes.size)) { Text(l) }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)); Text("Reading text size: ${lib.textZoom}%", style = MaterialTheme.typography.titleMedium) }
            item { Slider(lib.textZoom.toFloat(), { lib.setZoom(it.toInt()) }, valueRange = 80f..150f, steps = 13) }
            item {
                ListItem(headlineContent = { Text("New guide notifications") }, supportingContent = { Text("A notification when a new guide is published") },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    trailingContent = {
                        Switch(notify, { on ->
                            if (on && !NewPosts.canNotify(context) && android.os.Build.VERSION.SDK_INT >= 33) askNotify.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            else { NewPosts.setEnabled(context, on); notify = on }
                        })
                    })
            }
            if (com.randomthoughtsever.plainly.data.Community.enabled) item {
                var confirm by remember { mutableStateOf(false) }
                var status by remember { mutableStateOf<String?>(null) }
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                if (confirm) androidx.compose.material3.AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Delete my community data?") },
                    text = { Text("This removes your posts, your comments, your leaderboard entry and your anonymous community ID. It can't be undone.") },
                    confirmButton = { TextButton(onClick = { confirm = false; status = "Deleting…"
                        scope.launch { status = runCatching { com.randomthoughtsever.plainly.data.Community.deleteMe() }.fold({ "Done. Your community data was deleted." }, { "Couldn't delete right now. Check your connection." }) } }) { Text("Delete") } },
                    dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } })
                ListItem(headlineContent = { Text("Delete my community data") }, supportingContent = { Text(status ?: "Your posts, comments and community ID") },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent), modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { confirm = true })
            }
            else item {
                var shown by remember { mutableStateOf(false) }
                ListItem(headlineContent = { Text("Your community posts") },
                    supportingContent = { Text(if (shown) "Shown again: everything you hid or blocked." else
                        "They're comments on the blog, under your Google account. To delete one, open it on the blog and tap the bin. Tap here to show what you hid or blocked again.") },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { com.randomthoughtsever.plainly.data.BlogTalk.unblockAll(); shown = true })
            }
            item {
                var remind by remember { mutableStateOf(NewPosts.prefs(context).getBoolean("reminders", true)) }
                ListItem(headlineContent = { Text("Challenge and Daily 3 reminders") }, supportingContent = { Text("One evening nudge, only when something's left to do") },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    trailingContent = { Switch(remind, { on -> remind = on; NewPosts.prefs(context).edit().putBoolean("reminders", on).apply()
                        if (on) com.randomthoughtsever.plainly.data.Reminders.schedule(context) }) })
            }
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            val links = listOf(
                "Rate Plainly" to "market://details?id=${BuildConfig.APPLICATION_ID}",
                "Privacy policy" to Blog.PRIVACY,
                "Visit the Blog Digest website" to Blog.BASE,
            )
            items(links) { (title, url) ->
                ListItem(headlineContent = { Text(title) }, trailingContent = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable {
                        if (url.startsWith("market:")) runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))) }
                            .onFailure { nav.web("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}") }
                        else nav.web(url)
                    })
            }
            if (Ads.privacyOptionsRequired) item {
                ListItem(headlineContent = { Text("Privacy choices") }, supportingContent = { Text("Change your ad consent settings") },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { (context as? android.app.Activity)?.let(Ads::showPrivacyOptions) })
            }
            item {
                ListItem(headlineContent = { Text("Share Plainly") }, colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable {
                        nav.share("Plainly: Money, Mind & Work", "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                    })
            }
            item {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Logo(44.dp); Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Plainly ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleMedium)
                        Text("No account needed. Guides from Blog Digest.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
