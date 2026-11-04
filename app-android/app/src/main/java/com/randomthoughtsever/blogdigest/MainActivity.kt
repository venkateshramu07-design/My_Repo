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
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
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
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayInputStream

class MainActivity : AppCompatActivity() {

    companion object {
        const val HOST = "randomthoughtsever.blogspot.com"
        const val HOME = "https://$HOST/"
        private val TABS = mapOf(
            R.id.nav_home to HOME,
            R.id.nav_money to "https://$HOST/search/label/Personal%20Finance",
            R.id.nav_mind to "https://$HOST/search/label/Psychology",
            R.id.nav_career to "https://$HOST/search/label/Career%20%26%20Workplace",
            R.id.nav_tools to "https://$HOST/p/free-tools.html",
        )
        // Ads and ad trackers are not loaded inside the app (AdSense web ads aren't allowed in apps).
        private val BLOCKED = listOf(
            "googlesyndication.com", "doubleclick.net", "googleadservices.com", "adservice.google.",
            "adtrafficquality.google", "fundingchoicesmessages.google.com",
        )
        private val EMPTY = ByteArray(0)

        // Saves files the blog's tools create in the browser (blob: and data: links) through the app.
        private const val DOWNLOAD_BRIDGE = """
(function () {
  if (window.__bdBridge) return; window.__bdBridge = true;
  function save(href, name) {
    fetch(href).then(function (r) { return r.blob(); }).then(function (b) {
      var fr = new FileReader();
      fr.onload = function () { BDApp.saveFile(String(fr.result), b.type || '', name || ''); };
      fr.readAsDataURL(b);
    }).catch(function (e) { BDApp.failed(String(e)); });
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
    }

    private lateinit var web: WebView
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var nav: BottomNavigationView
    private lateinit var progress: LinearProgressIndicator
    private lateinit var offline: View
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private var mainFrameFailed = false
    private var bridgeAtStart = false

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
        intent.data?.let { if (it.host == HOST) web.loadUrl(it.toString()) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    private fun startUrl(i: Intent?): String = i?.data?.takeIf { it.host == HOST }?.toString() ?: HOME

    private fun sameTab(url: String?, id: Int): Boolean {
        val u = url?.substringBefore('?')?.substringBefore('#') ?: return false
        return TABS[id]?.let { u == it || (id == R.id.nav_home && u == "https://$HOST") } ?: false
    }

    private fun online(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return true
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = true
            allowFileAccess = false
            allowContentAccess = true
            setSupportMultipleWindows(false)
            userAgentString = "$userAgentString BlogDigestApp/${BuildConfig.VERSION_NAME}"
            cacheMode = if (online()) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
        CookieManager.getInstance().setAcceptCookie(true)
        web.addJavascriptInterface(Bridge(), "BDApp")
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(web, DOWNLOAD_BRIDGE, setOf("https://$HOST"))
            bridgeAtStart = true
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
                if (!bridgeAtStart) view.evaluateJavascript(DOWNLOAD_BRIDGE, null)
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

        web.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            when {
                url.startsWith("blob:") || url.startsWith("data:") ->
                    web.evaluateJavascript("fetch('$url').then(r=>r.blob()).then(b=>{var f=new FileReader();f.onload=()=>BDApp.saveFile(String(f.result),b.type||'','');f.readAsDataURL(b);})", null)
                else -> {
                    val name = URLUtil.guessFileName(url, contentDisposition, mimeType)
                    val req = DownloadManager.Request(Uri.parse(url))
                        .setMimeType(mimeType)
                        .addRequestHeader("User-Agent", userAgent)
                        .setTitle(name)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
                    getSystemService(DownloadManager::class.java)?.enqueue(req)
                    Toast.makeText(this, getString(R.string.downloading, name), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun selectTabFor(url: String?) {
        val id = TABS.keys.firstOrNull { sameTab(url, it) } ?: return
        if (nav.selectedItemId != id) {
            nav.menu.findItem(id)?.isChecked = true
        }
    }

    private fun openOutside(uri: Uri) {
        try {
            if (uri.scheme == "https" || uri.scheme == "http") {
                CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(this, uri)
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_app, Toast.LENGTH_SHORT).show()
        }
    }

    /** Called from the page's JavaScript to save a file made by one of the blog's tools. */
    inner class Bridge {
        @JavascriptInterface
        fun saveFile(dataUrl: String, mime: String, name: String) {
            val comma = dataUrl.indexOf(',')
            if (comma < 0) return
            val type = mime.ifBlank { dataUrl.substring(5, dataUrl.indexOf(';').coerceAtLeast(5)) }.ifBlank { "application/octet-stream" }
            val bytes = try { Base64.decode(dataUrl.substring(comma + 1), Base64.DEFAULT) } catch (e: IllegalArgumentException) { return }
            val ext = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
            val fileName = name.ifBlank { "blog-digest-${System.currentTimeMillis()}" + (ext?.let { ".$it" } ?: "") }
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, type)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return
            contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            values.clear(); values.put(MediaStore.Downloads.IS_PENDING, 0)
            contentResolver.update(uri, values, null, null)
            runOnUiThread {
                Snackbar.make(swipe, getString(R.string.saved, fileName), Snackbar.LENGTH_LONG)
                    .setAnchorView(nav)
                    .setAction(R.string.open) {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, type).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(this@MainActivity, R.string.no_app, Toast.LENGTH_SHORT).show()
                        }
                    }.show()
            }
        }

        @JavascriptInterface
        fun failed(message: String) {
            runOnUiThread { Toast.makeText(this@MainActivity, R.string.save_failed, Toast.LENGTH_SHORT).show() }
        }
    }
}
