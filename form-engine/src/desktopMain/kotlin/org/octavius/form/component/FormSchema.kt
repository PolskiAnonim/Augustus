package org.octavius.form.component

import org.octavius.form.control.base.Control
import org.octavius.form.control.base.ControlContext

/**
 * Schemat formularza - definicja struktury i układu kontrolek.
 *
 * FormSchema przechowuje kompletna definicję formularza włączając:
 * - Wszystkie kontrolki z ich konfiguracją
 * - Kolejność wyświetlania w głównym obszarze
 * - Kolejność wyświetlania w pasku akcji
 *
 * Schemat jest tworzona przez FormSchemaBuilder i jest niezmienny po utworzeniu.
 *
 * @param controls Mapa wszystkich kontrolek formularza (klucz = unikalna nazwa).
 * @param contentOrder Lista nazw kontrolek do wyświetlenia w głównym obszarze formularza.
 * @param actionBarOrder Lista nazw kontrolek do wyświetlenia w dolnym pasku akcji.
 *
 * @throws IllegalArgumentException jeśli nazwy w listach porządkujących nie istnieją w mapie controls.
 */
class FormSchema(
    private val definedControls: Map<String, Control<*>>,
    val contentOrder: List<String>,
    val actionBarOrder: List<String>
) {

    val controls: Map<String, Control<*>>

    init {
        val ctrls = definedControls.toMutableMap()
        definedControls.forEach { (key, value) ->
            ctrls.putAll(value.registerChildrenInGlobalMap(ControlContext(key)))
        }
        controls = ctrls.toMap()
    }

    /**
     * Zwraca kontrolkę o danej nazwie
     */
    fun getControl(name: String): Control<*>? = controls[name]

    /**
     * Zwraca wszystkie kontrolki zdefiniowane w tej klasie
     */
    fun getAllControls(): Map<String, Control<*>> = controls

    /**
     * Kontrolki z górnego poziomu definicji, z kontekstem bez rodzica. Od nich silnik schodzi
     * w dół drzewa przez [Control.childContexts].
     */
    internal fun rootContexts(): List<Pair<ControlContext, Control<*>>> {
        return definedControls.map { (name, control) -> ControlContext(name) to control }
    }
}

/**
 * Abstrakcyjna fabryka do tworzenia schematów formularzy.
 *
 * Każdy formularz powinien mieć własną implementację buildera,
 * która definiuje specyficzną strukturę kontrolek, ich kolejność
 * i konfiguracje dla danej domeny biznesowej.
 *
 * Przykładowa implementacja:
 * ```kotlin
 * class UserFormSchemaBuilder : FormSchemaBuilder() {
 *     override fun build(): FormSchema {
 *         return FormSchema(
 *             controls = mapOf(
 *                 "name" to StringControl(label = "Imię", required = true),
 *                 "email" to StringControl(label = "Email")
 *             ),
 *             contentOrder = listOf("name", "email"),
 *             actionBarOrder = listOf("save", "cancel")
 *         )
 *     }
 * }
 * ```
 */
abstract class FormSchemaBuilder {
    fun build(): FormSchema {
        return FormSchema(
            defineControls(),
            defineContentOrder(),
            defineActionBarOrder()
        )
    }

    abstract fun defineControls() : Map<String, Control<*>>

    abstract fun defineContentOrder() : List<String>

    abstract fun defineActionBarOrder() : List<String>

}