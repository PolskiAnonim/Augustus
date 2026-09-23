package org.octavius.app.settings.domain

import org.octavius.app.localization.AppTr
import org.octavius.domain.EnumWithFormatter

/**
 * Supported application languages.
 */
enum class AppLanguage(val code: String) : EnumWithFormatter<AppLanguage> {
    PL("pl"),
    EN("en");

    override fun toDisplayString(): String = when (this) {
        PL -> AppTr.Settings.Language.pl()
        EN -> AppTr.Settings.Language.en()
    }

    companion object {
        fun fromCode(code: String): AppLanguage = entries.find { it.code == code } ?: EN
    }
}
