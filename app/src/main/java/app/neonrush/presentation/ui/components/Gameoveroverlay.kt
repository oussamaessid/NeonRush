package app.neonrush.presentation.ui.components

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neonrush.ads.AdManager
import app.neonrush.data.model.GameState
import app.neonrush.ui.theme.rememberAdaptiveScale

@Composable
fun GameOverOverlay(
    gameState:   GameState,
    onReplay:    () -> Unit,
    onContinue:  () -> Unit,   // ← continuer après pub récompensée
    onBackToMenu: () -> Unit
) {
    val activity = LocalContext.current as Activity
    val scale    = rememberAdaptiveScale()
    var adReady  by remember { mutableStateOf(AdManager.isRewardedAdReady()) }
    var loading  by remember { mutableStateOf(false) }

    // Vérifie si la pub est prête toutes les secondes
    LaunchedEffect(Unit) {
        while (true) {
            adReady = AdManager.isRewardedAdReady()
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))),
                    RoundedCornerShape(28.dp)
                )
                .padding((32 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("GAME OVER", color = Color(0xFFF43F5E),
                fontSize = (34 * scale).sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)

            Spacer(Modifier.height(20.dp))

            Text("SCORE", color = Color.White.copy(alpha = 0.5f),
                fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text(gameState.score.toString(), color = Color(0xFF38BDF8),
                fontSize = (52 * scale).sp, fontWeight = FontWeight.Black)

            Spacer(Modifier.height(8.dp))

            Text("Temps : ${gameState.elapsedTime}s", color = Color.White.copy(alpha = 0.5f),
                fontSize = (14 * scale).sp)

            Spacer(Modifier.height(28.dp))

            // ── Bouton "Continuer" avec pub récompensée ───────────────────
            if (adReady) {
                Button(
                    onClick = {
                        loading = true
                        AdManager.showRewardedAd(
                            activity  = activity,
                            onRewarded = {
                                loading = false
                                onContinue()   // ← reprend la partie
                            },
                            onSkipped  = { loading = false }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height((58 * scale).dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    shape    = RoundedCornerShape(16.dp),
                    enabled  = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.White,
                            modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("▶  Regarder une pub pour continuer",
                            fontWeight = FontWeight.Bold, fontSize = (15 * scale).sp, color = Color.White,
                            textAlign = TextAlign.Center)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Button(
                onClick  = onReplay,
                modifier = Modifier.fillMaxWidth().height((54 * scale).dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Text("REJOUER", fontWeight = FontWeight.Black,
                    fontSize = (18 * scale).sp, color = Color(0xFF0F172A))
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick  = onBackToMenu,
                modifier = Modifier.fillMaxWidth().height((48 * scale).dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Menu principal", fontSize = (15 * scale).sp)
            }
        }
    }
}