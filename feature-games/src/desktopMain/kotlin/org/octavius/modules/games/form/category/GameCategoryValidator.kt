package org.octavius.modules.games.form.category

import io.github.octaviusframework.client.DataResult
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.component.FormValidator
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.getCurrent
import org.octavius.form.control.base.getCurrentAs
import org.octavius.localization.Tr

class GameCategoryValidator: FormValidator() {

    override fun defineActionValidations(): Map<String, (FormResultData) -> Boolean> = mapOf(
        "save" to { result -> checkIfUnique(result) }
    )

    fun checkIfUnique(result: FormResultData): Boolean {
        val name = result.getCurrentAs<String>("name")
        val id = result.getCurrent("id")
        val params = mutableMapOf<String, Any?>("name" to name)
        val builder = db.select("COUNT(*)").from("games.categories")
        if (id != null) {
            builder.where("id != @id AND name = @name")
            params["id"] = id
        } else {
            builder.where("name = @name")
        }
        return when (val result = builder.asResult().fetchField<Long>(params)) {
            is DataResult.Success -> {
                if ((result.value) > 0) {
                    // Kategoria już istnieje. Ustawiamy błąd dla konkretnego pola 'name'.
                    errorManager.setFieldErrors("name", listOf(Tr.Games.Validation.nameExists()))
                    false // Walidacja się nie powiodła.
                } else {
                    true // Kategoria jest unikalna. Walidacja powiodła się.
                }
            }
            is DataResult.Failure -> {
                GlobalDialogManager.show(ErrorDialogConfig(result.error))
                false
            }
        }

    }

}