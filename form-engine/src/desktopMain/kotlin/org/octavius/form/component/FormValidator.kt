package org.octavius.form.component

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.github.octaviusframework.client.OctaviusClient
import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlHierarchyRole
import org.octavius.form.control.base.FormResultData

/**
 * Klasa odpowiedzialna za walidację formularza na dwóch poziomach:
 * 1. Walidacja pól - sprawdza wymagalność, format, zależności między kontrolkami
 * 2. Walidacja reguł biznesowych - sprawdza niestandardowe reguły specyficzne dla domeny
 * 3. Walidacja specyficzna dla akcji - pozwala na dodatkowe reguły dla konkretnych przycisków
 * Operuje na stanie formularza
 *
 * Klasa może być rozszerzona dla implementacji niestandardowych reguł walidacji.
 */
open class FormValidator : KoinComponent {

    protected lateinit var formState: FormState
    protected lateinit var formSchema: FormSchema
    protected lateinit var errorManager: ErrorManager

    protected val db: OctaviusClient by inject()

    internal fun setupFormReferences(formState: FormState, formSchema: FormSchema, errorManager: ErrorManager) {
        this.formState = formState
        this.formSchema = formSchema
        this.errorManager = errorManager
    }

    /**
     * Waliduje wszystkie pola formularza.
     *
     * Proces walidacji:
     * 1. Czyści poprzednie błędy
     * 2. Uruchamia walidację każdej kontrolki przez jej validator
     * 3. Sprawdza wymagalność, format, zależności
     *
     * @return true jeśli wszystkie pola są poprawne
     */
    internal fun validateFields(): Boolean {

        for ((controlName, control) in formSchema.getAllControls()) {
            // Dzieci sekcji i repeatable zostaną pominięte, bo ich
            // walidację uruchomi walidator rodzica.
            if (control.hierarchyRole == ControlHierarchyRole.ROOT) {
                val state = formState.getControlState(controlName)!!

                // Zwykłe kontrolki nie mają rodzica, tworzymy prosty kontekst.
                control.validateControl(ControlContext(localName = controlName), state)
            }
        }

        // Sprawdź czy są jakieś błędy pól
        return !errorManager.hasFieldErrors() && !errorManager.hasFormatErrors()
    }

    /**
     * Waliduje reguły biznesowe specyficzne dla domeny.
     *
     * Domyślna implementacja zawsze zwraca true.
     * Klasy pochodne powinny przesłonić tę metodę dla implementacji
     * niestandardowych reguł walidacji (np. sprawdzanie duplikatów,
     * weryfikacja relacji między polami, itp.)
     *
     * @param formResultData zebrane dane z formularza
     * @return true jeśli reguły biznesowe są spełnione
     */
    open fun validateBusinessRules(formResultData: FormResultData): Boolean {
        return true
    }


    /**
     * Definiuje logikę walidacji specyficzną dla poszczególnych akcji formularza.
     * Ta walidacja jest uruchamiana ZAWSZE dla danej akcji, niezależnie od flagi `validates` na przycisku.
     * Uruchamia się po walidacji pól i reguł biznesowych.
     *
     * Klucz mapy odpowiada `actionKey` w `triggerAction`.
     * Wartość to lambda, która otrzymuje `formData` i powinna zwrócić `true` jeśli walidacja się powiodła.
     * W przypadku niepowodzenia, lambda jest odpowiedzialna за ustawienie błędów w `errorManager`.
     *
     * @return Mapa walidacji specyficznych dla akcji.
     */
    open fun defineActionValidations(): Map<String, (formResultData: FormResultData) -> Boolean> {
        return emptyMap()
    }
}