package org.octavius.theme

import androidx.compose.ui.unit.dp

/**
 * System odstępów i wymiarów używanych w aplikacji.
 *
 * Zwykłe stałe, a nie CompositionLocal - odstępy nie zmieniają się w zależności
 * od motywu ani poddrzewa kompozycji, więc nie ma czego wstrzykiwać.
 */

/** Ogólna skala odstępów - do paddingów i przerw poza formularzami. */
object Spacing {
    val none = 0.dp
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 24.dp
    val huge = 32.dp
}

/**
 * Wyspecjalizowane odstępy i wymiary dla komponentów formularzy.
 *
 * Zawiera predefiniowane stałe dla:
 * - Padding pól formularza
 * - Wymiary nagłówków i sekcji
 * - Odstępy kontrolek (dropdown, boolean, repeatable)
 * - Wymiary kart i kontenerów
 */
object FormSpacing {
    val fieldPaddingHorizontal = 4.dp
    val fieldPaddingVertical = 8.dp
    val labelPaddingStart = 4.dp
    val labelPaddingBottom = 4.dp
    val errorPaddingStart = 24.dp
    val errorPaddingBottom = 8.dp
    val sectionPadding = 16.dp
    val sectionHeaderPaddingBottom = 12.dp
    val sectionContentSpacing = 8.dp
    val containerPaddingVertical = 8.dp
    val itemSpacing = 8.dp
    val controlSpacing = 12.dp
    val cardPadding = 16.dp
    val headerHeight = 48.dp
    val smallHeaderHeight = 40.dp
    val booleanControlPadding = 4.dp
    val booleanRowPaddingHorizontal = 12.dp
    val booleanRowPaddingVertical = 8.dp
    val dropdownPaddingHorizontal = 16.dp
    val dropdownPaddingVertical = 8.dp
    val repeatableRowPadding = 16.dp
    val repeatableHeaderPadding = 12.dp
}
