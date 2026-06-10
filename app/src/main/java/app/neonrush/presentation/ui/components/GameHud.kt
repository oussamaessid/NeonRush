package app.neonrush.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neonrush.data.model.GameState
import app.neonrush.ui.theme.rememberAdaptiveScale

@Composable
fun GameHud(
    gameState: GameState,
    shieldTimeRemaining: Int,
    multiplierTimeRemaining: Int,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = rememberAdaptiveScale()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = (60 * scale).dp, start = (32 * scale).dp, end = (32 * scale).dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            androidx.compose.material3.IconButton(
                onClick = onQuit,
                modifier = Modifier
                    .size((48 * scale).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Text(
                    text = "✕",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = (24 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeDisplay(
                elapsedTime = gameState.elapsedTime,
                bestTime = gameState.bestTime
            )

            PowerUpsAndComboDisplay(
                hasShield = gameState.hasShield,
                shieldTimeRemaining = shieldTimeRemaining,
                scoreMultiplier = gameState.scoreMultiplier,
                multiplierTimeRemaining = multiplierTimeRemaining,
                combo = gameState.combo,
                comboTimeRemaining = gameState.comboTimeRemaining
            )

            ScoreDisplay(
                score = gameState.score,
                bestScore = gameState.bestScore,
                scoreMultiplier = gameState.scoreMultiplier
            )
        }
    }
}

@Composable
private fun TimeDisplay(elapsedTime: Long, bestTime: Long) {
    val scale = rememberAdaptiveScale()
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = "TEMPS", color = Color.White.copy(alpha = 0.6f), fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
        Text(text = "${elapsedTime}s", color = Color(0xFFfbbf24), fontSize = (36 * scale).sp, fontWeight = FontWeight.Black)
        if (bestTime > 0) {
            Text(text = "Best: ${bestTime}s", color = Color(0xFFfbbf24).copy(alpha = 0.5f), fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PowerUpsAndComboDisplay(
    hasShield: Boolean,
    shieldTimeRemaining: Int,
    scoreMultiplier: Float,
    multiplierTimeRemaining: Int,
    combo: Int,
    comboTimeRemaining: Int
) {
    val scale = rememberAdaptiveScale()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        if (hasShield && shieldTimeRemaining > 0) {
            Text(text = "🛡️", fontSize = (28 * scale).sp)
            Text(
                text = "${shieldTimeRemaining}s",
                color = Color(0xFF6EE7B7),
                fontSize = (22 * scale).sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (scoreMultiplier > 1f && multiplierTimeRemaining > 0) {
            Text(text = "⭐", fontSize = (22 * scale).sp)
            Text(
                text = "${multiplierTimeRemaining}s",
                color = Color(0xFFfbbf24),
                fontSize = (20 * scale).sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (combo >= 3 && comboTimeRemaining > 0) {
            val comboColor = when {
                combo >= 10 -> Color(0xFFEC4899)
                combo >= 5  -> Color(0xFFa78bfa)
                else        -> Color(0xFF7DD3FC)
            }
            Text(
                text = "🔥",
                fontSize = when {
                    combo >= 10 -> (28 * scale).sp
                    combo >= 6  -> (24 * scale).sp
                    else        -> (20 * scale).sp
                }
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "×$combo",
                    color = comboColor,
                    fontSize = when {
                        combo >= 10 -> (28 * scale).sp
                        combo >= 6  -> (24 * scale).sp
                        else        -> (20 * scale).sp
                    },
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${comboTimeRemaining}s",
                    color = comboColor.copy(alpha = 0.8f),
                    fontSize = (18 * scale).sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun ScoreDisplay(score: Int, bestScore: Int, scoreMultiplier: Float) {
    val scale = rememberAdaptiveScale()
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "SCORE", color = Color.White.copy(alpha = 0.6f), fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
            if (scoreMultiplier > 1f) {
                Text(
                    text = " ×${scoreMultiplier.toInt()}",
                    color = Color(0xFFfbbf24),
                    fontSize = (14 * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        Text(text = score.toString(), color = Color(0xFF38BDF8), fontSize = (36 * scale).sp, fontWeight = FontWeight.Black)
        if (bestScore > 0) {
            Text(text = "Best: $bestScore", color = Color(0xFF38BDF8).copy(alpha = 0.5f), fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold)
        }
    }
}