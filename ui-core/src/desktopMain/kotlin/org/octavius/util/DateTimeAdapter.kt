package org.octavius.util

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlin.reflect.KClass
import kotlin.time.Instant

private val DATE_FORMAT = LocalDate.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    year()
}

private val TIME_FORMAT = LocalTime.Format {
    hour()
    char(':')
    minute()
    char(':')
    second()
}

private val DATE_TIME_FORMAT = LocalDateTime.Format {
    date(DATE_FORMAT)
    char(' ')
    time(TIME_FORMAT)
}

enum class DateTimeComponent { DATE, TIME }

data class DateTimePickerState(
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val offset: UtcOffset? = null
)

interface DateTimeAdapter<T : Any> {
    /** Klasa wartości, po której kontrolka formularza sprawdza wartość początkową - [T] jest wymazane. */
    val valueClass: KClass<T>
    val requiredComponents: Set<DateTimeComponent>
    fun format(value: T?): String
    fun getComponents(value: T?): DateTimePickerState
    fun buildFromComponents(date: LocalDate?, time: LocalTime?, offset: UtcOffset?): T?

    fun serialize(value: T): String
    fun deserialize(value: String): T?
}

// --- Konkretne Implementacje ---
object DateAdapter : DateTimeAdapter<LocalDate> {
    override val valueClass = LocalDate::class
    override val requiredComponents = setOf(DateTimeComponent.DATE)
    override fun format(value: LocalDate?) = value?.format(DATE_FORMAT) ?: ""
    override fun getComponents(value: LocalDate?) = DateTimePickerState(date = value)
    override fun buildFromComponents(date: LocalDate?, time: LocalTime?, offset: UtcOffset?) = date

    override fun deserialize(value: String): LocalDate = LocalDate.parse(value)
    override fun serialize(value: LocalDate) = value.toString()
}

object LocalTimeAdapter : DateTimeAdapter<LocalTime> {
    override val valueClass = LocalTime::class
    override val requiredComponents = setOf(DateTimeComponent.TIME)
    override fun format(value: LocalTime?) = value?.format(TIME_FORMAT) ?: ""
    override fun getComponents(value: LocalTime?) = DateTimePickerState(time = value)
    override fun buildFromComponents(date: LocalDate?, time: LocalTime?, offset: UtcOffset?) = time

    override fun deserialize(value: String): LocalTime = LocalTime.parse(value)
    override fun serialize(value: LocalTime) = value.toString()
}

object LocalDateTimeAdapter : DateTimeAdapter<LocalDateTime> {
    override val valueClass = LocalDateTime::class
    override val requiredComponents = setOf(DateTimeComponent.DATE, DateTimeComponent.TIME)
    override fun format(value: LocalDateTime?) = value?.format(DATE_TIME_FORMAT) ?: ""
    override fun getComponents(value: LocalDateTime?) = DateTimePickerState(
        date = value?.date,
        time = value?.time
    )
    override fun buildFromComponents(date: LocalDate?, time: LocalTime?, offset: UtcOffset?): LocalDateTime? {
        return if (date != null && time != null) LocalDateTime(date, time) else null
    }

    override fun deserialize(value: String): LocalDateTime = LocalDateTime.parse(value)
    override fun serialize(value: LocalDateTime) = value.toString()
}

/**
 * Adapter dla Instant, który jest świadomy strefy czasowej.
 * UI pokaże datę, czas i offset, co daje użytkownikowi pełen kontekst.
 */
class InstantAdapter(private val timeZone: TimeZone = TimeZone.currentSystemDefault()) : DateTimeAdapter<Instant> {
    override val valueClass = Instant::class
    override val requiredComponents = setOf(DateTimeComponent.DATE, DateTimeComponent.TIME)
    override fun format(value: Instant?): String {
        return value?.let {
            val local = it.toLocalDateTime(timeZone)
            val offset = timeZone.offsetAt(it)
            "${local.format(DATE_TIME_FORMAT)} ($offset)"
        } ?: ""
    }
    override fun getComponents(value: Instant?): DateTimePickerState {
        return value?.let {
            val offset = timeZone.offsetAt(it)
            val local = it.toLocalDateTime(timeZone)
            DateTimePickerState(
                date = local.date,
                time = local.time,
                offset = offset
            )
        } ?: DateTimePickerState()
    }
    override fun buildFromComponents(date: LocalDate?, time: LocalTime?, offset: UtcOffset?): Instant? {
        return if (date != null && time != null && offset != null) {
            LocalDateTime(date, time).toInstant(offset)
        } else null
    }

    override fun deserialize(value: String): Instant = Instant.parse(value)

    override fun serialize(value: Instant) = value.toString()
}
