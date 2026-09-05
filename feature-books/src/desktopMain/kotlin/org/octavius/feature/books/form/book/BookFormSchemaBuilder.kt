package org.octavius.feature.books.form.book

import kotlinx.coroutines.launch
import org.octavius.dialog.DialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.feature.books.domain.ReadingStatus
import org.octavius.form.component.FormSchemaBuilder
import org.octavius.form.control.base.*
import org.octavius.form.control.type.button.ButtonControl
import org.octavius.form.control.type.button.ButtonType
import org.octavius.form.control.type.container.SectionControl
import org.octavius.form.control.type.number.IntegerControl
import org.octavius.form.control.type.primitive.StringControl
import org.octavius.form.control.type.repeatable.RepeatableControl
import org.octavius.form.control.type.selection.DatabaseControl
import org.octavius.form.control.type.selection.EnumControl
import org.octavius.localization.Tr
import org.octavius.navigation.AppRouter

class BookFormSchemaBuilder : FormSchemaBuilder() {

    override fun defineContentOrder(): List<String> = listOf("basic_info", "authors")
    override fun defineActionBarOrder(): List<String> = listOf("cancel_button", "save_button", "delete_button")

    override fun defineControls(): Map<String, Control<*>> = mapOf(
        "id" to IntegerControl(null),

        // Podstawowe informacje
        "title_pl" to StringControl(
            Tr.Books.Form.titlePl(),
            required = true
        ),
        "title_eng" to StringControl(
            Tr.Books.Form.titleEng(),
            required = false
        ),
        "status" to EnumControl(
            Tr.Books.Form.status(),
            ReadingStatus::class,
            required = true
        ),
        "basic_info" to SectionControl(
            controls = listOf("title_pl", "title_eng", "status"),
            collapsible = false,
            initiallyExpanded = true,
            columns = 1,
            label = Tr.Books.Form.basicInfo()
        ),

        // Autorzy
        "authors" to RepeatableControl(
            rowControls = mapOf(
                "author_id" to DatabaseControl(
                    label = Tr.Books.Form.author(),
                    relatedTable = "books.authors",
                    displayColumn = "name",
                    required = true
                )
            ),
            rowOrder = listOf("author_id"),
            label = Tr.Books.Form.authors(),
            validationOptions = RepeatableValidation(
                minItems = 0,
                maxItems = 10,
                uniqueFields = listOf("author_id")
            )
        ),

        // Przyciski
        "save_button" to ButtonControl(
            text = Tr.Action.save(),
            buttonType = ButtonType.Filled,
            actions = listOf(
                ControlAction {
                    if (trigger.triggerAction("save", true)) AppRouter.goBack()
                }
            )
        ),
        "delete_button" to ButtonControl(
            text = Tr.Action.remove(),
            buttonType = ButtonType.Filled,
            dependencies = mapOf(
                "visible" to ControlDependency(
                    controlPath = "id",
                    value = null,
                    dependencyType = DependencyType.Visible,
                    comparisonType = ComparisonType.NotEquals
                )
            ),
            actions = listOf(
                ControlAction {
                    GlobalDialogManager.show(
                        DialogConfig(
                            title = Tr.Action.confirm(),
                            text = Tr.Books.Form.confirmDelete(),
                            onDismiss = { GlobalDialogManager.dismiss() },
                            confirmButtonText = Tr.Action.confirm(),
                            onConfirm = {
                                coroutineScope.launch {
                                    if (trigger.triggerAction("delete", false)) AppRouter.goBack()
                                    GlobalDialogManager.dismiss()
                                }
                            }
                        )
                    )
                }
            )
        ),
        "cancel_button" to ButtonControl(
            text = Tr.Action.cancel(),
            buttonType = ButtonType.Outlined,
            actions = listOf(
                ControlAction {
                    AppRouter.goBack()
                }
            )
        )
    )
}