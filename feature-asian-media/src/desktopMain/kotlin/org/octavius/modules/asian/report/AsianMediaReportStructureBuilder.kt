package org.octavius.modules.asian.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import io.github.octaviusframework.client.query.QueryFragment
import io.github.octaviusframework.client.query.withParam
import org.octavius.domain.asian.PublicationLanguage
import org.octavius.domain.asian.PublicationStatus
import org.octavius.domain.asian.PublicationType
import org.octavius.localization.Tr
import org.octavius.modules.asian.form.ui.AsianMediaFormScreen
import org.octavius.navigation.AppRouter
import org.octavius.report.ReportMainAction
import org.octavius.report.ReportRowAction
import org.octavius.report.column.ReportColumn
import org.octavius.report.column.type.EnumColumn
import org.octavius.report.column.type.StringColumn
import org.octavius.report.column.type.asList
import org.octavius.report.component.ReportStructureBuilder
import org.octavius.report.localization.ReportTr

class AsianMediaReportStructureBuilder() : ReportStructureBuilder() {

    override fun getReportName(): String = "asianMedia"

    override fun buildQuery(): QueryFragment {
        val query = db.select(
            "t.id as title_id",
            "t.titles",
            "t.language",
            "ARRAY_AGG(p.publication_type ORDER BY p.id) AS publication_type",
            "ARRAY_AGG(p.status ORDER BY p.id) as status"
        ).from("titles t JOIN publications p ON p.title_id = t.id").groupBy("t.id").toSql()

        return QueryFragment(query)
    }

    override fun buildColumns(): Map<String, ReportColumn> = mapOf(
        "titles" to StringColumn(
            header = Tr.AsianMedia.Report.titles(),
            width = 2f
        ).asList(5),
        "language" to EnumColumn(
            header = Tr.AsianMedia.Report.language(),
            enumClass = PublicationLanguage::class,
            width = 1f
        ),
        "publication_type" to EnumColumn(
            header = Tr.AsianMedia.Report.publicationType(),
            enumClass = PublicationType::class,
            width = 1.5f
        ).asList(9),
        "status" to EnumColumn(
            header = Tr.AsianMedia.Report.status(),
            enumClass = PublicationStatus::class,
            width = 1.5f
        ).asList(9)
    )

    override fun buildDefaultRowAction(): ReportRowAction = ReportRowAction(ReportTr.Report.Actions.edit()) {
        val id = rowData["title_id"] as? Int
        if (id != null) {
            AppRouter.navigateTo(
                AsianMediaFormScreen.create(
                    entityId = id
                )
            )
        }
    }

    override fun buildMainActions(): List<ReportMainAction> = listOf(
        ReportMainAction(Tr.AsianMedia.Report.newTitle(), Icons.Default.Add) {
            AppRouter.navigateTo(
                AsianMediaFormScreen.create(
                )
            )
        },
        ReportMainAction(Tr.AsianMedia.Report.newKoreanWebNovel(), Icons.Default.Add) {
            val payload = mapOf(
                "publications" to listOf(
                    mapOf(
                        "publication_type" to PublicationType.WebNovel,
                        "status" to PublicationStatus.NotReading,
                        "track_progress" to false
                    )
                ), "language" to PublicationLanguage.Korean
            )
            AppRouter.navigateTo(
                AsianMediaFormScreen.create(null, payload)
            )
        }
    )

    override fun buildQuickSearch(searchQuery: String): QueryFragment {
        return "title_id IN (SELECT title_id FROM asian_media.title_variants WHERE title % @searchQuery)" withParam ("searchQuery" to searchQuery)
    }
}