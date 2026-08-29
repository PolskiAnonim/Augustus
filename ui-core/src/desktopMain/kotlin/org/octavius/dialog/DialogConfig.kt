package org.octavius.dialog

import androidx.compose.ui.graphics.Color
import io.github.octaviusframework.driver.exception.OctaviusException
import org.octavius.localization.Tr

/**
 * Uniwersalna konfiguracja dialogu.
 *
 * @param title Tytuł dialogu.
 * @param text Treść wiadomości w dialogu.
 * @param confirmButtonText Tekst na przycisku potwierdzającym (jeśli `null`, brak przycisku).
 * @param onConfirm Lambda, która zostanie wykonana po kliknięciu "Potwierdź" (jeśli `confirmButtonText` nie jest `null`).
 * @param dismissButtonText Tekst na przycisku odrzucającym (jeśli `null`, brak przycisku).
 * @param onDismiss Lambda, która zostanie wykonana po kliknięciu "Anuluj" lub zamknięciu dialogu.
 * @param titleColor Kolor tytułu (opcjonalny).
 * @param textColor Kolor tekstu (opcjonalny).
 */
data class DialogConfig(
    val title: String,
    val text: String,
    val confirmButtonText: String? = Tr.Action.confirm(),
    val onConfirm: (() -> Unit)? = null,
    val dismissButtonText: String? = Tr.Action.cancel(),
    val onDismiss: () -> Unit,
    val titleColor: Color? = null,
    val textColor: Color? = null
)

/**
 * Wyświetla dialog błędu z podanym tytułem i wiadomością.
 */
fun ErrorDialogConfig(title: String, message: String): DialogConfig {
    return DialogConfig(title,
        message,
        null,
        dismissButtonText = Tr.Error.Dialog.dismiss(),
        onDismiss = { GlobalDialogManager.dismiss() }
    )
}

/**
 * Wyświetla dialog błędu na podstawie wyjątku z bazy danych.
 */
fun ErrorDialogConfig(error: OctaviusException): DialogConfig {
    val title = Tr.Error.Database.title()
    return DialogConfig(title,
        error.toString(),
        null,
        dismissButtonText = Tr.Error.Dialog.dismiss(),
        onDismiss = { GlobalDialogManager.dismiss() }
    )
}

