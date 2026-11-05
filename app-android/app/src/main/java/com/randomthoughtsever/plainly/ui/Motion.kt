package com.randomthoughtsever.plainly.ui

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomthoughtsever.plainly.data.Entry
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Short looping motion scenes, one per topic, drawn by the app (no video files: crisp at any size, offline, tiny).
 * Money: coins fill a jar. Investing: a chart draws itself. Countries: a globe turns with a plane. Career: climbing
 * steps. Mind: a breathing circle. Self improvement: a plant grows. Health: a heartbeat. AI: a neural network.
 * Crypto: a spinning coin. Relationships: orbiting hearts. Zen: ripples and a leaf. Leadership: a flag. Education: a book.
 * When the phone's animations are turned off (Accessibility), a still frame is shown instead.
 */
@Composable
fun TopicScene(label: String, modifier: Modifier = Modifier, background: Boolean = true, seconds: Int = 5) {
    val context = LocalContext.current
    val still = remember { runCatching { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }.getOrDefault(false) }
    val t = if (still) .62f else rememberInfiniteTransition(label = "scene").animateFloat(0f, 1f,
        infiniteRepeatable(tween(seconds * 1000, easing = LinearEasing)), label = "t").value
    val topic = topicFor(label)
    val text = rememberTextMeasurer()
    Canvas(modifier.then(if (background) Modifier.background(Brush.linearGradient(listOf(topic.from, topic.to))) else Modifier)) {
        when (label) {
            "Personal Finance" -> coins(t)
            "Investing" -> chart(t)
            "Economics" -> bars(t)
            "Countries & Living Abroad" -> globe(t)
            "Career & Workplace" -> steps(t)
            "Psychology" -> breathe(t)
            "Self Improvement" -> plant(t)
            "Health & Wellness" -> heartbeat(t)
            "Technology & AI" -> network(t)
            "Cryptocurrency" -> coin(t, text)
            "Love & Relationships" -> hearts(t)
            "Zen & Mindfulness" -> ripples(t)
            "Leadership" -> flag(t)
            "Education" -> book(t)
            else -> sparkles(t)
        }
    }
}

/** A slow "Ken Burns" push-in, so a still cover feels like the first second of a film. */
@Composable
fun Modifier.kenBurns(seconds: Int = 14): Modifier {
    val context = LocalContext.current
    val still = remember { runCatching { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }.getOrDefault(false) }
    if (still) return this
    val k by rememberInfiniteTransition(label = "kb").animateFloat(0f, 1f, infiniteRepeatable(tween(seconds * 1000, easing = LinearEasing), RepeatMode.Reverse), label = "k")
    return graphicsLayer { scaleX = 1f + .1f * k; scaleY = 1f + .1f * k; translationX = -size.width * .03f * k; translationY = -size.height * .02f * k }
}

/** A full-width animated card for a guide: its topic's scene, then the 30-second answer. Used at random spots in feeds. */
@Composable
fun MotionCard(e: Entry, kicker: String, nav: Nav, modifier: Modifier = Modifier) {
    Surface(onClick = { nav.article(e.id) }, shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()) {
        Column {
            Box {
                TopicScene(e.label, Modifier.fillMaxWidth().height(190.dp))
                Text(kicker, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(14.dp).background(Color.Black.copy(alpha = .28f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp))
            }
            Column(Modifier.padding(18.dp)) {
                Kicker(topicFor(e.label).title)
                Spacer(Modifier.height(4.dp))
                Text(e.title, style = MaterialTheme.typography.titleLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
                if (e.quick.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(e.quick, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3,
                        overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Read the guide", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ---------- Scenes. t runs 0 → 1 over one loop. ----------

private val W = Color.White
private val G = Color(0xFFF2C46B)
private fun ease(x: Float) = x.coerceIn(0f, 1f).let { 1f - (1f - it) * (1f - it) }
private fun DrawScope.s() = min(size.width, size.height)
private fun wave(t: Float, k: Float = 1f) = sin(2 * PI.toFloat() * t * k)

private fun DrawScope.twinkles(t: Float, n: Int = 6) {
    repeat(n) { i ->
        val x = size.width * ((i * 0.37f + 0.11f) % 1f); val y = size.height * ((i * 0.53f + 0.07f) % 0.9f)
        val a = (0.5f + 0.5f * wave(t + i / n.toFloat())).coerceIn(0f, 1f)
        star(Offset(x, y), s() * 0.018f * (0.6f + a), W.copy(alpha = 0.25f + 0.5f * a))
    }
}

private fun DrawScope.star(c: Offset, r: Float, color: Color) {
    val p = Path().apply {
        moveTo(c.x, c.y - r * 2); lineTo(c.x + r * .5f, c.y - r * .5f); lineTo(c.x + r * 2, c.y); lineTo(c.x + r * .5f, c.y + r * .5f)
        lineTo(c.x, c.y + r * 2); lineTo(c.x - r * .5f, c.y + r * .5f); lineTo(c.x - r * 2, c.y); lineTo(c.x - r * .5f, c.y - r * .5f); close()
    }
    drawPath(p, color)
}

private fun DrawScope.coins(t: Float) {
    val s = s(); val cx = size.width / 2; val jw = s * .36f; val jh = s * .42f; val top = size.height * .42f
    val level = ease(t * 1.15f) * .85f
    clipRect(cx - jw / 2, top, cx + jw / 2, top + jh) {
        drawRect(G.copy(alpha = .9f), Offset(cx - jw / 2, top + jh * (1 - level)), Size(jw, jh * level))
        repeat(4) { i -> drawLine(Color(0xFFC9963A), Offset(cx - jw / 2, top + jh * (1 - level) + i * s * .035f), Offset(cx + jw / 2, top + jh * (1 - level) + i * s * .035f), s * .006f) }
    }
    drawRoundRect(W.copy(alpha = .9f), Offset(cx - jw / 2, top), Size(jw, jh), CornerRadius(s * .05f), style = Stroke(s * .018f))
    drawRoundRect(W.copy(alpha = .9f), Offset(cx - jw * .32f, top - s * .05f), Size(jw * .64f, s * .05f), CornerRadius(s * .02f))
    repeat(3) { k ->
        val p = (t * 3 + k / 3f) % 1f
        val y = -s * .08f + (top + jh * (1 - level) - s * .03f + s * .08f) * ease(p)
        val x = cx + (k - 1) * s * .07f
        if (p < .95f) {
            drawCircle(G, s * .045f, Offset(x, y)); drawCircle(Color(0xFFC9963A), s * .045f, Offset(x, y), style = Stroke(s * .01f))
            drawCircle(Color(0xFFC9963A), s * .018f, Offset(x, y))
        }
    }
    twinkles(t, 5)
}

private fun DrawScope.chart(t: Float) {
    val w = size.width; val h = size.height; val s = s()
    repeat(4) { i -> drawLine(W.copy(alpha = .12f), Offset(w * .08f, h * (.25f + i * .17f)), Offset(w * .92f, h * (.25f + i * .17f)), s * .004f) }
    val ys = floatArrayOf(.78f, .7f, .74f, .58f, .62f, .46f, .5f, .32f, .24f)
    val path = Path().apply { ys.forEachIndexed { i, y -> val x = w * (.08f + .84f * i / (ys.size - 1)); if (i == 0) moveTo(x, h * y) else lineTo(x, h * y) } }
    val pm = PathMeasure().apply { setPath(path, false) }
    val p = ease(t * 1.3f)
    val seg = Path(); pm.getSegment(0f, pm.length * p, seg, true)
    drawPath(seg, G, style = Stroke(s * .022f, cap = StrokeCap.Round))
    val head = pm.getPosition(pm.length * p)
    drawCircle(G.copy(alpha = .25f), s * (.05f + .02f * wave(t, 3f)), head); drawCircle(W, s * .022f, head)
    if (p > .98f) star(head + Offset(s * .06f, -s * .06f), s * .02f, W)
}

private fun DrawScope.bars(t: Float) {
    val w = size.width; val h = size.height; val s = s(); val base = h * .82f
    val heights = floatArrayOf(.22f, .32f, .28f, .42f, .55f)
    heights.forEachIndexed { i, b ->
        val g = ease(t * 1.6f - i * .12f); val bw = w * .1f; val x = w * (.18f + i * .14f)
        drawRoundRect(if (i == heights.lastIndex) G else W.copy(alpha = .75f), Offset(x, base - h * b * g), Size(bw, h * b * g), CornerRadius(s * .02f))
    }
    val bob = s * .02f * wave(t, 2f)
    val ax = w * .86f; val ay = h * .2f + bob
    drawLine(G, Offset(ax, ay + s * .14f), Offset(ax, ay), s * .02f, StrokeCap.Round)
    drawPath(Path().apply { moveTo(ax - s * .05f, ay + s * .04f); lineTo(ax, ay - s * .02f); lineTo(ax + s * .05f, ay + s * .04f) }, G, style = Stroke(s * .02f, cap = StrokeCap.Round))
    drawLine(W.copy(alpha = .5f), Offset(w * .12f, base), Offset(w * .88f, base), s * .008f)
}

private fun DrawScope.globe(t: Float) {
    val s = s(); val c = Offset(size.width / 2, size.height * .54f); val r = s * .3f
    drawCircle(W.copy(alpha = .12f), r, c); drawCircle(W.copy(alpha = .85f), r, c, style = Stroke(s * .012f))
    for (k in 0 until 4) {
        val ph = (t + k / 4f) % 1f; val rx = r * abs(cos(PI.toFloat() * ph))
        drawOval(W.copy(alpha = .45f), Offset(c.x - rx, c.y - r), Size(rx * 2, r * 2), style = Stroke(s * .006f))
    }
    for (lat in listOf(-.5f, 0f, .5f)) {
        val y = c.y + r * lat; val hw = r * kotlin.math.sqrt(1 - lat * lat)
        drawOval(W.copy(alpha = .4f), Offset(c.x - hw, y - r * .08f), Size(hw * 2, r * .16f), style = Stroke(s * .006f))
    }
    val a = 2 * PI.toFloat() * t; val rx = r * 1.45f; val ry = r * .5f
    repeat(8) { i -> val b = a - i * .12f; drawCircle(W.copy(alpha = .5f - i * .05f), s * .008f, Offset(c.x + rx * cos(b), c.y + ry * sin(b) - r * .2f)) }
    val pos = Offset(c.x + rx * cos(a), c.y + ry * sin(a) - r * .2f)
    val ang = Math.toDegrees(kotlin.math.atan2((ry * cos(a)).toDouble(), (-rx * sin(a)).toDouble())).toFloat()
    rotate(ang, pos) {
        drawPath(Path().apply { moveTo(pos.x + s * .05f, pos.y); lineTo(pos.x - s * .03f, pos.y - s * .03f); lineTo(pos.x - s * .015f, pos.y)
            lineTo(pos.x - s * .03f, pos.y + s * .03f); close() }, G)
    }
}

private fun DrawScope.steps(t: Float) {
    val w = size.width; val h = size.height; val s = s(); val n = 5
    val sw = w * .14f; val sh = h * .12f; val x0 = w * .12f; val y0 = h * .86f
    for (i in 0 until n) drawRect(W.copy(alpha = .22f + i * .1f), Offset(x0 + i * sw, y0 - (i + 1) * sh), Size(sw, (i + 1) * sh))
    val fx = x0 + n * sw - sw * .3f; val fy = y0 - n * sh
    drawLine(W, Offset(fx, fy), Offset(fx, fy - s * .2f), s * .01f)
    drawPath(Path().apply { moveTo(fx, fy - s * .2f); lineTo(fx + s * .11f, fy - s * .16f + s * .01f * wave(t, 3f)); lineTo(fx, fy - s * .12f); close() }, G)
    val p = (t * n * 1.1f).coerceAtMost(n - .001f); val i = p.toInt(); val f = p - i
    val hx = x0 + (i + f) * sw + sw * .5f; val hy = y0 - (i + ease(f)) * sh - sh * .0f - abs(sin(PI.toFloat() * f)) * sh * .6f
    drawCircle(G, s * .035f, Offset(hx, hy - s * .12f)); drawLine(G, Offset(hx, hy - s * .085f), Offset(hx, hy - s * .02f), s * .025f, StrokeCap.Round)
}

private fun DrawScope.breathe(t: Float) {
    val s = s(); val c = center
    repeat(3) { k -> val p = (t + k / 3f) % 1f; drawCircle(W.copy(alpha = .45f * (1 - p)), s * (.14f + p * .32f), c, style = Stroke(s * .008f)) }
    val r = s * (.15f + .05f * wave(t))
    drawCircle(Brush.radialGradient(listOf(W.copy(alpha = .95f), G.copy(alpha = .7f)), c, r), r, c)
    repeat(5) { i -> val a = 2 * PI.toFloat() * (t * .5f + i / 5f); drawCircle(W.copy(alpha = .7f), s * .014f, Offset(c.x + cos(a) * s * .3f, c.y + sin(a) * s * .3f)) }
}

private fun DrawScope.plant(t: Float) {
    val w = size.width; val h = size.height; val s = s(); val g = h * .84f
    translate(w * .78f, h * .22f) { rotate(t * 90f, Offset.Zero) { repeat(8) { i -> rotate(i * 45f, Offset.Zero) { drawLine(G.copy(alpha = .8f), Offset(s * .07f, 0f), Offset(s * .11f, 0f), s * .012f, StrokeCap.Round) } } }
        drawCircle(G, s * .05f, Offset.Zero) }
    drawLine(W.copy(alpha = .6f), Offset(w * .15f, g), Offset(w * .85f, g), s * .01f)
    val grow = ease(t * 1.4f); val top = g - s * .42f * grow; val cx = w * .45f
    drawLine(W, Offset(cx, g), Offset(cx, top), s * .016f, StrokeCap.Round)
    listOf(.3f to -1f, .55f to 1f, .78f to -1f).forEachIndexed { i, (at, side) ->
        val pop = ease((grow - at) * 4f); if (pop <= 0f) return@forEachIndexed
        val y = g - s * .42f * at; val leaf = Path().apply { moveTo(cx, y); quadraticTo(cx + side * s * .1f, y - s * .08f, cx + side * s * .16f * pop, y - s * .02f * pop); quadraticTo(cx + side * s * .08f, y + s * .02f, cx, y) }
        drawPath(leaf, if (i == 2) G else W.copy(alpha = .9f))
    }
}

private fun DrawScope.heartbeat(t: Float) {
    val w = size.width; val h = size.height; val s = s(); val mid = h * .58f
    val path = Path(); val step = w / 60f
    for (i in 0..60) {
        val x = i * step; val ph = ((i / 60f) + t) % 1f
        val y = mid - when { ph in .40f..0.44f -> (ph - .40f) / .04f * h * .28f; ph in .44f..0.48f -> (1 - (ph - .44f) / .04f * 2) * h * .28f
            ph in .48f..0.52f -> (-1 + (ph - .48f) / .04f) * h * .1f; else -> 0f }
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path, W.copy(alpha = .9f), style = Stroke(s * .014f, cap = StrokeCap.Round))
    val beat = 1f + .14f * abs(sin(2 * PI.toFloat() * t * 2))
    scale(beat, Offset(w * .5f, h * .26f)) { drawPath(heart(Offset(w * .5f, h * .26f), s * .16f), G) }
}

private fun DrawScope.network(t: Float) {
    val w = size.width; val h = size.height; val s = s()
    val layers = listOf(3, 4, 3).mapIndexed { li, n -> List(n) { i -> Offset(w * (.2f + .3f * li), h * (.5f + (i - (n - 1) / 2f) * .2f)) } }
    var e = 0
    for (li in 0 until 2) for (a in layers[li]) for (b in layers[li + 1]) {
        drawLine(W.copy(alpha = .18f), a, b, s * .005f)
        val p = (t * 2 + e * .137f) % 1f
        drawCircle(G.copy(alpha = 1 - abs(p - .5f) * 2), s * .012f, a + (b - a) * p); e++
    }
    layers.flatten().forEachIndexed { i, n ->
        val glow = (.5f + .5f * wave(t * 2 + i * .21f)).coerceIn(0f, 1f)
        drawCircle(G.copy(alpha = .25f * glow), s * .055f, n); drawCircle(W, s * .03f, n)
    }
}

private fun DrawScope.coin(t: Float, text: TextMeasurer) {
    val s = s(); val c = center; val r = s * .26f; val sx = cos(2 * PI.toFloat() * t)
    val rx = r * abs(sx).coerceAtLeast(.06f)
    drawOval(Color(0xFFC9963A), Offset(c.x - rx + s * .015f, c.y - r), Size(rx * 2, r * 2))
    drawOval(if (sx > 0) G else Color(0xFFE0AE4E), Offset(c.x - rx, c.y - r), Size(rx * 2, r * 2))
    drawOval(Color(0xFFC9963A), Offset(c.x - rx * .78f, c.y - r * .78f), Size(rx * 1.56f, r * 1.56f), style = Stroke(s * .012f))
    if (abs(sx) > .35f) {
        val layout = text.measure("₿", TextStyle(color = Color(0xFF7A4E0C), fontSize = (s * .2f / density).sp, fontWeight = FontWeight.ExtraBold))
        scale(abs(sx), 1f, c) { drawText(layout, topLeft = c - Offset(layout.size.width / 2f, layout.size.height / 2f)) }
    }
    twinkles(t, 6)
}

private fun DrawScope.hearts(t: Float) {
    val s = s(); val c = center; val a = 2 * PI.toFloat() * t
    val b1 = 1f + .1f * abs(sin(a * 2)); val b2 = 1f + .1f * abs(cos(a * 2))
    val p1 = Offset(c.x + cos(a) * s * .1f, c.y + sin(a) * s * .05f); val p2 = Offset(c.x - cos(a) * s * .1f, c.y - sin(a) * s * .05f)
    scale(b1, p1) { drawPath(heart(p1, s * .17f), W.copy(alpha = .92f)) }
    scale(b2, p2) { drawPath(heart(p2, s * .14f), G) }
    repeat(5) { i -> val p = (t + i / 5f) % 1f
        drawPath(heart(Offset(size.width * (.15f + i * .18f), size.height * (1 - p)), s * .04f), W.copy(alpha = .5f * (1 - p))) }
}

private fun DrawScope.ripples(t: Float) {
    val s = s(); val c = Offset(size.width / 2, size.height * .62f)
    repeat(3) { k -> val p = (t + k / 3f) % 1f; val rx = s * (.08f + p * .45f)
        drawOval(W.copy(alpha = .6f * (1 - p)), Offset(c.x - rx, c.y - rx * .32f), Size(rx * 2, rx * .64f), style = Stroke(s * .008f)) }
    val lx = size.width * (.15f + .7f * t); val ly = size.height * .32f + sin(t * 4 * PI.toFloat()) * s * .04f
    rotate(20f * wave(t, 2f), Offset(lx, ly)) {
        drawPath(Path().apply { moveTo(lx - s * .07f, ly); quadraticTo(lx, ly - s * .06f, lx + s * .07f, ly); quadraticTo(lx, ly + s * .06f, lx - s * .07f, ly) }, G)
        drawLine(Color(0xFFC9963A), Offset(lx - s * .07f, ly), Offset(lx + s * .07f, ly), s * .006f)
    }
    drawOval(W.copy(alpha = .85f), Offset(c.x - s * .05f, c.y - s * .025f), Size(s * .1f, s * .05f))
}

private fun DrawScope.flag(t: Float) {
    val w = size.width; val h = size.height; val s = s()
    drawOval(W.copy(alpha = .25f), Offset(w * .1f, h * .78f), Size(w * .8f, h * .4f))
    val px = w * .4f; val top = h * .2f; val fw = w * .32f; val fh = h * .22f
    drawLine(W, Offset(px, h * .82f), Offset(px, top - s * .02f), s * .014f, StrokeCap.Round)
    val path = Path(); val n = 20
    for (i in 0..n) { val x = px + fw * i / n; val y = top + sin(i / n.toFloat() * 2.5f * PI.toFloat() - t * 2 * PI.toFloat()) * s * .025f * (i / n.toFloat()); if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) }
    for (i in n downTo 0) { val x = px + fw * i / n; val y = top + fh + sin(i / n.toFloat() * 2.5f * PI.toFloat() - t * 2 * PI.toFloat()) * s * .025f * (i / n.toFloat()); path.lineTo(x, y) }
    path.close(); drawPath(path, G)
    twinkles(t, 4)
}

private fun DrawScope.book(t: Float) {
    val s = s(); val c = Offset(size.width / 2, size.height * .62f); val pw = s * .26f; val ph = s * .32f
    drawRoundRect(W.copy(alpha = .9f), Offset(c.x - pw, c.y - ph / 2), Size(pw, ph), CornerRadius(s * .015f))
    drawRoundRect(W.copy(alpha = .9f), Offset(c.x, c.y - ph / 2), Size(pw, ph), CornerRadius(s * .015f))
    repeat(4) { i -> drawLine(Color(0x55000000), Offset(c.x - pw * .8f, c.y - ph * .3f + i * ph * .17f), Offset(c.x - pw * .2f, c.y - ph * .3f + i * ph * .17f), s * .006f) }
    val f = cos(PI.toFloat() * ((t * 2) % 1f))
    scale(f, 1f, Offset(c.x, c.y)) { drawRoundRect(if (f > 0) Color(0xFFF7F1E3) else G, Offset(c.x, c.y - ph / 2), Size(pw, ph), CornerRadius(s * .015f)) }
    drawLine(Color(0x33000000), Offset(c.x, c.y - ph / 2), Offset(c.x, c.y + ph / 2), s * .008f)
    val by = size.height * .2f + s * .03f * wave(t, 2f)
    drawPath(Path().apply { moveTo(c.x, by - s * .06f); lineTo(c.x + s * .14f, by); lineTo(c.x, by + s * .06f); lineTo(c.x - s * .14f, by); close() }, G)
    drawLine(G, Offset(c.x + s * .1f, by), Offset(c.x + s * .12f, by + s * .1f), s * .01f)
}

private fun DrawScope.sparkles(t: Float) = twinkles(t, 9)

private fun heart(c: Offset, size: Float): Path = Path().apply {
    moveTo(c.x, c.y + size * .35f)
    cubicTo(c.x - size * .9f, c.y - size * .2f, c.x - size * .4f, c.y - size * .75f, c.x, c.y - size * .3f)
    cubicTo(c.x + size * .4f, c.y - size * .75f, c.x + size * .9f, c.y - size * .2f, c.x, c.y + size * .35f)
    close()
}
