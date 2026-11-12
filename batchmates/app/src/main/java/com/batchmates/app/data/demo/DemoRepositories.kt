package com.batchmates.app.data.demo

import android.content.Context
import com.batchmates.app.data.AppUser
import com.batchmates.app.data.AuthRepository
import com.batchmates.app.data.BatchKey
import com.batchmates.app.data.BatchStat
import com.batchmates.app.data.CollegeType
import com.batchmates.app.data.InstitutionKind
import com.batchmates.app.data.Track
import com.batchmates.app.data.Group
import com.batchmates.app.data.Member
import com.batchmates.app.data.InvitePreview
import com.batchmates.app.data.Message
import com.batchmates.app.data.MessageKind
import com.batchmates.app.data.MissingPerson
import com.batchmates.app.data.Reunion
import com.batchmates.app.data.Rsvp
import com.batchmates.app.data.Region
import com.batchmates.app.data.ReunionRepository
import com.batchmates.app.data.School
import com.batchmates.app.data.SchoolSource
import com.batchmates.app.util.InviteCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Runs the whole app without any backend so it can be tried instantly.
 * Activated automatically when no google-services.json was supplied at build time.
 */
class DemoAuthRepository : AuthRepository {
    private val _user = MutableStateFlow<AppUser?>(null)
    override val currentUser: StateFlow<AppUser?> = _user
    override val googleAvailable = true

    override suspend fun signInWithGoogle(activityContext: Context): Result<AppUser> {
        delay(600)
        return Result.success(AppUser("demo-me", "You (demo)", "you@example.com").also { _user.value = it })
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AppUser> {
        delay(500)
        if (password.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        return Result.success(AppUser("demo-me", email.substringBefore('@'), email).also { _user.value = it })
    }

    override suspend fun signUpWithEmail(name: String, email: String, password: String) =
        signInWithEmail(email, password).map { it.copy(name = name).also { u -> _user.value = u } }

    override suspend fun sendPasswordReset(email: String) = Result.success(Unit)
    override suspend fun signOut() { _user.value = null }
}

class DemoReunionRepository : ReunionRepository {

    private val friends = listOf("Priya", "Arjun", "Meena", "Karthik", "Divya", "Rahul", "Lakshmi", "Vikram", "Anitha", "Suresh")

    private val groups = MutableStateFlow<Map<String, Group>>(emptyMap())
    private val members = MutableStateFlow<Map<String, List<Member>>>(emptyMap())
    private val messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    private val mine = MutableStateFlow<List<String>>(emptyList())
    private val customSchools = MutableStateFlow<List<School>>(emptyList())
    private val codes = mutableMapOf<String, String>()

    override fun myGroups(uid: String): Flow<List<Group>> =
        mine.map { ids -> ids.mapNotNull { groups.value[it] } }

    override suspend fun customSchools(region: Region) = customSchools.value.filter {
        it.region.countryCode == region.countryCode && it.region.state == region.state
    }

    override suspend fun createSchool(
        name: String, region: Region, lat: Double?, lon: Double?, user: AppUser,
        kind: InstitutionKind, collegeType: CollegeType?,
    ): Result<School> {
        val s = School(UUID.randomUUID().toString().take(12), name.trim(), region, lat, lon, SchoolSource.CUSTOM, locality = region.town, kind = kind, collegeType = collegeType)
        customSchools.value = customSchools.value + s
        return Result.success(s)
    }

    override suspend fun setInstitutionType(school: School, kind: InstitutionKind, collegeType: CollegeType?): Result<Unit> {
        val updated = school.copy(kind = kind, collegeType = collegeType, distanceKm = null)
        customSchools.value = customSchools.value.filter { it.id != school.id } + updated
        return Result.success(Unit)
    }

    override suspend fun addAlias(school: School, alias: String): Result<Unit> {
        val clean = alias.trim()
        if (clean.isBlank() || clean.equals(school.name, true)) return Result.success(Unit)
        val existing = customSchools.value.firstOrNull { it.id == school.id }
        customSchools.value = if (existing != null) {
            customSchools.value.map { if (it.id == school.id) it.copy(aliases = (it.aliases + clean).distinct()) else it }
        } else {
            customSchools.value + school.copy(aliases = (school.aliases + clean).distinct(), distanceKm = null)
        }
        return Result.success(Unit)
    }

    /** Pretend a few classmates are already waiting, so the demo feels alive. */
    override suspend fun batchStats(schoolId: String): List<BatchStat> {
        delay(250)
        val seed = schoolId.hashCode()
        val real = groups.value.values.filter { it.schoolId == schoolId }
            .map { BatchStat(it.track, it.year, it.section, it.memberCount) }
        val fake = (0 until 6).map { i ->
            val r = (seed xor (i * 7919)).let { if (it < 0) -it else it }
            BatchStat(if (i % 2 == 0) Track.TENTH else Track.TWELFTH, 2000 + r % 25, "ABCD"[r % 4].toString(), 2 + r % 30)
        }
        return real + fake.filter { f -> real.none { it.track == f.track && it.year == f.year && it.section == f.section } }
    }

    /** Sample "where are they now" cards and memories, so the demo batch feels like a real class. */
    private val cards = listOf(
        Triple("Chennai", "Doctor at a city hospital", "Bunking PT period to watch the cricket final 🏏"),
        Triple("Bengaluru", "Software engineer", "Mr. Raman's surprise physics tests 😅"),
        Triple("Dubai", "Runs a bakery", "Sharing lunch boxes under the neem tree"),
        Triple("Kumbakonam", "Teacher — at our own school!", "Annual day drama where the curtain fell 🎭"),
        Triple("Singapore", "Product manager", ""),
        Triple("Coimbatore", "", "Cycle races after the last bell"),
    )

    override suspend fun joinBatch(school: School, key: BatchKey, user: AppUser): Result<Group> {
        delay(700)
        val existing = groups.value[key.groupId]
        val stat = batchStats(school.id).firstOrNull { it.track.code == key.track.code && it.year == key.year && it.section == key.section }
        val seeded = (stat?.members ?: 0).coerceAtMost(friends.size)
        val group = existing ?: Group(
            id = key.groupId, schoolId = school.id, schoolName = school.name, regionLabel = school.region.label,
            track = key.track, year = key.year, section = key.section, inviteCode = InviteCodes.generate(),
            memberCount = seeded + 1, createdBy = if (seeded > 0) "f0" else user.uid,
        ).also { g ->
            codes[g.inviteCode] = g.id
            val now = System.currentTimeMillis()
            members.value = members.value + (g.id to (friends.take(seeded).mapIndexed { i, n ->
                val c = cards.getOrNull(i)
                Member("f$i", n, null, now - (i + 1) * 86_400_000L, c?.first.orEmpty(), c?.second.orEmpty(), c?.third.orEmpty(),
                    invites = if (i == 0) 6 else i % 3)
            } + Member(user.uid, user.name, user.photoUrl, now)))
            val years = 2026 - key.year
            messages.value = messages.value + (g.id to if (seeded > 0) listOf(
                Message("m1", "f0", friends[0], null, "Can't believe it's been $years years! 😍", now - 3_600_000,
                    reactions = mapOf("❤️" to listOf("f1", "f2"), "😭" to listOf("f3"))),
                Message("m2", "f1", friends[1], null, "Mr. Raman, no contest. Still dream about his surprise tests.", now - 2_400_000,
                    kind = MessageKind.PROMPT, prompt = "Who was the strictest teacher?", reactions = mapOf("😂" to listOf("f0", "f2", "f4"))),
                Message("m3", "f2", friends[2], null, "Who still has the class photo? 📸", now - 1_800_000),
                Message("m4", user.uid, user.name, user.photoUrl, "${user.name} joined the batch 👋", now, kind = MessageKind.JOINED),
            ) else listOf(Message("m4", user.uid, user.name, user.photoUrl, "${user.name} started this batch 🌱", now, kind = MessageKind.JOINED)))
            if (seeded > 0) missing.value = missing.value + (g.id to listOf(
                MissingPerson("x1", "Ganesh", "cricket captain, sat on the last bench", "f0", friends[0], invited = 1),
                MissingPerson("x2", "Fathima R", "class topper, moved to Pune?", "f1", friends[1]),
                MissingPerson("x3", "Joseph", "always late, always smiling", "f2", friends[2]),
                MissingPerson("x4", "Revathi", "best handwriting in the class", "f3", friends.getOrElse(3) { friends[0] }),
            ))
        }
        groups.value = groups.value + (group.id to group)
        if (group.id !in mine.value) mine.value = listOf(group.id) + mine.value
        return Result.success(group)
    }

    override suspend fun joinByInviteCode(code: String, user: AppUser): Result<Group> {
        val id = codes[InviteCodes.normalize(code)]
            ?: return Result.failure(IllegalArgumentException("Demo mode: create a batch first, then use its code."))
        if (id !in mine.value) mine.value = listOf(id) + mine.value
        return Result.success(groups.value.getValue(id))
    }

    override suspend fun leaveGroup(groupId: String, user: AppUser): Result<Unit> {
        mine.value = mine.value - groupId
        return Result.success(Unit)
    }

    override fun group(groupId: String): Flow<Group?> = groups.map { it[groupId] }
    override fun members(groupId: String): Flow<List<Member>> = members.map { it[groupId].orEmpty() }
    override fun messages(groupId: String): Flow<List<Message>> =
        messages.map { all -> all[groupId].orEmpty().sortedByDescending { it.sentAt } }

    private fun post(groupId: String, msg: Message) {
        messages.value = messages.value + (groupId to (messages.value[groupId].orEmpty() + msg))
    }

    override suspend fun sendMessage(groupId: String, text: String, user: AppUser): Result<Unit> {
        post(groupId, Message(UUID.randomUUID().toString(), user.uid, user.name, user.photoUrl, text.trim(), System.currentTimeMillis()))
        return Result.success(Unit)
    }

    override suspend fun setWhatsappLink(groupId: String, link: String?): Result<Unit> {
        groups.value[groupId]?.let { groups.value = groups.value + (groupId to it.copy(whatsappLink = link?.ifBlank { null })) }
        return Result.success(Unit)
    }

    // ---- engagement (in memory) ----

    private val missing = MutableStateFlow<Map<String, List<MissingPerson>>>(emptyMap())
    private val reunions = MutableStateFlow<Map<String, Reunion>>(emptyMap())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun invitePreview(code: String): InvitePreview? {
        val g = codes[InviteCodes.normalize(code)]?.let { groups.value[it] } ?: return null
        return InvitePreview(g.inviteCode, g.id, g.schoolName, g.title, g.regionLabel, g.memberCount,
            members.value[g.id].orEmpty().map { it.name.substringBefore(' ') })
    }

    override fun missing(groupId: String): Flow<List<MissingPerson>> = missing.map { it[groupId].orEmpty() }

    override suspend fun addMissing(groupId: String, name: String, note: String, user: AppUser): Result<Unit> {
        val p = MissingPerson(UUID.randomUUID().toString(), name.trim(), note.trim(), user.uid, user.name)
        missing.value = missing.value + (groupId to (listOf(p) + missing.value[groupId].orEmpty()))
        return Result.success(Unit)
    }

    /** Demo magic: a few seconds after you invite someone, they "join", so the whole loop can be felt. */
    override suspend fun markInvited(groupId: String, missingId: String): Result<Unit> {
        updateMissing(groupId, missingId) { it.copy(invited = it.invited + 1) }
        scope.launch {
            delay(6_000)
            val person = missing.value[groupId].orEmpty().firstOrNull { it.id == missingId && !it.found } ?: return@launch
            val uid = "demo-" + missingId.take(6)
            updateMissing(groupId, missingId) { it.copy(foundUid = uid) }
            members.value = members.value + (groupId to (members.value[groupId].orEmpty() +
                Member(uid, person.name, null, System.currentTimeMillis(), "Hyderabad", "Just joined — say hi!", "")))
            groups.value[groupId]?.let { g -> groups.value = groups.value + (groupId to g.copy(memberCount = g.memberCount + 1)) }
            post(groupId, Message(UUID.randomUUID().toString(), uid, person.name, null,
                "${person.name} is back! 🎉 ${person.addedByName} was looking for them.", System.currentTimeMillis(), kind = MessageKind.FOUND))
        }
        return Result.success(Unit)
    }

    private fun updateMissing(groupId: String, id: String, f: (MissingPerson) -> MissingPerson) {
        missing.value = missing.value + (groupId to missing.value[groupId].orEmpty().map { if (it.id == id) f(it) else it })
    }

    override suspend fun removeMissing(groupId: String, missingId: String): Result<Unit> {
        missing.value = missing.value + (groupId to missing.value[groupId].orEmpty().filter { it.id != missingId })
        return Result.success(Unit)
    }

    private fun updateMember(groupId: String, uid: String, f: (Member) -> Member) {
        members.value = members.value + (groupId to members.value[groupId].orEmpty().map { if (it.uid == uid) f(it) else it })
    }

    override suspend fun updateMyCard(groupId: String, user: AppUser, city: String, now: String, memory: String): Result<Unit> {
        updateMember(groupId, user.uid) { it.copy(city = city.trim(), now = now.trim(), memory = memory.trim()) }
        return Result.success(Unit)
    }

    override suspend fun recordShare(groupId: String, user: AppUser): Result<Unit> {
        updateMember(groupId, user.uid) { it.copy(invites = it.invites + 1) }
        return Result.success(Unit)
    }

    override suspend fun sendPrompt(groupId: String, prompt: String, answer: String, user: AppUser): Result<Unit> {
        post(groupId, Message(UUID.randomUUID().toString(), user.uid, user.name, user.photoUrl, answer.trim(),
            System.currentTimeMillis(), kind = MessageKind.PROMPT, prompt = prompt))
        return Result.success(Unit)
    }

    override suspend fun react(groupId: String, messageId: String, emoji: String, user: AppUser, on: Boolean): Result<Unit> {
        messages.value = messages.value + (groupId to messages.value[groupId].orEmpty().map { m ->
            if (m.id != messageId) m else {
                val list = m.reactions[emoji].orEmpty().let { if (on) (it + user.uid).distinct() else it - user.uid }
                m.copy(reactions = (m.reactions + (emoji to list)).filterValues { it.isNotEmpty() })
            }
        })
        return Result.success(Unit)
    }

    override fun reunion(groupId: String): Flow<Reunion?> = reunions.map { it[groupId] }

    override suspend fun proposeReunion(groupId: String, place: String, options: List<Long>, user: AppUser): Result<Unit> {
        // A couple of classmates vote straight away in the demo.
        val votes = members.value[groupId].orEmpty().filter { it.uid != user.uid }.take(4).mapIndexed { i, m -> m.uid to (i % options.size) }.toMap()
        val rsvp = members.value[groupId].orEmpty().filter { it.uid != user.uid }.take(3).associate { it.uid to Rsvp.YES }
        reunions.value = reunions.value + (groupId to Reunion(place.trim(), options.take(3), votes, rsvp, null, user.uid))
        return Result.success(Unit)
    }

    private fun updateReunion(groupId: String, f: (Reunion) -> Reunion): Result<Unit> {
        reunions.value[groupId]?.let { reunions.value = reunions.value + (groupId to f(it)) }
        return Result.success(Unit)
    }

    override suspend fun vote(groupId: String, option: Int, user: AppUser) = updateReunion(groupId) { it.copy(votes = it.votes + (user.uid to option)) }
    override suspend fun rsvp(groupId: String, status: String, user: AppUser) = updateReunion(groupId) { it.copy(rsvp = it.rsvp + (user.uid to status)) }
    override suspend fun lockReunion(groupId: String, date: Long?) = updateReunion(groupId) { it.copy(lockedDate = date) }
}
