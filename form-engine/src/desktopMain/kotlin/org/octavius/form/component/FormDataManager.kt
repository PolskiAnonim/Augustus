package org.octavius.form.component

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.github.octaviusframework.client.OctaviusClient
import org.octavius.form.control.base.FormResultData

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

    fun loadData(id: Any?, block: DataLoaderBuilder.() -> Unit): Map<String, Any?> {
        val builder = DataLoaderBuilder(db).apply(block)
        return builder.execute(id)
    }

    /**
     * Dostarcza wartości początkowe dla kontrolek formularza.
     *
     * @param payload dodatkowe dane dla formularza (pusta mapa dla braku dodatkowych danych)
     * @return mapa kontrolka->wartość z wartościami domyślnymi lub obliczonymi
     */
    abstract fun initData(payload: Map<String, Any?>): Map<String, Any?>

    /**
    * Definiuje logikę dla wszystkich akcji formularza (Zapisz, Anuluj, Usuń, etc.).
    * Klucz mapy odpowiada `actionKey` w `SubmitButtonControl`.
    * Wartość to lambda, która otrzymuje:
    * - formData: aktualne dane z formularza
    * - loadedId: ID edytowanego rekordu
    * Powinna zwrócić `FormActionResult`, aby FormHandler wiedział, czy operacja się udała.
    *
    * @return Mapa akcji formularza.
    */
    open fun definedFormActions(): Map<String, (formResultData: FormResultData) -> FormActionResult> {
        return emptyMap()
    }
}