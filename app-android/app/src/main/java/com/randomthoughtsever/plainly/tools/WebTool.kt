package com.randomthoughtsever.plainly.tools

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.randomthoughtsever.plainly.ads.BannerAd
import com.randomthoughtsever.plainly.data.Blog
import com.randomthoughtsever.plainly.ui.AppGraph
import com.randomthoughtsever.plainly.ui.EmptyState
import com.randomthoughtsever.plainly.ui.Nav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * Runs one of the website's interactive tools inside the app. Only the tool itself is taken from the post (its own
 * markup, styles and scripts); the article, the website's layout, menus and ads are left out. The page gets the app's
 * colours, and file downloads, printing and copying are passed to the phone through a small bridge.
 */
object WebTool {
    const val ORIGIN = "https://" + Blog.HOST

    class Palette(val bg: Int, val surface: Int, val surface2: Int, val ink: Int, val muted: Int, val line: Int, val accent: Int, val dark: Boolean)

    private fun isWidget(e: Element) = e.classNames().any { it == "bd-calc" || it.startsWith("bd-calc") } && e.id().startsWith("bd-")

    /** The tool's HTML page, or null when the post has no tool. [widget] picks one tool when a post has several ("all" keeps every one). */
    fun page(postHtml: String, widget: String, p: Palette): String? {
        val body = Jsoup.parseBodyFragment(postHtml, "$ORIGIN/").body()
        val keep = ArrayList<Element>()
        var found = 0
        fun walk(el: Element) {
            for (c in el.children()) {
                val tag = c.tagName()
                when {
                    tag == "script" && c.attr("type").contains("ld+json") -> Unit
                    tag == "style" || tag == "script" -> keep += c
                    isWidget(c) -> if (widget == "all" || c.id() == widget) { keep += c; found++ }
                    c.select("style,script,[id^=bd-]").isNotEmpty() -> walk(c)
                }
            }
        }
        walk(body)
        if (found == 0) return null
        fun hex(c: Int) = "#%06X".format(0xFFFFFF and c)
        val css = """
:root{--bd-bg:${hex(p.bg)};--bd-surface:${hex(p.surface)};--bd-surface-2:${hex(p.surface2)};--bd-ink:${hex(p.ink)};--bd-muted:${hex(p.muted)};
--bd-line:${hex(p.line)};--bd-accent:${hex(p.accent)};--bd-accent-ink:#fff;--bd-radius:18px;--bd-sans:system-ui,-apple-system,Roboto,sans-serif;
color-scheme:${if (p.dark) "dark" else "light"}}
html,body{margin:0;background:var(--bd-bg);color:var(--bd-ink);font:16px/1.5 var(--bd-sans);-webkit-text-size-adjust:100%;overflow-x:hidden}
body{padding:6px 14px 28px}
.bd-calc{margin:0 0 18px!important;max-width:none!important;box-shadow:none!important}
a{color:var(--bd-accent)}img,canvas,svg{max-width:100%}
input,select,textarea,button{font-size:16px!important}
"""
        // Bridge: hands downloads, printing, copying and new windows to the app (see onMessage).
        val bridge = """
(function(){
 function send(o){try{PlainlyBridge.postMessage(JSON.stringify(o));}catch(e){}}
 function save(href,name){fetch(href).then(function(r){return r.blob();}).then(function(b){var f=new FileReader();
  f.onload=function(){send({t:'file',name:name||'download',mime:b.type||'',data:String(f.result).split(',')[1]||''});};f.readAsDataURL(b);});}
 function grab(a){return a&&(a.hasAttribute('download')||/^(blob|data):/.test(a.href));}
 var click=HTMLAnchorElement.prototype.click;
 HTMLAnchorElement.prototype.click=function(){if(grab(this)){save(this.href,this.getAttribute('download'));return;}return click.call(this);};
 document.addEventListener('click',function(e){var a=e.target&&e.target.closest?e.target.closest('a'):null;
  if(grab(a)){e.preventDefault();save(a.href,a.getAttribute('download'));}},true);
 window.print=function(){send({t:'print'});};
 window.open=function(u){if(u)send({t:'open',url:String(u)});return null;};
 try{Object.defineProperty(navigator,'clipboard',{value:{writeText:function(t){send({t:'copy',text:String(t)});return Promise.resolve();},
  readText:function(){return Promise.resolve('');}}});}catch(e){}
 document.execCommandOrig=document.execCommand;
 document.execCommand=function(c){if(c==='copy'){var s=String(window.getSelection()||'');var a=document.activeElement;
  if(!s&&a&&'value' in a)s=a.value.substring(a.selectionStart,a.selectionEnd)||a.value;send({t:'copy',text:s});return true;}
  return document.execCommandOrig.apply(document,arguments);};
})();
"""
        return "<!doctype html><html${if (p.dark) " class=\"bd-dark\"" else ""}><head><meta charset=\"utf-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><style>$css</style><script>$bridge</script></head><body>" +
            keep.joinToString("\n") { it.outerHtml() } + "</body></html>"
    }

    /** Handles a message from the page's bridge. */
    fun onMessage(context: Context, json: String, nav: Nav, web: WebView?) {
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return
        when (o.optString("t")) {
            "file" -> {
                val bytes = runCatching { Base64.decode(o.optString("data"), Base64.DEFAULT) }.getOrNull() ?: return
                if (bytes.isEmpty() || bytes.size > 60_000_000) return
                var mime = o.optString("mime").ifBlank { "application/octet-stream" }
                val name = o.optString("name").replace(Regex("[^A-Za-z0-9._ -]"), "_").take(80).ifBlank { "plainly-file" }
                if (mime == "application/octet-stream" && name.endsWith(".pdf", true)) mime = "application/pdf"
                if (!mime.matches(Regex("(application/(pdf|zip|json)|image/(png|jpeg|webp|gif|svg\\+xml)|text/(plain|csv|calendar|vcard|html))(;.*)?"))) return
                val clean = mime.substringBefore(';')
                saveToPhone(context, bytes, name, clean)
                AppGraph.library.countToolUse()
                Toast.makeText(context, if (clean.startsWith("image/")) "Saved to Pictures/Plainly" else "Saved to Downloads/Plainly", Toast.LENGTH_SHORT).show()
                nav.afterTask()
            }
            "print" -> web?.let { w ->
                val pm = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                pm.print("Plainly document", w.createPrintDocumentAdapter("Plainly document"), PrintAttributes.Builder().build())
            }
            "copy" -> {
                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Plainly", o.optString("text")))
                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
            }
            "open" -> openLink(Uri.parse(o.optString("url")), nav)
        }
    }

    fun openLink(uri: Uri, nav: Nav) {
        when {
            uri.scheme == "https" && uri.host == Blog.HOST -> nav.blogLink(uri)
            uri.scheme == "https" || uri.scheme == "http" -> nav.web(uri.toString())
            uri.scheme in setOf("mailto", "tel", "sms", "upi") -> nav.external(uri)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebToolScreen(postId: String, widget: String, nav: Nav) {
    val context = LocalContext.current
    val entry = remember(postId, widget) { Catalog.web(widget, postId) }
    val c = MaterialTheme.colorScheme
    val dark = c.background.luminance() < .5f
    val palette = WebTool.Palette(c.background.toArgb(), c.surface.toArgb(), c.surfaceVariant.toArgb(), c.onBackground.toArgb(),
        c.onSurfaceVariant.toArgb(), c.outline.toArgb(), (if (dark) Color(0xFF3B79B5) else c.primary).toArgb(), dark)
    var title by remember { mutableStateOf(entry?.title ?: "Tool") }
    var html by remember { mutableStateOf<String?>(null) }
    var state by remember { mutableIntStateOf(0) } // 0 loading, 1 ready, 2 offline, 3 no tool in this post
    var progress by remember { mutableIntStateOf(0) }
    var webRef by remember { mutableStateOf<WebView?>(null) }
    var pending by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val chooser = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val data = r.data
        val uris = when {
            r.resultCode != Activity.RESULT_OK || data == null -> null
            data.clipData != null -> Array(data.clipData!!.itemCount) { data.clipData!!.getItemAt(it).uri }
            data.data != null -> arrayOf(data.data!!)
            else -> null
        }
        pending?.onReceiveValue(uris); pending = null
    }

    LaunchedEffect(postId, widget, dark) {
        state = 0
        val post = AppGraph.repo.post(postId)
        if (post == null) { state = 2; return@LaunchedEffect }
        if (entry == null) title = post.title.substringBefore(":").substringBefore(" (").trim()
        html = withContext(Dispatchers.Default) { WebTool.page(post.html, widget, palette) }
        state = if (html == null) 3 else 1
    }
    DisposableEffect(Unit) { onDispose { pending?.onReceiveValue(null); webRef?.destroy() } }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            entry?.let { e ->
                Box(Modifier.size(34.dp).background(e.color, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                    Icon(e.icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            IconButton(onClick = { nav.article(postId) }) { Icon(Icons.AutoMirrored.Rounded.MenuBook, "Read the guide") }
        }
        if (state == 1 && progress < 100) LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().height(2.dp))
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (state) {
                0 -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                2 -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    EmptyState(Icons.Rounded.CloudOff, "This tool needs the internet", "Connect and try again. The app's own tools work offline.")
                }
                3 -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("This guide doesn't have an interactive tool.", style = MaterialTheme.typography.bodyLarge)
                    FilledTonalButton(onClick = { nav.article(postId) }, modifier = Modifier.padding(top = 12.dp)) { Text("Read the guide") }
                }
                else -> AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
                    WebView(ctx).apply {
                        webRef = this
                        setBackgroundColor(palette.bg)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true // some tools remember your list (habit tracker, subscriptions) on the phone
                        settings.allowFileAccess = false
                        settings.allowContentAccess = true // lets the page read the files you pick
                        settings.setGeolocationEnabled(false)
                        settings.setSupportMultipleWindows(false)
                        settings.mediaPlaybackRequiresUserGesture = true
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                            WebViewCompat.addWebMessageListener(this, "PlainlyBridge", setOf(WebTool.ORIGIN)) { _, msg, origin, isMain, _ ->
                                if (isMain && origin.toString().trimEnd('/') == WebTool.ORIGIN) msg.data?.let { WebTool.onMessage(context, it, nav, this) }
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                if (request.isForMainFrame) WebTool.openLink(request.url, nav)
                                return request.isForMainFrame
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView, p: Int) { progress = p }
                            override fun onShowFileChooser(view: WebView, cb: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
                                pending?.onReceiveValue(null); pending = cb
                                val intent = params.createIntent().apply {
                                    if (params.mode == FileChooserParams.MODE_OPEN_MULTIPLE) putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                }
                                return runCatching { chooser.launch(intent); true }.getOrElse { pending = null; false }
                            }
                        }
                        loadDataWithBaseURL("${WebTool.ORIGIN}/app/tool/${entry?.widget ?: postId}", html!!, "text/html", "utf-8", null)
                    }
                })
            }
        }
        if (state == 1) Box(Modifier.navigationBarsPadding()) { BannerAd("webtool:$postId", maxHeightDp = 60) }
    }
}
