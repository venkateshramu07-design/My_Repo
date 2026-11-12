package com.batchmates.app.data

/** Signed-in person. */
data class AppUser(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
)

/** A country from the bundled region catalogue. */
data class Country(val code: String, val name: String, val flag: String, val states: List<String>)

/** Where a school sits: World → Country → State → District. */
data class Region(
    val countryCode: String,
    val country: String,
    val state: String,
    val district: String,
) {
    val label: String get() = listOf(district, state, country).filter { it.isNotBlank() }.joinToString(", ")
}

enum class SchoolSource { OSM, CUSTOM }

data class School(
    /** Stable id: "osm_n123" for OpenStreetMap places, random for groups created by users. */
    val id: String,
    val name: String,
    val region: Region,
    val lat: Double? = null,
    val lon: Double? = null,
    val source: SchoolSource = SchoolSource.OSM,
    val memberCount: Int = 0,
)

/** 10th or 12th standard pass-outs (more can be enabled later). */
enum class Grade(val value: Int, val label: String) {
    TENTH(10, "10th Grade"),
    TWELFTH(12, "12th Grade");

    companion object {
        fun of(value: Int) = entries.firstOrNull { it.value == value } ?: TENTH
    }
}

/** One reunion group = school + grade + pass-out year + section. */
data class BatchKey(val schoolId: String, val grade: Grade, val year: Int, val section: String) {
    val groupId: String get() = "${schoolId}_${grade.value}_${year}_${section.uppercase()}"
}

data class Group(
    val id: String,
    val schoolId: String,
    val schoolName: String,
    val regionLabel: String,
    val grade: Grade,
    val year: Int,
    val section: String,
    val inviteCode: String,
    val memberCount: Int,
    val whatsappLink: String? = null,
    val createdBy: String = "",
) {
    val title: String get() = "${grade.label} · Class of $year · Section $section"
}

data class Member(val uid: String, val name: String, val photoUrl: String?, val joinedAt: Long)

data class Message(
    val id: String,
    val uid: String,
    val name: String,
    val photoUrl: String?,
    val text: String,
    val sentAt: Long,
)

/** How many classmates already joined a (grade, year, section) at a school. Used to lure people in. */
data class BatchStat(val grade: Grade, val year: Int, val section: String, val members: Int)
