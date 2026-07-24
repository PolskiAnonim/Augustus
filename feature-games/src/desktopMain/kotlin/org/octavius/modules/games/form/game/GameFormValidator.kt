package org.octavius.modules.games.form.game

import io.github.octaviusframework.db.api.DataResult
import io.github.octaviusframework.db.api.builder.toField
import io.github.octaviusframework.db.api.join
import io.github.octaviusframework.db.api.withParam
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.component.FormValidator
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.getCurrentAs
import org.octavius.localization.Tr

class GameFormValidator : FormValidator() {

    override fun defineActionValidations(): Map<String, (formResultData: FormResultData) -> Boolean> {
        return mapOf(
            "save" to { formData -> checkTitleUniqueness(formData) }
        )
    }

    /**
     * Sprawdza, czy podany tytuł gry jest unikalny w bazie danych.
     * Jeśli edytujemy istniejącą grę, jej własny tytuł jest ignorowany w sprawdzaniu.
     *
     * @param formResultData Dane z formularza.
     * @return `true` jeśli tytuł jest unikalny, `false` jeśli tytuł już istnieje lub wystąpił błąd.
     */
    private fun checkTitleUniqueness(formResultData: FormResultData): Boolean {
        val title = formResultData.getCurrentAs<String>("name")
        val id = formResultData.getCurrentAs<Int?>("id")
        val whereClause = listOfNotNull(
            "name = @title" withParam ("title" to title),
            id?.let { "id != @id" withParam ("id" to id) }
        ).join(" AND ")

        val result = dataAccess.select("COUNT(*)").from("games").where(whereClause.sql).toField<Long>(whereClause.params)

        return when (result) {
            is DataResult.Success -> {
                if ((result.value) > 0) {
                    // Tytuł już istnieje. Ustawiamy błąd dla konkretnego pola 'name'.
                    errorManager.setFieldErrors("name", listOf(Tr.Games.Validation.nameExists()))
                    false // Walidacja się nie powiodła.
                } else {
                    true // Tytuł jest unikalny. Walidacja powiodła się.
                }
            }
            is DataResult.Failure -> {
                GlobalDialogManager.show(ErrorDialogConfig(result.error))
                false
            }
        }
    }
}
