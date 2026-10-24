package io.github.octaviusframework.form.control.type.number

import io.github.octaviusframework.form.control.base.ControlAction
import io.github.octaviusframework.form.control.base.ControlDependency
import io.github.octaviusframework.form.control.base.ControlValidator
import io.github.octaviusframework.form.control.base.IntegerValidation
import io.github.octaviusframework.form.control.type.number.primitive.PrimitiveNumberControl
import io.github.octaviusframework.form.control.validator.number.IntegerValidator

/**
 * Kontrolka do wprowadzania liczb całkowitych.
 * Rozszerza PrimitiveNumberControl, dostarczając logikę parsowania dla Int.
 */
class IntegerControl(
    label: String?,
    required: Boolean? = false,
    dependencies: Map<String, ControlDependency<*>>? = null,
    validationOptions: IntegerValidation? = null,
    actions: List<ControlAction<Int>>? = null
) : PrimitiveNumberControl<Int>(
    label,
    required,
    dependencies,
    validationOptions = validationOptions,
    actions = actions
) {
    override val validator: ControlValidator<Int> = IntegerValidator(validationOptions)

    override fun acceptsInitValue(value: Any) = value is Int

    override fun parseValue(text: String): Int? {
        return text.toIntOrNull()
    }
}