package org.octavius.modules.games.form.series

import io.github.octaviusframework.client.DataResult
import io.github.octaviusframework.client.dbResult
import io.github.octaviusframework.client.transaction.TransactionPlan
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.component.FormActionResult
import org.octavius.form.component.FormDataManager
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.getCurrent
import org.octavius.form.control.base.getInitial

class GameSeriesFormDataManager : FormDataManager() {
    private fun loadData(loadedId: Any?) = loadData(loadedId) {
        from("games.series", "s")
        map("id")
        map("name")
    }

    override fun initData(
        payload: Map<String, Any?>
    ): Map<String, Any?> {
        val loadedData = loadData(payload["id"])
        return loadedData + payload
    }

    override fun definedFormActions(): Map<String, (FormResultData) -> FormActionResult> {
        return mapOf(
            "save" to { formData -> processSave(formData) },
            "cancel" to { _ -> FormActionResult.CloseScreen }
        )
    }

    private fun processSave(formResultData: FormResultData): FormActionResult {
        val loadedId = formResultData.getInitial("id")
        val plan = TransactionPlan()
        if (loadedId != null) {
            plan.add(
                db.update("games.series").setValues(listOf("name")).where("id = @id").asStep()
                    .update("name" to formResultData.getCurrent("name"), "id" to loadedId)
            )
        } else {
            plan.add(
                db.insertInto("games.series").values(listOf("name")).asStep().update("name" to formResultData.getCurrent("name"))
            )
        }
        return when (val result = dbResult { db.executeTransactionPlan(plan) }) {
            is DataResult.Failure -> {
                GlobalDialogManager.show(ErrorDialogConfig(result.error))
                FormActionResult.Failure
            }

            is DataResult.Success<*> -> FormActionResult.CloseScreen
        }
    }
}