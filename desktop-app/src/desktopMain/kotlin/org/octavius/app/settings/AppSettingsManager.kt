package org.octavius.app.settings

import io.github.octaviusframework.i18n.core.OctaviusI18n
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
        val folder = File(appData, "Octavius")
        if (!folder.exists()) folder.mkdirs()
        File(folder, "settings.json")
    }

    private var _currentSettings: AppSettings = loadSettings()
    
    /**
     * Currently active settings.
     */
    val currentSettings: AppSettings get() = _currentSettings

    /**
     * Updates settings, saves them to file, and applies changes (like language).
     */
    fun updateSettings(newSettings: AppSettings) {
        _currentSettings = newSettings
        saveSettings(newSettings)
        applySettings()
    }

    /**
     * Applies current settings to the application state (e.g. updates Tr.currentLanguage).
     */
    fun applySettings() {
        OctaviusI18n.currentLanguage = _currentSettings.language
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
