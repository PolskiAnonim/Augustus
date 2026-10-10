package org.octavius.form.control.type.repeatable

import org.octavius.form.component.FormState
import org.octavius.form.control.base.Control
import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.RepeatableValidation

class RepeatableRowManager(
    private val rowControls: Map<String, Control<*>>,
    private val formState: FormState,
    private val validationOptions: RepeatableValidation?
) {
    
    fun addRow(controlContext: ControlContext, controlState: ControlState<List<RepeatableRow>>) {
        val currentRows = controlState.value.value!!.toMutableList()
        val newIndex = currentRows.size
        val newRow = createRow(newIndex, controlContext, rowControls, formState)
        currentRows.add(newRow)
        controlState.value.value = currentRows
    }
    
    fun deleteRow(
        controlContext: ControlContext,
        controlState: ControlState<List<RepeatableRow>>,
        rowId: String
    ) {
        val minRows = validationOptions?.minItems ?: 0
        if (controlState.value.value!!.size <= minRows) {
            return
        }

        val currentRows = controlState.value.value!!.toMutableList()
        val index = currentRows.indexOfFirst { it.id == rowId }
        if (index < 0) return
        val rowToRemove = currentRows[index]
        
        // Sprawdź czy wiersz był w oryginalnych danych
        val wasOriginal = controlState.initValue.value!!.any { it.id == rowToRemove.id }
        
        // Stany wiersza z bazy zostają do końca życia formularza: jego wartości początkowe trafiają
        // do deletedRows przy każdym zbieraniu danych. Nowy wiersz nie ma czego zgłaszać.
        if (!wasOriginal) {
            formState.removeControlStatesWithPrefix("${controlContext.fullStatePath}[${rowToRemove.id}]")
        }
        
        // Usuń wiersz z listy
        currentRows.removeAt(index)
        
        // Zaktualizuj indeksy
        currentRows.forEachIndexed { newIndex, row ->
            currentRows[newIndex] = row.copy(index = newIndex)
        }
        
        controlState.value.value = currentRows
        return
    }
    
    fun canAddRow(controlState: ControlState<List<RepeatableRow>>): Boolean {
        val maxRows = validationOptions?.maxItems
        return maxRows == null || controlState.value.value!!.size < maxRows
    }
    
    fun canDeleteRow(controlState: ControlState<List<RepeatableRow>>): Boolean {
        val minRows = validationOptions?.minItems ?: 0
        return controlState.value.value!!.size > minRows
    }
}