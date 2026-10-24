package org.octavius.form.control.type.number

import org.octavius.form.control.base.BigDecimalValidation
import org.octavius.form.control.base.ControlAction
import org.octavius.form.control.base.ControlDependency
import org.octavius.form.control.base.ControlValidator
import org.octavius.form.control.type.number.primitive.PrimitiveNumberControl
import org.octavius.form.control.validator.number.BigDecimalValidator
import java.math.BigDecimal

/**
 * Kontrolka do wprowadzania liczb o wysokiej precyzji (BigDecimal).
 * Rozszerza PrimitiveNumberControl, dostarczając logikę parsowania dla BigDecimal.
 */
class BigDecimalControl(
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: BigDecimalValidation? = null,
    actions: List<ControlAction<BigDecimal>>? = null
) : PrimitiveNumberControl<BigDecimal>(
    label,
    required,
    dependencies,
    validationOptions = validationOptions,
    actions = actions
) {
    override val validator: ControlValidator<BigDecimal> = BigDecimalValidator(validationOptions)

    override fun acceptsInitValue(value: Any) = value is BigDecimal

    override fun parseValue(text: String): BigDecimal? {
        // Zapewniamy obsługę zarówno kropki jak i przecinka jako separatora dziesiętnego
        val cleanText = text.replace(",", ".")
        return cleanText.toBigDecimalOrNull()
    }
}