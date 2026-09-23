package org.octavius.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import org.octavius.app.localization.AppTr
import org.octavius.localization.Tr

/**
 * Główny pasek górny aplikacji z tytułem i przyciskami nawigacyjnymi.
 *
 * Wyświetla wyśrodkowany pasek z dynamicznym tytułem, opcjonalnym przyciskiem "Wstecz"
 * oraz przyciskiem ustawień.
 *
 * @param title Tekst tytułu wyświetlany w środku paska
 * @param showBackButton Czy pokazać przycisk "Wstecz" po lewej stronie
 * @param onBackClicked Callback wywoływany po kliknięciu przycisku "Wstecz"
 * @param onSettingsClicked Callback wywoływany po kliknięciu przycisku ustawień
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    showBackButton: Boolean,
    onBackClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            // Animujemy treść tytułu, aby przejścia były płynne
            AnimatedContent(targetState = title) { targetTitle ->
                Text(
                    text = targetTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis // W razie gdyby tytuł był za długi
                )
            }
        },
        navigationIcon = {
            AnimatedVisibility(visible = showBackButton) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Tr.Navigation.back()
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSettingsClicked) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = AppTr.Settings.title()
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}