package org.octavius.app.settings.form.theme

import org.octavius.app.settings.AppSettingsManager
import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormScreen
import org.octavius.localization.Tr

class ThemeSettingsFormScreen {
    companion object {
        fun create(settingsManager: AppSettingsManager): FormScreen {
            val title = Tr.Settings.Theme.title()
            val formHandler = FormHandler(
                formSchemaBuilder = ThemeSettingsSchemaBuilder(),
                formDataManager = ThemeSettingsDataManager(settingsManager)
            )
            return FormScreen(title, formHandler)
        }
    }
}
