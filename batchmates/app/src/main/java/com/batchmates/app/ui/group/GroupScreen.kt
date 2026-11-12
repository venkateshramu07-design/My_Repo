package com.batchmates.app.ui.group

import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batchmates.app.data.Group
import com.batchmates.app.data.Message
import com.batchmates.app.ui.components.AuroraBackground
import com.batchmates.app.ui.components.Avatar
import com.batchmates.app.ui.components.AvatarStack
import com.batchmates.app.ui.components.BrandCircle
import com.batchmates.app.ui.components.ConfettiBurst
import com.batchmates.app.ui.components.EmptyState
import com.batchmates.app.ui.components.GlassCard
import com.batchmates.app.ui.components.GradientButton
import com.batchmates.app.ui.components.Pill
import com.batchmates.app.ui.theme.Brand
import com.batchmates.app.ui.theme.Eyebrow
import com.batchmates.app.util.Invites
import com.batchmates.app.util.ShareTarget

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GroupScreen(vm: GroupViewModel, celebrate: Boolean, initialTab: Int = 0, onBack: () -> Unit) {
    val group by vm.group.collectAsState()
    val members by vm.members.collectAsState()
    val messages by vm.messages.collectAsState()
    val missing by vm.missing.collectAsState()
    val reunion by vm.reunion.collectAsState()
    val prompt by vm.prompt.collectAsState()
    val sending by vm.sending.collectAsState()
    val error by vm.error.collectAsState()
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    var draft by rememberSaveable { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    var askLeave by remember { mutableStateOf(false) }
    var askWhatsapp by remember { mutableStateOf(false) }
    var editCard by remember { mutableStateOf(false) }
    BackHandler(onBack = onBack)
    // Right after joining, ask the one question classmates care about most.
    LaunchedEffect(celebrate, members.isNotEmpty()) {
        if (celebrate && members.isNotEmpty()) {
            kotlinx.coroutines.delay(2_500)
            if (members.firstOrNull { it.uid == vm.user.uid }?.hasCard != true) editCard = true
        }
    }

    AuroraBackground(intensity = 0.6f) {
        val g = group
        if (g == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().statusBarsPadding().imePadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                        Spacer(Modifier.weight(1f))
                        IconButton({ Invites.share(context, ShareTarget.MORE, g) }) { Icon(Icons.Rounded.Share, "Share") }
                        Box {
                            IconButton({ menu = true }) { Icon(Icons.Rounded.MoreVert, "More") }
                            DropdownMenu(menu, { menu = false }) {
                                DropdownMenuItem(
                                    text = { Text(if (g.whatsappLink == null) "Link WhatsApp group" else "Change WhatsApp link") },
                                    onClick = { menu = false; askWhatsapp = true },
                                )
                                DropdownMenuItem(
                                    text = { Text("Leave batch") },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.ExitToApp, null) },
                                    onClick = { menu = false; askLeave = true },
                                )
                            }
                        }
                    }
                }
                item { Header(g, members.map { it.name to it.photoUrl }, missing.count { !it.found }) }
                item { InviteCard(g, vm::sharedInvite) { askWhatsapp = true } }
                stickyHeader(key = "tabs") {
                    SecondaryTabRow(tab, containerColor = Brand.Night.copy(alpha = 0.92f)) {
                        Tab(tab == 0, { tab = 0 }, text = { Text("Wall") })
                        Tab(tab == 1, { tab = 1 }, text = { Text("Classmates") })
                        Tab(tab == 2, { tab = 2 }, text = { Text(if (reunion?.lockedDate != null) "Reunion 🎉" else "Reunion") })
                    }
                }
                error?.let { e -> item { Text(e, color = MaterialTheme.colorScheme.error) } }
                when (tab) {
                    0 -> wallTab(vm, messages, prompt, draft, { draft = it }, sending)
                    1 -> classmatesTab(vm, g, members, missing) { editCard = true }
                    else -> reunionTab(vm, g, members, reunion)
                }
                item { Spacer(Modifier.navigationBarsPadding()) }
            }
            if (celebrate) ConfettiBurst(Modifier.fillMaxSize(), key = g.id)
        }
    }

    if (editCard) {
        val mine = members.firstOrNull { it.uid == vm.user.uid }
        CardSheet(mine, onDismiss = { editCard = false }) { city, now, memory -> vm.saveCard(city, now, memory); editCard = false }
    }
    if (askLeave) AlertDialog(
        onDismissRequest = { askLeave = false },
        title = { Text("Leave this batch?") },
        text = { Text("You can rejoin anytime from the school page or an invite link.") },
        confirmButton = { TextButton({ askLeave = false; vm.leave(onBack) }) { Text("Leave") } },
        dismissButton = { TextButton({ askLeave = false }) { Text("Stay") } },
    )
    if (askWhatsapp) {
        var link by remember { mutableStateOf(group?.whatsappLink.orEmpty()) }
        AlertDialog(
            onDismissRequest = { askWhatsapp = false },
            title = { Text("Link WhatsApp group") },
            text = {
                Column {
                    Text("In WhatsApp: Group info → Invite via link → Copy link. Paste it here.", color = Brand.TextDim)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(link, { link = it }, singleLine = true, placeholder = { Text("https://chat.whatsapp.com/…") })
                }
            },
            confirmButton = { TextButton({ askWhatsapp = false; vm.setWhatsapp(link) }) { Text("Save") } },
            dismissButton = { TextButton({ askWhatsapp = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Header(g: Group, faces: List<Pair<String, String?>>, missing: Int) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Brand.Violet, Brand.Orchid, Brand.Coral))).padding(22.dp),
    ) {
        Text("'${(g.year % 100).toString().padStart(2, '0')}", fontSize = 72.sp, fontWeight = FontWeight.Black,
            color = Color.White.copy(alpha = 0.18f), modifier = Modifier.align(Alignment.TopEnd))
        Column {
            Text("CLASS OF ${g.year}", style = Eyebrow.copy(color = Color.White.copy(alpha = 0.9f)))
            Spacer(Modifier.height(4.dp))
            Text(g.schoolName, style = MaterialTheme.typography.headlineSmall, color = Color.White, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (g.regionLabel.isNotBlank()) Text(g.regionLabel, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(g.track.short, Color.White); Pill(g.sectionLabel, Color.White)
            }
            g.milestone(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))?.let { years ->
                Spacer(Modifier.height(10.dp))
                Text(
                    "🎉 ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} is your $years-year reunion! Get everyone in.",
                    Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x33000000)).padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Brand.Sun, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarStack(faces, g.memberCount)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        g.memberCount <= 1 -> "You're the first one here!"
                        missing > 0 -> "${g.memberCount} back · $missing still missing"
                        else -> "${g.memberCount} batchmates reunited"
                    },
                    color = Color.White, fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun InviteCard(g: Group, onShared: () -> Unit, onLinkWhatsapp: () -> Unit) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    // Compact on purpose: the wall, classmates and reunion should start on the first screen.
    GlassCard(Modifier.fillMaxWidth().animateContentSize(), padding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Bring your batch back", style = MaterialTheme.typography.titleMedium)
                Row(
                    Modifier.clip(RoundedCornerShape(10.dp)).clickable { Invites.copy(context, Invites.message(g)); copied = true }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Code ", color = Brand.TextDim, fontSize = 13.sp)
                    Text(g.inviteCode, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Brand.Sun)
                    Spacer(Modifier.width(6.dp))
                    Text(if (copied) "Copied ✓" else "Copy", color = if (copied) Brand.Mint else Brand.TextDim, fontSize = 12.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShareDot(Brush.linearGradient(listOf(Brand.WhatsApp, Color(0xFF128C7E))), "Invite on WhatsApp", {
                    Invites.share(context, ShareTarget.WHATSAPP, g); onShared()
                }) { Text("💬", fontSize = 18.sp) }
                ShareDot(Brand.Instagram, "Invite on Instagram", {
                    Invites.share(context, ShareTarget.INSTAGRAM, g); onShared()
                }) { Text("📸", fontSize = 18.sp) }
                ShareDot(Brush.linearGradient(listOf(Brand.Facebook, Color(0xFF0A58CA))), "Invite on Facebook", {
                    Invites.share(context, ShareTarget.FACEBOOK, g); onShared()
                }) { Text("f", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White) }
                ShareDot(Brand.Warm, "Share", {
                    Invites.share(context, ShareTarget.MORE, g); onShared()
                }) { Icon(Icons.Rounded.Share, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
            }
        }
        Spacer(Modifier.height(10.dp))
        val link = g.whatsappLink
        Text(
            if (link != null) "💬  Open our class WhatsApp group  →" else "💬  Have a class WhatsApp group? Link it here  →",
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(if (link != null) Brand.WhatsApp.copy(alpha = 0.25f) else Color(0x14FFFFFF))
                .clickable { if (link != null) Invites.openUrl(context, link) else onLinkWhatsapp() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ShareDot(bg: Brush, label: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(bg)
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CardSheet(me: com.batchmates.app.data.Member?, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var city by remember { mutableStateOf(me?.city.orEmpty()) }
    var now by remember { mutableStateOf(me?.now.orEmpty()) }
    var memory by remember { mutableStateOf(me?.memory.orEmpty()) }
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Brand.Deep,
    ) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("📍 Where are you now?", style = MaterialTheme.typography.headlineSmall)
            Text("Your batchmates are dying to know. Share as much or as little as you like.", color = Brand.TextDim)
            OutlinedTextField(city, { city = it.take(60) }, Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("City you live in") }, shape = RoundedCornerShape(16.dp))
            OutlinedTextField(now, { now = it.take(80) }, Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("What you do now (job, business, studies…)") }, shape = RoundedCornerShape(16.dp))
            OutlinedTextField(memory, { memory = it.take(200) }, Modifier.fillMaxWidth(), maxLines = 3,
                label = { Text("A favourite school memory") }, shape = RoundedCornerShape(16.dp))
            GradientButton("Save my card", { onSave(city, now, memory) }, Modifier.fillMaxWidth())
        }
    }
}
