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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(vm: GroupViewModel, celebrate: Boolean, onBack: () -> Unit) {
    val group by vm.group.collectAsState()
    val members by vm.members.collectAsState()
    val messages by vm.messages.collectAsState()
    val sending by vm.sending.collectAsState()
    val error by vm.error.collectAsState()
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var draft by rememberSaveable { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    var askLeave by remember { mutableStateOf(false) }
    var askWhatsapp by remember { mutableStateOf(false) }
    BackHandler(onBack = onBack)

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
                item { Header(g, members.map { it.name to it.photoUrl }) }
                item { InviteCard(g) }
                item {
                    val link = g.whatsappLink
                    if (link != null) {
                        GradientButton(
                            "Open our class WhatsApp group", { Invites.openUrl(context, link) },
                            Modifier.fillMaxWidth(), brush = Brush.linearGradient(listOf(Brand.WhatsApp, Color(0xFF128C7E))),
                        )
                    } else {
                        GlassCard(Modifier.fillMaxWidth(), onClick = { askWhatsapp = true }, padding = PaddingValues(16.dp)) {
                            Text("💬  Already have a WhatsApp group?", style = MaterialTheme.typography.titleMedium)
                            Text("Link it here so everyone who joins can hop in with one tap.", color = Brand.TextDim, fontSize = 13.sp)
                        }
                    }
                }
                item {
                    SecondaryTabRow(tab, containerColor = Color.Transparent) {
                        Tab(tab == 0, { tab = 0 }, text = { Text("Wall") })
                        Tab(tab == 1, { tab = 1 }, text = { Text("Batchmates (${members.size})") })
                    }
                }
                if (tab == 0) {
                    item { Composer(draft, { draft = it }, sending) { vm.send(draft) { draft = "" } } }
                    error?.let { e -> item { Text(e, color = MaterialTheme.colorScheme.error) } }
                    if (messages.isEmpty()) item {
                        EmptyState("👋", "Break the ice", "Post the first memory — a teacher's nickname, the canteen samosa, the annual day disaster…")
                    }
                    items(messages, key = { it.id }) { m -> MessageBubble(m, mine = m.uid == vm.user.uid) }
                } else {
                    items(members, key = { it.uid }) { m ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0x0FFFFFFF)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(m.name, m.photoUrl, 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(m.name + if (m.uid == vm.user.uid) " (you)" else "", style = MaterialTheme.typography.titleMedium)
                                if (m.joinedAt > 0) Text("Joined " + DateUtils.getRelativeTimeSpanString(m.joinedAt), color = Brand.TextDim, fontSize = 12.sp)
                            }
                            if (m.uid == g.createdBy) Pill("FOUNDER", Brand.Sun)
                        }
                    }
                    item {
                        Text(
                            "Someone missing? Every invite brings the reunion closer 🎉",
                            color = Brand.TextDim, modifier = Modifier.fillMaxWidth().padding(8.dp).navigationBarsPadding(),
                        )
                    }
                }
            }
            if (celebrate) ConfettiBurst(Modifier.fillMaxSize(), key = g.id)
        }
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
private fun Header(g: Group, faces: List<Pair<String, String?>>) {
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
                Pill(g.grade.label, Color.White); Pill("Section ${g.section}", Color.White)
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarStack(faces, g.memberCount)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (g.memberCount <= 1) "You're the first one here!" else "${g.memberCount} batchmates reunited",
                    color = Color.White, fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun InviteCard(g: Group) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    GlassCard(Modifier.fillMaxWidth().animateContentSize()) {
        Text("Bring your batch back", style = MaterialTheme.typography.titleLarge)
        Text("One message in your old group chat can bring back the whole class.", color = Brand.TextDim, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            BrandCircle("WhatsApp", { Text("💬", fontSize = 24.sp) }, Brush.linearGradient(listOf(Brand.WhatsApp, Color(0xFF128C7E)))) {
                Invites.share(context, ShareTarget.WHATSAPP, g)
            }
            BrandCircle("Instagram", { Text("📸", fontSize = 24.sp) }, Brand.Instagram) {
                Invites.share(context, ShareTarget.INSTAGRAM, g)
            }
            BrandCircle("Facebook", { Text("f", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White) },
                Brush.linearGradient(listOf(Brand.Facebook, Color(0xFF0A58CA)))) {
                Invites.share(context, ShareTarget.FACEBOOK, g)
            }
            BrandCircle("More", { Icon(Icons.Rounded.Share, null, tint = Color.White) }, Brand.Warm) {
                Invites.share(context, ShareTarget.MORE, g)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0x22000000))
                .clickable { Invites.copy(context, Invites.message(g)); copied = true }.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("INVITE CODE", style = Eyebrow)
                Text(g.inviteCode, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            }
            Text(if (copied) "Copied ✓" else "Copy", color = if (copied) Brand.Mint else Brand.TextDim)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Rounded.ContentCopy, null, tint = Brand.TextDim, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun Composer(text: String, onText: (String) -> Unit, sending: Boolean, onSend: () -> Unit) {
    Row(verticalAlignment = Alignment.Bottom) {
        OutlinedTextField(
            text, { onText(it.take(1000)) }, Modifier.weight(1f),
            placeholder = { Text("Share a memory with your batch…") },
            shape = RoundedCornerShape(18.dp), maxLines = 4,
        )
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(Brand.Primary),
            contentAlignment = Alignment.Center,
        ) {
            if (sending) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            else IconButton(onSend, enabled = text.isNotBlank()) { Icon(Icons.AutoMirrored.Rounded.Send, "Post", tint = Color.White) }
        }
    }
}

@Composable
private fun MessageBubble(m: Message, mine: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        if (!mine) { Avatar(m.name, m.photoUrl, 34.dp); Spacer(Modifier.width(8.dp)) }
        Column(
            Modifier.widthIn(max = 300.dp).clip(
                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = if (mine) 18.dp else 4.dp, bottomEnd = if (mine) 4.dp else 18.dp),
            ).background(
                if (mine) Brand.Primary else Brush.linearGradient(listOf(Color(0x26FFFFFF), Color(0x14FFFFFF))),
            ).padding(12.dp),
        ) {
            if (!mine) Text(m.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Brand.pair(m.name)[0])
            Text(m.text)
            Text(DateUtils.getRelativeTimeSpanString(m.sentAt).toString(), fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.End))
        }
    }
}

