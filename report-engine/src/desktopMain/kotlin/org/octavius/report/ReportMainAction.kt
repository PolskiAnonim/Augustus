package org.octavius.report

import androidx.compose.ui.graphics.vector.ImageVector
import org.octavius.error.showUnhandledError

/**
 * Definiuje pojedynczą akcję, która może być wykonana z głównego menu "Dodaj" na ekranie raportu.
 *
 * @param label Tekst, który pojawi się w menu.
 * @param icon Opcjonalna ikona dla pozycji w menu.
 * @param action Prosta logika do wykonania po kliknięciu.
 */
data class ReportMainAction(
    val label: String,
    val icon: ImageVector? = null,
    val action: () -> Unit
) {
    /**
     * Wykonuje akcję. Wyjątek z niej kończy się dialogiem błędu, a nie zamknięciem okna.
     */
    internal fun execute() {
        try {
            action()
        } catch (e: Exception) {
            showUnhandledError(e)
        }
    }
}
