package org.octavius.app.settings.form.theme

import org.octavius.app.settings.AppSettingsManager
import org.octavius.app.settings.domain.AppThemeMode
import org.octavius.form.component.FormActionResult
import org.octavius.form.component.FormDataManager
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.getCurrentAs

class ThemeSettingsDataManager(
    private val settingsManager: AppSettingsManager
) : FormDataManager() {

    override fun initData(payload: Map<String, Any?>): Map<String, Any?> = mapOf(
        "theme" to settingsManager.currentSettings.theme
    )

    override fun definedFormActions(): Map<String, (FormResultData) -> FormActionResult> = mapOf(
        "save" to { data ->
            val selectedTheme = data.getCurrentAs<AppThemeMode>("theme")
            val newSettings = settingsManager.currentSettings.copy(
                theme = selectedTheme
            )
            settingsManager.updateSettings(newSettings)
            FormActionResult.Success
        }
    )
}
