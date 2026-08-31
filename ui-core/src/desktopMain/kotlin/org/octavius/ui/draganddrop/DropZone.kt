package org.octavius.ui.draganddrop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.unit.dp

object DropZoneConstants {
    val dropZoneHeight = 40.dp
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T: Any> DropZone(
    modifier: Modifier = Modifier,
    canAccept: (T) -> Boolean = { true },
    onDrop: (T) -> Boolean,
    content: @Composable BoxScope.(isHovered: Boolean) -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    val dropTarget = remember(onDrop) {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) {
                val data = event.getLocalData<T>()

                if (data != null && canAccept(data)) {
                    isHovered = true
                }
            }

            override fun onExited(event: DragAndDropEvent) {
                isHovered = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isHovered = false
                val data = event.getLocalData<T>() ?: return false
                if (!canAccept(data)) return false
                return onDrop(data)
            }
        }
    }

    Box(
        modifier = modifier
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dropTarget
            )
            // Wygląd może zależeć od stanu 'isHovered'
            .background(
                color = if (isHovered) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        content(isHovered) // Przekazujemy stan do treści
    }
}