package com.batchmates.app

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.batchmates.app.data.AppUser
import com.batchmates.app.data.BatchKey
import com.batchmates.app.data.Country
import com.batchmates.app.data.Track
import com.batchmates.app.data.InstitutionKind
import com.batchmates.app.data.Classifier
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
import com.batchmates.app.data.geo.GeoRepository
import com.batchmates.app.ui.finder.FinderScreen
import com.batchmates.app.ui.finder.FinderViewModel
import com.batchmates.app.ui.finder.Load
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.After
import org.junit.Before
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Test

/** Renders real screens on the JVM (no emulator) – run `./gradlew recordPaparazziDebug`. */
@OptIn(ExperimentalCoroutinesApi::class)
class ScreensTest {
    @Before fun mainDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun reset() = Dispatchers.resetMain()

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6, maxPercentDifference = 0.1)

    private val me = AppUser("demo-me", "Venkat R", "me@example.com")
    private val school = School(
        "osm_n1", "St. Joseph's Higher Secondary School",
        Region("IN", "India", "Tamil Nadu", "Tiruchirappalli"), 10.8, 78.7,
    )

    private fun seeded() = DemoReunionRepository().also { repo ->
        runBlocking {
            repo.joinBatch(school, BatchKey(school.id, Track.TWELFTH, 2006, "B"), me)
            repo.joinBatch(
                school.copy(id = "osm_n2", name = "Government Boys Hr. Sec. School"),
                BatchKey("osm_n2", Track.TENTH, 2004, "A"), me,
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
            BatchMatesTheme { GroupScreen(GroupViewModel(repo, me, "osm_n1_12_2006_B"), celebrate = false) {} }
        }
    }

    @Test fun districtsAreBundledForIndia() {
        val geo = GeoRepository(paparazzi.context)
        runBlocking {
            val tn = geo.districts("IN", "Tamil Nadu")
            assertEquals(38, tn.size)
            assertTrue("Tiruchirappalli" in tn)
            val india = geo.countries().first { it.code == "IN" }
            india.states.forEach { st -> assertTrue("no districts for $st", geo.districts("IN", st).isNotEmpty()) }
        }
    }

    @Test fun districtStep() {
        val vm = FinderViewModel(GeoRepository(paparazzi.context), DemoReunionRepository(), me, startOnMap = false)
        runBlocking {
            val india = GeoRepository(paparazzi.context).countries().first { it.code == "IN" }
            vm.pickCountry(india)
            vm.pickState("Tamil Nadu")
            repeat(100) { if (vm.state.value.districts is Load.Ready) return@repeat; Thread.sleep(50) }
        }
        assertTrue(vm.state.value.districts is Load.Ready)
        paparazzi.snapshot { BatchMatesTheme { FinderScreen(vm, {}, {}) } }
    }

    @Test fun duplicateSchoolIsCaught() {
        val vm = FinderViewModel(GeoRepository(paparazzi.context), DemoReunionRepository(), me, startOnMap = false)
        val kumbakonam = com.batchmates.app.data.Place("Kumbakonam", "city", "", 10.9604, 79.3821)
        runBlocking {
            val india = GeoRepository(paparazzi.context).countries().first { it.code == "IN" }
            vm.pickCountry(india); vm.pickState("Tamil Nadu"); vm.pickDistrict("Thanjavur")
            vm.pickTown(kumbakonam)
            vm.openCreate(name = "vallalar")
            vm.createSchool("vallalar", vm.state.value.region, InstitutionKind.SCHOOL, null)
            repeat(100) { if (vm.state.value.school != null) return@repeat; Thread.sleep(50) }
            assertEquals("vallalar", vm.state.value.school?.name)
            assertEquals("Kumbakonam", vm.state.value.school?.region?.town)
            // Second person types a longer name for the same school.
            vm.back()
            vm.openCreate(name = "Vallalar Higher Secondary School")
            assertTrue("duplicate not detected", vm.state.value.duplicates.any { it.name == "vallalar" })
            vm.createSchool("Vallalar Higher Secondary School", vm.state.value.region, InstitutionKind.SCHOOL, null)
            assertTrue("must ask before creating a duplicate", vm.state.value.confirmCreate)
        }
        paparazzi.snapshot { BatchMatesTheme { FinderScreen(vm, {}, {}) } }
    }

    @Test fun engineeringCollegeShowsOnlyEngineeringCourses() {
        val vm = FinderViewModel(GeoRepository(paparazzi.context), DemoReunionRepository(), me, startOnMap = false)
        val name = "K. S. K. College of Engineering & Technology"
        val college = School("osm_w9", name, Region("IN", "India", "Tamil Nadu", "Thanjavur", "Kumbakonam"),
            kind = Classifier.kind(name, null, "IN"), collegeType = Classifier.collegeType(name, null, "IN"))
        vm.pickSchool(college)
        val fields = vm.fieldsForCollege()
        assertTrue(com.batchmates.app.data.Field.ENGINEERING in fields)
        assertTrue("no arts/medicine at an engineering college",
            fields.none { it in setOf(com.batchmates.app.data.Field.ARTS, com.batchmates.app.data.Field.HEALTH, com.batchmates.app.data.Field.LAW) })
        paparazzi.snapshot { BatchMatesTheme { FinderScreen(vm, {}, {}) } }
    }

    @Test fun britishSchoolSpeaksGcse() {
        val vm = FinderViewModel(GeoRepository(paparazzi.context), DemoReunionRepository(), me, startOnMap = false)
        val name = "Manchester Grammar School"
        vm.pickSchool(School("osm_n77", name, Region("GB", "United Kingdom", "England", "Greater Manchester", "Manchester"),
            kind = Classifier.kind(name, "school", "GB")))
        paparazzi.snapshot { BatchMatesTheme { FinderScreen(vm, {}, {}) } }
    }

    @Test fun universityCourseStep() {
        val vm = FinderViewModel(GeoRepository(paparazzi.context), DemoReunionRepository(), me, startOnMap = false)
        val name = "University of Lagos"
        vm.pickSchool(School("osm_r5", name, Region("NG", "Nigeria", "Lagos", "Lagos", "Akoka"),
            kind = Classifier.kind(name, "university", "NG"), collegeType = Classifier.collegeType(name, "university", "NG")))
        vm.pickField(com.batchmates.app.data.Field.ENGINEERING)
        paparazzi.snapshot { BatchMatesTheme { FinderScreen(vm, {}, {}) } }
    }
}
