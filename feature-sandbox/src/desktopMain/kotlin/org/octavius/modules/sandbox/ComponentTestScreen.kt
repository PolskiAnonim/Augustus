package org.octavius.modules.sandbox

import androidx.compose.runtime.Composable
import org.octavius.modules.sandbox.localization.SandboxTr
import org.octavius.navigation.Screen
class ComponentTestScreen : Screen {

    override val title: String = SandboxTr.componentTestScreen()

    @Composable
    override fun Content() {

    }
}
