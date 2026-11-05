package com.randomthoughtsever.plainly

import androidx.test.core.app.ApplicationProvider
import com.randomthoughtsever.plainly.data.Community
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.HttpURLConnection
import java.net.URL

/**
 * Runs the community against the Firebase emulators with the real security rules (firebase/firestore.rules):
 *   cd firebase && firebase emulators:start --only auth,firestore --project demo-plainly
 * Two anonymous people post, like, comment and vote; then the test tries to cheat and checks the rules refuse.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CommunityTest {
    @Before fun setUp() {
        val up = runCatching { (URL("http://127.0.0.1:8080/").openConnection() as HttpURLConnection).responseCode == 200 }.getOrDefault(false)
        assumeTrue("Firebase emulators not running", up)
        // Fresh database for each run.
        (URL("http://127.0.0.1:8080/emulator/v1/projects/demo-plainly/databases/(default)/documents").openConnection() as HttpURLConnection).apply { requestMethod = "DELETE" }.responseCode
        Community.projectId = "demo-plainly"; Community.apiKey = "fake-api-key"
        Community.authBase = "http://127.0.0.1:9099/identitytoolkit.googleapis.com"
        Community.tokenBase = "http://127.0.0.1:9099/securetoken.googleapis.com"
        Community.firestoreBase = "http://127.0.0.1:8080"
        Community.init(ApplicationProvider.getApplicationContext())
        Community.resetSession()
    }

    private fun refused(block: suspend () -> Unit): Boolean = runBlocking {
        runCatching { block() }.exceptionOrNull().let { it is Community.CommunityError && (it.code == 403 || it.code == 400 || it.code == 409) }
    }

    @Test fun twoPeopleUseTheCommunityAndCheatingIsRefused() = runBlocking {
        // Person A posts and likes their post once.
        val a = Community.createPost("Calm Otter 12", "🦦", "Paying the smallest card first kept me motivated.", "Money")
        val aId = Community.uid!!
        assertEquals(a.id, Community.feed().first().id)
        Community.like(a.id, true)
        assertEquals(1, Community.getPost(a.id)!!.likes)
        assertTrue("a second like by the same person must be refused", refused { Community.like(a.id, true) })
        Community.voteNow("efund", 1)
        assertTrue("voting twice must be refused", refused { Community.voteNow("efund", 2) })

        // Person B comments twice, likes, and votes.
        Community.resetSession()
        Community.comment(a.id, "Bright Falcon 7", "🦅", "Same here, the snowball method works.")
        Community.comment(a.id, "Bright Falcon 7", "🦅", "And automate the payments.")
        val bId = Community.uid!!
        assertTrue(aId != bId)
        assertEquals(2, Community.getPost(a.id)!!.comments)
        assertEquals(listOf("${bId}_1", "${bId}_2"), Community.comments(a.id).map { it.id })
        Community.like(a.id, true)
        assertEquals(2, Community.getPost(a.id)!!.likes)
        assertEquals(setOf(a.id), Community.liked(listOf(a.id)))
        Community.voteNow("efund", 1)
        assertEquals(2, Community.poll("efund")[1])
        Community.like(a.id, false)
        assertEquals(1, Community.getPost(a.id)!!.likes)

        // Cheating, as B.
        val root = Community.documentRoot
        assertTrue("B can't delete A's post", refused { Community.deletePost(a.id) })
        assertTrue("likes can't be raised without a like", refused {
            Community.rawCommit(JSONObject().put("transform", JSONObject().put("document", "$root/posts/${a.id}")
                .put("fieldTransforms", JSONArray().put(JSONObject().put("fieldPath", "likes").put("increment", JSONObject().put("integerValue", "50"))))))
        })
        assertTrue("a post can't start with likes", refused {
            Community.rawCommit(JSONObject().put("update", JSONObject().put("name", "$root/posts/fake1").put("fields", JSONObject()
                .put("uid", JSONObject().put("stringValue", bId)).put("name", JSONObject().put("stringValue", "Cheater"))
                .put("avatar", JSONObject().put("stringValue", "😈")).put("text", JSONObject().put("stringValue", "hello there"))
                .put("topic", JSONObject().put("stringValue", "Money")).put("guide", JSONObject().put("stringValue", ""))
                .put("likes", JSONObject().put("integerValue", "999")).put("comments", JSONObject().put("integerValue", "0"))))
                .put("updateTransforms", JSONArray().put(JSONObject().put("fieldPath", "createdAt").put("setToServerValue", "REQUEST_TIME"))))
        })
        assertTrue("can't post as someone else", refused {
            Community.rawCommit(JSONObject().put("update", JSONObject().put("name", "$root/posts/fake2").put("fields", JSONObject()
                .put("uid", JSONObject().put("stringValue", aId)).put("name", JSONObject().put("stringValue", "Calm Otter 12"))
                .put("avatar", JSONObject().put("stringValue", "🦦")).put("text", JSONObject().put("stringValue", "I said something bad"))
                .put("topic", JSONObject().put("stringValue", "Money")).put("guide", JSONObject().put("stringValue", ""))
                .put("likes", JSONObject().put("integerValue", "0")).put("comments", JSONObject().put("integerValue", "0"))))
                .put("updateTransforms", JSONArray().put(JSONObject().put("fieldPath", "createdAt").put("setToServerValue", "REQUEST_TIME"))))
        })
        assertTrue("a poll total can't jump", refused {
            Community.rawCommit(JSONObject().put("transform", JSONObject().put("document", "$root/polls/efund")
                .put("fieldTransforms", JSONArray().put(JSONObject().put("fieldPath", "o0").put("increment", JSONObject().put("integerValue", "100"))))))
        })

        // Reports and the leaderboard.
        Community.report("posts/${a.id}", "Spam or selling")
        Community.publishStatsNow("Bright Falcon 7", "🦅", 9, 30)
        assertEquals("Bright Falcon 7", Community.leaders().first().name)
        assertTrue("streaks can't be absurd", refused { Community.publishStatsNow("Bright Falcon 7", "🦅", 99999, 1).also {
            Community.rawCommit(JSONObject().put("update", JSONObject().put("name", "$root/users/$bId").put("fields", JSONObject()
                .put("name", JSONObject().put("stringValue", "x")).put("avatar", JSONObject().put("stringValue", "🦅"))
                .put("streak", JSONObject().put("integerValue", "99999")).put("reads", JSONObject().put("integerValue", "1"))))
                .put("updateTransforms", JSONArray().put(JSONObject().put("fieldPath", "updatedAt").put("setToServerValue", "REQUEST_TIME"))))
        } })

        // B deletes everything they put in the community: comments and leaderboard row go; A's post stays.
        Community.deleteMe()
        assertTrue(Community.leaders().none { it.uid == bId })
        assertEquals(0, Community.comments(a.id).count { it.uid == bId })
        assertNotNull(Community.getPost(a.id))
        Unit
    }

    @Test fun postsAreCheckedBeforeSending() {
        assertNull(Community.problem("Automating my savings changed everything."))
        assertNotNull(Community.problem("x"))
        assertNotNull(Community.problem("Buy now at https://spam.example.com"))
        assertNull(Community.problem("This guide helped: https://randomthoughtsever.blogspot.com/2026/10/x.html"))
        assertNotNull(Community.problem("call me on +1 555 123 4567"))
        assertNotNull(Community.problem("you are a fucking idiot"))
    }
}
