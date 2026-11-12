package com.batchmates.app.ui.finder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batchmates.app.data.AppUser
import com.batchmates.app.data.BatchKey
import com.batchmates.app.data.BatchStat
import com.batchmates.app.data.Country
import com.batchmates.app.data.CollegeType
import com.batchmates.app.data.InstitutionKind
import com.batchmates.app.data.Level
import com.batchmates.app.data.Degrees
import com.batchmates.app.data.Field
import com.batchmates.app.data.Track
import com.batchmates.app.data.Place
import com.batchmates.app.data.Region
import com.batchmates.app.data.ReunionRepository
import com.batchmates.app.data.School
import com.batchmates.app.data.geo.GeoRepository
import com.batchmates.app.util.SchoolNames
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

private const val TOWN_RADIUS_KM = 12.0
private const val DISTRICT_RADIUS_KM = 40.0

enum class Step(val title: String) {
    QUICK("Search"), COUNTRY("Country"), STATE("State"), DISTRICT("District"), TOWN("Town"), SCHOOL("School"),
    MAP("Map"), CREATE("New school"), COURSE("Course"), BATCH("Batch"), SECTION("Section"),
}

sealed interface Load<out T> {
    data object Idle : Load<Nothing>
    data object Loading : Load<Nothing>
    data class Ready<T>(val value: T) : Load<T>
    data class Failed(val message: String) : Load<Nothing>
}

data class FinderState(
    val step: Step = Step.COUNTRY,
    val countries: List<Country> = emptyList(),
    val suggestedCountry: String? = null,
    val country: Country? = null,
    val state: String? = null,
    val districts: Load<List<String>> = Load.Idle,
    val district: String? = null,
    val schools: Load<List<School>> = Load.Idle,
    val school: School? = null,
    val stats: List<BatchStat> = emptyList(),
    /** What the batch studied: a school standard or a college course. */
    val track: Track? = null,
    /** Schools or Colleges tab on the results list. */
    val tab: InstitutionKind = InstitutionKind.SCHOOL,
    /** College: chosen level (Bachelor's…) and field (Engineering…) while picking a discipline. */
    val level: Level? = null,
    val field: Field? = null,
    val showAllProgrammes: Boolean = false,
    val showAllStandards: Boolean = false,
    val year: Int? = null,
    val joining: Boolean = false,
    val error: String? = null,
    // map
    val mapSchools: List<School> = emptyList(),
    val mapLoading: Boolean = false,
    val mapHint: String? = null,
    val searchResults: Load<List<School>> = Load.Idle,
    val pinned: Pair<Double, Double>? = null,
    val createRegion: Region? = null,
    val createName: String = "",
    val districtCenter: Pair<Double, Double>? = null,
    val lastQuery: String = "",
    // town
    val town: Place? = null,
    val townResults: Load<List<Place>> = Load.Idle,
    /** Town-first search anywhere in the world (the quick path). */
    val anywhereResults: Load<List<Place>> = Load.Idle,
    /** Towns where the district's mapped schools are, most schools first. */
    val townChips: List<Place> = emptyList(),
    val townsLoaded: Boolean = false,
    /** The fast list is showing; the complete one is still arriving. */
    val refining: Boolean = false,
    // duplicate guard
    val duplicates: List<School> = emptyList(),
    val confirmCreate: Boolean = false,
) {
    val region: Region
        get() = Region(country?.code.orEmpty(), country?.name.orEmpty(), state.orEmpty(), district.orEmpty(), town?.name.orEmpty())
}

class FinderViewModel(
    private val geo: GeoRepository,
    private val repo: ReunionRepository,
    private val user: AppUser,
    startOnMap: Boolean,
) : ViewModel() {

    private val _s = MutableStateFlow(FinderState(step = if (startOnMap) Step.MAP else Step.QUICK))
    val state: StateFlow<FinderState> = _s
    private val history = ArrayDeque<Step>()
    private val _joined = Channel<String>(Channel.BUFFERED)
    val joined = _joined.receiveAsFlow()
    private var mapJob: Job? = null
    private var townJob: Job? = null
    private var loadJob: Job? = null
    private var prefetchJob: Job? = null
    /** Every school we've seen in this district (map data + alumni-added); used to stop duplicates. */
    private var known: List<School> = emptyList()

    val years: List<Int> = (Calendar.getInstance().get(Calendar.YEAR) downTo 1960).toList()

    init {
        viewModelScope.launch {
            val all = geo.countries()
            val local = Locale.getDefault().country.uppercase()
            _s.update { it.copy(countries = all, suggestedCountry = local.takeIf { c -> all.any { it.code == c } }) }
        }
    }

    // ---------- navigation inside the flow ----------

    private fun go(step: Step) {
        history.addLast(_s.value.step)
        _s.update { it.copy(step = step, error = null) }
    }

    /** @return false when the flow is at its first step (caller should leave the screen). */
    fun back(): Boolean {
        val prev = history.removeLastOrNull() ?: return false
        _s.update { it.copy(step = prev, error = null, joining = false) }
        return true
    }

    // ---------- browse path ----------

    // ---------- quick path: type a town, skip country/state/district ----------

    private var anywhereJob: Job? = null

    fun searchAnywhere(text: String) {
        anywhereJob?.cancel()
        if (text.trim().length < 2) { _s.update { it.copy(anywhereResults = Load.Idle) }; return }
        anywhereJob = viewModelScope.launch {
            delay(300)
            _s.update { it.copy(anywhereResults = Load.Loading) }
            val home = _s.value.suggestedCountry
            // 1) Instant answer from our own town index.
            val local = geo.searchPlacesLocal(text, null, "", home.orEmpty())
            if (local.isNotEmpty()) _s.update { it.copy(anywhereResults = Load.Ready(local)) }
            // 2) Only if that's thin (small villages, uncovered countries), ask the public geocoder — time-limited.
            if (local.size < 4) {
                val remote = geo.searchPlacesRemote(text, null, "", bias = home?.let { geo.countryBias(it) })
                    .sortedByDescending { it.countryCode == home }
                val all = geo.mergePlaces(local, remote)
                _s.update { it.copy(anywhereResults = if (all.isEmpty() && remote.isEmpty() && local.isEmpty()) Load.Ready(emptyList()) else Load.Ready(all)) }
            }
        }
    }

    fun browse() = go(Step.COUNTRY)

    /** Town picked from the quick search: fill country, state and district for the user, then show schools. */
    fun pickAnywhere(place: Place) {
        viewModelScope.launch {
            val country = _s.value.countries.firstOrNull { it.code == place.countryCode }
                ?: Country(place.countryCode, place.country, "", emptyList())
            val district = geo.districtFor(place)
            _s.update {
                it.copy(country = country, state = place.state, district = district, town = place,
                    districtCenter = place.lat to place.lon, schools = Load.Idle, lastQuery = "")
            }
            known = emptyList()
            go(Step.SCHOOL)
            loadSchools(null)
        }
    }

    fun pickCountry(c: Country) {
        _s.update { it.copy(country = c, state = null, district = null, districts = Load.Idle, schools = Load.Idle) }
        go(Step.STATE)
    }

    fun pickState(state: String) {
        _s.update { it.copy(state = state.trim(), district = null, districts = Load.Loading, schools = Load.Idle) }
        go(Step.DISTRICT)
        loadDistricts()
    }

    fun loadDistricts() {
        val s = _s.value
        val country = s.country ?: return
        val state = s.state ?: return
        _s.update { it.copy(districts = Load.Loading) }
        viewModelScope.launch {
            val result = runCatching { geo.districts(country.code, state) }
            _s.update {
                it.copy(districts = result.fold({ d -> Load.Ready(d) }, { e -> Load.Failed(e.message ?: "Couldn't load districts") }))
            }
        }
    }

    /** Pass blank to skip the district and search the whole state by name. */
    fun pickDistrict(district: String) {
        _s.update {
            it.copy(
                district = district.trim(), town = null, schools = Load.Idle, districtCenter = null, lastQuery = "",
                townResults = Load.Idle, townChips = emptyList(), townsLoaded = false,
            )
        }
        known = emptyList()
        if (district.isBlank()) { go(Step.SCHOOL); return }
        go(Step.TOWN)
        val region = _s.value.region
        viewModelScope.launch {
            val c = geo.districtCenter(region)
            _s.update { it.copy(districtCenter = c) }
            launch { remember(repo.customSchools(region)) } // for the duplicate guard, off the critical path
            // One ~0.7 s request for the district's towns (cached on the phone afterwards).
            val chips = c?.let { runCatching { geo.townsNear(it, region.state, region.countryCode) }.getOrDefault(emptyList()) }.orEmpty().take(16)
            if (_s.value.district != region.district) return@launch
            _s.update { it.copy(townChips = chips, townsLoaded = true) }
            // Warm the cache for the likeliest towns while the user reads the list, so tapping one is instant.
            prefetchJob?.cancel()
            prefetchJob = viewModelScope.launch {
                chips.take(4).forEach { p -> runCatching { geo.schoolsFast(p.lat to p.lon, TOWN_RADIUS_KM, region, null) } }
            }
        }
    }

    fun searchTowns(text: String) {
        townJob?.cancel()
        if (text.trim().length < 2) { _s.update { it.copy(townResults = Load.Idle) }; return }
        townJob = viewModelScope.launch {
            delay(350)
            _s.update { it.copy(townResults = Load.Loading) }
            val s = _s.value
            val cc = s.country?.code.orEmpty()
            val local = geo.searchPlacesLocal(text, s.districtCenter, s.state.orEmpty(), cc)
            if (local.isNotEmpty()) _s.update { it.copy(townResults = Load.Ready(local)) }
            if (local.size < 4) {
                val remote = geo.searchPlacesRemote(text, s.districtCenter, s.state.orEmpty())
                _s.update { it.copy(townResults = Load.Ready(geo.mergePlaces(local, remote))) }
            }
        }
    }

    fun pickTown(place: Place?) {
        _s.update { it.copy(town = place, schools = Load.Idle, lastQuery = "") }
        go(Step.SCHOOL)
        loadSchools(null)
    }

    /**
     * Two-stage load: the fast answer (one request, ~1 s, often instant from cache) is shown at once;
     * the complete answer (Overpass, slow) merges in later without blocking anything.
     */
    fun loadSchools(nameFilter: String?) {
        val s0 = _s.value
        val region = s0.region
        val town = s0.town
        if (region.district.isBlank() && nameFilter.isNullOrBlank()) {
            _s.update { it.copy(schools = Load.Ready(emptyList()), refining = false) }; return
        }
        loadJob?.cancel()
        _s.update { it.copy(schools = Load.Loading, lastQuery = nameFilter?.trim().orEmpty(), refining = false) }
        loadJob = viewModelScope.launch {
            val center = town?.let { it.lat to it.lon } ?: s0.districtCenter ?: geo.districtCenter(region)
            val radius = if (town != null) TOWN_RADIUS_KM else DISTRICT_RADIUS_KM
            val customJob = async { runCatching { repo.customSchools(region) }.getOrDefault(emptyList()) }
            val fullJob = async { runCatching { geo.schoolsFull(center, radius, region, nameFilter) } }
            val fast = runCatching { geo.schoolsFast(center, radius, region, nameFilter) }

            fun nearby(custom: List<School>) = custom.map { c ->
                val d = if (center != null && c.lat != null && c.lon != null) SchoolNames.km(center.first, center.second, c.lat, c.lon) else null
                c.copy(distanceKm = d)
            }.filter { c ->
                val near = when {
                    town != null && (c.region.town.equals(town.name, true) || c.locality.equals(town.name, true)) -> true
                    c.distanceKm != null -> c.distanceKm <= radius * 1.3
                    else -> c.region.district.equals(region.district, true) // no coordinates: fall back to the district name
                }
                near && (nameFilter.isNullOrBlank() || SchoolNames.matches(nameFilter, c))
            }

            fun publish(custom: List<School>, osm: List<School>, refining: Boolean, error: Throwable?) {
                remember(custom + osm)
                // Alumni-edited copies (aliases, corrected type) share the map id and win.
                val byId = custom.associateBy { it.id }
                val merged = geo.merge(custom, osm.filter { it.id !in byId })
                _s.update {
                    it.copy(
                        refining = refining,
                        schools = if (merged.isEmpty() && error != null && !refining) {
                            Load.Failed(error.message ?: "Couldn't load schools. Check your internet and retry.")
                        } else if (merged.isEmpty() && refining) {
                            it.schools.takeIf { l -> l is Load.Ready } ?: Load.Loading
                        } else Load.Ready(merged),
                    )
                }
            }

            val custom = nearby(customJob.await())
            publish(custom, fast.getOrDefault(emptyList()), refining = true, error = fast.exceptionOrNull())
            // Never keep people waiting on the slow source: give it 15 s, then settle on what we have.
            val full = withTimeoutOrNull(15_000) { fullJob.await() } ?: Result.failure(IllegalStateException("timeout"))
            publish(custom, geo.merge(fast.getOrDefault(emptyList()), full.getOrDefault(emptyList())), refining = false,
                error = if (fast.isFailure && full.isFailure) fast.exceptionOrNull() else null)
        }
    }

    private fun remember(list: List<School>) {
        known = (known + list).distinctBy { it.id }
    }

    fun pickSchool(school: School) {
        // Schools from a list already know their region; map / search picks may need it filled in.
        val fixed = if (school.region.state.isBlank() && _s.value.state != null) school.copy(region = _s.value.region) else school
        val firstTrack = if (fixed.kind == InstitutionKind.SCHOOL) fixed.schoolProfile.tracks.firstOrNull() else null
        _s.update {
            it.copy(school = fixed, stats = emptyList(), year = null, track = firstTrack, field = null,
                level = defaultLevel(fixed.collegeType), showAllProgrammes = false, showAllStandards = false)
        }
        go(if (fixed.kind == InstitutionKind.COLLEGE) Step.COURSE else Step.BATCH)
        viewModelScope.launch {
            val withRegion = if (fixed.region.countryCode.isBlank() && fixed.lat != null && fixed.lon != null) {
                runCatching { geo.reverse(fixed.lat, fixed.lon) }.getOrNull()?.let { fixed.copy(region = it) } ?: fixed
            } else fixed
            val stats = repo.batchStats(withRegion.id)
            _s.update { it.copy(school = withRegion, stats = stats) }
        }
    }

    fun openMap() = go(Step.MAP)

    fun openCreate(lat: Double? = null, lon: Double? = null, name: String = "") {
        val t = _s.value.town
        _s.update {
            it.copy(
                pinned = if (lat != null && lon != null) lat to lon else null,
                createRegion = it.region, createName = name.trim(), duplicates = emptyList(), confirmCreate = false,
            )
        }
        go(Step.CREATE)
        onCreateNameChanged(name)
        if (lat != null && lon != null) viewModelScope.launch {
            runCatching { geo.reverse(lat, lon) }.getOrNull()?.let { r ->
                _s.update { it.copy(createRegion = r.copy(town = t?.name ?: r.district)) }
            }
        }
    }

    /** Live duplicate check while the user types a new school's name. */
    fun onCreateNameChanged(name: String) {
        val pin = _s.value.pinned
        val dups = if (name.trim().length < 3) emptyList() else {
            val t = _s.value.town
            SchoolNames.duplicates(name, pin?.first, pin?.second, known)
                .sortedBy { s -> if (t != null && s.lat != null && s.lon != null) SchoolNames.km(t.lat, t.lon, s.lat, s.lon) else 999.0 }
                .take(5)
        }
        _s.update { it.copy(duplicates = dups, confirmCreate = false) }
    }

    /** "Yes, that's my school": use it, and remember the name the user typed as an alias. */
    fun useExisting(school: School, typedName: String) {
        viewModelScope.launch { repo.addAlias(school, typedName) }
        pickSchool(school)
    }

    fun dismissConfirm() = _s.update { it.copy(confirmCreate = false) }

    fun setTab(kind: InstitutionKind) = _s.update { it.copy(tab = kind) }

    fun createSchool(
        name: String, region: Region, kind: InstitutionKind, collegeType: CollegeType?, force: Boolean = false,
    ) {
        if (name.trim().length < 3) { _s.update { it.copy(error = "Please enter the full school name") }; return }
        if (region.country.isBlank()) { _s.update { it.copy(error = "Please add at least the country") }; return }
        if (region.town.isBlank()) { _s.update { it.copy(error = "Add the town or village so classmates can find it") }; return }
        if (!force && _s.value.duplicates.isNotEmpty()) { _s.update { it.copy(confirmCreate = true) }; return }
        _s.update { it.copy(joining = true, error = null, confirmCreate = false) }
        viewModelScope.launch {
            val pin = _s.value.pinned ?: _s.value.town?.let { it.lat to it.lon }
            repo.createSchool(name, region, pin?.first, pin?.second, user, kind, if (kind == InstitutionKind.COLLEGE) collegeType else null)
                .onSuccess { school -> remember(listOf(school)); _s.update { it.copy(joining = false) }; pickSchool(school) }
                .onFailure { e -> _s.update { it.copy(joining = false, error = e.message) } }
        }
    }

    // ---------- map path ----------

    fun onMapMoved(south: Double, west: Double, north: Double, east: Double, zoom: Double) {
        mapJob?.cancel()
        if (zoom < 12.5) {
            _s.update { it.copy(mapHint = "Zoom in to see schools, or search above", mapLoading = false) }
            return
        }
        mapJob = viewModelScope.launch {
            delay(300) // wait until the user stops panning
            _s.update { it.copy(mapLoading = true, mapHint = null) }
            val fullJob = async { runCatching { geo.schoolsInBoxFull(south, west, north, east) } }
            val fast = runCatching { geo.schoolsInBoxFast(south, west, north, east) }
            fast.getOrNull()?.let { pins -> _s.update { it.copy(mapSchools = pins) } } // pins appear in ~1 s
            val full = withTimeoutOrNull(15_000) { fullJob.await() } ?: Result.failure(IllegalStateException("timeout"))
            val all = geo.merge(fast.getOrDefault(emptyList()), full.getOrDefault(emptyList()))
            _s.update {
                it.copy(
                    mapLoading = false,
                    mapSchools = all.ifEmpty { it.mapSchools },
                    mapHint = when {
                        fast.isFailure && full.isFailure -> "Map service is busy, try again shortly"
                        all.isEmpty() -> "No schools mapped here yet — long-press to add yours"
                        else -> null
                    },
                )
            }
        }
    }

    fun searchOnMap(query: String) {
        if (query.isBlank()) { _s.update { it.copy(searchResults = Load.Idle) }; return }
        _s.update { it.copy(searchResults = Load.Loading) }
        viewModelScope.launch {
            val r = runCatching { geo.searchSchools(query, _s.value.country?.code) }
            _s.update { st -> st.copy(searchResults = r.fold({ Load.Ready(it) }, { Load.Failed(it.message ?: "Search failed") })) }
        }
    }

    fun clearSearch() = _s.update { it.copy(searchResults = Load.Idle) }

    // ---------- batch + section ----------

    fun pickTrack(t: Track) = _s.update { it.copy(track = t) }
    fun showAllStandards() = _s.update { it.copy(showAllStandards = true) }
    fun showAllProgrammes() = _s.update { it.copy(showAllProgrammes = true) }

    private fun defaultLevel(type: CollegeType?): Level {
        val allowed = type?.levels ?: Level.entries.toSet()
        return if (Level.BACHELOR in allowed) Level.BACHELOR else Level.entries.first { it in allowed }
    }

    /** Levels this college awards ("Show all" lifts every filter for unusual cases). */
    fun levelsForCollege(): List<Level> {
        val s = _s.value
        val type = s.school?.collegeType
        return if (s.showAllProgrammes || type == null) Level.entries else Level.entries.filter { it in type.levels }
    }

    /** Fields this college teaches: an engineering institute never shows Arts or Medicine. */
    fun fieldsForCollege(): List<Field> {
        val s = _s.value
        val type = s.school?.collegeType
        return if (s.showAllProgrammes || type == null) Field.entries else Field.entries.filter { type.allows(it, s.level) }
    }

    fun disciplinesFor(field: Field): List<String> {
        val s = _s.value
        val type = s.school?.collegeType
        return if (s.showAllProgrammes || type == null || !type.allows(field)) field.disciplines else type.disciplines(field)
    }

    /** Local degree name hint for the institution's country (B.E./B.Tech, BA/BS, BSc (Hons), Licence…). */
    fun degreeHint(field: Field): String? {
        val s = _s.value
        val level = s.level ?: return null
        return Degrees.localName(s.school?.region?.countryCode.orEmpty(), level, field)
    }

    /** Batches that already exist at this institution, biggest first — the fastest path for most people. */
    fun existingTracks(): List<Pair<Track, Int>> = _s.value.stats
        .groupBy { it.track.code }
        .map { (_, list) -> list.first().track to list.sumOf { it.members } }
        .filter { (t, _) -> t.isCollege == (_s.value.school?.kind == InstitutionKind.COLLEGE) }
        .sortedByDescending { it.second }

    fun pickLevel(l: Level) = _s.update { it.copy(level = l) }

    fun pickField(f: Field) {
        val disciplines = disciplinesFor(f)
        if (disciplines.size == 1) pickDiscipline(disciplines.first()) else _s.update { it.copy(field = f) }
    }

    fun clearField() = _s.update { it.copy(field = null) }

    /** "CSE", "Computer Science and Engineering" and "computer science" all become one batch. */
    fun pickDiscipline(text: String) {
        val level = _s.value.level ?: return
        if (text.isBlank()) return
        pickCourse(Track.college(level, Degrees.canonical(text)))
    }

    fun pickCourse(t: Track) {
        _s.update { it.copy(track = t, field = null) }
        go(Step.BATCH)
    }

    /** Alumni fix a wrong guess ("this is an Arts college, not Engineering"); saved for everyone. */
    fun correctInstitution(kind: InstitutionKind, collegeType: CollegeType?) {
        val school = _s.value.school ?: return
        val fixed = school.copy(kind = kind, collegeType = if (kind == InstitutionKind.COLLEGE) collegeType else null)
        viewModelScope.launch { repo.setInstitutionType(school, fixed.kind, fixed.collegeType) }
        _s.update {
            it.copy(
                school = fixed, field = null, showAllProgrammes = false, level = defaultLevel(fixed.collegeType),
                track = if (kind == InstitutionKind.SCHOOL) fixed.schoolProfile.tracks.firstOrNull() else null,
                step = if (kind == InstitutionKind.COLLEGE) Step.COURSE else Step.BATCH,
            )
        }
    }

    fun pickYear(y: Int) {
        _s.update { it.copy(year = y) }
        go(Step.SECTION)
    }

    fun join(section: String) {
        val s = _s.value
        val school = s.school ?: return
        val year = s.year ?: return
        val sec = section.trim().uppercase()
        if (sec.isBlank()) return
        _s.update { it.copy(joining = true, error = null) }
        viewModelScope.launch {
            val track = s.track ?: return@launch
            repo.joinBatch(school, BatchKey(school.id, track, year, sec), user)
                .onSuccess { g -> _joined.send(g.id) }
                .onFailure { e -> _s.update { it.copy(joining = false, error = e.message ?: "Couldn't join. Try again.") } }
        }
    }

    fun membersFor(track: Track?, year: Int): Int =
        _s.value.stats.filter { it.track.code == track?.code && it.year == year }.sumOf { it.members }

    fun membersFor(section: String): Int {
        val s = _s.value
        return s.stats.firstOrNull { it.track.code == s.track?.code && it.year == s.year && it.section.equals(section, true) }?.members ?: 0
    }
}
