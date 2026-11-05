package com.randomthoughtsever.plainly

import androidx.test.core.app.ApplicationProvider
import com.randomthoughtsever.plainly.data.Repository
import com.randomthoughtsever.plainly.data.bloggerCover
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CoverTest {
    @Test fun bloggerImagesAreAskedForAtCoverSize() {
        val s = "w640-h336-p-k-no-nu-rw"
        assertEquals("https://blogger.googleusercontent.com/img/b/AB/$s/photo.jpg", bloggerCover("https://blogger.googleusercontent.com/img/b/AB/s72-c-rw/photo.jpg"))
        assertEquals("https://blogger.googleusercontent.com/img/b/AB/$s/photo.jpg", bloggerCover("https://blogger.googleusercontent.com/img/b/AB/s1600/photo.jpg"))
        assertEquals("https://blogger.googleusercontent.com/img/a/AVvX=$s", bloggerCover("https://blogger.googleusercontent.com/img/a/AVvX=w640-h400"))
        assertEquals("https://1.bp.blogspot.com/-x/y/z/$s/a.png", bloggerCover("https://1.bp.blogspot.com/-x/y/z/s320/a.png"))
        assertEquals("https://example.com/a.jpg", bloggerCover("https://example.com/a.jpg"))
    }

    /** The real index: nearly every guide should have a cover (designed, or the post's own photo), and the links must load. */
    @Test fun everyGuideInTheIndexHasACoverThatLoads() = runBlocking {
        val repo = Repository(ApplicationProvider.getApplicationContext())
        val index = repo.index(force = true)
        assumeTrue("network unavailable", index.isNotEmpty())
        val without = index.filter { it.image == null }
        println("COVERS ${index.size - without.size} of ${index.size} guides have a cover; without: ${without.take(8).map { it.title }}")
        assertTrue("too many guides without a cover: ${without.size}", without.size <= index.size / 30)
        val sample = index.mapNotNull { it.image }.filter { "googleusercontent" in it }.take(12) + index.mapNotNull { it.image }.filter { "jsdelivr" in it }.take(4)
        for (u in sample) {
            val c = URL(u).openConnection() as HttpURLConnection
            c.requestMethod = "GET"
            val code = c.responseCode; val type = c.contentType ?: ""; val size = c.inputStream.use { it.readBytes().size }
            assertEquals(u, 200, code); assertTrue("$u is $type", type.startsWith("image/")); assertTrue("$u is $size bytes", size in 1_000..150_000)
        }
    }
}
