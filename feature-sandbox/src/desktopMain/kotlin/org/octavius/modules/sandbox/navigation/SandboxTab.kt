package org.octavius.modules.sandbox.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.octavius.modules.sandbox.localization.SandboxTr
import org.octavius.navigation.Screen
import org.octavius.navigation.Tab
import org.octavius.navigation.TabOptions

class SandboxTab : Tab {
    override val id = "sandbox"

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            title = SandboxTr.tab(),
            icon = rememberVectorPainter(Icons.Filled.Science)
        )

    override fun getInitialScreen(): Screen = SandboxHomeScreen.create()
}
