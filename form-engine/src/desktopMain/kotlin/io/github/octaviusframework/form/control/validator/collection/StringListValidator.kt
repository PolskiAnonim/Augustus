package io.github.octaviusframework.form.control.validator.collection

import io.github.octaviusframework.form.control.base.ControlContext
import io.github.octaviusframework.form.control.base.ControlState
import io.github.octaviusframework.form.control.base.ControlValidator
import io.github.octaviusframework.form.control.base.StringListValidation
import io.github.octaviusframework.form.localization.FormTr

/**
 * Validator of text list controls, with validation options.
 */
class StringListValidator(
    private val validationOptions: StringListValidation? = null
) : ControlValidator<List<String>>() {

    override fun validateSpecific(controlContext: ControlContext, state: ControlState<*>) {
        @Suppress("UNCHECKED_CAST")
        val value = state.value.value as? List<String> ?: return
        val errors = mutableListOf<String>()

        validationOptions?.let { options ->
            // Minimum number of items
            options.minItems?.let { minItems ->
                if (value.size < minItems) {
                    errors.add(FormTr.Validation.minimumItems(minItems))
                }
            }

            // Maximum number of items
            options.maxItems?.let { maxItems ->
                if (value.size > maxItems) {
                    errors.add(FormTr.Validation.maximumItems(maxItems))
                }
            }

            // Validation of individual items
            options.itemValidation?.let { itemValidation ->
                value.forEachIndexed { index, item ->
                    val tempErrors = mutableListOf<String>()
                    val itemState = ControlState<String>()
                    itemState.value.value = item

                    // Validate the item without the ErrorManager (for now)
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