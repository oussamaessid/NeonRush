package app.neonrush.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.neonrush.data.model.*
import app.neonrush.data.repository.GameRepository
import app.neonrush.domain.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.random.Random

class GameViewModel(
    private val repository: GameRepository = GameRepository(),
    private val spawnItemUseCase: SpawnItemUseCase = SpawnItemUseCase(),
    private val collisionUseCase: CollisionDetectionUseCase = CollisionDetectionUseCase(),
    private val difficultyUseCase: CalculateDifficultyUseCase = CalculateDifficultyUseCase()
) : ViewModel() {

    val gameState = repository.gameState.stateAsStateFlow(viewModelScope)
    val items = repository.items.stateAsStateFlow(viewModelScope)
    val particles = repository.particles.stateAsStateFlow(viewModelScope)

    private val _visualState = MutableStateFlow(VisualState())
    val visualState = _visualState.asStateFlow()

    private val _powerUpState = MutableStateFlow(PowerUpState())
    val powerUpState = _powerUpState.asStateFlow()

    private var lastSpawnTime = 0L
    private var startTime = 0L
    private var lastBonusTime = 0L
    private var shakeDecay = 0f

    private var targetX = 0f
    private var isDragging = false

    // ✅ COMBO AVEC TIMER (comme shield/multiplier)
    private var comboEndTime = 0L
    private var lastComboCheck = 0
    private var comboJustBroke = false

    fun startGame() {
        repository.resetGame()
        startTime = System.currentTimeMillis()
        lastSpawnTime = 0L
        lastBonusTime = 0L
        targetX = gameState.value.playerX
        lastComboCheck = 0
        comboJustBroke = false
        comboEndTime = 0L  // ✅ Reset du timer combo

        repository.updateGameState { it.copy(isActive = true, currentSpeed = 12f) }
        _powerUpState.value = PowerUpState()
        _visualState.value = VisualState()

        viewModelScope.launch {
            runGameLoop()
        }
    }

    fun stopGame() {
        repository.updateGameState {
            it.copy(
                isActive = false,
                bestScore = maxOf(it.bestScore, it.score),
                bestTime = maxOf(it.bestTime, it.elapsedTime)
            )
        }
    }

    fun updatePlayerPosition(x: Float, dragging: Boolean) {
        isDragging = dragging
        targetX = x.coerceIn(0f, gameState.value.screenWidth)
    }

    fun updateScreenDimensions(width: Float, height: Float) {
        repository.updateGameState { state ->
            val newPlayerX = if (state.playerX == 0f) width / 2 else state.playerX
            state.copy(
                screenWidth = width,
                screenHeight = height,
                playerX = newPlayerX
            )
        }
        if (targetX == 0f) targetX = width / 2
    }

    private suspend fun runGameLoop() {
        while (gameState.value.isActive && coroutineContext.isActive) {
            val currentTime = System.currentTimeMillis()

            updateElapsedTime(currentTime)
            updatePlayerMovement()
            updatePowerUps(currentTime)
            updateComboTimer(currentTime)  // ✅ NOUVEAU: Gestion du timer combo
            updateDifficulty()
            checkComboReward(currentTime)
            spawnItems(currentTime)
            updateItems(currentTime)
            updateParticles()
            updateVisualEffects()

            _visualState.value = _visualState.value.copy(
                gameFrame = _visualState.value.gameFrame + 1
            )

            delay(16)
        }
    }

    private fun updateElapsedTime(currentTime: Long) {
        val elapsedTime = (currentTime - startTime) / 1000
        repository.updateGameState { it.copy(elapsedTime = elapsedTime) }
    }

    private fun updatePlayerMovement() {
        if (!isDragging) return

        val state = gameState.value
        val diff = targetX - state.playerX

        val newPlayerX = if (abs(diff) < 2f) {
            targetX
        } else {
            state.playerX + diff * 0.22f
        }

        repository.updateGameState { it.copy(playerX = newPlayerX) }
    }

    private fun updatePowerUps(currentTime: Long) {
        val powerUp = _powerUpState.value

        // Shield
        if (powerUp.hasShield && currentTime > powerUp.shieldEndTime) {
            _powerUpState.value = powerUp.copy(hasShield = false)
            repository.updateGameState { it.copy(hasShield = false, shieldTimeRemaining = 0) }
        } else if (powerUp.hasShield) {
            val remaining = ((powerUp.shieldEndTime - currentTime) / 1000).toInt() + 1
            repository.updateGameState { it.copy(shieldTimeRemaining = remaining) }
        }

        // Multiplier
        if (powerUp.scoreMultiplier > 1f && currentTime > powerUp.multiplierEndTime) {
            _powerUpState.value = powerUp.copy(scoreMultiplier = 1f)
            repository.updateGameState { it.copy(scoreMultiplier = 1f, multiplierTimeRemaining = 0) }
        } else if (powerUp.scoreMultiplier > 1f) {
            val remaining = ((powerUp.multiplierEndTime - currentTime) / 1000).toInt() + 1
            repository.updateGameState { it.copy(multiplierTimeRemaining = remaining) }
        }

        // Slow Motion
        if (powerUp.slowMotionActive && currentTime > powerUp.slowMotionEndTime) {
            _powerUpState.value = powerUp.copy(slowMotionActive = false)
            _visualState.value = _visualState.value.copy(slowMotionActive = false)
        }
    }

    // ✅ NOUVEAU: Gestion du timer de combo (comme shield/multiplier)
    private fun updateComboTimer(currentTime: Long) {
        val state = gameState.value

        // Si combo actif ET timer expiré → reset à 0
        if (state.combo >= 3 && currentTime > comboEndTime) {
            repository.updateGameState { it.copy(combo = 0, comboTimeRemaining = 0) }
            println("⏱️ COMBO EXPIRED: combo reset à 0")
        }
        // Si combo actif → afficher le temps restant
        else if (state.combo >= 3) {
            val remaining = ((comboEndTime - currentTime) / 1000).toInt() + 1
            repository.updateGameState { it.copy(comboTimeRemaining = remaining) }
        }
    }

    private fun updateDifficulty() {
        val state = gameState.value
        val settings = difficultyUseCase.execute(state.elapsedTime, state.score)
        repository.updateGameState { it.copy(currentSpeed = settings.speed) }
    }

    // ============================================
    // SYSTÈME DE RÉCOMPENSE COMBO
    // ============================================
    private fun checkComboReward(currentTime: Long) {
        val state = gameState.value

        // Vérifier si le combo vient de se casser (était >= 5, maintenant < 3)
        if (lastComboCheck >= 5 && state.combo < 3) {
            // Vérifier qu'on n'a pas déjà spawné et qu'assez de temps s'est écoulé
            if (!comboJustBroke && currentTime - lastBonusTime > 1500 && state.screenWidth > 0) {
                // Calculer le nombre de ballons en fonction du combo précédent
                val balloonCount = when {
                    lastComboCheck >= 15 -> 3  // Combo 15+ = 3 ballons
                    lastComboCheck >= 10 -> 2  // Combo 10+ = 2 ballons
                    else -> 1                   // Combo 5+ = 1 ballon
                }

                // Spawner plusieurs ballons bleus (SHIELD)
                repeat(balloonCount) { index ->
                    val offsetRange = 250f
                    val offset = when (index) {
                        0 -> 0f              // Centre
                        1 -> -offsetRange    // Gauche
                        else -> offsetRange  // Droite
                    }

                    val balloonX = (state.playerX + offset)
                        .coerceIn(50f, state.screenWidth - 50f)

                    repository.addItem(
                        GameItem(
                            x = balloonX,
                            y = -60f - (index * 80f), // Décalage vertical
                            size = 72f,
                            type = ItemType.SHIELD,
                            speed = state.currentSpeed * 0.65f
                        )
                    )
                }

                comboJustBroke = true
                println("🎁 COMBO REWARD! Combo était $lastComboCheck → $balloonCount ballon(s)")
            }
        }

        // Réinitialiser le flag quand le combo recommence
        if (state.combo >= 3) {
            comboJustBroke = false
        }

        lastComboCheck = state.combo
    }

    private fun spawnItems(currentTime: Long) {
        val state = gameState.value
        val difficultyLevel = (state.elapsedTime / 10f) + (state.score / 40f)
        val settings = difficultyUseCase.execute(state.elapsedTime, state.score)

        if (items.value.size >= settings.maxItems) return
        if (currentTime - lastSpawnTime <= settings.spawnInterval) return

        val newItems = spawnItemUseCase.execute(
            screenWidth = state.screenWidth,
            playerX = state.playerX,
            currentSpeed = state.currentSpeed,
            elapsedTime = state.elapsedTime,
            difficultyLevel = difficultyLevel,
            hasShield = _powerUpState.value.hasShield,
            scoreMultiplier = _powerUpState.value.scoreMultiplier,
            slowMotionActive = _powerUpState.value.slowMotionActive
        )

        newItems.forEach { repository.addItem(it) }
        lastSpawnTime = currentTime
    }

    private suspend fun updateItems(currentTime: Long) {
        val state = gameState.value
        val powerUp = _powerUpState.value
        val playerY = state.screenHeight * 0.85f

        val itemsToRemove = mutableListOf<GameItem>()

        items.value.forEach { item ->
            // Update position
            val speedMod = if (powerUp.slowMotionActive) 0.4f else 1.0f
            val updatedItem = item.copy(
                y = item.y + item.speed * speedMod,
                rotation = item.rotation + item.rotationSpeed,
                pulsePhase = item.pulsePhase + 0.08f
            )
            repository.updateItem(item.id) { updatedItem }

            // Check collision
            val collision = collisionUseCase.checkCollision(
                item = updatedItem,
                playerX = state.playerX,
                playerY = playerY,
                hasShield = powerUp.hasShield,
                scoreMultiplier = powerUp.scoreMultiplier,
                combo = state.combo,
                elapsedTime = state.elapsedTime,
                currentTime = currentTime,
                lastBonusTime = lastBonusTime
            )

            if (collision.hit) {
                handleCollision(collision, updatedItem, currentTime)
                itemsToRemove.add(updatedItem)
            } else {
                // Near miss check
                if (collisionUseCase.checkNearMiss(updatedItem, state.playerX, playerY, state.screenHeight)) {
                    handleNearMiss()
                }

                // Remove if off screen
                if (updatedItem.y > state.screenHeight + 100) {
                    if (updatedItem.type == ItemType.HAZARD) {
                        repository.updateGameState { it.copy(hazardsDodged = it.hazardsDodged + 1) }
                    }
                    itemsToRemove.add(updatedItem)
                }
            }
        }

        itemsToRemove.forEach { repository.removeItem(it) }
    }

    private suspend fun handleCollision(
        collision: CollisionResult,
        item: GameItem,
        currentTime: Long
    ) {
        when {
            collision.gameOver -> handleGameOver(item, currentTime)
            collision.shieldBlocked -> handleShieldBlock(item)
            collision.activateShield -> activateShield(item, currentTime)
            collision.activateMultiplier -> activateMultiplier(item, currentTime)
            collision.comboIncrement -> handleBonusWithCombo(collision, item, currentTime)
            else -> handleBonus(collision, item, currentTime)
        }
    }

    private suspend fun handleGameOver(item: GameItem, currentTime: Long) {
        _powerUpState.value = _powerUpState.value.copy(
            slowMotionActive = true,
            slowMotionEndTime = currentTime + 800
        )
        _visualState.value = _visualState.value.copy(slowMotionActive = true)

        addParticles(item.x, item.y, Color(0xFFF43F5E), 32)
        applyScreenShake(16f, 0.9f)

        delay(400)
        stopGame()
    }

    private fun handleShieldBlock(item: GameItem) {
        addParticles(item.x, item.y, Color(0xFF38BDF8), 24)
        applyScreenShake(8f, 0.85f)
    }

    private fun activateShield(item: GameItem, currentTime: Long) {
        _powerUpState.value = _powerUpState.value.copy(
            hasShield = true,
            shieldEndTime = currentTime + 5500
        )
        repository.updateGameState { it.copy(hasShield = true) }
        addParticles(item.x, item.y, Color(0xFF38BDF8), 16)
    }

    private fun activateMultiplier(item: GameItem, currentTime: Long) {
        _powerUpState.value = _powerUpState.value.copy(
            scoreMultiplier = 2f,
            multiplierEndTime = currentTime + 6500
        )
        repository.updateGameState { it.copy(scoreMultiplier = 2f) }
        addParticles(item.x, item.y, Color(0xFFfbbf24), 16)
    }

    // ✅ COMBO INCREMENT avec timer de 3 secondes
    private fun handleBonusWithCombo(collision: CollisionResult, item: GameItem, currentTime: Long) {
        val newCombo = gameState.value.combo + 1
        lastBonusTime = currentTime
        comboEndTime = currentTime + 3000  // ✅ Combo dure 3 secondes

        repository.updateGameState {
            it.copy(
                score = it.score + collision.points,
                greensCaught = it.greensCaught + 1,
                combo = newCombo
            )
        }

        val particleCount = 10 + (newCombo.coerceAtMost(10) * 2)
        addParticles(item.x, item.y, Color(0xFF38BDF8), particleCount)

        if (newCombo > 5) {
            addParticles(item.x, item.y, Color(0xFFa78bfa), 6)
        }

        println("✅ COMBO INCREMENT: combo = $newCombo, expire dans 3s")
    }

    // ✅ COMBO START à 3 avec timer de 3 secondes
    private fun handleBonus(collision: CollisionResult, item: GameItem, currentTime: Long) {
        lastBonusTime = currentTime
        comboEndTime = currentTime + 3000  // ✅ Combo dure 3 secondes

        repository.updateGameState {
            it.copy(
                score = it.score + collision.points,
                greensCaught = it.greensCaught + 1,
                combo = 3  // ← COMMENCE À 3
            )
        }
        addParticles(item.x, item.y, Color(0xFF38BDF8), 10)

        println("🔄 COMBO START: combo = 3, expire dans 3s")
    }

    private fun handleNearMiss() {
        repository.updateGameState { state ->
            val newCount = state.nearMissCount + 1
            val bonusPoints = if (newCount % 3 == 0) 1 else 0
            state.copy(
                nearMissCount = newCount,
                score = state.score + bonusPoints
            )
        }
    }

    private fun updateParticles() {
        repository.updateParticles { particles ->
            particles
                .map { p ->
                    p.copy(
                        x = p.x + p.vx,
                        y = p.y + p.vy,
                        life = p.life - 0.04f
                    )
                }
                .filter { it.life > 0 }
                .takeLast(200)
        }
    }

    private fun updateVisualEffects() {
        val visual = _visualState.value

        if (abs(visual.screenShakeX) > 0.1f || abs(visual.screenShakeY) > 0.1f) {
            _visualState.value = visual.copy(
                screenShakeX = visual.screenShakeX * shakeDecay,
                screenShakeY = visual.screenShakeY * shakeDecay
            )
        } else {
            _visualState.value = visual.copy(screenShakeX = 0f, screenShakeY = 0f)
        }
    }

    private fun addParticles(x: Float, y: Float, color: Color, count: Int) {
        val newParticles = repository.createParticles(x, y, color, count)
        repository.addParticles(newParticles)
    }

    private fun applyScreenShake(intensity: Float, decay: Float) {
        _visualState.value = _visualState.value.copy(
            screenShakeX = Random.nextFloat() * intensity - intensity / 2,
            screenShakeY = Random.nextFloat() * intensity - intensity / 2
        )
        shakeDecay = decay
    }
}

private fun <T> Flow<T>.stateAsStateFlow(scope: CoroutineScope): StateFlow<T> {
    return this.stateIn(scope, SharingStarted.Eagerly, (this as StateFlow<T>).value)
}