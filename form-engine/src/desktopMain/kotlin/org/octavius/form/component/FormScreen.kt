package org.octavius.form.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.octavius.form.control.base.ControlContext
import org.octavius.localization.Tr
import org.octavius.navigation.Screen

/**
 * Klasa będąca UI formularza - należy do niej wstawić klasę która odpowiada za jego obsługę
 */
class FormScreen(
    override val title: String,
    val formHandler: FormHandler
) : Screen {
    /**
     * Tworzenie wyglądu formularza
     */
    @Composable
    override fun Content() {
        val isLoading by formHandler.isLoading
        val actionTriggered by formHandler.actionTriggered

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                // Podczas ładowania - tylko kręciołek, bez renderowania kontrolek
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Formularz załadowany - renderuj kontrolki
                Column(modifier = Modifier.fillMaxSize()) {
                    // Główna zawartość
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Wyświetlanie błędów globalnych
                        val globalErrors by formHandler.errorManager.globalErrors
                        if (globalErrors.isNotEmpty()) {
                            GlobalErrorsCard(errors = globalErrors)
                        }

                        // content
                        formHandler.getContentControlsInOrder().forEach { controlName ->
                            val control = formHandler.getControl(controlName)!!
                            val state = formHandler.getControlState(controlName)!!
                            control.Render(controlContext = ControlContext(controlName), controlState = state)
                        }
                    } // Koniec strefy scrollowanej

                    // Pasek akcji
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            formHandler.getActionBarControlsInOrder().forEach { controlName ->
                                val control = formHandler.getControl(controlName)!!
                                val state = formHandler.getControlState(controlName)!!
                                Box(modifier = Modifier.weight(1f)) {
                                    control.Render(controlContext = ControlContext(controlName), controlState = state)
                                }
                            }
                        }
                    } // Koniec paska akcji
                }

                // Overlay blokujący interakcje podczas wykonywania akcji
                if (actionTriggered) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                            .pointerInput(Unit) { /* zjada wszystkie eventy */ },
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }


    /**
     * Komponent wyświetlający błędy globalne
     */
    @Composable
    private fun GlobalErrorsCard(errors: List<String>) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = Tr.Form.Actions.errorsLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                errors.forEach { error ->
                    Text(
                        text = "• $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

}