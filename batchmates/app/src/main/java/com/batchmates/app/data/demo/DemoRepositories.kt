package com.batchmates.app.data.demo

import android.content.Context
import com.batchmates.app.data.AppUser
import com.batchmates.app.data.AuthRepository
import com.batchmates.app.data.BatchKey
import com.batchmates.app.data.BatchStat
import com.batchmates.app.data.Grade
import com.batchmates.app.data.Group
import com.batchmates.app.data.Member
import com.batchmates.app.data.Message
import com.batchmates.app.data.Region
import com.batchmates.app.data.ReunionRepository
import com.batchmates.app.data.School
import com.batchmates.app.data.SchoolSource
import com.batchmates.app.util.InviteCodes
import kotlinx.coroutines.delay
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
        it.region.countryCode == region.countryCode && it.region.state == region.state && it.region.district == region.district
    }

    override suspend fun createSchool(name: String, region: Region, lat: Double?, lon: Double?, user: AppUser): Result<School> {
        val s = School(UUID.randomUUID().toString().take(12), name.trim(), region, lat, lon, SchoolSource.CUSTOM)
        customSchools.value = customSchools.value + s
        return Result.success(s)
    }

    /** Pretend a few classmates are already waiting, so the demo feels alive. */
    override suspend fun batchStats(schoolId: String): List<BatchStat> {
        delay(250)
        val seed = schoolId.hashCode()
        val real = groups.value.values.filter { it.schoolId == schoolId }
            .map { BatchStat(it.grade, it.year, it.section, it.memberCount) }
        val fake = (0 until 6).map { i ->
            val r = (seed xor (i * 7919)).let { if (it < 0) -it else it }
            BatchStat(if (i % 2 == 0) Grade.TENTH else Grade.TWELFTH, 2000 + r % 25, "ABCD"[r % 4].toString(), 2 + r % 30)
        }
        return real + fake.filter { f -> real.none { it.grade == f.grade && it.year == f.year && it.section == f.section } }
    }

    override suspend fun joinBatch(school: School, key: BatchKey, user: AppUser): Result<Group> {
        delay(700)
        val existing = groups.value[key.groupId]
        val stat = batchStats(school.id).firstOrNull { it.grade == key.grade && it.year == key.year && it.section == key.section }
        val seeded = stat?.members ?: 0
        val group = existing ?: Group(
            id = key.groupId, schoolId = school.id, schoolName = school.name, regionLabel = school.region.label,
            grade = key.grade, year = key.year, section = key.section, inviteCode = InviteCodes.generate(),
            memberCount = seeded + 1, createdBy = user.uid,
        ).also { g ->
            codes[g.inviteCode] = g.id
            val now = System.currentTimeMillis()
            members.value = members.value + (g.id to (friends.take(seeded.coerceAtMost(friends.size)).mapIndexed { i, n ->
                Member("f$i", n, null, now - (i + 1) * 86_400_000L)
            } + Member(user.uid, user.name, user.photoUrl, now)))
            messages.value = messages.value + (g.id to if (seeded > 0) listOf(
                Message("m1", "f0", friends[0], null, "Can't believe it's been ${2026 - key.year} years! 😍", now - 3_600_000),
                Message("m2", "f1", friends[1], null, "Who still has the class photo? 📸", now - 1_800_000),
            ) else emptyList())
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

    override suspend fun sendMessage(groupId: String, text: String, user: AppUser): Result<Unit> {
        val msg = Message(UUID.randomUUID().toString(), user.uid, user.name, user.photoUrl, text.trim(), System.currentTimeMillis())
        messages.value = messages.value + (groupId to (messages.value[groupId].orEmpty() + msg))
        return Result.success(Unit)
    }

    override suspend fun setWhatsappLink(groupId: String, link: String?): Result<Unit> {
        groups.value[groupId]?.let { groups.value = groups.value + (groupId to it.copy(whatsappLink = link?.ifBlank { null })) }
        return Result.success(Unit)
    }
}
