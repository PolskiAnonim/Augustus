package org.octavius.modules.games.form.category

import io.github.octaviusframework.client.DataResult
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.component.FormDataManager
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.getCurrent
import org.octavius.form.control.base.getInitial

class GameCategoryDataManager : FormDataManager() {

    private fun loadData(loadedId: Any?) = loadData(loadedId) {
        from("games.categories", "ctg")
        map("id")
        map("name")
    }

    override fun initData(
        payload: Map<String, Any?>
    ): Map<String, Any?> {
        val loadedData = loadData(payload["id"])
        return loadedData + payload
    }



    override fun definedFormActions(): Map<String, (FormResultData) -> Boolean> {
        return mapOf(
            "save" to { formData -> processSave(formData) }
        )
    }

    private fun processSave(formResultData: FormResultData): Boolean {
        val loadedId = formResultData.getInitial("id")
        val result = if (loadedId != null) {
            db.update("games.categories").setValue("name").where("id = @id")
                .asResult().update(mapOf("name" to formResultData.getCurrent("name"), "id" to loadedId))
        } else {
            db.insertInto("games.categories").value("name")
                .asResult().update(mapOf("name" to formResultData.getCurrent("name")))
        }
        return when (result) {
            is DataResult.Failure -> {
                GlobalDialogManager.show(ErrorDialogConfig(result.error))
                false
            }

            is DataResult.Success<*> -> true
        }
    }
}