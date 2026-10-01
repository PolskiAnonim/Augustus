package org.octavius.form.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.octavius.form.control.base.ComparisonType
import org.octavius.form.control.base.Control
import org.octavius.form.control.base.ControlDependency
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.DependencyType
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.RepeatableValidation
import org.octavius.form.control.base.getCurrent
import org.octavius.form.control.base.getCurrentAs
import org.octavius.form.control.base.getInitial
import org.octavius.form.control.type.container.SectionControl
import org.octavius.form.control.type.primitive.StringControl
import org.octavius.form.control.type.repeatable.RepeatableControl
import org.octavius.form.control.type.repeatable.RepeatableResultValue
import org.octavius.form.control.type.repeatable.RepeatableRow

class FormHandlerTest {

    private fun handler(
        controls: Map<String, Control<*>>,
        initData: Map<String, Any?> = emptyMap(),
        validator: FormValidator = FormValidator(),
        save: (FormResultData) -> Boolean
    ) = FormHandler(
        formSchemaBuilder = object : FormSchemaBuilder() {
            override fun defineControls() = controls
            override fun defineContentOrder() = controls.keys.toList()
            override fun defineActionBarOrder() = emptyList<String>()
        },
        formDataManager = object : FormDataManager() {
            override fun initData(payload: Map<String, Any?>) = initData
            override fun definedFormActions() = mapOf("save" to save)
        },
        formValidator = validator,
        // Unconfined: ładowanie danych kończy się w konstruktorze, bez czekania.
        handlerScope = CoroutineScope(Dispatchers.Unconfined)
    )

    @Test
    fun `retry after failed save still reports row deleted from the database`() {
        val deletedRowsSeen = mutableListOf<Int>()
        val handler = handler(
            controls = mapOf(
                "items" to RepeatableControl(mapOf("name" to StringControl(null)), listOf("name"), null)
            ),
            initData = mapOf("items" to listOf(mapOf("name" to "z bazy")))
        ) { formData ->
            deletedRowsSeen += formData.getCurrentAs<RepeatableResultValue>("items").deletedRows.size
            false // zapis się nie udał, użytkownik spróbuje jeszcze raz
        }

        @Suppress("UNCHECKED_CAST")
        val items = handler.getControlState("items") as ControlState<List<RepeatableRow>>
        items.value.value = emptyList()

        val first = runBlocking { handler.triggerAction("save", validates = false) }
        val second = runBlocking { handler.triggerAction("save", validates = false) }

        assertThat(first).isFalse()
        assertThat(second).isFalse()
        assertThat(deletedRowsSeen).containsExactly(1, 1)
    }

    @Test
    fun `exception in action releases the overlay`() {
        val handler = handler(controls = mapOf("name" to StringControl(null))) { error("akcja padła") }

        assertThatThrownBy { runBlocking { handler.triggerAction("save", validates = false) } }
            .hasMessage("akcja padła")
        assertThat(handler.actionTriggered.value).isFalse()
    }

    @Test
    fun `nested repeatable validates fields of its rows`() {
        var saved = false
        val inner = RepeatableControl(
            rowControls = mapOf("text" to StringControl(null, required = true)),
            rowOrder = listOf("text"),
            label = null,
            validationOptions = RepeatableValidation(minItems = 1)
        )
        // Oba poziomy dostają przy wczytaniu po jednym pustym wierszu z minItems.
        val handler = handler(
            controls = mapOf(
                "outer" to RepeatableControl(
                    rowControls = mapOf("inner" to inner),
                    rowOrder = listOf("inner"),
                    label = null,
                    validationOptions = RepeatableValidation(minItems = 1)
                )
            )
        ) { saved = true; true }

        val result = runBlocking { handler.triggerAction("save", validates = true) }

        assertThat(result).isFalse()
        assertThat(saved).isFalse()
        assertThat(handler.errorManager.fieldErrors.keys).singleElement()
            .satisfies({ assertThat(it).matches("outer\\[.+]/inner\\[.+]/text") })
    }

    // Sekcja widoczna tylko, gdy kontrolka "show" ma wartość "tak".
    private fun sectionShownBy(vararg controls: Pair<String, Control<*>>) = SectionControl(
        controls = mapOf(*controls),
        label = "sekcja",
        dependencies = mapOf(
            "visible" to ControlDependency(
                controlPath = "show",
                value = "tak",
                dependencyType = DependencyType.Visible,
                comparisonType = ComparisonType.Equals
            )
        )
    )

    @Test
    fun `child of hidden section does not pass its value to save`() {
        var savedName: Any? = "nie zapisano"
        var initialName: Any? = null
        val handler = handler(
            controls = mapOf(
                "show" to StringControl(null),
                "section" to sectionShownBy("name" to StringControl(null))
            ),
            initData = mapOf("show" to "nie", "section/name" to "z bazy")
        ) { formData ->
            savedName = formData.getCurrent("section/name")
            initialName = formData.getInitial("section/name")
            true
        }

        runBlocking { handler.triggerAction("save", validates = false) }

        assertThat(savedName).isNull()
        assertThat(initialName).isEqualTo("z bazy")
    }

    @Test
    fun `required child of section is validated`() {
        val handler = handler(
            controls = mapOf(
                "section" to SectionControl(controls = mapOf("name" to StringControl(null, required = true)), label = "sekcja")
            )
        ) { true }

        val result = runBlocking { handler.triggerAction("save", validates = true) }

        assertThat(result).isFalse()
        assertThat(handler.errorManager.fieldErrors.keys).containsExactly("section/name")
    }

    @Test
    fun `same name in two sections keeps two values`() {
        var saved: List<Any?> = emptyList()
        val handler = handler(
            controls = mapOf(
                "first" to SectionControl(controls = mapOf("name" to StringControl(null)), label = "pierwsza"),
                "second" to SectionControl(controls = mapOf("name" to StringControl(null)), label = "druga")
            ),
            initData = mapOf("first/name" to "pierwsza", "second/name" to "druga")
        ) { formData -> saved = listOf(formData.getCurrent("first/name"), formData.getCurrent("second/name")); true }

        runBlocking { handler.triggerAction("save", validates = false) }

        assertThat(saved).containsExactly("pierwsza", "druga")
    }

    @Test
    fun `dependency on a control that does not exist fails loudly`() {
        val handler = handler(
            controls = mapOf(
                "name" to StringControl(
                    null,
                    dependencies = mapOf(
                        "visible" to ControlDependency(
                            controlPath = "missing",
                            value = true,
                            dependencyType = DependencyType.Visible,
                            comparisonType = ComparisonType.Equals
                        )
                    )
                )
            )
        ) { true }

        assertThatThrownBy { runBlocking { handler.triggerAction("save", validates = true) } }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("'missing'")
    }

    @Test
    fun `required child of section nested in hidden section is not validated`() {
        var saved = false
        val handler = handler(
            controls = mapOf(
                "show" to StringControl(null),
                "outer" to sectionShownBy(
                    "inner" to SectionControl(controls = mapOf("name" to StringControl(null, required = true)), label = "wewnętrzna")
                )
            ),
            initData = mapOf("show" to "nie")
        ) { saved = true; true }

        val result = runBlocking { handler.triggerAction("save", validates = true) }

        assertThat(result).isTrue()
        assertThat(saved).isTrue()
    }

    @Test
    fun `section inside repeatable row keeps its children in the row`() {
        var modifiedRows: List<FormResultData> = emptyList()
        val handler = handler(
            controls = mapOf(
                "items" to RepeatableControl(
                    rowControls = mapOf(
                        "details" to SectionControl(controls = mapOf("name" to StringControl(null)), label = "szczegóły")
                    ),
                    rowOrder = listOf("details"),
                    label = null
                )
            ),
            initData = mapOf("items" to listOf(mapOf("details/name" to "z bazy")))
        ) { formData -> modifiedRows = formData.getCurrentAs<RepeatableResultValue>("items").modifiedRows; true }

        @Suppress("UNCHECKED_CAST")
        val rowId = (handler.getControlState("items") as ControlState<List<RepeatableRow>>).value.value!!.single().id
        @Suppress("UNCHECKED_CAST")
        val name = handler.getControlState("items[$rowId]/details/name") as ControlState<String>
        assertThat(name.value.value).isEqualTo("z bazy")
        name.value.value = "zmienione"

        runBlocking { handler.triggerAction("save", validates = false) }

        assertThat(modifiedRows).singleElement()
            .satisfies({ assertThat(it.getCurrent("details/name")).isEqualTo("zmienione") })
    }

    @Test
    fun `field error under a name without a control fails loudly`() {
        val validator = object : FormValidator() {
            override fun validateBusinessRules(formResultData: FormResultData): Boolean {
                errorManager.setFieldErrors("titlePl", listOf("tytuł istnieje"))
                return false
            }
        }
        val handler = handler(controls = mapOf("title_pl" to StringControl(null)), validator = validator) { true }

        assertThatThrownBy { runBlocking { handler.triggerAction("save", validates = true) } }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("titlePl")
    }
}
