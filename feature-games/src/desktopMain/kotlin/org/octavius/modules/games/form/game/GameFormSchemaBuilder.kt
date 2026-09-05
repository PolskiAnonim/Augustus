package org.octavius.modules.games.form.game

import kotlinx.coroutines.launch
import org.octavius.dialog.DialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.domain.game.GameStatus
import org.octavius.form.component.FormSchemaBuilder
import org.octavius.form.control.base.*
import org.octavius.form.control.type.button.ButtonControl
import org.octavius.form.control.type.button.ButtonType
import org.octavius.form.control.type.container.SectionControl
import org.octavius.form.control.type.number.DoubleControl
import org.octavius.form.control.type.number.IntegerControl
import org.octavius.form.control.type.primitive.CheckboxControl
import org.octavius.form.control.type.primitive.StringControl
import org.octavius.form.control.type.repeatable.RepeatableControl
import org.octavius.form.control.type.selection.DatabaseControl
import org.octavius.form.control.type.selection.EnumControl
import org.octavius.localization.Tr
import org.octavius.navigation.AppRouter

class GameFormSchemaBuilder : FormSchemaBuilder() {

    override fun defineControls(): Map<String, Control<*>> = mapOf(
        "id" to IntegerControl(null),
        // Podstawowe dane
        "visible_characters_section" to CheckboxControl(
            Tr.Games.Form.visibleCharacterSection(),
            required = true
        ),
        "play_time_exists" to CheckboxControl(null),
        "ratings_exists" to CheckboxControl(null),
        "characters_exists" to CheckboxControl(null),
        // Sekcja podstawowych informacji
        "name" to StringControl(
            Tr.Games.General.gameName(),
            required = true
        ),
        "series" to DatabaseControl(
            label = Tr.Games.Form.series(),
            relatedTable = "series",
            displayColumn = "name"
        ),
        "status" to EnumControl(
            Tr.Games.Form.status(),
            GameStatus::class,
            required = true
        ),
        "basic_info" to SectionControl(
            controls = listOf("name", "series", "status", "visible_characters_section"),
            collapsible = false,
            initiallyExpanded = true,
            columns = 1,
            label = Tr.Games.Form.basicInfo()
        ),
        // Sekcja czasu gry
        "play_time_hours" to DoubleControl(
            Tr.Games.Form.playTimeHours(),
            validationOptions = DoubleValidation(min = 0.0, decimalPlaces = 2)
        ),
        "completion_count" to IntegerControl(
            Tr.Games.Form.playCount(),
            required = true, // Automatycznie pomijana walidacja jak niewidoczna kontrolka
            dependencies = mapOf(
                "visible" to ControlDependency(
                    controlPath = "status",
                    value = listOf(GameStatus.Playing, GameStatus.Played),
                    dependencyType = DependencyType.Visible,
                    comparisonType = ComparisonType.OneOf
                )
            ),
            validationOptions = IntegerValidation(min = 0)
        ),
        "play_time_section" to SectionControl(
            controls = listOf("play_time_hours", "completion_count"),
            collapsible = false,
            initiallyExpanded = true,
            columns = 2,
            label = Tr.Games.Form.playTime(),
            dependencies = mapOf(
                "visible" to ControlDependency(
                    controlPath = "status",
                    value = listOf(
                        GameStatus.Playing,
                        GameStatus.Played,
                        GameStatus.WithoutTheEnd
                    ),
                    dependencyType = DependencyType.Visible,
                    comparisonType = ComparisonType.OneOf
                )
            )
        ),
        // Sekcja ocen
        "story_rating" to IntegerControl(
            Tr.Games.Form.storyRating(),
            validationOptions = IntegerValidation(min = 0, max = 10)
        ),
        "gameplay_rating" to IntegerControl(
            Tr.Games.Form.gameplayRating(),
            required = true,
            validationOptions = IntegerValidation(min = 0, max = 10)
        ),
        "atmosphere_rating" to IntegerControl(
            Tr.Games.Form.atmosphereRating(),
            validationOptions = IntegerValidation(min = 0, max = 10)
        ),
        "ratings_section" to SectionControl(
            controls = listOf("story_rating", "gameplay_rating", "atmosphere_rating"),
            collapsible = true,
            initiallyExpanded = true,
            columns = 3,
            label = Tr.Games.Form.ratings(),
            dependencies = mapOf(
                "visible" to ControlDependency(
                    controlPath = "status",
                    value = listOf(GameStatus.Played, GameStatus.WithoutTheEnd),
                    dependencyType = DependencyType.Visible,
                    comparisonType = ComparisonType.OneOf
                )
            )
        ),
        // Sekcja postaci
        "has_distinctive_character" to CheckboxControl(
            Tr.Games.Form.distinctiveCharacters(),
            required = true
        ),
        "has_distinctive_protagonist" to CheckboxControl(
            Tr.Games.Form.distinctiveProtagonist(),
            required = true
        ),
        "has_distinctive_antagonist" to CheckboxControl(
            Tr.Games.Form.distinctiveAntagonist(),
            required = true
        ),
        "characters_section" to SectionControl(
            controls = listOf("has_distinctive_character", "has_distinctive_protagonist", "has_distinctive_antagonist"),
            collapsible = true,
            initiallyExpanded = true,
            columns = 1,
            label = Tr.Games.Form.characters(),
            dependencies = mapOf(
                "visible" to ControlDependency(
                    controlPath = "visible_characters_section",
                    value = true,
                    dependencyType = DependencyType.Visible,
                    comparisonType = ComparisonType.Equals
                )
            )
        ),
        // Sekcja kategorii
        "categories" to RepeatableControl(
            rowControls = mapOf(
                "category" to DatabaseControl(
                    label = Tr.Games.Form.category(1),
                    relatedTable = "games.categories",
                    displayColumn = "name",
                    required = true
                )
            ),
            rowOrder = listOf("category"),
            label = Tr.Games.Form.category(2),
            validationOptions = RepeatableValidation(
                minItems = 0,
                maxItems = 10,
                uniqueFields = listOf("category")
            )
        ),
        // Przyciski
        "save_button" to ButtonControl(
            text = Tr.Action.save(),
            actions = listOf(
                ControlAction {
                    if (trigger.triggerAction("save", true)) AppRouter.goBack()
                }
            ),
            buttonType = ButtonType.Filled
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
            actions = listOf(
                ControlAction {
                    AppRouter.goBack()
                }
            ),
            buttonType = ButtonType.Outlined
        )
    )

    override fun defineContentOrder(): List<String> = listOf(
        "basic_info",
        "play_time_section",
        "ratings_section",
        "characters_section",
        "categories"
    )

    override fun defineActionBarOrder(): List<String> = listOf(
        "cancel_button",
        "save_button",
        "delete_button"
    )

}