package org.octavius.feature.books.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import io.github.octaviusframework.client.query.QueryFragment
import org.octavius.feature.books.domain.ReadingStatus
import org.octavius.feature.books.form.book.ui.BookFormScreen
import org.octavius.localization.Tr
import org.octavius.navigation.AppRouter
import org.octavius.report.ReportMainAction
import org.octavius.report.ReportRowAction
import org.octavius.report.column.ReportColumn
import org.octavius.report.column.type.EnumColumn
import org.octavius.report.column.type.InstantColumn
import org.octavius.report.column.type.StringColumn
import org.octavius.report.component.ReportStructureBuilder

class BooksReportStructureBuilder : ReportStructureBuilder() {

    override fun getReportName(): String = "books_books"

    override fun buildQuery(): QueryFragment = QueryFragment(
        sql = """
            SELECT
                b.id,
                b.title_pl,
                b.title_eng,
                b.status,
                b.created_at,
                b.updated_at,
                COALESCE(
                    STRING_AGG(a.name, ', ' ORDER BY a.sort_name),
                    ''
                ) as authors
            FROM books.books b
            LEFT JOIN books.book_to_authors bta ON b.id = bta.book_id
            LEFT JOIN books.authors a ON bta.author_id = a.id
            GROUP BY b.id, b.title_pl, b.title_eng, b.status, b.created_at, b.updated_at
        """.trimIndent()
    )

    override fun buildColumns(): Map<String, ReportColumn> = mapOf(
        "title_pl" to StringColumn(
            header = Tr.Books.Report.titlePl()
        ),
        "title_eng" to StringColumn(
            header = Tr.Books.Report.titleEng()
        ),
        "authors" to StringColumn(
            header = Tr.Books.Report.authors()
        ),
        "status" to EnumColumn(
            header = Tr.Books.Report.status(),
            enumClass = ReadingStatus::class
        ),
        "created_at" to InstantColumn(
            header = Tr.Books.Report.createdAt()
        ),
        "updated_at" to InstantColumn(
            header = Tr.Books.Report.updatedAt()
        )
    )

    override fun buildDefaultRowAction(): ReportRowAction = ReportRowAction(
        label = Tr.Books.Report.editBook(),
        icon = Icons.Default.Edit
    ) {
        val bookId = rowData["id"] as? Int
        if (bookId != null) {
            AppRouter.navigateTo(BookFormScreen.create(bookId))
        }
    }

    override fun buildMainActions(): List<ReportMainAction> = listOf(
        ReportMainAction(
            label = Tr.Books.Report.newBook(),
            icon = Icons.Default.Add
        ) {
            AppRouter.navigateTo(BookFormScreen.create())
        }
    )
}