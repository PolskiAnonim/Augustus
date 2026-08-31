package org.octavius.app.settings.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.octavius.localization.Tr
import org.octavius.app.settings.form.database.DatabaseSettingsFormScreen
import org.octavius.app.settings.form.language.LanguageSettingsFormScreen
import org.octavius.app.settings.form.theme.ThemeSettingsFormScreen
import org.octavius.app.settings.report.ui.ApiIntegrationsReportScreen
import org.octavius.navigation.AppRouter
import org.octavius.navigation.Screen
import org.octavius.app.settings.AppSettingsManager
import org.octavius.theme.FormSpacing

class SettingsScreen() : Screen, KoinComponent {
    override val title = Tr.Settings.title()

    private val settingsManager: AppSettingsManager by inject()

    data class SettingOption(
        val title: String,
        val description: String,
        val icon: ImageVector,
        val onClick: () -> Unit
    )

    @Composable
    override fun Content() {

        val settingOptions = listOf(
            SettingOption(
                title = Tr.Settings.Language.title(),
                description = Tr.Settings.Language.description(),
                icon = Icons.Default.Language,
                onClick = {
                    AppRouter.navigateTo(LanguageSettingsFormScreen.create(settingsManager))
                }
            ),
            SettingOption(
                title = Tr.Settings.Theme.title(),
                description = Tr.Settings.Theme.description(),
                icon = Icons.Default.Palette,
                onClick = {
                    AppRouter.navigateTo(ThemeSettingsFormScreen.create(settingsManager))
                }
            ),
            SettingOption(
                title = Tr.Settings.Database.title(),
                description = Tr.Settings.Database.description(),
                icon = Icons.Default.Storage,
                onClick = {
                    AppRouter.navigateTo(DatabaseSettingsFormScreen.create(settingsManager))
                }
            ),
            SettingOption(
                title = Tr.Settings.Api.title(),
                description = Tr.Settings.Api.description(),
                icon = Icons.Default.Api,
                onClick = {
                    AppRouter.navigateTo(ApiIntegrationsReportScreen.create())
                }
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(FormSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(FormSpacing.controlSpacing)
        ) {
            items(settingOptions) { option ->
                SettingOptionCard(option)
            }
        }
    }

    @Composable
    private fun SettingOptionCard(option: SettingOption) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = option.onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FormSpacing.cardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(FormSpacing.cardPadding))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}