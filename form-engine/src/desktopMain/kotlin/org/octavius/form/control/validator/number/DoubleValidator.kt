package org.octavius.form.control.validator.number

import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.ControlValidator
import org.octavius.form.control.base.DoubleValidation
import org.octavius.form.localization.FormTr
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

/**
 * Walidator dla kontrolek liczb zmiennoprzecinkowych z obsługą opcji walidacji.
 */
class DoubleValidator(
    private val validationOptions: DoubleValidation? = null
) : ControlValidator<Double>() {

    override fun validateSpecific(controlContext: ControlContext, state: ControlState<*>) {
        val value = state.value.value as? Double ?: return
        val errors = mutableListOf<String>()

        validationOptions?.let { options ->
            // Sprawdź wartość minimalną
            options.min?.let { min ->
                if (value < min) {
                    errors.add(FormTr.Validation.minValue(min))
                }
            }

            // Sprawdź wartość maksymalną
            options.max?.let { max ->
                if (value > max) {
                    errors.add(FormTr.Validation.maxValue(max))
                }
            }

            // Sprawdź miejsca dziesiętne
            options.decimalPlaces?.let { decimalPlaces ->
                val multiplier = 10.0.pow(decimalPlaces)
                val rounded = round(value * multiplier) / multiplier
                if (value != rounded) {
                    errors.add(FormTr.Validation.maxDecimalPlaces(decimalPlaces))
                }
            }

            // Sprawdź krok
            options.step?.let { step ->
                val remainder = value % step
                if (remainder != 0.0 && (remainder - step).let { abs(it) } > 1e-10) {
                    errors.add(FormTr.Validation.multipleOf(step))
                }
            }
        }

        errorManager.setFieldErrors(controlContext.fullStatePath, errors)
    }
}