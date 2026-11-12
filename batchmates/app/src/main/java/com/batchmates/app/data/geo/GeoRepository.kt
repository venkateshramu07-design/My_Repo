package com.batchmates.app.data.geo

import android.content.Context
import com.batchmates.app.data.Country
import com.batchmates.app.data.Region
import com.batchmates.app.data.School
import com.batchmates.app.data.SchoolSource
import kotlinx.coroutines.Dispatchers
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

    /** Districts (admin level 5, else 6) inside a state, from OpenStreetMap. */
    suspend fun districts(countryCode: String, state: String): List<String> {
        val q = """
            [out:json][timeout:60];
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
            .map { it.removeSuffix(" District").removeSuffix(" district") }
            .distinct().sorted()
    }

    /** Schools inside a district (or a whole state when a name filter is given). */
    suspend fun schoolsIn(region: Region, nameFilter: String? = null): List<School> {
        val filter = nameFilter?.trim()?.takeIf { it.length >= 2 }?.let { "[\"name\"~\"${regex(it)}\",i]" }.orEmpty()
        val area = if (region.district.isNotBlank()) {
            """
            ${stateArea(region.countryCode, region.state)}
            (rel(area.sa)["boundary"="administrative"]["name"~"^${regex(region.district)}( District)?$",i];
             rel(area.sa)["boundary"="administrative"]["name:en"~"^${regex(region.district)}( District)?$",i];)->.d;
            .d map_to_area->.a;
            """
        } else {
            if (filter.isEmpty()) return emptyList() // a whole state is too big without a name
            stateArea(region.countryCode, region.state) + "\n.sa->.a;"
        }
        val q = """
            [out:json][timeout:60];
            $area
            nwr(area.a)["amenity"~"^(school|college)$"]["name"]$filter;
            out center tags 1500;
        """.trimIndent()
        return parseSchools(overpass(q, ttlMs = 14 * Http.DAY), region)
    }

    /** Schools visible in a map viewport. */
    suspend fun schoolsInBox(south: Double, west: Double, north: Double, east: Double): List<School> {
        val q = """
            [out:json][timeout:40];
            nwr["amenity"~"^(school|college)$"]["name"](${"%.4f".format(south)},${"%.4f".format(west)},${"%.4f".format(north)},${"%.4f".format(east)});
            out center tags 400;
        """.trimIndent()
        return parseSchools(overpass(q, ttlMs = 14 * Http.DAY), null)
    }

    /** Free-text search, e.g. "St. Joseph's Higher Secondary School Trichy". */
    suspend fun searchSchools(query: String, countryCode: String?): List<School> {
        val url = buildString {
            append("https://nominatim.openstreetmap.org/search?format=jsonv2&addressdetails=1&limit=20&q=")
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
                id = "osm_" + o.optString("osm_type").take(1) + o.optLong("osm_id"),
                name = name,
                region = addr.toRegion(),
                lat = o.optString("lat").toDoubleOrNull(),
                lon = o.optString("lon").toDoubleOrNull(),
                source = SchoolSource.OSM,
            )
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
            )
        }.distinctBy { it.id }.sortedBy { it.name.lowercase() }

    private fun JSONObject.toRegion(): Region {
        val cc = optString("country_code").uppercase()
        val district = listOf("state_district", "county", "city", "town", "suburb").firstNotNullOfOrNull { k ->
            optString(k).takeIf { it.isNotBlank() }
        }.orEmpty().removeSuffix(" District").removeSuffix(" district")
        return Region(cc, optString("country"), optString("state"), district)
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
        private val OVERPASS_ENDPOINTS = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
            "https://maps.mail.ru/osm/tools/overpass/api/interpreter",
        )
    }
}
