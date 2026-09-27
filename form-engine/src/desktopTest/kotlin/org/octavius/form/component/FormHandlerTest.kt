package org.octavius.form.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.octavius.form.control.base.Control
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.FormResultData
import org.octavius.form.control.base.RepeatableValidation
import org.octavius.form.control.base.getCurrentAs
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
