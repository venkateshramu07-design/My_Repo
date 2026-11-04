package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.randomthoughtsever.plainly.data.Post
import com.randomthoughtsever.plainly.data.Ref

/** Width ÷ height of the blog's cover art (800 × 420). */
const val COVER_RATIO = 800f / 420f

/** Cover image with a crossfade; while loading or when an article has no image, a topic-coloured tile is drawn instead. */
@Composable
fun Cover(image: String?, label: String, modifier: Modifier = Modifier, iconSize: Dp = 34.dp, corner: Dp = 18.dp,
          alignment: Alignment = Alignment.Center) {
    val t = topicFor(label)
    val fallback: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(t.from, t.to))), contentAlignment = Alignment.Center) {
            Icon(t.icon, null, tint = Color.White.copy(alpha = .85f), modifier = Modifier.size(iconSize))
        }
    }
    // The coloured tile is always drawn underneath, so there is never an empty slot while the image loads or if it fails.
    Box(modifier.clip(RoundedCornerShape(corner))) {
        fallback()
        if (image != null) AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(image).crossfade(300).build(),
            contentDescription = null, contentScale = ContentScale.Crop, alignment = alignment, modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun Kicker(text: String, color: Color = MaterialTheme.colorScheme.secondary) =
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)

/** Compact list row: thumbnail, topic, title, reading time. */
@Composable
fun PostRow(title: String, image: String?, label: String, minutes: Int, onClick: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Cover art has its title on the left and the illustration on the right, so thumbnails show the illustration.
        Cover(image, label, Modifier.size(width = 104.dp, height = 78.dp), 26.dp, alignment = Alignment.CenterEnd)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Kicker(topicFor(label).title)
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text("$minutes min read", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
    }
}

@Composable
fun PostRow(p: Post, onClick: () -> Unit) = PostRow(p.title, p.image, p.label, p.minutes, onClick)

@Composable
fun PostRow(r: Ref, onClick: () -> Unit, trailing: (@Composable () -> Unit)? = null) = PostRow(r.title, r.image, r.label, r.minutes, onClick, trailing)

/** Large card: the full cover art on top, then the topic, title and reading time. */
@Composable
fun FeatureCard(p: Post, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column {
            Box {
                Cover(p.image, p.label, Modifier.fillMaxWidth().aspectRatio(COVER_RATIO), 56.dp, corner = 0.dp)
                Surface(shape = RoundedCornerShape(50), color = Gold, modifier = Modifier.padding(14.dp)) {
                    Text(topicFor(p.label).title.uppercase(), style = MaterialTheme.typography.labelSmall, color = NavyDeep,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(p.title, style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, minLines = 2)
                Text("${p.minutes} min read · ${p.excerpt.take(60).substringBeforeLast(' ')}…", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Shimmering placeholder while content loads. */
@Composable
fun Shimmer(modifier: Modifier) {
    val x by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = -400f, targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart), label = "x",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier.clip(RoundedCornerShape(18.dp)).background(
        Brush.linearGradient(listOf(base, base.copy(alpha = .45f), base), start = androidx.compose.ui.geometry.Offset(x, 0f),
            end = androidx.compose.ui.geometry.Offset(x + 400f, 400f)),
    ))
}

@Composable
fun RowSkeleton() = Row(Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
    Shimmer(Modifier.size(width = 104.dp, height = 78.dp))
    Spacer(Modifier.width(14.dp))
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Shimmer(Modifier.width(70.dp).height(10.dp)); Shimmer(Modifier.fillMaxWidth().height(14.dp)); Shimmer(Modifier.fillMaxWidth(.7f).height(14.dp))
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 10.dp), verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        if (action != null) Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onAction).padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(72.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun TopicTile(t: Topic, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.aspectRatio(1.25f).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(t.from, t.to))).clickable(onClick = onClick)) {
        Icon(t.icon, null, tint = Color.White.copy(alpha = .16f), modifier = Modifier.size(110.dp).align(Alignment.BottomEnd).padding(start = 20.dp, top = 20.dp))
        Column(Modifier.padding(16.dp).align(Alignment.BottomStart), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(t.icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(6.dp))
            Text(t.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Text(t.blurb, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .85f), maxLines = 2)
        }
    }
}
