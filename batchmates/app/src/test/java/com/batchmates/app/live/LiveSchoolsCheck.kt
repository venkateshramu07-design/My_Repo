package com.batchmates.app.live

import app.cash.paparazzi.Paparazzi
import com.batchmates.app.data.Region
import com.batchmates.app.data.geo.GeoRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Hits the real free services. Skipped unless run with -Dlive=true. */
class LiveSchoolsCheck {
    @get:Rule val paparazzi = Paparazzi()

    @Test fun thanjavur() {
        assumeTrue(System.getProperty("live") == "true")
        val geo = GeoRepository(paparazzi.context)
        runBlocking {
            val r = Region("IN", "India", "Tamil Nadu", "Thanjavur")
            val t0 = System.currentTimeMillis()
            val all = geo.schoolsIn(r)
            println("LIVE all=${all.size} in ${System.currentTimeMillis() - t0}ms sample=${all.take(8).map { it.name }}")
            val named = geo.schoolsIn(r, "stella")
            println("LIVE named=${named.map { it.name }}")
            val places = geo.searchPlaces("kumb", geo.districtCenter(r), "Tamil Nadu")
            println("LIVE places=${places.map { it.name + "/" + it.kind }}")
            val kb = places.first { it.name == "Kumbakonam" }
            val near = geo.schoolsNear(kb, r.copy(town = "Kumbakonam"))
            println("LIVE kinds=" + near.groupBy { it.kind }.mapValues { (_, v) -> v.take(4).map { it.name + " [" + (it.collegeType?.label ?: it.schoolProfile.label) + "]" } })
            println("LIVE near=${near.size} first=${near.take(6).map { it.name + " @" + it.locality + " " + "%.1f".format(it.distanceKm) }}")
        }
    }
}
