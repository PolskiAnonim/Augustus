package org.octavius.app.settings.form.database

import org.octavius.app.settings.AppSettingsManager
import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.localization.Tr
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class DatabaseSettingsFormScreen {
    companion object {
        fun create(settingsManager: AppSettingsManager): Screen {
            val title = Tr.Settings.Database.title()
            val formHandler = FormHandler(
                formSchemaBuilder = DatabaseSettingsSchemaBuilder(),
                formDataManager = DatabaseSettingsDataManager(settingsManager)
            )
            return ComponentScreen(title) { FormView(formHandler) }
        }
    }
}
