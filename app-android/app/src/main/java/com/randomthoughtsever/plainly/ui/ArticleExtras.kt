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
