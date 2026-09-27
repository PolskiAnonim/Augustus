package org.octavius.form.control.type.repeatable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import org.octavius.form.component.ErrorManager
import org.octavius.form.component.FormActionTrigger
import org.octavius.form.component.FormSchema
import org.octavius.form.component.FormState
import org.octavius.form.control.base.*
import org.octavius.form.control.layout.repeatable.RepeatableHeader
import org.octavius.form.control.layout.repeatable.RepeatableRowCard
import org.octavius.form.control.layout.repeatable.RepeatableRowContent
import org.octavius.form.control.validator.repeatable.RepeatableValidator
import org.octavius.theme.FormSpacing

/**
 * Kontrolka do tworzenia dynamicznych list kontrolek (wierszy).
 *
 * Umożliwia użytkownikowi dodawanie i usuwanie wierszy, gdzie każdy wiersz
 * zawiera zestaw kontrolek. Obsługuje walidację unikalności pól, ograniczenia
 * minimalnej i maksymalnej liczby wierszy oraz składanie/rozwijanie wierszy.
 * Każdy wiersz może być niezależnie edytowany i usuwany.
 */
class RepeatableControl(
    val rowControls: Map<String, Control<*>>,
    val rowOrder: List<String>,
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: RepeatableValidation? = null
) : Control<List<RepeatableRow>>(
    label,
    required,
    dependencies,
    hasStandardLayout = false,
    validationOptions = validationOptions
) {
    private lateinit var rowManager: RepeatableRowManager

    override fun initializeControlLifecycle(
        formState: FormState,
        formSchema: FormSchema,
        errorManager: ErrorManager,
        formActionTrigger: FormActionTrigger
    ) {
        super.initializeControlLifecycle(formState, formSchema, errorManager, formActionTrigger)
        rowControls.values.forEach { childControl ->
            childControl.hierarchyRole = ControlHierarchyRole.AGGREGATED_CHILD
        }
        this.rowManager = RepeatableRowManager(rowControls, formState, validationOptions as RepeatableValidation?)
    }

    override fun registerChildrenInGlobalMap(controlContext: ControlContext): Map<String, Control<*>> {
        val map = mutableMapOf<String, Control<*>>()
        rowControls.forEach { (key, value) ->
            // rowId nie jest używane w tym przypadku
            val childControlContext = controlContext.forRepeatableChild(key, "")
            map[childControlContext.fullControlPath] = value
            map.putAll(value.registerChildrenInGlobalMap(childControlContext))
        }
        return map
    }


    override val validator: ControlValidator<List<RepeatableRow>> = RepeatableValidator(
        validationOptions
    )

    override fun copyInitToValue(value: List<RepeatableRow>): List<RepeatableRow> {
        // Dla globalnego stanu nie trzeba kopiować stanów - są już w FormState
        return value.map { RepeatableRow(id = it.id, index = it.index) }
    }

    override fun setInitValue(controlContext: ControlContext, value: Any?): ControlState<List<RepeatableRow>> {

        @Suppress("UNCHECKED_CAST")
        val initialDataRows = value as? List<Map<String, Any?>> ?: emptyList()

        // 1. Utwórz wiersze i stany dla danych początkowych
        val initialModelRows = initialDataRows.mapIndexed { index, dataRow ->
            val row = RepeatableRow(index = index)

            // Dla każdego pola w danych, stwórz stan kontrolki-dziecka
            rowControls.forEach { (fieldName, control) ->
                val fieldValue = dataRow[fieldName]
                val childContext = controlContext.forRepeatableChild(fieldName, row.id)

                // Rekurencyjne wywołanie! Tworzymy stan dla dziecka.
                val childState = control.setInitValue(childContext, fieldValue)
                formState.setControlState(childContext.fullStatePath, childState)
            }
            row
        }

        // 2. Dodaj puste wiersze, jeśli wymagane przez minItems
        val additionalModelRows = mutableListOf<RepeatableRow>()
        val minRows = (validationOptions as? RepeatableValidation)?.minItems ?: 0
        while (initialModelRows.size + additionalModelRows.size < minRows) {
            val index = initialModelRows.size + additionalModelRows.size

            // Używamy nowej funkcji pomocniczej
            val newRow = createRow(index, controlContext, rowControls, formState)
            additionalModelRows.add(newRow)
        }

        // 3. Zwróć stan dla samego RepeatableControl
        val allInitialRows = initialModelRows + additionalModelRows
        return ControlState(
            initValue = mutableStateOf(initialModelRows),
            value = mutableStateOf(copyInitToValue(allInitialRows)),
        )
    }

    @Composable
    override fun Display(controlContext: ControlContext, controlState: ControlState<List<RepeatableRow>>, isRequired: Boolean) {
        Column(modifier = Modifier.fillMaxWidth()) {
            RepeatableHeader(
                label = label,
                onAddClick = {
                    rowManager.addRow(controlContext, controlState)
                },
                canAdd = rowManager.canAddRow(controlState)
            )

            DisplayFieldErrors(controlContext)

            Spacer(modifier = Modifier.height(FormSpacing.itemSpacing))

            controlState.value.value?.forEachIndexed { index, row ->
                RepeatableRowCard(
                    index = index,
                    canDelete = rowManager.canDeleteRow(controlState),
                    onDelete = {
                        rowManager.deleteRow(controlContext, controlState, index)
                    },
                    content = {
                        RepeatableRowContent(
                            row = row,
                            controlContext = controlContext,
                            rowOrder = rowOrder,
                            rowControls = rowControls,
                            formState = formState
                        )
                    }
                )

                Spacer(modifier = Modifier.height(FormSpacing.itemSpacing))
            }
        }
    }

    override fun convertToResult(
        controlContext: ControlContext,
        state: ControlState<*>, // Ten state to ControlState<List<RepeatableRow>> dla RepeatableControl
    ): ControlResultData {
        @Suppress("UNCHECKED_CAST")
        val controlState = state as ControlState<List<RepeatableRow>>
        val states = formState.getAllStates()

        val (newRows, deletedRows, changedRows) = getRowTypes(
            controlState,
            controlContext,
            rowControls,
            states
        )

        fun rowValues(row: RepeatableRow): FormResultData =
            rowControls.mapValues { (fieldName, control) ->
                val hierarchicalContext = controlContext.forRepeatableChild(fieldName, row.id)
                control.getResult(hierarchicalContext, states.getValue(hierarchicalContext.fullStatePath))
            }

        // Zbieranie danych niczego nie zmienia w stanie. Stany usuniętych wierszy z bazy muszą
        // przetrwać, bo ich wartości początkowe idą do deletedRows przy KAŻDYM zbieraniu, a to
        // powtarza się, gdy zapis nie przejdzie i użytkownik kliknie jeszcze raz.
        return ControlResultData(
            currentValue = RepeatableResultValue(
                deletedRows = deletedRows.map(::rowValues),
                addedRows = newRows.map(::rowValues),
                modifiedRows = changedRows.map(::rowValues),
                allCurrentRows = controlState.value.value!!.map(::rowValues)
            ),
            initialValue = null  // Oryginalne wartości są już zawarte w currentValue dla poszczególnych kontrolek
        )
    }
}