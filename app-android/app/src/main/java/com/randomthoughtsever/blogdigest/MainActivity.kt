package com.randomthoughtsever.blogdigest

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        const val HOST = "randomthoughtsever.blogspot.com"
        const val ORIGIN = "https://$HOST"
        const val HOME = "$ORIGIN/"
        private val TABS = mapOf(
            R.id.nav_home to HOME,
            R.id.nav_money to "$ORIGIN/search/label/Personal%20Finance",
            R.id.nav_mind to "$ORIGIN/search/label/Psychology",
            R.id.nav_career to "$ORIGIN/search/label/Career%20%26%20Workplace",
            R.id.nav_tools to "$ORIGIN/p/free-tools.html",
        )
        // Ads and ad trackers are not loaded inside the app (AdSense web ads aren't allowed in apps).
        private val BLOCKED = listOf(
            "googlesyndication.com", "doubleclick.net", "googleadservices.com", "adservice.google.",
            "adtrafficquality.google", "fundingchoicesmessages.google.com",
        )
        // Link types that may open another app. Everything else (intent:, file:, content:, javascript: ...) is ignored.
        private val OUTSIDE_SCHEMES = setOf("https", "http", "mailto", "tel", "sms", "smsto", "whatsapp", "tg", "upi", "geo", "market")
        // File types the blog's tools produce. Anything else (for example an .apk) is never written to the phone.
        private val SAVE_TYPES = setOf(
            "application/pdf", "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml",
            "application/zip", "application/x-zip-compressed", "text/csv", "text/plain", "application/json",
        )
        private const val MAX_SAVE_BYTES = 100L * 1024 * 1024
        private val EMPTY = ByteArray(0)

        // Runs only on the blog's own pages. Hands files made by the blog's in-browser tools (blob: and data: links)
        // to the app through a message channel that accepts messages from the blog's origin only.
        private const val DOWNLOAD_BRIDGE = """
(function () {
  if (window.__plSave || !window.BDApp) return; window.__plSave = true;
  function save(href, name) {
    fetch(href).then(function (r) { return r.blob(); }).then(function (b) {
      if (b.size > 104857600) { BDApp.postMessage(JSON.stringify({ t: 'fail' })); return; }
      var fr = new FileReader();
      fr.onload = function () { BDApp.postMessage(JSON.stringify({ t: 'save', data: String(fr.result), mime: b.type || '', name: name || '' })); };
      fr.readAsDataURL(b);
    }).catch(function () { BDApp.postMessage(JSON.stringify({ t: 'fail' })); });
  }
  var click = HTMLAnchorElement.prototype.click;
  HTMLAnchorElement.prototype.click = function () {
    if (this.href && /^(blob|data):/.test(this.href)) { save(this.href, this.getAttribute('download')); return; }
    return click.call(this);
  };
  document.addEventListener('click', function (e) {
    var a = e.target && e.target.closest ? e.target.closest('a') : null;
    if (a && a.href && /^(blob|data):/.test(a.href)) { e.preventDefault(); e.stopPropagation(); save(a.href, a.getAttribute('download')); }
  }, true);
})();
"""

        // Inside the app the site is branded Plainly: header name, the "B" marks and any "Blog Digest" text.
        private const val BRAND = """
(function () {
  if (window.__plBrand) return; window.__plBrand = true;
  var RX = /Blog Digest/g, LOGO = 'background:linear-gradient(145deg,#2a5d90,#12304f 60%,#0a1b2f)!important;color:#fff!important;box-shadow:0 8px 20px rgba(10,27,47,.3)!important;',
    CSS = '.header-image-wrapper::before{content:"P"!important;' + LOGO + '}.header-image-wrapper::after{content:"Plainly"!important}.bd-mark,.bd-foot-logo>span{' + LOGO + '}';
  function css() {
    var root = document.head || document.documentElement;
    if (!root || document.getElementById('pl-brand')) return;
    var s = document.createElement('style'); s.id = 'pl-brand'; s.textContent = CSS;
    root.appendChild(s);
  }
  function fix(root) {
    if (!root) return;
    var w = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, { acceptNode: function (n) {
      var p = n.parentNode, t = p && p.nodeName;
      if (!p || t === 'SCRIPT' || t === 'STYLE' || t === 'TEXTAREA' || p.isContentEditable) return NodeFilter.FILTER_REJECT;
      return n.nodeValue.indexOf('Blog Digest') >= 0 ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT;
    } });
    var n; while ((n = w.nextNode())) n.nodeValue = n.nodeValue.replace(RX, 'Plainly');
    document.querySelectorAll('.bd-mark, .bd-foot-logo > span').forEach(function (e) { if (e.textContent.trim() === 'B') e.textContent = 'P'; });
    document.querySelectorAll('[aria-label*="Blog Digest"], [title*="Blog Digest"], [alt*="Blog Digest"], [placeholder*="Blog Digest"]').forEach(function (e) {
      ['aria-label', 'title', 'alt', 'placeholder'].forEach(function (a) { var v = e.getAttribute(a); if (v && v.indexOf('Blog Digest') >= 0) e.setAttribute(a, v.replace(RX, 'Plainly')); });
    });
    if (document.title.indexOf('Blog Digest') >= 0) document.title = document.title.replace(RX, 'Plainly');
  }
  css();
  function start() {
    if (!document.body) return;
    css(); fix(document.body);
    var queued = false;
    new MutationObserver(function () {
      if (queued) return; queued = true;
      requestAnimationFrame(function () { queued = false; fix(document.body); });
    }).observe(document.body, { childList: true, subtree: true, characterData: true });
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start); else start();
})();
"""
    }

    private lateinit var web: WebView
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var nav: BottomNavigationView
    private lateinit var progress: LinearProgressIndicator
    private lateinit var offline: View
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private var mainFrameFailed = false
    private var scriptsAtStart = false
    private var bridgeAvailable = false
    private val io = Executors.newSingleThreadExecutor()

    private val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        var uris = WebChromeClient.FileChooserParams.parseResult(r.resultCode, r.data)
        val clip = r.data?.clipData
        if (clip != null && clip.itemCount > 1) uris = Array(clip.itemCount) { clip.getItemAt(it).uri }
        fileCallback?.onReceiveValue(uris)
        fileCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        web = findViewById(R.id.web)
        swipe = findViewById(R.id.swipe)
        nav = findViewById(R.id.nav)
        progress = findViewById(R.id.progress)
        offline = findViewById(R.id.offline)

        val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !night
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content)) { v, insets ->
            v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
            insets
        }

        setUpWebView()

        swipe.setColorSchemeResources(R.color.brand)
        swipe.setOnChildScrollUpCallback { _, _ -> web.canScrollVertically(-1) }
        swipe.setOnRefreshListener { web.reload() }

        nav.setOnItemSelectedListener { item ->
            TABS[item.itemId]?.let { if (!sameTab(web.url, item.itemId)) web.loadUrl(it) }
            true
        }
        nav.setOnItemReselectedListener { item ->
            if (web.url?.let { sameTab(it, item.itemId) } == true) web.evaluateJavascript("window.scrollTo({top:0,behavior:'smooth'})", null)
            else TABS[item.itemId]?.let { web.loadUrl(it) }
        }

        findViewById<Button>(R.id.retry).setOnClickListener {
            mainFrameFailed = false
            offline.visibility = View.GONE
            web.reload()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (web.canGoBack()) web.goBack() else { isEnabled = false; onBackPressedDispatcher.onBackPressed() }
            }
        })

        if (savedInstanceState != null) web.restoreState(savedInstanceState)
        else web.loadUrl(startUrl(intent))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        startUrl(intent).takeIf { intent.data != null }?.let { web.loadUrl(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    override fun onDestroy() {
        io.shutdown()
        super.onDestroy()
    }

    /** Only https links to the blog itself are opened from outside; anything else starts at the home page. */
    private fun startUrl(i: Intent?): String =
        i?.data?.takeIf { it.scheme == "https" && it.host == HOST }?.toString() ?: HOME

    private fun sameTab(url: String?, id: Int): Boolean {
        val u = url?.substringBefore('?')?.substringBefore('#') ?: return false
        return TABS[id]?.let { u == it || (id == R.id.nav_home && u == ORIGIN) } ?: false
    }

    private fun online(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return true
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isBlog(url: String?): Boolean = url != null && Uri.parse(url).let { it.scheme == "https" && it.host == HOST }

    @SuppressLint("SetJavaScriptEnabled", "RequiresFeature")
    private fun setUpWebView() {
        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = true
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            setGeolocationEnabled(false)
            allowFileAccess = false
            allowContentAccess = true // lets the tools read files the user picks
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            userAgentString = "$userAgentString PlainlyApp/${BuildConfig.VERSION_NAME}"
            cacheMode = if (online()) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebSettingsCompat.setSafeBrowsingEnabled(web.settings, true)
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false)

        // File saving: a message channel limited to the blog's own origin and main frame.
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(web, "BDApp", setOf(ORIGIN)) { _, message, origin, isMainFrame, _ ->
                if (isMainFrame && origin.scheme == "https" && origin.host == HOST) onBridgeMessage(message.data)
            }
            bridgeAvailable = true
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(web, BRAND, setOf(ORIGIN))
            if (bridgeAvailable) WebViewCompat.addDocumentStartJavaScript(web, DOWNLOAD_BRIDGE, setOf(ORIGIN))
            scriptsAtStart = true
        }

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                return when {
                    uri.scheme == "https" && uri.host == HOST -> false
                    uri.scheme == "http" && uri.host == HOST -> { view.loadUrl(uri.buildUpon().scheme("https").build().toString()); true }
                    else -> { openOutside(uri); true }
                }
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val host = request.url.host ?: return null
                return if (BLOCKED.any { host.contains(it) }) WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(EMPTY)) else null
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                progress.visibility = View.VISIBLE
                web.settings.cacheMode = if (online()) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_CACHE_ELSE_NETWORK
            }

            override fun onPageFinished(view: WebView, url: String?) {
                swipe.isRefreshing = false
                progress.visibility = View.GONE
                if (!scriptsAtStart && isBlog(url)) {
                    view.evaluateJavascript(BRAND, null)
                    if (bridgeAvailable) view.evaluateJavascript(DOWNLOAD_BRIDGE, null)
                }
                if (!mainFrameFailed) offline.visibility = View.GONE
                selectTabFor(url)
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    mainFrameFailed = true
                    swipe.isRefreshing = false
                    offline.visibility = View.VISIBLE
                }
            }
            // SSL errors are not overridden: the default behaviour cancels any connection with a bad certificate.
        }

        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress.setProgressCompat(newProgress, true)
            }

            override fun onShowFileChooser(view: WebView, callback: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                return try {
                    val i = params.createIntent()
                    if (params.mode == FileChooserParams.MODE_OPEN_MULTIPLE) i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    pickFiles.launch(i)
                    true
                } catch (e: ActivityNotFoundException) {
                    fileCallback = null
                    false
                }
            }
        }

        web.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val uri = Uri.parse(url)
            when {
                // Files made in the page are saved through the message channel (see DOWNLOAD_BRIDGE).
                url.startsWith("blob:") || url.startsWith("data:") ->
                    if (!bridgeAvailable) Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show()
                uri.scheme == "https" && allowedType(mimeType) -> {
                    val name = cleanName(URLUtil.guessFileName(url, contentDisposition, mimeType), mimeType)
                    val req = DownloadManager.Request(uri)
                        .setMimeType(mimeType)
                        .addRequestHeader("User-Agent", userAgent)
                        .setTitle(name)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
                    getSystemService(DownloadManager::class.java)?.enqueue(req)
                    Toast.makeText(this, getString(R.string.downloading, name), Toast.LENGTH_SHORT).show()
                }
                else -> Toast.makeText(this, R.string.save_blocked, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectTabFor(url: String?) {
        val id = TABS.keys.firstOrNull { sameTab(url, it) } ?: return
        if (nav.selectedItemId != id) nav.menu.findItem(id)?.isChecked = true
    }

    private fun openOutside(uri: Uri) {
        val scheme = uri.scheme?.lowercase() ?: return
        if (scheme !in OUTSIDE_SCHEMES) return
        try {
            if (scheme == "https" || scheme == "http") {
                CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(this, uri)
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE))
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun allowedType(mime: String?): Boolean = mime?.substringBefore(';')?.trim()?.lowercase() in SAVE_TYPES

    /** A safe display name: no folders or control characters, sensible length, extension matching the type. */
    private fun cleanName(raw: String, mime: String): String {
        var name = raw.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "_").trim().trimStart('.').take(100)
        if (name.isBlank()) name = "plainly-${System.currentTimeMillis()}"
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime.substringBefore(';').trim())
        if (ext != null && !name.lowercase().endsWith(".$ext") && !(ext == "jpg" && name.lowercase().endsWith(".jpeg"))) {
            name = name.substringBeforeLast('.', name) + ".$ext"
        }
        return name
    }

    private fun onBridgeMessage(raw: String?) {
        val msg = try { JSONObject(raw ?: return) } catch (e: Exception) { return }
        if (msg.optString("t") != "save") {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show()
            return
        }
        val dataUrl = msg.optString("data")
        val comma = dataUrl.indexOf(',')
        if (!dataUrl.startsWith("data:") || comma < 0 || dataUrl.length > MAX_SAVE_BYTES * 4 / 3 + 1024) return
        val declared = dataUrl.substring(5, comma).substringBefore(';')
        val mime = msg.optString("mime").ifBlank { declared }.substringBefore(';').trim().lowercase()
        if (mime !in SAVE_TYPES) {
            Toast.makeText(this, R.string.save_blocked, Toast.LENGTH_SHORT).show()
            return
        }
        val name = cleanName(msg.optString("name"), mime)
        io.execute {
            val bytes = try { Base64.decode(dataUrl.substring(comma + 1), Base64.DEFAULT) } catch (e: IllegalArgumentException) { null }
            if (bytes == null || bytes.size > MAX_SAVE_BYTES) {
                runOnUiThread { Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show() }
                return@execute
            }
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri == null) {
                runOnUiThread { Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show() }
                return@execute
            }
            contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            values.clear(); values.put(MediaStore.Downloads.IS_PENDING, 0)
            contentResolver.update(uri, values, null, null)
            runOnUiThread {
                Snackbar.make(swipe, getString(R.string.saved, name), Snackbar.LENGTH_LONG)
                    .setAnchorView(nav)
                    .setAction(R.string.open) {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(this, R.string.no_app, Toast.LENGTH_SHORT).show()
                        }
                    }.show()
            }
        }
    }
}
