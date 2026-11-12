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

/** Where a school sits: World → Country → State → District → Town. */
data class Region(
    val countryCode: String,
    val country: String,
    val state: String,
    val district: String,
    val town: String = "",
) {
    val label: String
        get() = listOf(town, district.takeIf { !it.equals(town, true) }.orEmpty(), state, country)
            .filter { it.isNotBlank() }.joinToString(", ")
}

/** A town / village / area the user picks to narrow the school search. */
data class Place(val name: String, val kind: String, val area: String, val lat: Double, val lon: Double)

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
    /** The school's own town/area from map data, e.g. "Kumbakonam". */
    val locality: String = "",
    /** Other names alumni know it by ("Vallalar HSS", "Vallalar school"). */
    val aliases: List<String> = emptyList(),
    /** Distance from the chosen town, for sorting and display. */
    val distanceKm: Double? = null,
    /** School or college (from the map tag and the name; alumni can correct it). */
    val kind: InstitutionKind = InstitutionKind.SCHOOL,
    /** For colleges: engineering / arts & science / medical …, decides which programmes appear. */
    val collegeType: CollegeType? = null,
    /** Raw map tag (school / college / university), kept to re-classify once the country is known. */
    val mapTag: String? = null,
) {
    /** Which school stages it can have, in its country's own terms (GCSE, Abitur, 12th grade…). */
    val schoolProfile: SchoolProfile get() = Classifier.schoolProfile(name, region.countryCode)
}

/** One reunion group = institution + what they studied + pass-out year + section. */
data class BatchKey(val schoolId: String, val track: Track, val year: Int, val section: String) {
    val groupId: String get() = "${schoolId}_${track.code}_${year}_${section.uppercase()}"
}

/** Colleges often have no sections; this value means "the whole batch". */
const val WHOLE_BATCH = "ALL"

data class Group(
    val id: String,
    val schoolId: String,
    val schoolName: String,
    val regionLabel: String,
    val track: Track,
    val year: Int,
    val section: String,
    val inviteCode: String,
    val memberCount: Int,
    val whatsappLink: String? = null,
    val createdBy: String = "",
) {
    val sectionLabel: String get() = if (section == WHOLE_BATCH) "Whole batch" else "Section $section"
    val title: String get() = "${track.short} · Class of $year · $sectionLabel"

    /** 5/10/15/20… years since passing out: the perfect excuse for a reunion. */
    fun milestone(currentYear: Int): Int? = (currentYear - year).takeIf { it >= 5 && it % 5 == 0 }
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
data class BatchStat(val track: Track, val year: Int, val section: String, val members: Int)
