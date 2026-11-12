package com.batchmates.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.batchmates.app.ui.theme.Brand
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Living "aurora" backdrop: three slow-drifting colour blobs plus twinkling stars.
 * Pure Canvas, no images, so it costs nothing in APK size.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    content: @Composable BoxScope.() -> Unit,
) {
    val t = rememberInfiniteTransition(label = "aurora")
    val phase by t.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(18_000, easing = LinearEasing)), label = "phase",
    )
    val twinkle by t.animateFloat(
        0f, 1f, infiniteRepeatable(tween(2_400, easing = LinearEasing), RepeatMode.Reverse), label = "twinkle",
    )
    val stars = remember {
        val r = Random(7)
        List(70) { Triple(r.nextFloat(), r.nextFloat(), r.nextFloat()) }
    }
    Box(modifier.fillMaxSize().background(Brand.Night)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            fun blob(color: Color, cx: Float, cy: Float, radius: Float) {
                drawCircle(
                    Brush.radialGradient(listOf(color.copy(alpha = 0.55f * intensity), Color.Transparent), Offset(cx, cy), radius),
                    radius, Offset(cx, cy),
                )
            }
            blob(Brand.Violet, w * (0.2f + 0.15f * cos(phase)), h * (0.18f + 0.06f * sin(phase * 2)), w * 0.9f)
            blob(Brand.Coral, w * (0.9f + 0.1f * sin(phase)), h * (0.55f + 0.1f * cos(phase)), w * 0.75f)
            blob(Brand.Sky, w * (0.1f + 0.12f * sin(phase + 1f)), h * (0.95f + 0.05f * cos(phase * 2)), w * 0.8f)
            stars.forEachIndexed { i, (x, y, s) ->
                val a = (0.25f + 0.75f * ((twinkle + s + i * 0.13f) % 1f)) * 0.8f
                drawCircle(Color.White.copy(alpha = a * 0.6f), 0.8f + s * 1.8f, Offset(x * w, y * h))
            }
        }
        content()
    }
}
