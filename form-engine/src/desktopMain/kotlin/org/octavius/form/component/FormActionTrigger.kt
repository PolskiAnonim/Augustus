package org.octavius.form.component

/**
 * Kontrakt, przez który kontrolki uruchamiają akcje formularza.
 *
 * `triggerAction` zwraca wyłącznie informację, czy akcja się powiodła - i to jest cała informacja,
 * jaką silnik oddaje na zewnątrz. Co ma się stać dalej (zamknięcie ekranu, przejście gdzie indziej)
 * decyduje lambda przycisku w schema builderze, czyli kod feature'a, który i tak zna nawigację.
 *
 */
interface FormActionTrigger {
    /**
     * Uruchamia pełen przebieg akcji: walidacja, zebranie danych formularza, wykonanie akcji.
     *
     * @param actionKey Klucz z `FormDataManager.definedFormActions()`.
     * @param validates Czy przed akcją uruchomić walidację pól i reguł biznesowych.
     * @return `true` gdy akcja wykonała się i zgłosiła powodzenie; `false` gdy walidacja nie
     *   przeszła, akcji o takim kluczu nie ma, albo sama akcja zgłosiła niepowodzenie.
     */
    suspend fun triggerAction(actionKey: String, validates: Boolean): Boolean
}
