package org.octavius.report

import androidx.compose.ui.unit.Dp
import io.github.octaviusframework.db.api.exception.DatabaseException
import org.octavius.domain.EnumWithFormatter
import org.octavius.report.localization.ReportTr


data class ReportPaginationState(
    val currentPage: Long = 0,
    val totalPages: Long = 1,
    val totalItems: Long = 0,
    val pageSize: Long = 50
) {
    fun resetPage(): ReportPaginationState = this.copy(currentPage = 0)
}

enum class NumberFilterDataType: EnumWithFormatter<NumberFilterDataType> {
    Equals,      // ==
    NotEquals,   // !=
    LessThan,    //
    LessEquals,  // <=
    GreaterThan, // >
    GreaterEquals, // >=
    Range;        // between min and max

    override fun toDisplayString(): String {
        return when(this) {
            Equals -> ReportTr.Filter.Number.equals()
            NotEquals -> ReportTr.Filter.Number.notEquals()
            LessThan -> ReportTr.Filter.Number.lessThan()
            LessEquals -> ReportTr.Filter.Number.lessEqual()
            GreaterThan -> ReportTr.Filter.Number.greaterThan()
            GreaterEquals -> ReportTr.Filter.Number.greaterEqual()
            Range -> ReportTr.Filter.Number.range()
        }
    }
}

enum class StringFilterDataType: EnumWithFormatter<StringFilterDataType> {
    Exact,       // dokładne dopasowanie
    NotExact,    // brak dokładnego dopasowania
    StartsWith,  // od początku
    EndsWith,    // od końca
    Contains,    // dowolny fragment
    NotContains;  // nie zawiera

    override fun toDisplayString(): String {
        return when(this) {
            Exact -> ReportTr.Filter.String.exact()
            StartsWith -> ReportTr.Filter.String.startsWith()
            EndsWith -> ReportTr.Filter.String.endsWith()
            Contains -> ReportTr.Filter.String.contains()
            NotContains -> ReportTr.Filter.String.notContains()
            NotExact -> ReportTr.Filter.String.notExact()
        }
    }
}

enum class DateTimeFilterDataType : EnumWithFormatter<DateTimeFilterDataType> {
    Equals,      // ==
    NotEquals,   // !=
    Before,      // <
    BeforeEquals, // <=
    After,       // >
    AfterEquals, // >=
    Range;        // between min and max

    override fun toDisplayString(): String {
        return when (this) {
            Equals -> ReportTr.Filter.Datetime.equals()
            NotEquals -> ReportTr.Filter.Datetime.notEquals()
            Before -> ReportTr.Filter.Datetime.before()
            BeforeEquals -> ReportTr.Filter.Datetime.beforeEqual()
            After -> ReportTr.Filter.Datetime.after()
            AfterEquals -> ReportTr.Filter.Datetime.afterEqual()
            Range -> ReportTr.Filter.Datetime.range()
        }
    }
}

enum class IntervalFilterDataType : EnumWithFormatter<IntervalFilterDataType> {
    Equals,      // ==
    NotEquals,   // !=
    LessThan,    // <
    LessEquals,  // <=
    GreaterThan, // >
    GreaterEquals, // >=
    Range;        // between min and max

    override fun toDisplayString(): String {
        return when (this) {
            Equals -> ReportTr.Filter.Interval.equals()
            NotEquals -> ReportTr.Filter.Interval.notEquals()
            LessThan -> ReportTr.Filter.Interval.lessThan()
            LessEquals -> ReportTr.Filter.Interval.lessEqual()
            GreaterThan -> ReportTr.Filter.Interval.greaterThan()
            GreaterEquals -> ReportTr.Filter.Interval.greaterEqual()
            Range -> ReportTr.Filter.Interval.range()
        }
    }
}

enum class NullHandling {
    Ignore,      // Ignoruj wartości null
    Include,     // Dołącz wartości null - dla pustej wartości - tylko nulle
    Exclude,     // Wyklucz wartości null
}

enum class FilterMode: EnumWithFormatter<FilterMode> {
    Single,
    ListAny,
    ListAll;

    override fun toDisplayString(): String {
        return when (this) {
            Single -> "" // Ta wartość jest niemożliwa do zmiany i jest niewidoczna
            ListAny -> ReportTr.Filter.List.any()
            ListAll -> ReportTr.Filter.List.all()
        }
    }
}

/**
 * Reprezentuje szerokość kolumny w raporcie.
 * Może być stała (w Dp) lub elastyczna (jako waga w RowScope).
 */
sealed class ColumnWidth {
    /**
     * Stała szerokość kolumny, zdefiniowana w Dp.
     * Używana dla kolumn z UI, jak przyciski akcji.
     */
    data class Fixed(val width: Dp) : ColumnWidth()

    /**
     * Elastyczna szerokość kolumny, zdefiniowana jako waga.
     * Kolumna zajmie przestrzeń proporcjonalną do swojej wagi.
     * Używana dla kolumn z danymi.
     */
    data class Flexible(val weight: Float) : ColumnWidth()
}

sealed class ReportDataResult {
    /** Sukces - zawiera dane i stan paginacji */
    data class Success(
        val data: List<Map<String, Any?>>,
        val paginationState: ReportPaginationState
    ) : ReportDataResult()

    /** Porażka - zawiera błąd */
    data class Failure(val error: DatabaseException) : ReportDataResult()
}