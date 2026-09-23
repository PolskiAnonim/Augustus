package org.octavius.modules.games.report.ui

import org.octavius.modules.games.localization.GamesTr
import org.octavius.modules.games.report.GameCategoriesReportStructureBuilder
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen
import org.octavius.report.component.ReportHandler
import org.octavius.report.component.ReportView

class GameCategoriesReportScreen {
    companion object {
        fun create(): Screen {
            val title: String = GamesTr.Categories.title()
            val reportStructureBuilder = GameCategoriesReportStructureBuilder()
            val reportHandler = ReportHandler(reportStructureBuilder)
            return ComponentScreen(title) { ReportView(reportHandler) }
        }
    }
}
