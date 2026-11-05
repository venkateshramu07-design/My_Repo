package com.randomthoughtsever.plainly.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Reads an article aloud with the phone's own text-to-speech voice (free, offline once the voice is installed, nothing
 * downloaded by the app). One paragraph at a time, so it can pause and resume where it was.
 */
class Speaker(context: Context) {
    var playing by mutableStateOf(false); private set
    var index by mutableIntStateOf(0); private set
    var parts: List<String> = emptyList(); private set
    var ready by mutableStateOf(false); private set
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                tts?.language = Locale.getDefault().takeIf { tts?.isLanguageAvailable(it) ?: -1 >= 0 } ?: Locale.US
                tts?.setSpeechRate(1.0f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String) { index = id.toIntOrNull() ?: index }
                    override fun onDone(id: String) { if (id.toIntOrNull() == parts.lastIndex) { playing = false; index = 0 } }
                    @Deprecated("old API") override fun onError(id: String) { playing = false }
                })
            }
        }
    }

    fun load(text: List<String>) { parts = text.filter { it.isNotBlank() }; index = 0 }
    /** Reads in another language (after the article is translated), if the phone has that voice. */
    fun language(tag: String?) {
        val l = if (tag == null) Locale.getDefault() else Locale.forLanguageTag(tag)
        tts?.language = l.takeIf { (tts?.isLanguageAvailable(it) ?: -1) >= 0 } ?: Locale.US
    }

    fun play() {
        val t = tts ?: return
        if (!ready || parts.isEmpty()) return
        playing = true
        parts.drop(index).forEachIndexed { k, s -> t.speak(s, if (k == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, (index + k).toString()) }
    }
    fun pause() { tts?.stop(); playing = false }
    fun stop() { tts?.stop(); playing = false; index = 0 }
    fun skip(by: Int) { val was = playing; tts?.stop(); index = (index + by).coerceIn(0, (parts.size - 1).coerceAtLeast(0)); if (was) play() }
    fun release() { tts?.stop(); tts?.shutdown(); tts = null }
}

/** The plain text of an article's blocks, in reading order, for the speaker. */
fun spokenParts(title: String, blocks: List<Block>): List<String> = listOf(title) + blocks.flatMap { b ->
    when (b) {
        is Block.Heading -> listOf(b.text.text)
        is Block.Para -> listOf(b.text.text)
        is Block.QuickAnswer -> listOf("The quick answer. " + b.text.text)
        is Block.Bullets -> b.items.map { it.second.text }
        is Block.Quote -> listOf(b.text.text)
        is Block.Faq -> listOf(b.question) + spokenParts("", b.answer).drop(1)
        else -> emptyList()
    }
}
