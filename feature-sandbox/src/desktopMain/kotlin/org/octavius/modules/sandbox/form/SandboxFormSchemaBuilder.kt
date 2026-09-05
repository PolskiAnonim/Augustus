package org.octavius.modules.sandbox.form

import org.octavius.form.component.FormSchemaBuilder
import org.octavius.form.control.base.*
import org.octavius.form.control.type.button.ButtonControl
import org.octavius.form.control.type.button.ButtonType
import org.octavius.form.control.type.collection.StringListControl
import org.octavius.form.control.type.container.SectionControl
import org.octavius.form.control.type.datetime.DateControl
import org.octavius.form.control.type.number.IntegerControl
import org.octavius.form.control.type.primitive.CheckboxControl
import org.octavius.form.control.type.primitive.FilePickerControl
import org.octavius.form.control.type.primitive.FilePickerMode
import org.octavius.form.control.type.primitive.MultilineStringControl
import org.octavius.form.control.type.primitive.StringControl
import org.octavius.form.control.type.primitive.SwitchControl
import org.octavius.form.control.type.repeatable.RepeatableControl
import org.octavius.form.control.layout.ControlOrientation
import org.octavius.form.control.type.selection.CheckboxGroupControl
import org.octavius.form.control.type.selection.EnumControl
import org.octavius.form.control.type.selection.RadioGroupControl
import org.octavius.form.control.type.selection.SelectionOption
import org.octavius.localization.Tr
import org.octavius.modules.sandbox.domain.SandboxPriority
import org.octavius.navigation.AppRouter

class SandboxFormSchemaBuilder : FormSchemaBuilder() {

    override fun defineControls(): Map<String, Control<*>> = mapOf(
        "name" to StringControl(
            Tr.Sandbox.Form.name(),
            required = true
        ),
        "quantity" to IntegerControl(
            Tr.Sandbox.Form.quantity(),
            validationOptions = IntegerValidation(min = 0)
        ),
        "active" to CheckboxControl(
            Tr.Sandbox.Form.active(),
            required = true
        ),
        "priority" to EnumControl(
            Tr.Sandbox.Form.priority(),
            SandboxPriority::class,
            required = true
        ),
        "start_date" to DateControl(
            Tr.Sandbox.Form.startDate()
        ),
        "tags" to StringListControl(
            Tr.Sandbox.Form.tags()
        ),
        "radio_test" to RadioGroupControl(
            label = "Test Radio (Poziom)",
            options = listOf(
                SelectionOption("opcja1", "Opcja 1"),
                SelectionOption("opcja2", "Opcja 2"),
                SelectionOption("opcja3", "Opcja 3")
            ),
            orientation = ControlOrientation.HORIZONTAL
        ),
        "checkbox_test" to CheckboxGroupControl(
            label = "Test Checkbox (Pion)",
            options = listOf(
                SelectionOption("opcjaA", "Opcja A"),
                SelectionOption("opcjaB", "Opcja B"),
                SelectionOption("opcjaC", "Opcja C")
            ),
            orientation = ControlOrientation.VERTICAL
        ),
        "multiline_test" to MultilineStringControl(
            label = "Test Multiline (Rozszerzalne)",
            required = false
        ),
        "switch_test" to SwitchControl(
            label = "Test Switch (Przełącznik)"
        ),
        "file_picker_path" to FilePickerControl<String>(
            label = "Wybór pliku (Ścieżka)",
            mode = FilePickerMode.PATH_STRING
        ),
        "file_picker_bytes" to FilePickerControl<ByteArray>(
            label = "Wybór pliku (Zawartość)",
            mode = FilePickerMode.CONTENT_BYTES
        ),
        "basic_info" to SectionControl(
            controls = listOf("name", "quantity", "active", "priority", "start_date", "tags", "radio_test", "checkbox_test", "multiline_test", "switch_test", "file_picker_path", "file_picker_bytes"),
            collapsible = true,
            initiallyExpanded = true,
            columns = 2,
            label = Tr.Sandbox.Form.basicInfo()
        ),
        "elements" to RepeatableControl(
            rowControls = mapOf(
                "element_name" to StringControl(
                    Tr.Sandbox.Form.elementName(),
                    required = true
                ),
                "element_value" to IntegerControl(
                    Tr.Sandbox.Form.elementValue(),
                    validationOptions = IntegerValidation(min = 0)
                )
            ),
            rowOrder = listOf("element_name", "element_value"),
            label = Tr.Sandbox.Form.elements(),
            validationOptions = RepeatableValidation(
                minItems = 0,
                maxItems = 10,
                uniqueFields = listOf("element_name")
            )
        ),
        "nested_repeatable" to RepeatableControl(
            rowControls = mapOf(
                "inner_repeatable" to RepeatableControl(
                    rowControls = mapOf(
                        "inner_string" to StringControl(
                            Tr.Sandbox.Form.innerString(),
                            dependencies = mapOf(
                                "visible" to ControlDependency(
                                    controlPath = "../inner_boolean",
                                    value = true,
                                    dependencyType = DependencyType.Visible,
                                    comparisonType = ComparisonType.Equals
                                )
                            )
                        )
                    ),
                    rowOrder = listOf("inner_string"),
                    label = Tr.Sandbox.Form.innerRepeatable()
                ),
                "inner_boolean" to CheckboxControl(
                    Tr.Sandbox.Form.innerBoolean()
                )
            ),
            rowOrder = listOf("inner_repeatable", "inner_boolean"),
            label = Tr.Sandbox.Form.nestedRepeatable()
        ),
        "save_button" to ButtonControl(
            text = Tr.Action.save(),
            actions = listOf(
                ControlAction {
                    if (trigger.triggerAction("save", true)) AppRouter.goBack()
                }
            ),
            buttonType = ButtonType.Filled
        ),
        "cancel_button" to ButtonControl(
            text = Tr.Action.cancel(),
            actions = listOf(
                ControlAction {
                    AppRouter.goBack()
                }
            ),
            buttonType = ButtonType.Outlined
        ),
        "clear_elements_button" to ButtonControl(
            text = "Wyczyść wartości elementów",
            actions = listOf(
                ControlAction {
                    updateControls<Int>("elements[*]/element_value", null)
                }
            ),
            buttonType = ButtonType.Text
        )
    )

    override fun defineContentOrder(): List<String> = listOf(
        "basic_info",
        "elements",
        "nested_repeatable"
    )

    override fun defineActionBarOrder(): List<String> = listOf(
        "clear_elements_button",
        "cancel_button",
        "save_button"
    )
}
