package org.octavius.navigation

import androidx.compose.runtime.Composable

/**
 * Uniwersalna implementacja [Screen], która opakowuje dowolną zawartość Compose.
 *
 * Istnieje po to, żeby silniki (`form-engine`, `report-engine`) nie musiały znać interfejsu
 * [Screen]. Silnik wystawia zwykły composable (np. `FormView`, `ReportView`), a moduł, który
 * już zna nawigację, dokłada tytuł i wsadza to na stos ekranów:
 *
 * ```kotlin
 * fun create(entityId: Int? = null): Screen {
 *     val title = Tr.Books.Form.editBook()
 *     val formHandler = FormHandler(BookFormSchemaBuilder(), BookFormDataManager())
 *     return ComponentScreen(title) { FormView(formHandler) }
 * }
 * ```
 *
 *
 * @param title Tytuł ekranu pokazywany w górnym pasku nawigacji.
 * @param content Zawartość ekranu.
 */
class ComponentScreen(
    override val title: String,
    private val content: @Composable () -> Unit
) : Screen {
    @Composable
    override fun Content() {
        content()
    }
}
