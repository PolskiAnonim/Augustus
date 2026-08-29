# Form Engine

<div align="center">

**A declarative, data-driven form framework for Compose Multiplatform**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![KDoc](https://img.shields.io/badge/KDoc-Documentation-blue)](https://polskianonim.github.io/Augustus/form-engine/)

</div>

---

## Overview

Form Engine turns a schema — a map of named controls plus a display order — into a full CRUD form. It renders
the controls, tracks their state, loads and saves data through a small SQL DSL, runs three levels of validation,
and lets one control's value drive another's visibility, requirement, or content.

A control never talks to the database directly. `FormDataManager` loads initial values with a declarative DSL,
and every form action receives a `FormResultData` — each control's current *and* initial value — so `save` /
`delete` / etc. can be written as a couple of `OctaviusClient` calls.

## Features

- **Declarative schema** — controls, content order and action-bar order in one builder class
- **Rich control types** — primitives, dropdowns, selection groups, dates/intervals, files, sections, repeatable rows
- **Dependencies** — show/hide or require a control based on another control's value
- **Cross-control actions** — a control action can read or write any other control by relative (`./`, `../`) or
  wildcard (`*`) path
- **Three-level validation** — per-field, business rules and per-action, each with its own error surface
- **Diffed repeatable rows** — added/modified/deleted rows are separated for you, not just handed back as a flat list
- **Two-way binding** — `ControlState` is Compose state, so edits recompose automatically

## Control Types

| Category | Package | Controls |
|---|---|---|
| **Primitive** | `control.type.primitive` | `StringControl`, `MultilineStringControl`, `CheckboxControl`, `SwitchControl`, `FilePickerControl` |
| **Number** | `control.type.number` | `IntegerControl`, `DoubleControl`, `BigDecimalControl` |
| **DateTime** | `control.type.datetime` | `DateTimeControl`, `IntervalControl` |
| **Selection** | `control.type.selection` | `EnumControl`, `DatabaseControl`, `RadioGroupControl`, `CheckboxGroupControl` |
| **Collection** | `control.type.collection` | `StringListControl` |
| **Container** | `control.type.container` | `SectionControl` |
| **Repeatable** | `control.type.repeatable` | `RepeatableControl` |
| **Action** | `control.type.button` | `ButtonControl` |

`EnumControl` and `DatabaseControl` both build on `AsyncPaginatedDropdownControl` — a search box with paginated
results. `EnumControl` reads its options from a `KClass<T>` whose `T` implements `EnumWithFormatter`;
`DatabaseControl` queries `relatedTable` / `displayColumn` directly, caching the selected row's display text.
`DateTimeControl<T>` is generic over a `DateTimeAdapter<T>`, covering `LocalDate`, `LocalDateTime`, `LocalTime`
and `Instant` from one implementation.

## Quick Start

### 1. Define a Schema

```kotlin
class BookFormSchemaBuilder : FormSchemaBuilder() {

    override fun defineControls(): Map<String, Control<*>> = mapOf(
        "id" to IntegerControl(label = null), // hidden — carries the loaded id through to the save action
        "title" to StringControl(label = "Title", required = true),
        "author" to StringControl(label = "Author"),
        "year" to IntegerControl(label = "Year"),
        "status" to EnumControl(label = "Status", enumClass = ReadingStatus::class),

        "save_button" to ButtonControl(
            text = "Save",
            buttonType = ButtonType.Filled,
            actions = listOf(ControlAction { trigger.triggerAction("save", validates = true) })
        ),
        "cancel_button" to ButtonControl(
            text = "Cancel",
            buttonType = ButtonType.Outlined,
            actions = listOf(ControlAction { trigger.triggerAction("cancel", validates = false) })
        )
    )

    // "id" is tracked but never rendered — it's simply absent from contentOrder
    override fun defineContentOrder() = listOf("title", "author", "year", "status")

    override fun defineActionBarOrder() = listOf("cancel_button", "save_button")
}
```

### 2. Implement Data Manager

```kotlin
class BookFormDataManager : FormDataManager() {

    override fun initData(payload: Map<String, Any?>): Map<String, Any?> {
        return loadData(payload["id"]) {
            from("books", "b")
            map("title")
            map("author")
            map("year")
            map("status")
        }
    }

    override fun definedFormActions(): Map<String, (FormResultData) -> FormActionResult> = mapOf(
        "save" to { formData -> processSave(formData) },
        "cancel" to { _ -> FormActionResult.CloseScreen }
    )

    private fun processSave(formResultData: FormResultData): FormActionResult {
        val loadedId = formResultData.getInitial("id")
        val params = mapOf(
            "title" to formResultData.getCurrent("title"),
            "author" to formResultData.getCurrent("author"),
            "year" to formResultData.getCurrent("year"),
            "status" to formResultData.getCurrent("status")
        )

        val result = if (loadedId != null) {
            db.update("books").setValues(params).where("id = @id")
                .asResult().update(params + ("id" to loadedId))
        } else {
            db.insertInto("books").values(params).asResult().update(params)
        }

        return when (result) {
            is DataResult.Success<*> -> FormActionResult.CloseScreen
            is DataResult.Failure -> FormActionResult.Failure
        }
    }
}
```

`getCurrent` / `getInitial` (and their typed `getCurrentAs<T>` / `getInitialAs<T>` variants) read
`ControlResultData` out of `FormResultData` without the `!!.currentValue as T` boilerplate.

### 3. Create the Screen

`FormScreen` is a `Screen` implementation, not a composable — build it behind a factory and hand it to the router:

```kotlin
class BookFormScreen {
    companion object {
        fun create(bookId: Int? = null): FormScreen {
            val formHandler = FormHandler(
                formSchemaBuilder = BookFormSchemaBuilder(),
                formDataManager = BookFormDataManager(),
                formValidator = BookFormValidator(),
                payload = mapOf("id" to bookId)
            )
            return FormScreen(title = "Books", formHandler)
        }
    }
}

AppRouter.navigateTo(BookFormScreen.create(bookId))
```

`FormHandler` loads the initial data asynchronously and owns `FormState`, `ErrorManager` and the wiring between
controls; `FormScreen` shows a spinner while `isLoading` is true and blocks input behind an overlay while an
action is running (`actionTriggered`).

## Data Loading DSL

The `loadData` function provides a DSL for loading form data:

```kotlin
override fun initData(payload: Map<String, Any?>): Map<String, Any?> {
    return loadData(payload["id"]) {
        // Main table
        from("books", "b")
        map("title", "title") // control name -> db column (default is control name in snake_case)
        map("author")
        map("publication_year", "pub_year")

        // One-to-one relation
        mapOneToOne {
            from("book_details", "bd")
            on("bd.book_id = b.id")
            map("isbn")
            map("page_count") // matches db column 'page_count' automatically
            existenceFlag("has_details", "bd.id")  // boolean flag if relation exists
        }

        // One-to-many relation (for RepeatableControl)
        mapRelatedList("authors") {
            from("book_authors", "ba")
            linkedBy("ba.book_id") // defaults to matching against the main row's "@id"
            map("name")
            map("role")
        }
    }
}
```

Everything compiles down to a single query: `mapOneToOne` becomes a `LEFT JOIN`, `mapRelatedList` becomes a
correlated `ARRAY(SELECT ROW(...))::record[]` subquery — an anonymous record read back as a
`Map<String, Any?>` per row, keys and values alternating, which is what a repeatable control wants and what
no declared type covers. `id` is assumed as the main table's primary key; override it with `idColumn("uuid")`
if it's named differently. If `id` (the value passed to `loadData`) is `null`, loading is skipped and an empty
map is returned — the natural path for a "new record" form.

## Dependencies

Control visibility and requirements based on other control values:

```kotlin
// Show "publisher" only when status is "Published"
"publisher" to StringControl(
    label = "Publisher",
    dependencies = mapOf(
        "visibility" to ControlDependency(
            controlPath = "status",
            value = BookStatus.Published,
            dependencyType = DependencyType.Visible,
            comparisonType = ComparisonType.Equals
        )
    )
)

// Make "isbn" required only when "has_isbn" is checked
"isbn" to StringControl(
    label = "ISBN",
    dependencies = mapOf(
        "required" to ControlDependency(
            controlPath = "has_isbn",
            value = true,
            dependencyType = DependencyType.Required,
            comparisonType = ComparisonType.Equals
        )
    )
)
```

`comparisonType` also accepts `ComparisonType.OneOf`, matching against a `Collection` passed as `value`, and
`ComparisonType.NotEquals`. A hidden control's `currentValue` is forced to `null` before it reaches
`FormResultData`, so conditionally-shown fields never leak stale data into a save.

## Control Actions and Path Resolution

Every control carries an optional list of `ControlAction<T>`, run on value change — for `ButtonControl` that
means on click — or, with `executeOnInit = true`, once when the control is first composed (on form load, or when
a `RepeatableControl` adds a row). The action lambda runs with `ActionContext<T>` as receiver, giving access to
`sourceValue`, the triggering control's `ControlContext`, and helpers to reach across the form:

```kotlin
"country" to EnumControl(
    label = "Country",
    enumClass = Country::class,
    actions = listOf(ControlAction { updateControl("../city", null) }) // clear a sibling when country changes
)
```

- `updateControl(path, value)` / `updateControls(path, value)` — write one or many control values by path
- `updateLabel(path, label)` — override a control's rendered label

Paths are resolved relative to the acting control's container: `./name` addresses a sibling, and a bare `name`
is treated as absolute for backward compatibility. Only `RepeatableControl` actually nests state paths — each
row appends `[rowId]` — so `../name` is what steps out, from a field inside a row back to the level containing
the `RepeatableControl` itself. `SectionControl` doesn't add a path level: its children share the section's own
`statePath`, so paths resolve straight through it as if the section weren't there. `updateControls` additionally
accepts a `*` wildcard segment (e.g. `"rows/*/total"`) and updates every matching control in `FormState`.

## Repeatable Sections

Dynamic rows for collections like order items or authors:

```kotlin
"authors" to RepeatableControl(
    label = "Authors",
    rowControls = mapOf(
        "name" to StringControl(label = "Name", required = true),
        "role" to EnumControl(label = "Role", enumClass = AuthorRole::class)
    ),
    rowOrder = listOf("name", "role"),
    validationOptions = RepeatableValidation(minItems = 1, maxItems = 10, uniqueFields = listOf("name"))
)
```

`uniqueFields` rejects rows whose combination of listed fields duplicates another row. A `RepeatableControl`
doesn't just hand back a flat list — its `currentValue` is a `RepeatableResultValue` with the rows already sorted
into `addedRows`, `modifiedRows`, `deletedRows` and `allCurrentRows`, so a `save` action can insert/update/delete
each bucket directly instead of diffing the list itself.

## Validation

Validation runs in three stages, in order, each gating the next:

1. **Field validation** — built into each control's `ControlValidator` (requiredness, format, dependency
   requirements). Failures go to `ErrorManager` as field or format errors and stop the action.
2. **Business rules** — `FormValidator.validateBusinessRules(FormResultData): Boolean`, overridden per form for
   checks that span multiple fields.
3. **Action-specific validation** — `FormValidator.defineActionValidations()`, keyed by the same `actionKey`
   passed to `triggerAction`. Runs *after* the first two and always runs for that action, regardless of the
   button's `validates` flag — the natural place for a uniqueness check that only matters on `save`, not `cancel`.

```kotlin
class GameCategoryValidator : FormValidator() {

    override fun defineActionValidations(): Map<String, (FormResultData) -> Boolean> = mapOf(
        "save" to { result -> checkIfUnique(result) }
    )

    private fun checkIfUnique(result: FormResultData): Boolean {
        val name = result.getCurrentAs<String>("name")
        val id = result.getCurrent("id")
        val params = mutableMapOf<String, Any?>("name" to name)

        val builder = db.select("COUNT(*)").from("games.categories")
        if (id != null) {
            builder.where("id != @id AND name = @name")
            params["id"] = id
        } else {
            builder.where("name = @name")
        }

        return when (val count = builder.asResult().fetchField<Long>(params)) {
            is DataResult.Success -> if (count.value > 0) {
                errorManager.setFieldErrors("name", listOf("Category already exists"))
                false
            } else true
            is DataResult.Failure -> false
        }
    }
}
```

`FormValidator` is a `KoinComponent` with `db` and `errorManager` available directly, so business-rule
and action validators can query the database and attach errors to specific fields in one place.

## Architecture

```
form-engine/
├── component/
│   ├── FormSchema.kt          # FormSchema + FormSchemaBuilder (template method)
│   ├── FormState.kt           # Reactive per-control state, keyed by hierarchical path
│   ├── FormHandler.kt         # Orchestrates load → validate → action → navigate
│   ├── FormScreen.kt          # Screen implementation rendering content + action bar
│   ├── FormDataManager.kt     # initData() / definedFormActions() contract
│   ├── FormLoader.kt          # loadData DSL: from/map/mapOneToOne/mapRelatedList
│   ├── FormValidator.kt       # Field / business-rule / action validation contract
│   ├── ErrorManager.kt        # Global, field and format error state
│   └── PathResolver.kt        # Relative ("./", "../") and wildcard ("*") path resolution
│
├── control/
│   ├── base/
│   │   ├── Control.kt         # Base control class: lifecycle, actions, rendering
│   │   ├── ControlData.kt     # ControlState, ControlContext, FormResultData helpers
│   │   ├── Dependencies.kt    # ControlDependency, DependencyType, ComparisonType
│   │   ├── ControlAction.kt   # ControlAction, ActionContext (updateControl/updateLabel)
│   │   └── ValidationOptions.kt
│   ├── type/                  # primitive/, number/, datetime/, selection/, collection/,
│   │                           # container/, repeatable/, button/ — one package per category
│   ├── validator/              # One ControlValidator implementation per control type
│   └── layout/                 # Section and repeatable-row rendering, shared layout helpers
│
└── resources/i18n/            # Engine-owned translation strings
```

The module targets the desktop JVM only and depends on `ui-core` for navigation and dialogs, and on
[Octavius for PostgreSQL](https://github.com/Octavius-Framework/octavius-postgresql) for `OctaviusClient`.
`FormValidator` and `FormDataManager` obtain `OctaviusClient` through Koin.

Terminals throw by default there; the engine asks for the result style with `.asResult()`, so a failure the
database reports arrives as a `DataResult.Failure` and reaches the user through `GlobalDialogManager` rather
than unwinding the form.
