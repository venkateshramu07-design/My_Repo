package com.randomthoughtsever.plainly.tools

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.HomeWork
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PhotoSizeSelectLarge
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.randomthoughtsever.plainly.ui.Nav
import java.io.File

data class ToolDef(val id: String, val title: String, val blurb: String, val icon: ImageVector, val color: Color, val group: String)

val AllTools = listOf(
    ToolDef("photo", "Photo resizer", "Shrink any photo to an exact KB size", Icons.Rounded.PhotoSizeSelectLarge, Color(0xFF1F6FB2), "Files"),
    ToolDef("pdf", "PDF compressor", "Make a PDF fit an upload limit", Icons.Rounded.PictureAsPdf, Color(0xFFC0392B), "Files"),
    ToolDef("qr", "QR code maker", "Links, text and Wi-Fi. Never expires", Icons.Rounded.QrCode2, Color(0xFF2E7D5B), "Files"),
    ToolDef("loan", "Loan & mortgage calculator", "Payment, interest and overpayments", Icons.Rounded.HomeWork, Color(0xFF12304F), "Money"),
    ToolDef("growth", "Investment growth calculator", "Monthly investing over time", Icons.Rounded.Savings, Color(0xFF0F7D6C), "Money"),
    ToolDef("card", "Credit card payoff calculator", "When you'll be debt-free", Icons.Rounded.CreditCard, Color(0xFF8E3B6E), "Money"),
    ToolDef("fire", "FIRE calculator", "Your financial independence number", Icons.Rounded.LocalFireDepartment, Color(0xFFC0602A), "Money"),
    ToolDef("emergency", "Emergency fund calculator", "How much cushion you need", Icons.Rounded.AccountBalanceWallet, Color(0xFF5B4BB5), "Money"),
)

/** The native tool that matches an article, so its web widget can be replaced by a button. */
fun toolFor(path: String): ToolDef? {
    val p = path.lowercase()
    val id = when {
        Regex("compress-pdf|jpg-to-pdf|merge-split|pdf").containsMatchIn(p) -> "pdf"
        Regex("resize-(image|photo)|signature|passport|photo-size|reduce-image|heic|exam-photo|visa-photo|kb-to-mb").containsMatchIn(p) -> "photo"
        p.contains("qr-code") -> "qr"
        Regex("credit-card-payoff|credit-card-debt|pay-off-credit").containsMatchIn(p) -> "card"
        Regex("emi-calculator|mortgage|home-loan|car-loan|afford-car|loan-or-invest|buy-house").containsMatchIn(p) -> "loan"
        Regex("sip-calculator|lump-sum|investment|retire-early|compound").containsMatchIn(p) && !p.contains("fire") -> "growth"
        p.contains("fire") -> "fire"
        p.contains("emergency-fund") -> "emergency"
        else -> null
    }
    return AllTools.firstOrNull { it.id == id }
}

@Composable
fun QuickTools(nav: Nav) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(AllTools) { t -> ToolCard(t, Modifier.width(150.dp)) { nav.tool(t.id) } }
    }
}

@Composable
fun ToolCard(t: ToolDef, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .6f))) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.size(46.dp).background(t.color, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                Icon(t.icon, null, tint = Color.White)
            }
            Spacer(Modifier.height(14.dp))
            Text(t.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(t.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
                overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun ToolsScreen(nav: Nav) {
    LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { GridItemSpan(2) }) {
            Column(Modifier.statusBarsPadding().padding(top = 16.dp, start = 4.dp)) {
                Text("Tools", style = MaterialTheme.typography.displaySmall)
                Text("Everything runs on your phone. Files are never uploaded.", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        for (group in listOf("Files", "Money")) {
            item(span = { GridItemSpan(2) }) {
                Text(if (group == "Files") "Photos, PDFs & QR codes" else "Money calculators", style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 14.dp, start = 4.dp))
            }
            items(AllTools.filter { it.group == group }) { t -> ToolCard(t, Modifier.fillMaxWidth()) { nav.tool(t.id) } }
        }
    }
}

/** Saves bytes to the phone's public Pictures or Downloads folder (no storage permission needed) and returns its address. */
fun saveToPhone(context: Context, bytes: ByteArray, name: String, mime: String): Uri? {
    val image = mime.startsWith("image/")
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, mime)
        put(MediaStore.MediaColumns.RELATIVE_PATH, if (image) "${Environment.DIRECTORY_PICTURES}/Plainly" else "${Environment.DIRECTORY_DOWNLOADS}/Plainly")
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }
    val collection = if (image) MediaStore.Images.Media.EXTERNAL_CONTENT_URI else MediaStore.Downloads.EXTERNAL_CONTENT_URI
    val uri = context.contentResolver.insert(collection, values) ?: return null
    context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
    values.clear(); values.put(MediaStore.MediaColumns.IS_PENDING, 0)
    context.contentResolver.update(uri, values, null, null)
    return uri
}

/** Shares a file made in the app (written to the app's cache and offered through a private FileProvider address). */
fun shareFile(context: Context, bytes: ByteArray, name: String, mime: String) {
    val dir = File(context.cacheDir, "share").apply { mkdirs() }
    val f = File(dir, name).apply { writeBytes(bytes) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", f)
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), null))
}

fun kb(bytes: Long): String = if (bytes >= 1_000_000) "%.2f MB".format(bytes / 1_000_000.0) else "%.0f KB".format(bytes / 1000.0)

/** Shared frame for every tool: back button, coloured header and a scrolling form. */
@Composable
fun ToolScaffold(id: String, nav: Nav, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val t = AllTools.firstOrNull { it.id == id } ?: AllTools.first()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()) {
        Box(Modifier.fillMaxWidth().background(
            Brush.verticalGradient(listOf(t.color.copy(alpha = .22f), MaterialTheme.colorScheme.background)))) {
            Column(Modifier.statusBarsPadding().padding(start = 8.dp, end = 20.dp, top = 4.dp, bottom = 8.dp)) {
                IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Row(Modifier.padding(start = 12.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).background(t.color, RoundedCornerShape(17.dp)), contentAlignment = Alignment.Center) {
                        Icon(t.icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(t.title, style = MaterialTheme.typography.headlineSmall)
                        Text(t.blurb, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
