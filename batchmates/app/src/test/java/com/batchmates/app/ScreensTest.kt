package com.batchmates.app

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.batchmates.app.data.AppUser
import com.batchmates.app.data.BatchKey
import com.batchmates.app.data.Country
import com.batchmates.app.data.Grade
import com.batchmates.app.data.Region
import com.batchmates.app.data.School
import com.batchmates.app.data.demo.DemoAuthRepository
import com.batchmates.app.data.demo.DemoReunionRepository
import com.batchmates.app.ui.theme.BatchMatesTheme
import com.batchmates.app.ui.welcome.AuthViewModel
import com.batchmates.app.ui.welcome.WelcomeScreen
import com.batchmates.app.ui.home.HomeScreen
import com.batchmates.app.ui.home.HomeViewModel
import com.batchmates.app.ui.group.GroupScreen
import com.batchmates.app.ui.group.GroupViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

/** Renders real screens on the JVM (no emulator) – run `./gradlew recordPaparazziDebug`. */
class ScreensTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6, maxPercentDifference = 0.1)

    private val me = AppUser("demo-me", "Venkat R", "me@example.com")
    private val school = School(
        "osm_n1", "St. Joseph's Higher Secondary School",
        Region("IN", "India", "Tamil Nadu", "Tiruchirappalli"), 10.8, 78.7,
    )

    private fun seeded() = DemoReunionRepository().also { repo ->
        runBlocking {
            repo.joinBatch(school, BatchKey(school.id, Grade.TWELFTH, 2008, "B"), me)
            repo.joinBatch(
                school.copy(id = "osm_n2", name = "Government Boys Hr. Sec. School"),
                BatchKey("osm_n2", Grade.TENTH, 2006, "A"), me,
            )
        }
    }

    @Test fun welcome() = paparazzi.snapshot {
        BatchMatesTheme { WelcomeScreen(AuthViewModel(DemoAuthRepository()), isDemo = false) }
    }

    @Test fun home() {
        val repo = seeded()
        paparazzi.snapshot {
            BatchMatesTheme {
                HomeScreen(HomeViewModel(repo, me), me, false, null, {}, {}, {}, { _, _ -> }, {})
            }
        }
    }

    @Test fun group() {
        val repo = seeded()
        paparazzi.snapshot {
            BatchMatesTheme { GroupScreen(GroupViewModel(repo, me, "osm_n1_12_2008_B"), celebrate = false) {} }
        }
    }
}
