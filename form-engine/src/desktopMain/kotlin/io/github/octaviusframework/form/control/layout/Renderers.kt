/**
 * Zestaw funkcji renderujących wspólne elementy kontrolek.
 * Zapewnia spójny wygląd etykiet, błędów i innych elementów UI.
 */
package io.github.octaviusframework.form.control.layout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.octavius.theme.FormSpacing



/**
 * Renderuje etykietę kontrolki z opcjonalną gwiazdką dla pól wymaganych.
 */
@Composable
private fun RenderLabel(label: String, isRequired: Boolean) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium
    )

    if (isRequired) {
        Text(
            text = "*",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Renderuje standardową etykietę kontrolki z paddingiem.
 * Używana przez większość kontrolek formularza.
 */
@Composable
internal fun RenderNormalLabel(label: String?, isRequired: Boolean) {
    if (label == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(
            start = FormSpacing.labelPaddingStart,
            bottom = FormSpacing.labelPaddingBottom
        )
    ) {
        RenderLabel(label, isRequired)
    }
}

/**
 * Renderuje etykietę dla kontrolki checkbox bez dodatkowego paddingu.
 * Checkboxy mają własne rozmieszczenie elementów.
 */
@Composable
internal fun RenderCheckboxLabel(label: String?, isRequired: Boolean) {
    if (label == null) return

    RenderLabel(label, isRequired)
}


/**
 * Renderuje pojedynczy błąd pola z ErrorManagera.
 *
 * @param error tekst błędu do wyświetlenia
 */
@Composable
internal fun RenderFieldError(error: String) {
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(
            start = FormSpacing.errorPaddingStart,
            bottom = FormSpacing.errorPaddingBottom
        )
    )
}