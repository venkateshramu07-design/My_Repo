package com.randomthoughtsever.plainly.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.rounded.Close
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.randomthoughtsever.plainly.data.Post
import com.randomthoughtsever.plainly.data.Quiz

/** The text-selection menu inside articles: Copy, plus Save to notebook. Shown by [NotePopup]. */
class NoteToolbar : TextToolbar {
    var rect by mutableStateOf<Rect?>(null); private set
    private var copy: (() -> Unit)? = null
    override val status: TextToolbarStatus get() = if (rect != null) TextToolbarStatus.Shown else TextToolbarStatus.Hidden
    override fun hide() { rect = null }
    override fun showMenu(rect: Rect, onCopyRequested: (() -> Unit)?, onPasteRequested: (() -> Unit)?, onCutRequested: (() -> Unit)?, onSelectAllRequested: (() -> Unit)?) {
        copy = onCopyRequested; this.rect = rect
    }
    fun doCopy() = copy?.invoke()
}

@Composable
fun NotePopup(bar: NoteToolbar, save: (String) -> Unit) {
    val r = bar.rect ?: return
    val context = LocalContext.current
    Popup(offset = IntOffset(r.left.toInt().coerceAtLeast(16), (r.top - 170).toInt().coerceAtLeast(40)), properties = PopupProperties(focusable = false),
        onDismissRequest = { bar.hide() }) {
        Surface(shape = RoundedCornerShape(50), color = NavyDeep, shadowElevation = 8.dp) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                Surface(onClick = { bar.doCopy(); bar.hide() }, shape = RoundedCornerShape(50), color = androidx.compose.ui.graphics.Color.Transparent) {
                    Text("Copy", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                }
                Surface(onClick = {
                    bar.doCopy()
                    val text = (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
                    if (text.isNotBlank()) save(text)
                    bar.hide()
                }, shape = RoundedCornerShape(50), color = Gold) {
                    Text("📓 Save to notebook", color = NavyDeep, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                }
            }
        }
    }
}

/** After the article: test yourself, say you'll try it, and see the guide's card. */
@Composable
fun EndOfGuide(p: Post, quiz: Quiz?, nav: Nav, changed: () -> Unit) {
    val pr = AppGraph.progress
    pr.version
    Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (quiz != null) QuizCard(quiz, nav, onAnswered = changed)
        val trying = pr.isTrying(p.id)
        Surface(onClick = { if (!trying) pr.startTrying(p.id, p.title) }, shape = RoundedCornerShape(22.dp),
            color = if (trying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp)) {
                Text(if (trying) "✅" else "💪", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.width(12.dp))
                Column {
                    Text(if (trying) "You're trying this" else "I'm going to try this", style = MaterialTheme.typography.titleMedium)
                    Text(if (trying) "We'll ask in a few days whether it worked." else "Commit to one idea from this guide. We'll check in with you in 3 days.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        pr.card(p.id)?.let { c ->
            Surface(onClick = { nav.cards() }, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp)) {
                    Text("🃏", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.width(12.dp))
                    Column {
                        Text("${com.randomthoughtsever.plainly.data.Progress.RARITY[c.rarity]} card collected", style = MaterialTheme.typography.titleMedium)
                        Text(if (c.rarity < 2) "Get every quick check and the quiz right to make it Legendary." else "Top rarity. See your collection.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ---------- translation ----------

/** One article's translation: which language, progress, and the translated title and blocks. */
class ArticleTranslation {
    var wanted by mutableStateOf<String?>(null); private set
    var lang by mutableStateOf<String?>(null); private set
    var title by mutableStateOf<String?>(null); private set
    var blocks by mutableStateOf<List<com.randomthoughtsever.plainly.ui.Block>?>(null); private set
    var status by mutableStateOf<String?>(null); private set
    var failed by mutableStateOf(false); private set
    val rtl get() = com.randomthoughtsever.plainly.data.Translate.lang(lang)?.rtl == true

    /** Ask for a language (null = show the original). The article screen then calls [run]. */
    fun request(code: String?) { wanted = code; failed = false; if (code == null) { lang = null; title = null; blocks = null; status = null } }

    suspend fun run(originalTitle: String, original: List<com.randomthoughtsever.plainly.ui.Block>) {
        val code = wanted ?: return
        if (code == lang && blocks != null) return
        val T = com.randomthoughtsever.plainly.data.Translate
        val name = T.lang(code)?.native ?: code
        runCatching {
            val e = T.ensureEngine(AppGraph.appContext) { status = it }
            if (!e.isDownloaded(code)) { status = "Downloading $name for your phone (about 30 MB, only once)…"; e.download(code) }
            status = "Translating to $name…"
            val t = T.blocks(original, { T.text(e, code, it) }) { i, n -> status = "Translating to $name… ${if (n == 0) 0 else i * 100 / n}%" }
            title = T.text(e, code, originalTitle); blocks = t; lang = code; status = null
            AppGraph.progress.award("translate:$code", 10, "Reading in $name")
        }.onFailure { status = null; failed = true; wanted = null }
    }
}

/** Above the article: progress, "translated · show original", or a one-tap suggestion in the phone's language. */
@Composable
fun TranslateBar(tr: ArticleTranslation, onPick: () -> Unit, translate: (String?) -> Unit) {
    val context = LocalContext.current
    val T = com.randomthoughtsever.plainly.data.Translate
    val device = remember { T.deviceLang() }
    var hideSuggestion by remember { mutableStateOf(T.suggestionDismissed(context)) }
    val c = MaterialTheme.colorScheme
    @Composable fun bar(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) = Surface(shape = RoundedCornerShape(18.dp), color = c.primaryContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) { Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { content() } }
    when {
        tr.status != null -> bar {
            androidx.compose.material3.CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp); Spacer(Modifier.width(10.dp))
            Text(tr.status!!, style = MaterialTheme.typography.bodyMedium)
        }
        tr.lang != null -> bar {
            Text("🌐", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
            Text("Translated to ${T.lang(tr.lang)?.native} by your phone. Machine translation may contain mistakes.", style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f))
            androidx.compose.material3.TextButton(onClick = { translate(null) }) { Text("Original") }
        }
        tr.failed -> bar {
            Text("Couldn't translate right now. Check your connection (the first time downloads the language).", style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f))
            androidx.compose.material3.TextButton(onClick = onPick) { Text("Retry") }
        }
        device != null && !hideSuggestion -> bar {
            Text("🌐", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
            Text("Read this in ${device.native}?", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            androidx.compose.material3.TextButton(onClick = { translate(device.code) }) { Text("Translate") }
            androidx.compose.material3.IconButton(onClick = { hideSuggestion = true; T.dismissSuggestion(context) }) {
                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Rounded.Close, "No thanks")
            }
        }
    }
}

/** Pick a language for articles (remembered), or English (the original). Downloaded languages are marked. */
@Composable
fun LanguagePicker(current: String?, done: (String?) -> Unit) {
    val T = com.randomthoughtsever.plainly.data.Translate
    val have by androidx.compose.runtime.produceState(emptySet<String>()) { value = T.downloaded().map { it.code }.toSet() }
    androidx.compose.material3.AlertDialog(onDismissRequest = { done(null) }, title = { Text("Read articles in") }, text = {
        Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
            Text("Translated on your phone. Each language downloads once (about 30 MB), then works offline.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            (listOf(com.randomthoughtsever.plainly.data.Translate.Lang("", "English (original)", "English")) + T.LANGS).forEach { l ->
                val on = (current ?: "") == l.code
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (on) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { done(l.code) }.padding(horizontal = 10.dp, vertical = 11.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(l.native, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    if (l.code.isNotBlank() && l.english != l.native) Text(l.english, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (l.code in have) Text("  ✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }, confirmButton = {}, dismissButton = { androidx.compose.material3.TextButton(onClick = { done(null) }) { Text("Cancel") } })
}
