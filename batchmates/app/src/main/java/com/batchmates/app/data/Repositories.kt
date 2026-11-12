package com.batchmates.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<AppUser?>
    /** True when the backend can do Google sign-in (needs a web client id from Firebase). */
    val googleAvailable: Boolean
    suspend fun signInWithGoogle(activityContext: Context): Result<AppUser>
    suspend fun signInWithEmail(email: String, password: String): Result<AppUser>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<AppUser>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun signOut()
}

interface ReunionRepository {
    fun myGroups(uid: String): Flow<List<Group>>

    /** Schools people created by hand in a region (shown next to map results). */
    suspend fun customSchools(region: Region): List<School>
    suspend fun createSchool(
        name: String, region: Region, lat: Double?, lon: Double?, user: AppUser,
        kind: InstitutionKind = InstitutionKind.SCHOOL, collegeType: CollegeType? = null,
    ): Result<School>

    /** Remembers another name for an existing school so the next person searching it finds it. */
    suspend fun addAlias(school: School, alias: String): Result<Unit>

    /** Alumni correct what kind of institution it is (e.g. "this is an Arts & Science college"). */
    suspend fun setInstitutionType(school: School, kind: InstitutionKind, collegeType: CollegeType?): Result<Unit>

    /** Existing groups at a school so we can show "12 classmates already here". */
    suspend fun batchStats(schoolId: String): List<BatchStat>

    suspend fun joinBatch(school: School, key: BatchKey, user: AppUser): Result<Group>
    suspend fun joinByInviteCode(code: String, user: AppUser): Result<Group>
    suspend fun leaveGroup(groupId: String, user: AppUser): Result<Unit>

    fun group(groupId: String): Flow<Group?>
    fun members(groupId: String): Flow<List<Member>>
    fun messages(groupId: String): Flow<List<Message>>
    suspend fun sendMessage(groupId: String, text: String, user: AppUser): Result<Unit>
    suspend fun setWhatsappLink(groupId: String, link: String?): Result<Unit>

    // ---- engagement ----

    /** Public preview of an invite (works before sign-in). */
    suspend fun invitePreview(code: String): InvitePreview?

    fun missing(groupId: String): Flow<List<MissingPerson>>
    suspend fun addMissing(groupId: String, name: String, note: String, user: AppUser): Result<Unit>
    suspend fun markInvited(groupId: String, missingId: String): Result<Unit>
    suspend fun removeMissing(groupId: String, missingId: String): Result<Unit>

    /** "Where are you now" card on your membership. */
    suspend fun updateMyCard(groupId: String, user: AppUser, city: String, now: String, memory: String): Result<Unit>
    suspend fun recordShare(groupId: String, user: AppUser): Result<Unit>

    suspend fun sendPrompt(groupId: String, prompt: String, answer: String, user: AppUser): Result<Unit>
    suspend fun react(groupId: String, messageId: String, emoji: String, user: AppUser, on: Boolean): Result<Unit>

    fun reunion(groupId: String): Flow<Reunion?>
    suspend fun proposeReunion(groupId: String, place: String, options: List<Long>, user: AppUser): Result<Unit>
    suspend fun vote(groupId: String, option: Int, user: AppUser): Result<Unit>
    suspend fun rsvp(groupId: String, status: String, user: AppUser): Result<Unit>
    suspend fun lockReunion(groupId: String, date: Long?): Result<Unit>
}
