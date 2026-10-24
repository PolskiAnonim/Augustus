package org.octavius.modules.games.form.category

import io.github.octaviusframework.form.component.FormSchemaBuilder
import io.github.octaviusframework.form.control.base.Control
import io.github.octaviusframework.form.control.base.ControlAction
import io.github.octaviusframework.form.control.type.button.ButtonControl
import io.github.octaviusframework.form.control.type.button.ButtonType
import io.github.octaviusframework.form.control.type.number.IntegerControl
import io.github.octaviusframework.form.control.type.primitive.StringControl
import org.octavius.localization.Tr
import org.octavius.modules.games.localization.GamesTr
import org.octavius.navigation.AppRouter

class GameCategorySchemaBuilder : FormSchemaBuilder() {

    override fun defineActionBarOrder(): List<String> = listOf("cancel_button", "save_button")
    override fun defineContentOrder(): List<String> = listOf("name")

    override fun defineControls(): Map<String, Control<*>> =
        mapOf(
            "id" to IntegerControl(
                null
            ),
            "name" to StringControl(
                GamesTr.Categories.name(),
                required = true
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