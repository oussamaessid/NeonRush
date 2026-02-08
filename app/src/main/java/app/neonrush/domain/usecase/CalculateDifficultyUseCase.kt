package app.neonrush.domain.usecase

data class DifficultySettings(
    val speed: Float,
    val spawnInterval: Long,
    val maxItems: Int
)

class CalculateDifficultyUseCase {

    fun execute(elapsedTime: Long, score: Int): DifficultySettings {
        val difficultyLevel = (elapsedTime / 10f) + (score / 40f)

        val speed = when {
            difficultyLevel < 3f  -> 12f + difficultyLevel * 0.8f
            difficultyLevel < 8f  -> 14.4f + (difficultyLevel - 3f) * 1.0f
            difficultyLevel < 15f -> 19.4f + (difficultyLevel - 8f) * 1.2f
            difficultyLevel < 25f -> 27.8f + (difficultyLevel - 15f) * 1.4f
            else -> 41.8f + (difficultyLevel - 25f) * 0.8f
        }.coerceAtMost(55f)

        val spawnInterval = when {
            elapsedTime < 10 -> 650L
            difficultyLevel < 5f  -> 1100L
            difficultyLevel < 12f -> 850L
            difficultyLevel < 20f -> 550L
            else -> 340L
        }.coerceAtLeast(240L)

        val maxItems = (6 + (difficultyLevel * 0.6f).toInt()).coerceAtMost(22)

        return DifficultySettings(speed, spawnInterval, maxItems)
    }
}