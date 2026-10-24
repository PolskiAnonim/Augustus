package io.github.octaviusframework.form.component

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import io.github.octaviusframework.form.control.type.container.SectionControl
import io.github.octaviusframework.form.control.type.primitive.StringControl

class FormStateTest {

    private val schema = FormSchema(
        mapOf("section" to SectionControl(controls = mapOf("name" to StringControl(null)), label = "sekcja")),
        contentOrder = listOf("section"),
        actionBarOrder = emptyList()
    )

    @Test
    fun `section given as a nested map fails loudly`() {
        // `defaults + loaded` z mapami sekcji podmieniłby całą sekcję, więc pola idą tylko ścieżkami.
        assertThatThrownBy { FormState().initializeStates(schema, mapOf("section" to mapOf("name" to "x"))) }
            .isInstanceOf(InitDataException::class.java)
            .hasFieldOrPropertyWithValue("path", "section")
            .hasMessageContaining("'section/pole'")
    }

    @Test
    fun `value under a section name and under its paths fails loudly`() {
        assertThatThrownBy { FormState().initializeStates(schema, mapOf("section" to "x", "section/name" to "y")) }
            .isInstanceOf(InitDataException::class.java)
            .hasFieldOrPropertyWithValue("path", "section")
            .hasMessageContaining("'section/...'")
    }

    @Test
    fun `value under a name without a control fails loudly`() {
        assertThatThrownBy { FormState().initializeStates(schema, mapOf("titel" to "x")) }
            .isInstanceOf(InitDataException::class.java)
            .hasFieldOrPropertyWithValue("path", "titel")
    }
}
