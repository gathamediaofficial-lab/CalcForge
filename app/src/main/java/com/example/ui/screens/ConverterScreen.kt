package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.TireRepair
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.MeasurementUnit
import com.example.domain.UnitCategory
import com.example.domain.UnitConverterEngine
import com.example.domain.UnitGroup
import com.example.ui.components.CalcKeypadButton
import com.example.ui.components.EditableCursorDisplay
import com.example.ui.theme.LocalCalcColors
import com.example.ui.viewmodel.ConverterUiState
import com.example.ui.viewmodel.UnitPickerTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    state: ConverterUiState,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onSelectCategory: (UnitCategory) -> Unit,
    onOpenUnitPicker: (UnitPickerTarget) -> Unit,
    onDismissUnitPicker: () -> Unit,
    onSelectUnit: (MeasurementUnit) -> Unit,
    onSwapUnits: () -> Unit,
    onToggleAllConversions: () -> Unit,
    onSelectionChanged: (TextRange) -> Unit,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onToggleSign: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCopyConverted: (String) -> Unit,
    onSendToCalculator: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var swapCount by remember { mutableIntStateOf(0) }
    val swapRotation by animateFloatAsState(
        targetValue = swapCount * 180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swapRotation"
    )
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        val isWide = maxWidth >= 600.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.converter_title).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.6.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("converter_theme_toggle")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = stringResource(R.string.cd_toggle_theme),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Organized Category Dropdown Menu Selector
            CategoryDropdownSelector(
                selectedCategory = state.selectedCategory,
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it },
                onSelectCategory = { cat ->
                    categoryDropdownExpanded = false
                    onSelectCategory(cat)
                }
            )

            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ConversionCardsBlock(
                            state = state,
                            swapRotation = swapRotation,
                            onSelectionChanged = onSelectionChanged,
                            onOpenUnitPicker = onOpenUnitPicker,
                            onSwap = {
                                swapCount++
                                onSwapUnits()
                            },
                            onCopyConverted = { onCopyConverted("${state.convertedFormatted} ${state.toUnit.symbol}") },
                            onSendToCalculator = { onSendToCalculator(state.convertedRaw) }
                        )
                        AllUnitsStrip(
                            state = state,
                            alwaysExpanded = true,
                            onToggle = onToggleAllConversions,
                            onCopyValue = onCopyConverted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    ConverterNumericKeypad(
                        onDigit = onDigit,
                        onDecimal = onDecimal,
                        onToggleSign = onToggleSign,
                        onBackspace = onBackspace,
                        onClear = onClear,
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                    )
                }
            } else {
                ConversionCardsBlock(
                    state = state,
                    swapRotation = swapRotation,
                    onSelectionChanged = onSelectionChanged,
                    onOpenUnitPicker = onOpenUnitPicker,
                    onSwap = {
                        swapCount++
                        onSwapUnits()
                    },
                    onCopyConverted = { onCopyConverted("${state.convertedFormatted} ${state.toUnit.symbol}") },
                    onSendToCalculator = { onSendToCalculator(state.convertedRaw) }
                )

                AllUnitsStrip(
                    state = state,
                    alwaysExpanded = false,
                    onToggle = onToggleAllConversions,
                    onCopyValue = onCopyConverted,
                    modifier = if (state.showAllConversions) {
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    } else {
                        Modifier.fillMaxWidth()
                    }
                )

                if (!state.showAllConversions) {
                    ConverterNumericKeypad(
                        onDigit = onDigit,
                        onDecimal = onDecimal,
                        onToggleSign = onToggleSign,
                        onBackspace = onBackspace,
                        onClear = onClear,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }

    if (state.pickerTarget != null) {
        val units = UnitConverterEngine.unitsByCategory[state.selectedCategory].orEmpty()
        val selectedId = if (state.pickerTarget == UnitPickerTarget.FROM) {
            state.fromUnit.id
        } else {
            state.toUnit.id
        }
        UnitPickerSheet(
            units = units,
            selectedUnitId = selectedId,
            onSelect = onSelectUnit,
            onDismiss = onDismissUnitPicker
        )
    }
}

@Composable
private fun CategoryDropdownSelector(
    selectedCategory: UnitCategory,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelectCategory: (UnitCategory) -> Unit
) {
    val calcColors = LocalCalcColors.current
    val groupedCategories = remember { UnitCategory.entries.groupBy { it.group } }

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = { onExpandedChange(true) },
            shape = RoundedCornerShape(50),
            color = calcColors.displayBackground,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("converter_category_dropdown")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = categoryIcon(selectedCategory),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedCategory.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (selectedCategory.isOfflineReference) {
                                "${selectedCategory.group.displayName} • ${stringResource(R.string.converter_offline_rates_badge)}"
                            } else {
                                "${selectedCategory.group.displayName} • ${UnitConverterEngine.unitsByCategory[selectedCategory]?.size ?: 0} units"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.cd_select_category),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 420.dp)
        ) {
            UnitGroup.entries.forEachIndexed { groupIdx, group ->
                val cats = groupedCategories[group].orEmpty()
                if (groupIdx > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                Text(
                    text = group.displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                cats.forEach { category ->
                    val isSelected = category == selectedCategory
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = "${UnitConverterEngine.unitsByCategory[category]?.size ?: 0}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = categoryIcon(category),
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        onClick = { onSelectCategory(category) },
                        modifier = Modifier.testTag("category_dropdown_${category.name.lowercase()}")
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversionCardsBlock(
    state: ConverterUiState,
    swapRotation: Float,
    onSelectionChanged: (TextRange) -> Unit,
    onOpenUnitPicker: (UnitPickerTarget) -> Unit,
    onSwap: () -> Unit,
    onCopyConverted: () -> Unit,
    onSendToCalculator: () -> Unit
) {
    val calcColors = LocalCalcColors.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // FROM card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = calcColors.displayBackground,
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            onClick = { onOpenUnitPicker(UnitPickerTarget.FROM) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("converter_from_unit_button"),
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Column {
                                    Text(
                                        text = state.fromUnit.symbol,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = state.fromUnit.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.converter_select_unit),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        EditableCursorDisplay(
                            text = state.inputText,
                            selection = state.inputSelection,
                            onSelectionChange = onSelectionChanged,
                            textStyle = MaterialTheme.typography.displaySmall.copy(
                                fontSize = if (state.inputText.length > 10) 21.sp else 27.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End
                            ),
                            cursorColor = MaterialTheme.colorScheme.primary,
                            placeholder = "0",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("converter_from_value")
                        )
                    }
                }

                // TO card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = calcColors.displayBackground,
                    border = BorderStroke(1.dp, calcColors.displayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(calcColors.displayGlowSecondary, Color.Transparent),
                                        center = Offset(size.width * 0.9f, size.height * 0.5f),
                                        radius = size.maxDimension * 0.55f
                                    )
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            onClick = { onOpenUnitPicker(UnitPickerTarget.TO) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("converter_to_unit_button"),
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Column {
                                    Text(
                                        text = state.toUnit.symbol,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = state.toUnit.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.converter_select_unit),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = state.convertedFormatted,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = if (state.convertedFormatted.length > 10) 21.sp else 27.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("converter_to_value")
                        )
                    }
                }
            }

            // Overlapping swap FAB centered vertically between cards
            Surface(
                onClick = onSwap,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 22.dp)
                    .size(48.dp)
                    .testTag("converter_swap_button"),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shadowElevation = 6.dp,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.background)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.SwapVert,
                        contentDescription = stringResource(R.string.cd_swap_units),
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { rotationZ = swapRotation }
                    )
                }
            }
        }

        // Live formula summary & quick actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = state.formulaSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                onClick = onSendToCalculator,
                modifier = Modifier
                    .heightIn(min = 38.dp)
                    .testTag("converter_send_to_calc"),
                shape = RoundedCornerShape(50),
                color = LocalCalcColors.current.sciKeyContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Calculate,
                        contentDescription = stringResource(R.string.cd_send_to_calc),
                        tint = LocalCalcColors.current.sciKeyContent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.converter_send_to_calc),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = LocalCalcColors.current.sciKeyContent
                    )
                }
            }

            IconButton(
                onClick = onCopyConverted,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("converter_copy_button")
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
}

@Composable
private fun AllUnitsStrip(
    state: ConverterUiState,
    alwaysExpanded: Boolean,
    onToggle: () -> Unit,
    onCopyValue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val expanded = alwaysExpanded || state.showAllConversions
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !alwaysExpanded, onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("converter_all_units_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.converter_all_units)} (${state.allConversions.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!alwaysExpanded) {
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = stringResource(R.string.converter_all_units),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.allConversions, key = { it.unit.id }) { entry ->
                        Surface(
                            onClick = { onCopyValue("${entry.formattedValue} ${entry.unit.symbol}") },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.unit.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = entry.unit.symbol,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = entry.formattedValue,
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConverterNumericKeypad(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onToggleSign: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calcColors = LocalCalcColors.current
    val shape = RoundedCornerShape(50)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val digitRows = listOf(
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3")
        )
        digitRows.forEachIndexed { idx, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { d ->
                    CalcKeypadButton(
                        label = d,
                        containerColor = calcColors.numKeyContainer,
                        contentColor = calcColors.numKeyContent,
                        borderColor = calcColors.numKeyBorder,
                        onClick = { onDigit(d) },
                        shape = shape,
                        fontSizeSp = 22f,
                        testTag = "conv_key_$d",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                when (idx) {
                    0 -> CalcKeypadButton(
                        label = "AC",
                        containerColor = calcColors.clearKeyContainer,
                        contentColor = calcColors.clearKeyContent,
                        onClick = onClear,
                        shape = shape,
                        fontSizeSp = 17f,
                        testTag = "conv_key_ac",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    1 -> CalcKeypadButton(
                        label = "DEL",
                        containerColor = calcColors.opKeyContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        onClick = onBackspace,
                        shape = shape,
                        icon = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = stringResource(R.string.cd_backspace),
                        testTag = "conv_key_backspace",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    else -> CalcKeypadButton(
                        label = "±",
                        containerColor = calcColors.opKeyContainer,
                        contentColor = calcColors.opKeyContent,
                        onClick = onToggleSign,
                        shape = shape,
                        fontSizeSp = 21f,
                        testTag = "conv_key_sign",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }

        // Bottom row: 00, 0, .
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcKeypadButton(
                label = "00",
                containerColor = calcColors.numKeyContainer,
                contentColor = calcColors.numKeyContent,
                borderColor = calcColors.numKeyBorder,
                onClick = {
                    onDigit("0")
                    onDigit("0")
                },
                shape = shape,
                fontSizeSp = 20f,
                testTag = "conv_key_00",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = "0",
                containerColor = calcColors.numKeyContainer,
                contentColor = calcColors.numKeyContent,
                borderColor = calcColors.numKeyBorder,
                onClick = { onDigit("0") },
                shape = shape,
                fontSizeSp = 22f,
                testTag = "conv_key_0",
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = ".",
                containerColor = calcColors.numKeyContainer,
                contentColor = calcColors.numKeyContent,
                borderColor = calcColors.numKeyBorder,
                onClick = onDecimal,
                shape = shape,
                fontSizeSp = 24f,
                testTag = "conv_key_dot",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitPickerSheet(
    units: List<MeasurementUnit>,
    selectedUnitId: String,
    onSelect: (MeasurementUnit) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered = remember(units, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) units else units.filter {
            it.name.lowercase().contains(q) || it.symbol.lowercase().contains(q)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.converter_select_unit),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.converter_search_units)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("unit_picker_search")
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.id }) { unit ->
                    val isSelected = unit.id == selectedUnitId
                    Surface(
                        onClick = { onSelect(unit) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unit_option_${unit.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = unit.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = unit.symbol,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    }
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun categoryIcon(category: UnitCategory): ImageVector = when (category) {
    UnitCategory.CURRENCY -> Icons.Outlined.AttachMoney
    UnitCategory.LENGTH -> Icons.Outlined.Straighten
    UnitCategory.MASS -> Icons.Outlined.Scale
    UnitCategory.TEMPERATURE -> Icons.Outlined.DeviceThermostat
    UnitCategory.AREA -> Icons.Outlined.SquareFoot
    UnitCategory.VOLUME -> Icons.Outlined.WaterDrop
    UnitCategory.SPEED -> Icons.Outlined.Speed
    UnitCategory.TIME -> Icons.Outlined.Schedule
    UnitCategory.DATA -> Icons.Outlined.Storage
    UnitCategory.FORCE -> Icons.Outlined.FitnessCenter
    UnitCategory.ENERGY -> Icons.Outlined.Bolt
    UnitCategory.POWER -> Icons.Outlined.Power
    UnitCategory.PRESSURE -> Icons.Outlined.TireRepair
    UnitCategory.ANGLE -> Icons.Outlined.RotateRight
    UnitCategory.FREQUENCY -> Icons.Outlined.GraphicEq
    UnitCategory.ELECTRIC_CHARGE -> Icons.Outlined.ElectricalServices
    UnitCategory.RADIATION -> Icons.Outlined.WarningAmber
    UnitCategory.AMOUNT_OF_SUBSTANCE -> Icons.Outlined.Science
    UnitCategory.CONCENTRATION -> Icons.Outlined.Biotech
    UnitCategory.DENSITY -> Icons.Outlined.Opacity
    UnitCategory.VISCOSITY -> Icons.Outlined.WaterDrop
}
