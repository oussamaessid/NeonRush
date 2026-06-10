package app.neonrush.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
fun GameMenu(
    gameState: GameState,
    onStartGame: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = rememberAdaptiveScale()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.88f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.96f)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding((32 * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = onQuit,
                        modifier = Modifier
                            .size((44 * scale).dp)
                            .background(color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp))
                    ) {
                        Text(text = "✕", color = Color.White.copy(alpha = 0.5f), fontSize = (22 * scale).sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (gameState.score > 0) {
                    GameOverContent(gameState)
                } else {
                    MainMenuContent()
                }

                Button(
                    onClick = onStartGame,
                    modifier = Modifier.padding(top = (24 * scale).dp).height((58 * scale).dp).fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (gameState.score == 0) "JOUER" else "REJOUER",
                        fontWeight = FontWeight.Bold,
                        fontSize = (19 * scale).sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MainMenuContent() {
    val scale = rememberAdaptiveScale()
    Text(text = "NEON RUSH", color = Color.White, fontSize = (34 * scale).sp, fontWeight = FontWeight.ExtraBold)
    Spacer(modifier = Modifier.height(20.dp))
    InstructionItem("🟦", "Carrés bleus = Points")
    InstructionItem("🔺", "Triangles rouges = Danger mortel")
    InstructionItem("🟢", "Ballon vert = Protection (5s)")
    InstructionItem("⭐", "Étoile jaune = Score ×2 (6s)")
    InstructionItem("🔥", "Combo rapide = Ballons bonus !")
}

@Composable
private fun GameOverContent(gameState: GameState) {
    val scale = rememberAdaptiveScale()
    Text(text = "GAME OVER", color = Color.White, fontSize = (34 * scale).sp, fontWeight = FontWeight.ExtraBold)
    Spacer(modifier = Modifier.height(24.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        FinalTimeDisplay(finalTime = gameState.elapsedTime, bestTime = gameState.bestTime)
        FinalScoreDisplay(finalScore = gameState.score, bestScore = gameState.bestScore)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        StatItem("🟦", "${gameState.greensCaught}", "Bonus")
        StatItem("🔺", "${gameState.hazardsDodged}", "Évités")
    }
}

@Composable
private fun FinalTimeDisplay(finalTime: Long, bestTime: Long) {
    val scale = rememberAdaptiveScale()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "TEMPS", color = Color.White.copy(alpha = 0.5f), fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
        Text(text = "${finalTime}s", color = Color(0xFFfbbf24), fontSize = (30 * scale).sp, fontWeight = FontWeight.Black)
        if (finalTime >= bestTime) {
            Text(text = "🏆 RECORD", color = Color(0xFFfbbf24), fontSize = (11 * scale).sp, fontWeight = FontWeight.ExtraBold)
        } else {
            Text(text = "Best: ${bestTime}s", color = Color.White.copy(alpha = 0.4f), fontSize = (10 * scale).sp)
        }
    }
}

@Composable
private fun FinalScoreDisplay(finalScore: Int, bestScore: Int) {
    val scale = rememberAdaptiveScale()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "SCORE", color = Color.White.copy(alpha = 0.5f), fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
        Text(text = "$finalScore", color = Color(0xFF38BDF8), fontSize = (30 * scale).sp, fontWeight = FontWeight.Black)
        if (finalScore >= bestScore) {
            Text(text = "🏆 RECORD", color = Color(0xFF38BDF8), fontSize = (11 * scale).sp, fontWeight = FontWeight.ExtraBold)
        } else {
            Text(text = "Best: $bestScore", color = Color.White.copy(alpha = 0.4f), fontSize = (10 * scale).sp)
        }
    }
}

@Composable
fun StatItem(emoji: String, value: String, label: String) {
    val scale = rememberAdaptiveScale()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = (24 * scale).sp)
        Text(text = value, color = Color.White, fontSize = (22 * scale).sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = (11 * scale).sp)
    }
}

@Composable
fun InstructionItem(emoji: String, text: String) {
    val scale = rememberAdaptiveScale()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = emoji, fontSize = (20 * scale).sp, modifier = Modifier.padding(end = 10.dp))
        Text(text = text, color = Color.White.copy(alpha = 0.85f), fontSize = (13 * scale).sp)
    }
}