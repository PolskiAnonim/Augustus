package org.octavius.form.component


/**
 * Wynik akcji formularza zwracany przez `FormDataManager`.
 *
 * Wariant `Navigate(screen: Screen)` został usunięty razem z wydzieleniem modułu `:navigation`:
 * nie miał ani jednego użycia poza silnikiem, a jako jedyny wciągał typ `Screen` do form-engine.
 * Jeżeli formularz ma po akcji przejść na inny ekran, robi to feature w swojej lambdzie akcji
 * (`AppRouter.navigateTo(...)`) i zwraca [Success] - tak jak wszystkie pozostałe miejsca w kodzie.
 */
sealed class FormActionResult {
    // Akcje zmieniające UI
    object CloseScreen : FormActionResult() // Akcja "Anuluj" - o tym, co znaczy "zamknij", decyduje `FormHandler.onClose`
    //Akcje generyczne
    object ValidationFailed : FormActionResult() // Błędy walidacji
    object Failure : FormActionResult() // Ogólny błąd
    object Success : FormActionResult() // Generyczny sukces, np. po zapisie
}

interface FormActionTrigger {
    suspend fun triggerAction(actionKey: String, validates: Boolean): FormActionResult
}
