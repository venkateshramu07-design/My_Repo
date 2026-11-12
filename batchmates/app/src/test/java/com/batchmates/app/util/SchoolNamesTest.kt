package com.batchmates.app.util

import com.batchmates.app.data.Region
import com.batchmates.app.data.School
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolNamesTest {
    @Test fun sameSchoolWrittenDifferently() {
        assertTrue(SchoolNames.similar("vallalar", "Vallalar Higher Secondary School"))
        assertTrue(SchoolNames.similar("Vallalar HSS", "Vallalar Hr. Sec. School"))
        assertTrue(SchoolNames.similar("Valalar Matric School", "Vallalar Matriculation Higher Secondary School"))
        assertTrue(SchoolNames.similar("St. Joseph's", "St Josephs Matric Hr Sec School"))
        assertTrue(SchoolNames.similar("Town HSS", "Town Higher Secondary School"))
        assertTrue(SchoolNames.similar("Government Higher Secondary School", "Govt. Higher Secondary School"))
    }

    @Test fun differentSchoolsStayDifferent() {
        assertFalse(SchoolNames.similar("Government Boys HSS", "Government Girls HSS"))
        assertFalse(SchoolNames.similar("Vallalar School", "Little Flower School"))
        assertFalse(SchoolNames.similar("St. Joseph's", "St. Mary's"))
        assertFalse(SchoolNames.similar("Town Higher Secondary School", "Banadurai Higher Secondary School"))
        assertFalse(SchoolNames.similar("Sacred Heart Girls", "Sacred Heart Boys"))
    }

    @Test fun searchUsesWordsAndAliases() {
        val s = School("x", "Vallalar Higher Secondary School", Region("IN", "India", "Tamil Nadu", "Thanjavur"),
            aliases = listOf("VHSS"), locality = "Kumbakonam")
        assertTrue(SchoolNames.matches("vallalar hr", s))
        assertTrue(SchoolNames.matches("vhss", s))
        assertTrue(SchoolNames.matches("kumbakonam", s))
        assertFalse(SchoolNames.matches("joseph", s))
    }

    @Test fun samePinIsDuplicateWhateverTheName() {
        val s = School("x", "Kalaimagal Vidyalaya", Region("IN", "India", "TN", "Thanjavur"), lat = 10.9604, lon = 79.3820)
        assertTrue(SchoolNames.duplicates("Some other name", 10.9610, 79.3825, listOf(s)).isNotEmpty())
    }
}
