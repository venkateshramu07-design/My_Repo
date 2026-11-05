package com.randomthoughtsever.plainly.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Download
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.ui.AppGraph
import com.randomthoughtsever.plainly.ui.Nav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

class PhotoResult(val name: String, val original: Long, val bytes: ByteArray, val width: Int, val height: Int, val quality: Int, val preview: Bitmap)

object PhotoEngine {
    /** Smallest change that fits: highest JPEG quality under the limit, shrinking the pixels only when quality would drop below ~55%. */
    fun resize(context: Context, uri: Uri, targetBytes: Int, maxSide: Int, gray: Boolean): PhotoResult {
        val src = ImageDecoder.createSource(context.contentResolver, uri)
        var bmp = ImageDecoder.decodeBitmap(src) { d, info, _ ->
            d.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val side = max(info.size.width, info.size.height)
            val cap = if (maxSide > 0) minOf(maxSide, 4096) else 4096
            if (side > cap) { val s = cap.toFloat() / side; d.setTargetSize((info.size.width * s).roundToInt(), (info.size.height * s).roundToInt()) }
        }
        bmp = flatten(bmp, gray)
        var current = bmp
        repeat(14) {
            var lo = 55; var hi = 95; var best: ByteArray? = null; var bestQ = 0
            while (lo <= hi) {
                val q = (lo + hi) / 2
                val out = jpeg(current, q)
                if (out.size <= targetBytes) { best = out; bestQ = q; lo = q + 1 } else hi = q - 1
            }
            if (best != null) return PhotoResult(name(context, uri), size(context, uri), best!!, current.width, current.height, bestQ, current)
            // Even at quality 55 it's too big: shrink the pixels in proportion and try again.
            val at55 = jpeg(current, 55).size
            val factor = (sqrt(targetBytes.toDouble() / at55) * 0.96).coerceIn(0.35, 0.92)
            val w = max(16, (current.width * factor).roundToInt()); val h = max(16, (current.height * factor).roundToInt())
            if (w <= 16 || h <= 16) return@repeat
            current = Bitmap.createScaledBitmap(current, w, h, true)
        }
        val last = jpeg(current, 35)
        return PhotoResult(name(context, uri), size(context, uri), last, current.width, current.height, 35, current)
    }

    private fun flatten(src: Bitmap, gray: Boolean): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(out); c.drawColor(android.graphics.Color.WHITE)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        if (gray) paint.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        c.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    fun jpeg(b: Bitmap, q: Int): ByteArray = ByteArrayOutputStream().also { b.compress(Bitmap.CompressFormat.JPEG, q, it) }.toByteArray()

    fun name(context: Context, uri: Uri): String = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) it.getString(0) else null }
    }.getOrNull()?.substringBeforeLast('.') ?: "photo"

    fun size(context: Context, uri: Uri): Long = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { if (it.moveToFirst()) it.getLong(0) else 0L }
    }.getOrNull() ?: 0L
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoScreen(nav: Nav) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var picked by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var targetKb by remember { mutableStateOf("100") }
    var maxSide by remember { mutableStateOf(0) }
    var gray by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<PhotoResult>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { if (it.isNotEmpty()) { picked = it; results = emptyList() } }

    ToolScaffold("photo", nav) {
        Surface(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(if (picked.isEmpty()) "Choose photos" else "${picked.size} photo${if (picked.size > 1) "s" else ""} selected", style = MaterialTheme.typography.titleMedium)
                    Text("JPG, PNG, WebP or HEIC. Stays on your phone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Label("Maximum file size")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("20", "50", "100", "200", "500", "1000").forEach { v ->
                FilterChip(targetKb == v, { targetKb = v }, label = { Text(if (v == "1000") "1 MB" else "$v KB") })
            }
        }
        OutlinedTextField(targetKb, { targetKb = it.filter(Char::isDigit).take(5) }, label = { Text("Custom size in KB") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Label("Longest side")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0 to "Keep size", 2000 to "2000 px", 1200 to "1200 px", 800 to "800 px", 400 to "400 px").forEach { (v, l) ->
                FilterChip(maxSide == v, { maxSide = v }, label = { Text(l) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Black and white", style = MaterialTheme.typography.titleSmall)
                Text("Smaller and sharper for signatures and documents", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(gray, { gray = it })
        }
        Button(onClick = {
            val target = (targetKb.toIntOrNull() ?: 100).coerceIn(5, 20000) * 1000
            busy = true; message = null
            scope.launch {
                results = withContext(Dispatchers.Default) {
                    picked.mapNotNull { runCatching { PhotoEngine.resize(context, it, target, maxSide, gray) }.getOrNull() }
                }
                busy = false
                if (results.isEmpty()) message = "Couldn't read those photos. Try a JPG or PNG."
            }
        }, enabled = picked.isNotEmpty() && !busy, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
            Text(if (busy) "Resizing…" else "Resize ${if (picked.size > 1) "${picked.size} photos" else "photo"}")
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        results.forEach { r ->
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(r.preview.asImageBitmap(), null, Modifier.size(76.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${kb(r.original)} → ${kb(r.bytes.size.toLong())}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("${r.width} × ${r.height} px · quality ${r.quality}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (results.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                val kbLabel = targetKb.ifBlank { "100" }
                results.forEach { saveToPhone(context, it.bytes, "${it.name}-${kbLabel}kb.jpg", "image/jpeg") }
                AppGraph.library.countToolUse(); message = "Saved to Pictures/Plainly"; nav.afterTask()
            }, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.Download, null); Spacer(Modifier.width(6.dp)); Text("Save")
            }
            OutlinedButton(onClick = { results.firstOrNull()?.let { shareFile(context, it.bytes, "${it.name}-resized.jpg", "image/jpeg") } },
                Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(16.dp), enabled = results.size == 1) {
                Icon(Icons.Rounded.Share, null); Spacer(Modifier.width(6.dp)); Text("Share")
            }
        }
    }
}

@Composable
fun Label(text: String) = Text(text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 10.dp))
