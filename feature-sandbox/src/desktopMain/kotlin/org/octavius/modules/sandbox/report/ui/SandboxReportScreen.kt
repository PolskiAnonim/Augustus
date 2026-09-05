package org.octavius.modules.sandbox.report.ui

import org.octavius.localization.Tr
import org.octavius.modules.sandbox.report.SandboxReportStructureBuilder
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen
import org.octavius.report.component.ReportHandler
import org.octavius.report.component.ReportView

class SandboxReportScreen {
    companion object {
        fun create(): Screen {
            val title = Tr.Sandbox.Report.title()
            val reportStructureBuilder = SandboxReportStructureBuilder()
            val reportHandler = ReportHandler(reportStructureBuilder)
            return ComponentScreen(title) { ReportView(reportHandler) }
        }
    }
}
