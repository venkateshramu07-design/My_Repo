package com.randomthoughtsever.plainly.tools

import android.content.Context
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.ElectricCar
import androidx.compose.material.icons.rounded.Elderly
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.automirrored.rounded.MergeType
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Portrait
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.randomthoughtsever.plainly.data.Blog
import com.randomthoughtsever.plainly.ui.Nav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** One tool in the app: either built into the app ([native]) or one of the website's tools ([post] + [widget]). */
data class ToolEntry(
    val key: String, val title: String, val blurb: String, val icon: ImageVector, val color: Color, val group: String,
    val native: String? = null, val post: String? = null, val widget: String? = null, val paths: List<String> = emptyList(),
    val isNew: Boolean = false,
) {
    val offline get() = native != null
    fun open(nav: Nav) = if (native != null) nav.tool(native) else nav.webTool(post!!, widget ?: "all")
}

/**
 * Every tool: the app's own tools, the website's tools listed in assets/tools.json, and any tool post published after
 * that list was made (found live from the blog's "Tools" and "Calculators" labels, so a new tool appears straight away).
 */
object Catalog {
    val GROUPS = listOf("New", "Photos", "PDF & documents", "Money & debt", "Investing & retirement", "Work & career", "Everyday",
        "Health & habits", "Travel & abroad", "World data", "India")
    private val NATIVE_GROUP = mapOf("photo" to "Photos", "pdf" to "PDF & documents", "qr" to "Everyday", "loan" to "Money & debt",
        "card" to "Money & debt", "emergency" to "Money & debt", "growth" to "Investing & retirement", "fire" to "Investing & retirement")

    @Volatile private var base: List<ToolEntry> = emptyList()
    @Volatile private var built = "2026-11-05"
    @Volatile var discovered: List<ToolEntry> = emptyList()
        private set

    fun load(context: Context) {
        if (base.isNotEmpty()) return
        val o = runCatching { JSONObject(context.assets.open("tools.json").bufferedReader().readText()) }.getOrNull() ?: return
        built = o.optString("built", built)
        val nativePaths = o.optJSONObject("native")
        val natives = AllTools.map { t ->
            val paths = nativePaths?.optJSONArray(t.id)?.let { a -> List(a.length()) { a.getString(it) } } ?: emptyList()
            ToolEntry("n:${t.id}", t.title, t.blurb, t.icon, t.color, NATIVE_GROUP[t.id] ?: "Everyday", native = t.id, paths = paths)
        }
        val arr = o.getJSONArray("tools")
        val web = List(arr.length()) { i ->
            val t = arr.getJSONObject(i)
            ToolEntry("w:${t.getString("id")}", t.getString("title"), t.getString("blurb"), icon(t.getString("icon")),
                Color(android.graphics.Color.parseColor(t.getString("color"))), t.getString("group"),
                post = t.getString("post"), widget = t.getString("id"),
                paths = t.getJSONArray("paths").let { a -> List(a.length()) { a.getString(it) } })
        }
        base = natives + web
    }

    fun all(): List<ToolEntry> = discovered + base
    fun web(widget: String, post: String): ToolEntry? = all().firstOrNull { it.widget == widget && widget != "all" } ?: all().firstOrNull { it.post == post }

    /** The tool that belongs to an article, so the article can offer it as a button. */
    fun forPath(path: String): ToolEntry? = all().firstOrNull { path in it.paths }
        ?: toolFor(path)?.let { n -> base.firstOrNull { it.native == n.id } }

    /** Looks for tool posts published after the built-in list was made. */
    suspend fun discover() = withContext(Dispatchers.IO) {
        val known = base.flatMap { it.paths }.toHashSet()
        val found = LinkedHashMap<String, ToolEntry>()
        for (label in listOf("Tools", "Calculators")) {
            val body = runCatching { get("${Blog.BASE}/feeds/posts/summary/-/${Uri.encode(label)}?alt=json&max-results=40") }.getOrNull() ?: continue
            val entries = JSONObject(body).getJSONObject("feed").optJSONArray("entry") ?: continue
            for (i in 0 until entries.length()) {
                val e = entries.getJSONObject(i)
                val published = e.getJSONObject("published").getString("\$t").take(10)
                if (published <= built) continue
                val id = e.getJSONObject("id").getString("\$t").substringAfter("post-")
                val links = e.getJSONArray("link")
                val url = (0 until links.length()).map { links.getJSONObject(it) }.firstOrNull { it.getString("rel") == "alternate" }?.getString("href") ?: continue
                val path = Uri.parse(url).path ?: continue
                if (path in known || found.containsKey(id)) continue
                val title = e.getJSONObject("title").getString("\$t").substringBefore(":").substringBefore(" (").trim()
                found[id] = ToolEntry("d:$id", title, "New on the website", if (label == "Calculators") Icons.Rounded.Calculate else Icons.Rounded.Build,
                    Color(0xFFB8873E), "New", post = id, widget = "all", paths = listOf(path), isNew = true)
            }
        }
        discovered = found.values.toList()
    }

    private fun get(url: String): String {
        val c = (URL(url).openConnection() as HttpURLConnection).apply { connectTimeout = 10000; readTimeout = 15000; setRequestProperty("User-Agent", "PlainlyApp/1.1 (Android)") }
        return c.inputStream.bufferedReader().use { it.readText() }
    }

    fun icon(name: String): ImageVector = when (name) {
        "merge" -> Icons.AutoMirrored.Rounded.MergeType; "draw" -> Icons.Rounded.Draw; "pdf" -> Icons.Rounded.PictureAsPdf; "receipt" -> Icons.Rounded.Receipt
        "badge" -> Icons.Rounded.Badge; "privacy" -> Icons.Rounded.PrivacyTip; "straighten" -> Icons.Rounded.Straighten; "image" -> Icons.Rounded.Image
        "portrait" -> Icons.Rounded.Portrait; "signature" -> Icons.Rounded.Gesture; "debt" -> Icons.Rounded.MoneyOff; "car" -> Icons.Rounded.DirectionsCar
        "home" -> Icons.Rounded.Home; "shield" -> Icons.Rounded.Shield; "policy" -> Icons.Rounded.Policy; "card" -> Icons.Rounded.CreditCard
        "savings" -> Icons.Rounded.Savings; "trending_up" -> Icons.AutoMirrored.Rounded.TrendingUp; "trending_down" -> Icons.AutoMirrored.Rounded.TrendingDown
        "event_repeat" -> Icons.Rounded.EventRepeat; "currency" -> Icons.Rounded.CurrencyExchange; "ev" -> Icons.Rounded.ElectricCar
        "payments" -> Icons.Rounded.Payments; "balance" -> Icons.Rounded.Balance; "chart" -> Icons.AutoMirrored.Rounded.ShowChart; "flight" -> Icons.Rounded.Flight
        "gold" -> Icons.Rounded.Diamond; "crypto" -> Icons.Rounded.CurrencyBitcoin; "casino" -> Icons.Rounded.Casino; "chat" -> Icons.AutoMirrored.Rounded.Chat
        "robot" -> Icons.Rounded.SmartToy; "schedule" -> Icons.Rounded.Schedule; "grid" -> Icons.Rounded.GridView; "text" -> Icons.Rounded.TextFields
        "password" -> Icons.Rounded.Password; "storage" -> Icons.Rounded.Storage; "health" -> Icons.Rounded.MonitorHeart; "sleep" -> Icons.Rounded.Bedtime
        "check" -> Icons.Rounded.TaskAlt; "checklist" -> Icons.Rounded.Checklist; "medication" -> Icons.Rounded.Medication; "public" -> Icons.Rounded.Public
        "beach" -> Icons.Rounded.BeachAccess; "bank" -> Icons.Rounded.AccountBalance; "bolt" -> Icons.Rounded.Bolt; "elderly" -> Icons.Rounded.Elderly
        "cake" -> Icons.Rounded.Cake; "percent" -> Icons.Rounded.Percent; "qr" -> Icons.Rounded.QrCode2
        else -> Icons.Rounded.Build
    }
}
