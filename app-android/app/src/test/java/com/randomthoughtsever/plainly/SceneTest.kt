package com.randomthoughtsever.plainly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.takahirom.roborazzi.captureRoboImage
import com.randomthoughtsever.plainly.ui.PlainlyTheme
import com.randomthoughtsever.plainly.ui.TopicScene
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Draws every topic's motion scene at three moments of its loop (build/shots/scenes-*.png), to check them by eye. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h900dp-xxhdpi")
class SceneTest {
    @get:Rule val compose = createComposeRule()
    private val labels = listOf("Personal Finance", "Investing", "Economics", "Countries & Living Abroad", "Career & Workplace", "Psychology",
        "Self Improvement", "Health & Wellness", "Technology & AI", "Cryptocurrency", "Love & Relationships", "Zen & Mindfulness", "Leadership", "Education", "Other")

    @Test fun scenes() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            PlainlyTheme("light") {
                Column(Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    labels.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { l ->
                                Column {
                                    TopicScene(l, Modifier.width(126.dp).aspectRatio(1.6f).clip(RoundedCornerShape(12.dp)))
                                    Text(l, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        listOf(700L, 1700L, 1500L).forEachIndexed { i, step ->
            compose.mainClock.advanceTimeBy(step)
            compose.onRoot().captureRoboImage("build/shots/scenes-$i.png")
        }
        // Frames for an animated preview (build/shots/frames/), 8 per second over 5 seconds.
        repeat(40) { f ->
            compose.mainClock.advanceTimeBy(125)
            compose.onRoot().captureRoboImage("build/shots/frames/f%02d.png".format(f))
        }
    }
}
