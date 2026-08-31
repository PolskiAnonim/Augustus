package org.octavius.app.settings.form.theme

import org.octavius.app.settings.domain.AppThemeMode
import org.octavius.form.component.FormSchemaBuilder
import org.octavius.form.control.base.Control
import org.octavius.form.control.base.ControlAction
import org.octavius.form.control.type.button.ButtonControl
import org.octavius.form.control.type.button.ButtonType
import org.octavius.form.control.type.selection.EnumControl
import org.octavius.localization.Tr

class ThemeSettingsSchemaBuilder : FormSchemaBuilder() {
    override fun defineControls(): Map<String, Control<*>> = mapOf(
        "theme" to EnumControl(
            label = Tr.Settings.Theme.select(),
            enumClass = AppThemeMode::class,
            required = true
        ),
        "save" to ButtonControl(
            text = Tr.Settings.Theme.save(),
            buttonType = ButtonType.Filled,
            actions = listOf(ControlAction {
                trigger.triggerAction("save", validates = true)
            })
        )
    )

    override fun defineContentOrder(): List<String> = listOf("theme")

    override fun defineActionBarOrder(): List<String> = listOf("save")
}
