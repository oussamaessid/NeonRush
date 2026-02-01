package com.example.neonrush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Modèles de données pour le jeu
 */
enum class ItemType { BONUS, HAZARD }

data class GameItem(
    val id: Long = Random.nextLong(),
    var x: Float,
    var y: Float,
    val size: Float,
    val type: ItemType,
    val speed: Float,
    var rotation: Float = 0f,
    val rotationSpeed: Float = Random.nextFloat() * 5f
)

data class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    var life: Float = 1.0f,
    val color: Color
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF020617)) {
                NeonRushGame()
            }
        }
    }
}

@Composable
fun NeonRushGame() {
    var score by remember { mutableIntStateOf(0) }
    var gameActive by remember { mutableStateOf(false) }
    var playerX by remember { mutableFloatStateOf(0f) }
    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }

    val items = remember { mutableStateListOf<GameItem>() }
    val particles = remember { mutableStateListOf<Particle>() }

    // Boucle de jeu principale
    LaunchedEffect(gameActive) {
        if (gameActive) {
            var lastSpawnTime = 0L
            while (gameActive) {
                val currentTime = System.currentTimeMillis()

                // Spawn d'objets
                if (currentTime - lastSpawnTime > (1000 - (score * 10)).coerceAtLeast(400)) {
                    if (screenWidth > 0) {
                        val isBonus = Random.nextFloat() > 0.25f
                        items.add(
                            GameItem(
                                x = Random.nextFloat() * screenWidth,
                                y = -50f,
                                size = if (isBonus) 60f else 80f,
                                type = if (isBonus) ItemType.BONUS else ItemType.HAZARD,
                                speed = 10f + (score * 0.5f) + Random.nextFloat() * 5f
                            )
                        )
                    }
                    lastSpawnTime = currentTime
                }

                // Mise à jour de la physique
                val iterator = items.listIterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    item.y += item.speed
                    item.rotation += item.rotationSpeed

                    // Collision
                    val distance = hypot(playerX - item.x, (screenHeight * 0.85f) - item.y)
                    if (distance < (50f + item.size / 2)) {
                        if (item.type == ItemType.BONUS) {
                            score++
                            spawnParticles(particles, item.x, item.y, Color(0xFF4ADE80))
                            iterator.remove()
                        } else {
                            gameActive = false
                        }
                    } else if (item.y > screenHeight + 100) {
                        iterator.remove()
                    }
                }

                // Mise à jour particules
                val pIterator = particles.listIterator()
                while (pIterator.hasNext()) {
                    val p = pIterator.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.life -= 0.05f
                    if (p.life <= 0) pIterator.remove()
                }

                delay(16) // ~60 FPS
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                playerX = change.position.x.coerceIn(0f, size.width.toFloat())
            }
        }
    ) {
        // Canvas de rendu du jeu
        Canvas(modifier = Modifier.fillMaxSize()) {
            screenWidth = size.width
            screenHeight = size.height
            if (playerX == 0f) playerX = screenWidth / 2

            // Dessiner les items
            items.forEach { item ->
                rotate(item.rotation, pivot = Offset(item.x, item.y)) {
                    if (item.type == ItemType.BONUS) {
                        drawRect(
                            color = Color(0xFF4ADE80),
                            topLeft = Offset(item.x - item.size / 2, item.y - item.size / 2),
                            size = Size(item.size, item.size)
                        )
                    } else {
                        drawHazard(item.x, item.y, item.size)
                    }
                }
            }

            // Dessiner les particules
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.life),
                    radius = 8f,
                    center = Offset(p.x, p.y)
                )
            }

            // Dessiner le joueur
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFF38BDF8)),
                    center = Offset(playerX, screenHeight * 0.85f),
                    radius = 50f
                ),
                radius = 50f,
                center = Offset(playerX, screenHeight * 0.85f)
            )
        }

        // Interface UI Score
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SCORE",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = score.toString(),
                color = Color(0xFF38BDF8),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Menu Principal
        if (!gameActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (score == 0) "NEON RUSH" else "GAME OVER",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (score > 0) {
                            Text(
                                text = "Score final: $score",
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Button(
                            onClick = {
                                score = 0
                                items.clear()
                                particles.clear()
                                gameActive = true
                            },
                            modifier = Modifier
                                .padding(top = 24.dp)
                                .height(56.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text("JOUER", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

fun DrawScope.drawHazard(x: Float, y: Float, size: Float) {
    val path = Path().apply {
        moveTo(x, y - size / 2)
        lineTo(x + size / 2, y + size / 2)
        lineTo(x - size / 2, y + size / 2)
        close()
    }
    drawPath(path, color = Color(0xFFF43F5E))
}

fun spawnParticles(list: MutableList<Particle>, x: Float, y: Float, color: Color) {
    repeat(10) {
        list.add(Particle(
            x = x, y = y,
            vx = (Random.nextFloat() - 0.5f) * 15f,
            vy = (Random.nextFloat() - 0.5f) * 15f,
            color = color
        ))
    }
}