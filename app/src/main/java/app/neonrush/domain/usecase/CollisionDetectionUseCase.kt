package app.neonrush.domain.usecase


import app.neonrush.data.model.GameItem
import app.neonrush.data.model.ItemType
import kotlin.math.hypot

data class CollisionResult(
    val hit: Boolean,
    val itemType: ItemType?,
    val shouldRemove: Boolean,
    val points: Int = 0,
    val comboIncrement: Boolean = false,
    val activateShield: Boolean = false,
    val activateMultiplier: Boolean = false,
    val gameOver: Boolean = false,
    val shieldBlocked: Boolean = false
)

class CollisionDetectionUseCase {

    fun checkCollision(
        item: GameItem,
        playerX: Float,
        playerY: Float,
        hasShield: Boolean,
        scoreMultiplier: Float,
        combo: Int,
        elapsedTime: Long,
        lastBonusTime: Long
    ): CollisionResult {

        val playerRadius = 52f
        val itemRadius = item.size / 2f
        val distance = hypot(playerX - item.x, playerY - item.y)
        val collisionDist = playerRadius + itemRadius

        if (distance >= collisionDist) {
            return CollisionResult(hit = false, itemType = null, shouldRemove = false)
        }

        // Collision détectée
        return when (item.type) {
            ItemType.BONUS -> handleBonusCollision(
                scoreMultiplier, combo, elapsedTime, lastBonusTime
            )

            ItemType.SHIELD -> CollisionResult(
                hit = true,
                itemType = ItemType.SHIELD,
                shouldRemove = true,
                activateShield = true
            )

            ItemType.MULTIPLIER -> CollisionResult(
                hit = true,
                itemType = ItemType.MULTIPLIER,
                shouldRemove = true,
                activateMultiplier = true
            )

            ItemType.HAZARD -> handleHazardCollision(hasShield)
        }
    }

    private fun handleBonusCollision(
        scoreMultiplier: Float,
        combo: Int,
        elapsedTime: Long,
        lastBonusTime: Long
    ): CollisionResult {
        val timeBonus = (elapsedTime / 10).toInt()
        val comboBonus = if (combo > 3) combo / 2 else 0
        val basePoints = (1 + timeBonus + comboBonus)
        val points = (basePoints * scoreMultiplier).toInt()

        val currentTime = System.currentTimeMillis()
        val shouldIncrementCombo = currentTime - lastBonusTime < 1500

        return CollisionResult(
            hit = true,
            itemType = ItemType.BONUS,
            shouldRemove = true,
            points = points,
            comboIncrement = shouldIncrementCombo
        )
    }

    private fun handleHazardCollision(hasShield: Boolean): CollisionResult {
        return if (hasShield) {
            CollisionResult(
                hit = true,
                itemType = ItemType.HAZARD,
                shouldRemove = true,
                shieldBlocked = true
            )
        } else {
            CollisionResult(
                hit = true,
                itemType = ItemType.HAZARD,
                shouldRemove = true,
                gameOver = true
            )
        }
    }

    fun checkNearMiss(
        item: GameItem,
        playerX: Float,
        playerY: Float,
        screenHeight: Float
    ): Boolean {
        if (item.type != ItemType.HAZARD) return false
        if (item.y < screenHeight * 0.75f) return false

        val playerRadius = 52f
        val itemRadius = item.size / 2f
        val distance = hypot(playerX - item.x, playerY - item.y)
        val nearMissDist = playerRadius + itemRadius + 35f

        return distance < nearMissDist
    }
}