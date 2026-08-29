package org.octavius.feature.books.form.author

import io.github.octaviusframework.client.DataResult
import io.github.octaviusframework.client.query.join
import io.github.octaviusframework.client.query.withParam
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.component.FormValidator
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.getCurrentAs
import org.octavius.localization.Tr

class BookAuthorValidator : FormValidator() {

    override fun defineActionValidations(): Map<String, (FormResultData) -> Boolean> {
        return mapOf("save" to { formData -> validateNameUniqueness(formData) })
    }

    private fun validateNameUniqueness(formResultData: FormResultData): Boolean {
        val name = formResultData.getCurrentAs<String>("name")
        val id = formResultData.getCurrentAs<Int?>("id")

        val whereClause = listOfNotNull(
            "name = @name" withParam ("name" to name),
            id?.let { "id != @id" withParam ("id" to id) }
        ).join(" AND ")

        val result = db.select("COUNT(*)")
            .from("books.authors")
            .where(whereClause.sql)
            .asResult().fetchField<Long>(whereClause.params)

        return when (result) {
            is DataResult.Success -> {
                if (result.value > 0) {
                    errorManager.setFieldErrors("name", listOf(Tr.Books.Authors.Validation.nameExists()))
                    false
                } else {
                    true
                }
            }

            is DataResult.Failure -> {
                GlobalDialogManager.show(ErrorDialogConfig(result.error))
                false
            }
        }
    }
}