package app.neonrush.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.neonrush.BuildConfig
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {

    // Mode production automatique : release = prod, debug = test
    // Ne jamais forcer IS_PRODUCTION = true manuellement pour tester !
    private val IS_PRODUCTION = !BuildConfig.DEBUG

    private object TestIds {
        const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
        const val BANNER   = "ca-app-pub-3940256099942544/6300978111"
        const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
    }

    private object ProdIds {
        const val APP_OPEN = "ca-app-pub-9651830078758870/5502402449"
        const val BANNER   = "ca-app-pub-9651830078758870/4283944680"
        const val REWARDED = "ca-app-pub-9651830078758870/1279026911"
    }

    private val APP_OPEN_AD_UNIT_ID = if (IS_PRODUCTION) ProdIds.APP_OPEN else TestIds.APP_OPEN
    private val BANNER_AD_UNIT_ID   = if (IS_PRODUCTION) ProdIds.BANNER   else TestIds.BANNER
    private val REWARDED_AD_UNIT_ID = if (IS_PRODUCTION) ProdIds.REWARDED else TestIds.REWARDED

    private const val TAG = "AdManager"

    // Délai minimum entre deux App Open Ads : 3 minutes (180 000 ms)
    private const val APP_OPEN_AD_MIN_INTERVAL_MS = 3 * 60 * 1000L
    private var lastAppOpenAdShownAt = 0L

    init {
        Log.d(TAG, if (IS_PRODUCTION) "MODE PRODUCTION" else "MODE TEST")
    }

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenAdShowing = false


    fun loadAppOpenAd(context: Context, onLoaded: (() -> Unit)? = null) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    Log.d(TAG, "App open ad loaded ✓")
                    onLoaded?.invoke()   // ← notifie MainActivity que la pub est prête
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "App open ad failed: ${error.message}")
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity, onComplete: () -> Unit) {
        val ad = appOpenAd
        if (ad == null) {
            Log.w(TAG, "App open ad not ready yet")
            onComplete()
            return
        }

        // Respecte le délai minimum pour éviter les impressions excessives
        val now = System.currentTimeMillis()
        if (now - lastAppOpenAdShownAt < APP_OPEN_AD_MIN_INTERVAL_MS) {
            Log.d(TAG, "App open ad skipped: cooldown not elapsed")
            onComplete()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isAppOpenAdShowing = false
                onComplete()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isAppOpenAdShowing = false
                Log.e(TAG, "App open ad failed to show: ${error.message}")
                onComplete()
            }
            override fun onAdShowedFullScreenContent() {
                isAppOpenAdShowing = true
                lastAppOpenAdShownAt = System.currentTimeMillis()
            }
        }
        ad.show(activity)
    }

    fun isAppOpenAdLoaded() = appOpenAd != null

    // ── Rewarded ─────────────────────────────────────────────────────────────
    private var rewardedAd: RewardedAd? = null

    fun loadRewardedAd(context: Context) {
        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Rewarded ad loaded ✓")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed: ${error.message}")
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onSkipped: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) { onSkipped(); return }

        var rewarded = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd(activity)
                if (rewarded) onRewarded() else onSkipped()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                onSkipped()
            }
        }
        ad.show(activity) { rewarded = true }
    }

    fun isRewardedAdReady() = rewardedAd != null

    // ── Banner ───────────────────────────────────────────────────────────────
    fun getBannerAdUnitId() = BANNER_AD_UNIT_ID
}