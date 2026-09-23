package org.octavius.modules.sandbox.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.octavius.dialog.DialogConfig
import org.octavius.dialog.GlobalDialogManager
import org.octavius.localization.Tr
import org.octavius.modules.sandbox.localization.SandboxTr
import org.octavius.navigation.Screen
import org.octavius.ui.color.ColorPickerDialog
import org.octavius.ui.datetime.DateTimePickerDialog
import org.octavius.ui.datetime.IntervalPickerDialog
import org.octavius.ui.snackbar.SnackbarManager
import org.octavius.util.DateAdapter
import kotlin.time.Duration

class PopupShowcaseScreen : Screen {

    override val title = SandboxTr.Popup.title()

    @Composable
    override fun Content() {
        var showColorPicker by remember { mutableStateOf(false) }
        var showDateTimePicker by remember { mutableStateOf(false) }
        var showIntervalPicker by remember { mutableStateOf(false) }

        var selectedColor by remember { mutableStateOf<Color?>(null) }
        var selectedDateTime by remember { mutableStateOf<String?>(null) }
        var selectedInterval by remember { mutableStateOf<Duration?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alert Dialog
            ShowcaseItem(
                title = SandboxTr.Popup.alertDialog(),
                result = null,
                onClick = {
                    GlobalDialogManager.show(
                        DialogConfig(
                            title = SandboxTr.Popup.alertTitle(),
                            text = SandboxTr.Popup.alertMessage(),
                            confirmButtonText = Tr.Action.confirm(),
                            onConfirm = { GlobalDialogManager.dismiss() },
                            dismissButtonText = Tr.Action.cancel(),
                            onDismiss = { GlobalDialogManager.dismiss() }
                        )
                    )
                }
            )

            // Error Dialog
            ShowcaseItem(
                title = SandboxTr.Popup.errorDialog(),
                result = null,
                onClick = {
                    GlobalDialogManager.show(
                        DialogConfig(
                            title = SandboxTr.Popup.errorTitle(),
                            text = SandboxTr.Popup.errorMessage(),
                            confirmButtonText = null,
                            onConfirm = null,
                            dismissButtonText = Tr.Error.Dialog.dismiss(),
                            onDismiss = { GlobalDialogManager.dismiss() }
                        )
                    )
                }
            )

            // Color Picker
            ShowcaseItem(
                title = SandboxTr.Popup.colorPicker(),
                result = selectedColor?.let { "${SandboxTr.Popup.selectedColor()}: #${colorToHex(it)}" },
                onClick = { showColorPicker = true },
                colorPreview = selectedColor
            )

            // DateTime Picker
            ShowcaseItem(
                title = SandboxTr.Popup.dateTimePicker(),
                result = selectedDateTime?.let { "${SandboxTr.Popup.selectedDateTime()}: $it" },
                onClick = { showDateTimePicker = true }
            )

            // Interval Picker
            ShowcaseItem(
                title = SandboxTr.Popup.intervalPicker(),
                result = selectedInterval?.let { "${SandboxTr.Popup.selectedInterval()}: $it" },
                onClick = { showIntervalPicker = true }
            )

            // Snackbar
            ShowcaseItem(
                title = SandboxTr.Popup.snackbar(),
                result = null,
                onClick = {
                    SnackbarManager.showMessage(SandboxTr.Popup.snackbarMessage())
                }
            )
        }

        // Dialogs
        if (showColorPicker) {
            ColorPickerDialog(
                initialColor = selectedColor ?: Color.Red,
                showAlphaSlider = false,
                onDismiss = { showColorPicker = false },
                onConfirm = { color ->
                    selectedColor = color
                    showColorPicker = false
                }
            )
        }

        if (showDateTimePicker) {
            DateTimePickerDialog(
                adapter = DateAdapter,
                initialValue = null,
                onDismiss = { showDateTimePicker = false },
                onConfirm = { value ->
                    selectedDateTime = value?.let { DateAdapter.format(it) }
                    showDateTimePicker = false
                }
            )
        }

        if (showIntervalPicker) {
            IntervalPickerDialog(
                initialValue = selectedInterval,
                onDismiss = { showIntervalPicker = false },
                onConfirm = { duration ->
                    selectedInterval = duration
                    showIntervalPicker = false
                }
            )
        }
    }

    companion object {
        fun create(): PopupShowcaseScreen = PopupShowcaseScreen()
    }
}

@Composable
private fun ShowcaseItem(
    title: String,
    result: String?,
    onClick: () -> Unit,
    colorPreview: Color? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (result != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (colorPreview != null) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(colorPreview, RoundedCornerShape(4.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SandboxTr.Popup.noSelection(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(onClick = onClick) {
                Text(title)
            }
        }
    }
}

private fun colorToHex(color: Color): String {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    return "%02X%02X%02X".format(r, g, b)
}
