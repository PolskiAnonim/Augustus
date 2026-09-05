package org.octavius.feature.books.report.ui

import org.octavius.feature.books.report.AuthorsReportStructureBuilder
import org.octavius.localization.Tr
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen
import org.octavius.report.component.ReportHandler
import org.octavius.report.component.ReportView

class AuthorsReportScreen {
    companion object {
        fun create(): Screen {
            val title: String = Tr.Books.Authors.Report.title()
            val reportStructureBuilder = AuthorsReportStructureBuilder()
            val reportHandler = ReportHandler(reportStructureBuilder)
            return ComponentScreen(title) { ReportView(reportHandler) }
        }
    }
}
