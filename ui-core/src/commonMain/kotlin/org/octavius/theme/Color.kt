package org.octavius.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta kolorów wygenerowana w Material Theme Builderze.
 *
 * Plik jest artefaktem generowanym - przy zmianie palety podmieniamy go w całości
 * eksportem z MTB (zostawiając tylko warianty standardowe, bez medium/high contrast).
 * Schematy [lightScheme] i [darkScheme] składane są z tych kolorów w [AppTheme].
 *
 * Kolory są `internal` celowo - reszta aplikacji ma sięgać po `MaterialTheme.colorScheme`,
 * nigdy po surowe stałe. Po wklejeniu nowego eksportu z MTB wystarczy dopisać `internal`.
 */

// --- Jasny motyw ---
internal val primaryLight = Color(0xFF834C74)
internal val onPrimaryLight = Color(0xFFFFFFFF)
internal val primaryContainerLight = Color(0xFFFFD7EF)
internal val onPrimaryContainerLight = Color(0xFF69355B)
internal val secondaryLight = Color(0xFF8F4A50)
internal val onSecondaryLight = Color(0xFFFFFFFF)
internal val secondaryContainerLight = Color(0xFFFFDADB)
internal val onSecondaryContainerLight = Color(0xFF723339)
internal val tertiaryLight = Color(0xFF8C4E28)
internal val onTertiaryLight = Color(0xFFFFFFFF)
internal val tertiaryContainerLight = Color(0xFFFFDBC9)
internal val onTertiaryContainerLight = Color(0xFF6F3813)
internal val errorLight = Color(0xFF904A43)
internal val onErrorLight = Color(0xFFFFFFFF)
internal val errorContainerLight = Color(0xFFFFDAD6)
internal val onErrorContainerLight = Color(0xFF73332D)
internal val backgroundLight = Color(0xFFFFF8F9)
internal val onBackgroundLight = Color(0xFF201A1E)
internal val surfaceLight = Color(0xFFFFF8F9)
internal val onSurfaceLight = Color(0xFF201A1E)
internal val surfaceVariantLight = Color(0xFFEFDEE6)
internal val onSurfaceVariantLight = Color(0xFF4F444A)
internal val outlineLight = Color(0xFF81737A)
internal val outlineVariantLight = Color(0xFFD2C2CA)
internal val scrimLight = Color(0xFF000000)
internal val inverseSurfaceLight = Color(0xFF362E32)
internal val inverseOnSurfaceLight = Color(0xFFFBEDF3)
internal val inversePrimaryLight = Color(0xFFF6B2E0)
internal val surfaceDimLight = Color(0xFFE4D7DC)
internal val surfaceBrightLight = Color(0xFFFFF8F9)
internal val surfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val surfaceContainerLowLight = Color(0xFFFEF0F6)
internal val surfaceContainerLight = Color(0xFFF8EAF0)
internal val surfaceContainerHighLight = Color(0xFFF2E5EA)
internal val surfaceContainerHighestLight = Color(0xFFEDDFE4)

// --- Ciemny motyw ---
internal val primaryDark = Color(0xFFF6B2E0)
internal val onPrimaryDark = Color(0xFF4F1E43)
internal val primaryContainerDark = Color(0xFF69355B)
internal val onPrimaryContainerDark = Color(0xFFFFD7EF)
internal val secondaryDark = Color(0xFFFFB2B7)
internal val onSecondaryDark = Color(0xFF561D24)
internal val secondaryContainerDark = Color(0xFF723339)
internal val onSecondaryContainerDark = Color(0xFFFFDADB)
internal val tertiaryDark = Color(0xFFFFB68E)
internal val onTertiaryDark = Color(0xFF532200)
internal val tertiaryContainerDark = Color(0xFF6F3813)
internal val onTertiaryContainerDark = Color(0xFFFFDBC9)
internal val errorDark = Color(0xFFFFB4AB)
internal val onErrorDark = Color(0xFF561E19)
internal val errorContainerDark = Color(0xFF73332D)
internal val onErrorContainerDark = Color(0xFFFFDAD6)
internal val backgroundDark = Color(0xFF181215)
internal val onBackgroundDark = Color(0xFFECDFE5)
internal val surfaceDark = Color(0xFF181215)
internal val onSurfaceDark = Color(0xFFEDDFE4)
internal val surfaceVariantDark = Color(0xFF4F444A)
internal val onSurfaceVariantDark = Color(0xFFD2C2CA)
internal val outlineDark = Color(0xFF9B8D94)
internal val outlineVariantDark = Color(0xFF4F444A)
internal val scrimDark = Color(0xFF000000)
internal val inverseSurfaceDark = Color(0xFFEDDFE4)
internal val inverseOnSurfaceDark = Color(0xFF362E32)
internal val inversePrimaryDark = Color(0xFF834C74)
internal val surfaceDimDark = Color(0xFF181215)
internal val surfaceBrightDark = Color(0xFF3F373B)
internal val surfaceContainerLowestDark = Color(0xFF120C10)
internal val surfaceContainerLowDark = Color(0xFF201A1E)
internal val surfaceContainerDark = Color(0xFF251E22)
internal val surfaceContainerHighDark = Color(0xFF2F282C)
internal val surfaceContainerHighestDark = Color(0xFF3A3337)
