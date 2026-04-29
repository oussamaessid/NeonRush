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
        val zoneWidth     = screenWidth / 5f
        val safeZoneStart = safeZoneIndex * zoneWidth
        val safeZoneEnd   = safeZoneStart + zoneWidth

        // ============================================================
        // RATIO DE HAZARDS RÉDUIT — encore plus de carrés bleus
        // ============================================================
        val hazardRatio = when {
            elapsedTime < 8  -> 0.20f
            elapsedTime < 15 -> 0.25f
            difficultyLevel < 7f  -> 0.30f
            difficultyLevel < 12f -> 0.35f
            difficultyLevel < 18f -> 0.40f
            difficultyLevel < 25f -> 0.45f
            else -> 0.50f
        }.let { base ->
            if (hasShield || scoreMultiplier > 1f) base + 0.04f
            else base
        }

        val items = mutableListOf<GameItem>()

        // ── Power-ups ──────────────────────────────────────────────
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

        // ── Burst count ────────────────────────────────────────────
        val burstCount = when {
            difficultyLevel < 4f  -> 2
            difficultyLevel < 9f  -> if (Random.nextFloat() < 0.40f) 3 else 2
            difficultyLevel < 16f -> when {
                Random.nextFloat() < 0.30f -> 4
                Random.nextFloat() < 0.55f -> 3
                else -> 2
            }
            else -> when {
                Random.nextFloat() < 0.25f -> 5
                Random.nextFloat() < 0.50f -> 4
                Random.nextFloat() < 0.70f -> 3
                else -> 2
            }
        }

        // ── Spawn du burst avec règle anti-adjacence ───────────────
        // FIX : on ne place jamais deux items du même type
        // à moins de MIN_SAME_TYPE_DIST px l'un de l'autre.
        val MIN_SAME_TYPE_DIST = 170f
        val spawnedThisBurst = mutableListOf<GameItem>()

        repeat(burstCount) {
            var candidate: GameItem? = null

            // On essaie jusqu'à 6 fois de trouver une position valide
            for (attempt in 0 until 6) {
                val wantType = if (Random.nextFloat() < hazardRatio) ItemType.HAZARD else ItemType.BONUS
                val tentative = createSmartItem(
                    screenWidth, playerX, safeZoneStart, safeZoneEnd,
                    safeZoneIndex, zoneWidth, currentSpeed, slowMotionActive,
                    wantType
                )

                // Vérifie que le type n'est pas déjà trop proche d'un item identique
                val tooClose = spawnedThisBurst.any { existing ->
                    existing.type == tentative.type &&
                            abs(existing.x - tentative.x) < MIN_SAME_TYPE_DIST
                }

                if (!tooClose) {
                    candidate = tentative
                    break
                }

                // Dernier essai : force BONUS pour garder de la fluidité
                if (attempt == 5) {
                    candidate = createSmartItem(
                        screenWidth, playerX, safeZoneStart, safeZoneEnd,
                        safeZoneIndex, zoneWidth, currentSpeed, slowMotionActive,
                        ItemType.BONUS
                    )
                }
            }

            candidate?.let { spawnedThisBurst.add(it) }
        }

        items.addAll(spawnedThisBurst)
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
        var itemX    = Random.nextFloat() * screenWidth
        var itemType = forceType ?: ItemType.BONUS

        if (itemType == ItemType.HAZARD) {
            // Déplace hors de la safe zone
            if (itemX >= safeZoneStart && itemX <= safeZoneEnd) {
                if (Random.nextFloat() < 0.75f) {
                    val otherZones = listOf(0, 1, 2, 3, 4).filter { it != safeZoneIndex }
                    val randomZone = otherZones.random()
                    itemX = randomZone * zoneWidth + Random.nextFloat() * zoneWidth
                } else {
                    itemType = ItemType.BONUS
                }
            }

            // Évite de coller au joueur
            val distToPlayer = abs(itemX - playerX)
            if (distToPlayer < 120f && Random.nextFloat() < 0.6f) {
                itemX = if (playerX < screenWidth / 2) {
                    (playerX + 150f).coerceAtMost(screenWidth - 50f)
                } else {
                    (playerX - 150f).coerceAtLeast(50f)
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
            x     = itemX,
            y     = -60f,
            size  = itemSize,
            type  = itemType,
            speed = (currentSpeed + Random.nextFloat() * 1.8f) * speedVariation
        )
    }
}