package org.octavius.form.control.validator.datetime

import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.ControlValidator
import org.octavius.form.control.base.DateTimeValidation
import org.octavius.form.localization.FormTr
import org.octavius.util.DateTimeAdapter

class DateTimeValidator<T: Any>(
    private val validationOptions: DateTimeValidation<T>? = null,
    private val adapter: DateTimeAdapter<T>
) : ControlValidator<T>() {

    override fun validateSpecific(controlContext: ControlContext, state: ControlState<*>) {
        @Suppress("UNCHECKED_CAST")
        val value = state.value.value as? T ?: return
        val errors = mutableListOf<String>()

        validationOptions?.let { options ->
            options.min?.let { min ->
                // Używamy standardowego porównania, zakładając, że typy T są Comparable
                @Suppress("UNCHECKED_CAST")
                if ((value as Comparable<T>) < min) {
                    errors.add(FormTr.Validation.minValue(adapter.format(min)))
                }
            }
            options.max?.let { max ->
                @Suppress("UNCHECKED_CAST")
                if ((value as Comparable<T>) > max) {
                    errors.add(FormTr.Validation.maxValue(adapter.format(max)))
                }
            }
        }
        errorManager.setFieldErrors(controlContext.fullStatePath, errors)
    }
}