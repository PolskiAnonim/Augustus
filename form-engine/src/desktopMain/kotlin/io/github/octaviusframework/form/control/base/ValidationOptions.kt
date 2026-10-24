package io.github.octaviusframework.form.control.base

import java.math.BigDecimal
import kotlin.time.Duration

/**
 * Opcje walidacji dla kontrolek formularza.
 * Definiuje reguły walidacji które można zastosować do różnych typów kontrolek.
 */
sealed class ValidationOptions

/**
 * Opcje walidacji dla pól tekstowych.
 */
data class StringValidation(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: Regex? = null,
    val patternErrorMessage: String? = null
) : ValidationOptions()

/**
 * Opcje walidacji dla liczb całkowitych.
 */
data class IntegerValidation(
    val min: Int? = null,
    val max: Int? = null,
    val step: Int? = null
) : ValidationOptions()

/**
 * Opcje walidacji dla liczb zmiennoprzecinkowych.
 */
data class DoubleValidation(
    val min: Double? = null,
    val max: Double? = null,
    val step: Double? = null,
    val decimalPlaces: Int? = null
) : ValidationOptions()

/**
 * Opcje walidacji dla liczb o wysokiej precyzji (BigDecimal).
 */
data class BigDecimalValidation(
    val min: BigDecimal? = null,
    val max: BigDecimal? = null,
    val step: BigDecimal? = null,
    val decimalPlaces: Int? = null
) : ValidationOptions()

/**
 * Opcje walidacji dla list tekstowych.
 */
data class StringListValidation(
    val minItems: Int? = null,
    val maxItems: Int? = null,
    val itemValidation: StringValidation? = null
) : ValidationOptions()

/**
 * Opcje walidacji dla kontrolek powtarzalnych.
 */
data class RepeatableValidation(
    val minItems: Int? = null,
    val maxItems: Int? = null,
    val uniqueFields: List<String> = emptyList()
) : ValidationOptions()

/**
 * Generyczne opcje walidacji dla wszystkich kontrolek daty i czasu.
 */
data class DateTimeValidation<T>(
    val min: T? = null,
    val max: T? = null
) : ValidationOptions()


/**
 * Opcje walidacji dla interwałów czasowych.
 */
data class IntervalValidation(
    val min: Duration? = null,
    val max: Duration? = null
) : ValidationOptions()