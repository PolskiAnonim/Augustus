package org.octavius.modules.sandbox.form.ui

import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.localization.Tr
import org.octavius.modules.sandbox.form.SandboxFormDataManager
import org.octavius.modules.sandbox.form.SandboxFormSchemaBuilder
import org.octavius.navigation.AppRouter
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class SandboxFormScreen {
    companion object {
        fun create(): Screen {
            val title = Tr.Sandbox.Form.newItem()

            val formHandler = FormHandler(
                formSchemaBuilder = SandboxFormSchemaBuilder(),
                formDataManager = SandboxFormDataManager(),
                onClose = { AppRouter.goBack() }
            )

            return ComponentScreen(title) { FormView(formHandler) }
        }
    }
}
