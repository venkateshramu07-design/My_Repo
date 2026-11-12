package com.batchmates.app.data.firebase

import com.batchmates.app.data.AppUser
import com.batchmates.app.data.BatchKey
import com.batchmates.app.data.BatchStat
import com.batchmates.app.data.CollegeType
import com.batchmates.app.data.InstitutionKind
import com.batchmates.app.data.Track
import com.batchmates.app.data.Group
import com.batchmates.app.data.Member
import com.batchmates.app.data.Message
import com.batchmates.app.data.Region
import com.batchmates.app.data.ReunionRepository
import com.batchmates.app.data.School
import com.batchmates.app.data.SchoolSource
import com.batchmates.app.util.InviteCodes
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * Firestore layout (all on the free Spark plan):
 *  schools/{schoolId}
 *  groups/{groupId}                      groupId = schoolId_track_year_section
 *  groups/{groupId}/members/{uid}
 *  groups/{groupId}/messages/{auto}
 *  codes/{inviteCode} -> { groupId }
 *  users/{uid}/groups/{groupId}          the user's own group list
 */
class FirestoreReunionRepository : ReunionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val schools get() = db.collection("schools")
    private val groups get() = db.collection("groups")
    private val codes get() = db.collection("codes")
    private fun myGroupsRef(uid: String) = db.collection("users").document(uid).collection("groups")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun myGroups(uid: String): Flow<List<Group>> =
        listen(myGroupsRef(uid).orderBy("joinedAt", Query.Direction.DESCENDING)) { snap ->
            snap.documents.map { it.id }
        }.flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf(emptyList())
            else combine(ids.map { group(it) }) { arr -> arr.filterNotNull() }
        }

    override suspend fun customSchools(region: Region): List<School> = runCatching {
        schools.whereEqualTo("countryCode", region.countryCode)
            .whereEqualTo("state", region.state)
            .whereEqualTo("district", region.district)
            .limit(400)
            .get().await().documents.mapNotNull { it.toSchool() }
    }.getOrDefault(emptyList())

    override suspend fun createSchool(
        name: String, region: Region, lat: Double?, lon: Double?, user: AppUser,
        kind: InstitutionKind, collegeType: CollegeType?,
    ): Result<School> = runCatching {
        val ref = schools.document()
        val school = School(ref.id, name.trim(), region, lat, lon, SchoolSource.CUSTOM, locality = region.town, kind = kind, collegeType = collegeType)
        ref.set(school.toMap() + ("createdBy" to user.uid) + ("createdAt" to FieldValue.serverTimestamp())).await()
        school
    }

    override suspend fun addAlias(school: School, alias: String): Result<Unit> = runCatching {
        val clean = alias.trim().take(120)
        if (clean.isBlank() || clean.equals(school.name, ignoreCase = true)) return@runCatching
        val ref = schools.document(school.id)
        db.runTransaction { tx ->
            if (tx.get(ref).exists()) {
                tx.update(ref, "aliases", FieldValue.arrayUnion(clean))
            } else {
                tx.set(ref, school.toMap() + ("aliases" to listOf(clean)) + ("memberCount" to 0L) + ("createdAt" to FieldValue.serverTimestamp()))
            }
        }.await()
        Unit
    }

    override suspend fun setInstitutionType(school: School, kind: InstitutionKind, collegeType: CollegeType?): Result<Unit> = runCatching {
        val ref = schools.document(school.id)
        val fields = mapOf("kind" to kind.name, "collegeType" to collegeType?.name)
        db.runTransaction { tx ->
            if (tx.get(ref).exists()) tx.update(ref, fields)
            else tx.set(ref, school.toMap() + fields + ("memberCount" to 0L) + ("createdAt" to FieldValue.serverTimestamp()))
        }.await()
        Unit
    }

    override suspend fun batchStats(schoolId: String): List<BatchStat> = runCatching {
        groups.whereEqualTo("schoolId", schoolId).limit(300).get().await().documents.map {
            BatchStat(
                it.readTrack(),
                it.getLong("year")?.toInt() ?: 0,
                it.getString("section").orEmpty(),
                it.getLong("memberCount")?.toInt() ?: 0,
            )
        }
    }.getOrDefault(emptyList())

    override suspend fun joinBatch(school: School, key: BatchKey, user: AppUser): Result<Group> = runCatching {
        val groupRef = groups.document(key.groupId)
        val schoolRef = schools.document(school.id)
        // Pick a free invite code up-front (reads are not allowed after writes inside a transaction).
        var candidate = InviteCodes.generate()
        repeat(3) { if (codes.document(candidate).get().await().exists()) candidate = InviteCodes.generate() }

        db.runTransaction { tx ->
            val g = tx.get(groupRef)
            val s = tx.get(schoolRef)
            val m = tx.get(groupRef.collection("members").document(user.uid))
            val isNewMember = !m.exists()

            if (!s.exists()) {
                tx.set(schoolRef, school.toMap() + ("memberCount" to 1L) + ("createdAt" to FieldValue.serverTimestamp()))
            } else if (isNewMember) {
                tx.update(schoolRef, "memberCount", FieldValue.increment(1))
            }
            val code = if (g.exists()) g.getString("inviteCode")!! else candidate
            if (!g.exists()) {
                tx.set(
                    groupRef,
                    mapOf(
                        "schoolId" to school.id,
                        "schoolName" to school.name,
                        "regionLabel" to school.region.label,
                        "track" to key.track.code,
                        "trackLabel" to key.track.label,
                        "trackShort" to key.track.short,
                        "kind" to school.kind.name,
                        "grade" to key.track.code.toIntOrNull(), // legacy field for school batches
                        "year" to key.year,
                        "section" to key.section.uppercase(),
                        "inviteCode" to code,
                        "memberCount" to 1L,
                        "createdBy" to user.uid,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(codes.document(code), mapOf("groupId" to key.groupId))
            } else if (isNewMember) {
                tx.update(groupRef, "memberCount", FieldValue.increment(1))
            }
            if (isNewMember) addMember(tx, groupRef.id, user)
        }.await()
        group(key.groupId).firstNonNull()
    }

    override suspend fun joinByInviteCode(code: String, user: AppUser): Result<Group> = runCatching {
        val groupId = codes.document(InviteCodes.normalize(code)).get().await().getString("groupId")
            ?: error("No batch found for code \"$code\". Double-check it with your friend.")
        val groupRef = groups.document(groupId)
        db.runTransaction { tx ->
            val g = tx.get(groupRef)
            check(g.exists()) { "This batch no longer exists." }
            val m = tx.get(groupRef.collection("members").document(user.uid))
            if (!m.exists()) {
                tx.update(groupRef, "memberCount", FieldValue.increment(1))
                tx.update(schools.document(g.getString("schoolId")!!), "memberCount", FieldValue.increment(1))
                addMember(tx, groupId, user)
            }
        }.await()
        group(groupId).firstNonNull()
    }

    private fun addMember(tx: com.google.firebase.firestore.Transaction, groupId: String, user: AppUser) {
        tx.set(
            groups.document(groupId).collection("members").document(user.uid),
            mapOf("name" to user.name, "photoUrl" to user.photoUrl, "joinedAt" to FieldValue.serverTimestamp()),
        )
        tx.set(myGroupsRef(user.uid).document(groupId), mapOf("joinedAt" to FieldValue.serverTimestamp()))
    }

    override suspend fun leaveGroup(groupId: String, user: AppUser): Result<Unit> = runCatching {
        val groupRef = groups.document(groupId)
        db.runTransaction { tx ->
            val m = tx.get(groupRef.collection("members").document(user.uid))
            if (m.exists()) {
                tx.delete(m.reference)
                tx.update(groupRef, "memberCount", FieldValue.increment(-1))
            }
            tx.delete(myGroupsRef(user.uid).document(groupId))
        }.await()
        Unit
    }

    override fun group(groupId: String): Flow<Group?> = listenDoc(groups.document(groupId)) { it.toGroup() }

    override fun members(groupId: String): Flow<List<Member>> =
        listen(groups.document(groupId).collection("members").orderBy("joinedAt").limit(500)) { snap ->
            snap.documents.map {
                Member(
                    it.id, it.getString("name") ?: "Friend", it.getString("photoUrl"),
                    it.getTimestamp("joinedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time ?: 0L,
                )
            }
        }

    override fun messages(groupId: String): Flow<List<Message>> =
        listen(
            groups.document(groupId).collection("messages")
                .orderBy("sentAt", Query.Direction.DESCENDING).limit(150),
        ) { snap ->
            snap.documents.map {
                Message(
                    it.id, it.getString("uid").orEmpty(), it.getString("name") ?: "Friend", it.getString("photoUrl"),
                    it.getString("text").orEmpty(),
                    it.getTimestamp("sentAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time
                        ?: System.currentTimeMillis(),
                )
            }
        }

    override suspend fun sendMessage(groupId: String, text: String, user: AppUser): Result<Unit> = runCatching {
        groups.document(groupId).collection("messages").add(
            mapOf(
                "uid" to user.uid, "name" to user.name, "photoUrl" to user.photoUrl,
                "text" to text.trim().take(1000), "sentAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
        Unit
    }

    override suspend fun setWhatsappLink(groupId: String, link: String?): Result<Unit> = runCatching {
        groups.document(groupId).set(mapOf("whatsappLink" to link?.trim()?.ifBlank { null }), SetOptions.merge()).await()
        Unit
    }

    // ---- mapping helpers ----

    private fun School.toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "countryCode" to region.countryCode,
        "country" to region.country,
        "state" to region.state,
        "district" to region.district,
        "town" to region.town,
        "locality" to locality,
        "lat" to lat,
        "lon" to lon,
        "source" to if (source == SchoolSource.CUSTOM) "custom" else "osm",
        "kind" to kind.name,
        "collegeType" to collegeType?.name,
    )

    private fun DocumentSnapshot.readTrack(): Track {
        val code = getString("track") ?: getLong("grade")?.toString() ?: "10"
        if ('.' !in code) return Track.school(code)
        return Track(code, getString("trackLabel") ?: code, getString("trackShort") ?: getString("trackLabel") ?: code)
    }

    private fun DocumentSnapshot.toSchool(): School? {
        if (!exists()) return null
        return School(
            id = id,
            name = getString("name") ?: return null,
            region = Region(
                getString("countryCode").orEmpty(), getString("country").orEmpty(),
                getString("state").orEmpty(), getString("district").orEmpty(), getString("town").orEmpty(),
            ),
            lat = getDouble("lat"),
            lon = getDouble("lon"),
            source = if (getString("source") == "custom") SchoolSource.CUSTOM else SchoolSource.OSM,
            memberCount = getLong("memberCount")?.toInt() ?: 0,
            locality = getString("locality") ?: getString("town").orEmpty(),
            aliases = (get("aliases") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            kind = getString("kind")?.let { k -> runCatching { InstitutionKind.valueOf(k) }.getOrNull() }
                ?: com.batchmates.app.data.Classifier.kind(getString("name").orEmpty()),
            collegeType = getString("collegeType")?.let { t -> runCatching { CollegeType.valueOf(t) }.getOrNull() },
        )
    }

    private fun DocumentSnapshot.toGroup(): Group? {
        if (!exists()) return null
        return Group(
            id = id,
            schoolId = getString("schoolId").orEmpty(),
            schoolName = getString("schoolName").orEmpty(),
            regionLabel = getString("regionLabel").orEmpty(),
            track = readTrack(),
            year = getLong("year")?.toInt() ?: 0,
            section = getString("section").orEmpty(),
            inviteCode = getString("inviteCode").orEmpty(),
            memberCount = getLong("memberCount")?.toInt() ?: 0,
            whatsappLink = getString("whatsappLink"),
            createdBy = getString("createdBy").orEmpty(),
        )
    }

    private fun <T> listen(query: Query, map: (com.google.firebase.firestore.QuerySnapshot) -> T): Flow<T> = callbackFlow {
        val reg = query.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            if (snap != null) trySend(map(snap))
        }
        awaitClose { reg.remove() }
    }

    private fun <T> listenDoc(
        ref: com.google.firebase.firestore.DocumentReference,
        map: (DocumentSnapshot) -> T,
    ): Flow<T> = callbackFlow {
        val reg = ref.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            if (snap != null) trySend(map(snap))
        }
        awaitClose { reg.remove() }
    }

    private suspend fun Flow<Group?>.firstNonNull(): Group = filterNotNull().first()
}
