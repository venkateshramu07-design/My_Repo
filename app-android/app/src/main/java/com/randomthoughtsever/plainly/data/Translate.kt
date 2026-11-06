package com.randomthoughtsever.plainly.data

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.randomthoughtsever.plainly.ui.Block
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** What the on-demand translation module provides (implemented by MlKitEngine in the :translate module). */
interface TranslateEngine {
    suspend fun isDownloaded(code: String): Boolean
    suspend fun download(code: String)
    suspend fun translate(code: String, text: String): String
    suspend fun downloaded(): List<String>
    suspend fun remove(code: String)
}

/**
 * Article translation on the phone (Google ML Kit). The translation engine is a separate on-demand part of the app
 * (the :translate module): Google Play downloads it only the first time someone taps Translate, so the app itself stays
 * small. Each language then downloads once (about 30 MB) and works offline; nothing about what you read leaves the phone.
 * Machine translation, so the app always says so and offers the original.
 */
object Translate {
    data class Lang(val code: String, val native: String, val english: String, val rtl: Boolean = false)

    /** ML Kit's language codes (BCP-47). */
    val LANGS = listOf(
        Lang("es", "Español", "Spanish"), Lang("hi", "हिन्दी", "Hindi"), Lang("fr", "Français", "French"), Lang("de", "Deutsch", "German"),
        Lang("pt", "Português", "Portuguese"), Lang("ar", "العربية", "Arabic", rtl = true), Lang("zh", "中文", "Chinese"), Lang("ja", "日本語", "Japanese"),
        Lang("ko", "한국어", "Korean"), Lang("id", "Bahasa Indonesia", "Indonesian"), Lang("ru", "Русский", "Russian"), Lang("bn", "বাংলা", "Bengali"),
        Lang("ta", "தமிழ்", "Tamil"), Lang("te", "తెలుగు", "Telugu"), Lang("mr", "मराठी", "Marathi"), Lang("gu", "ગુજરાતી", "Gujarati"),
        Lang("kn", "ಕನ್ನಡ", "Kannada"), Lang("ur", "اردو", "Urdu", rtl = true), Lang("tr", "Türkçe", "Turkish"), Lang("it", "Italiano", "Italian"),
        Lang("vi", "Tiếng Việt", "Vietnamese"), Lang("th", "ไทย", "Thai"), Lang("pl", "Polski", "Polish"), Lang("nl", "Nederlands", "Dutch"),
        Lang("sw", "Kiswahili", "Swahili"), Lang("fa", "فارسی", "Persian", rtl = true),
    )
    fun lang(code: String?) = LANGS.firstOrNull { it.code == code }
    /** The phone's own language, if it's one we offer (and not English): suggested at the top of articles. */
    fun deviceLang(locale: Locale = Locale.getDefault()) = lang(locale.language)

    private fun prefs(c: Context) = c.getSharedPreferences("translate", Context.MODE_PRIVATE)
    fun preferred(c: Context): String? = prefs(c).getString("lang", null)
    fun setPreferred(c: Context, code: String?) = prefs(c).edit().putString("lang", code).apply()
    fun suggestionDismissed(c: Context) = prefs(c).getBoolean("noSuggest", false)
    fun dismissSuggestion(c: Context) = prefs(c).edit().putBoolean("noSuggest", true).apply()

    // ---------- the on-demand engine ----------

    const val MODULE = "translate"
    private const val ENGINE = "com.randomthoughtsever.plainly.translate.MlKitEngine"
    @Volatile private var engine: TranslateEngine? = null

    /** The engine if its module is on the phone (installed from Play, or built in, as in preview APKs). */
    fun engineOrNull(): TranslateEngine? = engine ?: runCatching { Class.forName(ENGINE).getDeclaredConstructor().newInstance() as TranslateEngine }
        .getOrNull()?.also { engine = it }

    /** Gets the engine, asking Google Play for the translation module first if needed. [status] reports progress. */
    suspend fun ensureEngine(context: Context, status: (String) -> Unit): TranslateEngine {
        engineOrNull()?.let { return it }
        val m = SplitInstallManagerFactory.create(context.applicationContext)
        if (MODULE !in m.installedModules) {
            status("Getting the translator (one time)…")
            suspendCancellableCoroutine { c ->
                var session = 0
                lateinit var listener: SplitInstallStateUpdatedListener
                listener = SplitInstallStateUpdatedListener { s ->
                    if (s.sessionId() != session) return@SplitInstallStateUpdatedListener
                    when (s.status()) {
                        SplitInstallSessionStatus.DOWNLOADING -> if (s.totalBytesToDownload() > 0)
                            status("Getting the translator (one time)… ${s.bytesDownloaded() * 100 / s.totalBytesToDownload()}%")
                        SplitInstallSessionStatus.INSTALLED -> { m.unregisterListener(listener); if (c.isActive) c.resume(Unit) }
                        SplitInstallSessionStatus.FAILED, SplitInstallSessionStatus.CANCELED, SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                            m.unregisterListener(listener); if (c.isActive) c.resumeWithException(IllegalStateException("translator not installed (${s.status()})"))
                        }
                        SplitInstallSessionStatus.DOWNLOADED, SplitInstallSessionStatus.INSTALLING -> status("Installing the translator…")
                        else -> Unit
                    }
                }
                m.registerListener(listener)
                m.startInstall(SplitInstallRequest.newBuilder().addModule(MODULE).build())
                    .addOnSuccessListener { id -> session = id; if (id == 0 && c.isActive) { m.unregisterListener(listener); c.resume(Unit) } }
                    .addOnFailureListener { e -> m.unregisterListener(listener); if (c.isActive) c.resumeWithException(e) }
                c.invokeOnCancellation { m.unregisterListener(listener) }
            }
            com.google.android.play.core.splitcompat.SplitCompat.install(context.applicationContext)
        }
        return engineOrNull() ?: throw IllegalStateException("translator not available")
    }

    private val cache = ConcurrentHashMap<String, String>()

    suspend fun text(e: TranslateEngine, code: String, s: String): String {
        if (s.isBlank()) return s
        val key = "$code\u0001$s"
        cache[key]?.let { return it }
        return e.translate(code, s).also { if (cache.size < 4000) cache[key] = it }
    }

    /** Downloaded languages, for Settings (empty if the translator isn't on the phone). */
    suspend fun downloaded(): List<Lang> = runCatching { engineOrNull()?.downloaded().orEmpty().mapNotNull { lang(it) } }.getOrDefault(emptyList())
    suspend fun remove(code: String) { runCatching { engineOrNull()?.remove(code) } }

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
}
