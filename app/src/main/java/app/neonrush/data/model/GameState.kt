package app.neonrush.data.model

data class GameState(
    val isActive: Boolean = false,
    val score: Int = 0,
    val elapsedTime: Long = 0L,
    val bestScore: Int = 0,
    val bestTime: Long = 0L,
    val playerX: Float = 0f,
    val screenWidth: Float = 0f,
    val screenHeight: Float = 0f,
    val hasShield: Boolean = false,
    val shieldTimeRemaining: Int = 0,
    val scoreMultiplier: Float = 1f,
    val multiplierTimeRemaining: Int = 0,
    val currentSpeed: Float = 12f,
    val combo: Int = 0,
    val greensCaught: Int = 0,
    val hazardsDodged: Int = 0,
    val nearMissCount: Int = 0
)

data class VisualState(
    val screenShakeX: Float = 0f,
    val screenShakeY: Float = 0f,
    val slowMotionActive: Boolean = false,
    val gameFrame: Long = 0L
)

data class PowerUpState(
    val hasShield: Boolean = false,
    val shieldEndTime: Long = 0L,
    val scoreMultiplier: Float = 1f,
    val multiplierEndTime: Long = 0L,
    val slowMotionActive: Boolean = false,
    val slowMotionEndTime: Long = 0L
)