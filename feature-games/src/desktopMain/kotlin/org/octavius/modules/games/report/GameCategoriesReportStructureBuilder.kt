package org.octavius.modules.games.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import io.github.octaviusframework.client.query.QueryFragment
import org.octavius.localization.Tr
import org.octavius.modules.games.form.category.ui.GameCategoryFormScreen
import org.octavius.modules.games.report.ui.GameReportScreen
import org.octavius.navigation.AppRouter
import org.octavius.report.ReportMainAction
import org.octavius.report.ReportRowAction
import org.octavius.report.column.ReportColumn
import org.octavius.report.column.type.LongColumn
import org.octavius.report.column.type.StringColumn
import org.octavius.report.component.ReportStructureBuilder

class GameCategoriesReportStructureBuilder : ReportStructureBuilder() {

    override fun getReportName(): String = "game_categories"

    override fun buildQuery(): QueryFragment = QueryFragment(
        sql = """
            SELECT
                c.id,
                c.name,
                COALESCE(g.game_count, 0) as game_count
            FROM games.categories c
            LEFT JOIN (
                SELECT
                    category_id,
                    COUNT(*) as game_count
                FROM games.categories_to_games
                GROUP BY category_id
            ) g ON c.id = g.category_id
        """.trimIndent()
    )

    override fun buildColumns(): Map<String, ReportColumn> = mapOf(
        "name" to StringColumn(
            header = Tr.Games.Categories.name()
        ),
        "game_count" to LongColumn(
            header = Tr.Games.Categories.gameCount()
        )
    )

    override fun buildRowActions(): List<ReportRowAction> = listOf(
        ReportRowAction(
            label = Tr.Games.Report.viewGames(),
            icon = Icons.AutoMirrored.Filled.List
        ) {
            val categoryId = rowData["id"] as? Int
            if (categoryId != null) {
                AppRouter.navigateTo(GameReportScreen.create(categoryId = categoryId))
            }
        }
    )

    override fun buildDefaultRowAction(): ReportRowAction = ReportRowAction(
        label = Tr.Games.Form.editCategory(),
        icon = Icons.Default.Edit
    ) {
        val categoryId = rowData["id"] as? Int
        if (categoryId != null) {
            AppRouter.navigateTo(GameCategoryFormScreen.create(categoryId))
        }
    }

    override fun buildMainActions(): List<ReportMainAction> = listOf(
        ReportMainAction(
            label = Tr.Games.Form.newCategory(),
            icon = Icons.Default.Add
        ) {
            AppRouter.navigateTo(GameCategoryFormScreen.create())
        }
    )
}