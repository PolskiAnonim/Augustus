package org.octavius.modules.asian.form.ui

import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.localization.Tr
import org.octavius.modules.asian.form.AsianMediaFormDataManager
import org.octavius.modules.asian.form.AsianMediaFormSchemaBuilder
import org.octavius.modules.asian.form.AsianMediaValidator
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class AsianMediaFormScreen {
    companion object {
        fun create(
            entityId: Int? = null,
            payload: Map<String, Any?> = emptyMap()
        ): Screen {
            val title =
                if (entityId == null) Tr.AsianMedia.Form.newTitle() else Tr.AsianMedia.Form.editTitle()

            val formHandler = FormHandler(
                formSchemaBuilder = AsianMediaFormSchemaBuilder(),
                formDataManager = AsianMediaFormDataManager(),
                formValidator = AsianMediaValidator(),
                payload = payload + ("id" to entityId)
            )

            return ComponentScreen(title) { FormView(formHandler) }
        }
    }
}
