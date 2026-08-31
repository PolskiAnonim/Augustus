package org.octavius.modules.asian.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.octavius.modules.asian.api.ApiClient
import org.octavius.modules.asian.model.AsianPublicationData
import org.octavius.modules.asian.model.PublicationAddRequest
import org.octavius.modules.asian.model.PublicationCheckRequest
import org.octavius.modules.asian.model.PublicationCheckResponse
import org.octavius.navigation.Screen

/**
 * Ekran dedykowany do wyświetlania danych sparsowanych ze strony
 * i umożliwiający dodanie ich do bazy danych Augustus.
 */
class AsianMediaAddScreen(private val data: AsianPublicationData) : Screen {
    override val title = "asianMediaAddScreen"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        var isLoading by remember { mutableStateOf(false) }
        var checkResponse by remember { mutableStateOf<PublicationCheckResponse?>(null) }
        var statusMessage by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
        val coroutineScope = rememberCoroutineScope()

        // Sprawdź czy tytuł już istnieje przy wejściu na ekran
        LaunchedEffect(data.titles) {
            if (data.titles.isNotEmpty()) {
                checkResponse = ApiClient.checkPublicationExistence(PublicationCheckRequest(data.titles))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                "Augustus Helper",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            
            Text(
                "Źródło: ${data.source}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Warning Section if exists
            checkResponse?.let { check ->
                if (check.found) {
                    WarningCard(check.matchedTitle ?: "Nieznany tytuł")
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Main Content Card
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Wykryte tytuły:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    if (data.titles.isEmpty()) {
                        Text("Nie znaleziono tytułów...", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        data.titles.forEach { title ->
                            Text(
                                "• $title",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                    InfoRow("Typ", data.type.toDisplayString())
                    InfoRow("Język", data.language.toDisplayString())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = {
                    isLoading = true
                    statusMessage = null
                    coroutineScope.launch {
                        val request = PublicationAddRequest(
                            titles = data.titles,
                            type = data.type,
                            language = data.language
                        )
                        val response = ApiClient.addPublication(request)
                        statusMessage = Pair(response.message, response.success)
                        isLoading = false
                    }
                },
                enabled = data.titles.isNotEmpty() && !isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Dodaj do Augustusa", fontWeight = FontWeight.Bold)
                }
            }

            // Status Message
            statusMessage?.let { (message, isSuccess) ->
                val color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Text(
                    message,
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 12.sp
                )
            }
        }
    }

    @Composable
    private fun WarningCard(matchedTitle: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Ostrzeżenie",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Podobny tytuł w bazie!",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Znaleziono: \"$matchedTitle\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }

    @Composable
    private fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
    }
}