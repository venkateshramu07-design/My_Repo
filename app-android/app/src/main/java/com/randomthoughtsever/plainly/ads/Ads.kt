package com.randomthoughtsever.plainly.ads

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.randomthoughtsever.plainly.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Google AdMob, with Google's consent form (UMP) shown first where the law requires it (EEA, UK, Switzerland and some US states).
 * No ad is requested before consent rules allow it. Placements are deliberately few: inline banners between content,
 * and at most one full-screen ad after the reader finishes a tool task, never more than once every few minutes.
 */
object Ads {
    /** Ads are off in automated tests (Robolectric), where the ads SDK can't run. */
    private val enabled = Build.FINGERPRINT != "robolectric"
    private val started = AtomicBoolean(false)

    /** True once consent allows ads and the SDK is ready. Composables read this, so banners appear as soon as it flips. */
    var ready by mutableStateOf(false)
        private set
    /** True when the reader must be able to change their consent choice (shown as "Privacy choices" in Settings). */
    var privacyOptionsRequired by mutableStateOf(false)
        private set

    private const val INTERSTITIAL_GAP_MS = 4 * 60 * 1000L
    private var interstitial: InterstitialAd? = null
    private var lastFullScreen = 0L
    private var completedTasks = 0

    fun start(activity: Activity) {
        if (!enabled) return
        val consent = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()
        consent.requestConsentInfoUpdate(activity, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                privacyOptionsRequired = consent.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                if (consent.canRequestAds()) initialise(activity)
            }
        }, { if (consent.canRequestAds()) initialise(activity) })
        // Consent given in an earlier session lets ads start straight away, while the update above runs.
        if (consent.canRequestAds()) initialise(activity)
    }

    fun showPrivacyOptions(activity: Activity) = UserMessagingPlatform.showPrivacyOptionsForm(activity) {
        val consent = UserMessagingPlatform.getConsentInformation(activity)
        if (consent.canRequestAds()) initialise(activity)
    }

    private fun initialise(activity: Activity) {
        if (!started.compareAndSet(false, true)) return
        val app = activity.applicationContext
        MobileAds.setRequestConfiguration(RequestConfiguration.Builder()
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE)
            .build())
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(app) {}
            withContext(Dispatchers.Main) { ready = true; loadInterstitial(app) }
        }
    }

    private fun loadInterstitial(context: Context) {
        if (!ready || interstitial != null) return
        InterstitialAd.load(context, BuildConfig.ADMOB_INTERSTITIAL, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
            override fun onAdFailedToLoad(error: LoadAdError) { interstitial = null }
        })
    }

    /**
     * Call after the reader completes a task (a file saved). Shows a full-screen ad only from the second task on, and
     * at most once every 4 minutes. Returns true if one was shown.
     */
    fun afterTask(activity: Activity): Boolean {
        completedTasks++
        val ad = interstitial
        val now = SystemClock.elapsedRealtime()
        if (!ready || ad == null || completedTasks < 2 || (lastFullScreen != 0L && now - lastFullScreen < INTERSTITIAL_GAP_MS)) return false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { interstitial = null; loadInterstitial(activity.applicationContext) }
            override fun onAdFailedToShowFullScreenContent(e: com.google.android.gms.ads.AdError) { interstitial = null; loadInterstitial(activity.applicationContext) }
        }
        lastFullScreen = now
        ad.show(activity)
        return true
    }

    // ---------- Banners ----------

    /** A loaded banner is kept for its slot, so scrolling a list back and forth doesn't request a new ad each time. */
    internal class Banner(val view: AdView) {
        var loaded by mutableStateOf(false)
        var heightDp by mutableIntStateOf(0)
    }

    private val banners = object : LinkedHashMap<String, Banner>(16, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Banner>): Boolean =
            (size > 10).also { if (it) eldest.value.view.destroy() }
    }

    /** Called when the activity is destroyed, so no ad view outlives it. */
    fun clearBanners() { banners.values.forEach { it.view.destroy() }; banners.clear() }

    internal fun banner(context: Context, slot: String, widthDp: Int, maxHeightDp: Int): Banner = banners.getOrPut(slot) {
        val view = AdView(context) // the activity, so taps on the ad open correctly
        view.adUnitId = BuildConfig.ADMOB_BANNER
        view.setAdSize(AdSize.getInlineAdaptiveBannerAdSize(widthDp, maxHeightDp))
        Banner(view).also { b ->
            view.adListener = object : AdListener() {
                override fun onAdLoaded() { b.heightDp = view.adSize?.height ?: 0; b.loaded = b.heightDp > 0 }
                override fun onAdFailedToLoad(error: LoadAdError) { b.loaded = false }
            }
            view.loadAd(AdRequest.Builder().build())
        }
    }
}

/**
 * An inline banner for a fixed place in the app ([slot] names it, e.g. "home:1"). It takes no space until an ad has
 * loaded (offline, no consent or no ad means nothing is shown), and is always labelled "Advertisement".
 */
@Composable
fun BannerAd(slot: String, modifier: Modifier = Modifier, maxHeightDp: Int = 300) {
    if (!Ads.ready) return
    val context = LocalContext.current
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val width = maxWidth.value.toInt().coerceAtLeast(250)
        val b = remember(slot, width) { Ads.banner(context, slot, width, maxHeightDp) }
        Column(Modifier.fillMaxWidth().padding(vertical = if (b.loaded) 12.dp else 0.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (b.loaded) Text("Advertisement", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp))
            Box(Modifier.fillMaxWidth().height(if (b.loaded) b.heightDp.dp else 0.dp), contentAlignment = Alignment.Center) {
                AndroidView(factory = { b.view.also { (it.parent as? ViewGroup)?.removeView(it) } })
            }
        }
    }
}
