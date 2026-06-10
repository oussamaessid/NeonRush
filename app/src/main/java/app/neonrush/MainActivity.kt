package app.neonrush

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.neonrush.ads.AdManager
import app.neonrush.data.StatsManager
import app.neonrush.presentation.ui.screens.GameScreen
import app.neonrush.presentation.ui.screens.HomeScreen
import com.google.android.gms.ads.MobileAds
import kotlin.system.exitProcess

private enum class Screen { HOME, GAME }

class MainActivity : ComponentActivity() {

    private lateinit var statsManager: StatsManager

    private var currentScreen  = Screen.HOME
    private var isInBackground = false
    private var isFirstLaunch  = true   // ← détecte le cold start

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            // Retour depuis le background (pas le premier lancement)
            if (isInBackground) {
                isInBackground = false
                if (currentScreen == Screen.HOME) {
                    showAppOpenAdIfReady()
                }
            }
        }
        override fun onStop(owner: LifecycleOwner) {
            isInBackground = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupFullscreen()

        statsManager = StatsManager(applicationContext)

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        MobileAds.initialize(this) {
            // Charge la pub et l'affiche dès qu'elle est prête (cold start)
            AdManager.loadAppOpenAd(this) {
                if (isFirstLaunch && currentScreen == Screen.HOME) {
                    isFirstLaunch = false
                    runOnUiThread { showAppOpenAdIfReady() }
                }
            }
            AdManager.loadRewardedAd(this)
        }

        setContent {
            Surface(color = Color(0xFF020617)) {
                AppContent()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
    }

    // ── Affiche la pub si chargée, recharge ensuite ───────────────────────────
    private fun showAppOpenAdIfReady() {
        if (AdManager.isAppOpenAdLoaded()) {
            AdManager.showAppOpenAd(this) {
                // Recharge immédiatement pour la prochaine fois
                AdManager.loadAppOpenAd(this)
            }
        } else {
            // Pas encore chargée : recharge et réessaie au prochain onStart
            AdManager.loadAppOpenAd(this)
        }
    }

    @Composable
    private fun AppContent() {
        var composeScreen by remember { mutableStateOf(Screen.HOME) }
        var stats         by remember { mutableStateOf(statsManager.getStats()) }

        LaunchedEffect(composeScreen) {
            currentScreen = composeScreen
        }

        when (composeScreen) {

            Screen.HOME -> HomeScreen(
                stats          = stats,
                bannerAdUnitId = AdManager.getBannerAdUnitId(),
                onPlay         = { composeScreen = Screen.GAME },
                onQuit         = { finish(); exitProcess(0) }
            )

            Screen.GAME -> GameScreen(
                statsManager = statsManager,
                onBackToMenu = {
                    stats         = statsManager.getStats()
                    composeScreen = Screen.HOME
                }
            )
        }
    }

    private fun setupFullscreen() {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                view.onApplyWindowInsets(insets)
            }
        }
    }

}