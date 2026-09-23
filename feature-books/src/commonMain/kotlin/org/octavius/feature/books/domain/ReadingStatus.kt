package org.octavius.feature.books.domain

import io.github.octaviusframework.annotation.PgEnumType
import org.octavius.domain.EnumWithFormatter
import org.octavius.feature.books.localization.BooksTr

@PgEnumType(name = "reading_status")
enum class ReadingStatus : EnumWithFormatter<ReadingStatus> {
    NotReading,
    Reading,
    Completed,
    PlanToRead;

    override fun toDisplayString(): String {
        return when (this) {
            NotReading -> BooksTr.Status.notReading()
            Reading -> BooksTr.Status.reading()
            Completed -> BooksTr.Status.completed()
            PlanToRead -> BooksTr.Status.planToRead()
        }
    }
}