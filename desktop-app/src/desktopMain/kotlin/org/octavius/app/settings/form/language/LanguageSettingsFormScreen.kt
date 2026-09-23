package org.octavius.app.settings.form.language

import org.octavius.app.localization.AppTr
import org.octavius.app.settings.AppSettingsManager
import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class LanguageSettingsFormScreen {
    companion object {
        fun create(settingsManager: AppSettingsManager): Screen {
            val title = AppTr.Settings.Language.title()
            val formHandler = FormHandler(
                formSchemaBuilder = LanguageSettingsSchemaBuilder(),
                formDataManager = LanguageSettingsDataManager(settingsManager)
            )
            return ComponentScreen(title) { FormView(formHandler) }
        }
    }
}
