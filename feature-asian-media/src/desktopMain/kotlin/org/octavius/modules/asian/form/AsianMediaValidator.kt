package org.octavius.modules.asian.form

import io.github.octaviusframework.client.DataResult
import io.github.octaviusframework.client.query.join
import io.github.octaviusframework.client.query.withParam
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.component.FormValidator
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.getCurrentAs
import org.octavius.localization.Tr

class AsianMediaValidator: FormValidator() {
    override fun validateBusinessRules(formResultData: FormResultData): Boolean {
        return validateTitleDuplication(formResultData)
    }

    private fun validateTitleDuplication(formResultData: FormResultData): Boolean {
        val titles = formResultData.getCurrentAs<List<String>>("titles")
        val hasDuplicates = titles.size != titles.toSet().size

        if (hasDuplicates) {
            errorManager.addFieldError("titles", Tr.AsianMedia.Form.duplicateTitles())
        }

        return !hasDuplicates
    }

    fun validateTitlesAgainstDatabase(formResultData: FormResultData): Boolean {
        val titles = formResultData.getCurrentAs<List<String>>("titles")
        val id = formResultData.getCurrentAs<Int?>("id")

        if (titles.isEmpty()) return true

        val whereClause = listOfNotNull(
            "title = ANY(@titles)" withParam ("titles" to titles),
            id?.let { "id != @id" withParam ("id" to id) }
        ).join(separator = " AND ")

        val result = db.select("COUNT(*)").fromSubquery("SELECT id, UNNEST(titles) AS title FROM titles")
            .where(whereClause.sql).asResult().fetchField<Long>(whereClause.params)


        return when (result) {
            is DataResult.Failure -> {
                GlobalDialogManager.show(ErrorDialogConfig(result.error))
                false
            }
            is DataResult.Success -> {
                if (result.value > 0L) {
                    errorManager.addGlobalError(Tr.AsianMedia.Form.titlesAlreadyExist())
                    false
                } else {
                    true
                }
            }
        }
    }

    override fun defineActionValidations(): Map<String, (FormResultData) -> Boolean> {
        return mapOf(
            "save" to { formData -> validateTitlesAgainstDatabase(formData) }
        )
    }
}