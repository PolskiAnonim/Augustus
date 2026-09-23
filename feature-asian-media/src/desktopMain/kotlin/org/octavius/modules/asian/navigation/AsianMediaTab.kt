package org.octavius.modules.asian.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.octavius.modules.asian.home.ui.AsianMediaHomeScreen
import org.octavius.modules.asian.localization.AsianMediaTr
import org.octavius.navigation.Screen
import org.octavius.navigation.Tab
import org.octavius.navigation.TabOptions

class AsianMediaTab : Tab {
    override val id = "asian_media"

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            title = AsianMediaTr.tab(),
            icon = rememberVectorPainter(Icons.AutoMirrored.Filled.MenuBook)
        )

    override fun getInitialScreen(): Screen = AsianMediaHomeScreen.create()
}