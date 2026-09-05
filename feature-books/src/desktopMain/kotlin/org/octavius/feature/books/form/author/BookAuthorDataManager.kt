package org.octavius.feature.books.form.author

import io.github.octaviusframework.client.DataResult
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.form.component.FormDataManager
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.getCurrent
import org.octavius.form.control.base.getInitial

class BookAuthorDataManager : FormDataManager() {

    private fun loadData(loadedId: Any?) = loadData(loadedId) {
        from("books.authors", "a")
        map("id")
        map("name")
        map("sort_name")
    }

    override fun initData(
        payload: Map<String, Any?>
    ): Map<String, Any?> {
        val loadedData = loadData(payload["id"])
        return loadedData + payload
    }

    override fun definedFormActions(): Map<String, (FormResultData) -> Boolean> {
        return mapOf(
            "save" to { formData -> processSave(formData) },
            "delete" to { formData -> processDelete(formData) }
        )
    }

    private fun processSave(formResultData: FormResultData): Boolean {
        val loadedId = formResultData.getInitial("id")
        val params = mapOf(
            "name" to formResultData.getCurrent("name"),
            "sort_name" to formResultData.getCurrent("sort_name")
        )

        val result = if (loadedId != null) {
            db.update("books.authors")
                .setValues(params)
                .where("id = @id")
                .asResult().update(params + ("id" to loadedId))
        } else {
            db.insertInto("books.authors")
                .values(params)
                .asResult().update(params)
        }

        return when (result) {
            is DataResult.Failure -> {
                GlobalDialogManager.show(ErrorDialogConfig(result.error))
                false
            }
            is DataResult.Success<*> -> true
        }
    }

    private fun processDelete(formResultData: FormResultData): Boolean {
        // Brak id oznacza, ze nie ma czego usuwac - nie jest to blad, wiec formularz
        // ma sie normalnie zamknac. Przycisk usuwania i tak jest ukryty gdy id == null.
        val loadedId = formResultData.getInitial("id") ?: return true

        val result = db.deleteFrom("books.authors")
            .where("id = @id")
            .asResult().update(mapOf("id" to loadedId))

        return when (result) {
            is DataResult.Failure -> {
                GlobalDialogManager.show(ErrorDialogConfig(result.error))
                false
            }
            is DataResult.Success<*> -> true
        }
    }
}