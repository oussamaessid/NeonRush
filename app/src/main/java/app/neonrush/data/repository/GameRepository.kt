package app.neonrush.data.repository

import app.neonrush.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class GameRepository {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _items = MutableStateFlow<List<GameItem>>(emptyList())
    val items: StateFlow<List<GameItem>> = _items.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    fun updateGameState(update: (GameState) -> GameState) {
        _gameState.value = update(_gameState.value)
    }

    fun addItem(item: GameItem) {
        _items.value = _items.value + item
    }

    fun removeItem(item: GameItem) {
        _items.value = _items.value.filter { it.id != item.id }
    }

    fun updateItem(itemId: Long, update: (GameItem) -> GameItem) {
        _items.value = _items.value.map {
            if (it.id == itemId) update(it) else it
        }
    }

    fun clearItems() {
        _items.value = emptyList()
    }

    fun addParticles(newParticles: List<Particle>) {
        _particles.value = _particles.value + newParticles
    }

    fun updateParticles(update: (List<Particle>) -> List<Particle>) {
        _particles.value = update(_particles.value)
    }

    fun clearParticles() {
        _particles.value = emptyList()
    }

    fun createParticles(x: Float, y: Float, color: androidx.compose.ui.graphics.Color, count: Int): List<Particle> {
        return List(count) {
            val angle = Random.nextFloat() * 6.28f
            val speed = 8f + Random.nextFloat() * 10f
            Particle(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                color = color,
                size = 6f + Random.nextFloat() * 6f
            )
        }
    }

    fun resetGame() {
        _gameState.value = GameState(
            bestScore = _gameState.value.bestScore,
            bestTime = _gameState.value.bestTime
        )
        clearItems()
        clearParticles()
    }
}