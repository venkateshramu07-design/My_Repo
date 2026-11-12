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
     * FIRST answer for a school list, in one request (~1 s): nearest schools around a town or district centre,
     * or a name search inside that area. Returns schools with region/distance filled in, nearest first.
     */
    suspend fun schoolsFast(center: Pair<Double, Double>?, radiusKm: Double, region: Region, nameFilter: String?): List<School> {
        val name = nameFilter?.trim()?.takeIf { it.length >= 2 }
        val raw = when {
            center == null && name == null -> emptyList()
            center == null -> photonSchools(name!!, stateFilter = region.state)
            name != null -> photonSchools(name, box = bbox(center, radiusKm / 111.0))
            else -> schoolsQuick(center.first, center.second, radiusKm)
        }
        return finish(raw, region, center, radiusKm)
    }

    /** FULL answer from Overpass (slow, sometimes busy). Run in the background and merge with [merge]. */
    suspend fun schoolsFull(center: Pair<Double, Double>?, radiusKm: Double, region: Region, nameFilter: String?): List<School> {
        center ?: return emptyList()
        return finish(schoolsComplete(center.first, center.second, radiusKm, nameFilter), region, center, radiusKm)
    }

    /** Merges two answers: same id, or same name within 400 m (a map node and a building outline), count once. */
    fun merge(first: List<School>, more: List<School>): List<School> {
        val out = first.toMutableList()
        for (s in more) {
            val dup = out.any { o ->
                o.id == s.id || (SchoolNames.similar(o.name, s.name) && o.lat != null && s.lat != null &&
                    SchoolNames.km(o.lat, o.lon!!, s.lat, s.lon!!) < 0.4)
            }
            if (!dup) out += s
        }
        return out.sortedWith(compareBy<School> { it.distanceKm ?: 999.0 }.thenBy { it.name.lowercase() })
    }

    private fun bbox(c: Pair<Double, Double>, d: Double) =
        "%.4f,%.4f,%.4f,%.4f".format(java.util.Locale.US, c.second - d, c.first - d, c.second + d, c.first + d)

    private fun finish(raw: List<School>, region: Region, center: Pair<Double, Double>?, radiusKm: Double): List<School> =
        merge(emptyList(), raw.map { s ->
            val d = if (center != null && s.lat != null && s.lon != null) SchoolNames.km(center.first, center.second, s.lat, s.lon) else null
            s.copy(region = region, distanceKm = d).reclassified()
        }.filter { (it.distanceKm ?: 0.0) <= radiusKm * 1.3 })

    private suspend fun photonSchools(q: String, box: String? = null, stateFilter: String? = null): List<School> {
        val url = buildString {
            append(PHOTON).append("?limit=50").append(SCHOOL_TAGS).append("&q=").append(URLEncoder.encode(q, "UTF-8"))
            if (box != null) append("&bbox=").append(box)
        }
        return parsePhotonSchools(http.get(url, ttlMs = 14 * Http.DAY, readTimeoutMs = 15_000), stateFilter)
    }

    private fun parsePhotonSchools(json: String, stateFilter: String? = null): List<School> {
        val features = JSONObject(json).optJSONArray("features") ?: JSONArray()
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

    // ------------------------------------------------------------------ fast paths (one request each)

    /**
     * Every mapped school/college around a point, nearest first, in ONE ~1 s request (Photon reverse,
     * capped at 50). This is what the user sees first; slower, more complete sources merge in afterwards.
     */
    suspend fun schoolsQuick(lat: Double, lon: Double, radiusKm: Double): List<School> {
        val url = PHOTON_REVERSE + "?lat=%.4f&lon=%.4f&radius=%d&limit=50".format(java.util.Locale.US, lat, lon, radiusKm.toInt().coerceIn(1, 50)) + SCHOOL_TAGS
        return parsePhotonSchools(http.get(url, ttlMs = 14 * Http.DAY, readTimeoutMs = 10_000))
    }

    /** The complete list from Overpass (slow, often busy). Callers run it in the background and merge. */
    suspend fun schoolsComplete(lat: Double, lon: Double, radiusKm: Double, nameFilter: String? = null): List<School> {
        val filter = nameFilter?.trim()?.takeIf { it.length >= 2 }?.let { "[\"name\"~\"${regex(it)}\",i]" }.orEmpty()
        val q = """
            [out:json][timeout:25];
            nwr["amenity"~"^(school|college|university)$"]["name"]$filter(around:${(radiusKm * 1000).toInt()},$lat,$lon);
            out center tags 1500;
        """.trimIndent()
        return parseSchools(overpass(q, ttlMs = 14 * Http.DAY), null)
    }

    /** Towns of a district in ONE ~0.7 s request: cities and towns around its centre, biggest first. */
    suspend fun townsNear(center: Pair<Double, Double>, state: String, radiusKm: Int = 45): List<Place> {
        fun url(tags: String) = PHOTON_REVERSE + "?lat=%.4f&lon=%.4f&radius=%d&limit=50".format(java.util.Locale.US, center.first, center.second, radiusKm) + tags
        var places = parsePlaces(http.get(url("&osm_tag=place:city&osm_tag=place:town&osm_tag=place:municipality"), ttlMs = 60 * Http.DAY, readTimeoutMs = 10_000), state)
        if (places.size < 6) { // rural districts: add villages
            places = places + parsePlaces(http.get(url("&osm_tag=place:village&osm_tag=place:suburb"), ttlMs = 60 * Http.DAY, readTimeoutMs = 10_000), state)
        }
        // Same town twice: two map points close together, or two spellings nearby (Orattanadu / Orathanadu).
        val kept = mutableListOf<Place>()
        for (p in places) {
            val dup = kept.any {
                val km = SchoolNames.km(it.lat, it.lon, p.lat, p.lon)
                km < 1.5 || (km < 8 && SchoolNames.similar(it.name, p.name))
            }
            if (!dup) kept += p
        }
        return kept.sortedWith(compareBy<Place> { if (it.kind == "city") 0 else 1 }
            .thenBy { SchoolNames.km(center.first, center.second, it.lat, it.lon) })
    }

    private fun parsePlaces(json: String, state: String): List<Place> {
        val features = JSONObject(json).optJSONArray("features") ?: JSONArray()
        return (0 until features.length()).mapNotNull { i ->
            val f = features.getJSONObject(i)
            val p = f.optJSONObject("properties") ?: return@mapNotNull null
            if (state.isNotBlank() && p.optString("state").isNotBlank() && !p.optString("state").equals(state, true)) return@mapNotNull null
            val c = f.optJSONObject("geometry")?.optJSONArray("coordinates") ?: return@mapNotNull null
            Place(
                name = p.optString("name").ifBlank { return@mapNotNull null },
                kind = p.optString("osm_value"),
                area = listOf(p.optString("county"), p.optString("district")).firstOrNull { it.isNotBlank() }.orEmpty(),
                lat = c.getDouble(1), lon = c.getDouble(0),
            )
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

    /** Map viewport, fast: one reverse request from the viewport centre. */
    suspend fun schoolsInBoxFast(south: Double, west: Double, north: Double, east: Double): List<School> {
        val cLat = (south + north) / 2
        val cLon = (west + east) / 2
        val radius = SchoolNames.km(cLat, cLon, north, east).coerceIn(1.0, 50.0)
        return schoolsQuick(cLat, cLon, radius)
    }

    /** Map viewport, complete (Overpass). */
    suspend fun schoolsInBoxFull(south: Double, west: Double, north: Double, east: Double): List<School> {
        val q = """
            [out:json][timeout:25];
            nwr["amenity"~"^(school|college|university)$"]["name"](${"%.4f".format(java.util.Locale.US, south)},${"%.4f".format(java.util.Locale.US, west)},${"%.4f".format(java.util.Locale.US, north)},${"%.4f".format(java.util.Locale.US, east)});
            out center tags 400;
        """.trimIndent()
        return parseSchools(overpass(q, ttlMs = 14 * Http.DAY), null)
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
        val cc = region.countryCode
        val kind = Classifier.kind(name, mapTag, cc)
        return copy(kind = kind, collegeType = if (kind == InstitutionKind.COLLEGE) Classifier.collegeType(name, mapTag, cc) else null, mapTag = mapTag)
    }

    /** Re-classify once the real country is known (list results get the user's chosen region). */
    private fun School.reclassified(): School = if (source == SchoolSource.CUSTOM) this else classified(mapTag)

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
        private const val PHOTON_REVERSE = "https://photon.komoot.io/reverse"
        private const val SCHOOL_TAGS = "&osm_tag=amenity:school&osm_tag=amenity:college&osm_tag=amenity:university"
        private val PLACE_KINDS = setOf("city", "town", "village", "suburb", "hamlet", "neighbourhood", "quarter", "locality", "municipality")
        private val OVERPASS_ENDPOINTS = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
        )
    }
}
