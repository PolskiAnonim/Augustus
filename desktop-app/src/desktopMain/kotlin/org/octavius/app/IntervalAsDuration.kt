package org.octavius.app

import io.github.octaviusframework.driver.converter.parameter.mapper.ParameterConverter
import io.github.octaviusframework.driver.converter.parameter.mapper.SerializationContext
import io.github.octaviusframework.driver.converter.result.mapper.DeserializationContext
import io.github.octaviusframework.driver.converter.result.mapper.ResultConverter
import io.github.octaviusframework.driver.type.PgType
import io.github.octaviusframework.driver.type.datetime.PgInterval
import io.github.octaviusframework.driver.type.datetime.toDurationApproximate
import io.github.octaviusframework.driver.type.datetime.toPgIntervalApproximate
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.time.Duration

/**
 * `interval` czytany jako [Duration].
 *
 * Sterownik oddaje `interval` jako [PgInterval] - miesiące, dni i czas osobno, bo tyle właśnie trzyma
 * PostgreSQL i tylko tak da się to oddać bez zgadywania długości miesiąca. Report engine
 * (`IntervalColumn`, `IntervalFilter`) mówi jednak [Duration] i tak było w starej bibliotece, więc
 * spłaszczenie zostaje - po regule `justify_interval`: miesiąc to 30 dni, dzień to 24 godziny.
 */
object PgIntervalAsDurationConverter : ResultConverter<Any, Duration> {
    // Rejestr szuka konwertera po dokładnej klasie wartości, a `PgInterval` jest sealed interface -
    // przychodzi zawsze jako Finite, Infinity albo MinusInfinity. Wpis pod Any::class to jedyny sposób,
    // żeby objąć wszystkie trzy naraz; `canConvert` zawęża go z powrotem do tego, o co chodzi.
    override val supportedSourceClass: KClass<Any> = Any::class

    override fun canConvert(
        sourceClass: KClass<*>, expectedType: KType, sourceType: PgType, context: DeserializationContext
    ): Boolean = expectedType.classifier == Duration::class &&
        PgInterval::class.java.isAssignableFrom(sourceClass.java)

    override fun convert(
        source: Any, expectedType: KType, sourceType: PgType, context: DeserializationContext
    ): Duration = (source as PgInterval).toDurationApproximate()
}

/**
 * Druga strona tego samego: [Duration] w parametrze idzie do bazy jako `interval`.
 *
 * Zwracany [PgInterval] jest domyślnym typem Kotlina dla `interval`, więc sterownik sam zadeklaruje
 * właściwy OID i `getDefaultTypeName` nie ma tu nic do powiedzenia.
 */
object DurationAsPgIntervalConverter : ParameterConverter<Duration> {
    override val supportedClass: KClass<Duration> = Duration::class

    override fun convert(source: Duration, expectedOid: Int, context: SerializationContext): Any =
        source.toPgIntervalApproximate()
}
