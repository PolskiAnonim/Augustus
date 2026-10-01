package org.octavius.form.control.type.container

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.octavius.form.control.base.*
import org.octavius.form.control.layout.section.SectionContent
import org.octavius.form.control.layout.section.SectionHeader
import org.octavius.theme.FormSpacing

/**
 * Kontrolka do grupowania i organizacji innych kontrolek w sekcje.
 *
 * Renderuje grupę kontrolek w karcie z nagłówkiem. Obsługuje składanie/rozwijanie sekcji
 * i układanie kontrolek w kolumnach. Umożliwia logiczne organizowanie formularza w tematyczne sekcje.
 *
 * Sekcja to jeden poziom danych, jak wiersz repeatable: jej dzieci leżą pod `sekcja/nazwa`,
 * a wynik sekcji niesie mapę ich wyników. Widoczność dzieci zależy od widoczności sekcji.
 *
 * @param controls Kontrolki sekcji, w kolejności wyświetlania.
 */
class SectionControl(
    val controls: Map<String, Control<*>>,
    val collapsible: Boolean = true,
    val initiallyExpanded: Boolean = true,
    val columns: Int = 1,
    label: String,
    dependencies: Map<String, ControlDependency<*>>? = null
) : Control<Unit>(label, false, dependencies, hasStandardLayout = false) {

    override fun registerChildrenInGlobalMap(controlContext: ControlContext): Map<String, Control<*>> {
        val map = mutableMapOf<String, Control<*>>()
        childContexts(controlContext).forEach { (childControlContext, child) ->
            map[childControlContext.fullControlPath] = child
            map.putAll(child.registerChildrenInGlobalMap(childControlContext))
        }
        return map
    }

    override fun childContexts(controlContext: ControlContext): List<Pair<ControlContext, Control<*>>> {
        return controls.map { (childName, child) -> controlContext.forSectionChild(childName) to child }
    }

    // Wartość sekcji to mapa jej pól, którą FormState złożył ze ścieżek `sekcja/pole`.
    override fun setInitValue(controlContext: ControlContext, value: Any?): ControlState<Unit> {
        @Suppress("UNCHECKED_CAST")
        formState.initializeLevel(childContexts(controlContext), (value as Map<String, Any?>?).orEmpty())
        return ControlState()
    }

    // Sekcja nie ma własnej wartości - niesie wyniki dzieci, w obu polach, bo każde dziecko ma
    // i bieżącą, i początkową. Ukryta sekcja traci bieżącą mapę, ale jej dzieci i tak oddają `null`.
    override fun convertToResult(controlContext: ControlContext, state: ControlState<*>): ControlResultData {
        val children = formState.collectLevel(childContexts(controlContext))
        return ControlResultData(currentValue = children, initialValue = children)
    }

    @Composable
    override fun Display(controlContext: ControlContext, controlState: ControlState<Unit>, isRequired: Boolean) {
        val isExpanded = remember { mutableStateOf(initiallyExpanded) }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = FormSpacing.containerPaddingVertical),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    label = label,
                    collapsible = collapsible,
                    isExpanded = isExpanded.value,
                    onToggle = { isExpanded.value = !isExpanded.value }
                )

                AnimatedVisibility(visible = isExpanded.value) {
                    SectionContent(
                        children = childContexts(controlContext),
                        columns = columns,
                        formState = this@SectionControl.formState
                    )
                }
            }
        }
    }
}
