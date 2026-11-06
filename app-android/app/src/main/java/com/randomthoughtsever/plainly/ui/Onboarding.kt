package com.randomthoughtsever.plainly.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.data.Entry
import com.randomthoughtsever.plainly.data.GOALS
import com.randomthoughtsever.plainly.data.Numbers
import com.randomthoughtsever.plainly.tools.Catalog
import com.randomthoughtsever.plainly.tools.ToolEntry
import kotlinx.coroutines.delay
import java.util.Currency
import java.util.Locale

/** Countries offered at sign-up: name and ISO code (for the flag and the currency). */
val WORLD = listOf("United States" to "US", "United Kingdom" to "GB", "India" to "IN", "Canada" to "CA", "Australia" to "AU", "Germany" to "DE",
    "France" to "FR", "Spain" to "ES", "Italy" to "IT", "Netherlands" to "NL", "Ireland" to "IE", "Singapore" to "SG", "United Arab Emirates" to "AE",
    "Saudi Arabia" to "SA", "Japan" to "JP", "South Korea" to "KR", "China" to "CN", "Brazil" to "BR", "Mexico" to "MX", "South Africa" to "ZA",
    "Nigeria" to "NG", "Kenya" to "KE", "Egypt" to "EG", "Philippines" to "PH", "Malaysia" to "MY", "Indonesia" to "ID", "Pakistan" to "PK",
    "Bangladesh" to "BD", "New Zealand" to "NZ", "Switzerland" to "CH", "Sweden" to "SE", "Norway" to "NO", "Denmark" to "DK", "Finland" to "FI",
    "Belgium" to "BE", "Austria" to "AT", "Portugal" to "PT", "Poland" to "PL", "Greece" to "GR", "Czechia" to "CZ", "Turkey" to "TR",
    "Israel" to "IL", "Argentina" to "AR", "Chile" to "CL", "Colombia" to "CO", "Vietnam" to "VN", "Thailand" to "TH", "Sri Lanka" to "LK", "Nepal" to "NP")

fun flagOf(iso: String): String = iso.uppercase().map { c -> String(Character.toChars(0x1F1E6 + (c - 'A'))) }.joinToString("")
fun currencyOf(iso: String): String? = runCatching { Currency.getInstance(Locale.Builder().setRegion(iso).build()).currencyCode }.getOrNull()

/** The reader's plan: up to three guides for their goals (one per goal first), newest first, articles only. */
fun planFor(goals: Set<String>, index: List<Entry>): List<Entry> {
    val chosen = GOALS.filter { it.key in goals }.ifEmpty { GOALS.take(2) }
    val articles = index.filterNot { it.isToolPage }.sortedByDescending { it.published }
    val picks = LinkedHashMap<String, Entry>()
    for (round in 0 until 3) for (g in chosen) {
        if (picks.size >= 3) break
        articles.firstOrNull { (it.label in g.labels || g.words.containsMatchIn(it.title)) && it.id !in picks }?.let { picks[it.id] = it }
    }
    return picks.values.toList()
}

fun toolsFor(goals: Set<String>): List<ToolEntry> = GOALS.filter { it.key in goals }.mapNotNull { g -> Catalog.all().firstOrNull { it.key == g.tool } }

/** First launch: what do you want help with, where are you, your numbers (optional), then your plan. About 20 seconds. */
@Composable
fun OnboardingScreen(nav: Nav, onDone: (healthCheck: Boolean) -> Unit) {
    val profile = AppGraph.profile
    var step by rememberSaveable { mutableIntStateOf(0) }
    var goals by rememberSaveable { mutableStateOf(setOf<String>()) }
    var country by rememberSaveable { mutableStateOf(Locale.getDefault().country.takeIf { c -> WORLD.any { it.second == c } } ?: "US") }
    var income by rememberSaveable { mutableStateOf("") }
    var spend by rememberSaveable { mutableStateOf("") }
    var savings by rememberSaveable { mutableStateOf("") }
    var invested by rememberSaveable { mutableStateOf("") }
    var debt by rememberSaveable { mutableStateOf("") }
    fun numbers() = Numbers(income.toD(), spend.toD(), savings.toD(), invested.toD(), debt.toD(), 0.0).takeIf { it.isSet }
    fun finish(health: Boolean) { profile.finishOnboarding(goals, country, currencyOf(country), numbers()); onDone(health) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (step in 1..3) Row(Modifier.statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(progress = { step / 4f }, modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape))
            TextButton(onClick = { if (step == 3) step = 4 else finish(false) }) { Text("Skip") }
        }
        AnimatedContent(step, Modifier.weight(1f), transitionSpec = {
            (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
        }, label = "onboarding") { s ->
            when (s) {
                0 -> Welcome { step = 1 }
                1 -> GoalsStep(goals, { k -> goals = if (k in goals) goals - k else goals + k }) { step = 2 }
                2 -> CountryStep(country, { country = it }) { step = 3 }
                3 -> NumbersStep(currencyOf(country) ?: "USD", income, { income = it }, spend, { spend = it }, savings, { savings = it },
                    invested, { invested = it }, debt, { debt = it }) { step = 4 }
                else -> PlanReveal(goals, nav, onHealth = { finish(true) }, onStart = { finish(false) })
            }
        }
    }
}

private fun String.toD() = replace(",", "").toDoubleOrNull() ?: 0.0

@Composable
private fun Welcome(next: () -> Unit) {
    val labels = listOf("Personal Finance", "Investing", "Psychology", "Countries & Living Abroad", "Career & Workplace", "Self Improvement")
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(NavyDeep, Navy))).statusBarsPadding().navigationBarsPadding().padding(24.dp)) {
        Spacer(Modifier.height(18.dp))
        labels.chunked(3).forEachIndexed { r, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                row.forEachIndexed { i, l ->
                    val shown = remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { delay(120L * (r * 3 + i)); shown.value = true }
                    val k by animateFloatAsState(if (shown.value) 1f else .6f, spring(dampingRatio = .5f), label = "pop")
                    TopicScene(l, Modifier.weight(1f).aspectRatio(.9f).scale(k).clip(RoundedCornerShape(22.dp)))
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Logo(48.dp)
        Spacer(Modifier.height(16.dp))
        Text("Money, mind & work.\nPlainly.", style = MaterialTheme.typography.displaySmall, color = Color.White)
        Spacer(Modifier.height(10.dp))
        Text("Answers you can use in a minute, 72 free tools, and a plan made for you. No account needed.",
            style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = .8f))
        Spacer(Modifier.height(24.dp))
        Button(onClick = next, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(50),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NavyDeep)) {
            Text("Make it mine", fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
        }
    }
}

@Composable
private fun StepTitle(title: String, sub: String) = Column(Modifier.padding(horizontal = 24.dp)) {
    Text(title, style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(6.dp))
    Text(sub, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(18.dp))
}

@Composable
private fun NextButton(text: String, enabled: Boolean = true, onClick: () -> Unit) =
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(24.dp).height(56.dp), shape = RoundedCornerShape(50)) {
        Text(text, fontWeight = FontWeight.Bold)
    }

@Composable
private fun GoalsStep(goals: Set<String>, toggle: (String) -> Unit, next: () -> Unit) = Column(Modifier.fillMaxSize()) {
    StepTitle("What do you want help with?", "Pick one or more. Your Today page and plan follow your choice.")
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GOALS.forEach { g ->
            val on = g.key in goals
            val k by animateFloatAsState(if (on) 1f else .98f, spring(dampingRatio = .45f), label = "goal")
            Surface(onClick = { toggle(g.key) }, shape = RoundedCornerShape(22.dp), color = if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(if (on) 2.dp else 1.dp, if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().scale(k)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Brush.linearGradient(listOf(g.from, g.to))), contentAlignment = Alignment.Center) {
                        Icon(g.icon, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(g.title, style = MaterialTheme.typography.titleMedium)
                        Text(g.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(Modifier.size(26.dp).clip(CircleShape).background(if (on) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .then(if (on) Modifier else Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)), contentAlignment = Alignment.Center) {
                        if (on) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
    NextButton(if (goals.isEmpty()) "Pick at least one" else "Continue", goals.isNotEmpty(), next)
}

@Composable
private fun CountryStep(country: String, pick: (String) -> Unit, next: () -> Unit) = Column(Modifier.fillMaxSize()) {
    StepTitle("Where do you live?", "For your currency and the money facts that apply to you.")
    var q by remember { mutableStateOf("") }
    OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth().padding(horizontal = 20.dp), singleLine = true, shape = RoundedCornerShape(50),
        placeholder = { Text("Search countries") }, leadingIcon = { Icon(Icons.Rounded.Search, null) })
    val list = WORLD.filter { q.isBlank() || it.first.contains(q.trim(), true) }
    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        items(list, key = { it.second }) { (name, iso) ->
            val on = iso == country
            Surface(onClick = { pick(iso) }, shape = RoundedCornerShape(16.dp), color = if (on) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(flagOf(iso), style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.width(12.dp))
                    Text(name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text(currencyOf(iso) ?: "", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (on) { Spacer(Modifier.width(8.dp)); Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
    NextButton("Continue with ${WORLD.firstOrNull { it.second == country }?.first ?: country}", true, next)
}

@Composable
private fun NumbersStep(cur: String, income: String, setIncome: (String) -> Unit, spend: String, setSpend: (String) -> Unit, savings: String,
                        setSavings: (String) -> Unit, invested: String, setInvested: (String) -> Unit, debt: String, setDebt: (String) -> Unit,
                        next: () -> Unit) = Column(Modifier.fillMaxSize().imePadding()) {
    StepTitle("Want your own numbers?", "Optional, 30 seconds. They stay on your phone and power your FIRE number, safety net and debt-free date.")
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(Triple("Monthly take-home pay", income, setIncome), Triple("Monthly spending", spend, setSpend), Triple("Savings in the bank", savings, setSavings),
            Triple("Investments", invested, setInvested), Triple("Debt (cards and loans)", debt, setDebt)).forEach { (label, v, set) ->
            OutlinedTextField(v, { set(it.filter { c -> c.isDigit() || c == '.' }.take(12)) }, Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
                suffix = { Text(cur) }, shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
    }
    NextButton("See my plan", true, next)
}

@Composable
private fun PlanReveal(goals: Set<String>, nav: Nav, onHealth: () -> Unit, onStart: () -> Unit) {
    val index = rememberIndex()
    val plan = remember(index, goals) { planFor(goals, index.orEmpty()) }
    val tools = remember(goals) { toolsFor(goals).take(2) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); shown = true }
    val k by animateFloatAsState(if (shown) 1f else .3f, spring(dampingRatio = .45f, stiffness = 300f), label = "tick")
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp)) {
            Box(Modifier.size(72.dp).scale(k).clip(CircleShape).background(Brush.linearGradient(listOf(Gold, Color(0xFFC0602A)))), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Your plan is ready", style = MaterialTheme.typography.headlineMedium)
            Text("Three guides and the tools to act on them. It's waiting on your Today page.", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            if (index == null) repeat(3) { Shimmer(Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(16.dp))); Spacer(Modifier.height(10.dp)) }
            plan.forEachIndexed { i, e ->
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(e.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                }
            }
            tools.forEach { t ->
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(t.color), contentAlignment = Alignment.Center) {
                        Icon(t.icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Tool: ${t.title}", style = MaterialTheme.typography.titleSmall)
                }
            }
            Spacer(Modifier.height(18.dp))
            Surface(shape = RoundedCornerShape(22.dp), color = Color.Transparent) {
                Column(Modifier.background(Brush.linearGradient(listOf(Navy, NavyDeep))).padding(18.dp)) {
                    Text("60-SECOND MONEY HEALTH CHECK", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.ExtraBold)
                    Text("Get your score out of 100 and the one thing to fix first.", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
        }
        Button(onClick = onHealth, Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp).height(56.dp), shape = RoundedCornerShape(50)) {
            Text("Take the Money Health Check", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onStart, Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 12.dp)) { Text("Start exploring") }
    }
}

@Composable
fun FlagText(iso: String) = Text(flagOf(iso), textAlign = TextAlign.Center)
