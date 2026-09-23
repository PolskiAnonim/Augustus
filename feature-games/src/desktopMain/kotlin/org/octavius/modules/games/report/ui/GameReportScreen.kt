package org.octavius.modules.games.report.ui

import org.octavius.modules.games.localization.GamesTr
import org.octavius.modules.games.report.GameReportStructureBuilder
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen
import org.octavius.report.component.ReportHandler
import org.octavius.report.component.ReportView

class GameReportScreen {
    companion object {
        fun create(categoryId: Int? = null, seriesId: Int? = null): Screen {
            val title = GamesTr.Report.title()
            val reportStructureBuilder = GameReportStructureBuilder(categoryId, seriesId)
            val reportHandler = ReportHandler(reportStructureBuilder)
            return ComponentScreen(title) { ReportView(reportHandler) }
        }
    }
}
