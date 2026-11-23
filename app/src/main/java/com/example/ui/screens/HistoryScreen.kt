package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CalculationHistoryEntity
import com.example.ui.theme.LocalCalcColors
import com.example.ui.viewmodel.HistoryFilter
import com.example.ui.viewmodel.HistoryGroup
import com.example.ui.viewmodel.HistoryUiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (HistoryFilter) -> Unit,
    onTogglePin: (Long) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onReuseExpression: (String) -> Unit,
    onUseResult: (Double) -> Unit,
    onCopyResult: (String) -> Unit,
    onCopyEquation: (String) -> Unit,
    onOpenClearDialog: () -> Unit,
    onDismissClearDialog: () -> Unit,
    onClearUnpinned: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var recentlyCopiedId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(recentlyCopiedId) {
        if (recentlyCopiedId != null) {
            delay(1500)
            recentlyCopiedId = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.history_title).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (state.totalCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = state.totalCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (state.totalCount > 0) {
                IconButton(
                    onClick = onOpenClearDialog,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("history_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = stringResource(R.string.cd_clear_history),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Search field
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text(
                    text = stringResource(R.string.history_search_hint),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.cd_close)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input")
        )

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                HistoryFilter.ALL to stringResource(R.string.history_filter_all),
                HistoryFilter.PINNED to stringResource(R.string.history_filter_pinned),
                HistoryFilter.SCIENTIFIC to stringResource(R.string.history_filter_scientific)
            )
            filters.forEach { (filterType, label) ->
                val selected = state.filter == filterType
                FilterChip(
                    selected = selected,
                    onClick = { onFilterSelected(filterType) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("history_filter_${filterType.name.lowercase()}")
                )
            }
        }

        if (state.groupedEntries.isNotEmpty()) {
            Text(
                text = stringResource(R.string.history_tap_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // List or Empty State
        if (state.groupedEntries.isEmpty()) {
            EmptyHistoryState(
                isFiltered = state.totalCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.groupedEntries.forEach { (group, entries) ->
                    item(key = "header_${group.name}") {
                        val headerText = when (group) {
                            HistoryGroup.TODAY -> stringResource(R.string.history_group_today)
                            HistoryGroup.YESTERDAY -> stringResource(R.string.history_group_yesterday)
                            HistoryGroup.EARLIER -> stringResource(R.string.history_group_earlier)
                        }
                        Text(
                            text = headerText.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp, start = 4.dp)
                        )
                    }

                    items(entries, key = { it.id }) { entry ->
                        HistoryEntryCard(
                            entry = entry,
                            formattedTime = timeFormatter.format(Date(entry.timestamp)),
                            isRecentlyCopied = recentlyCopiedId == entry.id,
                            onReuseExpression = { onReuseExpression(entry.expression) },
                            onUseResult = { onUseResult(entry.rawResult) },
                            onTogglePin = { onTogglePin(entry.id) },
                            onCopyResult = {
                                recentlyCopiedId = entry.id
                                onCopyResult(entry.result)
                            },
                            onCopyEquation = {
                                recentlyCopiedId = entry.id
                                onCopyEquation("${entry.expression} = ${entry.result}")
                            },
                            onDelete = { onDeleteEntry(entry.id) }
                        )
                    }
                }
            }
        }
    }

    if (state.isClearDialogOpen) {
        AlertDialog(
            onDismissRequest = onDismissClearDialog,
            title = { Text(stringResource(R.string.history_clear_dialog_title)) },
            text = { Text(stringResource(R.string.history_clear_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = onClearAll,
                    modifier = Modifier.testTag("confirm_clear_all_history")
                ) {
                    Text(
                        text = stringResource(R.string.history_clear_all),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onDismissClearDialog) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    TextButton(
                        onClick = onClearUnpinned,
                        modifier = Modifier.testTag("confirm_clear_unpinned_history")
                    ) {
                        Text(stringResource(R.string.history_clear_unpinned))
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryEntryCard(
    entry: CalculationHistoryEntity,
    formattedTime: String,
    isRecentlyCopied: Boolean,
    onReuseExpression: () -> Unit,
    onUseResult: () -> Unit,
    onTogglePin: () -> Unit,
    onCopyResult: () -> Unit,
    onCopyEquation: () -> Unit,
    onDelete: () -> Unit
) {
    val calcColors = LocalCalcColors.current
    val haptic = LocalHapticFeedback.current
    val cardShape = RoundedCornerShape(18.dp)

    val copyPillContainer by animateColorAsState(
        targetValue = if (isRecentlyCopied) {
            calcColors.positiveAccent.copy(alpha = 0.2f)
        } else {
            calcColors.sciKeyContainer
        },
        label = "copyPillContainer"
    )
    val copyPillContent by animateColorAsState(
        targetValue = if (isRecentlyCopied) {
            calcColors.positiveAccent
        } else {
            calcColors.sciKeyContent
        },
        label = "copyPillContent"
    )

    Surface(
        shape = cardShape,
        color = calcColors.displayBackground,
        border = BorderStroke(
            width = 1.dp,
            color = if (isRecentlyCopied) {
                calcColors.positiveAccent.copy(alpha = 0.6f)
            } else if (entry.isPinned) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            } else {
                calcColors.displayBorder
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .combinedClickable(
                onClick = onReuseExpression,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCopyResult()
                }
            )
            .testTag("history_card_${entry.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Badges + timestamp + pin/delete actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = entry.angleMode,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (entry.isScientific) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = calcColors.sciKeyContainer
                        ) {
                            Text(
                                text = stringResource(R.string.mode_sci),
                                style = MaterialTheme.typography.labelSmall,
                                color = calcColors.sciKeyContent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("history_pin_${entry.id}")
                    ) {
                        Icon(
                            imageVector = if (entry.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = stringResource(R.string.cd_pin_item),
                            tint = if (entry.isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("history_delete_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.cd_delete_item),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expression
            Text(
                text = entry.expression,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Result
            Text(
                text = "= ${entry.result}",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Action strip: Copy Result (primary), Copy Eq, Use in Calc
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCopyResult()
                    },
                    shape = RoundedCornerShape(50),
                    color = copyPillContainer,
                    border = BorderStroke(1.dp, copyPillContent.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .testTag("history_copy_${entry.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (isRecentlyCopied) Icons.Rounded.Check else Icons.Outlined.ContentCopy,
                            contentDescription = stringResource(R.string.cd_copy_item),
                            tint = copyPillContent,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isRecentlyCopied) {
                                stringResource(R.string.action_copied)
                            } else {
                                stringResource(R.string.action_copy)
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = copyPillContent
                        )
                    }
                }

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCopyEquation()
                    },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .testTag("history_copy_eq_${entry.id}")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.action_copy_equation),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    onClick = onUseResult,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .testTag("history_use_result_${entry.id}")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.converter_send_to_calc),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(
    isFiltered: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Text(
                text = if (isFiltered) {
                    stringResource(R.string.history_empty_filtered_title)
                } else {
                    stringResource(R.string.history_empty_title)
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isFiltered) {
                    stringResource(R.string.history_empty_filtered_subtitle)
                } else {
                    stringResource(R.string.history_empty_subtitle)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
