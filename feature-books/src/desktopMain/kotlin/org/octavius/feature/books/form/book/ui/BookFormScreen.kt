package org.octavius.feature.books.form.book.ui

import org.octavius.feature.books.form.book.BookFormDataManager
import org.octavius.feature.books.form.book.BookFormSchemaBuilder
import org.octavius.feature.books.form.book.BookFormValidator
import org.octavius.form.component.FormHandler
import org.octavius.form.component.FormView
import org.octavius.localization.Tr
import org.octavius.navigation.ComponentScreen
import org.octavius.navigation.Screen

class BookFormScreen {
    companion object {
        fun create(
            entityId: Int? = null
        ): Screen {
            val title =
                if (entityId == null) Tr.Books.Form.newBook() else Tr.Books.Form.editBook()

            val formHandler = FormHandler(
                formSchemaBuilder = BookFormSchemaBuilder(),
                formDataManager = BookFormDataManager(),
                formValidator = BookFormValidator(),
                payload = mapOf("id" to entityId)
            )

            return ComponentScreen(title) { FormView(formHandler) }
        }
    }
}
