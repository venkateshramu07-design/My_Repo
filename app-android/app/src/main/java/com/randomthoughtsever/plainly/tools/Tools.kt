package com.randomthoughtsever.plainly.tools

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.randomthoughtsever.plainly.ads.BannerAd
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
fun ToolCard(t: ToolEntry, modifier: Modifier = Modifier, onClick: () -> Unit) =
    ToolCard(t.title, t.blurb, t.icon, t.color, modifier, if (t.isNew) "NEW" else if (t.offline) "OFFLINE" else null, onClick)

@Composable
fun ToolCard(t: ToolDef, modifier: Modifier = Modifier, onClick: () -> Unit) = ToolCard(t.title, t.blurb, t.icon, t.color, modifier, null, onClick)

@Composable
fun ToolCard(title: String, blurb: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, badge: String? = null, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .6f))) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.size(46.dp).background(color, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                badge?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = if (it == "NEW") Color(0xFF8A5A12) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.background(if (it == "NEW") Color(0xFFF6E7CB) else MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                            .padding(horizontal = 7.dp, vertical = 2.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, minLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
                overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToolsScreen(nav: Nav) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf("All") }
    var version by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { Catalog.load(context); version++; Catalog.discover(); version++ }
    val all = remember(version) { Catalog.all() }
    val groups = remember(version) { listOf("All") + Catalog.GROUPS.filter { g -> all.any { it.group == g } } }
    val q = query.trim().lowercase()
    val shown = all.filter { t -> (group == "All" || t.group == group) && (q.isEmpty() || t.title.lowercase().contains(q) || t.blurb.lowercase().contains(q)) }

    LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { GridItemSpan(2) }) {
            Column(Modifier.statusBarsPadding().padding(top = 16.dp, start = 4.dp)) {
                Text("Tools", style = MaterialTheme.typography.displaySmall)
                Text("${all.size} tools for photos, PDFs, money, work and travel.", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(50),
                    placeholder = { Text("Search ${all.size} tools") }, leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, "Clear") } })
            }
        }
        item(span = { GridItemSpan(2) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groups) { g -> FilterChip(group == g, { group = g }, label = { Text(g) }, shape = RoundedCornerShape(50)) }
            }
        }
        if (shown.isEmpty()) item(span = { GridItemSpan(2) }) {
            Text("No tool matches \"$query\".", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(24.dp))
        }
        if (group == "All" && q.isEmpty()) {
            Catalog.GROUPS.forEachIndexed { gi, g ->
                val inGroup = shown.filter { it.group == g }
                if (inGroup.isEmpty()) return@forEachIndexed
                item(span = { GridItemSpan(2) }) {
                    Row(Modifier.padding(top = 10.dp, start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(g, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        Text("${inGroup.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(inGroup, key = { it.key }) { t -> ToolCard(t, Modifier.fillMaxWidth()) { t.open(nav) } }
                if (gi == 3) item(span = { GridItemSpan(2) }) { BannerAd("tools:1") }
            }
        } else items(shown, key = { it.key }) { t -> ToolCard(t, Modifier.fillMaxWidth()) { t.open(nav) } }
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
