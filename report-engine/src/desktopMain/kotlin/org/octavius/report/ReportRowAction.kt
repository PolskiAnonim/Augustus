package org.octavius.report

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.CoroutineScope
import org.octavius.error.showUnhandledError
import org.octavius.report.component.ReportState

/**
 * Definiuje pojedynczą akcję, która może być wykonana na wierszu raportu.
 *
 * @param label Tekst, który pojawi się w menu.
 * @param icon Opcjonalna ikona dla pozycji w menu.
 * @param action Logika do wykonania, otrzymuje ReportActionContext.
 */
data class ReportRowAction(
    val label: String,
    val icon: ImageVector? = null,
    val action: ReportActionContext.() -> Unit
) {
    /**
     * Wykonuje akcję. Wyjątek z niej kończy się dialogiem błędu, a nie zamknięciem okna.
     */
    internal fun execute(context: ReportActionContext) {
        try {
            context.action()
        } catch (e: Exception) {
            showUnhandledError(e)
        }
    }
}

/**
 * Kontekst dostarczany do logiki ReportAction.
 *
 * @param rowData Mapa zawierająca dane dla wiersza, na którym wywołano akcję.
 * @param reportState Dostęp do ogólnego stanu raportu (np. do odświeżenia danych).
 * @param coroutineScope Scope do uruchamiania operacji asynchronicznych. Wyjątek z korutyny
 * odpalonej w nim kończy się dialogiem błędu (patrz `rememberSupervisedCoroutineScope`).
 */
data class ReportActionContext(
    val rowData: Map<String, Any?>,
    val reportState: ReportState,
    val onEvent: (ReportEvent) -> Unit,
    val coroutineScope: CoroutineScope
)