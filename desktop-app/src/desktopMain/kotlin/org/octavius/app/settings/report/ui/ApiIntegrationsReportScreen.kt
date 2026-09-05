package org.octavius.app.settings.report.ui

import org.octavius.app.settings.report.ApiIntegrationsReportStructureBuilder
import org.octavius.localization.Tr
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen
import org.octavius.report.component.ReportHandler
import org.octavius.report.component.ReportView

class ApiIntegrationsReportScreen {
    companion object {
        fun create(): Screen {
            val title = Tr.Settings.Api.title()
            val reportStructure = ApiIntegrationsReportStructureBuilder()
            val reportHandler = ReportHandler(reportStructure)
            return ComponentScreen(title) { ReportView(reportHandler) }
        }
    }
}
