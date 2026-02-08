package app.neonrush.domain.usecase

import app.neonrush.data.model.GameItem
import app.neonrush.data.model.ItemType
import kotlin.math.abs
import kotlin.random.Random

class SpawnItemUseCase {

    fun execute(
        screenWidth: Float,
        playerX: Float,
        currentSpeed: Float,
        elapsedTime: Long,
        difficultyLevel: Float,
        hasShield: Boolean,
        scoreMultiplier: Float,
        slowMotionActive: Boolean
    ): List<GameItem> {

        if (screenWidth <= 0) return emptyList()

        // Zone de sécurité rotative
        val safeZoneIndex = ((System.currentTimeMillis() / 3000) % 5).toInt()
        val zoneWidth = screenWidth / 5f
        val safeZoneStart = safeZoneIndex * zoneWidth
        val safeZoneEnd = safeZoneStart + zoneWidth

        // ============================================
        // RATIO DE HAZARDS RÉDUIT = PLUS DE CARRÉS BLEUS
        // ============================================
        val hazardRatio = when {
            elapsedTime < 8  -> 0.30f   // ← RÉDUIT de 0.40f à 0.30f
            elapsedTime < 15 -> 0.35f   // ← RÉDUIT de 0.45f à 0.35f
            difficultyLevel < 7f  -> 0.40f   // ← RÉDUIT de 0.50f à 0.40f
            difficultyLevel < 12f -> 0.45f   // ← RÉDUIT de 0.58f à 0.45f
            difficultyLevel < 18f -> 0.50f   // ← RÉDUIT de 0.65f à 0.50f
            difficultyLevel < 25f -> 0.55f   // ← RÉDUIT de 0.70f à 0.55f
            else -> 0.60f                     // ← RÉDUIT de 0.74f à 0.60f
        }.let { base ->
            if (hasShield || scoreMultiplier > 1f) base + 0.05f  // ← RÉDUIT de 0.06f
            else base
        }

        val items = mutableListOf<GameItem>()

        // Power-ups spawn
        val powerUpChance = Random.nextFloat()
        if (powerUpChance < 0.025f && !hasShield) {
            items.add(createSmartItem(
                screenWidth, playerX, safeZoneStart, safeZoneEnd,
                safeZoneIndex, zoneWidth, currentSpeed, slowMotionActive,
                ItemType.SHIELD
            ))
        } else if (powerUpChance < 0.055f && scoreMultiplier <= 1f) {
            items.add(createSmartItem(
                screenWidth, playerX, safeZoneStart, safeZoneEnd,
                safeZoneIndex, zoneWidth, currentSpeed, slowMotionActive,
                ItemType.MULTIPLIER
            ))
        }

        // ============================================
        // BURST COUNT AUGMENTÉ = PLUS D'ITEMS SPAWN
        // ============================================
        val burstCount = when {
            difficultyLevel < 4f  -> 2           // ← AUGMENTÉ de 1 à 2
            difficultyLevel < 9f  -> if (Random.nextFloat() < 0.40f) 3 else 2  // ← AUGMENTÉ
            difficultyLevel < 16f -> when {
                Random.nextFloat() < 0.30f -> 4  // ← AUGMENTÉ
                Random.nextFloat() < 0.55f -> 3  // ← AUGMENTÉ
                else -> 2                         // ← AUGMENTÉ
            }
            else -> when {
                Random.nextFloat() < 0.25f -> 5  // ← AUGMENTÉ
                Random.nextFloat() < 0.50f -> 4  // ← AUGMENTÉ
                Random.nextFloat() < 0.70f -> 3  // ← AUGMENTÉ
                else -> 2                         // ← AUGMENTÉ
            }
        }

        repeat(burstCount) {
            val itemType = if (Random.nextFloat() < hazardRatio) {
                ItemType.HAZARD
            } else {
                ItemType.BONUS
            }

            items.add(createSmartItem(
                screenWidth, playerX, safeZoneStart, safeZoneEnd,
                safeZoneIndex, zoneWidth, currentSpeed, slowMotionActive,
                itemType
            ))
        }

        return items
    }

    private fun createSmartItem(
        screenWidth: Float,
        playerX: Float,
        safeZoneStart: Float,
        safeZoneEnd: Float,
        safeZoneIndex: Int,
        zoneWidth: Float,
        currentSpeed: Float,
        slowMotionActive: Boolean,
        forceType: ItemType? = null
    ): GameItem {
        var itemX = Random.nextFloat() * screenWidth
        var itemType = forceType ?: ItemType.BONUS

        if (itemType == ItemType.HAZARD) {
            if (itemX >= safeZoneStart && itemX <= safeZoneEnd) {
                if (Random.nextFloat() < 0.75f) {
                    val otherZones = listOf(0, 1, 2, 3, 4).filter { it != safeZoneIndex }
                    val randomZone = otherZones.random()
                    itemX = randomZone * zoneWidth + Random.nextFloat() * zoneWidth
                } else {
                    itemType = ItemType.BONUS
                }
            }

            val distToPlayer = abs(itemX - playerX)
            if (distToPlayer < 120f && Random.nextFloat() < 0.6f) {
                if (playerX < screenWidth / 2) {
                    itemX = (playerX + 150f).coerceAtMost(screenWidth - 50f)
                } else {
                    itemX = (playerX - 150f).coerceAtLeast(50f)
                }
            }
        }

        val itemSize = when (itemType) {
            ItemType.SHIELD     -> 72f
            ItemType.MULTIPLIER -> 72f
            ItemType.BONUS      -> 62f
            ItemType.HAZARD     -> 88f
        }

        val speedVariation = if (slowMotionActive) 0.5f else 1.0f

        return GameItem(
            x = itemX,
            y = -60f,
            size = itemSize,
            type = itemType,
            speed = (currentSpeed + Random.nextFloat() * 1.8f) * speedVariation
        )
    }
}