package org.octavius.app.settings.form.theme

import org.octavius.app.settings.AppSettingsManager
import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.localization.Tr
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class ThemeSettingsFormScreen {
    companion object {
        fun create(settingsManager: AppSettingsManager): Screen {
            val title = Tr.Settings.Theme.title()
            val formHandler = FormHandler(
                formSchemaBuilder = ThemeSettingsSchemaBuilder(),
                formDataManager = ThemeSettingsDataManager(settingsManager)
            )
            return ComponentScreen(title) { FormView(formHandler) }
        }
    }
}
