package com.batchmates.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

/** Real names from the Thanjavur / Kumbakonam results. */
class ClassifierTest {
    private fun kind(n: String, tag: String? = null) = Classifier.kind(n, tag)
    private fun type(n: String) = Classifier.collegeType(n)

    @Test fun schoolsVsColleges() {
        assertEquals(InstitutionKind.SCHOOL, kind("C.P. Vidya Mandir Matriculation Higher Secondary School"))
        assertEquals(InstitutionKind.SCHOOL, kind("Tillaiyambur Government Primary School"))
        assertEquals(InstitutionKind.SCHOOL, kind("National Vidyalaya CBSE School"))
        assertEquals(InstitutionKind.COLLEGE, kind("K. S. K. College of Engineering & Technology"))
        assertEquals(InstitutionKind.COLLEGE, kind("Arasu Polytechnic College"))
        assertEquals(InstitutionKind.COLLEGE, kind("Annai Vailankanni Arts ans Science College"))
        assertEquals(InstitutionKind.SCHOOL, kind("Vivekananda Pre-University College"))
        assertEquals(InstitutionKind.COLLEGE, kind("Some Place", "university"))
    }

    @Test fun collegeTypes() {
        assertEquals(CollegeType.ENGINEERING, type("Arasu Engineering College"))
        assertEquals(CollegeType.ENGINEERING, type("K. S. K. College of Engineering & Technology"))
        assertEquals(CollegeType.EDUCATION, type("K.S.K. College of Education"))
        assertEquals(CollegeType.POLYTECHNIC, type("Arasu Polytechnic College"))
        assertEquals(CollegeType.ARTS_SCIENCE, type("Annai Vailankanni Arts ans Science College"))
        assertEquals(CollegeType.MEDICAL, type("Thanjavur Medical College"))
        assertEquals(CollegeType.NURSING, type("Sri Venkateswara College of Nursing"))
        assertEquals(CollegeType.UNIVERSITY, type("SASTRA Deemed University"))
        assertEquals(CollegeType.ARTS_SCIENCE, type("Government Arts College (Autonomous), Kumbakonam"))
        assertEquals(null, type("ABI & ABI College"))
        assertEquals(CollegeType.ARTS_SCIENCE, type("St. Lawrence Arts College")) // not a law college
    }

    @Test fun schoolStandards() {
        assertEquals(SchoolType.PRIMARY, Classifier.schoolType("Tillaiyambur Government Primary School"))
        assertEquals(SchoolType.HIGHER_SECONDARY, Classifier.schoolType("Banadurai Higher Secondary School"))
        assertEquals(SchoolType.HIGHER_SECONDARY, Classifier.schoolType("Al Mubeen Matric Hr Sec School"))
        assertEquals(SchoolType.MIDDLE, Classifier.schoolType("Panchayat Union Middle School"))
        assertEquals(SchoolType.HIGH, Classifier.schoolType("Government High School, Swamimalai"))
        assertEquals(SchoolType.JUNIOR_COLLEGE, Classifier.schoolType("Vivekananda Pre-University College"))
    }

    @Test fun courseNamesCollapseToOneGroup() {
        val be = Programmes.byCode("BE")!!
        val a = Track.of(be, "CSE")
        val b = Track.of(be, "Computer Science and Engineering")
        val c = Track.of(be, "computer science")
        assertEquals(a.code, b.code); assertEquals(a.code, c.code)
        assertEquals("UG.BE.computer-science", a.code)
        assertEquals("B.E. / B.Tech · Computer Science", a.label)
        assertEquals(Track.of(be, "ECE").code, Track.of(be, "Electronics & Communication").code)
    }
}
