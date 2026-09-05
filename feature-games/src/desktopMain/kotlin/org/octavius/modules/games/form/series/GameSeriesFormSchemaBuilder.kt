package org.octavius.modules.games.form.series

import org.octavius.form.component.FormSchemaBuilder
import org.octavius.form.control.base.Control
import org.octavius.form.control.base.ControlAction
import org.octavius.form.control.type.button.ButtonControl
import org.octavius.form.control.type.button.ButtonType
import org.octavius.form.control.type.container.SectionControl
import org.octavius.form.control.type.number.IntegerControl
import org.octavius.form.control.type.primitive.StringControl
import org.octavius.localization.Tr
import org.octavius.navigation.AppRouter

class GameSeriesFormSchemaBuilder : FormSchemaBuilder() {
    override fun defineContentOrder(): List<String> = listOf("basic_info")
    override fun defineActionBarOrder(): List<String> = listOf("cancel_button", "save_button")

    override fun defineControls(): Map<String, Control<*>> =
        mapOf(
            "id" to IntegerControl(null),
            "name" to StringControl(
                Tr.Games.Series.name(),
                required = true
            ),
            "basic_info" to SectionControl(
                controls = listOf("name"),
                collapsible = false,
                initiallyExpanded = true,
                columns = 1,
                label = Tr.Games.Series.basicInfo()
            ),
            // Przyciski
            "save_button" to ButtonControl(
                text = Tr.Action.save(), actions = listOf(
                    ControlAction {
                        if (trigger.triggerAction("save", true)) AppRouter.goBack()
                    }
                ), buttonType = ButtonType.Filled
            ),
            "cancel_button" to ButtonControl(
                text = Tr.Action.cancel(), buttonType = ButtonType.Outlined, actions = listOf(
                    ControlAction {
                        AppRouter.goBack()
                    }
                )
            )
        )
}