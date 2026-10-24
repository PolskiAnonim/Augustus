package io.github.octaviusframework.form.control.validator.primitive

import io.github.octaviusframework.form.control.base.ControlContext
import io.github.octaviusframework.form.control.base.ControlState
import io.github.octaviusframework.form.control.base.ControlValidator
import io.github.octaviusframework.form.control.base.StringValidation
import io.github.octaviusframework.form.localization.FormTr

/**
 * Walidator dla kontrolek tekstowych z obsługą opcji walidacji.
 */
class StringValidator(
    private val validationOptions: StringValidation? = null
) : ControlValidator<String>() {

    override fun validateSpecific(controlContext: ControlContext, state: ControlState<*>) {
        val value = state.value.value as? String ?: return
        val errors = mutableListOf<String>()

        if (value.isBlank()) {
            errorManager.setFieldErrors(controlContext.fullStatePath, errors)
            return
        }

        validationOptions?.let { options ->
            // Sprawdź minimalną długość
            options.minLength?.let { minLength ->
                if (value.length < minLength) {
                    errors.add(FormTr.Validation.minLength(minLength))
                }
            }

            // Sprawdź maksymalną długość
            options.maxLength?.let { maxLength ->
                if (value.length > maxLength) {
                    errors.add(FormTr.Validation.maxLength(maxLength))
                }
            }

            // Sprawdź wzorzec
            options.pattern?.let { pattern ->
                if (!pattern.matches(value)) {
                    errors.add(options.patternErrorMessage ?: FormTr.Validation.invalidFormat())
                }
            }
        }

        errorManager.setFieldErrors(controlContext.fullStatePath, errors)
    }
}