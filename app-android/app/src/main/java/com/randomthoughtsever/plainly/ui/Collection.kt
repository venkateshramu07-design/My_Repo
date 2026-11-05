package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.randomthoughtsever.plainly.data.Progress

private fun rarityBrush(r: Int, from: Color, to: Color) = when (r) {
    2 -> Brush.linearGradient(listOf(Color(0xFFE2462C), Gold, Color(0xFF7B67D6)))
    1 -> Brush.linearGradient(listOf(Gold, Color(0xFFC0602A)))
    else -> Brush.linearGradient(listOf(from, to))
}

/** One collectible card: the topic's art, the guide's title and its rarity. */
@Composable
fun GuideCard(c: Progress.Card, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val t = topicFor(c.label)
    Surface(onClick = { onClick?.invoke() }, enabled = onClick != null, shape = RoundedCornerShape(18.dp), color = Color.Transparent,
        modifier = modifier.aspectRatio(.7f).border(BorderStroke(if (c.rarity > 0) 3.dp else 0.dp, rarityBrush(c.rarity, t.from, t.to)), RoundedCornerShape(18.dp))) {
        Column(Modifier.background(Brush.verticalGradient(listOf(t.from, t.to)))) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                TopicScene(c.label, Modifier.fillMaxSize(), background = false)
                Text(Progress.RARITY[c.rarity].uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold,
                    color = if (c.rarity > 0) NavyDeep else Color.White,
                    modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(50)).background(if (c.rarity > 0) Gold else Color.White.copy(alpha = .2f)).padding(horizontal = 8.dp, vertical = 2.dp))
            }
            Text(c.title, style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 3, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(10.dp))
        }
    }
}

/** The new card flips in when a guide is finished. */
@Composable
fun CardRevealDialog(c: Progress.Card, nav: Nav, done: () -> Unit) {
    val flip = remember { Animatable(180f) }
    LaunchedEffect(Unit) { Sounds.fanfare(); flip.animateTo(0f, tween(900)) }
    Dialog(onDismissRequest = done) {
        Box {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (c.rarity == 2) "LEGENDARY CARD!" else if (c.rarity == 1) "RARE CARD!" else "NEW CARD", color = Gold, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.width(220.dp).graphicsLayer { rotationY = flip.value; cameraDistance = 14 * density }) {
                    if (flip.value > 90f) Box(Modifier.fillMaxWidth().aspectRatio(.7f).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(NavyDeep, NavyBright))),
                        contentAlignment = Alignment.Center) { Text("P", style = MaterialTheme.typography.displayLarge, color = Gold) }
                    else GuideCard(c, Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(14.dp))
                Text(when (c.rarity) { 2 -> "Finished, quiz right and every quick check known."; 1 -> "Finished and tested yourself."; else -> "Finish the quick checks or quiz to upgrade it." },
                    color = Color.White, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = done, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) { Text("Nice!") }
                    OutlinedButton(onClick = { done(); nav.cards() }, shape = RoundedCornerShape(50)) { Text("My collection", color = Color.White) }
                }
            }
            Confetti(Modifier.matchParentSize())
        }
    }
}

/** Albums by topic: how many of each topic's guides you've collected, and the cards themselves. */
@Composable
fun CollectionScreen(nav: Nav) {
    val pr = AppGraph.progress
    pr.version
    val cards = pr.cards()
    val index = rememberIndex()
    val totals = remember(index) { index.orEmpty().filterNot { it.isToolPage }.groupingBy { it.label }.eachCount() }
    val albums = Topics.filter { (totals[it.label] ?: 0) > 0 || cards.any { c -> c.label == it.label } }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(bottom = 30.dp)) {
        item {
            Row(Modifier.statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Column(Modifier.weight(1f)) {
                    Text("My collection", style = MaterialTheme.typography.titleLarge)
                    Text("${cards.size} cards · ${cards.count { it.rarity == 1 }} rare · ${cards.count { it.rarity == 2 }} legendary", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text("Finish a guide to collect its card. Test yourself with its quick checks and quiz to make it Rare or Legendary.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp))
        }
        albums.forEach { t ->
            val mine = cards.filter { it.label == t.label }
            val total = (totals[t.label] ?: mine.size).coerceAtLeast(mine.size)
            item(key = t.label) {
                Column(Modifier.padding(top = 18.dp)) {
                    Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(t.from, t.to))), contentAlignment = Alignment.Center) {
                            Icon(t.icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(t.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text("${mine.size} / $total" + if (mine.size >= total && total > 0) " ✓" else "", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(progress = { if (total == 0) 0f else mine.size / total.toFloat() }, Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                        .height(6.dp).clip(CircleShape), color = t.to)
                    if (mine.isEmpty()) TextButton(onClick = { nav.topic(t.label) }, modifier = Modifier.padding(horizontal = 12.dp)) { Text("Start collecting: read a ${t.title} guide") }
                    else LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(mine, key = { it.postId }) { c -> GuideCard(c, Modifier.width(130.dp)) { nav.article(c.postId) } }
                    }
                }
            }
        }
    }
}

/** Lines saved from guides, newest first. Each can be shared as a picture or opened in its guide. */
@Composable
fun NotebookScreen(nav: Nav) {
    val pr = AppGraph.progress
    pr.version
    val notes = pr.notes()
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(bottom = 30.dp)) {
        item {
            Row(Modifier.statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Text("My notebook", style = MaterialTheme.typography.titleLarge)
            }
        }
        if (notes.isEmpty()) item { EmptyCentered("Select any text in a guide and tap \"Save to notebook\". Your best lines collect here.") }
        items(notes, key = { it.text.hashCode() }) { n ->
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("“${n.text}”", style = MaterialTheme.typography.bodyLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { nav.article(n.postId) }, modifier = Modifier.weight(1f)) { Text(n.title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        IconButton(onClick = {
                            val t = topicFor(AppGraph.repo.preview(n.postId)?.label ?: "")
                            ShareCards.share(nav.context, ShareCards.answer(nav.context, t.title, n.title, n.text, t.from.toArgb(), t.to.toArgb()), "plainly-note")
                        }) { Icon(Icons.Rounded.Share, "Share") }
                        IconButton(onClick = { pr.removeNote(n) }) { Icon(Icons.Rounded.Delete, "Delete") }
                    }
                }
            }
        }
    }
}

/** "Did it work?" a few days after the reader said they'd try a guide's advice. */
@Composable
fun TriedFollowUpCard() {
    val pr = AppGraph.progress
    pr.version
    val t = pr.followUp() ?: return
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text("YOU SAID YOU'D TRY THIS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
            Text(t.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Did it work?", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("yes" to "👍 Yes", "some" to "🤏 A bit", "no" to "👎 Not really", "later" to "⏳ Not yet").forEach { (k, l) ->
                    Surface(onClick = { if (k == "later") pr.snoozeTried(t.postId) else pr.answerTried(t.postId, k) }, shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface) { Text(l, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) }
                }
            }
        }
    }
}
