package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.ActivityLevel
import com.example.domain.BiologicalSex
import com.example.domain.DateCalculatorEngine
import com.example.domain.DateOffsetUnit
import com.example.domain.HealthCategoryLevel
import com.example.domain.HealthMetricsResult
import com.example.ui.theme.LocalCalcColors
import com.example.ui.viewmodel.DateCalcMode
import com.example.ui.viewmodel.DatePickerTarget
import com.example.ui.viewmodel.ToolsSubTab
import com.example.ui.viewmodel.ToolsUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    state: ToolsUiState,
    onSelectTab: (ToolsSubTab) -> Unit,
    onSelectDateMode: (DateCalcMode) -> Unit,
    onOpenDatePicker: (DatePickerTarget) -> Unit,
    onDismissDatePicker: () -> Unit,
    onDatePicked: (Long) -> Unit,
    onSwapDates: () -> Unit,
    onToggleIncludeEndDate: () -> Unit,
    onApplyQuickDateRange: (Int, DateOffsetUnit) -> Unit,
    onOffsetAmountChanged: (String) -> Unit,
    onSelectOffsetUnit: (DateOffsetUnit) -> Unit,
    onSetSubtractOffset: (Boolean) -> Unit,
    onToggleUnitSystem: (Boolean) -> Unit,
    onSelectSex: (BiologicalSex) -> Unit,
    onSelectActivityLevel: (ActivityLevel) -> Unit,
    onAgeChanged: (String) -> Unit,
    onHeightPrimaryChanged: (String) -> Unit,
    onHeightInchesChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onWaistChanged: (String) -> Unit,
    onNeckChanged: (String) -> Unit,
    onHipChanged: (String) -> Unit,
    onCopyValue: (String) -> Unit,
    onSendDaysToCalculator: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.tools_title).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
                letterSpacing = 1.6.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Rounded Segmented Tab Bar: Date & Days vs Body & Health
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SegmentedPillTab(
                    label = stringResource(R.string.tools_tab_date),
                    icon = Icons.Outlined.CalendarMonth,
                    selected = state.activeTab == ToolsSubTab.DATE,
                    onClick = { onSelectTab(ToolsSubTab.DATE) },
                    testTag = "tools_tab_date",
                    modifier = Modifier.weight(1f)
                )
                SegmentedPillTab(
                    label = stringResource(R.string.tools_tab_health),
                    icon = Icons.Outlined.MonitorHeart,
                    selected = state.activeTab == ToolsSubTab.HEALTH,
                    onClick = { onSelectTab(ToolsSubTab.HEALTH) },
                    testTag = "tools_tab_health",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (state.activeTab) {
            ToolsSubTab.DATE -> DateCalculatorContent(
                state = state,
                onSelectDateMode = onSelectDateMode,
                onOpenDatePicker = onOpenDatePicker,
                onSwapDates = onSwapDates,
                onToggleIncludeEndDate = onToggleIncludeEndDate,
                onApplyQuickDateRange = onApplyQuickDateRange,
                onOffsetAmountChanged = onOffsetAmountChanged,
                onSelectOffsetUnit = onSelectOffsetUnit,
                onSetSubtractOffset = onSetSubtractOffset,
                onCopyValue = onCopyValue,
                onSendDaysToCalculator = onSendDaysToCalculator,
                modifier = Modifier.weight(1f)
            )

            ToolsSubTab.HEALTH -> HealthCalculatorContent(
                state = state,
                onToggleUnitSystem = onToggleUnitSystem,
                onSelectSex = onSelectSex,
                onSelectActivityLevel = onSelectActivityLevel,
                onAgeChanged = onAgeChanged,
                onHeightPrimaryChanged = onHeightPrimaryChanged,
                onHeightInchesChanged = onHeightInchesChanged,
                onWeightChanged = onWeightChanged,
                onWaistChanged = onWaistChanged,
                onNeckChanged = onNeckChanged,
                onHipChanged = onHipChanged,
                onCopyValue = onCopyValue,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (state.activeDatePicker != null) {
        val initialMillis = when (state.activeDatePicker) {
            DatePickerTarget.START -> state.startDateMillis
            DatePickerTarget.END -> state.endDateMillis
            DatePickerTarget.OFFSET_BASE -> state.offsetBaseMillis
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = onDismissDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onDatePicked)
                            ?: onDismissDatePicker()
                    }
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDatePicker) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SegmentedPillTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = modifier
            .heightIn(min = 44.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DateCalculatorContent(
    state: ToolsUiState,
    onSelectDateMode: (DateCalcMode) -> Unit,
    onOpenDatePicker: (DatePickerTarget) -> Unit,
    onSwapDates: () -> Unit,
    onToggleIncludeEndDate: () -> Unit,
    onApplyQuickDateRange: (Int, DateOffsetUnit) -> Unit,
    onOffsetAmountChanged: (String) -> Unit,
    onSelectOffsetUnit: (DateOffsetUnit) -> Unit,
    onSetSubtractOffset: (Boolean) -> Unit,
    onCopyValue: (String) -> Unit,
    onSendDaysToCalculator: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val calcColors = LocalCalcColors.current
    val numberFmt = remember { NumberFormat.getIntegerInstance(Locale.US) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sub-mode pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChoicePill(
                    label = stringResource(R.string.date_mode_between),
                    selected = state.dateCalcMode == DateCalcMode.BETWEEN_DATES,
                    onClick = { onSelectDateMode(DateCalcMode.BETWEEN_DATES) },
                    testTag = "date_mode_between",
                    modifier = Modifier.weight(1f)
                )
                ModeChoicePill(
                    label = stringResource(R.string.date_mode_offset),
                    selected = state.dateCalcMode == DateCalcMode.ADD_SUBTRACT,
                    onClick = { onSelectDateMode(DateCalcMode.ADD_SUBTRACT) },
                    testTag = "date_mode_offset",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.dateCalcMode == DateCalcMode.BETWEEN_DATES) {
            // Start & End Date Cards with overlapping Swap button
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DatePickerCard(
                            label = stringResource(R.string.date_start_label),
                            formattedDate = DateCalculatorEngine.formatDate(state.startDateMillis),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { onOpenDatePicker(DatePickerTarget.START) },
                            testTag = "date_start_picker"
                        )
                        DatePickerCard(
                            label = stringResource(R.string.date_end_label),
                            formattedDate = DateCalculatorEngine.formatDate(state.endDateMillis),
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = { onOpenDatePicker(DatePickerTarget.END) },
                            testTag = "date_end_picker"
                        )
                    }

                    Surface(
                        onClick = onSwapDates,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 18.dp)
                            .size(44.dp)
                            .testTag("date_swap_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.SwapVert,
                                contentDescription = stringResource(R.string.cd_swap_units),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Quick presets + Include end date toggle
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("+7d", 7, DateOffsetUnit.DAYS),
                            Triple("+30d", 30, DateOffsetUnit.DAYS),
                            Triple("+90d", 90, DateOffsetUnit.DAYS),
                            Triple("+1yr", 1, DateOffsetUnit.YEARS)
                        ).forEach { (label, amt, unit) ->
                            Surface(
                                onClick = { onApplyQuickDateRange(amt, unit) },
                                shape = RoundedCornerShape(50),
                                color = calcColors.sciKeyContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 38.dp)
                                    .testTag("date_preset_$label")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = calcColors.sciKeyContent
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.date_include_end),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = state.includeEndDate,
                                onCheckedChange = { onToggleIncludeEndDate() },
                                modifier = Modifier.testTag("date_include_end_switch")
                            )
                        }
                    }
                }
            }

            // Hero Total Days Result Card
            item {
                val diff = state.dateDiff
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = calcColors.displayBackground,
                    border = BorderStroke(1.dp, calcColors.displayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(calcColors.displayGlowSecondary, Color.Transparent),
                                        center = Offset(size.width * 0.85f, size.height * 0.25f),
                                        radius = size.maxDimension * 0.6f
                                    )
                                )
                            }
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.date_total_days).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    onClick = { onSendDaysToCalculator(diff.totalDays.toDouble()) },
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.testTag("date_send_to_calc")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Calculate,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.converter_send_to_calc),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onCopyValue("${diff.totalDays} days") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = stringResource(R.string.cd_copy_result),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${numberFmt.format(diff.totalDays)} days",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.testTag("date_total_days_value")
                        )

                        Text(
                            text = "${diff.years} yr, ${diff.months} mo, ${diff.days} d",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Breakdown Grid
            item {
                val diff = state.dateDiff
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMetricTile(
                            label = stringResource(R.string.date_weeks_label),
                            value = "${numberFmt.format(diff.weeks)} wk ${diff.remainingDaysAfterWeeks}d",
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricTile(
                            label = stringResource(R.string.date_weekdays_label),
                            value = numberFmt.format(diff.weekdays),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMetricTile(
                            label = stringResource(R.string.date_weekends_label),
                            value = numberFmt.format(diff.weekendDays),
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricTile(
                            label = stringResource(R.string.date_hours_label),
                            value = numberFmt.format(diff.totalHours),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    StatMetricTile(
                        label = stringResource(R.string.date_minutes_label),
                        value = numberFmt.format(diff.totalMinutes),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // ADD / SUBTRACT DAYS MODE
            item {
                DatePickerCard(
                    label = stringResource(R.string.date_base_label),
                    formattedDate = DateCalculatorEngine.formatDate(state.offsetBaseMillis),
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = { onOpenDatePicker(DatePickerTarget.OFFSET_BASE) },
                    testTag = "date_offset_base_picker"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeChoicePill(
                        label = stringResource(R.string.date_add_label),
                        selected = !state.isSubtractOffset,
                        onClick = { onSetSubtractOffset(false) },
                        testTag = "date_offset_add",
                        modifier = Modifier.weight(1f)
                    )
                    ModeChoicePill(
                        label = stringResource(R.string.date_sub_label),
                        selected = state.isSubtractOffset,
                        onClick = { onSetSubtractOffset(true) },
                        testTag = "date_offset_sub",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = state.offsetAmountText,
                    onValueChange = onOffsetAmountChanged,
                    label = { Text(stringResource(R.string.date_amount_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_offset_amount_input")
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DateOffsetUnit.entries.forEach { unit ->
                        val selected = state.offsetUnit == unit
                        Surface(
                            onClick = { onSelectOffsetUnit(unit) },
                            shape = RoundedCornerShape(50),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 42.dp)
                                .testTag("date_unit_${unit.name.lowercase()}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = unit.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item {
                val targetDateStr = DateCalculatorEngine.formatDate(state.offsetResultMillis)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = calcColors.displayBackground,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.date_resulting_date).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = { onCopyValue(targetDateStr) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = stringResource(R.string.cd_copy_result),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = targetDateStr,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.testTag("date_offset_result_text")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePickerCard(
    label: String,
    formattedDate: String,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val calcColors = LocalCalcColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = calcColors.displayBackground,
        border = BorderStroke(1.dp, calcColors.displayBorder),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.16f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = stringResource(R.string.cd_pick_date),
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun HealthCalculatorContent(
    state: ToolsUiState,
    onToggleUnitSystem: (Boolean) -> Unit,
    onSelectSex: (BiologicalSex) -> Unit,
    onSelectActivityLevel: (ActivityLevel) -> Unit,
    onAgeChanged: (String) -> Unit,
    onHeightPrimaryChanged: (String) -> Unit,
    onHeightInchesChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onWaistChanged: (String) -> Unit,
    onNeckChanged: (String) -> Unit,
    onHipChanged: (String) -> Unit,
    onCopyValue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activityDropdownOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Unit System + Sex selectors
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChoicePill(
                    label = stringResource(R.string.health_metric),
                    selected = state.useMetricUnits,
                    onClick = { onToggleUnitSystem(true) },
                    testTag = "health_unit_metric",
                    modifier = Modifier.weight(1f)
                )
                ModeChoicePill(
                    label = stringResource(R.string.health_imperial),
                    selected = !state.useMetricUnits,
                    onClick = { onToggleUnitSystem(false) },
                    testTag = "health_unit_imperial",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BiologicalSex.entries.forEach { sex ->
                    ModeChoicePill(
                        label = sex.label,
                        selected = state.sex == sex,
                        onClick = { onSelectSex(sex) },
                        testTag = "health_sex_${sex.name.lowercase()}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Core biometric inputs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumericInputField(
                        value = state.ageText,
                        onValueChange = onAgeChanged,
                        label = stringResource(R.string.health_age),
                        testTag = "health_age_input",
                        modifier = Modifier.weight(1f)
                    )
                    NumericInputField(
                        value = state.weightText,
                        onValueChange = onWeightChanged,
                        label = stringResource(
                            if (state.useMetricUnits) R.string.health_weight_kg else R.string.health_weight_lb
                        ),
                        testTag = "health_weight_input",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (state.useMetricUnits) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NumericInputField(
                            value = state.heightPrimaryText,
                            onValueChange = onHeightPrimaryChanged,
                            label = stringResource(R.string.health_height_cm),
                            testTag = "health_height_input",
                            modifier = Modifier.weight(1f)
                        )
                        NumericInputField(
                            value = state.waistText,
                            onValueChange = onWaistChanged,
                            label = stringResource(R.string.health_waist_cm),
                            testTag = "health_waist_input",
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NumericInputField(
                            value = state.heightPrimaryText,
                            onValueChange = onHeightPrimaryChanged,
                            label = stringResource(R.string.health_height_ft),
                            testTag = "health_height_ft_input",
                            modifier = Modifier.weight(1f)
                        )
                        NumericInputField(
                            value = state.heightInchesText,
                            onValueChange = onHeightInchesChanged,
                            label = stringResource(R.string.health_height_in),
                            testTag = "health_height_in_input",
                            modifier = Modifier.weight(1f)
                        )
                        NumericInputField(
                            value = state.waistText,
                            onValueChange = onWaistChanged,
                            label = stringResource(R.string.health_waist_in),
                            testTag = "health_waist_input",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumericInputField(
                        value = state.neckText,
                        onValueChange = onNeckChanged,
                        label = stringResource(
                            if (state.useMetricUnits) R.string.health_neck_cm else R.string.health_neck_in
                        ),
                        testTag = "health_neck_input",
                        modifier = Modifier.weight(1f)
                    )
                    if (state.sex == BiologicalSex.FEMALE) {
                        NumericInputField(
                            value = state.hipText,
                            onValueChange = onHipChanged,
                            label = stringResource(
                                if (state.useMetricUnits) R.string.health_hip_cm else R.string.health_hip_in
                            ),
                            testTag = "health_hip_input",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Activity level dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { activityDropdownOpen = true },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("health_activity_dropdown")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stringResource(R.string.health_activity_label)}: ${state.activityLevel.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = activityDropdownOpen,
                        onDismissRequest = { activityDropdownOpen = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        ActivityLevel.entries.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level.label) },
                                onClick = {
                                    activityDropdownOpen = false
                                    onSelectActivityLevel(level)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Health Metric Result Cards
        val result = state.healthResult
        if (result != null) {
            item {
                BmiHeroCard(
                    result = result,
                    useMetric = state.useMetricUnits,
                    onCopy = {
                        onCopyValue(
                            String.format(
                                Locale.US,
                                "BMI: %.1f (%s) | Body Fat: %.1f%% | Visceral Fat: %d | Body Age: %d yrs",
                                result.bmi,
                                result.bmiCategory,
                                result.bodyFatPercent,
                                result.visceralFatRating,
                                result.estimatedBodyAge
                            )
                        )
                    }
                )
            }

            item {
                val massUnit = if (state.useMetricUnits) "kg" else "lb"
                val massFactor = if (state.useMetricUnits) 1.0 else 2.20462262
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthMetricCard(
                        title = stringResource(R.string.health_body_fat_title),
                        value = String.format(Locale.US, "%.1f%%", result.bodyFatPercent),
                        badge = result.bodyFatCategory,
                        level = result.bodyFatLevel,
                        subtitle = result.bodyFatMethod,
                        testTag = "health_body_fat_card",
                        modifier = Modifier.weight(1f)
                    )
                    HealthMetricCard(
                        title = stringResource(R.string.health_visceral_title),
                        value = "${result.visceralFatRating}",
                        badge = result.visceralFatCategory,
                        level = result.visceralFatLevel,
                        subtitle = String.format(Locale.US, "WHtR %.2f (1–59)", result.waistToHeightRatio),
                        testTag = "health_visceral_card",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val deltaText = when {
                        result.bodyAgeDelta < 0 -> "${-result.bodyAgeDelta} yr younger"
                        result.bodyAgeDelta > 0 -> "+${result.bodyAgeDelta} yr older"
                        else -> "Matches age"
                    }
                    val ageLevel = when {
                        result.bodyAgeDelta <= 0 -> HealthCategoryLevel.OPTIMAL
                        result.bodyAgeDelta <= 4 -> HealthCategoryLevel.ELEVATED
                        else -> HealthCategoryLevel.HIGH
                    }
                    HealthMetricCard(
                        title = stringResource(R.string.health_body_age_title),
                        value = "${result.estimatedBodyAge} yrs",
                        badge = deltaText,
                        level = ageLevel,
                        subtitle = "Metabolic estimate",
                        testTag = "health_body_age_card",
                        modifier = Modifier.weight(1f)
                    )
                    HealthMetricCard(
                        title = stringResource(R.string.health_lean_mass),
                        value = String.format(Locale.US, "%.1f %s", result.leanBodyMassKg * massFactor, massUnit),
                        badge = String.format(Locale.US, "Fat %.1f %s", result.fatMassKg * massFactor, massUnit),
                        level = HealthCategoryLevel.OPTIMAL,
                        subtitle = "Lean vs Fat mass",
                        testTag = "health_lean_mass_card",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMetricTile(
                        label = stringResource(R.string.health_bmr_title),
                        value = "${result.bmrKcal} kcal/d",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricTile(
                        label = stringResource(R.string.health_tdee_title),
                        value = "${result.tdeeKcal} kcal/d",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.health_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BmiHeroCard(
    result: HealthMetricsResult,
    useMetric: Boolean,
    onCopy: () -> Unit
) {
    val calcColors = LocalCalcColors.current
    val badgeColor = levelColor(result.bmiLevel)
    val unit = if (useMetric) "kg" else "lb"
    val factor = if (useMetric) 1.0 else 2.20462262
    val progress = ((result.bmi - 14.0) / (38.0 - 14.0)).toFloat().coerceIn(0.03f, 0.97f)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = calcColors.displayBackground,
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("health_bmi_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.health_bmi_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = result.bmiCategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.cd_copy_result),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f", result.bmi),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor
                )
                Text(
                    text = String.format(
                        Locale.US,
                        "Healthy: %.0f–%.0f %s",
                        result.healthyWeightMinKg * factor,
                        result.healthyWeightMaxKg * factor,
                        unit
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Spectrum bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                calcColors.positiveAccent,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.error
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthMetricCard(
    title: String,
    value: String,
    badge: String,
    level: HealthCategoryLevel,
    subtitle: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val calcColors = LocalCalcColors.current
    val accent = levelColor(level)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = calcColors.displayBackground,
        border = BorderStroke(1.dp, calcColors.displayBorder),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = accent.copy(alpha = 0.16f)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ModeChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .heightIn(min = 42.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NumericInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.testTag(testTag)
    )
}

@Composable
private fun levelColor(level: HealthCategoryLevel): Color {
    val calcColors = LocalCalcColors.current
    return when (level) {
        HealthCategoryLevel.LOW -> MaterialTheme.colorScheme.secondary
        HealthCategoryLevel.OPTIMAL -> calcColors.positiveAccent
        HealthCategoryLevel.ELEVATED -> MaterialTheme.colorScheme.primary
        HealthCategoryLevel.HIGH -> MaterialTheme.colorScheme.error
    }
}
