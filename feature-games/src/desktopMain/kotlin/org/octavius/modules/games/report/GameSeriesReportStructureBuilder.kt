package org.octavius.modules.games.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import io.github.octaviusframework.client.query.QueryFragment
import org.octavius.localization.Tr
import org.octavius.modules.games.form.game.ui.GameFormScreen
import org.octavius.modules.games.form.series.ui.GameSeriesFormScreen
import org.octavius.modules.games.report.ui.GameReportScreen
import org.octavius.navigation.AppRouter
import org.octavius.report.ReportMainAction
import org.octavius.report.ReportRowAction
import org.octavius.report.column.ReportColumn
import org.octavius.report.column.type.LongColumn
import org.octavius.report.column.type.StringColumn
import org.octavius.report.component.ReportStructureBuilder

class GameSeriesReportStructureBuilder : ReportStructureBuilder() {

    override fun getReportName(): String = "game_series"

    override fun buildQuery(): QueryFragment = QueryFragment(
        sql = """
        SELECT
            s.id,
            s.name,
            COALESCE(g.game_count, 0) as game_count
        FROM games.series s
            LEFT JOIN (
                SELECT
                    series,
                    COUNT(*) as game_count
                FROM games.games
                WHERE series IS NOT NULL
                GROUP BY series
            ) g ON s.id = g.series
        """.trimIndent()
    )

    override fun buildColumns(): Map<String, ReportColumn> = mapOf(
        "name" to StringColumn(
            header = Tr.Games.Series.name()
        ),
        "game_count" to LongColumn(
            header = Tr.Games.Series.gameCount()
        )
    )

    override fun buildDefaultRowAction(): ReportRowAction = ReportRowAction(
        label = Tr.Games.Form.editSeries(),
        icon = Icons.Default.Edit
    ) {
        val seriesId = rowData["id"] as? Int
        if (seriesId != null) {
            AppRouter.navigateTo(GameSeriesFormScreen.create(seriesId))
        }
    }

    override fun buildMainActions(): List<ReportMainAction> = listOf(
        ReportMainAction(
            label = Tr.Games.Form.newSeries(),
            icon = Icons.Default.Add
        ) {
            AppRouter.navigateTo(GameSeriesFormScreen.create())
        }
    )

    override fun buildRowActions(): List<ReportRowAction> = listOf(
        ReportRowAction(
            label = Tr.Games.Report.viewGames(),
            icon = Icons.AutoMirrored.Filled.List
        ) {
            val seriesId = rowData["id"] as? Int
            if (seriesId != null) {
                AppRouter.navigateTo(GameReportScreen.create(seriesId = seriesId))
            }
        },
        ReportRowAction(Tr.Games.Report.addGameInSeries(), Icons.Default.Add) {
            val seriesId = rowData["id"] as? Int
            if (seriesId != null) {
                val payload = mapOf("series" to seriesId)
                AppRouter.navigateTo(GameFormScreen.create(null, payload))
            }
        }
    )
}