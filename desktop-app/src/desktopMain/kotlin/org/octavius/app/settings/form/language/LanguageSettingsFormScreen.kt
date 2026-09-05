package org.octavius.app.settings.form.language

import org.octavius.app.settings.AppSettingsManager
import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.localization.Tr
import org.octavius.navigation.AppRouter
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class LanguageSettingsFormScreen {
    companion object {
        fun create(settingsManager: AppSettingsManager): Screen {
            val title = Tr.Settings.Language.title()
            val formHandler = FormHandler(
                formSchemaBuilder = LanguageSettingsSchemaBuilder(),
                formDataManager = LanguageSettingsDataManager(settingsManager),
                onClose = { AppRouter.goBack() }
            )
            return ComponentScreen(title) { FormView(formHandler) }
        }
    }
}
