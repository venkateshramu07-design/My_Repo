package com.batchmates.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import com.batchmates.app.ui.theme.Brand
import kotlin.random.Random

/** One-shot celebratory burst when someone joins their batch. */
@Composable
fun ConfettiBurst(modifier: Modifier = Modifier, key: Any = Unit) {
    val progress = remember(key) { Animatable(0f) }
    val pieces = remember(key) {
        val r = Random(System.nanoTime())
        val colors = listOf(Brand.Violet, Brand.Coral, Brand.Sun, Brand.Mint, Brand.Sky, Brand.Orchid)
        List(140) {
            Piece(
                x = r.nextFloat(), vx = (r.nextFloat() - 0.5f) * 0.6f, vy = 0.6f + r.nextFloat() * 0.9f,
                spin = r.nextFloat() * 720f, color = colors[it % colors.size], w = 6f + r.nextFloat() * 8f,
                delay = r.nextFloat() * 0.25f,
            )
        }
    }
    LaunchedEffect(key) { progress.animateTo(1f, tween(3200, easing = LinearEasing)) }
    if (progress.value >= 1f) return
    Canvas(modifier.fillMaxSize()) {
        val p = progress.value
        pieces.forEach { c ->
            val t = ((p - c.delay) / (1f - c.delay)).coerceIn(0f, 1f)
            if (t <= 0f) return@forEach
            val x = (c.x + c.vx * t) * size.width
            val y = (-0.1f + c.vy * t + 0.6f * t * t) * size.height
            rotate(c.spin * t, Offset(x, y)) {
                drawRect(c.color.copy(alpha = 1f - t * 0.6f), Offset(x, y), Size(c.w, c.w * 0.5f))
            }
        }
    }
}

private class Piece(
    val x: Float, val vx: Float, val vy: Float, val spin: Float,
    val color: androidx.compose.ui.graphics.Color, val w: Float, val delay: Float,
)
