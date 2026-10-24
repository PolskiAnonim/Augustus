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
 * (`IntervalColumn`, `IntervalFilter`) mówi jednak [Duration], więc konwerter spłaszcza go po regule
 * `justify_interval`: miesiąc to 30 dni, dzień to 24 godziny.
 */
object PgIntervalAsDurationConverter : ResultConverter<PgInterval, Duration> {
    // `PgInterval` jest sealed, więc wpis pod nim obejmuje Finite, Infinity i MinusInfinity naraz.
    override val supportedSourceClass: KClass<PgInterval> = PgInterval::class

    override fun canConvert(
        sourceClass: KClass<*>, expectedType: KType, sourceType: PgType, context: DeserializationContext
    ): Boolean = expectedType.classifier == Duration::class

    override fun convert(
        source: PgInterval, expectedType: KType, sourceType: PgType, context: DeserializationContext
    ): Duration = source.toDurationApproximate()
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
