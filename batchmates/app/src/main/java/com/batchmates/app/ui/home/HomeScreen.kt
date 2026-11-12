package com.batchmates.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batchmates.app.data.AppUser
import com.batchmates.app.data.Group
import com.batchmates.app.ui.components.AuroraBackground
import com.batchmates.app.ui.components.Avatar
import com.batchmates.app.ui.components.GlassCard
import com.batchmates.app.ui.components.Pill
import com.batchmates.app.ui.theme.Brand
import com.batchmates.app.ui.theme.Eyebrow
import java.util.Calendar

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    user: AppUser,
    isDemo: Boolean,
    pendingCode: String?,
    onPendingCodeConsumed: () -> Unit,
    onBrowse: () -> Unit,
    onMap: () -> Unit,
    onOpenGroup: (String, Boolean) -> Unit,
    onSignOut: () -> Unit,
) {
    val groups by vm.groups.collectAsState()
    val joining by vm.joining.collectAsState()
    val error by vm.error.collectAsState()
    var code by rememberSaveable { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.openGroup.collect { onOpenGroup(it, true) } }
    LaunchedEffect(pendingCode) {
        if (pendingCode != null) { code = pendingCode; vm.joinByCode(pendingCode); onPendingCodeConsumed() }
    }

    AuroraBackground(intensity = 0.7f) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(greeting().uppercase(), style = Eyebrow)
                        Text(user.name.substringBefore(' '), style = MaterialTheme.typography.headlineLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (isDemo) { Pill("DEMO", Brand.Mint); Spacer(Modifier.width(8.dp)) }
                    Box {
                        IconButton({ menu = true }) { Avatar(user.name, user.photoUrl, 40.dp) }
                        DropdownMenu(menu, { menu = false }) {
                            DropdownMenuItem(
                                text = { Text("Sign out") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, null) },
                                onClick = { menu = false; onSignOut() },
                            )
                        }
                    }
                }
            }
            item { FindHero(onBrowse, onMap) }
            item {
                GlassCard(padding = PaddingValues(16.dp)) {
                    Text("Got an invite code?", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            code, { code = it.uppercase().take(40) }, Modifier.weight(1f),
                            placeholder = { Text("e.g. K7QM2X") }, singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { vm.joinByCode(code) }),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0x33FFFFFF)),
                        )
                        Spacer(Modifier.width(10.dp))
                        Box(
                            Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(Brand.Warm),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (joining) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                            else IconButton({ vm.joinByCode(code) }) { Icon(Icons.AutoMirrored.Rounded.ArrowForward, "Join", tint = Color.White) }
                        }
                    }
                    error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("My batches", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    groups?.let { Text("${it.size}", color = Brand.TextDim) }
                }
            }
            val list = groups
            when {
                list == null -> item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                list.isEmpty() -> item {
                    GlassCard {
                        Text("🌱  No batches yet", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Find your school above — it takes under a minute. Be the first from your class and everyone you invite lands right here.",
                            color = Brand.TextDim,
                        )
                    }
                }
                else -> itemsIndexed(list, key = { _, g -> g.id }) { i, g -> GroupCard(g, i) { onOpenGroup(g.id, false) } }
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}

@Composable
private fun FindHero(onBrowse: () -> Unit, onMap: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Brand.Violet, Brand.Orchid, Brand.Coral)))
            .padding(22.dp),
    ) {
        Text("🏫", fontSize = 64.sp, modifier = Modifier.align(Alignment.TopEnd).graphicsLayer { alpha = 0.9f; rotationZ = 8f })
        Column {
            Text("FIND YOUR OLD GANG", style = Eyebrow.copy(color = Color.White.copy(alpha = 0.85f)))
            Spacer(Modifier.height(6.dp))
            Text("Where did you\nstudy?", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroChoice("Browse places", Icons.Rounded.Explore, Modifier.weight(1f), onBrowse)
                HeroChoice("Pick on map", Icons.Rounded.Map, Modifier.weight(1f), onMap)
            }
        }
    }
}

@Composable
private fun HeroChoice(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    GlassCard(modifier, onClick = onClick, corner = 18.dp, padding = PaddingValues(14.dp)) {
        Icon(icon, null, tint = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Composable
private fun GroupCard(g: Group, index: Int, onClick: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val alpha by animateFloatAsState(if (shown) 1f else 0f, tween(400, delayMillis = 60 * index), label = "in")
    GlassCard(Modifier.graphicsLayer { this.alpha = alpha; translationY = (1 - alpha) * 40f }, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(Brand.pair(g.schoolName))),
                contentAlignment = Alignment.Center,
            ) { Text("'${g.year % 100}".padStart(3, '0'), fontWeight = androidx.compose.ui.text.font.FontWeight.Black, color = Color.White) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(g.schoolName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(g.title, color = Brand.TextDim, fontSize = 13.sp, maxLines = 1)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Groups, null, tint = Brand.Sun, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("${g.memberCount}", color = Brand.Sun, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

