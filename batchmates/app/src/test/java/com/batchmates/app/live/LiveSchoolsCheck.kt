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
        }
    }
}
