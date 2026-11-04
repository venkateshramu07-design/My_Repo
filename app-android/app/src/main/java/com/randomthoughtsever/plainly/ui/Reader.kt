package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.randomthoughtsever.plainly.data.Blog
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/** An article broken into native blocks. Nothing from the website (scripts, styles, widgets) is ever run or shown. */
sealed interface Block {
    data class Heading(val level: Int, val text: AnnotatedString) : Block
    data class Para(val text: AnnotatedString) : Block
    data class QuickAnswer(val text: AnnotatedString) : Block
    data class Bullets(val ordered: Boolean, val items: List<Pair<Int, AnnotatedString>>) : Block
    data class Quote(val text: AnnotatedString) : Block
    data class Image(val src: String, val caption: String?) : Block
    data class Table(val header: List<AnnotatedString>?, val rows: List<List<AnnotatedString>>) : Block
    data class Faq(val question: String, val answer: List<Block>) : Block
    data object Divider : Block
}

object Reader {
    private val SKIP_TAGS = setOf("script", "style", "noscript", "iframe", "form", "button", "select", "textarea", "canvas", "input", "label",
        "option", "datalist", "svg", "object", "embed", "video", "audio", "link", "meta", "head", "title", "nav")
    private val SKIP_CLASS = Regex("(^|\\s)(bd-cite|seo-toc|bd-calc\\S*|bt-\\S+|pp-\\S+|ik-\\S+|pk-\\S+|bc-\\S+|med-\\S+|mfa-\\S+|qr-f|sv-f|bd-cf|bd-pp-\\S+|\\S*share\\S*|\\S*embed\\S*|tool-card)(\\s|$)")
    private val BLOCK_TAGS = setOf("p", "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "table", "blockquote", "figure", "details", "hr", "div", "section",
        "article", "header", "footer", "aside", "main", "img", "pre", "dl")

    fun parse(html: String): List<Block> {
        val body = Jsoup.parseBodyFragment(html, Blog.BASE + "/").body()
        val out = ArrayList<Block>()
        var coverSkipped = false
        fun skip(e: Element) = e.tagName() in SKIP_TAGS || SKIP_CLASS.containsMatchIn(e.className()) || e.attr("hidden").isNotEmpty() ||
            e.attr("style").replace(" ", "").contains("display:none")

        fun image(e: Element): Block? {
            val src = e.absUrl("src").ifEmpty { e.attr("src") }
            if (!src.startsWith("https://") || src.contains("emoji")) return null
            if (!coverSkipped && src.contains("/seo/covers/")) { coverSkipped = true; return null }
            return Block.Image(src, null) // alt text on the blog is written for search engines, so it isn't shown as a caption
        }

        fun walk(parent: Element, sink: MutableList<Block>) {
            val loose = InlineBuilder()
            fun flush() { loose.build()?.let { sink += Block.Para(it) } ; loose.reset() }
            for (n in parent.childNodes()) {
                if (n is TextNode) { loose.text(n); continue }
                val e = n as? Element ?: continue
                if (skip(e)) continue
                val tag = e.tagName()
                if (tag !in BLOCK_TAGS) {
                    // Inline element sitting directly in a container: collect it into a paragraph,
                    // unless it secretly holds block content (e.g. <span><p>…</p></span>).
                    if (e.children().any { it.tagName() in BLOCK_TAGS }) { flush(); walk(e, sink) } else loose.node(e)
                    continue
                }
                flush()
                when (tag) {
                    "h1", "h2" -> inline(e)?.let { sink += Block.Heading(2, it) }
                    "h3", "h4", "h5", "h6" -> inline(e)?.let { sink += Block.Heading(3, it) }
                    "p" -> {
                        val imgs = e.select("img")
                        if (imgs.isNotEmpty() && e.text().isBlank()) imgs.forEach { img -> image(img)?.let { sink += it } }
                        else if (sink.isEmpty() && e.text().trim().startsWith("Updated") && e.text().length < 60) Unit
                        else inline(e)?.let { t ->
                            val quick = Regex("^\\s*Quick answer:?\\s*", RegexOption.IGNORE_CASE)
                            if (quick.containsMatchIn(t.text)) sink += Block.QuickAnswer(t.subSequence(quick.find(t.text)!!.range.last + 1, t.length))
                            else sink += Block.Para(t)
                        }
                    }
                    "ul", "ol" -> list(e, tag == "ol").takeIf { it.isNotEmpty() }?.let { sink += Block.Bullets(tag == "ol", it) }
                    "blockquote" -> inline(e)?.let { sink += Block.Quote(it) }
                    "img" -> image(e)?.let { sink += it }
                    "figure" -> {
                        e.selectFirst("img")?.let { img -> image(img) }?.let { b ->
                            val cap = e.selectFirst("figcaption")?.text()?.takeIf { it.isNotBlank() }
                            sink += (b as Block.Image).copy(caption = cap)
                        }
                    }
                    "table" -> table(e)?.let { sink += it }
                    "details" -> {
                        val q = e.selectFirst("summary")?.text()?.trim().orEmpty()
                        val answer = ArrayList<Block>()
                        val copy = e.clone().also { it.select("summary").remove() }
                        walk(copy, answer)
                        if (q.isNotEmpty()) sink += Block.Faq(q, answer)
                    }
                    "hr" -> if (sink.lastOrNull() !is Block.Divider) sink += Block.Divider
                    "pre" -> e.text().takeIf { it.isNotBlank() }?.let { sink += Block.Para(AnnotatedString(it)) }
                    else -> walk(e, sink)
                }
            }
            flush()
        }
        walk(body, out)
        return out
    }

    private fun list(e: Element, ordered: Boolean, depth: Int = 0): List<Pair<Int, AnnotatedString>> {
        val items = ArrayList<Pair<Int, AnnotatedString>>()
        for (li in e.children()) {
            if (li.tagName() != "li") continue
            val nested = li.children().filter { it.tagName() == "ul" || it.tagName() == "ol" }
            val copy = li.clone().also { c -> c.children().filter { it.tagName() == "ul" || it.tagName() == "ol" }.forEach { it.remove() } }
            inline(copy)?.let { items += depth to it }
            nested.forEach { items += list(it, ordered, depth + 1) }
        }
        return items
    }

    private fun table(e: Element): Block.Table? {
        val rows = e.select("tr").filter { tr -> tr.parents().firstOrNull { it.tagName() == "table" } == e }
        if (rows.isEmpty()) return null
        val cells = rows.map { tr -> tr.children().filter { it.tagName() == "td" || it.tagName() == "th" }.map { inline(it) ?: AnnotatedString("") } }
            .filter { it.isNotEmpty() }
        val headed = rows.first().children().all { it.tagName() == "th" } && cells.size > 1
        return Block.Table(if (headed) cells.first() else null, if (headed) cells.drop(1) else cells)
    }

    fun inline(e: Element): AnnotatedString? = InlineBuilder().apply { e.childNodes().forEach { node(it) } }.build()

    private class InlineBuilder {
        private var b = AnnotatedString.Builder()
        private var lastSpace = true
        fun reset() { b = AnnotatedString.Builder(); lastSpace = true }

        fun text(t: TextNode) = append(t.wholeText)
        private fun append(raw: String) {
            val s = raw.replace(Regex("\\s+"), " ").replace(' ', ' ')
            if (s.isEmpty()) return
            val t = if (lastSpace) s.trimStart() else s
            if (t.isEmpty()) return
            b.append(t); lastSpace = t.endsWith(" ")
        }

        fun node(n: Node) {
            if (n is TextNode) { text(n); return }
            val e = n as? Element ?: return
            if (e.tagName() in SKIP_TAGS || SKIP_CLASS.containsMatchIn(e.className())) return
            when (e.tagName()) {
                "br" -> { b.append("\n"); lastSpace = true }
                "strong", "b" -> b.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { kids(e) }
                "em", "i", "cite" -> b.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { kids(e) }
                "u" -> b.withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { kids(e) }
                "code", "kbd" -> b.withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { kids(e) }
                "sup" -> b.withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = .7.em)) { kids(e) }
                "sub" -> b.withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = .7.em)) { kids(e) }
                "small" -> b.withStyle(SpanStyle(fontSize = .85.em)) { kids(e) }
                "img" -> Unit
                "a" -> {
                    val href = e.absUrl("href").ifEmpty { e.attr("href") }
                    if (href.isEmpty() || href.startsWith("#") || href.startsWith("javascript:")) kids(e)
                    else b.withLink(LinkAnnotation.Url(href)) { kids(e) }
                }
                else -> kids(e)
            }
        }
        private fun kids(e: Element) = e.childNodes().forEach { node(it) }

        fun build(): AnnotatedString? {
            val s = b.toAnnotatedString()
            val end = s.text.trimEnd().length
            val start = s.text.length - s.text.trimStart().length
            return if (end <= start) null else s.subSequence(start, end)
        }
    }
}

// ---------- Rendering ----------

@Composable
fun ArticleBlocks(blocks: List<Block>, zoom: Int, onLink: (String) -> Unit) {
    val c = MaterialTheme.colorScheme
    val k = zoom / 100f
    fun TextStyle.z() = copy(fontSize = fontSize * k, lineHeight = if (lineHeight == TextUnit.Unspecified) lineHeight else lineHeight * k)
    val body = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 28.sp, color = c.onBackground).z()
    val links = TextLinkStyles(SpanStyle(color = c.primary, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline))

    val handler = remember(onLink) { object : UriHandler { override fun openUri(uri: String) = onLink(uri) } }
    CompositionLocalProvider(LocalUriHandler provides handler) {
        SelectionContainer {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
                blocks.forEachIndexed { i, b -> RenderBlock(b, i, body, links) }
            }
        }
    }
}

private fun AnnotatedString.withLinkStyles(styles: TextLinkStyles): AnnotatedString = buildAnnotatedString {
    append(this@withLinkStyles.text)
    this@withLinkStyles.spanStyles.forEach { addStyle(it.item, it.start, it.end) }
    this@withLinkStyles.getLinkAnnotations(0, length).forEach { r ->
        val l = r.item
        if (l is LinkAnnotation.Url) addLink(LinkAnnotation.Url(l.url, styles), r.start, r.end)
    }
}

@Composable
private fun RenderBlock(b: Block, index: Int, body: TextStyle, links: TextLinkStyles) {
    val c = MaterialTheme.colorScheme
    fun AnnotatedString.s() = withLinkStyles(links)
    when (b) {
        is Block.Heading -> Text(b.text.s(), style = if (b.level == 2)
            MaterialTheme.typography.headlineSmall.copy(fontSize = body.fontSize * 1.38f, lineHeight = body.fontSize * 1.75f, color = c.onBackground)
        else MaterialTheme.typography.titleLarge.copy(fontSize = body.fontSize * 1.08f, lineHeight = body.fontSize * 1.5f, color = c.onBackground),
            modifier = Modifier.padding(top = if (index == 0) 4.dp else if (b.level == 2) 26.dp else 18.dp, bottom = 8.dp))
        is Block.Para -> Text(b.text.s(), style = body, modifier = Modifier.padding(bottom = 16.dp))
        is Block.QuickAnswer -> Surface(shape = RoundedCornerShape(22.dp), color = c.primaryContainer, modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 20.dp)) {
            Row(Modifier.height(IntrinsicSize.Min)) {
                Box(Modifier.width(5.dp).fillMaxHeight().background(c.secondary))
                Column(Modifier.padding(18.dp)) {
                    Text("QUICK ANSWER", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.6.sp), color = c.secondary, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(6.dp))
                    Text(b.text.s(), style = body.copy(color = c.onPrimaryContainer))
                }
            }
        }
        is Block.Bullets -> Column(Modifier.padding(bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            var n = 0
            b.items.forEach { (depth, t) ->
                if (depth == 0) n++
                Row(Modifier.padding(start = (depth * 20).dp)) {
                    if (b.ordered && depth == 0) Box(Modifier.padding(top = 2.dp).size(24.dp).background(c.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Text("$n", style = MaterialTheme.typography.labelMedium, color = c.primary, fontWeight = FontWeight.ExtraBold)
                    } else Box(Modifier.padding(top = (body.lineHeight.value / 2 - 3).coerceAtLeast(6f).dp, start = 6.dp, end = 6.dp)
                        .size(7.dp).background(if (depth == 0) c.secondary else c.outline, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(t.s(), style = body, modifier = Modifier.weight(1f))
                }
            }
        }
        is Block.Quote -> Row(Modifier.padding(vertical = 8.dp).height(IntrinsicSize.Min)) {
            Box(Modifier.width(3.dp).fillMaxHeight().background(c.secondary, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(14.dp))
            Text(b.text.s(), style = body.copy(fontStyle = FontStyle.Italic, color = c.onSurfaceVariant), modifier = Modifier.padding(bottom = 8.dp))
        }
        is Block.Image -> Column(Modifier.padding(vertical = 10.dp)) {
            AsyncImage(b.src, b.caption, Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surfaceVariant), contentScale = ContentScale.FillWidth)
            b.caption?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp)) }
        }
        is Block.Table -> TableBlock(b, body, links)
        is Block.Faq -> FaqBlock(b, body, links)
        Block.Divider -> HorizontalDivider(Modifier.padding(vertical = 22.dp), color = c.outline)
    }
}

@Composable
private fun TableBlock(t: Block.Table, body: TextStyle, links: TextLinkStyles) {
    val c = MaterialTheme.colorScheme
    val cols = maxOf(t.header?.size ?: 0, t.rows.maxOfOrNull { it.size } ?: 0)
    val small = body.copy(fontSize = body.fontSize * .84f, lineHeight = body.fontSize * 1.25f)
    val width = if (cols <= 2) 158.dp else 140.dp
    Surface(shape = RoundedCornerShape(18.dp), color = c.surface, border = BorderStroke(1.dp, c.outline), modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            t.header?.let { h ->
                Row(Modifier.background(c.primaryContainer)) {
                    (0 until cols).forEach { i ->
                        Text(h.getOrNull(i)?.withLinkStyles(links) ?: AnnotatedString(""), style = small.copy(fontWeight = FontWeight.Bold, color = c.onPrimaryContainer),
                            modifier = Modifier.width(width).padding(12.dp))
                    }
                }
            }
            t.rows.forEachIndexed { r, row ->
                Row(Modifier.background(if (r % 2 == 1) c.surfaceVariant.copy(alpha = .5f) else c.surface).height(IntrinsicSize.Min)) {
                    (0 until cols).forEach { i ->
                        Text(row.getOrNull(i)?.withLinkStyles(links) ?: AnnotatedString(""),
                            style = if (i == 0) small.copy(fontWeight = FontWeight.Bold) else small, modifier = Modifier.width(width).padding(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqBlock(f: Block.Faq, body: TextStyle, links: TextLinkStyles) {
    val c = MaterialTheme.colorScheme
    var open by rememberSaveable(f.question) { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, label = "faq")
    Surface(onClick = { open = !open }, shape = RoundedCornerShape(18.dp), color = c.surfaceVariant.copy(alpha = .7f),
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Column(Modifier.animateContentSize().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(f.question, style = MaterialTheme.typography.titleMedium.copy(fontSize = body.fontSize * .96f), modifier = Modifier.weight(1f))
                Icon(Icons.Rounded.ExpandMore, null, Modifier.rotate(turn), tint = c.primary)
            }
            if (open) {
                Spacer(Modifier.height(8.dp))
                f.answer.forEachIndexed { i, b -> RenderBlock(b, i + 1, body.copy(fontSize = body.fontSize * .94f), links) }
            }
        }
    }
}
