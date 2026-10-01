package org.octavius.form.control.type.repeatable

import org.octavius.form.component.FormState
import org.octavius.form.control.base.Control
import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.FormResultData

/**
 * Reprezentuje jeden wiersz w kontrolce powtarzalnej.
 * Uproszczona wersja - stan jest teraz zarządzany globalnie.
 *
 * @param id unikalny identyfikator wiersza (generowany automatycznie)
 * @param index pozycja wiersza w liście (używana do budowania hierarchicznych nazw)
 */
data class RepeatableRow(
    val id: String = java.util.UUID.randomUUID().toString(),
    val index: Int = 0
)

/**
 * Reprezentuje wynik kontrolki powtarzalnej z podziałem na typy operacji.
 *
 * @param deletedRows wiersze do usunięcia z bazy danych
 * @param addedRows nowe wiersze do dodania
 * @param modifiedRows wiersze do zaktualizowania
 */
data class RepeatableResultValue(
    val deletedRows: List<FormResultData>,
    val addedRows: List<FormResultData>,
    val modifiedRows: List<FormResultData>,
    val allCurrentRows: List<FormResultData>
)

/**
 * Kontrolki wiersza, każda z kontekstem, w którym rodzicem jest kontrolka powtarzalna.
 * Wiersz to osobny poziom danych: stany jego kontrolek leżą pod `nazwa[rowId]/...`.
 */
internal fun rowContexts(
    parentContext: ControlContext,
    row: RepeatableRow,
    rowControls: Map<String, Control<*>>
): List<Pair<ControlContext, Control<*>>> {
    return rowControls.map { (controlName, control) -> parentContext.forRepeatableChild(controlName, row.id) to control }
}

/**
 * Tworzy nowy wiersz i dodaje stany jego kontrolek do globalnego FormState.
 *
 * @param index pozycja wiersza w liście
 * @param parentContext kontekst kontrolki powtarzalnej
 * @param rowControls mapa kontrolek które mają być w wierszu
 * @param formState globalny stan formularza
 * @param values wartości początkowe pól wiersza (klucz = nazwa kontrolki), dla nowego wiersza puste
 * @return nowy wiersz z ustawionym indeksem
 */
internal fun createRow(
    index: Int,
    parentContext: ControlContext, // Np. "publications" lub "projects[uuid].tasks"
    rowControls: Map<String, Control<*>>,
    formState: FormState,
    values: Map<String, Any?> = emptyMap()
): RepeatableRow {
    val row = RepeatableRow(index = index)
    formState.initializeLevel(rowContexts(parentContext, row, rowControls), values)
    return row
}

/**
 * Analizuje stan kontrolki powtarzalnej i klasyfikuje wiersze według typu operacji.
 * Używa globalnego stanu zamiast lokalnych stanów wierszy.
 *
 * @param controlState stan kontrolki powtarzalnej
 * @param controlContext kontekst kontrolki powtarzalnej (do budowania hierarchicznych nazw)
 * @param rowControls mapa kontrolek w wierszu
 * @param formState globalny stan formularza
 * @return Triple(nowe wiersze, usunięte wiersze, zmienione wiersze)
 */
internal fun getRowTypes(
    controlState: ControlState<List<RepeatableRow>>,
    controlContext: ControlContext,
    rowControls: Map<String, Control<*>>,
    formState: FormState
): Triple<List<RepeatableRow>, List<RepeatableRow>, List<RepeatableRow>> {
    val currentRowIds = controlState.value.value!!.map { it.id }.toSet()
    val initialRowIds = controlState.initValue.value!!.map { it.id }.toSet()

    // Nowe wiersze: te w current, których ID nie ma w initial
    val newRows = controlState.value.value!!.filter { it.id !in initialRowIds }

    // Usunięte wiersze: te w initial, których ID nie ma w current
    val deletedRows = controlState.initValue.value!!.filter { it.id !in currentRowIds }

    // Zmienione wiersze: te, które są w obu listach (wg ID) I mają przynajmniej jedno pole "dirty"
    val changedRows = controlState.value.value!!.filter { currentRow ->
        if (currentRow.id !in initialRowIds) return@filter false

        rowContexts(controlContext, currentRow, rowControls).any { (fieldContext, field) ->
            isChanged(fieldContext, field, formState)
        }
    }
    return Triple(newRows, deletedRows, changedRows)
}

// Kontrolka jest zmieniona, gdy zmieniła się jej wartość albo cokolwiek pod nią - pole sekcji
// w wierszu czy wiersz zagnieżdżonego repeatable.
private fun isChanged(controlContext: ControlContext, control: Control<*>, formState: FormState): Boolean {
    val state = formState.getControlState(controlContext.fullStatePath)!!
    return state.initValue.value != state.value.value ||
        control.childContexts(controlContext).any { (childContext, child) -> isChanged(childContext, child, formState) }
}