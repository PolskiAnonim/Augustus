package org.octavius.form.control.type.datetime

import androidx.compose.runtime.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.octavius.error.rememberSupervisedCoroutineScope
import org.octavius.form.control.base.*
import org.octavius.form.control.validator.datetime.DateTimeValidator
import org.octavius.ui.datetime.DateTimePickerDialog
import org.octavius.ui.datetime.PickerTextField
import org.octavius.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class DateTimeControl<T : Any>(
    private val adapter: DateTimeAdapter<T>,
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: DateTimeValidation<T>? = null,
    actions: List<ControlAction<T>>? = null
) : Control<T>(label, required, dependencies, validationOptions, actions) {

    override val validator: ControlValidator<T> = DateTimeValidator(validationOptions, adapter)

    @Composable
    override fun Display(controlContext: ControlContext, controlState: ControlState<T>, isRequired: Boolean) {
        val scope = rememberSupervisedCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }

        PickerTextField(
            value = adapter.format(controlState.value.value),
            onClick = { showDialog = true },
            onClear = if (!isRequired) { {
                controlState.value.value = null
                executeActions(controlContext, null, scope)
            } } else null,
            isRequired = isRequired
        )

        if (showDialog) {
            DateTimePickerDialog(
                adapter = adapter,
                initialValue = controlState.value.value,
                onDismiss = { showDialog = false },
                onConfirm = { newValue ->
                    showDialog = false
                    controlState.value.value = newValue
                    executeActions(controlContext, newValue, scope)
                }
            )
        }
    }
}

// --- Funkcje fabryczne dla konkretnych typów daty/czasu ---

/**
 * Funkcja fabryczna tworząca kontrolkę do wyboru dat (LocalDate).
 */
@Suppress("FunctionName")
fun DateControl(
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: DateTimeValidation<LocalDate>? = null,
    actions: List<ControlAction<LocalDate>>? = null
) = DateTimeControl(
    adapter = DateAdapter,
    label = label,
    required = required,
    dependencies = dependencies,
    validationOptions = validationOptions,
    actions = actions
)

/**
 * Funkcja fabryczna tworząca kontrolkę do wyboru daty i czasu (LocalDateTime).
 */
@Suppress("FunctionName")
fun LocalDateTimeControl(
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: DateTimeValidation<LocalDateTime>? = null,
    actions: List<ControlAction<LocalDateTime>>? = null
) = DateTimeControl(
    adapter = LocalDateTimeAdapter,
    label = label,
    required = required,
    dependencies = dependencies,
    validationOptions = validationOptions,
    actions = actions
)

/**
 * Funkcja fabryczna tworząca kontrolkę do wyboru chwil czasowych (Instant).
 */
@OptIn(ExperimentalTime::class)
@Suppress("FunctionName")
fun InstantControl(
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: DateTimeValidation<Instant>? = null,
    actions: List<ControlAction<Instant>>? = null,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
) = DateTimeControl(
    adapter = InstantAdapter(timeZone),
    label = label,
    required = required,
    dependencies = dependencies,
    validationOptions = validationOptions,
    actions = actions
)

/**
 * Funkcja fabryczna tworząca kontrolkę do wyboru czasu (LocalTime).
 */
@Suppress("FunctionName")
fun LocalTimeControl(
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: DateTimeValidation<LocalTime>? = null,
    actions: List<ControlAction<LocalTime>>? = null
) = DateTimeControl(
    adapter = LocalTimeAdapter,
    label = label,
    required = required,
    dependencies = dependencies,
    validationOptions = validationOptions,
    actions = actions
)
