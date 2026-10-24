package org.octavius.app.settings.form.database

import org.octavius.app.localization.AppTr
import io.github.octaviusframework.form.component.FormSchemaBuilder
import io.github.octaviusframework.form.control.base.Control
import io.github.octaviusframework.form.control.base.ControlAction
import io.github.octaviusframework.form.control.type.button.ButtonControl
import io.github.octaviusframework.form.control.type.button.ButtonType
import io.github.octaviusframework.form.control.type.primitive.StringControl

class DatabaseSettingsSchemaBuilder : FormSchemaBuilder() {
    override fun defineControls(): Map<String, Control<*>> = mapOf(
        "url" to StringControl(
            label = AppTr.Settings.Database.url(),
            required = true
        ),
        "username" to StringControl(
            label = AppTr.Settings.Database.username(),
            required = true
        ),
        "password" to StringControl(
            label = AppTr.Settings.Database.password(),
            required = true
        ),
        "save" to ButtonControl(
            text = AppTr.Settings.Database.save(),
            buttonType = ButtonType.Filled,
            actions = listOf(ControlAction {
                trigger.triggerAction("save", validates = true)
            })
        )
    )

    override fun defineContentOrder(): List<String> = listOf("url", "username", "password")

    override fun defineActionBarOrder(): List<String> = listOf("save")
}
