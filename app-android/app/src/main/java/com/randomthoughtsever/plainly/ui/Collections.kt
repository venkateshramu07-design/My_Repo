package com.randomthoughtsever.plainly.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Portrait
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.Entry

/** A hand-picked shelf of guides, defined by a rule over the full index, so new posts join the right shelf by themselves. */
class Shelf(val key: String, val title: String, val blurb: String, val icon: ImageVector, val from: Color, val to: Color, val match: (Entry) -> Boolean)

private fun Entry.t() = title.lowercase()
val COUNTRY_TITLE = Regex("^(.+?) (Minimum Wage|Income Tax) 2026")

val Shelves = listOf(
    Shelf("start", "Start here", "The best place to begin", Icons.Rounded.Flag, Color(0xFF1F4E79), Color(0xFF3B79B5)) { "Start Here" in it.labels },
    Shelf("debt", "Get out of debt", "Cards, loans and payoff plans", Icons.Rounded.MoneyOff, Color(0xFF8E3B6E), Color(0xFFC25A8C)) {
        Regex("debt|credit card|payoff|consolidat|loan with|bad credit|terrible credit").containsMatchIn(it.t()) },
    Shelf("invest", "Invest & retire early", "Funds, FIRE, 401(k) and pensions", Icons.AutoMirrored.Rounded.TrendingUp, Color(0xFF0F5F55), Color(0xFF2E9C88)) {
        "Investing" in it.labels || Regex("retire|fire calculator|401\\(k\\)|pension|sip|annuit").containsMatchIn(it.t()) },
    Shelf("countries", "Countries in numbers", "Wages, tax and homes in 35 countries", Icons.Rounded.Public, Color(0xFF0E4D64), Color(0xFF2B86A3)) {
        COUNTRY_TITLE.containsMatchIn(it.title) || it.title.startsWith("Compare Countries") },
    Shelf("photos", "Passport & visa photos", "Exact sizes, made at home", Icons.Rounded.Portrait, Color(0xFF243B6B), Color(0xFF4A6FB0)) {
        Regex("photo size|passport photo|photo maker").containsMatchIn(it.t()) },
    Shelf("money", "Money basics", "Budgets, credit scores and banks", Icons.Rounded.AccountBalance, Color(0xFF12304F), Color(0xFF2F5E8C)) {
        "Personal Finance" in it.labels && !Regex("debt|loan").containsMatchIn(it.t()) },
    Shelf("mind", "A calmer mind", "Psychology, stress and stillness", Icons.Rounded.Spa, Color(0xFF145A6B), Color(0xFF3A9AAE)) {
        "Psychology" in it.labels || "Zen & Mindfulness" in it.labels },
    Shelf("growth", "Build better habits", "Focus, consistency and growth", Icons.Rounded.SelfImprovement, Color(0xFF7A4E0C), Color(0xFFC08A2E)) {
        "Self Improvement" in it.labels },
    Shelf("career", "Career moves", "Pay, interviews and burnout", Icons.Rounded.Work, Color(0xFF8A4B1E), Color(0xFFC77A43)) {
        "Career & Workplace" in it.labels || "Leadership" in it.labels },
    Shelf("ai", "AI & the future", "What's changing and how to keep up", Icons.Rounded.Memory, Color(0xFF3B2F6B), Color(0xFF6E5BB5)) {
        "Technology & AI" in it.labels },
    Shelf("love", "Love & people", "Relationships and family", Icons.Rounded.Favorite, Color(0xFF7D2340), Color(0xFFC0506F)) {
        "Love & Relationships" in it.labels || "Parenting & Family" in it.labels },
)

/** Country name in the blog's titles → ISO code, for the flag. */
private val ISO = mapOf("US" to "US", "UK" to "GB", "Switzerland" to "CH", "Sweden" to "SE", "Spain" to "ES", "South Korea" to "KR",
    "South Africa" to "ZA", "Slovenia" to "SI", "Slovakia" to "SK", "Portugal" to "PT", "Poland" to "PL", "Norway" to "NO", "New Zealand" to "NZ",
    "Netherlands" to "NL", "Mexico" to "MX", "Luxembourg" to "LU", "Lithuania" to "LT", "Latvia" to "LV", "Japan" to "JP", "Italy" to "IT",
    "Ireland" to "IE", "India" to "IN", "Greece" to "GR", "Germany" to "DE", "France" to "FR", "Finland" to "FI", "Estonia" to "EE",
    "Denmark" to "DK", "Czechia" to "CZ", "China" to "CN", "Canada" to "CA", "Brazil" to "BR", "Belgium" to "BE", "Austria" to "AT",
    "Australia" to "AU", "Hungary" to "HU", "Croatia" to "HR", "Romania" to "RO", "Turkey" to "TR", "Israel" to "IL", "Chile" to "CL",
    "Colombia" to "CO", "Singapore" to "SG", "United Arab Emirates" to "AE", "UAE" to "AE", "Iceland" to "IS")

fun flag(country: String): String = ISO[country]?.let { code -> code.map { c -> String(Character.toChars(0x1F1E6 + (c - 'A'))) }.joinToString("") } ?: "🌍"

data class Country(val name: String, val flag: String, val id: String)

fun countries(index: List<Entry>): List<Country> = index.mapNotNull { e ->
    COUNTRY_TITLE.find(e.title)?.groupValues?.get(1)?.let { n -> Country(n, flag(n), e.id) }
}.distinctBy { it.name }.sortedBy { it.name }

@Composable
fun rememberIndex(): List<Entry>? {
    val idx by produceState<List<Entry>?>(null) { value = AppGraph.repo.index() }
    return idx
}

/** One shelf, full screen. */
@Composable
fun ShelfScreen(key: String, nav: Nav) {
    val shelf = Shelves.firstOrNull { it.key == key } ?: Shelves.first()
    val index = rememberIndex()
    val list = index?.filter(shelf.match)?.sortedByDescending { it.published }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(shelf.from, shelf.to)))) {
                Icon(shelf.icon, null, tint = Color.White.copy(alpha = .14f), modifier = Modifier.size(170.dp).align(Alignment.BottomEnd).padding(top = 30.dp))
                Column(Modifier.statusBarsPadding().padding(start = 8.dp, end = 20.dp, bottom = 26.dp)) {
                    IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White) }
                    Column(Modifier.padding(start = 12.dp, top = 18.dp)) {
                        Text("COLLECTION", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = .75f), fontWeight = FontWeight.ExtraBold)
                        Text(shelf.title, style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        Text(shelf.blurb + (list?.let { " · ${it.size} guides" } ?: ""), style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = .85f))
                    }
                }
            }
        }
        if (list == null) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else items(list, key = { it.id }) { e -> Box(Modifier.padding(horizontal = 16.dp)) { PostRow(e.ref, { nav.article(e.id) }) } }
    }
}

/** Every guide, filterable by topic and words, newest first or A to Z. */
@Composable
fun LibraryScreen(nav: Nav) {
    val index = rememberIndex()
    var query by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("All") }
    var az by rememberSaveable { mutableStateOf(false) }
    val topics = listOf("All") + Topics.map { it.label }.filter { l -> index?.any { it.label == l } != false }
    val q = query.trim().lowercase()
    val list = index.orEmpty().filter { (topic == "All" || it.label == topic || topic in it.labels) && (q.isEmpty() || it.title.lowercase().contains(q)) }
        .let { if (az) it.sortedBy { e -> e.title.lowercase() } else it.sortedByDescending { e -> e.published } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.statusBarsPadding().padding(start = 8.dp, end = 16.dp, top = 4.dp)) {
                IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Text("All guides", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(start = 12.dp))
                Text(if (index == null) "Loading…" else "${list.size} of ${index.size} guides", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(start = 8.dp), singleLine = true, shape = RoundedCornerShape(50),
                    placeholder = { Text("Filter by title") }, leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, "Clear") } })
                Spacer(Modifier.height(10.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(start = 8.dp)) {
                    SegmentedButton(!az, { az = false }, SegmentedButtonDefaults.itemShape(0, 2)) { Text("Newest") }
                    SegmentedButton(az, { az = true }, SegmentedButtonDefaults.itemShape(1, 2)) { Text("A to Z") }
                }
            }
        }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(topics) { l -> FilterChip(topic == l, { topic = l }, label = { Text(if (l == "All") "All" else topicFor(l).title) }, shape = RoundedCornerShape(50)) }
            }
        }
        if (index == null) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        var letter = ' '
        list.forEach { e ->
            val first = e.title.first().uppercaseChar().let { if (it.isLetter()) it else '#' }
            if (az && first != letter) {
                letter = first
                item(key = "h$first") {
                    Text(first.toString(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 24.dp, top = 14.dp, bottom = 4.dp))
                }
            }
            item(key = e.id) { Box(Modifier.padding(horizontal = 16.dp)) { PostRow(e.ref, { nav.article(e.id) }) } }
        }
    }
}

@Composable
fun CountryChip(c: Country, onClick: () -> Unit) {
    androidx.compose.material3.Surface(onClick = onClick, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .7f)), modifier = Modifier.width(150.dp)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(c.flag, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(10.dp))
            Text(c.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
        }
    }
}
