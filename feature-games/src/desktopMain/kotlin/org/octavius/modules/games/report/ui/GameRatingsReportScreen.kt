package org.octavius.modules.games.report.ui

import org.octavius.localization.Tr
import org.octavius.modules.games.report.GameRatingsReportStructureBuilder
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen
import org.octavius.report.component.ReportHandler
import org.octavius.report.component.ReportView

class GameRatingsReportScreen {
    companion object {
        fun create(): Screen {
            val title = Tr.Games.Details.title()
            val reportStructureBuilder = GameRatingsReportStructureBuilder()
            val reportHandler = ReportHandler(reportStructureBuilder)
            return ComponentScreen(title) { ReportView(reportHandler) }
        }
    }
}
