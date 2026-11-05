package com.randomthoughtsever.plainly

import com.randomthoughtsever.plainly.tools.WebTool
import org.json.JSONObject
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.net.URL

/**
 * Extracts every website tool in assets/tools.json exactly as the app does and writes the pages to build/webtools,
 * so tools-catalog/check_webtools.js can run each one in Chromium (the engine behind Android's WebView).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WebToolPagesTest {
    @Test fun everyCatalogueToolHasItsWidget() {
        val catalog = JSONObject(File("src/main/assets/tools.json").readText()).getJSONArray("tools")
        val out = File("build/webtools").apply { mkdirs() }
        val light = WebTool.Palette(0xF6F4F0.toInt(), 0xFFFFFF, 0xEEEAE3, 0x0F1B2A, 0x5B6576, 0xD9D3C8, 0x1F4E79, false)
        var reachable = true
        for (i in 0 until catalog.length()) {
            val t = catalog.getJSONObject(i)
            val body = runCatching { URL("https://randomthoughtsever.blogspot.com/feeds/posts/default/${t.getString("post")}?alt=json").readText() }.getOrNull()
            if (body == null) { reachable = false; break }
            val html = JSONObject(body).getJSONObject("entry").getJSONObject("content").getString("\$t")
            val page = WebTool.page(html, t.getString("id"), light)
            assertNotNull("${t.getString("id")} not found in its post", page)
            File(out, t.getString("id") + ".html").writeText(page!!)
        }
        assumeTrue("network unavailable", reachable)
        println("WEBTOOLS wrote ${catalog.length()} pages")
    }
}
