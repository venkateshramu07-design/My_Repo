package com.randomthoughtsever.plainly.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import com.randomthoughtsever.plainly.data.ArticlePicks
import com.randomthoughtsever.plainly.data.NewPosts
import com.randomthoughtsever.plainly.data.Reminders

/**
 * Plainly's own invitation to turn on notifications (shown before Android's permission box, which only appears after
 * "Yes"). Shown on Today once the reader is set up; if they say "Not now" it comes back after 3 days, at most 3 times.
 */
object NotifyPrompt {
    const val MAX_SHOWS = 3
    const val GAP_MS = 3L * 86_400_000

    fun shouldShow(c: Context, onboarded: Boolean, route: String?, now: Long = System.currentTimeMillis()): Boolean {
        if (android.os.Build.VERSION.SDK_INT < 33 || !onboarded || route != "today" || NewPosts.canNotify(c)) return false
        val p = NewPosts.prefs(c)
        val shown = p.getInt("notifyPromptShown", 0)
        return shown < MAX_SHOWS && (shown == 0 || now - p.getLong("notifyPromptAt", 0) >= GAP_MS)
    }

    fun markShown(c: Context, now: Long = System.currentTimeMillis()) {
        val p = NewPosts.prefs(c)
        p.edit().putInt("notifyPromptShown", p.getInt("notifyPromptShown", 0) + 1).putLong("notifyPromptAt", now).apply()
    }

    fun turnedOn(c: Context) {
        NewPosts.setEnabled(c, true); ArticlePicks.schedule(c); Reminders.schedule(c)
        runCatching { AppGraph.progress.award("notifications-on", 50, "Notifications on", null) }
    }
}

@Composable
fun NotifyPromptDialog(sampleTitle: String?, onDone: () -> Unit) {
    val context = LocalContext.current
    val system = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) NotifyPrompt.turnedOn(context)
        onDone()
    }
    fun yes() {
        val act = context as? Activity
        val p = NewPosts.prefs(context)
        // If Android has stopped showing its box (the reader declined twice before), open the app's notification settings.
        val blocked = act != null && p.getBoolean("notifySystemAsked", false) &&
            !ActivityCompat.shouldShowRequestPermissionRationale(act, android.Manifest.permission.POST_NOTIFICATIONS)
        p.edit().putBoolean("notifySystemAsked", true).apply()
        if (blocked) {
            runCatching { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }
            onDone()
        } else system.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    val wiggle = rememberInfiniteTransition(label = "bell")
    val angle by wiggle.animateFloat(-14f, 14f, infiniteRepeatable(tween(260, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "angle")
    val glow by wiggle.animateFloat(0.85f, 1.08f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "glow")
    val accent = Color(0xFFE9844A)

    Dialog(onDismissRequest = onDone, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp,
            modifier = Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
            Column {
                // Header: warm gradient, a ringing bell and a preview of a real Plainly notification.
                Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF1F2A44), Color(0xFF3B2F4A))))
                    .padding(top = 26.dp, bottom = 22.dp, start = 20.dp, end = 20.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(Modifier.size((76 * glow).dp).clip(CircleShape).background(accent.copy(alpha = .22f)))
                            Text("🔔", fontSize = 40.sp, modifier = Modifier.rotate(angle))
                        }
                        Spacer(Modifier.height(16.dp))
                        Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(18.dp))) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(accent), contentAlignment = Alignment.Center) {
                                    Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Plainly · now", fontSize = 11.sp, color = Color(0xFF6B6B6B))
                                    Text("New on Plainly", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF15171D))
                                    Text(sampleTitle ?: "A new guide you'll want to read", fontSize = 13.sp, color = Color(0xFF3A3A3A),
                                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 12.dp)) {
                    Text("Be the first to know", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text("Money moves fast. Get the guides that matter while they still do.", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Benefit("⚡", "New guides the moment they're live", "Rates, tax changes and pay updates, first")
                        Benefit("📖", "One great read every few hours", "Picked from 300+ guides, never at night")
                        Benefit("🔥", "A heads-up before your streak breaks", "At most one reminder a day")
                    }
                    Spacer(Modifier.height(18.dp))
                    Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = .14f), modifier = Modifier.fillMaxWidth()) {
                        Text("🎁  Turn on now and get +50 XP", modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = ::yes, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(54.dp).semantics { contentDescription = "Yes, keep me posted" }) {
                        Text("Yes, keep me posted", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                        Text("Not now", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("You can change this any time in Settings.", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun Benefit(icon: String, title: String, sub: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
