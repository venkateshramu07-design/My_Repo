package com.batchmates.app.ui.finder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batchmates.app.data.AppUser
import com.batchmates.app.data.BatchKey
import com.batchmates.app.data.BatchStat
import com.batchmates.app.data.Country
import com.batchmates.app.data.Grade
import com.batchmates.app.data.Region
import com.batchmates.app.data.ReunionRepository
import com.batchmates.app.data.School
import com.batchmates.app.data.geo.GeoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

enum class Step(val title: String) {
    COUNTRY("Country"), STATE("State"), DISTRICT("District"), SCHOOL("School"),
    MAP("Map"), CREATE("New school"), BATCH("Batch"), SECTION("Section"),
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
    val grade: Grade = Grade.TENTH,
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
) {
    val region: Region
        get() = Region(country?.code.orEmpty(), country?.name.orEmpty(), state.orEmpty(), district.orEmpty())
}

class FinderViewModel(
    private val geo: GeoRepository,
    private val repo: ReunionRepository,
    private val user: AppUser,
    startOnMap: Boolean,
) : ViewModel() {

    private val _s = MutableStateFlow(FinderState(step = if (startOnMap) Step.MAP else Step.COUNTRY))
    val state: StateFlow<FinderState> = _s
    private val history = ArrayDeque<Step>()
    private val _joined = Channel<String>(Channel.BUFFERED)
    val joined = _joined.receiveAsFlow()
    private var mapJob: Job? = null

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
        _s.update { it.copy(district = district.trim(), schools = Load.Idle) }
        go(Step.SCHOOL)
        if (district.isNotBlank()) loadSchools(null)
    }

    fun loadSchools(nameFilter: String?) {
        val region = _s.value.region
        if (region.district.isBlank() && nameFilter.isNullOrBlank()) {
            _s.update { it.copy(schools = Load.Ready(emptyList())) }; return
        }
        _s.update { it.copy(schools = Load.Loading) }
        viewModelScope.launch {
            val custom = repo.customSchools(region)
            val osm = runCatching { geo.schoolsIn(region, nameFilter) }
            _s.update {
                it.copy(
                    schools = osm.fold(
                        { list -> Load.Ready(custom + list) },
                        { e -> if (custom.isNotEmpty()) Load.Ready(custom) else Load.Failed(e.message ?: "Couldn't load schools") },
                    ),
                )
            }
        }
    }

    fun pickSchool(school: School) {
        // Schools from a list already know their region; map / search picks may need it filled in.
        val fixed = if (school.region.state.isBlank() && _s.value.state != null) school.copy(region = _s.value.region) else school
        _s.update { it.copy(school = fixed, stats = emptyList(), year = null) }
        go(Step.BATCH)
        viewModelScope.launch {
            val withRegion = if (fixed.region.countryCode.isBlank() && fixed.lat != null && fixed.lon != null) {
                runCatching { geo.reverse(fixed.lat, fixed.lon) }.getOrNull()?.let { fixed.copy(region = it) } ?: fixed
            } else fixed
            val stats = repo.batchStats(withRegion.id)
            _s.update { it.copy(school = withRegion, stats = stats) }
        }
    }

    fun openMap() = go(Step.MAP)

    fun openCreate(lat: Double? = null, lon: Double? = null) {
        _s.update { it.copy(pinned = if (lat != null && lon != null) lat to lon else null, createRegion = it.region) }
        go(Step.CREATE)
        if (lat != null && lon != null) viewModelScope.launch {
            runCatching { geo.reverse(lat, lon) }.getOrNull()?.let { r -> _s.update { it.copy(createRegion = r) } }
        }
    }

    fun createSchool(name: String, region: Region) {
        if (name.trim().length < 3) { _s.update { it.copy(error = "Please enter the full school name") }; return }
        if (region.country.isBlank()) { _s.update { it.copy(error = "Please add at least the country") }; return }
        _s.update { it.copy(joining = true, error = null) }
        viewModelScope.launch {
            val pin = _s.value.pinned
            repo.createSchool(name, region, pin?.first, pin?.second, user)
                .onSuccess { school -> _s.update { it.copy(joining = false) }; pickSchool(school) }
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
            delay(450) // wait until the user stops panning
            _s.update { it.copy(mapLoading = true, mapHint = null) }
            val list = runCatching { geo.schoolsInBox(south, west, north, east) }
            _s.update {
                it.copy(
                    mapLoading = false,
                    mapSchools = list.getOrNull() ?: it.mapSchools,
                    mapHint = when {
                        list.isFailure -> "Map service is busy, try again shortly"
                        list.getOrNull().isNullOrEmpty() -> "No schools mapped here yet — long-press to add yours"
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

    fun pickGrade(g: Grade) = _s.update { it.copy(grade = g) }

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
            repo.joinBatch(school, BatchKey(school.id, s.grade, year, sec), user)
                .onSuccess { g -> _joined.send(g.id) }
                .onFailure { e -> _s.update { it.copy(joining = false, error = e.message ?: "Couldn't join. Try again.") } }
        }
    }

    fun membersFor(grade: Grade, year: Int): Int = _s.value.stats.filter { it.grade == grade && it.year == year }.sumOf { it.members }
    fun membersFor(section: String): Int {
        val s = _s.value
        return s.stats.firstOrNull { it.grade == s.grade && it.year == s.year && it.section.equals(section, true) }?.members ?: 0
    }
}
