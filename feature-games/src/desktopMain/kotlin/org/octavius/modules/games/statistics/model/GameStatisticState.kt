package org.octavius.modules.games.statistics.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import io.github.octaviusframework.annotation.DynamicallyMappable
import org.octavius.domain.game.GameStatus
import java.math.BigDecimal


interface DashboardGame {
    val id: Int
    val name: String
    val value: String
}

@DynamicallyMappable("game_dashboard_status")
@Serializable
data class DashboardStatusCount(
    @Contextual
    val status: GameStatus,
    val count: Long
)

// Typ dla gry w liście "najwięcej grane"
@DynamicallyMappable("game_dashboard_time")
@Serializable
data class DashboardGameByTime(
    override val id: Int,
    override val name: String,
    @Contextual
    val playTimeHours: BigDecimal
) : DashboardGame {
    override val value: String = "${this.playTimeHours}h"
}

// Typ dla gry w liście "najwyżej oceniane"
@DynamicallyMappable("game_dashboard_rating")
@Serializable
data class DashboardGameByRating(
    override val id: Int,
    override val name: String,
    @Contextual
    val averageRating: BigDecimal
) : DashboardGame {
    override val value: String = averageRating.toString()
}

// Główna data class, która mapuje się na wynik całego zapytania
data class GameStatisticsData(
    val totalGames: Long,
    val totalPlaytimeHours: BigDecimal,
    val playedGamesCount: Long,
    val avgPlaytimeForPlayed: BigDecimal,
    val statusDistribution: List<DashboardStatusCount>?,
    val avgStoryRating: BigDecimal?,
    val avgGameplayRating: BigDecimal?,
    val avgAtmosphereRating: BigDecimal?,
    val mostPlayedGames: List<DashboardGameByTime>?,
    val highestRatedGames: List<DashboardGameByRating>?,
    val favoriteCategoryName: String?,
    val favoriteSeriesName: String?
)

// Stan, którego będzie używać UI
data class GameStatisticsState(
    val data: GameStatisticsData? = null,
    val isLoading: Boolean = true
)