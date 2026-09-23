package org.octavius.form.control.validator.number

import org.octavius.form.control.base.BigDecimalValidation
import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.ControlValidator
import org.octavius.form.localization.FormTr
import java.math.BigDecimal

/**
 * Walidator dla kontrolek liczb o wysokiej precyzji (BigDecimal) z obsługą opcji walidacji.
 */
class BigDecimalValidator(
    private val validationOptions: BigDecimalValidation? = null
) : ControlValidator<BigDecimal>() {

    override fun validateSpecific(controlContext: ControlContext, state: ControlState<*>) {
        val value = state.value.value as? BigDecimal ?: return
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
                if (value.scale() > decimalPlaces) {
                    errors.add(FormTr.Validation.maxDecimalPlaces(decimalPlaces))
                }
            }

            // Sprawdź krok
            options.step?.let { step ->
                if (value.remainder(step).compareTo(BigDecimal.ZERO) != 0) {
                    errors.add(FormTr.Validation.multipleOf(step))
                }
            }
        }

        errorManager.setFieldErrors(controlContext.fullStatePath, errors)
    }
}