package org.octavius.modules.games.form.category.ui

import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.localization.Tr
import org.octavius.modules.games.form.category.GameCategoryDataManager
import org.octavius.modules.games.form.category.GameCategorySchemaBuilder
import org.octavius.modules.games.form.category.GameCategoryValidator
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class GameCategoryFormScreen {

    companion object {
        fun create(
            entityId: Int? = null
        ): Screen {
            val title =
                if (entityId == null) Tr.Games.Form.newCategory() else Tr.Games.Form.editCategory()

            val formHandler = FormHandler(
                formSchemaBuilder = GameCategorySchemaBuilder(),
                formDataManager = GameCategoryDataManager(),
                formValidator = GameCategoryValidator(),
                payload = mapOf("id" to entityId)
            )
            return ComponentScreen(title) { FormView(formHandler) }
        }
    }
}
