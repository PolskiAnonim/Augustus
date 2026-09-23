package org.octavius.modules.games.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.octavius.modules.games.localization.GamesTr
import org.octavius.navigation.Screen
import org.octavius.navigation.Tab
import org.octavius.navigation.TabOptions

class GameTab : Tab {
    override val id = "games"

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            title = GamesTr.tab(),
            icon = rememberVectorPainter(Icons.Filled.SportsEsports)
        )

    override fun getInitialScreen(): Screen = GamesHomeScreen.create()
}