package io.github.octaviusframework.form.component

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.github.octaviusframework.client.OctaviusClient
import io.github.octaviusframework.form.control.base.FormResultData

/**
 * Abstrakcyjna klasa zarządzająca przepływem danych w formularzach.
 *
 * FormDataManager odpowiada za:
 * - Ładowanie danych z bazy danych dla edytowanych encji
 * - Dostarczanie wartości domyślnych dla nowych rekordów
 * - Definiowanie relacji między tabelami
 * - Przetwarzanie danych formularza do operacji bazodanowych
 *
 * Każdy formularz musi implementować własny DataManager dostosowany
 * do specyfiki domeny i struktury bazy danych.
 */
abstract class FormDataManager: KoinComponent {

    lateinit var errorManager: ErrorManager
    internal fun setupFormReferences(errorManager: ErrorManager) {
        this.errorManager = errorManager
    }

    protected val db: OctaviusClient by inject()

    /**
     * Wczytuje rekord o [id] według mapowania z [block] (DSL z `FormLoader.kt`).
     *
     * @return Wartości pod ścieżkami kontrolek albo pusta mapa, gdy [id] to `null` (nowy rekord).
     *   `null`, gdy wczytanie się nie udało (błąd bazy, brak rekordu) - dialog z powodem już się pokazał,
     *   a `initData` powinno oddać `null` dalej.
     */
    fun loadData(id: Any?, block: DataLoaderBuilder.() -> Unit): Map<String, Any?>? {
        val builder = DataLoaderBuilder(db).apply(block)
        return builder.execute(id)
    }

    /**
     * Dostarcza wartości początkowe dla kontrolek formularza.
     *
     * Każdy klucz musi wskazywać kontrolkę, a wartość musi pasować do jej typu - inaczej silnik rzuci
     * `InitDataException` z tą ścieżką, zanim formularz się narysuje.
     *
     * @param payload dodatkowe dane dla formularza (pusta mapa dla braku dodatkowych danych)
     * @return mapa kontrolka->wartość z wartościami domyślnymi lub obliczonymi. `null` znaczy, że
     *   wczytanie się nie udało, a metoda pokazała już powód (zwykle przez `loadData`) - formularz zostaje
     *   wtedy bez kontrolek. Tak jak `false` z akcji, `null` niesie sam fakt porażki, nie jej treść.
     */
    abstract fun initData(payload: Map<String, Any?>): Map<String, Any?>?

    /**
    * Definiuje logikę dla akcji formularza operujących na danych (Zapisz, Usuń, etc.).
    * Klucz mapy odpowiada `actionKey` w `FormActionTrigger.triggerAction`.
    * Wartość to lambda, która otrzymuje aktualne dane z formularza i zwraca `true`, jeśli
    * operacja się powiodła.
    *
    * Lambda odpowiada za pokazanie błędu (dialog, `errorManager`) przy niepowodzeniu - `false`
    * niesie sam fakt niepowodzenia, nie jego treść.
    *
    * Nie ma tu miejsca na akcje, które nie dotykają danych. Zamknięcie ekranu jest sprawą przycisku
    * w schema builderze, nie warstwy danych.
    *
    * @return Mapa akcji formularza.
    */
    open fun definedFormActions(): Map<String, (formResultData: FormResultData) -> Boolean> {
        return emptyMap()
    }
}