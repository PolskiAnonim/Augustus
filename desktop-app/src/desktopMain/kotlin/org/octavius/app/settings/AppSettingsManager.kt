package org.octavius.app.settings

import io.github.octaviusframework.i18n.core.OctaviusI18n
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.octavius.app.settings.domain.AppSettings
import org.octavius.localization.Tr
import java.io.File

/**
 * Manager for application settings stored in a JSON file.
 * Handles loading, saving, and applying settings like language.
 */
class AppSettingsManager {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val settingsFile: File by lazy {
        val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
        val folder = File(appData, "Augustus")
        if (!folder.exists()) folder.mkdirs()
        File(folder, "settings.json")
    }

    private val _settings = MutableStateFlow(loadSettings())

    /**
     * Observable settings. Collect this in composables that must react to a change
     * without an app restart (e.g. the light/dark theme mode).
     */
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    /**
     * Currently active settings.
     */
    val currentSettings: AppSettings get() = _settings.value

    /**
     * Updates settings, saves them to file, and applies changes (like language).
     */
    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        saveSettings(newSettings)
        applySettings()
    }

    /**
     * Applies current settings to the application state (e.g. updates Tr.currentLanguage).
     */
    fun applySettings() {
        OctaviusI18n.currentLanguage = currentSettings.language
    }

    private fun saveSettings(settings: AppSettings) {
        try {
            val jsonString = json.encodeToString(settings)
            settingsFile.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSettings(): AppSettings {
        return try {
            if (settingsFile.exists()) {
                val jsonString = settingsFile.readText()
                json.decodeFromString<AppSettings>(jsonString)
            } else {
                val default = AppSettings()
                saveSettings(default)
                default
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppSettings()
        }
    }
}
