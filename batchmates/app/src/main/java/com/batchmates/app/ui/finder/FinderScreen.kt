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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import com.batchmates.app.data.Classifier
import com.batchmates.app.data.CollegeType
import com.batchmates.app.data.InstitutionKind
import com.batchmates.app.data.Track
import com.batchmates.app.data.Level
import com.batchmates.app.data.WHOLE_BATCH
import java.util.Calendar
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.verticalScroll
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
import com.batchmates.app.util.SchoolNames
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
                    Step.QUICK -> QuickStep(s, vm)
                    Step.COUNTRY -> CountryStep(s, vm::pickCountry)
                    Step.STATE -> StateStep(s, vm::pickState)
                    Step.DISTRICT -> DistrictStep(s, vm::pickDistrict, vm::loadDistricts)
                    Step.TOWN -> TownStep(s, vm)
                    Step.SCHOOL -> SchoolStep(s, vm)
                    Step.MAP -> MapStep(s, vm, onBack = { if (!vm.back()) onExit() })
                    Step.CREATE -> CreateStep(s, vm)
                    Step.COURSE -> CourseStep(s, vm)
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
            s.state, s.district?.takeIf { it.isNotBlank() }, s.town?.name?.takeIf { s.step.ordinal > Step.TOWN.ordinal },
            s.school?.name?.takeIf { s.step.ordinal >= Step.BATCH.ordinal },
            s.track?.takeIf { s.step == Step.SECTION || (s.step == Step.BATCH && it.isCollege) }?.let { t -> listOfNotNull(t.short, s.year?.toString()).joinToString(" · ") },
        )
        if (crumbs.isNotEmpty() && s.step != Step.COUNTRY && s.step != Step.QUICK) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) { crumbs.forEach { Pill(it, Color.White) } }
        }
    }
}

private fun stepEyebrow(step: Step) = when (step) {
    Step.QUICK -> "FIND YOUR BATCH"
    Step.COUNTRY -> "STEP 1 OF 6 · WORLD"
    Step.STATE -> "STEP 2 OF 6"
    Step.DISTRICT -> "STEP 3 OF 6"
    Step.TOWN -> "STEP 4 OF 6"
    Step.SCHOOL, Step.CREATE, Step.MAP -> "STEP 5 OF 6"
    Step.COURSE, Step.BATCH, Step.SECTION -> "STEP 6 OF 6 · ALMOST THERE"
}

private fun stepTitle(step: Step) = when (step) {
    Step.QUICK -> "Where did you study?"
    Step.COUNTRY -> "Which country?"
    Step.STATE -> "Which state?"
    Step.DISTRICT -> "Which district?"
    Step.TOWN -> "Which town or village?"
    Step.SCHOOL -> "Find your school or college"
    Step.COURSE -> "What did you study?"
    Step.MAP -> "Pick on map"
    Step.CREATE -> "Add it in 10 seconds"
    Step.BATCH -> "When did you pass out?"
    Step.SECTION -> "Which class?"
}

// ------------------------------------------------------------------ steps

/** The fast lane: type the town, get schools. Browsing by country → state → district stays one tap away. */
@Composable
private fun QuickStep(s: FinderState, vm: FinderViewModel) {
    var q by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        SearchField(q, { q = it; vm.searchAnywhere(it) }, "Town or city — e.g. Kumbakonam, Manchester", Modifier.padding(horizontal = 20.dp))
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.imePadding(),
        ) {
            when (val r = s.anywhereResults) {
                Load.Loading -> items(3) { TownSkeleton() }
                is Load.Failed -> item { Text("Search is busy right now — try again, or browse below.", color = Brand.TextDim) }
                is Load.Ready -> {
                    if (r.value.isEmpty()) item { Text("No place called \"${q.trim()}\". Try another spelling, or browse below.", color = Brand.TextDim) }
                    items(r.value, key = { "a" + it.name + it.lat }) { p ->
                        val flag = s.countries.firstOrNull { it.code == p.countryCode }?.flag ?: "📍"
                        Row1(leading = flag, title = p.name, subtitle = p.label) { vm.pickAnywhere(p) }
                    }
                }
                Load.Idle -> item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text("⚡ Fastest way", style = MaterialTheme.typography.titleMedium)
                        Text("Type the town or city where your school or college is. We'll list everything nearby, closest first.",
                            color = Brand.TextDim)
                    }
                }
            }
            item {
                Spacer(Modifier.height(10.dp))
                Text("Or", color = Brand.TextDim, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            item {
                Row1(leading = "🌍", title = "Browse by place", subtitle = "Country → State → District → Town") { vm.browse() }
            }
            item { Row1(leading = "🗺️", title = "Pick on the map", subtitle = "Find it visually, or pin a missing school") { vm.openMap() } }
        }
    }
}

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
private fun TownStep(s: FinderState, vm: FinderViewModel) {
    var q by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        SearchField(q, { q = it; vm.searchTowns(it) }, "Type your school's town, e.g. Kumbakonam", Modifier.padding(horizontal = 20.dp))
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.imePadding(),
        ) {
            when (val r = s.townResults) {
                Load.Loading -> item { Text("Searching places in ${s.district}…", color = Brand.TextDim) }
                is Load.Failed -> item { Text("Couldn't search places right now. Pick a town below or skip.", color = Brand.TextDim) }
                is Load.Ready -> {
                    if (r.value.isEmpty()) item { Text("No place called \"${q.trim()}\" near ${s.district}. Try another spelling.", color = Brand.TextDim) }
                    items(r.value, key = { it.name + it.lat }) { p ->
                        Row1(
                            leading = if (p.kind in setOf("city", "town", "municipality")) "🏙️" else "🏡",
                            title = p.name,
                            subtitle = listOf(p.kind.replaceFirstChar { it.uppercase() }, p.area).filter { it.isNotBlank() }.joinToString(" · "),
                        ) { vm.pickTown(p) }
                    }
                }
                Load.Idle -> Unit
            }
            if (q.isBlank()) {
                item {
                    Text(
                        when {
                            !s.townsLoaded -> "Finding towns in ${s.district}…"
                            s.townChips.isEmpty() -> "Type your town above"
                            else -> "Towns in and around ${s.district}"
                        },
                        style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )
                }
                if (!s.townsLoaded) items(4) { TownSkeleton() }
                items(s.townChips, key = { "chip" + it.name + it.lat }) { p ->
                    Row1(
                        leading = if (p.kind == "city") "🏙️" else "📍", title = p.name,
                        subtitle = listOf(p.kind.replaceFirstChar { it.uppercase() }, p.area.takeIf { a -> !a.equals(p.name, true) }.orEmpty())
                            .filter { it.isNotBlank() }.joinToString(" · "),
                    ) { vm.pickTown(p) }
                }
            }
            item {
                TextButton({ vm.pickTown(null) }, Modifier.fillMaxWidth()) { Text("Skip — show all schools in ${s.district}") }
            }
        }
    }
}

@Composable
private fun TownSkeleton() {
    val t = rememberInfiniteTransition(label = "ts")
    val a by t.animateFloat(0.05f, 0.14f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a")
    Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = a)))
}

@Composable
private fun SchoolStep(s: FinderState, vm: FinderViewModel) {
    var q by rememberSaveable { mutableStateOf("") }
    val wholeState = s.district.isNullOrBlank()
    val place = s.town?.name ?: s.district?.ifBlank { null } ?: s.state.orEmpty()
    val noun = if (s.tab == InstitutionKind.COLLEGE) "college" else "school"
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            SearchField(
                q, { q = it },
                if (wholeState) "Type the name and hit search" else "Type part of the name",
                onSearch = { vm.loadSchools(q) },
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(vm::openMap, Modifier.weight(1f)) { Icon(Icons.Rounded.Map, null, Modifier.size(18.dp)); Text("  See on map") }
                OutlinedButton({ vm.openCreate(name = q) }, Modifier.weight(1f)) { Icon(Icons.Rounded.AddLocationAlt, null, Modifier.size(18.dp)); Text("  Add $noun") }
            }
        }
        when (val l = s.schools) {
            Load.Loading -> Shimmer("Looking for schools and colleges in $place…")
            is Load.Failed -> LazyColumn(contentPadding = PaddingValues(20.dp), modifier = Modifier.imePadding()) {
                item { EmptyState("📡", "Directory unreachable", l.message) }
                item {
                    OutlinedButton({ vm.loadSchools(q) }, Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Refresh, null); Text("  Retry") }
                }
                item { CantFind(vm, q, s.tab) }
            }
            Load.Idle -> LazyColumn(contentPadding = PaddingValues(20.dp), modifier = Modifier.imePadding()) {
                item { EmptyState("🔎", "Search by name", "Type part of the name — e.g. \"St. Joseph\" or \"Government Higher Secondary\" — and press search.") }
                item { CantFind(vm, q, s.tab) }
            }
            is Load.Ready -> {
                val matching = remember(q, l.value) { l.value.filter { q.isBlank() || SchoolNames.matches(q, it) } }
                val schools = matching.filter { it.kind == InstitutionKind.SCHOOL }
                val colleges = matching.filter { it.kind == InstitutionKind.COLLEGE }
                val list = if (s.tab == InstitutionKind.COLLEGE) colleges else schools
                val other = if (s.tab == InstitutionKind.COLLEGE) schools.size else colleges.size
                val canSearchServer = q.trim().length >= 2 && !q.trim().equals(s.lastQuery, ignoreCase = true)
                KindTabs(s.tab, schools.size, colleges.size, vm::setTab)
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.imePadding(),
                ) {
                    if (list.isEmpty()) {
                        item {
                            EmptyState(
                                if (s.tab == InstitutionKind.COLLEGE) "🏛️" else "🏫",
                                if (q.isBlank()) "No ${noun}s found here yet" else "No $noun matches \"${q.trim()}\"",
                                (if (other > 0) "There ${if (other == 1) "is 1 match" else "are $other matches"} in the other tab. " else "") +
                                    "Older and smaller ${noun}s often aren't on public maps — add yours below and classmates will find it.",
                            )
                        }
                    } else {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (s.town != null) "${list.size} ${noun}s in and around $place · nearest first" else "${list.size} ${noun}s in $place",
                                    color = Brand.TextDim, fontSize = 13.sp, modifier = Modifier.weight(1f),
                                )
                                if (s.refining) {
                                    CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Brand.Sun)
                                    Text("  finding more", color = Brand.Sun, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    if (canSearchServer) item {
                        OutlinedButton({ vm.loadSchools(q) }, Modifier.fillMaxWidth()) {
                            Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp)); Text("  Search \"${q.trim()}\" in $place")
                        }
                    }
                    if (list.isEmpty()) item { CantFind(vm, q, s.tab) }
                    items(list, key = { it.id }) { school -> SchoolRow(school) { vm.pickSchool(school) } }
                    if (list.isNotEmpty()) item { CantFind(vm, q, s.tab) }
                }
            }
        }
    }
}

@Composable
private fun KindTabs(selected: InstitutionKind, schools: Int, colleges: Int, onSelect: (InstitutionKind) -> Unit) {
    Row(
        Modifier.padding(horizontal = 20.dp, vertical = 10.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0x14FFFFFF)).padding(4.dp),
    ) {
        listOf(InstitutionKind.SCHOOL to "🏫 Schools · $schools", InstitutionKind.COLLEGE to "🏛️ Colleges · $colleges").forEach { (k, label) ->
            val on = k == selected
            Box(
                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (on) Brand.Primary else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                    .clickable { onSelect(k) },
                contentAlignment = Alignment.Center,
            ) { Text(label, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium, color = if (on) Color.White else Brand.TextDim) }
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
            ) {
                if (school.kind == InstitutionKind.COLLEGE) Text(school.collegeType?.emoji ?: "🏛️", fontSize = 20.sp)
                else Icon(Icons.Rounded.School, null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(school.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                val kindLabel = if (school.kind == InstitutionKind.COLLEGE) school.collegeType?.label else school.schoolProfile.label
                val where = listOfNotNull(
                    kindLabel,
                    school.locality.ifBlank { school.region.town }.ifBlank { null },
                    school.distanceKm?.let { if (it < 1) "${(it * 1000).toInt()} m" else "%.1f km".format(it) },
                ).joinToString(" · ")
                if (where.isNotBlank()) Text(where, color = Brand.TextDim, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (school.aliases.isNotEmpty()) {
                    Text("Also known as " + school.aliases.take(2).joinToString(", "), color = Brand.Sun, fontSize = 12.sp, maxLines = 1)
                }
            }
            if (school.source == SchoolSource.CUSTOM) Pill("ADDED BY ALUMNI", Brand.Mint)
        }
    }
}

@Composable
private fun CantFind(vm: FinderViewModel, typed: String, kind: InstitutionKind) {
    val name = typed.trim()
    val noun = if (kind == InstitutionKind.COLLEGE) "college" else "school"
    GlassCard(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Can't find your $noun?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("Create its group in 10 seconds — your batchmates will find it here.", color = Brand.TextDim)
        Spacer(Modifier.height(12.dp))
        GradientButton(
            if (name.length >= 2) "Add \"${name.take(28)}\"" else "Add my $noun",
            { vm.openCreate(name = name) }, Modifier.fillMaxWidth(), brush = Brand.Warm,
        )
    }
}

@Composable
private fun CreateStep(s: FinderState, vm: FinderViewModel) {
    val start = s.createRegion ?: s.region
    var name by rememberSaveable(s.createName) { mutableStateOf(s.createName) }
    var country by rememberSaveable(start.country) { mutableStateOf(start.country) }
    var state by rememberSaveable(start.state) { mutableStateOf(start.state) }
    var district by rememberSaveable(start.district) { mutableStateOf(start.district) }
    var town by rememberSaveable(start.town) { mutableStateOf(start.town) }
    // Kind and college type follow the name until the user picks one themselves.
    var kindPicked by rememberSaveable { mutableStateOf<InstitutionKind?>(null) }
    var typePicked by rememberSaveable { mutableStateOf<CollegeType?>(null) }
    val cc = s.countries.firstOrNull { it.name.equals(country.trim(), true) }?.code ?: start.countryCode
    val kind = kindPicked ?: if (name.isBlank()) s.tab else Classifier.kind(name, null, cc)
    val type = typePicked ?: Classifier.collegeType(name, null, cc)
    fun region() = Region(
        s.countries.firstOrNull { it.name.equals(country.trim(), true) }?.code ?: start.countryCode,
        country.trim(), state.trim(), district.trim(), town.trim(),
    )
    LazyColumn(
        Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Use the full name everyone remembers. We'll check it isn't already here.", color = Brand.TextDim) }
        item { Field("Full name", name) { name = it; vm.onCreateNameChanged(it) } }
        if (s.duplicates.isNotEmpty()) {
            item {
                GlassCard(Modifier.fillMaxWidth(), corner = 18.dp, padding = PaddingValues(14.dp)) {
                    Text("✋ Is it one of these?", style = MaterialTheme.typography.titleMedium, color = Brand.Sun)
                    Text("These look like the same place. Tap yours — we'll remember \"${name.trim()}\" as another name for it.", color = Brand.TextDim, fontSize = 13.sp)
                }
            }
            items(s.duplicates, key = { "dup" + it.id }) { d -> SchoolRow(d) { vm.useExisting(d, name) } }
        }
        item {
            Text("It's a…", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            KindTabs(kind, 0, 0) { kindPicked = it }
        }
        if (kind == InstitutionKind.SCHOOL) {
            item {
                Text(
                    Classifier.schoolProfile(name, cc).let { p -> "${p.label} (${p.system.name}): batches for " + p.tracks.joinToString(" & ") { it.short } },
                    color = Brand.TextDim, fontSize = 13.sp,
                )
            }
        } else {
            item { Text("What kind of college? This decides which courses appear.", style = MaterialTheme.typography.titleSmall) }
            item { CollegeTypeChips(type) { typePicked = it } }
        }
        item { Field("Town / village", town) { town = it } }
        item { Field("District", district) { district = it } }
        item { Field("State / province", state) { state = it } }
        item { Field("Country", country) { country = it } }
        if (s.pinned != null) item { Pill("📍 Pinned on map", Brand.Mint) }
        s.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        item {
            GradientButton(
                if (s.duplicates.isEmpty()) "Create & continue" else "Mine isn't listed — create it",
                { vm.createSchool(name, region(), kind, type) },
                Modifier.fillMaxWidth().navigationBarsPadding(), loading = s.joining,
                brush = if (s.duplicates.isEmpty()) Brand.Primary else Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x22FFFFFF))),
            )
        }
    }
    if (s.confirmCreate) {
        AlertDialog(
            onDismissRequest = vm::dismissConfirm,
            title = { Text("Create a separate one?") },
            text = {
                Text(
                    "\"${s.duplicates.first().name}\" looks like the same place. Two groups for one institution split your batchmates. " +
                        "Create a new one only if it's really different.",
                )
            },
            confirmButton = { TextButton({ vm.createSchool(name, region(), kind, type, force = true) }) { Text("It's different — create") } },
            dismissButton = { TextButton({ vm.useExisting(s.duplicates.first(), name) }) { Text("Use existing") } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CollegeTypeChips(selected: CollegeType?, onPick: (CollegeType) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CollegeType.entries.forEach { t ->
            val on = t == selected
            Text(
                "${t.emoji} ${t.label}",
                Modifier.clip(RoundedCornerShape(50))
                    .background(if (on) Brand.Primary else Brush.linearGradient(listOf(Color(0x1AFFFFFF), Color(0x10FFFFFF))))
                    .clickable { onPick(t) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 13.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

/** College: level → field → discipline, showing only what this kind of institution teaches. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseStep(s: FinderState, vm: FinderViewModel) {
    val school = s.school ?: return
    var fixing by remember { mutableStateOf(false) }
    val field = s.field
    LazyColumn(
        Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth(), corner = 20.dp, padding = PaddingValues(16.dp)) {
                Text(school.name, style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        school.collegeType?.let { "${it.emoji} ${it.label}" } ?: "🏛️ College / University",
                        color = Brand.TextDim, modifier = Modifier.weight(1f),
                    )
                    TextButton({ fixing = true }) { Text("Wrong type?") }
                }
            }
        }
        if (field == null) {
            val existing = vm.existingTracks()
            if (existing.isNotEmpty()) {
                item { Text("🔥 Batches already here", style = MaterialTheme.typography.titleMedium) }
                items(existing, key = { "ex" + it.first.code }) { (t, n) ->
                    Row1(leading = "👥", title = t.label, subtitle = "$n alumni joined") { vm.pickCourse(t) }
                }
                item { Spacer(Modifier.height(4.dp)); Text("Or pick your course", style = MaterialTheme.typography.titleMedium) }
            }
            item {
                Text("Level", style = Eyebrow)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    vm.levelsForCollege().forEach { l ->
                        val on = l == s.level
                        Text(
                            "${l.emoji} ${l.short}",
                            Modifier.clip(RoundedCornerShape(50))
                                .background(if (on) Brand.Primary else Brush.linearGradient(listOf(Color(0x1FFFFFFF), Color(0x12FFFFFF))))
                                .clickable { vm.pickLevel(l) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
            item { Text("Field of study", style = Eyebrow, modifier = Modifier.padding(top = 6.dp)) }
            items(vm.fieldsForCollege(), key = { "f" + it.name }) { f ->
                val disciplines = vm.disciplinesFor(f)
                Row1(
                    leading = f.emoji, title = f.label,
                    subtitle = vm.degreeHint(f) ?: disciplines.take(3).joinToString(", ") + if (disciplines.size > 3) "…" else "",
                ) { vm.pickField(f) }
            }
            if (!s.showAllProgrammes && school.collegeType != null && school.collegeType != CollegeType.UNIVERSITY) item {
                TextButton(vm::showAllProgrammes, Modifier.fillMaxWidth()) { Text("My course isn't here — show every field") }
            }
        } else {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${s.level?.short ?: ""} · ${field.label}".uppercase(), style = Eyebrow)
                        Text("Which subject / branch?", style = MaterialTheme.typography.titleLarge)
                        vm.degreeHint(field)?.let { Text(it, color = Brand.TextDim, fontSize = 13.sp) }
                    }
                    TextButton(vm::clearField) { Text("Change") }
                }
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    vm.disciplinesFor(field).forEach { d ->
                        Text(
                            d,
                            Modifier.clip(RoundedCornerShape(50)).background(Color(0x1FFFFFFF))
                                .clickable { vm.pickDiscipline(d) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            item { ManualEntry("Not listed? Type it (e.g. CSE, Marine Biology, MBA Finance)", "") { vm.pickDiscipline(it) } }
        }
    }
    if (fixing) InstitutionFixDialog(school, onDismiss = { fixing = false }) { k, t -> fixing = false; vm.correctInstitution(k, t) }
}

@Composable
private fun InstitutionFixDialog(school: School, onDismiss: () -> Unit, onSave: (InstitutionKind, CollegeType?) -> Unit) {
    var kind by remember { mutableStateOf(school.kind) }
    var type by remember { mutableStateOf(school.collegeType) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("What is ${school.name.take(40)}?") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Your fix is saved for every alumnus who comes after you.", color = Brand.TextDim, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                KindTabs(kind, 0, 0) { kind = it }
                if (kind == InstitutionKind.COLLEGE) CollegeTypeChips(type) { type = it }
            }
        },
        confirmButton = { TextButton({ onSave(kind, type) }) { Text("Save") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun BatchStep(s: FinderState, vm: FinderViewModel) {
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val decades = remember(vm.years) { vm.years.map { it / 10 * 10 }.distinct() }
    val school = s.school
    val isCollege = school?.kind == InstitutionKind.COLLEGE
    var fixing by remember { mutableStateOf(false) }
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
                school?.let { sc ->
                    GlassCard(Modifier.fillMaxWidth(), corner = 20.dp, padding = PaddingValues(16.dp)) {
                        Text(sc.name, style = MaterialTheme.typography.titleLarge)
                        if (sc.region.label.isNotBlank()) Text(sc.region.label, color = Brand.TextDim)
                        val total = s.stats.sumOf { it.members }
                        if (total > 0) { Spacer(Modifier.height(8.dp)); Pill("🔥 $total alumni already joined", Brand.Sun) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (isCollege) {
                    s.track?.let { t ->
                        GlassCard(Modifier.fillMaxWidth(), corner = 18.dp, padding = PaddingValues(14.dp)) {
                            Text("YOUR COURSE", style = Eyebrow)
                            Text(t.label, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    val profile = school?.schoolProfile
                    val tracks = if (s.showAllStandards) profile?.allTracks.orEmpty() else profile?.tracks.orEmpty()
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        tracks.forEach { t ->
                            val selected = s.track?.code == t.code
                            Box(
                                Modifier.widthIn(min = if (tracks.size <= 2) 150.dp else 104.dp).height(64.dp).clip(RoundedCornerShape(18.dp))
                                    .background(if (selected) Brand.Primary else Brush.linearGradient(listOf(Color(0x1AFFFFFF), Color(0x10FFFFFF))))
                                    .border(1.dp, if (selected) Color.Transparent else Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                                    .clickable { vm.pickTrack(t) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(Modifier.padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(t.short, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1)
                                    Text(t.label.substringBefore(" · ").takeIf { it != t.short } ?: t.label.substringAfter("· "),
                                        fontSize = 11.sp, color = Brand.TextDim, maxLines = 1)
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!s.showAllStandards) TextButton(vm::showAllStandards) { Text("Other class") }
                        Spacer(Modifier.weight(1f))
                        TextButton({ fixing = true }) { Text("It's a college?") }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(if (isCollege) "Year you graduated" else "Year you passed out", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
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
            val count = vm.membersFor(s.track, y)
            YearTile(y, count, y == s.year) { vm.pickYear(y) }
        }
    }
    if (fixing && school != null) InstitutionFixDialog(school, onDismiss = { fixing = false }) { k, t -> fixing = false; vm.correctInstitution(k, t) }
}

@Composable
private fun YearTile(year: Int, count: Int, selected: Boolean, onClick: () -> Unit) {
    val ago = Calendar.getInstance().get(Calendar.YEAR) - year
    val milestone = ago >= 5 && ago % 5 == 0
    Box(
        Modifier.aspectRatio(1f).clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    selected -> Brand.Primary
                    count > 0 -> Brush.linearGradient(listOf(Brand.Violet.copy(alpha = 0.45f), Brand.Coral.copy(alpha = 0.35f)))
                    else -> Brush.linearGradient(listOf(Color(0x18FFFFFF), Color(0x0CFFFFFF)))
                },
            )
            .border(1.dp, if (milestone) Brand.Sun.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$year", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            when {
                count > 0 -> Text("👥 $count", fontSize = 11.sp, color = Brand.Sun)
                milestone -> Text("🎉 $ago yrs", fontSize = 10.sp, color = Brand.Sun)
            }
        }
    }
}

@Composable
private fun SectionStep(s: FinderState, vm: FinderViewModel) {
    val isCollege = s.school?.kind == InstitutionKind.COLLEGE
    val word = if (isCollege) "Section" else s.school?.schoolProfile?.system?.sectionWord ?: "Section"
    var picked by rememberSaveable { mutableStateOf<String?>(if (isCollege) WHOLE_BATCH else null) }
    var custom by rememberSaveable { mutableStateOf("") }
    val sections = (if (isCollege) listOf(WHOLE_BATCH) else emptyList()) + listOf("A", "B", "C", "D", "E", "F", "G", "H").take(if (isCollege) 7 else 8)
    Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp)) {
        Text(
            if (isCollege) "Most college batches stay together as one group. Had sections? Pick yours."
            else "Pick your ${word.lowercase()}. Don't remember? Choose the one most friends were in — you can join more later.",
            color = Brand.TextDim,
        )
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(sections, span = { if (it == WHOLE_BATCH) GridItemSpan(maxLineSpan) else GridItemSpan(1) }) { sec ->
                val count = vm.membersFor(sec)
                val selected = picked == sec
                val pulse = rememberInfiniteTransition(label = "p").animateFloat(
                    1f, if (count > 0 && !selected) 1.05f else 1f,
                    infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "s",
                )
                Box(
                    Modifier.then(if (sec == WHOLE_BATCH) Modifier.height(76.dp) else Modifier.aspectRatio(1f))
                        .graphicsLayer { scaleX = pulse.value; scaleY = pulse.value }
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
                        Text(if (sec == WHOLE_BATCH) "👥 Whole batch" else sec, fontSize = if (sec == WHOLE_BATCH) 20.sp else 30.sp, fontWeight = FontWeight.Black)
                        Text(if (count > 0) "$count here" else "be first", fontSize = 11.sp, color = if (count > 0) Brand.Sun else Brand.TextDim)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            custom, { custom = it.take(20); if (it.isNotBlank()) picked = null },
            Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp),
            label = { Text(if (isCollege) "Other section (e.g. Batch 2, Evening)" else "Other ${word.lowercase()} / stream (e.g. 11B, Science, Commerce)") },
        )
        s.error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.weight(1f))
        val choice = picked ?: custom.trim().takeIf { it.isNotEmpty() }
        GradientButton(
            when {
                choice == null -> "Choose a ${word.lowercase()}"
                choice == WHOLE_BATCH -> "Join my batch 🎉"
                else -> "Join $word ${choice.uppercase()} 🎉"
            },
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
