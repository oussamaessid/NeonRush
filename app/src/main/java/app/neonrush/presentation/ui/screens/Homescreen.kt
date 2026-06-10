package app.neonrush.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neonrush.ui.theme.rememberAdaptiveScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import app.neonrush.data.GameStats

@Composable
fun HomeScreen(
    stats: GameStats,
    bannerAdUnitId: String,
    onPlay: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) { onQuit() }

    val scale = rememberAdaptiveScale()
    var isBannerLoaded by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                )
            )
            .systemBarsPadding()
    ) {
        IconButton(
            onClick  = onQuit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
        ) {
            Text("✕", color = Color.White.copy(alpha = 0.5f),
                fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                // ✅ padding bottom seulement si bannière chargée, sinon 0
                .padding(bottom = if (isBannerLoaded) 60.dp else 0.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text          = "NEON",
                    color         = Color(0xFF38BDF8),
                    fontSize      = (48 * scale).sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 6.sp
                )
                Text(
                    text          = "RUSH",
                    color         = Color(0xFFfbbf24),
                    fontSize      = (48 * scale).sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 6.sp
                )
            }

            if (stats.totalGames > 0) {
                StatsSection(stats)
            }

            InstructionsSection()

            Button(
                onClick   = onPlay,
                modifier  = Modifier.fillMaxWidth().height((58 * scale).dp),
                colors    = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                shape     = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text       = "JOUER",
                    fontWeight = FontWeight.Black,
                    fontSize   = (22 * scale).sp,
                    color      = Color(0xFF0F172A)
                )
            }
        }

        AndroidView(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(if (isBannerLoaded) 60.dp else 0.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = bannerAdUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() { isBannerLoaded = true }
                        override fun onAdFailedToLoad(error: LoadAdError) { isBannerLoaded = false }
                    }
                    loadAd(AdRequest.Builder().build())

                    // Gestion du cycle de vie : pause/resume/destroy obligatoires
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME  -> resume()
                            Lifecycle.Event.ON_PAUSE   -> pause()
                            Lifecycle.Event.ON_DESTROY -> destroy()
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                }
            }
        )
    }
}

@Composable
private fun StatsSection(stats: GameStats) {
    val scale = rememberAdaptiveScale()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text          = "MES STATISTIQUES",
            color         = Color.White.copy(alpha = 0.5f),
            fontSize      = (11 * scale).sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBig("🏆", stats.bestScore.toString(), "MEILLEUR SCORE", Color(0xFF38BDF8))
            StatBig("🏆", stats.bestScore.toString(), "MEILLEUR SCORE", Color(0xFF38BDF8))
            StatBig("⏱",  "${stats.bestTime}s",       "MEILLEUR TEMPS", Color(0xFFfbbf24))
        }
    }
}

@Composable
private fun StatBig(emoji: String, value: String, label: String, color: Color) {
    val scale = rememberAdaptiveScale()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = (26 * scale).sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = color, fontSize = (30 * scale).sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White.copy(alpha = 0.4f),
            fontSize = (10 * scale).sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun InstructionsSection() {
    val scale = rememberAdaptiveScale()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text          = "COMMENT JOUER",
            color         = Color.White.copy(alpha = 0.5f),
            fontSize      = (11 * scale).sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier      = Modifier.fillMaxWidth(),
            textAlign     = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        InstructionRow("🟦", "Carrés bleus",     "Attrape pour marquer des points")
        InstructionRow("🔺", "Triangles rouges", "Évite — contact = game over !")
        InstructionRow("🟢", "Ballon vert",       "Protection pendant 5 secondes")
        InstructionRow("⭐", "Étoile jaune",      "Score ×2 pendant 6 secondes")
        InstructionRow("🔥", "Combo rapide",      "Enchaîne les bonus pour des récompenses")
        InstructionRow("👆", "Glisser le doigt",  "Déplace ton vaisseau gauche / droite")
    }
}

@Composable
private fun InstructionRow(emoji: String, title: String, desc: String) {
    val scale = rememberAdaptiveScale()
    Row(
        modifier          = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = (20 * scale).sp, modifier = Modifier.width(34.dp))
        Column {
            Text(title, color = Color.White, fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
            Text(desc,  color = Color.White.copy(alpha = 0.5f), fontSize = (10 * scale).sp)
        }
    }
}