package com.randomthoughtsever.plainly.data

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.randomthoughtsever.plainly.ui.Block
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Article translation on the phone (Google ML Kit). Each language's model downloads once (about 30 MB), then
 * translation works offline and nothing about what you read leaves the phone. Machine translation, so the app
 * always says so and offers the original.
 */
object Translate {
    data class Lang(val code: String, val native: String, val english: String, val rtl: Boolean = false)

    val LANGS = listOf(
        Lang(TranslateLanguage.SPANISH, "Español", "Spanish"), Lang(TranslateLanguage.HINDI, "हिन्दी", "Hindi"),
        Lang(TranslateLanguage.FRENCH, "Français", "French"), Lang(TranslateLanguage.GERMAN, "Deutsch", "German"),
        Lang(TranslateLanguage.PORTUGUESE, "Português", "Portuguese"), Lang(TranslateLanguage.ARABIC, "العربية", "Arabic", rtl = true),
        Lang(TranslateLanguage.CHINESE, "中文", "Chinese"), Lang(TranslateLanguage.JAPANESE, "日本語", "Japanese"),
        Lang(TranslateLanguage.KOREAN, "한국어", "Korean"), Lang(TranslateLanguage.INDONESIAN, "Bahasa Indonesia", "Indonesian"),
        Lang(TranslateLanguage.RUSSIAN, "Русский", "Russian"), Lang(TranslateLanguage.BENGALI, "বাংলা", "Bengali"),
        Lang(TranslateLanguage.TAMIL, "தமிழ்", "Tamil"), Lang(TranslateLanguage.TELUGU, "తెలుగు", "Telugu"),
        Lang(TranslateLanguage.MARATHI, "मराठी", "Marathi"), Lang(TranslateLanguage.GUJARATI, "ગુજરાતી", "Gujarati"),
        Lang(TranslateLanguage.KANNADA, "ಕನ್ನಡ", "Kannada"), Lang(TranslateLanguage.URDU, "اردو", "Urdu", rtl = true),
        Lang(TranslateLanguage.TURKISH, "Türkçe", "Turkish"), Lang(TranslateLanguage.ITALIAN, "Italiano", "Italian"),
        Lang(TranslateLanguage.VIETNAMESE, "Tiếng Việt", "Vietnamese"), Lang(TranslateLanguage.THAI, "ไทย", "Thai"),
        Lang(TranslateLanguage.POLISH, "Polski", "Polish"), Lang(TranslateLanguage.DUTCH, "Nederlands", "Dutch"),
        Lang(TranslateLanguage.SWAHILI, "Kiswahili", "Swahili"), Lang(TranslateLanguage.PERSIAN, "فارسی", "Persian", rtl = true),
    )
    fun lang(code: String?) = LANGS.firstOrNull { it.code == code }
    /** The phone's own language, if it's one we offer (and not English): suggested at the top of articles. */
    fun deviceLang(locale: Locale = Locale.getDefault()) = lang(locale.language)

    private fun prefs(c: Context) = c.getSharedPreferences("translate", Context.MODE_PRIVATE)
    fun preferred(c: Context): String? = prefs(c).getString("lang", null)
    fun setPreferred(c: Context, code: String?) = prefs(c).edit().putString("lang", code).apply()
    fun suggestionDismissed(c: Context) = prefs(c).getBoolean("noSuggest", false)
    fun dismissSuggestion(c: Context) = prefs(c).edit().putBoolean("noSuggest", true).apply()

    private val translators = ConcurrentHashMap<String, Translator>()
    private val cache = ConcurrentHashMap<String, String>()

    private fun client(code: String) = translators.getOrPut(code) {
        Translation.getClient(TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(code).build())
    }

    suspend fun isDownloaded(code: String): Boolean =
        runCatching { RemoteModelManager.getInstance().isModelDownloaded(TranslateRemoteModel.Builder(code).build()).await() }.getOrDefault(false)

    suspend fun download(code: String, wifiOnly: Boolean = false) {
        val conditions = DownloadConditions.Builder().apply { if (wifiOnly) requireWifi() }.build()
        client(code).downloadModelIfNeeded(conditions).await()
    }

    suspend fun text(code: String, s: String): String {
        if (s.isBlank()) return s
        val key = "$code\u0001$s"
        cache[key]?.let { return it }
        return client(code).translate(s).await().also { if (cache.size < 4000) cache[key] = it }
    }

    /** Downloaded languages other than English, for Settings. */
    suspend fun downloaded(): List<Lang> = runCatching {
        RemoteModelManager.getInstance().getDownloadedModels(TranslateRemoteModel::class.java).await().mapNotNull { lang(it.language) }
    }.getOrDefault(emptyList())

    suspend fun remove(code: String) {
        runCatching { RemoteModelManager.getInstance().deleteDownloadedModel(TranslateRemoteModel.Builder(code).build()).await() }
        translators.remove(code)?.close()
    }

    /**
     * Translates an article's blocks with [t] (one call per piece of text), keeping images, tables' shape and order.
     * Links and bold inside a paragraph are not kept in translations. Pure apart from [t], so it's tested with a fake.
     */
    suspend fun blocks(blocks: List<Block>, t: suspend (String) -> String, progress: (Int, Int) -> Unit = { _, _ -> }): List<Block> {
        val total = blocks.size
        return blocks.mapIndexed { i, b ->
            progress(i, total)
            when (b) {
                is Block.Heading -> Block.Heading(b.level, AnnotatedString(t(b.text.text)))
                is Block.Para -> Block.Para(AnnotatedString(t(b.text.text)))
                is Block.QuickAnswer -> Block.QuickAnswer(AnnotatedString(t(b.text.text)))
                is Block.Bullets -> Block.Bullets(b.ordered, b.items.map { (d, s) -> d to AnnotatedString(t(s.text)) })
                is Block.Quote -> Block.Quote(AnnotatedString(t(b.text.text)))
                is Block.Table -> Block.Table(b.header?.map { AnnotatedString(t(it.text)) }, b.rows.map { r -> r.map { AnnotatedString(t(it.text)) } })
                is Block.Faq -> Block.Faq(t(b.question), blocks(b.answer, t))
                is Block.Image -> b
                Block.Divider -> b
            }
        }.also { progress(total, total) }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { c ->
        addOnSuccessListener { c.resume(it) }
        addOnFailureListener { c.resumeWithException(it) }
        addOnCanceledListener { c.cancel() }
    }
}
