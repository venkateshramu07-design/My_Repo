package com.randomthoughtsever.plainly.tools

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.randomthoughtsever.plainly.ui.AppGraph
import com.randomthoughtsever.plainly.ui.Nav
import java.io.ByteArrayOutputStream

object QrEngine {
    /** Draws a QR code with a quiet zone; each module is [scale] pixels. */
    fun bitmap(text: String, fg: Int, scale: Int = 16): Bitmap {
        val m = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0,
            mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M, EncodeHintType.MARGIN to 0, EncodeHintType.CHARACTER_SET to "UTF-8"))
        val quiet = 4
        val n = m.width + quiet * 2
        val px = IntArray(n * n * scale * scale) { android.graphics.Color.WHITE }
        val side = n * scale
        for (y in 0 until m.height) for (x in 0 until m.width) if (m.get(x, y)) {
            for (dy in 0 until scale) {
                val row = ((y + quiet) * scale + dy) * side + (x + quiet) * scale
                java.util.Arrays.fill(px, row, row + scale, fg)
            }
        }
        return Bitmap.createBitmap(px, side, side, Bitmap.Config.ARGB_8888)
    }

    fun png(b: Bitmap): ByteArray = ByteArrayOutputStream().also { b.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()

    private fun esc(s: String) = s.replace(Regex("([\\\\;,:\"])"), "\\\\$1")
    fun wifi(ssid: String, password: String, security: String, hidden: Boolean): String =
        "WIFI:T:${if (security == "None") "nopass" else security};S:${esc(ssid)};" + (if (security == "None") "" else "P:${esc(password)};") +
            (if (hidden) "H:true;" else "") + ";"
}

private val INK = listOf(Color(0xFF12304F), Color(0xFF111111), Color(0xFF0F7D6C), Color(0xFF8E3B6E), Color(0xFFB0472A))

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QrScreen(nav: Nav) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(0) }
    var text by remember { mutableStateOf("") }
    var ssid by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var security by remember { mutableStateOf("WPA") }
    var hidden by remember { mutableStateOf(false) }
    var ink by remember { mutableStateOf(INK.first()) }
    var message by remember { mutableStateOf<String?>(null) }

    val payload = when (mode) {
        0 -> text.trim().let { if (it.isNotEmpty() && !it.contains("://") && Regex("^[\\w-]+(\\.[\\w-]+)+(/.*)?$").matches(it)) "https://$it" else it }
        1 -> text.trim()
        else -> if (ssid.isBlank()) "" else QrEngine.wifi(ssid.trim(), pass, security, hidden)
    }
    val qr = remember(payload, ink) { if (payload.isEmpty()) null else runCatching { QrEngine.bitmap(payload, ink.toArgb()) }.getOrNull() }

    ToolScaffold("qr", nav) {
        val modes = listOf("Link", "Text", "Wi-Fi")
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            modes.forEachIndexed { i, l -> SegmentedButton(mode == i, { mode = i; message = null }, SegmentedButtonDefaults.itemShape(i, modes.size)) { Text(l) } }
        }
        when (mode) {
            0 -> OutlinedTextField(text, { text = it }, label = { Text("Website address") }, placeholder = { Text("example.com") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            1 -> OutlinedTextField(text, { text = it.take(1200) }, label = { Text("Any text") }, minLines = 3,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            else -> {
                OutlinedTextField(ssid, { ssid = it }, label = { Text("Network name (SSID)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("WPA" to "WPA/WPA2/WPA3", "WEP" to "WEP", "None" to "Open").forEach { (k, l) -> FilterChip(security == k, { security = k }, label = { Text(l) }) }
                }
                AnimatedVisibility(security != "None") {
                    OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Hidden network", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f)); Switch(hidden, { hidden = it })
                }
            }
        }
        Label("Colour")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            INK.forEach { c ->
                Surface(onClick = { ink = c }, shape = RoundedCornerShape(50), color = c, modifier = Modifier.size(38.dp),
                    border = if (ink == c) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primaryContainer) else null) {}
            }
        }
        Surface(shape = RoundedCornerShape(28.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                if (qr != null) Image(qr.asImageBitmap(), "QR code", Modifier.size(240.dp), filterQuality = FilterQuality.None)
                else Box(Modifier.size(240.dp).background(Color(0xFFF1EFEA), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                    Text("Your code appears here", color = Color(0xFF6B7380))
                }
            }
        }
        Text("Plain QR codes never expire and don't track who scans them.", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                val b = qr ?: return@Button
                saveToPhone(context, QrEngine.png(b), "plainly-qr-${System.currentTimeMillis() / 1000}.png", "image/png")
                AppGraph.library.countToolUse(); message = "Saved to Pictures/Plainly"; nav.maybeAskForReview()
            }, Modifier.weight(1f).height(50.dp), enabled = qr != null, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.Download, null); Spacer(Modifier.width(6.dp)); Text("Save")
            }
            OutlinedButton(onClick = { qr?.let { shareFile(context, QrEngine.png(it), "qr-code.png", "image/png") } },
                Modifier.weight(1f).height(50.dp), enabled = qr != null, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.Share, null); Spacer(Modifier.width(6.dp)); Text("Share")
            }
        }
    }
}
