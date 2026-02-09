package app.neonrush.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.neonrush.presentation.ui.components.*
import app.neonrush.presentation.viewmodel.GameViewModel

@Composable
fun GameScreen(
    onQuit: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val items by viewModel.items.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val visualState by viewModel.visualState.collectAsState()
    val powerUpState by viewModel.powerUpState.collectAsState()

    BackHandler(enabled = true) { }

    // Colors and animations
    val mainColor by animateColorAsState(
        targetValue = when {
            visualState.slowMotionActive -> Color(0xFF7C3AED)
            gameState.currentSpeed < 7f -> Color(0xFF1e3a8a)
            gameState.currentSpeed < 12f -> Color(0xFF4c1d95)
            gameState.currentSpeed < 18f -> Color(0xFF831843)
            gameState.currentSpeed < 25f -> Color(0xFF9a3412)
            else -> Color(0xFF991b1b)
        },
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "mainColor"
    )

    val shieldAlpha by animateFloatAsState(
        targetValue = if (gameState.hasShield) 0.7f else 0f,
        animationSpec = tween(300),
        label = "shieldAlpha"
    )

    // ✅ COULEUR DU JOUEUR - VÉRIFIE TIMER COMBO
    val playerColor by animateColorAsState(
        targetValue = when {
            gameState.hasShield -> Color(0xFF22C55E)                                        // 🟢 VERT avec shield
            gameState.scoreMultiplier > 1f -> Color(0xFFfbbf24)                             // 🟡 Jaune avec multiplier
            gameState.combo >= 8 && gameState.comboTimeRemaining > 0 -> Color(0xFFEC4899)   // 🟣 Rose combo 8+ ACTIF
            gameState.combo >= 5 && gameState.comboTimeRemaining > 0 -> Color(0xFFa78bfa)   // 💜 Violet combo 5+ ACTIF
            gameState.combo >= 3 && gameState.comboTimeRemaining > 0 -> Color(0xFF7DD3FC)   // 🔵 Cyan combo 3+ ACTIF
            else -> Color(0xFF60A5FA)                                                       // 🔵 Bleu normal
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),                           // Animation rapide pour reset
        label = "playerColor"
    )

    val playerGradient = remember(playerColor) {
        Brush.radialGradient(
            colors = listOf(Color.White, playerColor),
            radius = 55f
        )
    }

    // ✅ GLOW SIZE ET COLOR - VÉRIFIE TIMER COMBO
    val glowSize = when {
        gameState.combo >= 10 && gameState.comboTimeRemaining > 0 -> 80f    // Énorme pour combo 10+ ACTIF
        gameState.combo >= 8 && gameState.comboTimeRemaining > 0 -> 75f     // Très grand pour combo 8+ ACTIF
        gameState.combo >= 5 && gameState.comboTimeRemaining > 0 -> 70f     // Grand pour combo 5+ ACTIF
        gameState.combo >= 3 && gameState.comboTimeRemaining > 0 -> 65f     // Moyen pour combo 3+ ACTIF
        gameState.scoreMultiplier > 1f -> 68f
        else -> 58f
    }

    val glowColor by animateColorAsState(
        targetValue = when {
            gameState.combo >= 8 && gameState.comboTimeRemaining > 0 -> Color(0xFFEC4899)   // 🟣 Rose combo 8+ ACTIF
            gameState.combo >= 5 && gameState.comboTimeRemaining > 0 -> Color(0xFFa78bfa)   // 💜 Violet combo 5+ ACTIF
            gameState.combo >= 3 && gameState.comboTimeRemaining > 0 -> Color(0xFF7DD3FC)   // 🔵 Cyan combo 3+ ACTIF
            gameState.hasShield -> Color(0xFF22C55E)                                        // 🟢 VERT avec shield
            gameState.scoreMultiplier > 1f -> Color(0xFFfbbf24)                             // 🟡 Jaune avec multiplier
            else -> Color(0xFF60A5FA)                                                       // 🔵 Bleu normal
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "glowColor"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF020617)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .pointerInput(gameState.isActive) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (gameState.isActive) {
                                viewModel.updatePlayerPosition(
                                    offset.x.coerceIn(0f, size.width.toFloat()),
                                    dragging = true
                                )
                            }
                        },
                        onDrag = { change, _ ->
                            if (gameState.isActive) {
                                change.consume()
                                viewModel.updatePlayerPosition(
                                    change.position.x.coerceIn(0f, size.width.toFloat()),
                                    dragging = true
                                )
                            }
                        },
                        onDragEnd = {
                            viewModel.updatePlayerPosition(gameState.playerX, dragging = false)
                        },
                        onDragCancel = {
                            viewModel.updatePlayerPosition(gameState.playerX, dragging = false)
                        }
                    )
                }
        ) {
            // Game Canvas
            key(visualState.gameFrame) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(
                            x = visualState.screenShakeX.dp,
                            y = visualState.screenShakeY.dp
                        )
                ) {
                    // Update screen dimensions
                    if (gameState.screenWidth == 0f || gameState.screenHeight == 0f) {
                        viewModel.updateScreenDimensions(size.width, size.height)
                    }

                    // Draw background
                    drawGameBackground(mainColor)

                    // Draw items
                    val hazardPath = Path()
                    items.forEach { item ->
                        drawGameItem(item, hazardPath)
                    }

                    // Draw particles
                    drawParticles(particles)

                    // Draw shield aura
                    if (gameState.hasShield) {
                        drawShieldAura(
                            gameState.playerX,
                            size.height * 0.85f,
                            shieldAlpha
                        )
                    }

                    // Draw player
                    drawPlayer(
                        gameState.playerX,
                        size.height * 0.85f,
                        playerGradient,
                        glowColor,
                        glowSize
                    )
                }
            }

            // HUD
            if (gameState.isActive) {
                GameHud(
                    gameState = gameState,
                    shieldTimeRemaining = gameState.shieldTimeRemaining,
                    multiplierTimeRemaining = gameState.multiplierTimeRemaining,
                    onQuit = onQuit
                )
            }

            // Menu
            if (!gameState.isActive) {
                GameMenu(
                    gameState = gameState,
                    onStartGame = { viewModel.startGame() },
                    onQuit = onQuit
                )
            }
        }
    }
}