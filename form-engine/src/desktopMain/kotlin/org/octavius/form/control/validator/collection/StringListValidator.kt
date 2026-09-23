package org.octavius.form.control.validator.collection

import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.ControlValidator
import org.octavius.form.control.base.StringListValidation
import org.octavius.form.localization.FormTr

/**
 * Walidator dla kontrolek list tekstowych z obsługą opcji walidacji.
 */
class StringListValidator(
    private val validationOptions: StringListValidation? = null
) : ControlValidator<List<String>>() {

    override fun validateSpecific(controlContext: ControlContext, state: ControlState<*>) {
        @Suppress("UNCHECKED_CAST")
        val value = state.value.value as? List<String> ?: return
        val errors = mutableListOf<String>()

        validationOptions?.let { options ->
            // Sprawdź minimalną liczbę elementów
            options.minItems?.let { minItems ->
                if (value.size < minItems) {
                    errors.add(FormTr.Validation.minimumItems(minItems))
                }
            }

            // Sprawdź maksymalną liczbę elementów
            options.maxItems?.let { maxItems ->
                if (value.size > maxItems) {
                    errors.add(FormTr.Validation.maximumItems(maxItems))
                }
            }

            // Sprawdź walidację pojedynczych elementów
            options.itemValidation?.let { itemValidation ->
                value.forEachIndexed { index, item ->
                    val tempErrors = mutableListOf<String>()
                    val itemState = ControlState<String>()
                    itemState.value.value = item

                    // Sprawdź walidację elementu bez używania ErrorManagera (tymczasowo)
                    if (item.isNotBlank()) {
                        itemValidation.minLength?.let { minLength ->
                            if (item.length < minLength) {
                                tempErrors.add(FormTr.Validation.itemMinLength(index + 1, minLength))
                            }
                        }
                        itemValidation.maxLength?.let { maxLength ->
                            if (item.length > maxLength) {
                                tempErrors.add(FormTr.Validation.itemMaxLength(index + 1, maxLength))
                            }
                        }
                        itemValidation.pattern?.let { pattern ->
                            if (!pattern.matches(item)) {
                                tempErrors.add(FormTr.Validation.itemPatternError(
                                    index + 1,
                                    itemValidation.patternErrorMessage ?: FormTr.Validation.invalidFormat()
                                ))
                            }
                        }
                    }
                    errors.addAll(tempErrors)
                }
            }
        }

        errorManager.setFieldErrors(controlContext.fullStatePath, errors)
    }
}