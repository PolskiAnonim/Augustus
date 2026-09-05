package org.octavius.feature.books.form.author.ui

import org.octavius.feature.books.form.author.BookAuthorDataManager
import org.octavius.feature.books.form.author.BookAuthorSchemaBuilder
import org.octavius.feature.books.form.author.BookAuthorValidator
import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.localization.Tr
import org.octavius.navigation.AppRouter
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class BookAuthorFormScreen {
    companion object {
        fun create(
            entityId: Int? = null
        ): Screen {
            val title =
                if (entityId == null) Tr.Books.Authors.Form.newAuthor() else Tr.Books.Authors.Form.editAuthor()

            val formHandler = FormHandler(
                formSchemaBuilder = BookAuthorSchemaBuilder(),
                formDataManager = BookAuthorDataManager(),
                onClose = { AppRouter.goBack() },
                formValidator = BookAuthorValidator(),
                payload = mapOf("id" to entityId)
            )

            return ComponentScreen(title) { FormView(formHandler) }
        }
    }
}
