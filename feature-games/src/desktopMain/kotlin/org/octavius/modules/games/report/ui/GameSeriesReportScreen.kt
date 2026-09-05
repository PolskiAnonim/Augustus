package org.octavius.modules.games.report.ui

import org.octavius.localization.Tr
import org.octavius.modules.games.report.GameSeriesReportStructureBuilder
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen
import org.octavius.report.component.ReportHandler
import org.octavius.report.component.ReportView

class GameSeriesReportScreen {
    companion object {
        fun create(): Screen {
            val title: String = Tr.Games.Series.title()
            val reportStructureBuilder = GameSeriesReportStructureBuilder()
            val reportHandler = ReportHandler(reportStructureBuilder)
            return ComponentScreen(title) { ReportView(reportHandler) }
        }
    }
}
