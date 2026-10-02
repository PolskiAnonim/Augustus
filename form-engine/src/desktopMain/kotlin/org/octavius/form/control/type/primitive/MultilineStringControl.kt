package org.octavius.form.control.type.primitive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.octavius.error.rememberSupervisedCoroutineScope
import org.octavius.form.control.base.*
import org.octavius.form.control.validator.primitive.StringValidator
import org.octavius.theme.FormSpacing
import java.awt.Cursor

/**
 * Kontrolka do wprowadzania długiego tekstu (wieloliniowego).
 * 
 * Zawiera wbudowany uchwyt (prawy dolny róg) pozwalający na zmianę
 * wysokości pola przez użytkownika (podobnie jak textarea w HTML).
 */
class MultilineStringControl(
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: StringValidation? = null,
    actions: List<ControlAction<String>>? = null,
    private val minHeightDp: Int = 120
) : Control<String>(
    label,
    required,
    dependencies,
    validationOptions = validationOptions,
    actions = actions
) {
    override val validator: ControlValidator<String> = StringValidator(validationOptions)

    @Composable
    override fun Display(controlContext: ControlContext, controlState: ControlState<String>, isRequired: Boolean) {
        val scope = rememberSupervisedCoroutineScope()
        var height by remember { mutableStateOf(minHeightDp.dp) }
        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = FormSpacing.fieldPaddingVertical,
                    horizontal = FormSpacing.fieldPaddingHorizontal
                )
        ) {
            OutlinedTextField(
                value = controlState.value.value.orEmpty(),
                onValueChange = {
                    controlState.value.value = it
                    executeActions(controlContext, it, scope)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                singleLine = false
            )

            // Uchwyt do zmiany rozmiaru (resize handle)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // Niewielkie przesunięcie, aby nie nakładać się bezpośrednio na obramowanie
                    .padding(bottom = 6.dp, end = 6.dp)
                    .size(16.dp)
                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR)))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            with(density) {
                                val newHeight = height + dragAmount.y.toDp()
                                height = maxOf(minHeightDp.dp, newHeight)
                            }
                        }
                    }
            ) {
                val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Rysujemy paski uchwytu w prawym dolnym rogu
                    drawLine(color, Offset(w, h * 0.4f), Offset(w * 0.4f, h), strokeWidth = 2f)
                    drawLine(color, Offset(w, h * 0.7f), Offset(w * 0.7f, h), strokeWidth = 2f)
                    drawLine(color, Offset(w, h * 1.0f), Offset(w * 1.0f, h), strokeWidth = 2f)
                }
            }
        }
    }
}
