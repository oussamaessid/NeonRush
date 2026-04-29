package app.neonrush.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import app.neonrush.data.StatsManager
import app.neonrush.presentation.ui.components.*
import app.neonrush.presentation.viewmodel.GameViewModel

@Composable
fun GameScreen(
    statsManager: StatsManager,
    onBackToMenu: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val gameState   by viewModel.gameState.collectAsState()
    val items       by viewModel.items.collectAsState()
    val particles   by viewModel.particles.collectAsState()
    val visualState by viewModel.visualState.collectAsState()

    LaunchedEffect(Unit) { viewModel.startGame() }

    LaunchedEffect(gameState.isActive) {
        if (!gameState.isActive && gameState.score > 0) {
            statsManager.recordGame(
                score         = gameState.score,
                time          = gameState.elapsedTime,
                bonusCaught   = gameState.greensCaught,
                hazardsDodged = gameState.hazardsDodged
            )
        }
    }

    BackHandler(enabled = true) { /* bloqué */ }

    val mainColor by animateColorAsState(
        targetValue = when {
            visualState.slowMotionActive -> Color(0xFF7C3AED)
            gameState.currentSpeed < 7f  -> Color(0xFF1e3a8a)
            gameState.currentSpeed < 12f -> Color(0xFF4c1d95)
            gameState.currentSpeed < 18f -> Color(0xFF831843)
            gameState.currentSpeed < 25f -> Color(0xFF9a3412)
            else                         -> Color(0xFF991b1b)
        },
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "mainColor"
    )

    val playerColor by animateColorAsState(
        targetValue = when {
            gameState.hasShield            -> Color(0xFF6EE7B7)
            gameState.scoreMultiplier > 1f -> Color(0xFFFCD34D)
            else                           -> Color(0xFF60A5FA)
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "playerColor"
    )

    val playerGradient by remember(playerColor) {
        derivedStateOf {
            Brush.radialGradient(colors = listOf(Color.White, playerColor), radius = 55f)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF020617)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .pointerInput(gameState.isActive) {
                    detectDragGestures(
                        onDragStart  = { offset ->
                            if (gameState.isActive)
                                viewModel.updatePlayerPosition(
                                    offset.x.coerceIn(0f, size.width.toFloat()), dragging = true
                                )
                        },
                        onDrag       = { change, _ ->
                            if (gameState.isActive) {
                                change.consume()
                                viewModel.updatePlayerPosition(
                                    change.position.x.coerceIn(0f, size.width.toFloat()), dragging = true
                                )
                            }
                        },
                        onDragEnd    = { viewModel.updatePlayerPosition(gameState.playerX, dragging = false) },
                        onDragCancel = { viewModel.updatePlayerPosition(gameState.playerX, dragging = false) }
                    )
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = visualState.screenShakeX.dp, y = visualState.screenShakeY.dp)
            ) {
                if (gameState.screenWidth == 0f || gameState.screenHeight == 0f) {
                    viewModel.updateScreenDimensions(size.width, size.height)
                }

                drawGameBackground(mainColor)

                val hazardPath = Path()
                items.forEach { item -> drawGameItem(item, hazardPath) }

                drawParticles(particles)

                drawPlayer(
                    playerX        = gameState.playerX,
                    playerY        = size.height * 0.85f,
                    playerGradient = playerGradient,
                    glowColor      = playerColor,
                    // ✅ FIX : glow réduit → supprime le cercle visible autour du joueur
                    glowSize       = 46f
                )
            }

            if (gameState.isActive) {
                GameHud(
                    gameState               = gameState,
                    shieldTimeRemaining     = gameState.shieldTimeRemaining,
                    multiplierTimeRemaining = gameState.multiplierTimeRemaining,
                    onQuit                  = onBackToMenu
                )
            }

            if (!gameState.isActive && gameState.score > 0) {
                GameOverOverlay(
                    gameState    = gameState,
                    onReplay     = { viewModel.startGame() },
                    onContinue   = { viewModel.continueAfterAd() },
                    onBackToMenu = onBackToMenu
                )
            }
        }
    }
}