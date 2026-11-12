package com.batchmates.app.ui.finder

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batchmates.app.data.Country
import com.batchmates.app.data.Grade
import com.batchmates.app.data.Region
import com.batchmates.app.data.School
import com.batchmates.app.data.SchoolSource
import com.batchmates.app.ui.components.AuroraBackground
import com.batchmates.app.ui.components.EmptyState
import com.batchmates.app.ui.components.GlassCard
import com.batchmates.app.ui.components.GradientButton
import com.batchmates.app.ui.components.Pill
import com.batchmates.app.ui.components.SearchField
import com.batchmates.app.ui.theme.Brand
import com.batchmates.app.ui.theme.Eyebrow
import kotlinx.coroutines.launch

@Composable
fun FinderScreen(vm: FinderViewModel, onExit: () -> Unit, onJoined: (String) -> Unit) {
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.joined.collect(onJoined) }
    BackHandler { if (!vm.back()) onExit() }

    AuroraBackground(intensity = 0.55f) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            if (s.step != Step.MAP) TopBar(s) { if (!vm.back()) onExit() }
            AnimatedContent(
                targetState = s.step,
                transitionSpec = {
                    val forward = targetState.ordinal >= initialState.ordinal
                    (slideInHorizontally { if (forward) it / 3 else -it / 3 } + fadeIn(tween(250))) togetherWith
                        (slideOutHorizontally { if (forward) -it / 3 else it / 3 } + fadeOut(tween(200)))
                },
                label = "step",
                modifier = Modifier.weight(1f),
            ) { step ->
                when (step) {
                    Step.COUNTRY -> CountryStep(s, vm::pickCountry)
                    Step.STATE -> StateStep(s, vm::pickState)
                    Step.DISTRICT -> DistrictStep(s, vm::pickDistrict, vm::loadDistricts)
                    Step.SCHOOL -> SchoolStep(s, vm)
                    Step.MAP -> MapStep(s, vm, onBack = { if (!vm.back()) onExit() })
                    Step.CREATE -> CreateStep(s, vm)
                    Step.BATCH -> BatchStep(s, vm)
                    Step.SECTION -> SectionStep(s, vm)
                }
            }
        }
    }
}

@Composable
private fun TopBar(s: FinderState, onBack: () -> Unit) {
    Column(Modifier.padding(horizontal = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) {
                Text(stepEyebrow(s.step), style = Eyebrow)
                Text(stepTitle(s.step), style = MaterialTheme.typography.headlineSmall)
            }
        }
        val crumbs = listOfNotNull(
            s.country?.let { "${it.flag} ${it.name}" },
            s.state, s.district?.takeIf { it.isNotBlank() },
            s.school?.name?.takeIf { s.step.ordinal >= Step.BATCH.ordinal },
            s.year?.takeIf { s.step == Step.SECTION }?.let { "${s.grade.label} · $it" },
        )
        if (crumbs.isNotEmpty() && s.step != Step.COUNTRY) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) { crumbs.forEach { Pill(it, Color.White) } }
        }
    }
}

private fun stepEyebrow(step: Step) = when (step) {
    Step.COUNTRY -> "STEP 1 OF 5 · WORLD"
    Step.STATE -> "STEP 2 OF 5"
    Step.DISTRICT -> "STEP 3 OF 5"
    Step.SCHOOL, Step.CREATE, Step.MAP -> "STEP 4 OF 5"
    Step.BATCH, Step.SECTION -> "STEP 5 OF 5 · ALMOST THERE"
}

private fun stepTitle(step: Step) = when (step) {
    Step.COUNTRY -> "Which country?"
    Step.STATE -> "Which state?"
    Step.DISTRICT -> "Which district?"
    Step.SCHOOL -> "Find your school"
    Step.MAP -> "Pick on map"
    Step.CREATE -> "Add your school"
    Step.BATCH -> "When did you pass out?"
    Step.SECTION -> "Which section?"
}

// ------------------------------------------------------------------ steps

@Composable
private fun CountryStep(s: FinderState, onPick: (Country) -> Unit) {
    var q by rememberSaveable { mutableStateOf("") }
    val suggested = s.countries.firstOrNull { it.code == s.suggestedCountry }
    val list = remember(q, s.countries) {
        s.countries.filter { q.isBlank() || it.name.contains(q, true) || it.code.equals(q, true) }
    }
    ListWithSearch(q, { q = it }, "Search 250 countries") {
        if (q.isBlank() && suggested != null) {
            item {
                GlassCard(Modifier.fillMaxWidth().padding(bottom = 8.dp), onClick = { onPick(suggested) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(suggested.flag, fontSize = 40.sp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("YOU'RE PROBABLY IN", style = Eyebrow)
                            Text(suggested.name, style = MaterialTheme.typography.titleLarge)
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null)
                    }
                }
            }
        }
        items(list, key = { it.code }) { c -> Row1(leading = c.flag, title = c.name, subtitle = "${c.states.size} states") { onPick(c) } }
    }
}

@Composable
private fun StateStep(s: FinderState, onPick: (String) -> Unit) {
    var q by rememberSaveable { mutableStateOf("") }
    val states = s.country?.states.orEmpty()
    val list = remember(q, states) { states.filter { q.isBlank() || it.contains(q, true) } }
    ListWithSearch(q, { q = it }, "Search states / provinces") {
        items(list, key = { it }) { st -> Row1(leading = "📍", title = st) { onPick(st) } }
        item { ManualEntry(if (states.isEmpty()) "Type your state / province" else "Not listed? Type it", q, onPick) }
    }
}

@Composable
private fun DistrictStep(s: FinderState, onPick: (String) -> Unit, retry: () -> Unit) {
    var q by rememberSaveable { mutableStateOf("") }
    when (val d = s.districts) {
        Load.Idle, Load.Loading -> Shimmer("Fetching districts of ${s.state}…")
        is Load.Failed -> Column(Modifier.padding(20.dp)) {
            EmptyState("📡", "Couldn't reach the map", d.message)
            OutlinedButton(retry, Modifier.align(Alignment.CenterHorizontally)) { Icon(Icons.Rounded.Refresh, null); Text("  Retry") }
            ManualEntry("Or type your district", q, onPick)
            SkipDistrict(onPick)
        }
        is Load.Ready -> {
            val list = remember(q, d.value) { d.value.filter { q.isBlank() || it.contains(q, true) } }
            ListWithSearch(q, { q = it }, "Search ${d.value.size} districts") {
                items(list, key = { it }) { name -> Row1(leading = "🏙️", title = name) { onPick(name) } }
                item { ManualEntry("Not listed? Type your district / city", q, onPick) }
                item { SkipDistrict(onPick) }
            }
        }
    }
}

@Composable
private fun SkipDistrict(onPick: (String) -> Unit) {
    TextButton({ onPick("") }, Modifier.fillMaxWidth()) { Text("Skip — I'll search the whole state by school name") }
}

@Composable
private fun SchoolStep(s: FinderState, vm: FinderViewModel) {
    var q by rememberSaveable { mutableStateOf("") }
    val wholeState = s.district.isNullOrBlank()
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            SearchField(
                q, { q = it },
                if (wholeState) "Type your school name and hit search" else "Filter schools by name",
                onSearch = { vm.loadSchools(q) },
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(vm::openMap, Modifier.weight(1f)) { Icon(Icons.Rounded.Map, null, Modifier.size(18.dp)); Text("  See on map") }
                OutlinedButton({ vm.openCreate() }, Modifier.weight(1f)) { Icon(Icons.Rounded.AddLocationAlt, null, Modifier.size(18.dp)); Text("  Add school") }
            }
        }
        when (val l = s.schools) {
            Load.Loading -> Shimmer("Looking for schools near ${s.district?.ifBlank { s.state } ?: s.state}…")
            is Load.Failed -> Column(Modifier.padding(20.dp)) {
                EmptyState("📡", "Map service is busy", l.message)
                OutlinedButton({ vm.loadSchools(q) }, Modifier.align(Alignment.CenterHorizontally)) { Text("Retry") }
            }
            Load.Idle -> EmptyState("🔎", "Search your school", "Type part of the name — e.g. \"St. Joseph\" or \"Government Higher Secondary\".")
            is Load.Ready -> {
                val list = remember(q, l.value) { l.value.filter { q.isBlank() || it.name.contains(q, true) } }
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.imePadding(),
                ) {
                    item { Text("${list.size} schools found", color = Brand.TextDim, fontSize = 13.sp) }
                    items(list, key = { it.id }) { school -> SchoolRow(school) { vm.pickSchool(school) } }
                    item { CantFind(vm) }
                }
            }
        }
    }
}

@Composable
private fun SchoolRow(school: School, onClick: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), onClick = onClick, corner = 18.dp, padding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(Brand.pair(school.name))),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.School, null, tint = Color.White) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(school.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (school.region.label.isNotBlank()) Text(school.region.label, color = Brand.TextDim, fontSize = 12.sp, maxLines = 1)
            }
            if (school.source == SchoolSource.CUSTOM) Pill("ADDED BY ALUMNI", Brand.Mint)
        }
    }
}

@Composable
private fun CantFind(vm: FinderViewModel) {
    GlassCard(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Can't find your school?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("Small and old schools are often missing from maps. Create its group in 10 seconds — your classmates will find it here.", color = Brand.TextDim)
        Spacer(Modifier.height(12.dp))
        GradientButton("Create my school's group", { vm.openCreate() }, Modifier.fillMaxWidth(), brush = Brand.Warm)
    }
}

@Composable
private fun CreateStep(s: FinderState, vm: FinderViewModel) {
    val start = s.createRegion ?: s.region
    var name by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable(start.country) { mutableStateOf(start.country) }
    var state by rememberSaveable(start.state) { mutableStateOf(start.state) }
    var district by rememberSaveable(start.district) { mutableStateOf(start.district) }
    Column(Modifier.fillMaxSize().imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Give it the name everyone remembers. You can add the area so batchmates find it easily.", color = Brand.TextDim)
        Field("School name", name) { name = it }
        Field("Country", country) { country = it }
        Field("State / province", state) { state = it }
        Field("District / city", district) { district = it }
        if (s.pinned != null) Pill("📍 Pinned on map", Brand.Mint)
        s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.weight(1f))
        GradientButton(
            "Create & continue", {
                val code = s.countries.firstOrNull { it.name.equals(country.trim(), true) }?.code ?: start.countryCode
                vm.createSchool(name, Region(code, country.trim(), state.trim(), district.trim()))
            },
            Modifier.fillMaxWidth().navigationBarsPadding(), loading = s.joining,
        )
    }
}

@Composable
private fun BatchStep(s: FinderState, vm: FinderViewModel) {
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val decades = remember(vm.years) { vm.years.map { it / 10 * 10 }.distinct() }
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        state = grid,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                s.school?.let { school ->
                    GlassCard(Modifier.fillMaxWidth(), corner = 20.dp, padding = PaddingValues(16.dp)) {
                        Text(school.name, style = MaterialTheme.typography.titleLarge)
                        if (school.region.label.isNotBlank()) Text(school.region.label, color = Brand.TextDim)
                        val total = s.stats.sumOf { it.members }
                        if (total > 0) { Spacer(Modifier.height(8.dp)); Pill("🔥 $total alumni already joined", Brand.Sun) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Grade.entries.forEach { g ->
                        val selected = s.grade == g
                        Box(
                            Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(18.dp))
                                .background(if (selected) Brand.Primary else Brush.linearGradient(listOf(Color(0x1AFFFFFF), Color(0x10FFFFFF))))
                                .border(1.dp, if (selected) Color.Transparent else Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                                .clickable { vm.pickGrade(g) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (g == Grade.TENTH) "10th" else "12th", fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text(if (g == Grade.TENTH) "SSLC / Secondary" else "HSC / Senior Secondary", fontSize = 11.sp, color = Brand.TextDim)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(decades) { d ->
                        Pill("${d}s", Brand.Sky, Modifier.clickable {
                            val idx = vm.years.indexOfFirst { it / 10 * 10 == d }
                            scope.launch { grid.animateScrollToItem(idx + 1) }
                        })
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
        items(vm.years, key = { it }) { y ->
            val count = vm.membersFor(s.grade, y)
            YearTile(y, count, y == s.year) { vm.pickYear(y) }
        }
    }
}

@Composable
private fun YearTile(year: Int, count: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.aspectRatio(1f).clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    selected -> Brand.Primary
                    count > 0 -> Brush.linearGradient(listOf(Brand.Violet.copy(alpha = 0.45f), Brand.Coral.copy(alpha = 0.35f)))
                    else -> Brush.linearGradient(listOf(Color(0x18FFFFFF), Color(0x0CFFFFFF)))
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$year", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (count > 0) Text("👥 $count", fontSize = 11.sp, color = Brand.Sun)
        }
    }
}

@Composable
private fun SectionStep(s: FinderState, vm: FinderViewModel) {
    var picked by rememberSaveable { mutableStateOf<String?>(null) }
    var custom by rememberSaveable { mutableStateOf("") }
    val sections = listOf("A", "B", "C", "D", "E", "F", "G", "H")
    Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp)) {
        Text("Pick your section. Don't remember? Choose the one most friends were in — you can join more later.", color = Brand.TextDim)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(sections) { sec ->
                val count = vm.membersFor(sec)
                val selected = picked == sec
                val pulse = rememberInfiniteTransition(label = "p").animateFloat(
                    1f, if (count > 0 && !selected) 1.05f else 1f,
                    infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "s",
                )
                Box(
                    Modifier.aspectRatio(1f).graphicsLayer { scaleX = pulse.value; scaleY = pulse.value }
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) Brand.Primary
                            else Brush.linearGradient(listOf(Color(0x22FFFFFF), Color(0x0FFFFFFF))),
                        )
                        .border(1.dp, if (count > 0) Brand.Sun.copy(alpha = 0.6f) else Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                        .clickable { picked = sec; custom = "" },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(sec, fontSize = 30.sp, fontWeight = FontWeight.Black)
                        Text(if (count > 0) "$count here" else "be first", fontSize = 11.sp, color = if (count > 0) Brand.Sun else Brand.TextDim)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            custom, { custom = it.take(20); if (it.isNotBlank()) picked = null },
            Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp),
            label = { Text("Other section / stream (e.g. Bio-Maths, Commerce)") },
        )
        s.error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.weight(1f))
        val choice = picked ?: custom.trim().takeIf { it.isNotEmpty() }
        GradientButton(
            if (choice != null) "Join Section ${choice.uppercase()} 🎉" else "Choose a section",
            { choice?.let(vm::join) },
            Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 12.dp),
            enabled = choice != null, loading = s.joining,
        )
    }
}

// ------------------------------------------------------------------ shared bits

@Composable
private fun ListWithSearch(
    q: String,
    onQ: (String) -> Unit,
    placeholder: String,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        SearchField(q, onQ, placeholder, Modifier.padding(horizontal = 20.dp))
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.imePadding(),
            content = content,
        )
    }
}

@Composable
private fun Row1(leading: String, title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0x0FFFFFFF))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(leading, fontSize = 22.sp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) Text(subtitle, color = Brand.TextDim, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Brand.TextDim)
    }
}

@Composable
private fun ManualEntry(label: String, prefill: String, onDone: (String) -> Unit) {
    var text by remember(prefill) { mutableStateOf(prefill) }
    GlassCard(Modifier.fillMaxWidth().padding(top = 10.dp), corner = 18.dp, padding = PaddingValues(14.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(text, { text = it }, Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(14.dp))
            Spacer(Modifier.width(8.dp))
            GradientButton("Go", { if (text.isNotBlank()) onDone(text) }, enabled = text.isNotBlank())
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, shape = RoundedCornerShape(16.dp))
}

@Composable
private fun Shimmer(message: String) {
    val t = rememberInfiniteTransition(label = "sh")
    val a by t.animateFloat(0.05f, 0.16f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a")
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(message, color = Brand.TextDim, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        repeat(7) { i ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = a)))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.height(18.dp).fillMaxWidth(0.5f + (i % 3) * 0.15f).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = a)))
            }
        }
    }
}
