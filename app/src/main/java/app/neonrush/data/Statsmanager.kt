package app.neonrush.data

import android.content.Context

data class GameStats(
    val bestScore: Int  = 0,
    val bestTime: Long  = 0L,
    val totalGames: Int = 0,
    val totalBonusCaught: Int    = 0,
    val totalHazardsDodged: Int  = 0
)

class StatsManager(context: Context) {

    private val prefs = context.getSharedPreferences("neon_rush_stats", Context.MODE_PRIVATE)

    fun getStats() = GameStats(
        bestScore          = prefs.getInt("best_score", 0),
        bestTime           = prefs.getLong("best_time", 0L),
        totalGames         = prefs.getInt("total_games", 0),
        totalBonusCaught   = prefs.getInt("total_bonus", 0),
        totalHazardsDodged = prefs.getInt("total_hazards", 0)
    )

    fun recordGame(
        score: Int,
        time: Long,
        bonusCaught: Int,
        hazardsDodged: Int
    ) {
        val current = getStats()
        prefs.edit()
            .putInt("best_score",    maxOf(current.bestScore, score))
            .putLong("best_time",    maxOf(current.bestTime, time))
            .putInt("total_games",   current.totalGames + 1)
            .putInt("total_bonus",   current.totalBonusCaught + bonusCaught)
            .putInt("total_hazards", current.totalHazardsDodged + hazardsDodged)
            .apply()
    }
}