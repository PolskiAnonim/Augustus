package org.octavius.app.settings.domain

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlinx.serialization.Serializable
import org.octavius.domain.EnumWithFormatter
import org.octavius.localization.Tr

/**
 * Tryb kolorystyczny interfejsu wybrany przez użytkownika.
 */
@Serializable
enum class AppThemeMode : EnumWithFormatter<AppThemeMode> {
    /** Podąża za ustawieniem systemu operacyjnego. */
    SYSTEM,
    LIGHT,
    DARK;

    override fun toDisplayString(): String = when (this) {
        SYSTEM -> Tr.Settings.Theme.system()
        LIGHT -> Tr.Settings.Theme.light()
        DARK -> Tr.Settings.Theme.dark()
    }
}

/**
 * Rozstrzyga tryb na konkretną wartość dla [org.octavius.theme.AppTheme].
 *
 * Dla [AppThemeMode.SYSTEM] odczytuje ustawienie systemowe, dzięki czemu zmiana
 * motywu w systemie jest podchwytywana bez restartu aplikacji.
 */
@Composable
@ReadOnlyComposable
fun AppThemeMode.isDark(): Boolean = when (this) {
    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
}
