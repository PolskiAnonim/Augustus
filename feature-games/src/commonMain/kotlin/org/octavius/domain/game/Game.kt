package org.octavius.domain.game

import io.github.octaviusframework.annotation.PgEnumType
import org.octavius.domain.EnumWithFormatter
import org.octavius.modules.games.localization.GamesTr

@PgEnumType
enum class GameStatus : EnumWithFormatter<GameStatus> {
    Trash,
    WithoutTheEnd,
    Played,
    ToPlay,
    Playing;

    override fun toDisplayString(): String {
        return when (this) {
            Trash -> GamesTr.Status.trash()
            WithoutTheEnd -> GamesTr.Status.endless()
            Played -> GamesTr.Status.played()
            ToPlay -> GamesTr.Status.toPlay()
            Playing -> GamesTr.Status.playing()
        }
    }
}
