package com.randomthoughtsever.plainly.translate

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.randomthoughtsever.plainly.data.TranslateEngine
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Google ML Kit translation, English to the chosen language, entirely on the phone once a language is downloaded. */
class MlKitEngine : TranslateEngine {
    private val clients = ConcurrentHashMap<String, Translator>()
    private fun client(code: String) = clients.getOrPut(code) {
        Translation.getClient(TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.fromLanguageTag(code) ?: code).build())
    }
    private fun model(code: String) = TranslateRemoteModel.Builder(TranslateLanguage.fromLanguageTag(code) ?: code).build()

    override suspend fun isDownloaded(code: String) = RemoteModelManager.getInstance().isModelDownloaded(model(code)).await()
    override suspend fun download(code: String) { client(code).downloadModelIfNeeded(DownloadConditions.Builder().build()).await() }
    override suspend fun translate(code: String, text: String): String = client(code).translate(text).await()
    override suspend fun downloaded(): List<String> =
        RemoteModelManager.getInstance().getDownloadedModels(TranslateRemoteModel::class.java).await().map { it.language }.filter { it != TranslateLanguage.ENGLISH }
    override suspend fun remove(code: String) {
        RemoteModelManager.getInstance().deleteDownloadedModel(model(code)).await()
        clients.remove(code)?.close()
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { c ->
        addOnSuccessListener { c.resume(it) }
        addOnFailureListener { c.resumeWithException(it) }
        addOnCanceledListener { c.cancel() }
    }
}
