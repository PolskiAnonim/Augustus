package org.octavius.form.control.layout.section

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.octavius.form.component.FormState
import org.octavius.form.control.base.Control
import org.octavius.form.control.base.ControlContext
import org.octavius.theme.FormSpacing

@Composable
internal fun SectionContent(
    children: List<Pair<ControlContext, Control<*>>>,
    columns: Int,
    formState: FormState
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FormSpacing.sectionPadding)
        ) {
            if (columns > 1) {
                // Logika dla wielu kolumn
                RenderMultiColumnContent(children, columns, formState)
            } else {
                // Logika dla jednej kolumny
                RenderSingleColumnContent(children, formState)
            }
        }
    }
}

@Composable
private fun RenderMultiColumnContent(
    children: List<Pair<ControlContext, Control<*>>>,
    columns: Int,
    formState: FormState
) {
    // Dzielimy listę kontrolek na grupy dla każdej kolumny
    val childGroups = children.chunked(
        (children.size + columns - 1) / columns
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        childGroups.forEach { group ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = FormSpacing.fieldPaddingHorizontal)
            ) {
                group.forEach { (childContext, child) ->
                    RenderChild(childContext, child, formState)
                    Spacer(modifier = Modifier.height(FormSpacing.sectionContentSpacing))
                }
            }
        }
    }
}

@Composable
private fun RenderSingleColumnContent(
    children: List<Pair<ControlContext, Control<*>>>,
    formState: FormState,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        children.forEach { (childContext, child) ->
            RenderChild(childContext, child, formState)
            Spacer(modifier = Modifier.height(FormSpacing.sectionHeaderPaddingBottom))
        }
    }
}

// Wyodrębniamy powtarzającą się logikę renderowania pojedynczej kontrolki
@Composable
private fun RenderChild(
    childContext: ControlContext,
    child: Control<*>,
    formState: FormState
) {
    formState.getControlState(childContext.fullStatePath)?.let { state ->
        child.Render(childContext, state)
    }
}
