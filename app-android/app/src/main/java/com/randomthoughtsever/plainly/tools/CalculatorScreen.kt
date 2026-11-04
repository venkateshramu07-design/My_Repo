package com.randomthoughtsever.plainly.tools

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.randomthoughtsever.plainly.ui.AppGraph
import com.randomthoughtsever.plainly.ui.Nav
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow

// ---------- Maths (pure, so it can be unit-tested) ----------

object Calc {
    data class Loan(val payment: Double, val months: Int, val interest: Double, val baseMonths: Int, val baseInterest: Double)

    fun loan(principal: Double, ratePct: Double, years: Double, extra: Double): Loan? {
        val n = (years * 12).toInt()
        if (principal <= 0 || n <= 0) return null
        val i = ratePct / 1200
        val pay = if (i == 0.0) principal / n else principal * i / (1 - (1 + i).pow(-n))
        var bal = principal; var months = 0; var interest = 0.0
        while (bal > 0.005 && months < 1200) {
            val int = bal * i; interest += int
            bal = bal + int - (pay + extra); months++
        }
        return Loan(pay, months, interest, n, pay * n - principal)
    }

    data class Growth(val value: Double, val contributed: Double, val yearly: List<Pair<Double, Double>>)

    /** Monthly contributions at the start of each month; [stepUpPct] raises the contribution once a year. */
    fun growth(initial: Double, monthly: Double, ratePct: Double, years: Int, stepUpPct: Double): Growth {
        val i = (1 + ratePct / 100).pow(1.0 / 12) - 1
        var v = initial; var paid = initial; var m = monthly
        val yearly = ArrayList<Pair<Double, Double>>()
        for (y in 1..years) {
            repeat(12) { v = (v + m) * (1 + i); paid += m }
            yearly += paid to v
            m *= 1 + stepUpPct / 100
        }
        return Growth(v, paid, yearly)
    }

    data class Card(val months: Int, val interest: Double, val balances: List<Double>, val forTwoYears: Double)

    fun card(balance: Double, aprPct: Double, payment: Double): Card? {
        val i = aprPct / 1200
        if (balance <= 0) return null
        val forTwoYears = if (i == 0.0) balance / 24 else balance * i / (1 - (1 + i).pow(-24))
        if (payment <= balance * i) return Card(-1, 0.0, emptyList(), forTwoYears)
        var b = balance; var months = 0; var interest = 0.0
        val balances = arrayListOf(b)
        while (b > 0.005 && months < 1200) {
            val int = b * i; interest += int; b = max(0.0, b + int - payment); months++; balances += b
        }
        return Card(months, interest, balances, forTwoYears)
    }

    data class Fire(val target: Double, val years: Double, val path: List<Double>)

    fun fire(annualSpend: Double, saved: Double, monthly: Double, ratePct: Double, withdrawPct: Double): Fire? {
        if (annualSpend <= 0 || withdrawPct <= 0) return null
        val target = annualSpend * 100 / withdrawPct
        val i = (1 + ratePct / 100).pow(1.0 / 12) - 1
        var v = saved; var month = 0
        val path = arrayListOf(v)
        while (v < target && month < 12 * 80) { v = (v + monthly) * (1 + i); month++; if (month % 12 == 0) path += v }
        if (v >= target && month % 12 != 0) path += v
        return Fire(target, if (v >= target) month / 12.0 else -1.0, path)
    }

    fun monthsToFill(gap: Double, monthly: Double): Int = if (gap <= 0) 0 else if (monthly <= 0) -1 else ceil(gap / monthly).toInt()
}

// ---------- Currency ----------

private val CURRENCIES = listOf("USD", "EUR", "GBP", "CAD", "AUD", "INR", "JPY", "CHF", "SGD", "AED", "NZD", "ZAR", "BRL", "MXN", "PHP", "NGN")

private object Money {
    private fun prefs(c: Context) = c.getSharedPreferences("plainly", Context.MODE_PRIVATE)
    fun get(c: Context): String = prefs(c).getString("currency", null)
        ?: runCatching { Currency.getInstance(Locale.getDefault()).currencyCode }.getOrNull()?.takeIf { it in CURRENCIES } ?: "USD"
    fun set(c: Context, code: String) = prefs(c).edit().putString("currency", code).apply()
    fun symbol(code: String): String = Currency.getInstance(code).getSymbol(Locale.getDefault()).let { if (it == code) "$code " else it }
    fun fmt(code: String, v: Double): String = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = Currency.getInstance(code); maximumFractionDigits = 0; minimumFractionDigits = 0
    }.format(v)
}

// ---------- UI pieces ----------

/** 0 → 1 once when a chart first appears, so it draws itself in. */
@Composable
private fun rememberGrow(ms: Int): Float {
    val a = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, tween(ms)) }
    return a.value
}

private fun String.num(): Double = replace(",", "").toDoubleOrNull() ?: 0.0

@Composable
private fun NumField(label: String, value: String, onChange: (String) -> Unit, prefix: String? = null, suffix: String? = null, modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(value, { v -> onChange(v.filter { it.isDigit() || it == '.' }.take(12)) }, label = { Text(label) }, singleLine = true,
        prefix = prefix?.let { { Text(it) } }, suffix = suffix?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = modifier, shape = RoundedCornerShape(16.dp))
}

/** A number that counts up to its new value whenever it changes. */
@Composable
private fun Counting(value: Double, format: (Double) -> String, style: androidx.compose.ui.text.TextStyle, color: Color) {
    val a by animateFloatAsState(value.toFloat(), tween(650), label = "count")
    Text(if (a == value.toFloat()) format(value) else format(a.toDouble()), style = style, color = color)
}

@Composable
private fun ResultCard(color: Color, headline: String, value: Double?, format: (Double) -> String, sub: String? = null,
                       rows: List<Pair<String, String>> = emptyList(), chart: (@Composable () -> Unit)? = null) {
    Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(headline, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (value != null) Counting(value, format, MaterialTheme.typography.displaySmall, color)
            sub?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            chart?.let { Spacer(Modifier.height(16.dp)); it() }
            if (rows.isNotEmpty()) Spacer(Modifier.height(12.dp))
            rows.forEach { (k, v) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Text(k, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text(v, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
private fun Legend(items: List<Pair<Color, String>>) = Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 10.dp)) {
    items.forEach { (c, l) ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(c, CircleShape)); Spacer(Modifier.width(6.dp))
            Text(l, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Donut(parts: List<Pair<Float, Color>>, centre: String, caption: String) {
    val grow = rememberGrow(900)
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(170.dp)) {
            val stroke = 26.dp.toPx()
            val inset = stroke / 2
            val arc = Size(size.width - stroke, size.height - stroke)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arc, style = Stroke(stroke))
            val total = parts.sumOf { it.first.toDouble() }.toFloat().coerceAtLeast(0.0001f)
            var start = -90f
            parts.forEach { (v, c) ->
                val sweep = 360f * v / total * grow
                drawArc(c, start, sweep, false, Offset(inset, inset), arc, style = Stroke(stroke, cap = StrokeCap.Butt))
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centre, style = MaterialTheme.typography.titleLarge)
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Bars(data: List<Pair<Double, Double>>, base: Color, top: Color) {
    val grow = rememberGrow(900)
    Canvas(Modifier.fillMaxWidth().height(150.dp)) {
        if (data.isEmpty()) return@Canvas
        val maxV = data.maxOf { max(it.first, it.second) }.coerceAtLeast(1.0)
        val gap = 4.dp.toPx()
        val w = (size.width - gap * (data.size - 1)) / data.size
        data.forEachIndexed { i, (paid, value) ->
            val x = i * (w + gap)
            val hv = (value / maxV * size.height * grow).toFloat()
            val hp = (minOf(paid, value) / maxV * size.height * grow).toFloat()
            drawRoundRect(top, Offset(x, size.height - hv), Size(w, hv), CornerRadius(w / 3))
            drawRoundRect(base, Offset(x, size.height - hp), Size(w, hp), CornerRadius(w / 3))
        }
    }
}

@Composable
private fun Line(points: List<Double>, color: Color, goal: Double? = null, goalColor: Color = color) {
    val grow = rememberGrow(1000)
    Canvas(Modifier.fillMaxWidth().height(140.dp)) {
        if (points.size < 2) return@Canvas
        val maxV = max(points.max(), goal ?: 0.0).coerceAtLeast(1.0)
        fun y(v: Double) = (size.height - v / maxV * size.height).toFloat()
        val step = size.width / (points.size - 1)
        val shown = (points.size * grow).toInt().coerceIn(2, points.size)
        val path = Path().apply { moveTo(0f, y(points[0])); for (k in 1 until shown) lineTo(k * step, y(points[k])) }
        val fill = Path().apply { addPath(path); lineTo((shown - 1) * step, size.height); lineTo(0f, size.height); close() }
        drawPath(fill, color.copy(alpha = .14f))
        drawPath(path, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        goal?.let { drawLine(goalColor, Offset(0f, y(it)), Offset(size.width, y(it)), 2.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 10f))) }
    }
}

@Composable
private fun CurrencyPicker(code: String, onPick: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) { Text("Currency: $code ${Money.symbol(code).trim()}") }
        DropdownMenu(open, { open = false }) {
            CURRENCIES.forEach { c -> DropdownMenuItem(text = { Text("$c  ${Money.symbol(c).trim()}") }, onClick = { onPick(c); open = false }) }
        }
    }
}

private fun years(months: Int): String {
    val y = months / 12; val m = months % 12
    return listOfNotNull(if (y > 0) "$y yr" + (if (y > 1) "s" else "") else null, if (m > 0) "$m mo" else null).joinToString(" ").ifEmpty { "0 mo" }
}

// ---------- Screen ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculatorScreen(id: String, nav: Nav) {
    val context = LocalContext.current
    var cur by remember { mutableStateOf(Money.get(context)) }
    val sym = Money.symbol(cur)
    val f: (Double) -> String = { Money.fmt(cur, it) }
    val primary = MaterialTheme.colorScheme.primary
    val gold = MaterialTheme.colorScheme.secondary
    val bad = Color(0xFFC0602A)
    LaunchedEffect(id) { AppGraph.library.countToolUse() }

    ToolScaffold(id, nav) {
        CurrencyPicker(cur) { cur = it; Money.set(context, it) }
        when (id) {
            "loan" -> {
                var amount by remember { mutableStateOf("300000") }
                var rate by remember { mutableStateOf("6.5") }
                var yrs by remember { mutableStateOf("30") }
                var extra by remember { mutableStateOf("0") }
                NumField("Loan amount", amount, { amount = it }, prefix = sym)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumField("Interest rate", rate, { rate = it }, suffix = "%", modifier = Modifier.weight(1f))
                    NumField("Term", yrs, { yrs = it }, suffix = "years", modifier = Modifier.weight(1f))
                }
                NumField("Extra payment each month (optional)", extra, { extra = it }, prefix = sym)
                Calc.loan(amount.num(), rate.num(), yrs.num(), extra.num())?.let { r ->
                    ResultCard(primary, "Monthly payment", r.payment, f, "Principal and interest",
                        rows = buildList {
                            add("Total interest" to f(r.interest))
                            add("Total paid" to f(amount.num() + r.interest))
                            if (extra.num() > 0) {
                                add("Paid off in" to years(r.months))
                                add("Interest saved" to f(r.baseInterest - r.interest))
                                add("Time saved" to years(r.baseMonths - r.months))
                            }
                        }) {
                        Donut(listOf(amount.num().toFloat() to primary, r.interest.toFloat() to gold),
                            "${(r.interest / (amount.num() + r.interest) * 100).toInt()}%", "goes to interest")
                        Legend(listOf(primary to "Principal", gold to "Interest"))
                    }
                }
            }
            "growth" -> {
                var start by remember { mutableStateOf("1000") }
                var monthly by remember { mutableStateOf("500") }
                var rate by remember { mutableStateOf("7") }
                var yrs by remember { mutableStateOf(20) }
                var step by remember { mutableStateOf("0") }
                NumField("Starting amount", start, { start = it }, prefix = sym)
                NumField("Monthly investment", monthly, { monthly = it }, prefix = sym)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumField("Yearly return", rate, { rate = it }, suffix = "%", modifier = Modifier.weight(1f))
                    NumField("Raise it yearly by", step, { step = it }, suffix = "%", modifier = Modifier.weight(1f))
                }
                Label("Years")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 20, 25, 30, 40).forEach { y -> FilterChip(yrs == y, { yrs = y }, label = { Text("$y") }) }
                }
                val r = Calc.growth(start.num(), monthly.num(), rate.num(), yrs, step.num())
                ResultCard(primary, "Value after $yrs years", r.value, f, "Before tax and inflation",
                    rows = listOf("You put in" to f(r.contributed), "Growth" to f(r.value - r.contributed),
                        "Growth share" to "${if (r.value > 0) ((r.value - r.contributed) / r.value * 100).toInt() else 0}%")) {
                    Bars(r.yearly, primary, gold)
                    Legend(listOf(primary to "Your money", gold to "Growth"))
                }
            }
            "card" -> {
                var bal by remember { mutableStateOf("5000") }
                var apr by remember { mutableStateOf("22.9") }
                var pay by remember { mutableStateOf("200") }
                NumField("Card balance", bal, { bal = it }, prefix = sym)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumField("APR", apr, { apr = it }, suffix = "%", modifier = Modifier.weight(1f))
                    NumField("Monthly payment", pay, { pay = it }, prefix = sym, modifier = Modifier.weight(1f))
                }
                Calc.card(bal.num(), apr.num(), pay.num())?.let { r ->
                    if (r.months < 0) ResultCard(bad, "This payment never clears the debt", null, f,
                        "It doesn't cover the monthly interest of ${f(bal.num() * apr.num() / 1200)}.",
                        rows = listOf("Pay this to be debt-free in 2 years" to f(r.forTwoYears)))
                    else ResultCard(primary, "Debt-free in", null, f, null,
                        rows = listOf("Months" to "${r.months}", "Total interest" to f(r.interest), "Total paid" to f(bal.num() + r.interest),
                            "To clear it in 2 years, pay" to "${f(r.forTwoYears)}/mo")) {
                        Text(years(r.months), style = MaterialTheme.typography.displaySmall, color = primary)
                        Spacer(Modifier.height(12.dp))
                        Line(r.balances, bad)
                    }
                }
            }
            "fire" -> {
                var spend by remember { mutableStateOf("40000") }
                var saved by remember { mutableStateOf("50000") }
                var monthly by remember { mutableStateOf("1500") }
                var rate by remember { mutableStateOf("6") }
                var swr by remember { mutableStateOf("4") }
                NumField("Yearly spending in retirement", spend, { spend = it }, prefix = sym)
                NumField("Invested so far", saved, { saved = it }, prefix = sym)
                NumField("Invested every month", monthly, { monthly = it }, prefix = sym)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumField("Yearly return", rate, { rate = it }, suffix = "%", modifier = Modifier.weight(1f))
                    NumField("Withdrawal rate", swr, { swr = it }, suffix = "%", modifier = Modifier.weight(1f))
                }
                Calc.fire(spend.num(), saved.num(), monthly.num(), rate.num(), swr.num())?.let { r ->
                    ResultCard(primary, "Your FIRE number", r.target, f,
                        if (r.years < 0) "Not reached within 80 years at this pace." else "Reached in about ${"%.1f".format(r.years)} years",
                        rows = listOf("Progress today" to "${(saved.num() / r.target * 100).coerceAtMost(100.0).toInt()}%",
                            "Safe yearly income at ${swr.num()}%" to f(r.target * swr.num() / 100))) {
                        Line(r.path, primary, r.target, gold)
                        Legend(listOf(primary to "Your portfolio", gold to "FIRE number"))
                    }
                }
            }
            else -> {
                var rent by remember { mutableStateOf("1500") }
                var food by remember { mutableStateOf("500") }
                var bills by remember { mutableStateOf("300") }
                var other by remember { mutableStateOf("400") }
                var have by remember { mutableStateOf("2000") }
                var save by remember { mutableStateOf("300") }
                var months by remember { mutableStateOf(6) }
                Label("Essential monthly costs")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumField("Rent or mortgage", rent, { rent = it }, prefix = sym, modifier = Modifier.weight(1f))
                    NumField("Food", food, { food = it }, prefix = sym, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumField("Bills & insurance", bills, { bills = it }, prefix = sym, modifier = Modifier.weight(1f))
                    NumField("Transport & debt", other, { other = it }, prefix = sym, modifier = Modifier.weight(1f))
                }
                Label("Months of cover")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 6, 9, 12).forEach { m -> FilterChip(months == m, { months = m }, label = { Text("$m months") }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumField("Saved already", have, { have = it }, prefix = sym, modifier = Modifier.weight(1f))
                    NumField("Can save monthly", save, { save = it }, prefix = sym, modifier = Modifier.weight(1f))
                }
                val monthlyCost = rent.num() + food.num() + bills.num() + other.num()
                val target = monthlyCost * months
                val gap = max(0.0, target - have.num())
                val fill = Calc.monthsToFill(gap, save.num())
                ResultCard(primary, "Your emergency fund target", target, f,
                    when { gap == 0.0 -> "You're fully covered."; fill < 0 -> "Add a monthly saving to see when you'll get there."; else -> "Fully funded in ${years(fill)}" },
                    rows = listOf("Monthly essentials" to f(monthlyCost), "Still to save" to f(gap))) {
                    val pct = if (target > 0) (have.num() / target).coerceIn(0.0, 1.0) else 0.0
                    Donut(listOf(pct.toFloat() to primary, (1 - pct).toFloat() to Color.Transparent), "${(pct * 100).toInt()}%", "covered")
                }
            }
        }
        Text("Estimates for planning only, not financial advice.", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
