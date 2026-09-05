package org.octavius.modules.asian.report.ui

import org.octavius.localization.Tr
import org.octavius.modules.asian.report.AsianMediaReportStructureBuilder
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen
import org.octavius.report.component.ReportHandler
import org.octavius.report.component.ReportView

class AsianMediaReportScreen {
    companion object {
        fun create(): Screen {
            val title = Tr.AsianMedia.Report.title()
            val builder = AsianMediaReportStructureBuilder()
            val reportHandler = ReportHandler(builder)
            return ComponentScreen(title) { ReportView(reportHandler) }
        }
    }
}
