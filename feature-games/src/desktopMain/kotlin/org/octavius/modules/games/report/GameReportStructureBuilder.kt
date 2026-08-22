package org.octavius.modules.games.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import io.github.octaviusframework.db.api.QueryFragment
import org.octavius.domain.game.GameStatus
import org.octavius.localization.Tr
import org.octavius.modules.games.form.game.ui.GameFormScreen
import org.octavius.modules.games.form.series.ui.GameSeriesFormScreen
import org.octavius.navigation.AppRouter
import org.octavius.report.ReportMainAction
import org.octavius.report.ReportRowAction
import org.octavius.report.column.ReportColumn
import org.octavius.report.column.type.EnumColumn
import org.octavius.report.column.type.StringColumn
import org.octavius.report.column.type.asList
import org.octavius.report.component.ReportStructureBuilder
import io.github.octaviusframework.db.api.withParam
import org.octavius.report.localization.ReportTr

class GameReportStructureBuilder(
    private val categoryId: Int? = null,
    private val seriesId: Int? = null
) : ReportStructureBuilder() {

    override fun getReportName(): String = "games"

    override fun buildQuery(): QueryFragment {
        val query = dataAccess.select(
            "games.id",
            "games.name AS game_name",
            "series.name AS series_name",
            "games.status",
            """COALESCE(
                (SELECT array_agg(c.name ORDER BY c.name)
                 FROM games.categories_to_games ctg
                 JOIN games.categories c ON c.id = ctg.category_id
                 WHERE ctg.game_id = games.id),
                ARRAY[]::text[]
            ) AS categories"""
        ).from("games LEFT JOIN series ON series.id = games.series")

        return when {
            categoryId != null -> {
                query.where("EXISTS (SELECT 1 FROM games.categories_to_games ctg WHERE ctg.game_id = games.id AND ctg.category_id = @categoryId)")
                    .toSql() withParam ("categoryId" to categoryId)
            }

            seriesId != null -> {
                query.where("games.series = @seriesId").toSql() withParam ("seriesId" to seriesId)
            }

            else -> {
                QueryFragment(query.toSql())
            }
        }
    }

    override fun buildColumns(): Map<String, ReportColumn> = mapOf(
        "game_name" to StringColumn(Tr.Games.General.gameName()),
        "series_name" to StringColumn(Tr.Games.General.series()),
        "status" to EnumColumn(
            Tr.Games.General.status(), enumClass = GameStatus::class
        ),
        "categories" to StringColumn(Tr.Games.Form.category(2)).asList(),
    )

    override fun buildDefaultRowAction(): ReportRowAction = ReportRowAction(
        ReportTr.Report.Actions.edit()
    ) {
        val id = rowData["id"] as? Int
        if (id != null) {
            AppRouter.navigateTo(
                GameFormScreen.create(
                    entityId = id
                )
            )
        }
    }


    override fun buildMainActions(): List<ReportMainAction> = listOf(
        ReportMainAction(Tr.Games.Report.newGame(), Icons.Default.Add) {
            AppRouter.navigateTo(
                GameFormScreen.create()
            )
        },
        ReportMainAction(Tr.Games.Report.newSeries(), Icons.Default.Add) {
            AppRouter.navigateTo(
                GameSeriesFormScreen.create()
            )
        })
}