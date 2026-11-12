package com.batchmates.app.live

import app.cash.paparazzi.Paparazzi
import com.batchmates.app.data.Region
import com.batchmates.app.data.geo.GeoRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Hits the real free services and prints timings. Skipped unless run with -Dlive=true. */
class LiveSchoolsCheck {
    @get:Rule val paparazzi = Paparazzi()

    private inline fun <T> timed(label: String, block: () -> T): T {
        val t0 = System.currentTimeMillis()
        val r = block()
        println("LIVE $label: ${System.currentTimeMillis() - t0} ms")
        return r
    }

    @Test fun kumbakonamSpeed() {
        assumeTrue(System.getProperty("live") == "true")
        // Fresh cache so we measure the network, like a first-time user.
        java.io.File("geo").deleteRecursively()
        val geo = GeoRepository(paparazzi.context)
        runBlocking {
            val r = Region("IN", "India", "Tamil Nadu", "Thanjavur")
            val center = timed("district centre (bundled)") { geo.districtCenter(r)!! }
            val towns = timed("towns of district (first time)") { geo.townsNear(center, "Tamil Nadu") }
            println("LIVE towns=${towns.take(12).map { it.name }}")
            val kb = towns.first { it.name == "Kumbakonam" }
            val region = r.copy(town = "Kumbakonam")
            val fast = timed("schools near Kumbakonam, fast (first time)") { geo.schoolsFast(kb.lat to kb.lon, 12.0, region, null) }
            println("LIVE fast=${fast.size} ${fast.take(5).map { it.name + " " + "%.1f".format(it.distanceKm) + "km" }}")
            timed("same again (cached)") { geo.schoolsFast(kb.lat to kb.lon, 12.0, region, null) }
            timed("towns again (cached)") { geo.townsNear(center, "Tamil Nadu") }
            val full = timed("complete list (background)") { runCatching { geo.schoolsFull(kb.lat to kb.lon, 12.0, region, null) } }
            println("LIVE full=${full.getOrNull()?.size} err=${full.exceptionOrNull()?.message} merged=${geo.merge(fast, full.getOrDefault(emptyList())).size}")
            val named = timed("name search 'banadurai'") { geo.schoolsFast(kb.lat to kb.lon, 12.0, region, "banadurai") }
            println("LIVE named=${named.map { it.name }}")
        }
    }
}
