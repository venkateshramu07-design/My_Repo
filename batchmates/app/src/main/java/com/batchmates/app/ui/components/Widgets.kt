package com.batchmates.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.batchmates.app.ui.theme.Brand

/** Frosted-glass card used everywhere. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    corner: Dp = 24.dp,
    padding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(stiffness = 600f), label = "press")
    Column(
        modifier
            .scale(scale)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0x24FFFFFF), Color(0x0FFFFFFF))))
            .border(BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0x55FFFFFF), Color(0x11FFFFFF)))), shape)
            .then(if (onClick != null) Modifier.clickable(interaction, null, onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

/** Big gradient call-to-action with a springy press. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush = Brand.Primary,
    icon: ImageVector? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, spring(stiffness = 700f), label = "btn")
    Box(
        modifier
            .scale(scale)
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) brush else Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x22FFFFFF))))
            .clickable(interaction, null, enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Text(text, color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** Initials avatar with a per-person gradient, or their photo when available. */
@Composable
fun Avatar(name: String, photoUrl: String?, size: Dp = 44.dp, ring: Color = Brand.Night) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(ring)
            .padding(2.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(Brand.pair(name))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" },
            color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.36f).sp,
        )
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(photoUrl, null, Modifier.size(size).clip(CircleShape), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
fun AvatarStack(names: List<Pair<String, String?>>, total: Int, size: Dp = 32.dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        names.take(4).forEachIndexed { i, (n, p) ->
            Box(Modifier.offset(x = (-10 * i).dp)) { Avatar(n, p, size) }
        }
        val extra = total - names.take(4).size
        if (extra > 0) {
            Box(
                Modifier.offset(x = (-10 * names.take(4).size).dp).size(size).clip(CircleShape)
                    .background(Brand.Night).padding(2.dp).clip(CircleShape).background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center,
            ) { Text("+$extra", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        trailingIcon = {
            if (value.isNotEmpty()) IconButton({ onValueChange("") }) { Icon(Icons.Rounded.Close, "Clear") }
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0x1AFFFFFF),
            unfocusedContainerColor = Color(0x12FFFFFF),
            focusedBorderColor = Brand.Violet,
            unfocusedBorderColor = Color(0x22FFFFFF),
        ),
    )
}

@Composable
fun Pill(text: String, color: Color = Brand.Sun, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1,
    )
}

/** Circle icon chip (used for invite targets). */
@Composable
fun BrandCircle(
    label: String,
    icon: @Composable () -> Unit,
    background: Brush,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(background).clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Text(label, style = MaterialTheme.typography.labelMedium, color = Brand.TextDim)
    }
}

@Composable
fun EmptyState(emoji: String, title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = Brand.TextDim, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
