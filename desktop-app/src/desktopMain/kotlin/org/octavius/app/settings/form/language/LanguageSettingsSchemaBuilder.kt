package org.octavius.app.settings.form.language

import org.octavius.app.localization.AppTr
import org.octavius.form.component.FormSchemaBuilder
import org.octavius.form.control.base.Control
import org.octavius.form.control.base.ControlAction
import org.octavius.form.control.type.button.ButtonControl
import org.octavius.form.control.type.button.ButtonType
import org.octavius.form.control.type.selection.EnumControl
import org.octavius.app.settings.domain.AppLanguage

class LanguageSettingsSchemaBuilder : FormSchemaBuilder() {
    override fun defineControls(): Map<String, Control<*>> = mapOf(
        "language" to EnumControl(
            label = AppTr.Settings.Language.select(),
            enumClass = AppLanguage::class,
            required = true
        ),
        "save" to ButtonControl(
            text = AppTr.Settings.Language.save(),
            buttonType = ButtonType.Filled,
            actions = listOf(ControlAction {
                trigger.triggerAction("save", validates = true)
            })
        )
    )

    override fun defineContentOrder(): List<String> = listOf("language")

    override fun defineActionBarOrder(): List<String> = listOf("save")
}
