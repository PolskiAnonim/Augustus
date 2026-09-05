package org.octavius.modules.asian.form

import kotlinx.coroutines.launch
import org.octavius.dialog.DialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.domain.asian.PublicationLanguage
import org.octavius.domain.asian.PublicationStatus
import org.octavius.domain.asian.PublicationType
import org.octavius.form.component.FormSchemaBuilder
import org.octavius.form.control.base.*
import org.octavius.form.control.type.button.ButtonControl
import org.octavius.form.control.type.button.ButtonType
import org.octavius.form.control.type.collection.StringListControl
import org.octavius.form.control.type.container.SectionControl
import org.octavius.form.control.type.number.IntegerControl
import org.octavius.form.control.type.primitive.CheckboxControl
import org.octavius.form.control.type.repeatable.RepeatableControl
import org.octavius.form.control.type.selection.EnumControl
import org.octavius.localization.Tr
import org.octavius.navigation.AppRouter

class AsianMediaFormSchemaBuilder : FormSchemaBuilder() {

    override fun defineActionBarOrder(): List<String> = listOf("cancel_button", "save_button", "delete_button")
    override fun defineContentOrder(): List<String> = listOf("title_info", "publications")

    override fun defineControls(): Map<String, Control<*>> = mapOf(
        "id" to IntegerControl(
            null
        ),
        "title_info" to SectionControl(
            controls = listOf("titles", "language"),
            collapsible = false,
            initiallyExpanded = true,
            columns = 2,
            label = Tr.AsianMedia.Form.titleInfo()
        ),
        "titles" to StringListControl(
            Tr.AsianMedia.Form.titles(),
            required = true,
            validationOptions = StringListValidation(minItems = 1)
        ),
        "language" to EnumControl(
            Tr.AsianMedia.Form.originalLanguage(),
            PublicationLanguage::class,
            required = true
        ),

        // Sekcja publikacji - używa RepeatableControl
        "publications" to RepeatableControl(
            rowControls = createPublicationControls(),
            rowOrder = listOf(
                "publication_type",
                "status",
                "track_progress",
                "volumes",
                "translated_volumes",
                "chapters",
                "translated_chapters",
                "original_completed"
            ),
            validationOptions = RepeatableValidation(
                uniqueFields = listOf("publication_type"),
                minItems = 1,
                maxItems = 7
            ),
            label = Tr.AsianMedia.Form.publications()
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
                            text = Tr.Dialog.confirmDelete(),
                            onDismiss = { GlobalDialogManager.dismiss() },
                            confirmButtonText = Tr.Dialog.yes(),
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


    private fun createPublicationControls(): Map<String, Control<*>> {
        return mapOf(
            "id" to IntegerControl(null),
            "publication_type" to EnumControl(
                Tr.AsianMedia.Form.publicationType(),
                PublicationType::class,
                required = true,
                actions = listOf(
                    ControlAction(executeOnInit = true) {
                        val isWebtoon = sourceValue == PublicationType.Webtoon
                        if (isWebtoon) {
                            updateLabel("./volumes", Tr.AsianMedia.Form.seasonsCount())
                            updateLabel("./translated_volumes", Tr.AsianMedia.Form.translatedSeasons())
                        } else {
                            updateLabel("./volumes", Tr.AsianMedia.Form.volumeCount())
                            updateLabel("./translated_volumes", Tr.AsianMedia.Form.translatedVolumes())
                        }
                    }
                )
            ),
            "status" to EnumControl(
                Tr.AsianMedia.Form.readingStatus(),
                PublicationStatus::class,
                required = true,
                actions = listOf(
                    ControlAction {
                        val shouldBeChecked = (sourceValue in listOf(
                            PublicationStatus.PlanToRead, PublicationStatus.Reading,
                            PublicationStatus.Completed
                        ))
                        updateControl("./track_progress", shouldBeChecked)
                    }
                )
            ),
            "track_progress" to CheckboxControl(
                Tr.AsianMedia.Form.trackProgress(),
                required = true
            ),
            "volumes" to IntegerControl(
                Tr.AsianMedia.Form.volumeCount(),
                dependencies = visibleWhenTrackProgress()
            ),
            "translated_volumes" to IntegerControl(
                Tr.AsianMedia.Form.translatedVolumes(),
                dependencies = visibleWhenTrackProgress()
            ),
            "chapters" to IntegerControl(
                Tr.AsianMedia.Form.chapterCount(),
                dependencies = visibleWhenTrackProgress()
            ),
            "translated_chapters" to IntegerControl(
                Tr.AsianMedia.Form.translatedChapters(),
                dependencies = visibleWhenTrackProgress()
            ),
            "original_completed" to CheckboxControl(
                Tr.AsianMedia.Form.originalCompleted(),
                required = true,
                dependencies = visibleWhenTrackProgress()
            )
        )
    }

    private fun visibleWhenTrackProgress(): Map<String, ControlDependency<Boolean>> = mapOf(
        "visible" to ControlDependency(
            controlPath = "./track_progress",
            value = true,
            dependencyType = DependencyType.Visible,
            comparisonType = ComparisonType.Equals,
        )
    )

}