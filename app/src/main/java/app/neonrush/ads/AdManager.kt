package app.neonrush.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {

    // ══════════════════════════════════════════════════════════════════════════
    // 🔧 CHANGE ICI POUR PASSER EN MODE TEST
    // ══════════════════════════════════════════════════════════════════════════
    private const val IS_PRODUCTION = true   // ← false = prod  |  true = test
    // ══════════════════════════════════════════════════════════════════════════

    private object TestIds {
        const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
        const val BANNER   = "ca-app-pub-3940256099942544/6300978111"
        const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
    }

    private object ProdIds {
        const val APP_OPEN = "ca-app-pub-2498267529185476/9289947096"
        const val BANNER   = "ca-app-pub-2498267529185476/5845526104"
        const val REWARDED = "ca-app-pub-2498267529185476/7642869688"
    }

    private val APP_OPEN_AD_UNIT_ID = if (IS_PRODUCTION) TestIds.APP_OPEN else ProdIds.APP_OPEN
    private val BANNER_AD_UNIT_ID   = if (IS_PRODUCTION) TestIds.BANNER   else ProdIds.BANNER
    private val REWARDED_AD_UNIT_ID = if (IS_PRODUCTION) TestIds.REWARDED else ProdIds.REWARDED

    private const val TAG = "AdManager"

    init {
        Log.d(TAG, if (IS_PRODUCTION) "🟡 MODE TEST" else "🟢 MODE PRODUCTION")
    }

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenAdShowing = false

    fun loadAppOpenAd(context: Context) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    Log.d(TAG, "App open ad loaded ✓")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "App open ad failed: ${error.message}")
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity, onComplete: () -> Unit) {
        val ad = appOpenAd
        if (ad == null) { onComplete(); return }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isAppOpenAdShowing = false
                loadAppOpenAd(activity)
                onComplete()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isAppOpenAdShowing = false
                onComplete()
            }
            override fun onAdShowedFullScreenContent() {
                isAppOpenAdShowing = true
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