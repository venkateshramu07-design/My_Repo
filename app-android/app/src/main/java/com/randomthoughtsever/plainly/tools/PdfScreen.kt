package com.randomthoughtsever.plainly.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.ui.AppGraph
import com.randomthoughtsever.plainly.ui.Nav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

/** A minimal PDF writer: one JPEG image per page, drawn to fill the page. */
object PdfWriter {
    class Page(val jpeg: ByteArray, val px: Int, val py: Int, val wPt: Float, val hPt: Float)

    fun write(pages: List<Page>): ByteArray {
        val out = ByteArrayOutputStream()
        val offsets = ArrayList<Int>()
        fun w(s: String) = out.write(s.toByteArray(Charsets.ISO_8859_1))
        fun obj(n: Int, body: () -> Unit) { while (offsets.size < n) offsets.add(0); offsets[n - 1] = out.size(); w("$n 0 obj\n"); body(); w("\nendobj\n") }
        w("%PDF-1.4\n%âãÏÓ\n")
        val n = pages.size
        // objects: 1 catalog, 2 pages, then per page: page, contents, image
        obj(1) { w("<< /Type /Catalog /Pages 2 0 R >>") }
        obj(2) { w("<< /Type /Pages /Count $n /Kids [" + (0 until n).joinToString(" ") { "${3 + it * 3} 0 R" } + "] >>") }
        pages.forEachIndexed { i, p ->
            val page = 3 + i * 3; val content = page + 1; val image = page + 2
            val wPt = "%.2f".format(java.util.Locale.US, p.wPt); val hPt = "%.2f".format(java.util.Locale.US, p.hPt)
            obj(page) { w("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $wPt $hPt] /Resources << /XObject << /Im0 $image 0 R >> >> /Contents $content 0 R >>") }
            val draw = "q $wPt 0 0 $hPt 0 0 cm /Im0 Do Q"
            obj(content) { w("<< /Length ${draw.length} >>\nstream\n$draw\nendstream") }
            obj(image) {
                w("<< /Type /XObject /Subtype /Image /Width ${p.px} /Height ${p.py} /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length ${p.jpeg.size} >>\nstream\n")
                out.write(p.jpeg); w("\nendstream")
            }
        }
        val xref = out.size()
        w("xref\n0 ${offsets.size + 1}\n0000000000 65535 f \n")
        offsets.forEach { w("%010d 00000 n \n".format(it)) }
        w("trailer\n<< /Size ${offsets.size + 1} /Root 1 0 R >>\nstartxref\n$xref\n%%EOF\n")
        return out.toByteArray()
    }
}

class PdfResult(val name: String, val original: Long, val bytes: ByteArray, val pages: Int, val dpi: Int, val quality: Int, val fits: Boolean)

object PdfEngine {
    private val DPIS = listOf(150, 125, 110, 96, 84, 72, 60, 50)

    fun compress(context: Context, uri: Uri, targetBytes: Int, gray: Boolean, progress: (Float) -> Unit): PdfResult {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: error("Can't open file")
        pfd.use { fd ->
            PdfRenderer(fd).use { r ->
                val count = minOf(r.pageCount, 60)
                fun encode(dpi: Int, q: Int): List<PdfWriter.Page> = (0 until count).map { i ->
                    r.openPage(i).use { page ->
                        val w = (page.width * dpi / 72f).roundToInt().coerceAtLeast(1)
                        val h = (page.height * dpi / 72f).roundToInt().coerceAtLeast(1)
                        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        bmp.eraseColor(android.graphics.Color.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        val src = if (gray) Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { g ->
                            Canvas(g).drawBitmap(bmp, 0f, 0f, Paint().apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) })
                        } else bmp
                        val jpg = ByteArrayOutputStream().also { src.compress(Bitmap.CompressFormat.JPEG, q, it) }.toByteArray()
                        PdfWriter.Page(jpg, w, h, page.width.toFloat(), page.height.toFloat())
                    }
                }
                val budget = targetBytes - 900 - count * 300
                var step = 0f
                for ((di, dpi) in DPIS.withIndex()) {
                    progress((di + 1f) / DPIS.size * .9f)
                    val low = encode(dpi, 40)
                    if (low.sumOf { it.jpeg.size } > budget) continue
                    var lo = 40; var hi = 88; var best = low; var bestQ = 40
                    while (lo <= hi) {
                        val q = (lo + hi) / 2
                        val pages = encode(dpi, q)
                        if (pages.sumOf { it.jpeg.size } <= budget) { best = pages; bestQ = q; lo = q + 6 } else hi = q - 6
                    }
                    progress(1f)
                    return PdfResult(PhotoEngine.name(context, uri), PhotoEngine.size(context, uri), PdfWriter.write(best), count, dpi, bestQ, true)
                }
                val smallest = encode(DPIS.last(), 30)
                progress(1f)
                val bytes = PdfWriter.write(smallest)
                return PdfResult(PhotoEngine.name(context, uri), PhotoEngine.size(context, uri), bytes, count, DPIS.last(), 30, bytes.size <= targetBytes)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PdfScreen(nav: Nav) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var picked by remember { mutableStateOf<Uri?>(null) }
    var targetKb by remember { mutableStateOf("500") }
    var gray by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var result by remember { mutableStateOf<PdfResult?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { if (it != null) { picked = it; result = null; message = null } }

    ToolScaffold("pdf", nav) {
        Surface(onClick = { picker.launch(arrayOf("application/pdf")) }, shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.PictureAsPdf, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(picked?.let { PhotoEngine.name(context, it) + ".pdf" } ?: "Choose a PDF", style = MaterialTheme.typography.titleMedium)
                    Text(picked?.let { "Original: ${kb(PhotoEngine.size(context, it))}" } ?: "Processed on your phone, never uploaded.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Label("Make it smaller than")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("100", "200", "300", "500", "1000", "2000").forEach { v ->
                FilterChip(targetKb == v, { targetKb = v }, label = { Text(if (v.length > 3) "${v.toInt() / 1000} MB" else "$v KB") })
            }
        }
        OutlinedTextField(targetKb, { targetKb = it.filter(Char::isDigit).take(6) }, label = { Text("Custom size in KB") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Black and white", style = MaterialTheme.typography.titleSmall)
                Text("Best for scanned text and certificates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(gray, { gray = it })
        }
        Text("Pages are saved as sharp images, so the text in the new PDF can't be selected or searched.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = {
            val uri = picked ?: return@Button
            val target = (targetKb.toIntOrNull() ?: 500).coerceIn(30, 50000) * 1000
            busy = true; message = null; result = null; progress = 0f
            scope.launch {
                runCatching { withContext(Dispatchers.Default) { PdfEngine.compress(context, uri, target, gray) { progress = it } } }
                    .onSuccess { result = it; if (!it.fits) message = "This is the smallest it can go: ${kb(it.bytes.size.toLong())}." }
                    .onFailure { message = "Couldn't open this PDF. It may be password-protected or damaged." }
                busy = false
            }
        }, enabled = picked != null && !busy, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
            Text(if (busy) "Compressing…" else "Compress PDF")
        }
        if (busy) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        result?.let { r ->
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("${kb(r.original)} → ${kb(r.bytes.size.toLong())}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text("${r.pages} page${if (r.pages > 1) "s" else ""} · ${r.dpi} dpi · quality ${r.quality}%", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    saveToPhone(context, r.bytes, "${r.name}-${targetKb}kb.pdf", "application/pdf")
                    AppGraph.library.countToolUse(); message = "Saved to Downloads/Plainly"; nav.maybeAskForReview()
                }, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Rounded.Download, null); Spacer(Modifier.width(6.dp)); Text("Save") }
                OutlinedButton(onClick = { shareFile(context, r.bytes, "${r.name}-compressed.pdf", "application/pdf") },
                    Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Rounded.Share, null); Spacer(Modifier.width(6.dp)); Text("Share") }
            }
        }
    }
}
