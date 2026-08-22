package org.octavius.modules.games.report

import io.github.octaviusframework.db.api.QueryFragment
import org.octavius.localization.Tr
import org.octavius.modules.games.form.game.ui.GameFormScreen
import org.octavius.navigation.AppRouter
import org.octavius.report.ReportRowAction
import org.octavius.report.column.ReportColumn
import org.octavius.report.column.type.BigDecimalColumn
import org.octavius.report.column.type.BooleanColumn
import org.octavius.report.column.type.IntegerColumn
import org.octavius.report.column.type.StringColumn
import org.octavius.report.component.ReportStructureBuilder
import org.octavius.report.localization.ReportTr

class GameRatingsReportStructureBuilder : ReportStructureBuilder() {

    override fun getReportName(): String = "games_details"

    override fun buildQuery(): QueryFragment = QueryFragment(
        sql = """
            SELECT
                g.id,
                g.name AS game_name,
                pt.play_time_hours,
                pt.completion_count,
                r.story_rating,
                r.gameplay_rating,
                r.atmosphere_rating,
                c.has_distinctive_character,
                c.has_distinctive_protagonist,
                c.has_distinctive_antagonist
            FROM games.games g
            LEFT JOIN games.play_time pt ON pt.game_id = g.id
            LEFT JOIN games.ratings r ON r.game_id = g.id
            LEFT JOIN games.characters c ON c.game_id = g.id
            WHERE g.status IN ('PLAYING', 'PLAYED', 'WITHOUT_THE_END')
        """.trimIndent()
    )

    override fun buildColumns(): Map<String, ReportColumn> = mapOf(
        "game_name" to StringColumn(
            header = Tr.Games.General.gameName()
        ),
        "play_time_hours" to BigDecimalColumn(
            header = Tr.Games.Details.playTimeHours()
        ),
        "completion_count" to IntegerColumn(
            header = Tr.Games.Details.completionCount()
        ),
        "story_rating" to IntegerColumn(
            header = Tr.Games.Details.storyRating()
        ),
        "gameplay_rating" to IntegerColumn(
            header = Tr.Games.Details.gameplayRating()
        ),
        "atmosphere_rating" to IntegerColumn(
            header = Tr.Games.Details.atmosphereRating()
        ),
        "has_distinctive_character" to BooleanColumn(
            header = Tr.Games.Details.distinctiveCharacter()
        ),
        "has_distinctive_protagonist" to BooleanColumn(
            header = Tr.Games.Details.distinctiveProtagonist()
        ),
        "has_distinctive_antagonist" to BooleanColumn(
            header = Tr.Games.Details.distinctiveAntagonist()
        )
    )

    override fun buildDefaultRowAction(): ReportRowAction =
        ReportRowAction(ReportTr.Report.Actions.edit()) {
            val id = rowData["id"] as? Int
            if (id != null) {
                AppRouter.navigateTo(GameFormScreen.create(entityId = id))
            }
        }
}
