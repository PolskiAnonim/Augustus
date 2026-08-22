package org.octavius.report.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.octavius.report.ReportEvent
import org.octavius.report.ReportPaginationState
import org.octavius.report.localization.ReportTr

@Composable
fun PaginationComponent(
    pagination: ReportPaginationState,
    onEvent: (ReportEvent) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PreviousPage(pagination, onEvent)
            ActualPage(pagination, onEvent)
            PageSize(pagination, onEvent)
            NextPage(pagination, onEvent)
        }
    }
}

@Composable
fun PreviousPage(pagination: ReportPaginationState, onEvent: (ReportEvent) -> Unit) {
    // Przycisk poprzedniej strony
    IconButton(
        onClick = {
            if (pagination.currentPage > 0) {
                onEvent.invoke(ReportEvent.PageChanged(pagination.currentPage - 1))
            }
        },
        enabled = pagination.currentPage > 0
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = ReportTr.Pagination.previousPage(),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun NextPage(pagination: ReportPaginationState, onEvent: (ReportEvent) -> Unit) {
    // Przycisk następnej strony
    IconButton(
        onClick = {
            if (pagination.currentPage < pagination.totalPages - 1) {
                onEvent.invoke(ReportEvent.PageChanged(pagination.currentPage + 1))
            }
        },
        enabled = pagination.currentPage < pagination.totalPages - 1
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = ReportTr.Pagination.nextPage(),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun PageSize(pagination: ReportPaginationState, onEvent: (ReportEvent) -> Unit) {
    // Selektor rozmiaru strony
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = ReportTr.Pagination.size(),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        val pageSizeOptions = listOf<Long>(10, 20, 50, 100, 200)
        var expanded by remember { mutableStateOf(false) }

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.width(60.dp).height(32.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text(
                    text = pagination.pageSize.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                pageSizeOptions.forEach { size ->
                    DropdownMenuItem(
                        text = { Text(size.toString()) },
                        onClick = {
                            onEvent.invoke(ReportEvent.PageSizeChanged(size))
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActualPage(pagination: ReportPaginationState, onEvent: (ReportEvent) -> Unit) {
    var pageInputValue by remember(pagination.currentPage) {
        mutableStateOf(
            TextFieldValue(
                text = (pagination.currentPage + 1).toString(),
                selection = TextRange((pagination.currentPage + 1).toString().length)
            )
        )
    }

    var isEditingPage by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }


    // Wyświetlanie aktualnej strony z możliwością edycji
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = ReportTr.Pagination.page(),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        if (isEditingPage) {
            BasicTextField(
                value = pageInputValue,
                onValueChange = { newValue ->
                    // Pozwól tylko na cyfry
                    if (newValue.text.all { char -> char.isDigit() }) {
                        pageInputValue = newValue.copy(
                            selection = TextRange(newValue.text.length)
                        )
                    }
                },
                modifier = Modifier
                    .width(40.dp)
                    .height(36.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.shapes.small
                    )
                    .padding(4.dp)
                    .wrapContentHeight(Alignment.CenterVertically)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val newPage = pageInputValue.text.toLongOrNull()
                        if (newPage != null && newPage > 0 && newPage <= pagination.totalPages) {
                            onEvent(ReportEvent.PageChanged(newPage - 1))
                        } else {
                            pageInputValue = TextFieldValue(
                                text = (pagination.currentPage + 1).toString(),
                                selection = TextRange((pagination.currentPage + 1).toString().length)
                            )
                        }
                        isEditingPage = false
                    }
                )
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

        } else {
            TextButton(
                onClick = {
                    isEditingPage = true
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("${pagination.currentPage + 1}")
            }
        }

        Text(
            text = " " + ReportTr.Pagination.of() + " ${pagination.totalPages}",
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}