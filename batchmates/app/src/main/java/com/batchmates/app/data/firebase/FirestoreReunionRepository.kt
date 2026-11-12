package com.batchmates.app.data.firebase

import com.batchmates.app.data.AppUser
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

        val joinedNow = db.runTransaction { tx ->
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
                tx.set(
                    codes.document(code),
                    mapOf(
                        "groupId" to key.groupId,
                        "schoolName" to school.name,
                        "title" to "${key.track.short} · Class of ${key.year} · " +
                            if (key.section.uppercase() == com.batchmates.app.data.WHOLE_BATCH) "Whole batch" else "Section ${key.section.uppercase()}",
                        "regionLabel" to school.region.label,
                        "memberCount" to 1L,
                        "faces" to listOf(user.firstName()),
                    ),
                )
            } else if (isNewMember) {
                tx.update(groupRef, "memberCount", FieldValue.increment(1))
                tx.update(codes.document(code), mapOf("memberCount" to FieldValue.increment(1), "faces" to FieldValue.arrayUnion(user.firstName())))
            }
            if (isNewMember) addMember(tx, groupRef.id, user)
            isNewMember
        }.await()
        if (joinedNow) afterJoin(key.groupId, user)
        group(key.groupId).firstNonNull()
    }

    private fun AppUser.firstName() = name.trim().substringBefore(' ').take(20)

    /** Announce the newcomer, and if someone had them on the "missing" list, celebrate the find. */
    private suspend fun afterJoin(groupId: String, user: AppUser) {
        runCatching {
            val g = groups.document(groupId)
            val match = g.collection("missing").whereEqualTo("foundUid", null).limit(300).get().await().documents
                .firstOrNull { com.batchmates.app.util.SchoolNames.samePerson(it.getString("name").orEmpty(), user.name) }
            if (match != null) {
                match.reference.update("foundUid", user.uid).await()
                g.collection("messages").add(
                    systemMessage(user, MessageKind.FOUND, "${user.name} is back! 🎉 ${match.getString("addedByName") ?: "A classmate"} was looking for them."),
                ).await()
            } else {
                g.collection("messages").add(systemMessage(user, MessageKind.JOINED, "${user.name} joined the batch 👋")).await()
            }
        }
    }

    private fun systemMessage(user: AppUser, kind: MessageKind, text: String) = mapOf(
        "uid" to user.uid, "name" to user.name, "photoUrl" to user.photoUrl, "text" to text,
        "kind" to kind.name, "sentAt" to FieldValue.serverTimestamp(),
    )

    override suspend fun joinByInviteCode(code: String, user: AppUser): Result<Group> = runCatching {
        val groupId = codes.document(InviteCodes.normalize(code)).get().await().getString("groupId")
            ?: error("No batch found for code \"$code\". Double-check it with your friend.")
        val groupRef = groups.document(groupId)
        val joinedNow = db.runTransaction { tx ->
            val g = tx.get(groupRef)
            check(g.exists()) { "This batch no longer exists." }
            val m = tx.get(groupRef.collection("members").document(user.uid))
            if (!m.exists()) {
                tx.update(groupRef, "memberCount", FieldValue.increment(1))
                tx.update(schools.document(g.getString("schoolId")!!), "memberCount", FieldValue.increment(1))
                tx.update(codes.document(InviteCodes.normalize(code)),
                    mapOf("memberCount" to FieldValue.increment(1), "faces" to FieldValue.arrayUnion(user.firstName())))
                addMember(tx, groupId, user)
            }
            !m.exists()
        }.await()
        if (joinedNow) afterJoin(groupId, user)
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
                    city = it.getString("city").orEmpty(), now = it.getString("now").orEmpty(),
                    memory = it.getString("memory").orEmpty(), invites = it.getLong("invites")?.toInt() ?: 0,
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
                    kind = it.getString("kind")?.let { k -> runCatching { MessageKind.valueOf(k) }.getOrNull() } ?: MessageKind.TEXT,
                    prompt = it.getString("prompt"),
                    reactions = (it.get("reactions") as? Map<*, *>)?.mapNotNull { (k, v) ->
                        (k as? String)?.let { e -> e to (v as? List<*>)?.filterIsInstance<String>().orEmpty() }
                    }?.toMap().orEmpty(),
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

    // ---- engagement ----

    override suspend fun invitePreview(code: String): InvitePreview? = runCatching {
        val c = InviteCodes.normalize(code)
        val d = codes.document(c).get().await()
        if (!d.exists()) return@runCatching null
        InvitePreview(
            code = c,
            groupId = d.getString("groupId").orEmpty(),
            schoolName = d.getString("schoolName").orEmpty(),
            title = d.getString("title").orEmpty(),
            regionLabel = d.getString("regionLabel").orEmpty(),
            memberCount = d.getLong("memberCount")?.toInt() ?: 0,
            faces = (d.get("faces") as? List<*>)?.filterIsInstance<String>().orEmpty(),
        )
    }.getOrNull()

    override fun missing(groupId: String): Flow<List<MissingPerson>> =
        listen(groups.document(groupId).collection("missing").orderBy("addedAt", Query.Direction.DESCENDING).limit(300)) { snap ->
            snap.documents.map {
                MissingPerson(
                    it.id, it.getString("name").orEmpty(), it.getString("note").orEmpty(),
                    it.getString("addedBy").orEmpty(), it.getString("addedByName").orEmpty(),
                    it.getLong("invited")?.toInt() ?: 0, it.getString("foundUid"),
                )
            }
        }

    override suspend fun addMissing(groupId: String, name: String, note: String, user: AppUser): Result<Unit> = runCatching {
        groups.document(groupId).collection("missing").add(
            mapOf(
                "name" to name.trim().take(80), "note" to note.trim().take(120), "addedBy" to user.uid,
                "addedByName" to user.name, "invited" to 0L, "foundUid" to null, "addedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
        Unit
    }

    override suspend fun markInvited(groupId: String, missingId: String): Result<Unit> = runCatching {
        groups.document(groupId).collection("missing").document(missingId).update("invited", FieldValue.increment(1)).await()
        Unit
    }

    override suspend fun removeMissing(groupId: String, missingId: String): Result<Unit> = runCatching {
        groups.document(groupId).collection("missing").document(missingId).delete().await()
        Unit
    }

    override suspend fun updateMyCard(groupId: String, user: AppUser, city: String, now: String, memory: String): Result<Unit> = runCatching {
        groups.document(groupId).collection("members").document(user.uid).update(
            mapOf("city" to city.trim().take(60), "now" to now.trim().take(80), "memory" to memory.trim().take(200)),
        ).await()
        Unit
    }

    override suspend fun recordShare(groupId: String, user: AppUser): Result<Unit> = runCatching {
        groups.document(groupId).collection("members").document(user.uid).update("invites", FieldValue.increment(1)).await()
        Unit
    }

    override suspend fun sendPrompt(groupId: String, prompt: String, answer: String, user: AppUser): Result<Unit> = runCatching {
        groups.document(groupId).collection("messages").add(
            mapOf(
                "uid" to user.uid, "name" to user.name, "photoUrl" to user.photoUrl, "text" to answer.trim().take(1000),
                "kind" to MessageKind.PROMPT.name, "prompt" to prompt, "sentAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
        Unit
    }

    override suspend fun react(groupId: String, messageId: String, emoji: String, user: AppUser, on: Boolean): Result<Unit> = runCatching {
        groups.document(groupId).collection("messages").document(messageId).update(
            "reactions.$emoji", if (on) FieldValue.arrayUnion(user.uid) else FieldValue.arrayRemove(user.uid),
        ).await()
        Unit
    }

    private fun reunionRef(groupId: String) = groups.document(groupId).collection("meta").document("reunion")

    override fun reunion(groupId: String): Flow<Reunion?> = listenDoc(reunionRef(groupId)) { d ->
        if (!d.exists()) null else Reunion(
            place = d.getString("place").orEmpty(),
            options = (d.get("options") as? List<*>)?.mapNotNull { (it as? Number)?.toLong() }.orEmpty(),
            votes = (d.get("votes") as? Map<*, *>)?.mapNotNull { (k, v) -> (k as? String)?.let { it to ((v as? Number)?.toInt() ?: 0) } }?.toMap().orEmpty(),
            rsvp = (d.get("rsvp") as? Map<*, *>)?.mapNotNull { (k, v) -> (k as? String)?.let { it to (v as? String).orEmpty() } }?.toMap().orEmpty(),
            lockedDate = d.getLong("lockedDate"),
            createdBy = d.getString("createdBy").orEmpty(),
        )
    }

    override suspend fun proposeReunion(groupId: String, place: String, options: List<Long>, user: AppUser): Result<Unit> = runCatching {
        reunionRef(groupId).set(
            mapOf(
                "place" to place.trim().take(120), "options" to options.take(3), "votes" to emptyMap<String, Int>(),
                "rsvp" to emptyMap<String, String>(), "lockedDate" to null, "createdBy" to user.uid,
            ),
        ).await()
        Unit
    }

    override suspend fun vote(groupId: String, option: Int, user: AppUser): Result<Unit> = runCatching {
        reunionRef(groupId).update("votes.${user.uid}", option).await()
        Unit
    }

    override suspend fun rsvp(groupId: String, status: String, user: AppUser): Result<Unit> = runCatching {
        reunionRef(groupId).update("rsvp.${user.uid}", status).await()
        Unit
    }

    override suspend fun lockReunion(groupId: String, date: Long?): Result<Unit> = runCatching {
        reunionRef(groupId).update("lockedDate", date).await()
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
        val label = getString("trackLabel") ?: return Track.school(code)
        return Track(code, label, getString("trackShort") ?: label)
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
                ?: com.batchmates.app.data.Classifier.kind(getString("name").orEmpty(), null, getString("countryCode").orEmpty()),
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
