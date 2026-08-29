package org.octavius.modules.games.form.series

import io.github.octaviusframework.client.DataResult
import io.github.octaviusframework.client.query.join
import io.github.octaviusframework.client.query.withParam
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.component.FormValidator
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.getCurrentAs
import org.octavius.localization.Tr

class GameSeriesFormValidator : FormValidator() {

    override fun defineActionValidations(): Map<String, (FormResultData) -> Boolean> {
        return mapOf("save" to { formData -> validateTitleAgainstDatabase(formData) })
    }

    /**
     * Sprawdza, czy podany tytuł serii jest unikalny w bazie danych.
     * Jeśli edytujemy istniejącą serię, jej własny tytuł jest ignorowany w sprawdzaniu.
     *
     * @param formResultData Dane z formularza.
     * @return `true` jeśli tytuł jest unikalny lub wystąpił błąd, `false` jeśli tytuł już istnieje.
     */
    private fun validateTitleAgainstDatabase(formResultData: FormResultData): Boolean {
        val title = formResultData.getCurrentAs<String>("name")
        val id = formResultData.getCurrentAs<Int?>("id")
        val whereClause = listOfNotNull(
            "name = @title" withParam ("title" to title),
            id?.let { "id != @id" withParam ("id" to id) }
        ).join(" AND ")

        val result = db.select("COUNT(*)").from("series").where(whereClause.sql).asResult().fetchField<Long>(whereClause.params)

        return when (result) {
            is DataResult.Success -> {
                if ((result.value) > 0) {
                    // Tytuł już istnieje. Ustawiamy błąd dla konkretnego pola 'name'.
                    errorManager.setFieldErrors("name", listOf(Tr.Games.Validation.duplicatedSeries()))
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