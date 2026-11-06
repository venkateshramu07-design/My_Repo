package com.randomthoughtsever.plainly.data

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.randomthoughtsever.plainly.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.URLEncoder

/**
 * "Support Plainly": optional tips. On Google Play they go through Play Billing (three consumable products the owner
 * creates in Play Console: tip_small, tip_medium, tip_large). Builds made with -PsupportLinks=true also offer the same
 * PayPal and UPI details as the blog's support box. Asking is gentle: only after the reader has got real value, at most
 * once a week, never in the first three days, and never again for a month after a tip.
 */
object Support {
    val PRODUCTS = listOf("tip_small", "tip_medium", "tip_large")
    const val DAY = 86_400_000L

    var tips by mutableStateOf<List<ProductDetails>>(emptyList()); private set
    var status by mutableStateOf("idle"); private set   // idle, loading, ready, unavailable
    var thanks by mutableStateOf(false)
    private var client: BillingClient? = null
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun prefs(c: Context) = c.getSharedPreferences("support", Context.MODE_PRIVATE)

    // ---------- when to ask ----------

    /** Pure rule, tested: enough value, not too new, not asked this week, not tipped this month. */
    fun shouldAsk(now: Long, firstOpen: Long, lastAsked: Long, lastTip: Long, reads: Int, runs: Int, dismissed: Int): Boolean =
        now - firstOpen >= 3 * DAY && (reads >= 5 || runs >= 3) && now - lastAsked >= 7 * DAY && now - lastTip >= 30 * DAY && dismissed < 6

    fun shouldAsk(c: Context, reads: Int, runs: Int): Boolean {
        val p = prefs(c)
        val first = c.getSharedPreferences("plainly", Context.MODE_PRIVATE).getLong("firstOpen", System.currentTimeMillis())
        return shouldAsk(System.currentTimeMillis(), first, p.getLong("asked", 0), p.getLong("tipped", 0), reads, runs, p.getInt("dismissed", 0))
    }
    /** Records that a gentle ask was shown (so the next one waits a week). Call once per showing. */
    fun asked(c: Context) = prefs(c).edit().putLong("asked", System.currentTimeMillis()).apply()
    fun dismissed(c: Context) = prefs(c).edit().putInt("dismissed", prefs(c).getInt("dismissed", 0) + 1).putLong("asked", System.currentTimeMillis()).apply()
    fun supporter(c: Context) = prefs(c).getLong("tipped", 0) > 0
    fun tipCount(c: Context) = prefs(c).getInt("tips", 0)

    // ---------- Google Play tips ----------

    fun load(c: Context) {
        if (status == "loading" || status == "ready") return
        // The JVM test environment has no Google Play app for the billing library to bind to (it would keep retrying).
        if (android.os.Build.FINGERPRINT == "robolectric") { status = "unavailable"; return }
        status = "loading"
        val bc = client ?: BillingClient.newBuilder(c.applicationContext)
            .setListener { r, purchases -> onPurchases(c.applicationContext, r, purchases) }
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build().also { client = it }
        runCatching {
            bc.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(r: BillingResult) {
                    if (r.responseCode != BillingClient.BillingResponseCode.OK) { status = "unavailable"; return }
                    io.launch {
                        val q = QueryProductDetailsParams.newBuilder().setProductList(PRODUCTS.map {
                            QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.INAPP).build()
                        }).build()
                        val res = runCatching { bc.queryProductDetails(q) }.getOrNull()
                        tips = res?.productDetailsList.orEmpty().sortedBy { it.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0 }
                        status = if (tips.isEmpty()) "unavailable" else "ready"
                    }
                }
                override fun onBillingServiceDisconnected() { if (status != "ready") status = "unavailable" }
            })
        }.onFailure { status = "unavailable" }
    }

    /** Closes the Google Play connection when the Support screen goes away (it's only needed there). */
    fun end() {
        runCatching { client?.endConnection() }
        client = null
        status = "idle"; tips = emptyList()
    }

    fun tip(activity: Activity, p: ProductDetails) {
        val bc = client ?: return
        bc.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(p).build())).build())
    }

    private fun onPurchases(c: Context, r: BillingResult, purchases: List<Purchase>?) {
        if (r.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) return
        purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { p ->
            // Tips are consumable, so the same tip can be given again later.
            io.launch { runCatching { client?.consumePurchase(ConsumeParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) } }
            recordTip(c)
        }
    }

    fun recordTip(c: Context) {
        val p = prefs(c)
        p.edit().putLong("tipped", System.currentTimeMillis()).putInt("tips", p.getInt("tips", 0) + 1).apply()
        thanks = true
    }

    // ---------- PayPal and UPI (the blog's own details; only when enabled for the build) ----------

    val linksEnabled get() = BuildConfig.SUPPORT_LINKS
    fun paypal(amount: Int, currency: String) = "https://paypal.me/${BuildConfig.SUPPORT_PAYPAL}/$amount$currency"
    fun upi(amount: Int) = "upi://pay?pa=${BuildConfig.SUPPORT_UPI}&pn=${enc("Blog Digest")}&cu=INR&am=$amount&tn=${enc("Support Plainly")}"
    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8").replace("+", "%20")
    /** PayPal amounts in a currency people in that place use; India gets UPI first. */
    fun paypalCurrency(code: String) = if (code in setOf("USD", "EUR", "GBP", "CAD", "AUD", "JPY", "SGD", "CHF", "NZD")) code else "USD"
    fun paypalAmounts(code: String) = if (paypalCurrency(code) == "JPY") listOf(300, 500, 1000) else listOf(3, 5, 10)
    val UPI_AMOUNTS = listOf(49, 99, 199)
}
