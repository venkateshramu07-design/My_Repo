package com.randomthoughtsever.plainly.data

import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import java.io.File

/**
 * Covers: a quarter of the app's memory and 150 MB on the phone, kept regardless of what the server's cache headers
 * say (cover files never change once published), so a cover seen once shows instantly, even offline.
 */
object Images {
    fun install(context: Context) {
        Coil.setImageLoader(ImageLoader.Builder(context)
            .memoryCache { MemoryCache.Builder(context).maxSizePercent(0.25).build() }
            .diskCache { DiskCache.Builder().directory(File(context.cacheDir, "covers")).maxSizeBytes(150L * 1024 * 1024).build() }
            .respectCacheHeaders(false)
            .crossfade(180)
            .build())
    }

    /** Downloads covers ahead of time (to the phone, not memory), so the next ones in a list appear without waiting. */
    fun prefetch(context: Context, urls: List<String?>) {
        val loader = Coil.imageLoader(context)
        urls.filterNotNull().distinct().forEach { url ->
            loader.enqueue(ImageRequest.Builder(context).data(url).memoryCachePolicy(CachePolicy.DISABLED).build())
        }
    }
}
