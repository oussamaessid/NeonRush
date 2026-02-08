package app.neonrush.data.model

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class ItemType {
    BONUS, HAZARD, SHIELD, MULTIPLIER
}

data class GameItem(
    val id: Long = Random.nextLong(),
    var x: Float,
    var y: Float,
    val size: Float,
    val type: ItemType,
    val speed: Float,
    var rotation: Float = 0f,
    val rotationSpeed: Float = Random.nextFloat() * 5f,
    var pulsePhase: Float = Random.nextFloat() * 6.28f
)

data class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    var life: Float = 1.0f,
    val color: Color,
    val size: Float = 8f
)