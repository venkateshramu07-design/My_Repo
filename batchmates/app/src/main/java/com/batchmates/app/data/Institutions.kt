package com.batchmates.app.data

/**
 * Global model of "what you studied where", built on UNESCO's International Standard Classification of
 * Education (ISCED 2011 levels, ISCED-F 2013 fields) with each country's own names on top, so a school in
 * Ohio, Manchester, Lagos or Kumbakonam all feel native.
 */
enum class InstitutionKind { SCHOOL, COLLEGE }

/**
 * One "track" = what a batch studied. It is part of the group id, so codes must be canonical:
 * school stages use the country's code ("12", "Y13", "ABI"), colleges use "<level>.<discipline>" ("UG.computer-science").
 */
data class Track(val code: String, val label: String, val short: String) {
    val isCollege: Boolean get() = '.' in code

    companion object {
        // Kept for India and for old data.
        val FIFTH = Track("5", "5th Standard · Primary", "5th")
        val EIGHTH = Track("8", "8th Standard · Middle school", "8th")
        val TENTH = Track("10", "10th Standard · SSLC / Matric", "10th")
        val TWELFTH = Track("12", "12th Standard · HSC / Plus Two", "12th")

        /** Finds a school stage by code in any system (for reading stored groups). */
        fun school(code: String): Track =
            SchoolSystems.all.asSequence().flatMap { it.stages.asSequence() }.firstOrNull { it.track.code == code }?.track
                ?: Track(code, code, code)

        fun college(level: Level, discipline: String): Track {
            val d = discipline.trim()
            return Track("${level.code}.${slug(d)}", "${level.label} · $d", "${level.short} · $d")
        }

        fun slug(s: String) = s.lowercase().replace("&", " and ")
            .split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotBlank() }.joinToString("-")
    }
}

// ============================================================================ schools

/** A school-leaving milestone, tied to its ISCED level (1 primary, 2 lower secondary, 3 upper secondary). */
data class SchoolStage(val track: Track, val isced: Int)

data class SchoolSystem(
    val name: String,
    val countries: Set<String>,
    /** Youngest first. */
    val stages: List<SchoolStage>,
    /** What people call a class division here: Section, Form, Homeroom, Klasse… */
    val sectionWord: String = "Class",
    /** Stages offered when the school's name gives no hint (oldest first). */
    val defaults: List<String> = stages.filter { it.isced >= 2 }.map { it.track.code }.reversed(),
)

private fun st(code: String, label: String, short: String, isced: Int) = SchoolStage(Track(code, label, short), isced)

object SchoolSystems {
    val INDIA = SchoolSystem(
        "India", setOf("IN"),
        listOf(st("5", "5th Standard · Primary", "5th", 1), st("8", "8th Standard · Middle school", "8th", 2),
            st("10", "10th Standard · SSLC / Matric", "10th", 3), st("12", "12th Standard · HSC / Plus Two", "12th", 3)),
        "Section", listOf("12", "10"),
    )
    val PAKISTAN = SchoolSystem(
        "Pakistan", setOf("PK"),
        listOf(st("5", "Class 5 · Primary", "Class 5", 1), st("8", "Class 8 · Middle", "Class 8", 2),
            st("10", "Class 10 · Matric (SSC)", "Matric", 3), st("12", "Class 12 · Intermediate (HSSC)", "Inter", 3)),
        "Section", listOf("12", "10"),
    )
    val BANGLADESH = SchoolSystem(
        "Bangladesh", setOf("BD"),
        listOf(st("5", "Class 5 · Primary (PEC)", "Class 5", 1), st("8", "Class 8 · JSC", "JSC", 2),
            st("10", "Class 10 · SSC", "SSC", 3), st("12", "Class 12 · HSC", "HSC", 3)),
        "Section", listOf("12", "10"),
    )
    val NEPAL = SchoolSystem(
        "Nepal", setOf("NP", "BT"),
        listOf(st("8", "Grade 8 · Basic level", "Grade 8", 2), st("10", "Grade 10 · SEE", "SEE", 3), st("12", "Grade 12 · +2", "+2", 3)),
        "Section", listOf("12", "10"),
    )
    val SRI_LANKA = SchoolSystem(
        "Sri Lanka", setOf("LK", "MV"),
        listOf(st("G5", "Grade 5 · Scholarship", "Grade 5", 1), st("OL", "Grade 11 · O/L", "O/L", 2), st("AL", "Grade 13 · A/L", "A/L", 3)),
        "Class",
    )
    val US = SchoolSystem(
        "United States", setOf("US", "PR"),
        listOf(st("US5", "5th grade · Elementary school", "Elementary", 1), st("US8", "8th grade · Middle school", "Middle school", 2),
            st("US12", "12th grade · High school", "High school", 3)),
        "Homeroom",
    )
    val CANADA = SchoolSystem(
        "Canada", setOf("CA"),
        listOf(st("CA6", "Grade 6 · Elementary", "Elementary", 1), st("CA8", "Grade 8 · Middle / Junior high", "Junior high", 2),
            st("CA12", "Grade 12 · High school", "High school", 3)),
        "Homeroom",
    )
    val UK = SchoolSystem(
        "United Kingdom", setOf("GB", "GI", "IM", "JE", "GG", "MT", "CY"),
        listOf(st("Y6", "Year 6 · Primary", "Primary", 1), st("Y11", "Year 11 · GCSE", "GCSE", 2), st("Y13", "Year 13 · A-Levels / Sixth form", "A-Levels", 3)),
        "Form",
    )
    val IRELAND = SchoolSystem(
        "Ireland", setOf("IE"),
        listOf(st("6C", "6th Class · Primary", "Primary", 1), st("JC", "3rd Year · Junior Cert", "Junior Cert", 2), st("LC", "6th Year · Leaving Cert", "Leaving Cert", 3)),
        "Class",
    )
    val AUSTRALIA = SchoolSystem(
        "Australia", setOf("AU"),
        listOf(st("Y6", "Year 6 · Primary", "Primary", 1), st("Y10", "Year 10", "Year 10", 2), st("Y12", "Year 12 · HSC / VCE / ATAR", "Year 12", 3)),
        "Class",
    )
    val NEW_ZEALAND = SchoolSystem(
        "New Zealand", setOf("NZ", "FJ", "WS", "TO"),
        listOf(st("Y8", "Year 8 · Intermediate", "Intermediate", 1), st("Y11", "Year 11 · NCEA Level 1", "Year 11", 2), st("Y13", "Year 13 · NCEA Level 3", "Year 13", 3)),
        "Form",
    )
    val GERMAN = SchoolSystem(
        "Germany / Austria / Switzerland", setOf("DE", "AT", "CH", "LI"),
        listOf(st("GS4", "Klasse 4 · Grundschule / Volksschule", "Grundschule", 1), st("MR10", "Klasse 10 · Mittlere Reife", "Mittlere Reife", 2),
            st("ABI", "Abitur / Matura", "Abitur", 3)),
        "Klasse",
    )
    val FRENCH = SchoolSystem(
        "France & francophone", setOf("FR", "BE", "LU", "MC", "MA", "TN", "DZ", "SN", "CI", "CM", "ML", "BF", "NE", "TG", "BJ", "GA", "CG", "CD", "MG", "HT", "RE", "GP", "MQ"),
        listOf(st("CM2", "CM2 · École primaire", "Primaire", 1), st("3E", "3e · Brevet (Collège)", "Brevet", 2), st("TERM", "Terminale · Baccalauréat (Lycée)", "Bac", 3)),
        "Classe",
    )
    val SPANISH = SchoolSystem(
        "Spain & Latin America", setOf("ES", "MX", "AR", "CO", "CL", "PE", "VE", "EC", "GT", "CU", "BO", "DO", "HN", "PY", "SV", "NI", "CR", "PA", "UY", "GQ"),
        listOf(st("PRI", "6º Primaria", "Primaria", 1), st("SEC", "Secundaria / ESO", "Secundaria", 2), st("BACH", "Bachillerato / Preparatoria", "Bachillerato", 3)),
        "Grupo",
    )
    val PORTUGUESE = SchoolSystem(
        "Brazil & Portugal", setOf("BR", "PT", "AO", "MZ", "CV", "GW", "ST", "TL"),
        listOf(st("EF1", "5º ano · Fundamental I", "Fundamental I", 1), st("EF2", "9º ano · Fundamental II", "Fundamental II", 2),
            st("EM", "3º ano · Ensino Médio / Secundário", "Ensino Médio", 3)),
        "Turma",
    )
    val ITALY = SchoolSystem(
        "Italy", setOf("IT", "SM", "VA"),
        listOf(st("EL5", "5ª Elementare", "Elementari", 1), st("MED3", "3ª Media", "Medie", 2), st("MAT", "5ª Superiore · Maturità", "Maturità", 3)),
        "Sezione",
    )
    val SINGAPORE = SchoolSystem(
        "Singapore", setOf("SG"),
        listOf(st("P6", "Primary 6 · PSLE", "PSLE", 1), st("S4", "Secondary 4 · O-Level", "O-Level", 2), st("JC2", "JC2 · A-Level", "A-Level", 3)),
        "Class",
    )
    val MALAYSIA = SchoolSystem(
        "Malaysia & Brunei", setOf("MY", "BN"),
        listOf(st("STD6", "Standard 6 · Primary", "Standard 6", 1), st("F5", "Form 5 · SPM", "SPM", 2), st("F6", "Form 6 · STPM", "STPM", 3)),
        "Class",
    )
    val PHILIPPINES = SchoolSystem(
        "Philippines", setOf("PH"),
        listOf(st("G6", "Grade 6 · Elementary", "Elementary", 1), st("G10", "Grade 10 · Junior High", "Junior High", 2), st("G12", "Grade 12 · Senior High", "Senior High", 3)),
        "Section",
    )
    val WEST_AFRICA = SchoolSystem(
        "West Africa", setOf("NG", "GH", "SL", "LR", "GM"),
        listOf(st("P6", "Primary 6", "Primary 6", 1), st("JSS3", "JSS 3 · BECE", "JSS 3", 2), st("SSS3", "SSS 3 · WASSCE / NECO", "SSS 3", 3)),
        "Arm",
    )
    val EAST_AFRICA = SchoolSystem(
        "East Africa", setOf("KE", "TZ", "UG", "RW", "BI", "SS", "ET", "SO", "MW", "ZM"),
        listOf(st("STD8", "Standard 7/8 · Primary", "Primary", 1), st("F4", "Form 4 · KCSE / CSEE / UCE", "Form 4", 2), st("F6", "Form 6 · A-Level", "Form 6", 3)),
        "Stream",
    )
    val SOUTHERN_AFRICA = SchoolSystem(
        "Southern Africa", setOf("ZA", "ZW", "NA", "BW", "LS", "SZ"),
        listOf(st("G7", "Grade 7 · Primary", "Grade 7", 1), st("G9", "Grade 9", "Grade 9", 2), st("MATRIC", "Grade 12 · Matric", "Matric", 3)),
        "Class",
    )
    val EAST_ASIA = SchoolSystem(
        "East & South-East Asia", setOf("JP", "KR", "KP", "CN", "TW", "HK", "MO", "VN", "TH", "ID", "MN", "KH", "LA", "MM"),
        listOf(st("ES", "Elementary school (final year)", "Elementary", 1), st("JHS", "Junior high / Middle school", "Junior high", 2),
            st("HS", "High school / Senior high", "High school", 3)),
        "Class",
    )
    val MIDDLE_EAST = SchoolSystem(
        "Middle East & North Africa", setOf("AE", "SA", "QA", "KW", "OM", "BH", "EG", "JO", "LB", "IQ", "SY", "YE", "PS", "IR", "TR", "LY", "SD", "IL", "AF"),
        listOf(st("G6", "Grade 6 · Primary", "Grade 6", 1), st("G9", "Grade 9 · Preparatory / Intermediate", "Grade 9", 2), st("G12", "Grade 12 · Secondary", "Grade 12", 3)),
        "Section",
    )
    val EUROPE = SchoolSystem(
        "Europe (other)", setOf("NL", "SE", "NO", "DK", "FI", "IS", "PL", "CZ", "SK", "HU", "RO", "BG", "GR", "HR", "SI", "RS", "BA", "ME", "MK", "AL", "XK", "EE", "LV", "LT", "UA", "BY", "MD", "RU", "GE", "AM", "AZ", "KZ", "UZ", "KG", "TJ", "TM"),
        listOf(st("PRI", "Primary school (final year)", "Primary", 1), st("LSEC", "Lower secondary / Basic school", "Lower secondary", 2),
            st("USEC", "Upper secondary / Gymnasium (final year)", "Upper secondary", 3)),
        "Class",
    )
    val DEFAULT = SchoolSystem(
        "International", emptySet(),
        listOf(st("PRI", "Primary school (final year)", "Primary", 1), st("LSEC", "Lower secondary / Middle school", "Middle school", 2),
            st("USEC", "Upper secondary / High school (final year)", "High school", 3)),
        "Class",
    )

    val all = listOf(
        INDIA, PAKISTAN, BANGLADESH, NEPAL, SRI_LANKA, US, CANADA, UK, IRELAND, AUSTRALIA, NEW_ZEALAND, GERMAN, FRENCH,
        SPANISH, PORTUGUESE, ITALY, SINGAPORE, MALAYSIA, PHILIPPINES, WEST_AFRICA, EAST_AFRICA, SOUTHERN_AFRICA, EAST_ASIA,
        MIDDLE_EAST, EUROPE, DEFAULT,
    )

    fun forCountry(code: String): SchoolSystem = all.firstOrNull { code.uppercase() in it.countries } ?: DEFAULT

    /**
     * Curriculum beats geography: an "Indian School" in Dubai runs CBSE (10th/12th),
     * a "British School" in Doha runs GCSE/A-Levels.
     */
    fun forSchool(name: String, countryCode: String): SchoolSystem {
        val n = name.lowercase()
        return when {
            Regex("\\b(indian|cbse|icse|kendriya|delhi public)\\b").containsMatchIn(n) -> INDIA
            Regex("\\b(pakistan|pakistani)\\b").containsMatchIn(n) -> PAKISTAN
            Regex("\\b(british|igcse|gcse|a[- ]level)\\b").containsMatchIn(n) -> UK
            Regex("\\b(american)\\b").containsMatchIn(n) && countryCode.uppercase() != "US" -> US
            Regex("\\b(lyc[ée]e fran[çc]ais|french school)\\b").containsMatchIn(n) -> FRENCH
            Regex("\\b(deutsche schule|german school)\\b").containsMatchIn(n) -> GERMAN
            else -> forCountry(countryCode)
        }
    }
}

/** A school's profile: its system plus which stages it can have, most likely first. */
data class SchoolProfile(val label: String, val system: SchoolSystem, val tracks: List<Track>) {
    /** Everything the system has, oldest first, for "Other class". */
    val allTracks: List<Track> get() = system.stages.reversed().map { it.track }
}

// ============================================================================ colleges

/** ISCED 2011 tertiary levels, in the order people progress through them. */
enum class Level(val code: String, val label: String, val short: String, val emoji: String) {
    CERT("CERT", "Certificate / Diploma", "Diploma", "🛠️"),     // ISCED 4-5 (ITI, polytechnic, TAFE, BTEC)
    ASSOC("ASSOC", "Associate / Foundation degree", "Associate", "📗"), // ISCED 5 (US associate, UK foundation/HND)
    BACHELOR("UG", "Bachelor's degree", "Bachelor's", "🎓"),     // ISCED 6
    MASTER("PG", "Master's degree", "Master's", "📜"),           // ISCED 7
    DOCTORATE("PHD", "Doctorate / PhD", "Doctorate", "🔬"),       // ISCED 8
}

/** ISCED-F 2013 broad fields (lightly renamed for humans), with common disciplines. */
enum class Field(val label: String, val emoji: String, val disciplines: List<String>) {
    ENGINEERING("Engineering & Technology", "⚙️", listOf(
        "Mechanical", "Civil", "Electrical", "Electrical & Electronics", "Electronics & Communication", "Computer Engineering",
        "Chemical", "Aerospace / Aeronautical", "Automobile", "Biomedical", "Industrial / Production", "Instrumentation & Control",
        "Mechatronics", "Materials & Metallurgy", "Petroleum", "Mining", "Marine", "Textile", "Environmental Engineering",
    )),
    ICT("Computing & IT", "💻", listOf(
        "Computer Science", "Information Technology", "Software Engineering", "Data Science", "AI & Machine Learning",
        "Cybersecurity", "Computer Applications", "Information Systems",
    )),
    SCIENCE("Natural Sciences & Maths", "🔭", listOf(
        "Physics", "Chemistry", "Mathematics", "Statistics", "Biology", "Biotechnology", "Microbiology", "Biochemistry",
        "Botany", "Zoology", "Environmental Science", "Geology / Earth Science", "Astronomy",
    )),
    HEALTH("Health & Medicine", "🩺", listOf(
        "Medicine", "Dentistry", "Nursing", "Pharmacy", "Physiotherapy", "Public Health", "Medical Laboratory Science",
        "Optometry", "Nutrition & Dietetics", "Traditional Medicine (Ayurveda, Siddha, TCM…)", "Allied Health",
    )),
    BUSINESS("Business & Management", "💼", listOf(
        "Business Administration", "Commerce", "Accounting", "Finance", "Marketing", "Human Resources",
        "International Business", "Entrepreneurship", "Business Analytics", "Supply Chain & Logistics",
    )),
    LAW("Law", "⚖️", listOf("Law", "Criminology", "Legal Studies")),
    ARTS("Arts & Humanities", "🎭", listOf(
        "English / Literature", "Languages", "History", "Philosophy", "Religious Studies / Theology", "Music",
        "Fine Arts", "Performing Arts", "Film & Media Production", "Design",
    )),
    SOCIAL("Social Sciences & Journalism", "🗞️", listOf(
        "Economics", "Political Science", "Psychology", "Sociology", "Anthropology", "Geography",
        "International Relations", "Journalism & Mass Communication", "Social Work", "Library & Information Science",
    )),
    EDUCATION("Education & Teaching", "🍎", listOf(
        "Early Childhood Education", "Primary Teaching", "Secondary Teaching", "Special Education", "Physical Education", "Educational Leadership",
    )),
    ARCHITECTURE("Architecture & Planning", "📐", listOf(
        "Architecture", "Urban & Regional Planning", "Interior Design", "Landscape Architecture", "Construction Management",
    )),
    AGRICULTURE("Agriculture & Veterinary", "🌾", listOf(
        "Agriculture", "Horticulture", "Forestry", "Fisheries", "Food Technology", "Dairy Technology", "Veterinary Science",
    )),
    SERVICES("Hospitality, Sport & Services", "🍽️", listOf(
        "Hotel & Hospitality Management", "Tourism", "Culinary Arts", "Aviation", "Sports Science", "Fashion", "Beauty & Wellness",
    )),
}

/** What a college is. Decides which levels and fields (and sometimes which disciplines) can appear. */
enum class CollegeType(
    val label: String,
    val emoji: String,
    val levels: Set<Level>,
    val fields: Set<Field>,
    /** Narrow further when a field is too broad (a dental school only teaches dentistry). */
    val onlyDisciplines: Set<String> = emptySet(),
    /** Fields offered only at some levels (engineering institutes teach business as an MBA, not a BBA). */
    val fieldLevels: Map<Field, Set<Level>> = emptyMap(),
) {
    UNIVERSITY("University (all faculties)", "🏛️", Level.entries.toSet(), Field.entries.toSet()),
    ENGINEERING("Engineering / Technology institute", "⚙️",
        setOf(Level.BACHELOR, Level.MASTER, Level.DOCTORATE),
        setOf(Field.ENGINEERING, Field.ICT, Field.SCIENCE, Field.ARCHITECTURE, Field.BUSINESS),
        fieldLevels = mapOf(Field.BUSINESS to setOf(Level.MASTER, Level.DOCTORATE))),
    POLYTECHNIC("Polytechnic / Technical / Vocational", "🛠️",
        setOf(Level.CERT, Level.ASSOC),
        setOf(Field.ENGINEERING, Field.ICT, Field.BUSINESS, Field.SERVICES, Field.AGRICULTURE, Field.ARCHITECTURE, Field.HEALTH)),
    COMMUNITY("Community college", "🏫",
        setOf(Level.CERT, Level.ASSOC), Field.entries.toSet()),
    MEDICAL("Medical school / college", "🩺",
        setOf(Level.CERT, Level.BACHELOR, Level.MASTER, Level.DOCTORATE), setOf(Field.HEALTH)),
    DENTAL("Dental school", "🦷", setOf(Level.BACHELOR, Level.MASTER, Level.DOCTORATE), setOf(Field.HEALTH), setOf("Dentistry")),
    NURSING("Nursing school", "💉", setOf(Level.CERT, Level.BACHELOR, Level.MASTER), setOf(Field.HEALTH), setOf("Nursing", "Allied Health")),
    PHARMACY("Pharmacy school", "💊", setOf(Level.CERT, Level.BACHELOR, Level.MASTER, Level.DOCTORATE), setOf(Field.HEALTH), setOf("Pharmacy")),
    TRADITIONAL_MEDICINE("Traditional medicine college", "🌿", setOf(Level.BACHELOR, Level.MASTER), setOf(Field.HEALTH), setOf("Traditional Medicine (Ayurveda, Siddha, TCM…)")),
    ARTS_SCIENCE("Arts & Science / Liberal arts college", "📚",
        setOf(Level.CERT, Level.BACHELOR, Level.MASTER, Level.DOCTORATE),
        setOf(Field.ARTS, Field.SOCIAL, Field.SCIENCE, Field.ICT, Field.BUSINESS)),
    BUSINESS("Business school", "💼", setOf(Level.CERT, Level.BACHELOR, Level.MASTER, Level.DOCTORATE), setOf(Field.BUSINESS)),
    LAW("Law school", "⚖️", setOf(Level.BACHELOR, Level.MASTER, Level.DOCTORATE), setOf(Field.LAW)),
    EDUCATION("College of Education / Teachers' college", "🍎", Level.entries.toSet(), setOf(Field.EDUCATION)),
    AGRICULTURE("Agricultural college", "🌾", Level.entries.toSet(), setOf(Field.AGRICULTURE)),
    VETERINARY("Veterinary school", "🐄", setOf(Level.BACHELOR, Level.MASTER, Level.DOCTORATE), setOf(Field.AGRICULTURE), setOf("Veterinary Science")),
    ARCHITECTURE("School of Architecture", "📐", setOf(Level.BACHELOR, Level.MASTER, Level.DOCTORATE), setOf(Field.ARCHITECTURE)),
    ART_DESIGN("Art, Music & Design school", "🎨", Level.entries.toSet(), setOf(Field.ARTS), setOf(
        "Music", "Fine Arts", "Performing Arts", "Film & Media Production", "Design")),
    HOSPITALITY("Hospitality / Hotel school", "🍽️", setOf(Level.CERT, Level.ASSOC, Level.BACHELOR, Level.MASTER), setOf(Field.SERVICES)),
    THEOLOGY("Seminary / Theological college", "⛪", Level.entries.toSet(), setOf(Field.ARTS), setOf("Religious Studies / Theology", "Philosophy")),
    ;

    fun allows(field: Field) = field in fields
    fun allows(field: Field, level: Level?) = allows(field) && (level == null || fieldLevels[field]?.contains(level) != false)
    fun disciplines(field: Field): List<String> =
        if (onlyDisciplines.isEmpty()) field.disciplines else field.disciplines.filter { it in onlyDisciplines }
}

object Degrees {
    /** Local degree names shown as hints, so people recognise their own degree. */
    fun localName(country: String, level: Level, field: Field): String? {
        val c = country.uppercase()
        val south = c in setOf("IN", "PK", "BD", "LK", "NP")
        return when {
            south -> when (level) {
                Level.CERT -> when (field) {
                    Field.ENGINEERING, Field.ICT -> "Polytechnic diploma / ITI"
                    Field.HEALTH -> "GNM / D.Pharm / DMLT"
                    Field.EDUCATION -> "D.El.Ed / D.T.Ed"
                    else -> null
                }
                Level.BACHELOR -> when (field) {
                    Field.ENGINEERING -> "B.E. / B.Tech"
                    Field.ICT -> "B.E. / B.Tech · B.Sc · BCA"
                    Field.SCIENCE -> "B.Sc"
                    Field.ARTS, Field.SOCIAL -> "B.A."
                    Field.BUSINESS -> "B.Com · BBA"
                    Field.HEALTH -> "MBBS · BDS · B.Sc Nursing · B.Pharm · BAMS/BSMS"
                    Field.LAW -> "LLB · BA LLB"
                    Field.EDUCATION -> "B.Ed"
                    Field.ARCHITECTURE -> "B.Arch"
                    Field.AGRICULTURE -> "B.Sc Agri · B.V.Sc · B.Tech Food"
                    Field.SERVICES -> "BHM · B.Sc Hospitality"
                }
                Level.MASTER -> when (field) {
                    Field.ENGINEERING -> "M.E. / M.Tech"
                    Field.ICT -> "MCA · M.Tech · M.Sc"
                    Field.SCIENCE -> "M.Sc"
                    Field.ARTS, Field.SOCIAL -> "M.A. · MSW"
                    Field.BUSINESS -> "MBA · M.Com · PGDM"
                    Field.HEALTH -> "MD · MS · MDS · M.Pharm"
                    Field.LAW -> "LLM"
                    Field.EDUCATION -> "M.Ed"
                    Field.ARCHITECTURE -> "M.Arch · M.Plan"
                    else -> null
                }
                Level.DOCTORATE -> "PhD"
                Level.ASSOC -> null
            }
            c in setOf("US", "CA", "PR", "PH") -> when (level) {
                Level.ASSOC -> "AA · AS · AAS"
                Level.BACHELOR -> when (field) {
                    Field.ENGINEERING -> "BS / BSE"
                    Field.HEALTH -> "BSN · BS"
                    Field.ARTS -> "BA · BFA"
                    Field.BUSINESS -> "BBA · BS"
                    else -> "BA · BS"
                }
                Level.MASTER -> if (field == Field.BUSINESS) "MBA" else "MA · MS"
                Level.DOCTORATE -> when (field) {
                    Field.HEALTH -> "MD · DDS · PharmD · DNP"
                    Field.LAW -> "JD"
                    else -> "PhD"
                }
                Level.CERT -> "Certificate"
            }
            c in setOf("GB", "IE", "AU", "NZ", "ZA", "SG", "MY", "HK", "NG", "GH", "KE") -> when (level) {
                Level.CERT -> "BTEC · HNC · TAFE / Polytechnic diploma"
                Level.ASSOC -> "Foundation degree · HND"
                Level.BACHELOR -> when (field) {
                    Field.HEALTH -> "MBBS / MBChB · BDS · BSc Nursing"
                    Field.LAW -> "LLB"
                    Field.ENGINEERING -> "BEng · MEng"
                    Field.ARTS, Field.SOCIAL -> "BA (Hons)"
                    else -> "BSc / BA (Hons)"
                }
                Level.MASTER -> if (field == Field.BUSINESS) "MBA · MSc" else "MSc · MA · MRes"
                Level.DOCTORATE -> "PhD · DPhil"
            }
            c in SchoolSystems.GERMAN.countries -> when (level) {
                Level.BACHELOR -> "Bachelor (B.Sc. / B.A. / B.Eng.)"
                Level.MASTER -> "Master · Diplom · Staatsexamen"
                Level.DOCTORATE -> "Promotion (Dr.)"
                Level.CERT -> "Ausbildung"
                else -> null
            }
            c in SchoolSystems.FRENCH.countries -> when (level) {
                Level.CERT, Level.ASSOC -> "BTS · DUT / BUT"
                Level.BACHELOR -> "Licence"
                Level.MASTER -> "Master · Diplôme d'ingénieur"
                Level.DOCTORATE -> "Doctorat"
            }
            c in SchoolSystems.SPANISH.countries -> when (level) {
                Level.CERT, Level.ASSOC -> "Técnico / Tecnólogo"
                Level.BACHELOR -> "Grado / Licenciatura / Ingeniería"
                Level.MASTER -> "Máster / Maestría"
                Level.DOCTORATE -> "Doctorado"
            }
            c in SchoolSystems.PORTUGUESE.countries -> when (level) {
                Level.CERT, Level.ASSOC -> "Técnico / Tecnólogo"
                Level.BACHELOR -> "Bacharelado / Licenciatura"
                Level.MASTER -> "Mestrado"
                Level.DOCTORATE -> "Doutorado"
            }
            else -> null
        }
    }

    private val shortcuts: Map<String, Pair<Field, String>> = mapOf(
        "cse" to (Field.ICT to "Computer Science"), "cs" to (Field.ICT to "Computer Science"),
        "comp sci" to (Field.ICT to "Computer Science"), "compsci" to (Field.ICT to "Computer Science"),
        "computer science and engineering" to (Field.ICT to "Computer Science"),
        "it" to (Field.ICT to "Information Technology"), "bca" to (Field.ICT to "Computer Applications"),
        "mca" to (Field.ICT to "Computer Applications"), "ai" to (Field.ICT to "AI & Machine Learning"),
        "aids" to (Field.ICT to "Data Science"), "ai and ds" to (Field.ICT to "Data Science"), "ds" to (Field.ICT to "Data Science"),
        "ece" to (Field.ENGINEERING to "Electronics & Communication"), "eee" to (Field.ENGINEERING to "Electrical & Electronics"),
        "ee" to (Field.ENGINEERING to "Electrical"), "mech" to (Field.ENGINEERING to "Mechanical"),
        "eie" to (Field.ENGINEERING to "Instrumentation & Control"), "ice" to (Field.ENGINEERING to "Instrumentation & Control"),
        "aero" to (Field.ENGINEERING to "Aerospace / Aeronautical"), "auto" to (Field.ENGINEERING to "Automobile"),
        "mbbs" to (Field.HEALTH to "Medicine"), "mbchb" to (Field.HEALTH to "Medicine"), "md" to (Field.HEALTH to "Medicine"),
        "bds" to (Field.HEALTH to "Dentistry"), "dds" to (Field.HEALTH to "Dentistry"), "mds" to (Field.HEALTH to "Dentistry"),
        "bpharm" to (Field.HEALTH to "Pharmacy"), "pharmd" to (Field.HEALTH to "Pharmacy"), "bsn" to (Field.HEALTH to "Nursing"),
        "bams" to (Field.HEALTH to "Traditional Medicine (Ayurveda, Siddha, TCM…)"), "bsms" to (Field.HEALTH to "Traditional Medicine (Ayurveda, Siddha, TCM…)"),
        "mba" to (Field.BUSINESS to "Business Administration"), "bba" to (Field.BUSINESS to "Business Administration"),
        "bcom" to (Field.BUSINESS to "Commerce"), "mcom" to (Field.BUSINESS to "Commerce"), "hr" to (Field.BUSINESS to "Human Resources"),
        "llb" to (Field.LAW to "Law"), "jd" to (Field.LAW to "Law"), "llm" to (Field.LAW to "Law"),
        "bed" to (Field.EDUCATION to "Secondary Teaching"), "barch" to (Field.ARCHITECTURE to "Architecture"),
        "maths" to (Field.SCIENCE to "Mathematics"), "math" to (Field.SCIENCE to "Mathematics"), "stats" to (Field.SCIENCE to "Statistics"),
        "bio" to (Field.SCIENCE to "Biology"), "chem" to (Field.SCIENCE to "Chemistry"), "phy" to (Field.SCIENCE to "Physics"),
        "econ" to (Field.SOCIAL to "Economics"), "eco" to (Field.SOCIAL to "Economics"), "psych" to (Field.SOCIAL to "Psychology"),
        "poli sci" to (Field.SOCIAL to "Political Science"), "polsci" to (Field.SOCIAL to "Political Science"),
        "ir" to (Field.SOCIAL to "International Relations"), "msw" to (Field.SOCIAL to "Social Work"), "bsw" to (Field.SOCIAL to "Social Work"),
        "viscom" to (Field.SOCIAL to "Journalism & Mass Communication"), "bhm" to (Field.SERVICES to "Hotel & Hospitality Management"),
    )

    private fun key(t: String) = t.lowercase().replace("&", " and ").replace(Regex("[^\\p{L}\\p{N} ]"), " ").replace(Regex("\\s+"), " ").trim()

    /** "cse", "Computer Science and Engineering", "computer science" → "Computer Science", so batches don't split. */
    fun canonical(text: String): String {
        val raw = key(text)
        val stripped = raw.removeSuffix(" engineering").removeSuffix(" engg").trim().removeSuffix(" and").trim()
        for (k in listOf(raw, stripped)) {
            shortcuts[k]?.let { return it.second }
            Field.entries.flatMap { it.disciplines }.firstOrNull { key(it) == k }?.let { return it }
        }
        return text.trim().split(Regex("\\s+")).joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
    }
}

// ============================================================================ classifier

/** Reads an institution's name (any common language), map tag and country to decide what it is. Users can correct it. */
object Classifier {
    private fun has(name: String, vararg patterns: String) =
        patterns.any { Regex("(?<![\\p{L}])$it", RegexOption.IGNORE_CASE).containsMatchIn(name) }

    /** Countries where "college" on a school-tagged place usually means a secondary school (Eton College, Colegio…). */
    private val collegeCanBeSchool = setOf(
        "GB", "IE", "AU", "NZ", "ZA", "ZW", "HK", "MT", "CY", "FR", "BE", "CA", "JM", "TT", "BB", "BS",
        "NG", "GH", "SL", "KE", "UG", "TZ", "ZM", "MW", "MU", "LK", "MY", "SG", "FJ",
    )

    private val tertiary = arrayOf(
        "universit", "uniwersytet", "üniversite", "universidad", "universidade", "université", "università", "universiteit",
        "hochschule", "fachhochschule", "polytechnic", "politécnic", "politecnic", "faculty of", "facultad", "faculté", "fakultät", "faculdade",
        "institute of technology", "institute of management", "institute of medical", "institute of science", "institute of engineering",
        "college of (engineering|technology|medicine|nursing|pharmacy|law|education|arts|science|commerce|agriculture|business|architecture|dental|veterinary|management|physiotherapy)",
        "medical college", "dental college", "engineering college", "law college", "arts college", "degree college", "nursing college", "pharmacy college",
        "medical school", "school of medicine", "law school", "school of law", "business school", "school of business", "school of management",
        "school of economics", "graduate school", "community college", "tafe", "iti\\b", "industrial training", "conservator",
        "seminary", "école (supérieure|nationale supérieure|normale)", "grande école", "instituto (tecnológico|superior|federal)",
    )

    fun kind(name: String, mapTag: String? = null, countryCode: String = ""): InstitutionKind {
        val c = countryCode.uppercase()
        return when {
            // Pre-university stages that are called "college" in some countries.
            has(name, "junior college") && c != "US" -> InstitutionKind.SCHOOL
            has(name, "pre-university", "pre university", "p\\.?u\\.? college", "sixth form", "colegio(?! (universitario|mayor))", "colégio", "collège(?! de france)", "cégep") ->
                if (has(name, "cégep")) InstitutionKind.COLLEGE else InstitutionKind.SCHOOL
            has(name, *tertiary) -> InstitutionKind.COLLEGE
            has(name, "college") -> if (mapTag == "school" && c in collegeCanBeSchool) InstitutionKind.SCHOOL else InstitutionKind.COLLEGE
            has(name, "school", "schule", "escuela", "escola", "scuola", "école", "ecole", "lycée", "lycee", "liceo", "liceu", "gymnasium",
                "gimnazjum", "vidyalaya", "vidyalayam", "matric", "hr\\.? ?sec", "hss\\b", "academy", "akademie", "instituto", "high\\b") -> InstitutionKind.SCHOOL
            mapTag == "college" || mapTag == "university" -> InstitutionKind.COLLEGE
            else -> InstitutionKind.SCHOOL
        }
    }

    /** Which ISCED levels a school name suggests (empty = no hint). Works across common languages. */
    private fun iscedHint(name: String): Set<Int> = when {
        has(name, "k-?12", "all[- ]through", "through school", "primary (and|&) secondary") -> setOf(1, 2, 3)
        has(name, "junior college", "pre-university", "pre university", "p\\.?u\\.? college", "sixth form", "senior high", "senior secondary",
            "higher secondary", "hr\\.? ?sec", "h\\.s\\.s", "hss\\b", "lycée", "lycee", "liceo", "liceu", "bachillerato", "preparatoria",
            "ensino médio", "ensino medio", "gymnasium", "a[- ]level", "plus two", "inter college") -> setOf(3)
        has(name, "middle", "junior high", "intermediate school", "upper primary", "collège", "college d", "realschule", "hauptschule",
            "mittelschule", "scuola media", "secundaria básica", "\\beso\\b", "jss\\b", "junior secondary", "lower secondary", "basic school") -> setOf(2)
        has(name, "primary", "elementary", "grundschule", "volksschule", "école primaire", "ecole primaire", "école élémentaire",
            "escuela primaria", "primaria", "scuola elementare", "scuola primaria", "infant", "junior school", "nursery", "kindergarten", "\\bps\\b") &&
            !has(name, "high", "secondary") -> setOf(1)
        has(name, "high school", "secondary", "secundaria", "matric", "grammar school", "comprehensive", "academy", "\\bhs\\b") -> setOf(2, 3)
        else -> emptySet()
    }

    fun schoolProfile(name: String, countryCode: String): SchoolProfile {
        val system = SchoolSystems.forSchool(name, countryCode)
        // India: "High school" there means up to 10th; "Matriculation" schools usually run to 12th.
        if (system == SchoolSystems.INDIA || system == SchoolSystems.PAKISTAN || system == SchoolSystems.BANGLADESH) {
            val abroad = system.countries.none { it == countryCode.uppercase() } // e.g. Indian schools in the Gulf run to 12th
            val codes = when {
                abroad -> system.defaults
                has(name, "junior college", "pre-university", "pre university", "p\\.?u\\.? college", "inter college") -> listOf("12")
                has(name, "higher secondary", "hr\\.? ?sec", "h\\.s\\.s", "hss\\b", "senior secondary", "sr\\.? sec", "matric", "plus two") -> listOf("12", "10")
                has(name, "primary", "elementary", "nursery", "\\bps\\b") && !has(name, "high", "secondary") -> listOf("5")
                has(name, "middle", "upper primary", "\\bms\\b") -> listOf("8", "5")
                has(name, "high school", "\\bhs\\b") -> listOf("10", "8")
                else -> system.defaults
            }
            return profile(system, codes)
        }
        val hint = iscedHint(name).let { h ->
            // In North America and East Asia "high school" is only the final stage (ISCED 3).
            if (h == setOf(2, 3) && has(name, "high") &&
                system in setOf(SchoolSystems.US, SchoolSystems.CANADA, SchoolSystems.EAST_ASIA, SchoolSystems.PHILIPPINES)) setOf(3) else h
        }
        val codes = if (hint.isEmpty()) system.defaults
        else system.stages.filter { it.isced in hint }.map { it.track.code }.reversed().ifEmpty { system.defaults }
        return profile(system, codes)
    }

    private fun profile(system: SchoolSystem, codes: List<String>): SchoolProfile {
        val tracks = codes.mapNotNull { c -> system.stages.firstOrNull { it.track.code == c }?.track }
        val iscedMax = system.stages.filter { it.track.code in codes }.maxOfOrNull { it.isced } ?: 3
        val iscedMin = system.stages.filter { it.track.code in codes }.minOfOrNull { it.isced } ?: 2
        val label = when {
            iscedMax == 1 -> "Primary school"
            iscedMax == 2 && iscedMin == 2 -> "Lower secondary school"
            iscedMin == 1 && iscedMax == 3 -> "All-through school"
            else -> "Secondary school"
        }
        return SchoolProfile(label, system, tracks)
    }

    fun collegeType(name: String, mapTag: String? = null, countryCode: String = ""): CollegeType? {
        val c = countryCode.uppercase()
        return when {
            has(name, "community college", "cégep", "tafe\\b", "further education") -> CollegeType.COMMUNITY
            has(name, "polytechnic", "iti\\b", "industrial training", "technical college", "technical institute", "vocational", "berufsschule", "institut universitaire de technologie") &&
                !has(name, "universit") -> CollegeType.POLYTECHNIC
            has(name, "dental", "dentistry", "odontolog") -> CollegeType.DENTAL
            has(name, "nursing", "enfermería", "infirmi") -> CollegeType.NURSING
            has(name, "pharmacy", "pharmaceutical", "pharmac", "farmacia") -> CollegeType.PHARMACY
            has(name, "siddha", "ayurved", "homoeopath", "homeopath", "unani", "naturopath", "chinese medicine", "\\btcm\\b") -> CollegeType.TRADITIONAL_MEDICINE
            has(name, "veterinary", "animal sciences", "veterinari", "tierärzt") -> CollegeType.VETERINARY
            has(name, "medical", "medicine", "médecine", "medicina", "medizin", "aiims", "health sciences") -> CollegeType.MEDICAL
            has(name, "law\\b", "legal", "derecho", "droit", "direito", "jura", "rechtswissen") -> CollegeType.LAW
            has(name, "agricultur", "agronom", "horticultur", "fisheries", "forestry", "landwirtschaft") -> CollegeType.AGRICULTURE
            has(name, "architecture", "arquitectura", "architektur", "planning") && !has(name, "universit", "technology") -> CollegeType.ARCHITECTURE
            has(name, "hotel", "catering", "hospitality", "culinary", "tourism") -> CollegeType.HOSPITALITY
            has(name, "seminary", "theolog", "divinity", "bible college", "seminario") -> CollegeType.THEOLOGY
            has(name, "conservator", "music", "fine arts", "art school", "school of art", "college of art", "design", "fashion", "film school", "beaux-arts", "bellas artes", "kunsthochschule") -> CollegeType.ART_DESIGN
            has(name, "college of education", "teachers'? college", "teacher training", "training college", "normal school", "normal university", "école normale", "pädagogische", "b\\.?ed\\b") -> CollegeType.EDUCATION
            has(name, "business school", "school of business", "school of management", "institute of management", "management studies", "\\bmba\\b", "école de commerce", "wirtschaft") -> CollegeType.BUSINESS
            // "University of Technology", "Technische Universität", MIT, Caltech… are full universities, not engineering-only colleges.
            has(name, "universit", "universidad", "universidade", "université", "università", "universiteit", "uniwersytet", "deemed") -> CollegeType.UNIVERSITY
            has(name, "institute of technology") && c != "IN" -> CollegeType.UNIVERSITY
            has(name, "engineering", "engg", "technology", "\\biit\\b", "\\bnit\\b", "ingenier", "ingénieur", "ingegneria", "engenharia", "technische", "politécnic", "politecnic") -> CollegeType.ENGINEERING
            has(name, "arts", "science", "commerce", "liberal arts", "women'?s college", "men'?s college", "autonomous", "degree college", "humanities") -> CollegeType.ARTS_SCIENCE
            mapTag == "university" -> CollegeType.UNIVERSITY
            else -> null // unknown: show everything, grouped by level
        }
    }
}
