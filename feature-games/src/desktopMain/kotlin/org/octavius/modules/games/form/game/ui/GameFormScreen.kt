package org.octavius.modules.games.form.game.ui

import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.modules.games.form.game.GameFormDataManager
import org.octavius.modules.games.form.game.GameFormSchemaBuilder
import org.octavius.modules.games.form.game.GameFormValidator
import org.octavius.modules.games.localization.GamesTr
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class GameFormScreen {
    companion object {
        fun create(
            entityId: Int? = null,
            payload: Map<String, Any?> = emptyMap()
        ): Screen {
            val title =
                if (entityId == null) GamesTr.Form.newGame() else GamesTr.Form.editGame()

            val formHandler = FormHandler(
                formSchemaBuilder = GameFormSchemaBuilder(),
                formDataManager = GameFormDataManager(),
                formValidator = GameFormValidator(),
                payload = payload + ("id" to entityId)
            )

            return ComponentScreen(title) { FormView(formHandler) }
        }
    }

}
