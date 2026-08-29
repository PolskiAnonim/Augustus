package org.octavius.feature.books.form.book

import io.github.octaviusframework.client.DataResult
import io.github.octaviusframework.client.query.join
import io.github.octaviusframework.client.query.withParam
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.component.FormValidator
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.getCurrentAs
import org.octavius.localization.Tr

class BookFormValidator : FormValidator() {

    override fun defineActionValidations(): Map<String, (FormResultData) -> Boolean> {
        return mapOf("save" to { formData -> validateTitleUniqueness(formData) })
    }

    private fun validateTitleUniqueness(formResultData: FormResultData): Boolean {
        val titlePl = formResultData.getCurrentAs<String>("titlePl")
        val id = formResultData.getCurrentAs<Int?>("id")

        val whereClause = listOfNotNull(
            "title_pl = @title_pl" withParam ("title_pl" to titlePl),
            id?.let { "id != @id" withParam ("id" to id) }
        ).join(" AND ")

        val result = db.select("COUNT(*)")
            .from("books.books")
            .where(whereClause.sql)
            .asResult().fetchField<Long>(whereClause.params)

        return when (result) {
            is DataResult.Success -> {
                if ((result.value) > 0) {
                    errorManager.setFieldErrors("titlePl", listOf(Tr.Books.Validation.titleExists()))
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
