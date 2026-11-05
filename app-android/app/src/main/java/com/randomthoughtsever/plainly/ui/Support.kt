package com.randomthoughtsever.plainly.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomthoughtsever.plainly.data.Support

/** Why support, then the ways to do it: Google Play tips, and (when enabled) PayPal and UPI, the same as the blog. */
@Composable
fun SupportScreen(nav: Nav) {
    val context = nav.context
    LaunchedEffect(Unit) { Support.load(context) }
    val currency = AppGraph.profile.currency()
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()) {
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Text("Support Plainly", style = MaterialTheme.typography.titleLarge)
            }
            Column(Modifier.padding(horizontal = 20.dp)) {
                Box(Modifier.fillMaxWidth().height(150.dp).background(Brush.linearGradient(listOf(Color(0xFFB0472A), Gold)), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center) { Text("☕💛", fontSize = 56.sp) }
                Spacer(Modifier.height(16.dp))
                Text("Plainly is free, and made by one person.", style = MaterialTheme.typography.headlineSmall)
                Text("Every guide, tool, puzzle and calculator is free, with no account and no paywall. If it has helped you, a small tip keeps " +
                    "new guides coming and the ads few. It's completely optional, and everything stays free either way.",
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                if (Support.thanks || Support.supporter(context)) Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Text("💛 Thank you for supporting Plainly!", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                }
                Text("TIP THROUGH GOOGLE PLAY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
                when (Support.status) {
                    "ready" -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Support.tips.forEachIndexed { i, p ->
                            TipButton(listOf("☕", "🍰", "🎁")[i.coerceIn(0, 2)], listOf("A coffee", "A treat", "A big thank-you")[i.coerceIn(0, 2)],
                                p.oneTimePurchaseOfferDetails?.formattedPrice ?: "", Modifier.weight(1f)) { (context as? Activity)?.let { Support.tip(it, p) } }
                        }
                    }
                    "unavailable" -> Text("Tips through Google Play work once Plainly is installed from Google Play.", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else -> CircularProgressIndicator(Modifier.padding(8.dp))
                }
                if (Support.linksEnabled) {
                    if (currency == "INR") UpiRow(nav)
                    Text("PAYPAL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
                    val cur = Support.paypalCurrency(currency)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Support.paypalAmounts(currency).forEachIndexed { i, a ->
                            TipButton(listOf("☕", "🍰", "🎁")[i], "PayPal", "${symbolFor(cur)}$a", Modifier.weight(1f)) { open(nav, Support.paypal(a, cur)) }
                        }
                    }
                    if (currency != "INR") UpiRow(nav)
                }
                Text("Other ways to help, free: rate Plainly, share a guide, or tell a friend.", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { open(nav, "market://details?id=com.randomthoughtsever.plainly") }) { Text("⭐ Rate") }
                    TextButton(onClick = { nav.share("Plainly: Money, Mind & Work", "https://play.google.com/store/apps/details?id=com.randomthoughtsever.plainly") }) { Text("📤 Share the app") }
                }
                Spacer(Modifier.height(24.dp)); Spacer(Modifier.navigationBarsPadding())
            }
        }
        if (Support.thanks) {
            Confetti(Modifier.fillMaxSize())
            LaunchedEffect(Unit) { AppGraph.progress.award("tip:${System.currentTimeMillis() / Support.DAY}", 100, "Thank you for your support!") }
        }
    }
}

@Composable
private fun UpiRow(nav: Nav) {
    Text("UPI (INDIA)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Support.UPI_AMOUNTS.forEachIndexed { i, a -> TipButton(listOf("☕", "🍰", "🎁")[i], "UPI", "₹$a", Modifier.weight(1f)) { open(nav, Support.upi(a)) } }
    }
}

@Composable
private fun TipButton(emoji: String, label: String, price: String, modifier: Modifier, onClick: () -> Unit) =
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = modifier) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 28.sp)
            Text(price, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }

private fun open(nav: Nav, url: String) = runCatching {
    nav.context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(url)), "Support Plainly"))
}

/**
 * A small, polite card in places where the reader has just got something good (finished a guide, the Daily Run, a
 * simulated year). Shows only when [Support.shouldAsk] says so, at most once a week, and can be closed.
 */
@Composable
fun SupportNudge(nav: Nav, line: String, modifier: Modifier = Modifier) {
    val context = nav.context
    val show = remember { Support.shouldAsk(context, AppGraph.library.reads(), AppGraph.progress.runs()) }
    var open by remember { mutableStateOf(show) }
    LaunchedEffect(show) { if (show) Support.asked(context) }
    if (!open) return
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("💛", fontSize = 26.sp); Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(line, style = MaterialTheme.typography.titleSmall)
                Text("Plainly is free. A small tip helps keep it that way.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { nav.support() }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text("Support Plainly", fontWeight = FontWeight.Bold) }
            }
            IconButton(onClick = { open = false; Support.dismissed(context) }) { Icon(Icons.Rounded.Close, "Not now") }
        }
    }
}
