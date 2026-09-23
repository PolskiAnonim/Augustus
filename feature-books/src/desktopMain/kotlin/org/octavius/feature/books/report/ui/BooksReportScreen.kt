package org.octavius.feature.books.report.ui

import org.octavius.feature.books.localization.BooksTr
import org.octavius.feature.books.report.BooksReportStructureBuilder
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen
import org.octavius.report.component.ReportHandler
import org.octavius.report.component.ReportView

class BooksReportScreen {
    companion object {
        fun create(): Screen {
            val title: String = BooksTr.Report.title()
            val reportStructureBuilder = BooksReportStructureBuilder()
            val reportHandler = ReportHandler(reportStructureBuilder)
            return ComponentScreen(title) { ReportView(reportHandler) }
        }
    }
}
