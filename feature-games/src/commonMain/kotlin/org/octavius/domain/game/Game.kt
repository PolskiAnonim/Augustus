package org.octavius.domain.game

import io.github.octaviusframework.annotation.PgEnumType
import org.octavius.domain.EnumWithFormatter
import org.octavius.localization.Tr

@PgEnumType
enum class GameStatus : EnumWithFormatter<GameStatus> {
    NotPlaying,
    WithoutTheEnd,
    Played,
    ToPlay,
    Playing;

    override fun toDisplayString(): String {
        return when (this) {
            NotPlaying -> Tr.Games.Status.notPlaying()
            WithoutTheEnd -> Tr.Games.Status.endless()
            Played -> Tr.Games.Status.played()
            ToPlay -> Tr.Games.Status.toPlay()
            Playing -> Tr.Games.Status.playing()
        }
    }
}
