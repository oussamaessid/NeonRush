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
import androidx.compose.ui.viewinterop.AndroidView
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

    // ✅ true seulement quand la bannière est chargée avec succès
    var isBannerLoaded by remember { mutableStateOf(false) }

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
                    fontSize      = 48.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 6.sp
                )
                Text(
                    text          = "RUSH",
                    color         = Color(0xFFfbbf24),
                    fontSize      = 48.sp,
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
                modifier  = Modifier.fillMaxWidth().height(58.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                shape     = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text       = "JOUER",
                    fontWeight = FontWeight.Black,
                    fontSize   = 22.sp,
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
                        override fun onAdLoaded() {
                            isBannerLoaded = true
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isBannerLoaded = false
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
private fun StatsSection(stats: GameStats) {
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
            fontSize      = 11.sp,
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 26.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = color, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun InstructionsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text          = "COMMENT JOUER",
            color         = Color.White.copy(alpha = 0.5f),
            fontSize      = 11.sp,
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
    Row(
        modifier          = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp, modifier = Modifier.width(34.dp))
        Column {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(desc,  color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        }
    }
}