package com.batchmates.app.data.geo

import android.content.Context
import com.batchmates.app.data.Classifier
import com.batchmates.app.data.Country
import com.batchmates.app.data.InstitutionKind
import com.batchmates.app.data.Place
import com.batchmates.app.data.Region
import com.batchmates.app.data.School
import com.batchmates.app.data.SchoolSource
import com.batchmates.app.util.SchoolNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Everything location related, at zero cost:
 *  - Countries and states are bundled (countries-states-cities-database, ODbL).
 *  - Districts and schools come live from OpenStreetMap via Overpass, cached on disk.
 *  - Free-text school search and reverse geocoding use Nominatim (1 request/second policy).
 */
class GeoRepository(private val context: Context) {

    private val http = Http(context)
    private val background = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val nominatimLock = Mutex()
    private var lastNominatimCall = 0L

    @Volatile private var countriesCache: List<Country>? = null

    suspend fun countries(): List<Country> = countriesCache ?: withContext(Dispatchers.IO) {
        val json = context.assets.open("regions.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val s = o.getJSONArray("s")
            Country(o.getString("c"), o.getString("n"), o.optString("e"), List(s.length()) { s.getString(it) })
        }.also { countriesCache = it }
    }

    /** A district name plus its centre point (when known). */
    private data class BundledDistrict(val name: String, val lat: Double?, val lon: Double?)

    @Volatile private var bundledDistricts: Map<String, Map<String, List<BundledDistrict>>>? = null

    /** Districts bundled in the app (all of India today), so the most common path never waits on a server. */
    private suspend fun bundled(): Map<String, Map<String, List<BundledDistrict>>> = bundledDistricts ?: withContext(Dispatchers.IO) {
        val root = JSONObject(context.assets.open("districts.json").bufferedReader().use { it.readText() })
        root.keys().asSequence().associateWith { cc ->
            val states = root.getJSONObject(cc)
            states.keys().asSequence().associateWith { st ->
                val arr = states.getJSONArray(st)
                List(arr.length()) {
                    val d = arr.getJSONArray(it)
                    BundledDistrict(d.getString(0), d.optDouble(1).takeIf { v -> !v.isNaN() }, d.optDouble(2).takeIf { v -> !v.isNaN() })
                }
            }
        }.also { bundledDistricts = it }
    }

    private suspend fun bundledState(countryCode: String, state: String): List<BundledDistrict>? =
        bundled()[countryCode.uppercase()]?.entries?.firstOrNull { it.key.equals(state.trim(), ignoreCase = true) }?.value

    /** Centre of a district: bundled, else looked up once with Photon. */
    suspend fun districtCenter(region: Region): Pair<Double, Double>? {
        if (region.district.isBlank()) return null
        bundledState(region.countryCode, region.state)
            ?.firstOrNull { it.name.equals(region.district, ignoreCase = true) && it.lat != null }
            ?.let { return it.lat!! to it.lon!! }
        val q = listOf(region.district, region.state, region.country).filter { it.isNotBlank() }.joinToString(", ")
        return runCatching {
            val f = JSONObject(http.get(PHOTON + "?limit=1&q=" + URLEncoder.encode(q, "UTF-8"), ttlMs = 90 * Http.DAY))
                .getJSONArray("features").getJSONObject(0)
            val c = f.getJSONObject("geometry").getJSONArray("coordinates")
            c.getDouble(1) to c.getDouble(0)
        }.getOrNull()
    }

    /** Districts inside a state: bundled list first, else OpenStreetMap (admin level 5, else 6). */
    suspend fun districts(countryCode: String, state: String): List<String> {
        bundledState(countryCode, state)?.let { list -> return list.map { it.name } }
        val q = """
            [out:json][timeout:25];
            ${stateArea(countryCode, state)}
            rel(area.sa)["boundary"="administrative"]["admin_level"~"^(5|6)$"];
            out tags;
        """.trimIndent()
        val elements = overpass(q, ttlMs = 30 * Http.DAY)
        val byLevel = (0 until elements.length()).map { elements.getJSONObject(it).getJSONObject("tags") }
            .groupBy { it.optString("admin_level") }
        val best = byLevel["5"]?.takeIf { it.size >= 2 } ?: byLevel["6"] ?: byLevel["5"].orEmpty()
        return best.map { it.optString("name:en").ifBlank { it.optString("name") } }
            .filter { it.isNotBlank() }
            .map { it.replace(Regex("\\s+district$", RegexOption.IGNORE_CASE), "") }
            .distinct().sorted()
    }

    /**
     * Schools in a district. Asks two independent free services at once and merges the answers:
     * Photon (fast OpenStreetMap search) and Overpass (complete but often busy).
     * Without a district, a school name is required and the whole state is searched.
     */
    suspend fun schoolsIn(region: Region, nameFilter: String? = null): List<School> = coroutineScope {
        val name = nameFilter?.trim()?.takeIf { it.length >= 2 }
        val center = districtCenter(region)
        if (center == null && name == null) return@coroutineScope emptyList()

        // Overpass is complete but often slow; run it detached so a slow server never blocks the screen.
        val overpass = if (center == null) null else background.async {
            val filter = name?.let { "[\"name\"~\"${regex(it)}\",i]" }.orEmpty()
            val q = """
                [out:json][timeout:25];
                nwr["amenity"~"^(school|college|university)$"]["name"]$filter(around:30000,${center.first},${center.second});
                out center tags 1500;
            """.trimIndent()
            runCatching { parseSchools(overpass(q, ttlMs = 14 * Http.DAY), null) }
        }
        // Photon answers in about a second; ask one word at a time (it throttles bursts).
        val photon = if (center != null) {
            val box = bbox(center, 0.4)
            (if (name != null) listOf(name) else PHOTON_SCHOOL_WORDS).map { w -> runCatching { photonSchools(w, box = box) } }
        } else {
            listOf(runCatching { photonSchools(name!!, stateFilter = region.state) })
        }
        val photonFound = photon.any { (it.getOrNull()?.size ?: 0) > 0 }
        val fromOverpass = overpass?.let { job ->
            if (photonFound) withTimeoutOrNull(5_000) { job.await() } else job.await()
        }
        val results = photon + listOfNotNull(fromOverpass)
        val schools = results.mapNotNull { it.getOrNull() }.flatten()
            .distinctBy { it.id }
            .map { it.copy(region = region) }
            .sortedBy { it.name.lowercase() }
        if (schools.isEmpty() && results.all { it.isFailure }) {
            throw IllegalStateException("Couldn't reach the school directory. Check your internet and retry.")
        }
        schools
    }

    private suspend fun photonSchools(q: String, box: String? = null, stateFilter: String? = null): List<School> {
        val url = buildString {
            append(PHOTON).append("?limit=50&osm_tag=amenity:school&osm_tag=amenity:college&osm_tag=amenity:university&q=").append(URLEncoder.encode(q, "UTF-8"))
            if (box != null) append("&bbox=").append(box)
        }
        val features = JSONObject(http.get(url, ttlMs = 14 * Http.DAY, readTimeoutMs = 15_000)).optJSONArray("features") ?: JSONArray()
        return (0 until features.length()).mapNotNull { i ->
            val f = features.getJSONObject(i)
            val p = f.optJSONObject("properties") ?: return@mapNotNull null
            val name = p.optString("name").ifBlank { return@mapNotNull null }
            if (stateFilter != null && !p.optString("state").equals(stateFilter, ignoreCase = true)) return@mapNotNull null
            val c = f.optJSONObject("geometry")?.optJSONArray("coordinates")
            School(
                id = "osm_" + p.optString("osm_type").take(1).lowercase() + p.optLong("osm_id"),
                name = name,
                region = Region(
                    p.optString("countrycode").uppercase(), p.optString("country"), p.optString("state"),
                    p.optString("county").ifBlank { p.optString("city") },
                ),
                lat = c?.optDouble(1), lon = c?.optDouble(0),
                locality = p.optString("city").ifBlank { p.optString("county") },
            ).classified(p.optString("osm_value"))
        }
    }

    /** Towns / villages / areas matching what the user types, near the chosen district. */
    suspend fun searchPlaces(text: String, near: Pair<Double, Double>?, state: String): List<Place> {
        if (text.trim().length < 2) return emptyList()
        val url = buildString {
            append(PHOTON).append("?limit=12&osm_tag=place&q=").append(URLEncoder.encode(text.trim(), "UTF-8"))
            if (near != null) append("&bbox=").append(bbox(near, 0.75))
        }
        val features = JSONObject(http.get(url, ttlMs = 30 * Http.DAY, readTimeoutMs = 12_000)).optJSONArray("features") ?: JSONArray()
        return (0 until features.length()).mapNotNull { i ->
            val f = features.getJSONObject(i)
            val p = f.optJSONObject("properties") ?: return@mapNotNull null
            val kind = p.optString("osm_value")
            if (kind !in PLACE_KINDS) return@mapNotNull null
            if (state.isNotBlank() && p.optString("state").isNotBlank() && !p.optString("state").equals(state, true)) return@mapNotNull null
            val c = f.optJSONObject("geometry")?.optJSONArray("coordinates") ?: return@mapNotNull null
            Place(
                name = p.optString("name").ifBlank { return@mapNotNull null },
                kind = kind,
                area = listOf(p.optString("county"), p.optString("district")).firstOrNull { it.isNotBlank() }.orEmpty(),
                lat = c.getDouble(1), lon = c.getDouble(0),
            )
        }.distinctBy { it.name.lowercase() + it.kind }
    }

    /** Schools around a town, nearest first. Towns are small, so ~10 km covers everything people mean by "in Kumbakonam". */
    suspend fun schoolsNear(place: Place, region: Region, nameFilter: String? = null, radiusKm: Double = 10.0): List<School> {
        val center = place.lat to place.lon
        val name = nameFilter?.trim()?.takeIf { it.length >= 2 }
        val overpass = background.async {
            val filter = name?.let { "[\"name\"~\"${regex(it)}\",i]" }.orEmpty()
            val q = """
                [out:json][timeout:25];
                nwr["amenity"~"^(school|college|university)$"]["name"]$filter(around:${(radiusKm * 1000).toInt()},${center.first},${center.second});
                out center tags 800;
            """.trimIndent()
            runCatching { parseSchools(overpass(q, ttlMs = 14 * Http.DAY), null) }
        }
        val box = bbox(center, radiusKm / 111.0)
        val photon = (if (name != null) listOf(name) else PHOTON_SCHOOL_WORDS).map { w -> runCatching { photonSchools(w, box = box) } }
        val found = photon.any { (it.getOrNull()?.size ?: 0) > 0 }
        val fromOverpass = if (found) withTimeoutOrNull(5_000) { overpass.await() } else overpass.await()
        val results = photon + listOfNotNull(fromOverpass)
        val schools = results.mapNotNull { it.getOrNull() }.flatten()
            .distinctBy { it.id }
            .map { s ->
                val d = if (s.lat != null && s.lon != null) SchoolNames.km(center.first, center.second, s.lat, s.lon) else null
                s.copy(region = region, distanceKm = d, locality = s.locality.ifBlank { if (d != null && d < 3) place.name else "" })
            }
            .filter { (it.distanceKm ?: 0.0) <= radiusKm * 1.3 }
            .sortedBy { it.distanceKm ?: Double.MAX_VALUE }
        if (schools.isEmpty() && results.all { it.isFailure }) {
            throw IllegalStateException("Couldn't reach the school directory. Check your internet and retry.")
        }
        return schools
    }

    private fun bbox(c: Pair<Double, Double>, d: Double) =
        "%.4f,%.4f,%.4f,%.4f".format(java.util.Locale.US, c.second - d, c.first - d, c.second + d, c.first + d)

    /** Schools visible in a map viewport. */
    suspend fun schoolsInBox(south: Double, west: Double, north: Double, east: Double): List<School> {
        val q = """
            [out:json][timeout:25];
            nwr["amenity"~"^(school|college|university)$"]["name"](${"%.4f".format(java.util.Locale.US, south)},${"%.4f".format(java.util.Locale.US, west)},${"%.4f".format(java.util.Locale.US, north)},${"%.4f".format(java.util.Locale.US, east)});
            out center tags 400;
        """.trimIndent()
        val fromOverpass = runCatching { parseSchools(overpass(q, ttlMs = 14 * Http.DAY), null) }
        fromOverpass.getOrNull()?.let { return it }
        val box = "%.4f,%.4f,%.4f,%.4f".format(java.util.Locale.US, west, south, east, north)
        return coroutineScope {
            PHOTON_SCHOOL_WORDS.map { w -> runCatching { photonSchools(w, box = box) }.getOrDefault(emptyList()) }
                .flatten().distinctBy { it.id }
        }
    }

    /** Free-text search, e.g. "St. Joseph's Higher Secondary School Trichy". */
    suspend fun searchSchools(query: String, countryCode: String?): List<School> {
        runCatching { photonSchools(query) }.getOrNull()
            ?.filter { countryCode.isNullOrBlank() || it.region.countryCode.equals(countryCode, true) }
            ?.takeIf { it.isNotEmpty() }?.let { return it }
        val url = buildString {
            append("https://nominatim.openstreetmap.org/search?format=jsonv2&addressdetails=1&limit=40&q=")
            append(URLEncoder.encode(query, "UTF-8"))
            if (!countryCode.isNullOrBlank()) append("&countrycodes=").append(countryCode.lowercase())
        }
        val arr = JSONArray(nominatim(url))
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.getJSONObject(i)
            val addr = o.optJSONObject("address") ?: JSONObject()
            val name = o.optString("name").ifBlank { o.optString("display_name").substringBefore(',') }
            if (name.isBlank()) return@mapNotNull null
            School(
                id = "osm_" + o.optString("osm_type").take(1).lowercase() + o.optLong("osm_id"),
                name = name,
                region = addr.toRegion(),
                lat = o.optString("lat").toDoubleOrNull(),
                lon = o.optString("lon").toDoubleOrNull(),
                source = SchoolSource.OSM,
            ).classified(o.optString("type"))
        }
    }

    /** Turns a tapped map point into Country / State / District. */
    suspend fun reverse(lat: Double, lon: Double): Region {
        val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&addressdetails=1&zoom=12&lat=$lat&lon=$lon"
        return JSONObject(nominatim(url)).optJSONObject("address")?.toRegion() ?: Region("", "", "", "")
    }

    // ---- internals ----

    private suspend fun nominatim(url: String): String = nominatimLock.withLock {
        val wait = 1100 - (System.currentTimeMillis() - lastNominatimCall)
        if (wait > 0) kotlinx.coroutines.delay(wait)
        try { http.get(url, ttlMs = 30 * Http.DAY) } finally { lastNominatimCall = System.currentTimeMillis() }
    }

    private suspend fun overpass(query: String, ttlMs: Long): JSONArray {
        var last: Exception? = null
        for (endpoint in OVERPASS_ENDPOINTS) {
            try {
                return JSONObject(http.postForm(endpoint, "data", query, ttlMs)).optJSONArray("elements") ?: JSONArray()
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IllegalStateException("Map service unavailable")
    }

    private fun stateArea(countryCode: String, state: String) = """
        area["ISO3166-1"="${countryCode.uppercase()}"]["admin_level"="2"]->.c;
        (rel(area.c)["boundary"="administrative"]["admin_level"="4"]["name"~"^${regex(state)}$",i];
         rel(area.c)["boundary"="administrative"]["admin_level"="4"]["name:en"~"^${regex(state)}$",i];)->.s;
        .s map_to_area->.sa;
    """.trimIndent()

    private fun parseSchools(elements: JSONArray, region: Region?): List<School> =
        (0 until elements.length()).mapNotNull { i ->
            val e = elements.getJSONObject(i)
            val tags = e.optJSONObject("tags") ?: return@mapNotNull null
            val name = tags.optString("name:en").ifBlank { tags.optString("name") }.ifBlank { return@mapNotNull null }
            val center = e.optJSONObject("center")
            School(
                id = "osm_" + e.optString("type").take(1) + e.optLong("id"),
                name = name,
                region = region ?: Region(
                    tags.optString("addr:country"), tags.optString("addr:country"),
                    tags.optString("addr:state"), tags.optString("addr:district").ifBlank { tags.optString("addr:city") },
                ),
                lat = center?.optDouble("lat") ?: e.optDouble("lat").takeIf { !it.isNaN() },
                lon = center?.optDouble("lon") ?: e.optDouble("lon").takeIf { !it.isNaN() },
                locality = tags.optString("addr:city").ifBlank { tags.optString("addr:place") },
            ).classified(tags.optString("amenity"))
        }.distinctBy { it.id }.sortedBy { it.name.lowercase() }

    private fun JSONObject.toRegion(): Region {
        val cc = optString("country_code").uppercase()
        val district = listOf("state_district", "county", "city", "town", "suburb").firstNotNullOfOrNull { k ->
            optString(k).takeIf { it.isNotBlank() }
        }.orEmpty().replace(Regex("\\s+district$", RegexOption.IGNORE_CASE), "")
        return Region(cc, optString("country"), optString("state"), district)
    }

    private fun School.classified(mapTag: String?): School {
        val kind = Classifier.kind(name, mapTag)
        return copy(kind = kind, collegeType = if (kind == InstitutionKind.COLLEGE) Classifier.collegeType(name, mapTag) else null)
    }

    /** Escapes text for an Overpass regex inside double quotes. */
    private fun regex(s: String) = buildString {
        for (ch in s) {
            if (ch in "\\^$.|?*+()[]{}") append("\\\\")
            if (ch == '"') append('\\')
            append(ch)
        }
    }

    companion object {
        private const val PHOTON = "https://photon.komoot.io/api/"
        private val PLACE_KINDS = setOf("city", "town", "village", "suburb", "hamlet", "neighbourhood", "quarter", "locality", "municipality")
        /** Photon is a text search, so we ask for the words Indian school names usually contain. */
        private val PHOTON_SCHOOL_WORDS = listOf("school", "higher secondary", "matriculation", "vidyalaya", "college", "polytechnic", "university", "institute")
        private val OVERPASS_ENDPOINTS = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
        )
    }
}
