package org.octavius.feature.books.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import io.github.octaviusframework.client.query.QueryFragment
import org.octavius.feature.books.form.author.ui.BookAuthorFormScreen
import org.octavius.localization.Tr
import org.octavius.navigation.AppRouter
import org.octavius.report.ReportMainAction
import org.octavius.report.ReportRowAction
import org.octavius.report.column.ReportColumn
import org.octavius.report.column.type.InstantColumn
import org.octavius.report.column.type.LongColumn
import org.octavius.report.column.type.StringColumn
import org.octavius.report.component.ReportStructureBuilder

class AuthorsReportStructureBuilder : ReportStructureBuilder() {

    override fun getReportName(): String = "books_authors"

    override fun buildQuery(): QueryFragment = QueryFragment(
        sql = """
            SELECT
                a.id,
                a.name,
                a.sort_name,
                a.created_at,
                COALESCE(b.book_count, 0) as book_count
            FROM books.authors a
            LEFT JOIN (
                SELECT
                    author_id,
                    COUNT(*) as book_count
                FROM books.book_to_authors
                GROUP BY author_id
            ) b ON a.id = b.author_id
        """.trimIndent()
    )

    override fun buildColumns(): Map<String, ReportColumn> = mapOf(
        "name" to StringColumn(
            header = Tr.Books.Authors.Report.name()
        ),
        "sort_name" to StringColumn(
            header = Tr.Books.Authors.Report.sortName()
        ),
        "book_count" to LongColumn(
            header = Tr.Books.Authors.Report.bookCount()
        ),
        "created_at" to InstantColumn(
            header = Tr.Books.Authors.Report.createdAt()
        )
    )

    override fun buildDefaultRowAction(): ReportRowAction = ReportRowAction(
        label = Tr.Books.Authors.Report.editAuthor(),
        icon = Icons.Default.Edit
    ) {
        val authorId = rowData["id"] as? Int
        if (authorId != null) {
            AppRouter.navigateTo(BookAuthorFormScreen.create(authorId))
        }
    }

    override fun buildMainActions(): List<ReportMainAction> = listOf(
        ReportMainAction(
            label = Tr.Books.Authors.Report.newAuthor(),
            icon = Icons.Default.Add
        ) {
            AppRouter.navigateTo(BookAuthorFormScreen.create())
        }
    )
}