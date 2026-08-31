package org.octavius.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.octavius.app.settings.AppSettingsManager
import org.octavius.app.settings.domain.isDark
import org.octavius.navigation.Tab
import org.octavius.theme.AppTheme
import org.octavius.ui.screen.MainScreen

/**
 * Główny komponent aplikacji desktopowej.
 *
 * Odpowiada za inicjalizację motywu aplikacji i renderowanie głównego ekranu
 * z przekazanymi zakładkami.
 *
 * @param tabs Lista zakładek [Tab] dostępnych w aplikacji
 * @param settingsManager Źródło ustawień, z którego pochodzi wybrany tryb kolorystyczny
 */
@Composable
fun App(tabs: List<Tab>, settingsManager: AppSettingsManager) {
    AppThemeFromSettings(settingsManager) {
        MainScreen.Content(tabs)
    }
}

/**
 * Opakowuje treść w [AppTheme] z trybem jasny/ciemny wziętym z ustawień użytkownika.
 *
 * Każde okno desktopowe to osobny root kompozycji, więc każde musi zostać opakowane
 * osobno - inaczej dostanie domyślny schemat baseline Material 3.
 */
@Composable
internal fun AppThemeFromSettings(
    settingsManager: AppSettingsManager,
    content: @Composable () -> Unit,
) {
    val settings by settingsManager.settings.collectAsState()
    AppTheme(isDarkTheme = settings.theme.isDark(), content = content)
}
