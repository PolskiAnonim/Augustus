package org.octavius.form.component

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.octavius.form.control.type.container.SectionControl
import org.octavius.form.control.type.primitive.StringControl

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
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("'section/pole'")
    }

    @Test
    fun `value under a section name and under its paths fails loudly`() {
        assertThatThrownBy { FormState().initializeStates(schema, mapOf("section" to "x", "section/name" to "y")) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("'section/...'")
    }
}
