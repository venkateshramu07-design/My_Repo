package com.batchmates.app.ui.group

import android.text.format.DateUtils
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batchmates.app.data.Group
import com.batchmates.app.data.Member
import com.batchmates.app.data.Message
import com.batchmates.app.data.MessageKind
import com.batchmates.app.data.MissingPerson
import com.batchmates.app.data.Reunion
import com.batchmates.app.data.Rsvp
import com.batchmates.app.ui.components.Avatar
import com.batchmates.app.ui.components.AvatarStack
import com.batchmates.app.ui.components.EmptyState
import com.batchmates.app.ui.components.GlassCard
import com.batchmates.app.ui.components.GradientButton
import com.batchmates.app.ui.components.Pill
import com.batchmates.app.ui.theme.Brand
import com.batchmates.app.ui.theme.Eyebrow
import com.batchmates.app.util.Invites
import com.batchmates.app.util.ShareTarget
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

private val REACTIONS = listOf("❤️", "😂", "😭", "🙌")

// =================================================================== Wall

fun LazyListScope.wallTab(
    vm: GroupViewModel,
    messages: List<Message>,
    prompt: String,
    draft: String,
    onDraft: (String) -> Unit,
    sending: Boolean,
) {
    item(key = "throwback") { ThrowbackCard(prompt, onRoll = vm::rollPrompt) { answer, done -> vm.answerPrompt(answer, done) } }
    item(key = "composer") { Composer(draft, onDraft, sending) { vm.send(draft) { onDraft("") } } }
    if (messages.isEmpty()) item(key = "empty") {
        EmptyState("👋", "Break the ice", "Answer the throwback above or post the first memory.")
    }
    items(messages, key = { it.id }) { m ->
        when (m.kind) {
            MessageKind.JOINED, MessageKind.FOUND -> EventPill(m)
            else -> Column {
                MessageBubble(m, mine = m.uid == vm.user.uid)
                ReactionBar(m, vm.user.uid, mine = m.uid == vm.user.uid) { e -> vm.toggleReaction(m, e) }
            }
        }
    }
}

@Composable
private fun ThrowbackCard(prompt: String, onRoll: () -> Unit, onAnswer: (String, () -> Unit) -> Unit) {
    var answer by rememberSaveable(prompt) { mutableStateOf("") }
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF2B1F6B), Color(0xFF5A2A7A), Color(0xFF7A2E55))))
            .padding(18.dp),
    ) {
        Column(Modifier.animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎲 THROWBACK", style = Eyebrow, modifier = Modifier.weight(1f))
                IconButton(onRoll) { Icon(Icons.Rounded.Casino, "Another question", tint = Brand.Sun) }
            }
            Text(prompt, style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    answer, { answer = it.take(500) }, Modifier.weight(1f),
                    placeholder = { Text("Your answer…") }, shape = RoundedCornerShape(16.dp), maxLines = 3,
                )
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Brand.Warm), contentAlignment = Alignment.Center) {
                    IconButton({ onAnswer(answer) { answer = "" } }, enabled = answer.isNotBlank()) {
                        Icon(Icons.AutoMirrored.Rounded.Send, "Answer", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
internal fun Composer(text: String, onText: (String) -> Unit, sending: Boolean, onSend: () -> Unit) {
    Row(verticalAlignment = Alignment.Bottom) {
        OutlinedTextField(
            text, { onText(it.take(1000)) }, Modifier.weight(1f),
            placeholder = { Text("Share a memory, a photo story, a hello…") },
            shape = RoundedCornerShape(18.dp), maxLines = 4,
        )
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(Brand.Primary), contentAlignment = Alignment.Center) {
            if (sending) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            else IconButton(onSend, enabled = text.isNotBlank()) { Icon(Icons.AutoMirrored.Rounded.Send, "Post", tint = Color.White) }
        }
    }
}

@Composable
private fun EventPill(m: Message) {
    val found = m.kind == MessageKind.FOUND
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            Modifier.clip(RoundedCornerShape(50))
                .background(if (found) Brush.linearGradient(listOf(Brand.Sun.copy(alpha = 0.3f), Brand.Coral.copy(alpha = 0.3f)))
                else Brush.linearGradient(listOf(Color(0x1FFFFFFF), Color(0x14FFFFFF))))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(m.name, m.photoUrl, 24.dp)
            Spacer(Modifier.width(8.dp))
            Text(m.text, fontSize = 13.sp, fontWeight = if (found) FontWeight.Bold else FontWeight.Medium,
                color = if (found) Brand.Sun else Color.White)
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
            m.prompt?.let {
                Text("🎲 $it", fontSize = 12.sp, color = Brand.Sun, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
            }
            Text(m.text)
            Text(DateUtils.getRelativeTimeSpanString(m.sentAt).toString(), fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
private fun ReactionBar(m: Message, me: String, mine: Boolean, onToggle: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp, start = if (mine) 0.dp else 42.dp),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        REACTIONS.forEach { e ->
            val who = m.reactions[e].orEmpty()
            val on = me in who
            Text(
                if (who.isEmpty()) e else "$e ${who.size}",
                Modifier.padding(end = 6.dp).clip(RoundedCornerShape(50))
                    .background(if (on) Brand.Violet.copy(alpha = 0.45f) else Color(0x14FFFFFF))
                    .clickable { onToggle(e) }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                fontSize = 12.sp, color = if (who.isEmpty()) Color.White.copy(alpha = 0.55f) else Color.White,
            )
        }
    }
}

// =================================================================== Classmates

fun LazyListScope.classmatesTab(
    vm: GroupViewModel,
    group: Group,
    members: List<Member>,
    missing: List<MissingPerson>,
    onEditCard: () -> Unit,
) {
    val me = members.firstOrNull { it.uid == vm.user.uid }
    val earlyBirds = members.sortedBy { it.joinedAt }.take(3).map { it.uid }.toSet()
    val stillMissing = missing.filter { !it.found }
    item(key = "progress") { ReunionProgress(members.size, stillMissing.size) }
    item(key = "mycard") {
        if (me == null || !me.hasCard) {
            GlassCard(Modifier.fillMaxWidth(), onClick = onEditCard) {
                Text("📍 Where are you now?", style = MaterialTheme.typography.titleMedium)
                Text("Add your city, what you do now and a favourite memory. It's the first thing classmates look for.",
                    color = Brand.TextDim, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                GradientButton("Add my card", onEditCard, Modifier.fillMaxWidth(), brush = Brand.Warm)
            }
        }
    }
    item(key = "found-title") { Text("👥 Back together · ${members.size}", style = MaterialTheme.typography.titleMedium) }
    items(members.sortedWith(compareByDescending<Member> { it.uid == vm.user.uid }.thenByDescending { it.hasCard }), key = { "m" + it.uid }) { m ->
        MemberCard(
            m, isMe = m.uid == vm.user.uid,
            badges = buildList {
                if (m.uid == group.createdBy) add("🌱 Founder")
                if (m.invites >= 3) add("🔗 Connector")
                if (m.uid in earlyBirds && m.uid != group.createdBy && members.size > 5) add("⭐ Early bird")
            },
            onEdit = onEditCard,
        )
    }
    item(key = "missing-title") { MissingHeader(stillMissing.size) }
    item(key = "missing-add") { AddMissing(vm::addMissing) }
    items(missing.sortedBy { it.found }, key = { "x" + it.id }) { p ->
        MissingRow(p, group, members, vm.user, onInvited = { vm.invitedMissing(p) }, onRemove = { vm.removeMissing(p) })
    }
}

@Composable
private fun ReunionProgress(back: Int, missing: Int) {
    val total = (back + missing).coerceAtLeast(1)
    val p by animateFloatAsState(back.toFloat() / total, tween(900), label = "p")
    GlassCard(Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator({ p }, Modifier.size(64.dp), color = Brand.Sun, trackColor = Color(0x22FFFFFF), strokeWidth = 7.dp)
                Text("${(p * 100).toInt()}%", fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("$back back · $missing still missing", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (missing == 0) "Add the names you remember — let's find everyone."
                    else "Every name you remember is one invite away.",
                    color = Brand.TextDim, fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun MemberCard(m: Member, isMe: Boolean, badges: List<String>, onEdit: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), onClick = if (isMe) onEdit else null, corner = 20.dp, padding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(m.name, m.photoUrl, 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(m.name + if (isMe) " (you)" else "", style = MaterialTheme.typography.titleMedium)
                val line = listOfNotNull(m.city.takeIf { it.isNotBlank() }?.let { "📍 $it" }, m.now.takeIf { it.isNotBlank() }?.let { "💼 $it" })
                if (line.isNotEmpty()) Text(line.joinToString("  ·  "), color = Brand.TextDim, fontSize = 13.sp)
                else if (m.joinedAt > 0) Text("Joined " + DateUtils.getRelativeTimeSpanString(m.joinedAt), color = Brand.TextDim, fontSize = 12.sp)
            }
            if (isMe) TextButton(onEdit) { Text(if (m.hasCard) "Edit" else "Add card") }
        }
        if (badges.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                badges.forEach { Pill(it, Brand.Sun) }
            }
        }
        if (m.memory.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text("“${m.memory}”", fontStyle = FontStyle.Italic, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
        }
    }
}

@Composable
private fun MissingHeader(count: Int) {
    Column(Modifier.padding(top = 8.dp)) {
        Text("🔍 Who's missing?", style = MaterialTheme.typography.titleLarge)
        Text(
            if (count == 0) "Add classmates you remember. Each name becomes a personal invite."
            else "$count classmate${if (count == 1) "" else "s"} still to find. Tap Invite — a personal note beats a group link.",
            color = Brand.TextDim, fontSize = 13.sp,
        )
    }
}

@Composable
private fun AddMissing(onAdd: (String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    GlassCard(Modifier.fillMaxWidth(), corner = 20.dp, padding = PaddingValues(14.dp)) {
        OutlinedTextField(name, { name = it.take(60) }, Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Name you remember") }, shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(note, { note = it.take(100) }, Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("A hint (optional) — e.g. cricket captain, last bench") }, shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(10.dp))
        GradientButton("Add to the missing list", { onAdd(name, note); name = ""; note = "" }, Modifier.fillMaxWidth(),
            enabled = name.trim().length >= 2)
    }
}

@Composable
private fun MissingRow(
    p: MissingPerson, group: Group, members: List<Member>, me: com.batchmates.app.data.AppUser,
    onInvited: () -> Unit, onRemove: () -> Unit,
) {
    val context = LocalContext.current
    GlassCard(Modifier.fillMaxWidth(), corner = 20.dp, padding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape)
                    .background(if (p.found) Brand.Mint.copy(alpha = 0.3f) else Color(0x14FFFFFF))
                    .border(1.5.dp, if (p.found) Brand.Mint else Color(0x55FFFFFF), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text(if (p.found) "✅" else "❓", fontSize = 20.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, style = MaterialTheme.typography.titleMedium)
                if (p.note.isNotBlank()) Text(p.note, color = Brand.TextDim, fontSize = 13.sp)
                Text(
                    if (p.found) "Found! 🎉 Back in the batch" else "Added by ${p.addedByName}" + if (p.invited > 0) " · invited ${p.invited}×" else "",
                    color = if (p.found) Brand.Mint else Brand.TextDim, fontSize = 12.sp,
                )
            }
            if (!p.found && p.addedBy == me.uid) IconButton(onRemove) { Icon(Icons.Rounded.Close, "Remove", tint = Brand.TextDim) }
        }
        if (!p.found) {
            Spacer(Modifier.height(10.dp))
            val text = Invites.missingMessage(group, p.name, me.name.substringBefore(' '), members.map { it.name.substringBefore(' ') })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(
                    "💬 Invite on WhatsApp", { Invites.shareText(context, ShareTarget.WHATSAPP, text); onInvited() },
                    Modifier.weight(1f), brush = Brush.linearGradient(listOf(Brand.WhatsApp, Color(0xFF128C7E))),
                )
                GradientButton("More", { Invites.shareText(context, ShareTarget.MORE, text); onInvited() },
                    brush = Brush.linearGradient(listOf(Color(0x26FFFFFF), Color(0x14FFFFFF))))
            }
        }
    }
}

// =================================================================== Reunion

fun LazyListScope.reunionTab(vm: GroupViewModel, group: Group, members: List<Member>, reunion: Reunion?) {
    if (reunion == null) item(key = "plan") { PlanReunion(members.size) { place, dates -> vm.proposeReunion(place, dates) } }
    else item(key = "reunion") { ReunionCard(vm, group, members, reunion) }
}

private fun fmt(d: Long) = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(d))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanReunion(memberCount: Int, onPropose: (String, List<Long>) -> Unit) {
    var place by rememberSaveable { mutableStateOf("") }
    val dates = remember { mutableStateListOf<Long>() }
    var picking by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Brand.Coral, Brand.Orchid, Brand.Violet))).padding(22.dp),
        ) {
            Column {
                Text("🥳", fontSize = 44.sp)
                Text("Plan the reunion", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Text(
                    "Suggest up to three dates and a place. $memberCount batchmates can vote and RSVP — the date with most votes wins.",
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
        }
        OutlinedTextField(place, { place = it.take(100) }, Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Where? (e.g. our school ground, a café in town)") }, shape = RoundedCornerShape(16.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            dates.forEach { d ->
                Text("📅 ${fmt(d)}  ✕", Modifier.clip(RoundedCornerShape(50)).background(Color(0x22FFFFFF))
                    .clickable { dates.remove(d) }.padding(horizontal = 12.dp, vertical = 8.dp))
            }
            if (dates.size < 3) Text("＋ Add a date", Modifier.clip(RoundedCornerShape(50)).background(Brand.Violet.copy(alpha = 0.4f))
                .clickable { picking = true }.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
        }
        GradientButton("Propose to the batch", { onPropose(place, dates.toList()) }, Modifier.fillMaxWidth(), enabled = dates.isNotEmpty())
    }
    if (picking) {
        val state = rememberDatePickerState(selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= System.currentTimeMillis() - 86_400_000
        })
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton({ state.selectedDateMillis?.let { if (it !in dates) dates += it }; picking = false }) { Text("Add") }
            },
            dismissButton = { TextButton({ picking = false }) { Text("Cancel") } },
        ) { DatePicker(state) }
    }
}

@Composable
private fun ReunionCard(vm: GroupViewModel, group: Group, members: List<Member>, r: Reunion) {
    val context = LocalContext.current
    val me = vm.user.uid
    val isOrganiser = r.createdBy == me
    val going = members.filter { r.rsvp[it.uid] == Rsvp.YES }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Brand.Coral, Brand.Orchid, Brand.Violet))).padding(22.dp),
        ) {
            Column {
                Text("THE REUNION", style = Eyebrow.copy(color = Color.White))
                val locked = r.lockedDate
                if (locked != null) {
                    val days = TimeUnit.MILLISECONDS.toDays(locked - System.currentTimeMillis()).coerceAtLeast(0)
                    Text(if (days == 0L) "It's today! 🎉" else "$days days to go", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("📅 ${fmt(locked)}", color = Color.White, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Vote for the date", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
                if (r.place.isNotBlank()) Text("📍 ${r.place}", color = Color.White.copy(alpha = 0.9f))
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarStack(going.map { it.name to it.photoUrl }, going.size, 28.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("${going.size} going · ${r.rsvpCount(Rsvp.MAYBE)} maybe", color = Color.White)
                }
            }
        }
        if (r.lockedDate == null) {
            val total = r.votes.size.coerceAtLeast(1)
            r.options.forEachIndexed { i, d ->
                val n = r.votesFor(i)
                val mine = r.votes[me] == i
                GlassCard(Modifier.fillMaxWidth(), onClick = { vm.vote(i) }, corner = 18.dp, padding = PaddingValues(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text((if (mine) "✅ " else "📅 ") + fmt(d), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text("$n vote${if (n == 1) "" else "s"}", color = Brand.Sun, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator({ n.toFloat() / total }, Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                        color = Brand.Sun, trackColor = Color(0x22FFFFFF))
                    if (isOrganiser) TextButton({ vm.lock(d) }) { Text("Lock this date") }
                }
            }
            Text("Tap a date to vote. ${if (isOrganiser) "As organiser, lock the winner when you're ready." else ""}",
                color = Brand.TextDim, fontSize = 12.sp)
        } else if (isOrganiser) {
            TextButton({ vm.lock(null) }) { Text("Reopen voting") }
        }
        Text("Are you coming?", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Rsvp.YES to "🙋 Going", Rsvp.MAYBE to "🤔 Maybe", Rsvp.NO to "😢 Can't").forEach { (k, label) ->
                val on = r.rsvp[me] == k
                Text(
                    "$label ${r.rsvpCount(k)}",
                    Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                        .background(if (on) Brand.Primary else Brush.linearGradient(listOf(Color(0x1FFFFFFF), Color(0x12FFFFFF))))
                        .clickable { vm.rsvp(k) }.padding(vertical = 12.dp),
                    textAlign = TextAlign.Center, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
        val dateText = r.lockedDate?.let(::fmt) ?: r.options.joinToString(" / ", transform = ::fmt)
        GradientButton(
            "📣 Share reunion update on WhatsApp",
            { Invites.shareText(context, ShareTarget.WHATSAPP, Invites.reunionMessage(group, r, dateText)) },
            Modifier.fillMaxWidth(), brush = Brush.linearGradient(listOf(Brand.WhatsApp, Color(0xFF128C7E))),
        )
    }
}
