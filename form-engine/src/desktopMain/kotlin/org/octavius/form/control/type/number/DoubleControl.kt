package org.octavius.form.control.type.number

import org.octavius.form.control.base.ControlAction
import org.octavius.form.control.base.ControlDependency
import org.octavius.form.control.base.ControlValidator
import org.octavius.form.control.base.DoubleValidation
import org.octavius.form.control.type.number.primitive.PrimitiveNumberControl
import org.octavius.form.control.validator.number.DoubleValidator

/**
 * Kontrolka do wprowadzania liczb rzeczywistych (zmiennoprzecinkowych).
 * Rozszerza PrimitiveNumberControl, dostarczając logikę parsowania dla Double.
 */
class DoubleControl(
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: DoubleValidation? = null,
    actions: List<ControlAction<Double>>? = null
) : PrimitiveNumberControl<Double>(
    label,
    required,
    dependencies,
    validationOptions = validationOptions,
    actions = actions
) {
    override val validator: ControlValidator<Double> = DoubleValidator(validationOptions)

    override fun acceptsInitValue(value: Any) = value is Double

    override fun parseValue(text: String): Double? {
        val cleanText = text.replace(",", ".")
        val doubleValue = cleanText.toDoubleOrNull()
        return if (doubleValue != null && doubleValue.isFinite()) doubleValue else null
    }
}