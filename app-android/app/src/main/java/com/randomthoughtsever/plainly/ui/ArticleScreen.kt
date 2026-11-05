package com.randomthoughtsever.plainly.ui

import com.randomthoughtsever.plainly.ads.BannerAd
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.Blog
import com.randomthoughtsever.plainly.data.Post
import com.randomthoughtsever.plainly.tools.Catalog
import com.randomthoughtsever.plainly.tools.ToolEntry
import androidx.compose.material.icons.rounded.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ArticleScreen(id: String, nav: Nav) {
    val repo = AppGraph.repo
    val lib = AppGraph.library
    val scope = rememberCoroutineScope()
    var post by remember { mutableStateOf(repo.cached(id) ?: lib.savedPost(id)) }
    var failed by remember { mutableStateOf(false) }
    var related by remember { mutableStateOf<List<Post>>(emptyList()) }
    LaunchedEffect(id) {
        if (post == null) post = repo.post(id) ?: lib.savedPost(id)
        val p = post
        if (p == null) failed = true else {
            lib.opened(p)
            related = runCatching { repo.topic(p.label, 1, 8) }.getOrDefault(emptyList()).filter { it.id != p.id }.take(4)
            nav.maybeAskForReview()
        }
    }
    val p = post
    if (p == null) {
        Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
            if (failed) EmptyState(Icons.Rounded.CloudOff, "Couldn't open this article", "Check your connection and try again.")
            else CircularProgressIndicator()
            IconButton(onClick = nav::back, modifier = Modifier.align(Alignment.TopStart)) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
        }
        return
    }

    val scroll = rememberScrollState()
    val progress = if (scroll.maxValue > 0) scroll.value.toFloat() / scroll.maxValue else 0f
    var menu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun openLink(uri: Uri) = when {
        uri.scheme == "https" && uri.host == Blog.HOST -> nav.blogLink(uri)
        uri.scheme == "https" || uri.scheme == "http" -> { nav.web(uri.toString()); Unit }
        uri.scheme in setOf("mailto", "tel") -> { nav.external(uri); Unit }
        else -> Unit
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Box {
                Cover(p.image, p.label, Modifier.fillMaxWidth().statusBarsPadding().padding(top = 60.dp, start = 16.dp, end = 16.dp).aspectRatio(COVER_RATIO), 64.dp, corner = 24.dp)
            }
            Column(Modifier.padding(horizontal = 22.dp)) {
                Spacer(Modifier.height(20.dp))
                Kicker(topicFor(p.label).title)
                Spacer(Modifier.height(8.dp))
                Text(p.title, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Logo(26.dp); Spacer(Modifier.width(8.dp))
                    Text("${formatDate(p.published)} · ${p.minutes} min read", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val articleTool = remember(p.id) {
                    Catalog.forPath(p.path) ?: if (p.html.contains("bd-calc")) ToolEntry("a:${p.id}", "Interactive tool", "From this guide",
                        Icons.Rounded.Build, Color(0xFF1F6FB2), "", post = p.id, widget = "all") else null
                }
                articleTool?.let { tool ->
                    Spacer(Modifier.height(18.dp))
                    Surface(onClick = { tool.open(nav) }, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(tool.color, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(tool.icon, null, tint = Color.White)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Try it: ${tool.title}", style = MaterialTheme.typography.titleSmall)
                                Text(tool.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
            val blocks by produceState<List<Block>?>(null, p.id) { value = withContext(Dispatchers.Default) { Reader.parse(p.html) } }
            blocks?.let { b ->
                val slots = remember(b) { adSlots(b) }
                ArticleBlocks(b, lib.textZoom, slots, { i -> BannerAd("article:${p.id}:$i") }) { link -> openLink(Uri.parse(link)) }
            }
                ?: Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { repeat(6) { Shimmer(Modifier.fillMaxWidth().height(18.dp)) } }
            BannerAd("article:${p.id}:end", Modifier.padding(horizontal = 22.dp))
            if (related.isNotEmpty()) Column(Modifier.padding(horizontal = 16.dp)) {
                Box(Modifier.padding(horizontal = 4.dp)) { SectionTitle("More in ${topicFor(p.label).title}") }
                related.forEach { r -> PostRow(r) { nav.article(r.id) } }
            }
            Spacer(Modifier.height(40.dp))
            Spacer(Modifier.navigationBarsPadding())
        }

        // Floating top bar over the cover
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundButton { IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } }
            Spacer(Modifier.weight(1f))
            RoundButton {
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.FormatSize, "Text size") }
                    DropdownMenu(menu, { menu = false }) {
                        listOf(90 to "Small", 100 to "Default", 115 to "Large", 130 to "Extra large").forEach { (z, l) ->
                            DropdownMenuItem(text = { Text(if (lib.textZoom == z) "✓  $l" else "     $l") }, onClick = { lib.setZoom(z); menu = false })
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            RoundButton {
                IconButton(onClick = { lib.toggleSave(p) }) {
                    Icon(if (lib.isSaved(p.id)) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, "Save",
                        tint = if (lib.isSaved(p.id)) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(Modifier.width(8.dp))
            RoundButton { IconButton(onClick = { nav.share(p.title, p.url) }) { Icon(Icons.Rounded.Share, "Share") } }
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(3.dp).align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.secondary, trackColor = Color.Transparent)
    }
}

@Composable
private fun RoundButton(content: @Composable () -> Unit) =
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface.copy(alpha = .92f), shadowElevation = 3.dp) { content() }

private fun formatDate(iso: String): String = runCatching {
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern(android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMyyyy"), Locale.getDefault()))
}.getOrDefault(iso)

