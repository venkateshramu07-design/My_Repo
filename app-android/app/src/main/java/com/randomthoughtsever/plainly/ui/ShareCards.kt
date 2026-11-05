package com.randomthoughtsever.plainly.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.randomthoughtsever.plainly.BuildConfig
import com.randomthoughtsever.plainly.R
import com.randomthoughtsever.plainly.tools.shareFile
import java.io.ByteArrayOutputStream

/**
 * Picture cards for sharing to WhatsApp, Instagram and the rest: a check-up score or a guide's quick answer,
 * in Plainly's colours, with the app's name and Play Store link. 1080 × 1350 (the shape feeds show in full).
 */
object ShareCards {
    private const val W = 1080; private const val H = 1350
    private val PLAY = "play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

    fun score(context: Context, title: String, score: Int, verdict: String, weakest: String): Bitmap = card(context, 0xFF12304F.toInt(), 0xFF0A1B2F.toInt()) { c, serif, sans ->
        text(c, title.uppercase(), sans, 40f, 0xFFF2C46B.toInt(), 90f, 170f, bold = true)
        // ring
        val cx = W / 2f; val cy = 560f; val r = 250f
        val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 46f; color = 0x33FFFFFF }
        c.drawCircle(cx, cy, r, track)
        val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 46f; strokeCap = Paint.Cap.ROUND; color = 0xFFF2C46B.toInt() }
        c.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), -90f, 360f * score / 100f, false, arc)
        centered(c, "$score", serif, 190f, 0xFFFFFFFF.toInt(), cy + 60f)
        centered(c, "out of 100", sans, 40f, 0xCCFFFFFF.toInt(), cy + 120f)
        centered(c, verdict, serif, 72f, 0xFFFFFFFF.toInt(), 960f)
        centered(c, "Working on: $weakest", sans, 42f, 0xCCFFFFFF.toInt(), 1030f)
        centered(c, "What's your score?", sans, 44f, 0xFFF2C46B.toInt(), 1130f, bold = true)
    }

    fun answer(context: Context, topic: String, title: String, answer: String, from: Int, to: Int): Bitmap = card(context, from, to) { c, serif, sans ->
        text(c, topic.uppercase(), sans, 38f, 0xFFF2C46B.toInt(), 90f, 170f, bold = true)
        val t = block(c, title, serif, 74f, 0xFFFFFFFF.toInt(), 90f, 210f, 900, 4)
        val box = RectF(70f, 240f + t + 30f, W - 70f, 1120f)
        c.drawRoundRect(box, 36f, 36f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x1FFFFFFF })
        text(c, "THE SHORT ANSWER", sans, 32f, 0xFFF2C46B.toInt(), 110f, box.top + 70f, bold = true)
        block(c, answer, sans, 44f, 0xF2FFFFFF.toInt(), 110f, box.top + 100f, 860, ((box.height() - 130f) / 62f).toInt().coerceAtLeast(3))
    }

    fun share(context: Context, bmp: Bitmap, name: String) {
        val bytes = ByteArrayOutputStream().also { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
        shareFile(context, bytes, "$name.png", "image/png")
    }

    private fun card(context: Context, from: Int, to: Int, draw: (Canvas, Typeface, Typeface) -> Unit): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawRect(0f, 0f, W.toFloat(), H.toFloat(), Paint().apply { shader = LinearGradient(0f, 0f, W.toFloat(), H.toFloat(), from, to, Shader.TileMode.CLAMP) })
        val serif = runCatching { ResourcesCompat.getFont(context, R.font.fraunces_bold) }.getOrNull() ?: Typeface.SERIF
        val sans = runCatching { ResourcesCompat.getFont(context, R.font.jakarta_bold) }.getOrNull() ?: Typeface.SANS_SERIF
        draw(c, serif, sans)
        // footer: logo tile + name + link
        val tile = RectF(90f, H - 170f, 170f, H - 90f)
        c.drawRoundRect(tile, 22f, 22f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1F4E79.toInt() })
        centeredIn(c, "P", sans, 50f, 0xFFFFFFFF.toInt(), tile)
        c.drawCircle(tile.right - 16f, tile.top + 16f, 8f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF2C46B.toInt() })
        text(c, "Plainly: Money, Mind & Work", sans, 38f, 0xFFFFFFFF.toInt(), 196f, H - 128f, bold = true)
        text(c, "Free on Google Play", sans, 30f, 0xB3FFFFFF.toInt(), 196f, H - 88f)
        return bmp
    }

    private fun paint(face: Typeface, size: Float, color: Int, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = face; textSize = size; this.color = color; isFakeBoldText = false; if (bold) letterSpacing = .06f
    }
    private fun text(c: Canvas, s: String, face: Typeface, size: Float, color: Int, x: Float, y: Float, bold: Boolean = false) = c.drawText(s, x, y, paint(face, size, color, bold))
    private fun centered(c: Canvas, s: String, face: Typeface, size: Float, color: Int, y: Float, bold: Boolean = false) {
        val p = paint(face, size, color, bold); c.drawText(s, (W - p.measureText(s)) / 2f, y, p)
    }
    private fun centeredIn(c: Canvas, s: String, face: Typeface, size: Float, color: Int, r: RectF) {
        val p = paint(face, size, color); c.drawText(s, r.centerX() - p.measureText(s) / 2f, r.centerY() + size / 3f, p)
    }
    /** Wrapped text; returns its height. */
    private fun block(c: Canvas, s: String, face: Typeface, size: Float, color: Int, x: Float, y: Float, width: Int, maxLines: Int): Float {
        val layout = StaticLayout.Builder.obtain(s, 0, s.length, paint(face, size, color), width).setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.15f).setMaxLines(maxLines).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
        c.save(); c.translate(x, y); layout.draw(c); c.restore()
        return layout.height.toFloat()
    }
}
