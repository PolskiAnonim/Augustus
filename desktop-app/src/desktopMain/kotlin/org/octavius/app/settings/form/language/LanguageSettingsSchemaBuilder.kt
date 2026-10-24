package org.octavius.app.settings.form.language

import org.octavius.app.localization.AppTr
import io.github.octaviusframework.form.component.FormSchemaBuilder
import io.github.octaviusframework.form.control.base.Control
import io.github.octaviusframework.form.control.base.ControlAction
import io.github.octaviusframework.form.control.type.button.ButtonControl
import io.github.octaviusframework.form.control.type.button.ButtonType
import io.github.octaviusframework.form.control.type.selection.EnumControl
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
