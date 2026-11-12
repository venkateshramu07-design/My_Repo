package com.batchmates.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassifierTest {
    private fun kind(n: String, cc: String = "IN", tag: String? = null) = Classifier.kind(n, tag, cc)
    private fun type(n: String, cc: String = "IN") = Classifier.collegeType(n, null, cc)
    private fun stages(n: String, cc: String) = Classifier.schoolProfile(n, cc).tracks.map { it.short }

    @Test fun schoolOrCollege_aroundTheWorld() {
        assertEquals(InstitutionKind.SCHOOL, kind("C.P. Vidya Mandir Matriculation Higher Secondary School"))
        assertEquals(InstitutionKind.COLLEGE, kind("K. S. K. College of Engineering & Technology"))
        assertEquals(InstitutionKind.SCHOOL, kind("Vivekananda Pre-University College"))
        assertEquals(InstitutionKind.SCHOOL, kind("Eton College", "GB", "school"))          // a school, despite "College"
        assertEquals(InstitutionKind.COLLEGE, kind("Imperial College London", "GB", "university"))
        assertEquals(InstitutionKind.SCHOOL, kind("Colegio San Ignacio", "CL"))             // colegio = school
        assertEquals(InstitutionKind.SCHOOL, kind("Collège Jean Moulin", "FR"))             // collège = lower secondary
        assertEquals(InstitutionKind.SCHOOL, kind("Lycée Henri-IV", "FR"))
        assertEquals(InstitutionKind.SCHOOL, kind("Goethe-Gymnasium", "DE"))
        assertEquals(InstitutionKind.COLLEGE, kind("Technische Universität München", "DE"))
        assertEquals(InstitutionKind.COLLEGE, kind("Santa Monica Community College", "US"))
        assertEquals(InstitutionKind.COLLEGE, kind("Universidad Nacional Autónoma de México", "MX"))
        assertEquals(InstitutionKind.SCHOOL, kind("Lincoln High School", "US"))
        assertEquals(InstitutionKind.SCHOOL, kind("King's College Lagos", "NG", "school"))
        assertEquals(InstitutionKind.SCHOOL, kind("Royal College Colombo", "LK", "school"))
    }

    @Test fun collegeTypes_aroundTheWorld() {
        assertEquals(CollegeType.ENGINEERING, type("Arasu Engineering College"))
        assertEquals(CollegeType.EDUCATION, type("K.S.K. College of Education"))
        assertEquals(CollegeType.POLYTECHNIC, type("Arasu Polytechnic College"))
        assertEquals(CollegeType.ARTS_SCIENCE, type("Government Arts College (Autonomous), Kumbakonam"))
        assertEquals(CollegeType.ARTS_SCIENCE, type("St. Lawrence Arts College"))          // "Lawrence" is not "law"
        assertEquals(CollegeType.UNIVERSITY, type("SASTRA Deemed University"))
        assertEquals(CollegeType.UNIVERSITY, type("Massachusetts Institute of Technology", "US"))
        assertEquals(CollegeType.UNIVERSITY, type("Technische Universität München", "DE"))
        assertEquals(CollegeType.LAW, type("Harvard Law School", "US"))
        assertEquals(CollegeType.MEDICAL, type("Stanford University School of Medicine", "US"))
        assertEquals(CollegeType.BUSINESS, type("London Business School", "GB"))
        assertEquals(CollegeType.ART_DESIGN, type("Berklee College of Music", "US"))
        assertEquals(CollegeType.COMMUNITY, type("Santa Monica Community College", "US"))
        assertEquals(CollegeType.POLYTECHNIC, type("Ngee Ann Polytechnic", "SG"))
        assertEquals(CollegeType.DENTAL, type("Saveetha Dental College"))
        assertEquals(CollegeType.TRADITIONAL_MEDICINE, type("Government Siddha Medical College"))
    }

    @Test fun engineeringNeverShowsArtsOrMedicine() {
        val e = CollegeType.ENGINEERING
        assertFalse(e.allows(Field.ARTS)); assertFalse(e.allows(Field.HEALTH)); assertFalse(e.allows(Field.LAW))
        assertTrue(e.allows(Field.ENGINEERING)); assertTrue(e.allows(Field.ICT))
        assertFalse("no BBA at an engineering college", e.allows(Field.BUSINESS, Level.BACHELOR))
        assertTrue("MBA is fine", e.allows(Field.BUSINESS, Level.MASTER))
        assertEquals(listOf("Dentistry"), CollegeType.DENTAL.disciplines(Field.HEALTH))
    }

    @Test fun schoolStagesSpeakTheLocalLanguage() {
        assertEquals(listOf("12th", "10th"), stages("Banadurai Higher Secondary School", "IN"))
        assertEquals(listOf("5th"), stages("Tillaiyambur Government Primary School", "IN"))
        assertEquals(listOf("10th", "8th"), stages("Government High School, Swamimalai", "IN"))
        assertEquals(listOf("High school"), stages("Lincoln High School", "US"))
        assertEquals(listOf("Middle school"), stages("Roosevelt Middle School", "US"))
        assertEquals(listOf("Elementary"), stages("Maple Elementary School", "US"))
        assertEquals(listOf("A-Levels", "GCSE"), stages("Manchester Grammar School", "GB"))
        assertEquals(listOf("Abitur"), stages("Goethe-Gymnasium", "DE"))
        assertEquals(listOf("Bac"), stages("Lycée Henri-IV", "FR"))
        assertEquals(listOf("Brevet"), stages("Collège Jean Moulin", "FR"))
        assertEquals(listOf("Bachillerato"), stages("Preparatoria Federal Lázaro Cárdenas", "MX"))
        assertEquals(listOf("SSS 3", "JSS 3"), stages("Federal Government College Secondary School", "NG"))
        assertEquals(listOf("A-Level", "O-Level"), stages("Raffles Institution", "SG"))
        // Curriculum beats geography: an Indian school in Dubai runs 10th/12th.
        assertEquals(listOf("12th", "10th"), stages("Indian High School Dubai", "AE"))
        assertEquals("India", Classifier.schoolProfile("Indian High School Dubai", "AE").system.name)
        assertEquals("United Kingdom", Classifier.schoolProfile("The British School Al Khubairat", "AE").system.name)
    }

    @Test fun courseNamesCollapseToOneGroup() {
        val a = Track.college(Level.BACHELOR, Degrees.canonical("CSE"))
        val b = Track.college(Level.BACHELOR, Degrees.canonical("Computer Science and Engineering"))
        val c = Track.college(Level.BACHELOR, Degrees.canonical("computer science"))
        assertEquals(a.code, b.code); assertEquals(a.code, c.code)
        assertEquals("UG.computer-science", a.code)
        assertEquals("Bachelor's degree · Computer Science", a.label)
        assertEquals(Degrees.canonical("MBBS"), Degrees.canonical("medicine"))
        assertEquals("Law", Degrees.canonical("JD"))
    }

    @Test fun localDegreeNames() {
        assertEquals("B.E. / B.Tech", Degrees.localName("IN", Level.BACHELOR, Field.ENGINEERING))
        assertEquals("BS / BSE", Degrees.localName("US", Level.BACHELOR, Field.ENGINEERING))
        assertEquals("BEng · MEng", Degrees.localName("GB", Level.BACHELOR, Field.ENGINEERING))
        assertEquals("Licence", Degrees.localName("FR", Level.BACHELOR, Field.SCIENCE))
        assertEquals("JD", Degrees.localName("US", Level.DOCTORATE, Field.LAW))
    }
}
