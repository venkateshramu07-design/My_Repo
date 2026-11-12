package com.batchmates.app.data

/**
 * What kind of place an institution is, and therefore which batches it can have.
 * An engineering college must never offer B.A. or MBBS; a primary school must never offer 12th.
 */
enum class InstitutionKind { SCHOOL, COLLEGE }

/** Degree levels, in the order people think of them. */
enum class Level(val code: String, val label: String, val emoji: String) {
    DIPLOMA("DIP", "Diploma / ITI", "🛠️"),
    UG("UG", "Bachelor's", "🎓"),
    PG("PG", "Master's", "📜"),
    PHD("PHD", "PhD", "🔬"),
}

data class Programme(
    val level: Level,
    val code: String,
    val name: String,
    val specialisations: List<String> = emptyList(),
)

/**
 * One "track" = what a batch studied. Schools: "10" / "12" / "8" / "5".
 * Colleges: "UG.BE.computer-science". The code is part of the group id, so it must be canonical.
 */
data class Track(val code: String, val label: String, val short: String) {
    val isCollege: Boolean get() = '.' in code

    companion object {
        val FIFTH = Track("5", "5th Standard · Primary", "5th")
        val EIGHTH = Track("8", "8th Standard · Middle school", "8th")
        val TENTH = Track("10", "10th Standard · SSLC", "10th")
        val TWELFTH = Track("12", "12th Standard · HSC / Plus Two", "12th")
        val schoolTracks = listOf(FIFTH, EIGHTH, TENTH, TWELFTH)

        fun school(code: String) = schoolTracks.firstOrNull { it.code == code } ?: TENTH

        fun of(programme: Programme, specialisation: String?): Track {
            val spec = specialisation?.let { Programmes.canonicalSpecialisation(programme, it) }?.takeIf { it.isNotBlank() }
            val code = listOfNotNull(programme.level.code, programme.code, spec?.let(::slug)).joinToString(".")
            val label = listOfNotNull(programme.name, spec).joinToString(" · ")
            return Track(code, label, programme.name.substringBefore(" /").substringBefore(" (") + (spec?.let { " $it" } ?: ""))
        }

        fun slug(s: String) = s.lowercase().replace("&", " and ")
            .split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotBlank() }.joinToString("-")
    }
}

/** School profile → which standards it can have. */
enum class SchoolType(val label: String, val tracks: List<Track>) {
    PRIMARY("Primary school", listOf(Track.FIFTH)),
    MIDDLE("Middle school", listOf(Track.EIGHTH, Track.FIFTH)),
    HIGH("High school", listOf(Track.TENTH, Track.EIGHTH)),
    HIGHER_SECONDARY("Higher secondary school", listOf(Track.TENTH, Track.TWELFTH)),
    JUNIOR_COLLEGE("Junior / Pre-University college", listOf(Track.TWELFTH)),
}

/** College profile → which programmes it can have. */
enum class CollegeType(val label: String, val emoji: String, val programmes: List<String>) {
    ENGINEERING("Engineering college", "⚙️", listOf("BE", "BARCH", "ME", "MBA", "MCA", "PHD")),
    POLYTECHNIC("Polytechnic", "🛠️", listOf("DIPENG")),
    ITI("ITI", "🔧", listOf("ITI")),
    MEDICAL("Medical college", "🩺", listOf("MBBS", "MD", "BSCNURSING", "PHD")),
    DENTAL("Dental college", "🦷", listOf("BDS", "MDS")),
    NURSING("Nursing college", "💉", listOf("GNM", "BSCNURSING", "MSCNURSING")),
    PHARMACY("Pharmacy college", "💊", listOf("DPHARM", "BPHARM", "PHARMD", "MPHARM")),
    ARTS_SCIENCE("Arts & Science college", "📚", listOf("BA", "BSC", "BCOM", "BBA", "BCA", "BSW", "MA", "MSC", "MCOM", "MBA", "MCA", "MSW", "MPHIL", "PHD")),
    EDUCATION("College of Education", "🍎", listOf("DELED", "BED", "MED")),
    LAW("Law college", "⚖️", listOf("LLB", "LLM")),
    AGRICULTURE("Agricultural college", "🌾", listOf("BSCAGRI", "BTECHAGRI", "MSCAGRI", "PHD")),
    VETERINARY("Veterinary college", "🐄", listOf("BVSC", "MVSC")),
    ARCHITECTURE("Architecture college", "📐", listOf("BARCH", "MARCH")),
    MANAGEMENT("Business school", "💼", listOf("BBA", "MBA", "PGDM")),
    HOTEL("Hotel management", "🍽️", listOf("BHM")),
    AYUSH("Siddha / Ayurveda / Homoeopathy", "🌿", listOf("AYUSH")),
    DESIGN("Design / Fine arts", "🎨", listOf("BDES", "BFA", "MDES", "MFA")),
    UNIVERSITY("University (all departments)", "🏛️", emptyList()), // empty = everything
}

object Programmes {
    private val engg = listOf(
        "Computer Science", "Information Technology", "Electronics & Communication", "Electrical & Electronics",
        "Mechanical", "Civil", "Chemical", "Aeronautical", "Automobile", "Biotechnology", "Biomedical",
        "Instrumentation & Control", "Production", "Mechatronics", "AI & Data Science", "Textile", "Marine",
    )
    private val science = listOf(
        "Physics", "Chemistry", "Mathematics", "Computer Science", "Botany", "Zoology", "Biotechnology",
        "Microbiology", "Biochemistry", "Statistics", "Electronics", "Psychology", "Visual Communication",
        "Nutrition & Dietetics", "Information Technology", "Geology", "Home Science",
    )
    private val arts = listOf(
        "English", "Tamil", "Hindi", "Telugu", "Malayalam", "Kannada", "Sanskrit", "History", "Economics",
        "Political Science", "Sociology", "Public Administration", "Geography", "Philosophy", "Journalism",
    )
    private val commerce = listOf("General", "Computer Applications", "Corporate Secretaryship", "Accounting & Finance", "Bank Management", "Professional Accounting")
    private val mba = listOf("General", "Finance", "Marketing", "Human Resources", "Systems", "Operations", "Business Analytics")
    private val medicine = listOf("General Medicine", "General Surgery", "Paediatrics", "Obstetrics & Gynaecology", "Orthopaedics", "Anaesthesia", "Radiology", "Dermatology", "Psychiatry", "Ophthalmology", "ENT", "Community Medicine")
    private val polytechnic = listOf("Mechanical", "Civil", "Electrical & Electronics", "Electronics & Communication", "Computer", "Automobile", "Instrumentation", "Chemical", "Textile", "Printing")
    private val iti = listOf("Electrician", "Fitter", "Welder", "Turner", "Machinist", "Mechanic Motor Vehicle", "Electronics Mechanic", "COPA", "Plumber", "Draughtsman Civil", "Refrigeration & AC")

    val all: List<Programme> = listOf(
        Programme(Level.DIPLOMA, "DIPENG", "Diploma in Engineering", polytechnic),
        Programme(Level.DIPLOMA, "ITI", "ITI", iti),
        Programme(Level.DIPLOMA, "GNM", "Diploma in Nursing (GNM)"),
        Programme(Level.DIPLOMA, "DPHARM", "D.Pharm"),
        Programme(Level.DIPLOMA, "DELED", "D.El.Ed / D.T.Ed"),
        Programme(Level.DIPLOMA, "PGDM", "PGDM", mba),
        Programme(Level.UG, "BE", "B.E. / B.Tech", engg),
        Programme(Level.UG, "BARCH", "B.Arch"),
        Programme(Level.UG, "BSC", "B.Sc", science),
        Programme(Level.UG, "BA", "B.A.", arts),
        Programme(Level.UG, "BCOM", "B.Com", commerce),
        Programme(Level.UG, "BBA", "BBA"),
        Programme(Level.UG, "BCA", "BCA"),
        Programme(Level.UG, "BSW", "BSW"),
        Programme(Level.UG, "MBBS", "MBBS"),
        Programme(Level.UG, "BDS", "BDS"),
        Programme(Level.UG, "BSCNURSING", "B.Sc Nursing"),
        Programme(Level.UG, "BPHARM", "B.Pharm"),
        Programme(Level.UG, "PHARMD", "Pharm.D"),
        Programme(Level.UG, "BED", "B.Ed"),
        Programme(Level.UG, "LLB", "LLB / B.A. LLB"),
        Programme(Level.UG, "BSCAGRI", "B.Sc Agriculture"),
        Programme(Level.UG, "BTECHAGRI", "B.Tech Agri / Food Tech"),
        Programme(Level.UG, "BVSC", "B.V.Sc"),
        Programme(Level.UG, "BHM", "Hotel Management (BHM)"),
        Programme(Level.UG, "AYUSH", "BSMS / BAMS / BHMS / BUMS"),
        Programme(Level.UG, "BDES", "B.Des"),
        Programme(Level.UG, "BFA", "BFA"),
        Programme(Level.PG, "ME", "M.E. / M.Tech", engg),
        Programme(Level.PG, "MSC", "M.Sc", science),
        Programme(Level.PG, "MA", "M.A.", arts),
        Programme(Level.PG, "MCOM", "M.Com"),
        Programme(Level.PG, "MBA", "MBA", mba),
        Programme(Level.PG, "MCA", "MCA"),
        Programme(Level.PG, "MSW", "MSW"),
        Programme(Level.PG, "MD", "MD / MS", medicine),
        Programme(Level.PG, "MDS", "MDS"),
        Programme(Level.PG, "MSCNURSING", "M.Sc Nursing"),
        Programme(Level.PG, "MPHARM", "M.Pharm"),
        Programme(Level.PG, "MED", "M.Ed"),
        Programme(Level.PG, "LLM", "LLM"),
        Programme(Level.PG, "MSCAGRI", "M.Sc Agriculture"),
        Programme(Level.PG, "MVSC", "M.V.Sc"),
        Programme(Level.PG, "MARCH", "M.Arch"),
        Programme(Level.PG, "MDES", "M.Des"),
        Programme(Level.PG, "MFA", "MFA"),
        Programme(Level.PG, "MPHIL", "M.Phil"),
        Programme(Level.PHD, "PHD", "PhD"),
    )

    fun byCode(code: String) = all.firstOrNull { it.code == code }

    /** Programmes offered by a college of this type (all of them for universities / unknown). */
    fun forType(type: CollegeType?): List<Programme> =
        if (type == null || type.programmes.isEmpty()) all else type.programmes.mapNotNull(::byCode)

    /** Common short forms people type, mapped to the catalogue spelling, so groups don't split. */
    private val abbreviations = mapOf(
        "cse" to "Computer Science", "cs" to "Computer Science", "computer science and engineering" to "Computer Science",
        "it" to "Information Technology", "ece" to "Electronics & Communication", "eee" to "Electrical & Electronics",
        "mech" to "Mechanical", "mechanical engineering" to "Mechanical", "civil engineering" to "Civil",
        "eie" to "Instrumentation & Control", "ice" to "Instrumentation & Control", "aids" to "AI & Data Science",
        "ai and ds" to "AI & Data Science", "bio tech" to "Biotechnology", "maths" to "Mathematics", "math" to "Mathematics",
        "viscom" to "Visual Communication", "hr" to "Human Resources", "ca" to "Computer Applications",
        "obg" to "Obstetrics & Gynaecology", "ortho" to "Orthopaedics",
    )

    /** "cse", "Computer science & engg" → "Computer Science"; unknown text is kept as typed (tidied). */
    fun canonicalSpecialisation(p: Programme, text: String): String {
        fun key(t: String) = t.lowercase().replace("&", " and ").replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()
        val raw = key(text)
        val stripped = raw.removeSuffix(" engineering").removeSuffix(" engg").trim().removeSuffix(" and").trim()
        for (k in listOf(raw, stripped)) {
            abbreviations[k]?.let { return it }
            p.specialisations.firstOrNull { key(it) == k }?.let { return it }
        }
        return text.trim().split(Regex("\\s+")).joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
    }
}

/** Reads an institution's name (and the map's tag) to decide what it is. Users can correct it. */
object Classifier {
    private fun has(name: String, vararg words: String) = words.any { Regex("\\b$it", RegexOption.IGNORE_CASE).containsMatchIn(name) }

    fun kind(name: String, mapTag: String? = null): InstitutionKind = when {
        has(name, "junior college", "pre-university", "pre university", "p\\.?u\\.? college") -> InstitutionKind.SCHOOL
        has(name, "college", "university", "polytechnic", "institute of technology", "iti\\b", "industrial training",
            "engineering", "medical sciences", "academy of higher") -> InstitutionKind.COLLEGE
        has(name, "school", "vidyalaya", "vidyalayam", "matric", "hr\\.? ?sec", "hss\\b", "gurukul", "kendriya", "navodaya") -> InstitutionKind.SCHOOL
        mapTag == "college" || mapTag == "university" -> InstitutionKind.COLLEGE
        else -> InstitutionKind.SCHOOL
    }

    fun schoolType(name: String): SchoolType = when {
        has(name, "junior college", "pre-university", "pre university", "p\\.?u\\.? college") -> SchoolType.JUNIOR_COLLEGE
        has(name, "higher secondary", "hr\\.? ?sec", "h\\.s\\.s", "hss\\b", "senior secondary", "sr\\.? sec", "matric", "plus two", "inter college") -> SchoolType.HIGHER_SECONDARY
        has(name, "primary", "elementary", "nursery", "kindergarten", "\\bps\\b") && !has(name, "high", "secondary") -> SchoolType.PRIMARY
        has(name, "middle", "upper primary", "\\bms\\b") -> SchoolType.MIDDLE
        has(name, "high school", "\\bhs\\b") -> SchoolType.HIGH
        else -> SchoolType.HIGHER_SECONDARY // most schools people reunite from teach up to 10th/12th
    }

    fun collegeType(name: String, mapTag: String? = null): CollegeType? = when {
        has(name, "polytechnic") -> CollegeType.POLYTECHNIC
        has(name, "iti\\b", "industrial training") -> CollegeType.ITI
        has(name, "dental") -> CollegeType.DENTAL
        has(name, "nursing") -> CollegeType.NURSING
        has(name, "pharmacy", "pharmaceutical") -> CollegeType.PHARMACY
        has(name, "medical", "medicine", "aiims", "health sciences") -> CollegeType.MEDICAL
        has(name, "siddha", "ayurved", "homoeopath", "homeopath", "unani", "naturopathy") -> CollegeType.AYUSH
        has(name, "veterinary", "animal sciences") -> CollegeType.VETERINARY
        has(name, "agricultur", "horticultur", "fisheries") -> CollegeType.AGRICULTURE
        has(name, "law\\b", "legal") -> CollegeType.LAW
        has(name, "architecture", "planning") -> CollegeType.ARCHITECTURE
        has(name, "hotel management", "catering", "hospitality") -> CollegeType.HOTEL
        has(name, "design", "fine arts", "fashion") -> CollegeType.DESIGN
        has(name, "engineering", "technology", "\\biit\\b", "\\bnit\\b", "engg", "tech\\b") -> CollegeType.ENGINEERING
        has(name, "college of education", "teacher", "b\\.?ed\\b", "training college") -> CollegeType.EDUCATION
        has(name, "business school", "management", "\\bmba\\b") -> CollegeType.MANAGEMENT
        has(name, "university", "deemed") -> CollegeType.UNIVERSITY
        has(name, "arts", "science", "commerce", "women'?s college", "men'?s college", "autonomous", "degree college") -> CollegeType.ARTS_SCIENCE
        mapTag == "university" -> CollegeType.UNIVERSITY
        else -> null // unknown: show everything, grouped by level
    }
}
