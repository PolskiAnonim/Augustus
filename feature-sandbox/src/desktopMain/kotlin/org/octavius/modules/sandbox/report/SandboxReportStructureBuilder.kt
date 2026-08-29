package org.octavius.modules.sandbox.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import io.github.octaviusframework.client.query.QueryFragment
import org.octavius.localization.Tr
import org.octavius.modules.sandbox.domain.SandboxPriority
import org.octavius.modules.sandbox.form.ui.SandboxFormScreen
import org.octavius.navigation.AppRouter
import org.octavius.report.ReportMainAction
import org.octavius.report.ReportRowAction
import org.octavius.report.column.ReportColumn
import org.octavius.report.column.type.*
import org.octavius.report.component.ReportStructureBuilder
import org.octavius.report.localization.ReportTr

class SandboxReportStructureBuilder : ReportStructureBuilder() {

    override fun getReportName(): String = "sandbox"

    override fun buildQuery(): QueryFragment {
        val query = """
            SELECT
                gs.id,
                'Element ' || gs.id AS name,
                CASE
                    WHEN gs.id % 4 = 0 THEN 'LOW'
                    WHEN gs.id % 4 = 1 THEN 'MEDIUM'
                    WHEN gs.id % 4 = 2 THEN 'HIGH'
                    ELSE 'CRITICAL'
                END AS priority,
                gs.id % 2 = 0 AS is_active,
                gs.id * 7 AS score,
                NOW() - (gs.id || ' days')::interval AS created_at
            FROM generate_series(1, 50) AS gs(id)
        """.trimIndent()
        return QueryFragment(query)
    }

    override fun buildColumns(): Map<String, ReportColumn> = mapOf(
        "name" to StringColumn(Tr.Sandbox.Report.name()),
        "priority" to EnumColumn(Tr.Sandbox.Report.priority(), enumClass = SandboxPriority::class),
        "is_active" to BooleanColumn(Tr.Sandbox.Report.active()),
        "score" to IntegerColumn(Tr.Sandbox.Report.score()),
        "created_at" to InstantColumn(Tr.Sandbox.Report.createdAt())
    )

    override fun buildDefaultRowAction(): ReportRowAction = ReportRowAction(ReportTr.Report.Actions.edit()) {
        AppRouter.navigateTo(SandboxFormScreen.create())
    }

    override fun buildMainActions(): List<ReportMainAction> = listOf(
        ReportMainAction(Tr.Sandbox.Report.newItem(), Icons.Default.Add) {
            AppRouter.navigateTo(SandboxFormScreen.create())
        }
    )
}
