package org.octavius.modules.games.form.series.ui

import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.modules.games.form.series.GameSeriesFormDataManager
import org.octavius.modules.games.form.series.GameSeriesFormSchemaBuilder
import org.octavius.modules.games.form.series.GameSeriesFormValidator
import org.octavius.modules.games.localization.GamesTr
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class GameSeriesFormScreen {

    companion object {
        fun create(
            entityId: Int? = null
        ): Screen {
            val title =
                if (entityId == null) GamesTr.Form.newSeries() else GamesTr.Form.editSeries()

            val formHandler = FormHandler(
                formSchemaBuilder = GameSeriesFormSchemaBuilder(),
                formDataManager = GameSeriesFormDataManager(),
                formValidator = GameSeriesFormValidator(),
                payload = mapOf("id" to entityId)
            )
            return ComponentScreen(title) { FormView(formHandler) }
        }
    }
}
