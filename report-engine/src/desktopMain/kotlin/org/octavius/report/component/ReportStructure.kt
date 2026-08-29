package org.octavius.report.component

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.github.octaviusframework.client.OctaviusClient
import io.github.octaviusframework.client.query.QueryFragment
import io.github.octaviusframework.client.query.join
import io.github.octaviusframework.client.query.withParam
import org.octavius.report.ReportMainAction
import org.octavius.report.ReportRowAction
import org.octavius.report.column.ReportColumn
import org.octavius.report.column.type.special.ActionColumn
import org.octavius.report.column.type.special.QuickActionColumn

/**
 * Definicja struktury raportu - schemat określająca zapytania, kolumny i akcje.
 *
 * ReportStructure to główna klasa konfiguracyjna systemu raportowania, która:
 * - Definiuje zapytanie SQL dla pobierania danych
 * - Konfiguruje kolumny z ich typami, filtrami i opcjami sortowania
 * - Określa akcje dostępne dla wierszy i całego raportu
 * - Automatycznie dodaje kolumny specjalne (akcje) do struktury
 *
 * Kolumny dzielą się na:
 * - **Zwykłe kolumny**: Definiowane przez developera, mapują dane z bazy
 * - **Kolumny specjalne**: Generowane automatycznie (np. kolumna akcji)
 *
 * @param queryFragment Zapytanie SQL do pobrania danych raportu.
 * @param initColumns Mapa kolumn zdefiniowanych przez developera (klucz = nazwa kolumny w SQL).
 * @param reportName Unikalna nazwa raportu (używana do zapisywania konfiguracji).
 * @param rowActions Lista akcji dostępnych dla każdego wiersza.
 * @param defaultRowAction Domyślna akcja wykonywana po podwójnym kliknięciu wiersza.
 * @param mainActions Lista akcji głównych (np. "Dodaj nowy").
 * @param quickSearchMapper Opcjonalna funkcja mapująca tekst wyszukiwania na fragment zapytania.
 */
class ReportStructure(
    val queryFragment: QueryFragment,
    private val initColumns: Map<String, ReportColumn>,
    val reportName: String,
    val rowActions: List<ReportRowAction> = emptyList(),
    val defaultRowAction: ReportRowAction? = null,
    val mainActions: List<ReportMainAction> = emptyList(),
    private val quickSearchMapper: ((String) -> QueryFragment?)? = null
) {
    lateinit var columns: Map<String, ReportColumn>

    /**
     * Zwraca listę kluczy kolumn, które mogą być zarządzane przez użytkownika
     * w panelu konfiguracji (ukrywanie, zmiana kolejności).
     * Pomija kolumny specjalne (np. akcje).
     */
    val manageableColumnKeys: List<String> get() = initColumns.keys.toList()

    fun initSpecialColumns() {
        val specialColumns = mutableMapOf<String, ReportColumn>()
        if (rowActions.isNotEmpty()) {
            specialColumns["_actions"] = ActionColumn(rowActions)
        }
        if (defaultRowAction != null) {
            specialColumns["_quick_action"] = QuickActionColumn(defaultRowAction)
        }
        columns = specialColumns + initColumns
    }

    /**
     * Zwraca kolumnę o danej nazwie
     */
    fun getColumn(name: String): ReportColumn {
       return columns.getValue(name)
    }

    /**
     * Zwraca wszystkie kolumny zdefiniowane w tej strukturze
     */
    fun getAllColumns(): Map<String, ReportColumn> = columns

    /**
     * Zwraca fragment zapytania dla szybkiego wyszukiwania.
     * Jeśli zdefiniowano [quickSearchMapper], używa go.
     * W przeciwnym razie buduje domyślny filtr po wszystkich filtrowalnych kolumnach.
     */
    fun getQuickSearchFilter(searchQuery: String): QueryFragment {
        if (searchQuery.isBlank()) return QueryFragment("")

        return quickSearchMapper?.invoke(searchQuery) ?: buildDefaultQuickSearchFilter(searchQuery)
    }

    private fun buildDefaultQuickSearchFilter(searchQuery: String): QueryFragment {
        return getAllColumns()
            .filter { (_, column) -> column.filterable }
            .map { (columnKey, _) ->
                "$columnKey::text ILIKE @searchQuery" withParam ("searchQuery" to "%$searchQuery%")
            }.join(" OR ")
    }
}

/**
 * Abstrakcyjna fabryka do tworzenia struktur raportów.
 *
 * Używa wzorca Template Method do zdefiniowania szkieletu procesu budowania raportu.
 * Klasy dziedziczące implementują konkretne części:
 * - Zapytanie SQL
 * - Definicje kolumn
 * - Akcje użytkownika
 *
 * Przykład implementacji:
 * ```kotlin
 * class UsersReportStructureBuilder : ReportStructureBuilder() {
 *     override fun getReportName() = "users"
 *     override fun buildQuery() = Query("SELECT id, name, email FROM users")
 *     override fun buildColumns() = mapOf(
 *         "id" to NumberColumn("ID"),
 *         "name" to StringColumn("Nazwa"),
 *         "email" to StringColumn("Email")
 *     )
 * }
 * ```
 */
abstract class ReportStructureBuilder: KoinComponent {

    val db: OctaviusClient by inject()
    /**
     * Główna metoda budująca. Orkiestruje proces i nie powinna być nadpisywana.
     */
    fun build(): ReportStructure {
        val structure = buildStructure()
        structure.initSpecialColumns()
        return structure
    }

    /**
     * Metoda szablonowa, która składa raport z części dostarczonych
     * przez metody abstrakcyjne.
     */
    private fun buildStructure(): ReportStructure {
        return ReportStructure(
            queryFragment = buildQuery(),
            initColumns = buildColumns(),
            reportName = getReportName(),
            rowActions = buildRowActions(),
            defaultRowAction = buildDefaultRowAction(),
            mainActions = buildMainActions(),
            quickSearchMapper = { buildQuickSearch(it) }
        )
    }

    /**
     * Zwraca nazwę raportu.
     */
    abstract fun getReportName(): String

    /**
     * Buduje i zwraca obiekt Query dla raportu.
     */
    abstract fun buildQuery(): QueryFragment

    /**
     * Buduje i zwraca mapę kolumn dla raportu.
     */
    abstract fun buildColumns(): Map<String, ReportColumn>

    /**
     * Buduje i zwraca listę akcji dla wierszy.
     * Domyślnie zwraca pustą listę, ponieważ nie każdy raport ma akcje.
     */
    open fun buildRowActions(): List<ReportRowAction> = emptyList()

    /**
     * Buduje i zwraca domyślną akcję dla wierszy.
     * Domyślnie zwraca null.
     */
    open fun buildDefaultRowAction(): ReportRowAction? = null

    /**
     * Buduje i zwraca listę akcji dla menu "Dodaj".
     * Domyślnie zwraca pustą listę.
     */
    open fun buildMainActions(): List<ReportMainAction> = emptyList()

    /**
     * Buduje fragment zapytania dla szybkiego wyszukiwania.
     * Nadpisz tę metodę, aby dostarczyć niestandardową logikę wyszukiwania.
     */
    open fun buildQuickSearch(searchQuery: String): QueryFragment? = null
}