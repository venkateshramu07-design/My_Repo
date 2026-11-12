package com.batchmates.app.data.geo

import com.batchmates.app.BuildConfig
import com.batchmates.app.data.Place
import com.batchmates.app.data.Region
import com.batchmates.app.data.School
import com.batchmates.app.data.SchoolSource
import com.batchmates.app.util.SchoolNames
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.floor

/**
 * BatchMates' own school & college directory: static files built from Overture Maps
 * (tools/build_directory.py) and served by a free CDN. One small file per ~55 km map cell, so finding the
 * schools around a town is a ~100-300 ms download the first time and instant after that.
 * The free public geocoders are only used as a background top-up, never on the critical path.
 */
class SchoolDirectory(private val http: Http) {

    /** Countries the directory covers. Elsewhere the app falls back to the public map services. */
    fun covers(countryCode: String) = countryCode.uppercase() in COUNTRIES

    private val mirrors: List<String> = BuildConfig.DATA_MIRRORS.split(',').map { it.trim().trimEnd('/') }.filter { it.isNotEmpty() }

    private fun urls(path: String) = mirrors.map { "$it/$path" }

    private val townLock = Mutex()
    private val towns = mutableMapOf<String, List<DirTown>>()

    /** A town from the index, with its official state and district (empty when unknown). */
    data class DirTown(
        val name: String, val lat: Double, val lon: Double, val schools: Int,
        val state: String = "", val district: String = "", val population: Int = 0,
    ) {
        /** How likely this is the place someone means: schools found plus city size. */
        val score: Double get() = schools + population / 20_000.0
    }

    /** Every school/college within [radiusKm] of a point, nearest first. */
    suspend fun schoolsNear(countryCode: String, lat: Double, lon: Double, radiusKm: Double, region: Region): List<School> = coroutineScope {
        val dLat = radiusKm / 111.0
        val dLon = radiusKm / (111.0 * cos(Math.toRadians(lat)).coerceAtLeast(0.2))
        val cells = buildSet {
            var a = floor((lat - dLat) / CELL).toInt()
            while (a <= floor((lat + dLat) / CELL).toInt()) {
                var b = floor((lon - dLon) / CELL).toInt()
                while (b <= floor((lon + dLon) / CELL).toInt()) { add(a to b); b++ }
                a++
            }
        }
        cells.map { (a, b) -> async { tile(countryCode, a, b) } }.awaitAll().flatten()
            .map { s ->
                val d = SchoolNames.km(lat, lon, s.lat!!, s.lon!!)
                s.copy(region = region.copy(town = s.locality.ifBlank { region.town }), distanceKm = d)
            }
            .filter { it.distanceKm!! <= radiusKm }
            .sortedBy { it.distanceKm }
    }

    private suspend fun tile(cc: String, a: Int, b: Int): List<School> {
        val json = http.getGzipJson(urls("schools/${cc.uppercase()}/${a}_$b.json.gz"), ttlMs = 30 * Http.DAY)
        if (json.isBlank()) return emptyList()
        val arr = JSONObject(json).getJSONArray("s")
        return List(arr.length()) { i ->
            val r = arr.getJSONArray(i)
            School(
                id = r.getString(0), name = r.getString(1),
                region = Region(cc.uppercase(), "", "", "", r.optString(5)),
                lat = r.getDouble(2), lon = r.getDouble(3), source = SchoolSource.OSM,
                locality = r.optString(5),
                mapTag = when (r.optString(4)) { "c" -> "college"; "v" -> "college"; else -> "school" },
            )
        }
    }

    /** Towns that have schools, biggest first (one ~100 KB file per country, cached for a month). */
    suspend fun towns(countryCode: String): List<DirTown> = townLock.withLock {
        val cc = countryCode.uppercase()
        towns[cc]?.let { return it }
        val json = http.getGzipJson(urls("towns/$cc.json.gz"), ttlMs = 30 * Http.DAY)
        val list = if (json.isBlank()) emptyList() else {
            val arr = JSONObject(json).getJSONArray("t")
            List(arr.length()) { i ->
                arr.getJSONArray(i).let { DirTown(it.getString(0), it.getDouble(1), it.getDouble(2), it.getInt(3), it.optString(4), it.optString(5), it.optInt(6)) }
            }.sortedByDescending { it.score }
        }
        towns[cc] = list
        list
    }

    /** Instant, offline town search: prefix matches first, then contains, biggest towns first. */
    suspend fun searchTowns(countryCode: String, text: String, limit: Int = 12): List<DirTown> {
        val q = text.trim().lowercase()
        if (q.length < 2) return emptyList()
        val all = towns(countryCode)
        val starts = all.filter { it.name.lowercase().startsWith(q) }
        val words = all.filter { t -> t !in starts && t.name.lowercase().split(' ', '-').any { it.startsWith(q) } }
        val fuzzy = if (starts.size + words.size >= limit || q.length < 5) emptyList()
        else all.filter { t -> t !in starts && t !in words && SchoolNames.similar(t.name, text) }
        return (starts + words + fuzzy).take(limit)
    }

    /** Towns around a point (a district's towns), biggest first. */
    suspend fun townsNear(countryCode: String, lat: Double, lon: Double, radiusKm: Double): List<DirTown> =
        towns(countryCode).filter { SchoolNames.km(lat, lon, it.lat, it.lon) <= radiusKm }.sortedByDescending { it.score }

    companion object {
        const val CELL = 0.5
        val COUNTRIES = setOf("IN")
    }
}
