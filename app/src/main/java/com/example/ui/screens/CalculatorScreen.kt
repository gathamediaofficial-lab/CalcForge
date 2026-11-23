package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AngleMode
import com.example.domain.MathEvaluator
import com.example.ui.components.CalcKeypadButton
import com.example.ui.components.EditableCursorDisplay
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCalcColors
import com.example.ui.viewmodel.CalculatorUiState

@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    onSelectionChanged: (TextRange) -> Unit,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onPostfix: (String) -> Unit,
    onParenthesis: (String) -> Unit,
    onReciprocal: () -> Unit,
    onToggleSign: () -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onEquals: () -> Unit,
    onToggleAngleMode: () -> Unit,
    onToggleScientific: () -> Unit,
    onToggleInverse: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryStore: () -> Unit,
    onCopyResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calcColors = LocalCalcColors.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        val isWideLayout = maxWidth >= 600.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorHeaderBar(
                angleMode = state.angleMode,
                isScientificExpanded = state.isScientificExpanded,
                isDarkMode = state.isDarkMode,
                showSciToggle = !isWideLayout,
                onToggleAngleMode = onToggleAngleMode,
                onToggleScientific = onToggleScientific,
                onToggleDarkMode = onToggleDarkMode
            )

            CalculatorDisplayPanel(
                state = state,
                onSelectionChanged = onSelectionChanged,
                onCopyResult = onCopyResult,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isWideLayout) 0.30f else if (state.isScientificExpanded) 0.27f else 0.36f)
            )

            if (isWideLayout) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.70f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.46f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MemoryUtilityRow(
                            hasMemory = state.hasMemory,
                            onMemoryClear = onMemoryClear,
                            onMemoryRecall = onMemoryRecall,
                            onMemoryAdd = onMemoryAdd,
                            onMemorySubtract = onMemorySubtract,
                            onMemoryStore = onMemoryStore,
                            onAns = { onConstant("Ans") }
                        )
                        ScientificKeypadGrid(
                            isInverseMode = state.isInverseMode,
                            onToggleInverse = onToggleInverse,
                            onFunction = onFunction,
                            onConstant = onConstant,
                            onOperator = onOperator,
                            onPostfix = onPostfix,
                            onParenthesis = onParenthesis,
                            onReciprocal = onReciprocal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BasicKeypadGrid(
                        onDigit = onDigit,
                        onDecimal = onDecimal,
                        onOperator = onOperator,
                        onPostfix = onPostfix,
                        onToggleSign = onToggleSign,
                        onBackspace = onBackspace,
                        onClearAll = onClearAll,
                        onEquals = onEquals,
                        modifier = Modifier
                            .weight(0.54f)
                            .fillMaxHeight()
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = state.isScientificExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MemoryUtilityRow(
                            hasMemory = state.hasMemory,
                            onMemoryClear = onMemoryClear,
                            onMemoryRecall = onMemoryRecall,
                            onMemoryAdd = onMemoryAdd,
                            onMemorySubtract = onMemorySubtract,
                            onMemoryStore = onMemoryStore,
                            onAns = { onConstant("Ans") }
                        )
                        ScientificKeypadGrid(
                            isInverseMode = state.isInverseMode,
                            onToggleInverse = onToggleInverse,
                            onFunction = onFunction,
                            onConstant = onConstant,
                            onOperator = onOperator,
                            onPostfix = onPostfix,
                            onParenthesis = onParenthesis,
                            onReciprocal = onReciprocal,
                            modifier = Modifier.height(166.dp)
                        )
                    }
                }

                BasicKeypadGrid(
                    onDigit = onDigit,
                    onDecimal = onDecimal,
                    onOperator = onOperator,
                    onPostfix = onPostfix,
                    onToggleSign = onToggleSign,
                    onBackspace = onBackspace,
                    onClearAll = onClearAll,
                    onEquals = onEquals,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (state.isScientificExpanded) 0.48f else 0.64f)
                )
            }
        }
    }
}

@Composable
private fun CalculatorHeaderBar(
    angleMode: AngleMode,
    isScientificExpanded: Boolean,
    isDarkMode: Boolean,
    showSciToggle: Boolean,
    onToggleAngleMode: () -> Unit,
    onToggleScientific: () -> Unit,
    onToggleDarkMode: () -> Unit
) {
    val calcColors = LocalCalcColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Functions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(18.dp)
                )
            }
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                onClick = onToggleAngleMode,
                modifier = Modifier
                    .heightIn(min = 38.dp)
                    .testTag("angle_mode_toggle"),
                shape = RoundedCornerShape(50),
                color = calcColors.sciKeyContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (angleMode == AngleMode.DEG) {
                            stringResource(R.string.mode_deg)
                        } else {
                            stringResource(R.string.mode_rad)
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                        color = calcColors.sciKeyContent
                    )
                }
            }

            if (showSciToggle) {
                Surface(
                    onClick = onToggleScientific,
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .testTag("sci_mode_toggle"),
                    shape = RoundedCornerShape(50),
                    color = if (isScientificExpanded) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isScientificExpanded) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isScientificExpanded) {
                                stringResource(R.string.mode_sci)
                            } else {
                                stringResource(R.string.mode_std)
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = if (isScientificExpanded) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = stringResource(R.string.cd_toggle_theme),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalculatorDisplayPanel(
    state: CalculatorUiState,
    onSelectionChanged: (TextRange) -> Unit,
    onCopyResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calcColors = LocalCalcColors.current

    val displayLen = state.expression.length
    val dynamicFontSize = when {
        displayLen > 22 -> 24.sp
        displayLen > 15 -> 30.sp
        displayLen > 10 -> 38.sp
        else -> 46.sp
    }

    val defaultInk = MaterialTheme.colorScheme.onSurface
    val operatorInk = MaterialTheme.colorScheme.primary
    val scientificInk = MaterialTheme.colorScheme.secondary

    Surface(
        modifier = modifier.clip(RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        color = calcColors.displayBackground,
        border = BorderStroke(1.dp, calcColors.displayBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(calcColors.displayGlowPrimary, Color.Transparent),
                            center = Offset(size.width * 0.92f, size.height * 0.15f),
                            radius = size.maxDimension * 0.65f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(calcColors.displayGlowSecondary, Color.Transparent),
                            center = Offset(size.width * 0.08f, size.height * 0.92f),
                            radius = size.maxDimension * 0.55f
                        )
                    )
                }
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Status row: memory indicator, previous expression, copy button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.hasMemory) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${stringResource(R.string.label_memory_active)} ${MathEvaluator.formatDisplayNumber(state.memoryValue)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (state.evaluatedExpression.isNotEmpty()) {
                            Text(
                                text = "${state.evaluatedExpression} =",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("evaluated_expression_text")
                            )
                        }
                    }

                    IconButton(
                        onClick = onCopyResult,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("copy_result_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = stringResource(R.string.cd_copy_result),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Editable expression line (tap or drag cursor anywhere) & live preview result
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EditableCursorDisplay(
                        text = state.expression,
                        selection = state.selection,
                        onSelectionChange = onSelectionChanged,
                        textStyle = MaterialTheme.typography.displayLarge.copy(
                            fontSize = dynamicFontSize,
                            lineHeight = (dynamicFontSize.value + 6f).sp,
                            color = defaultInk,
                            textAlign = TextAlign.End
                        ),
                        cursorColor = operatorInk,
                        placeholder = "0",
                        placeholderColor = defaultInk,
                        highlighter = { expr ->
                            buildHighlightedExpression(
                                expression = expr,
                                defaultColor = defaultInk,
                                operatorColor = operatorInk,
                                scientificColor = scientificInk
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calculator_display_expression")
                    )

                    val secondaryLineText = when {
                        state.errorMessage != null -> state.errorMessage
                        !state.justEvaluated && state.previewResult.isNotEmpty() -> "= ${state.previewResult}"
                        else -> ""
                    }
                    val secondaryColor = if (state.errorMessage != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }

                    Text(
                        text = secondaryLineText,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = secondaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .heightIn(min = 26.dp)
                            .testTag("calculator_live_preview")
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryUtilityRow(
    hasMemory: Boolean,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryStore: () -> Unit,
    onAns: () -> Unit
) {
    val calcColors = LocalCalcColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val items = listOf(
            Triple("MC", hasMemory, onMemoryClear),
            Triple("MR", hasMemory, onMemoryRecall),
            Triple("M+", true, onMemoryAdd),
            Triple("M−", true, onMemorySubtract),
            Triple("MS", true, onMemoryStore),
            Triple("Ans", true, onAns)
        )
        items.forEach { (label, enabled, action) ->
            Surface(
                onClick = action,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .testTag("mem_${label.lowercase()}"),
                shape = RoundedCornerShape(50),
                color = calcColors.memChipContainer,
                border = BorderStroke(1.dp, calcColors.subtleDivider)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (enabled) {
                            if (label == "Ans") MaterialTheme.colorScheme.secondary else calcColors.memChipContent
                        } else {
                            calcColors.memChipContent.copy(alpha = 0.35f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScientificKeypadGrid(
    isInverseMode: Boolean,
    onToggleInverse: () -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onOperator: (String) -> Unit,
    onPostfix: (String) -> Unit,
    onParenthesis: (String) -> Unit,
    onReciprocal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calcColors = LocalCalcColors.current
    val sciShape = RoundedCornerShape(50)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: 2nd, sin/sin⁻¹, cos/cos⁻¹, tan/tan⁻¹, ln/eˣ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKeypadButton(
                label = "2nd",
                containerColor = if (isInverseMode) calcColors.sciKeyActiveContainer else calcColors.sciKeyContainer,
                contentColor = if (isInverseMode) calcColors.sciKeyActiveContent else calcColors.sciKeyContent,
                borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = if (isInverseMode) 0.7f else 0.2f),
                onClick = onToggleInverse,
                shape = sciShape,
                fontSizeSp = 13f,
                minHeight = 44.dp,
                testTag = "key_2nd",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = if (isInverseMode) "sin⁻¹" else "sin",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onFunction(if (isInverseMode) "sin⁻¹(" else "sin(") },
                shape = sciShape,
                fontSizeSp = 13f,
                minHeight = 44.dp,
                testTag = "key_sin",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = if (isInverseMode) "cos⁻¹" else "cos",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onFunction(if (isInverseMode) "cos⁻¹(" else "cos(") },
                shape = sciShape,
                fontSizeSp = 13f,
                minHeight = 44.dp,
                testTag = "key_cos",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = if (isInverseMode) "tan⁻¹" else "tan",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onFunction(if (isInverseMode) "tan⁻¹(" else "tan(") },
                shape = sciShape,
                fontSizeSp = 13f,
                minHeight = 44.dp,
                testTag = "key_tan",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = if (isInverseMode) "eˣ" else "ln",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onFunction(if (isInverseMode) "e^(" else "ln(") },
                shape = sciShape,
                fontSizeSp = 13f,
                minHeight = 44.dp,
                testTag = "key_ln",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 2: log/10ˣ, √/∛, x²/x³, xʸ, 1/x
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKeypadButton(
                label = if (isInverseMode) "10ˣ" else "log",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onFunction(if (isInverseMode) "10^(" else "log(") },
                shape = sciShape,
                fontSizeSp = 13f,
                minHeight = 44.dp,
                testTag = "key_log",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = if (isInverseMode) "∛" else "√",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onFunction(if (isInverseMode) "∛(" else "√(") },
                shape = sciShape,
                fontSizeSp = 15f,
                minHeight = 44.dp,
                testTag = "key_sqrt",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = if (isInverseMode) "x³" else "x²",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onPostfix(if (isInverseMode) "^3" else "^2") },
                shape = sciShape,
                fontSizeSp = 14f,
                minHeight = 44.dp,
                testTag = "key_square",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = "xʸ",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onOperator("^") },
                shape = sciShape,
                fontSizeSp = 14f,
                minHeight = 44.dp,
                testTag = "key_power",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = "1/x",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = onReciprocal,
                shape = sciShape,
                fontSizeSp = 13f,
                minHeight = 44.dp,
                testTag = "key_reciprocal",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 3: π, e, n!, (, )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKeypadButton(
                label = "π",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onConstant("π") },
                shape = sciShape,
                fontSizeSp = 16f,
                minHeight = 44.dp,
                testTag = "key_pi",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = "e",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onConstant("e") },
                shape = sciShape,
                fontSizeSp = 15f,
                minHeight = 44.dp,
                testTag = "key_e",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = if (isInverseMode) "|x|" else "n!",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = {
                    if (isInverseMode) onFunction("abs(") else onPostfix("!")
                },
                shape = sciShape,
                fontSizeSp = 13f,
                minHeight = 44.dp,
                testTag = "key_factorial",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = "(",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onParenthesis("(") },
                shape = sciShape,
                fontSizeSp = 16f,
                minHeight = 44.dp,
                testTag = "key_lparen",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = ")",
                containerColor = calcColors.sciKeyContainer,
                contentColor = calcColors.sciKeyContent,
                onClick = { onParenthesis(")") },
                shape = sciShape,
                fontSizeSp = 16f,
                minHeight = 44.dp,
                testTag = "key_rparen",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun BasicKeypadGrid(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onPostfix: (String) -> Unit,
    onToggleSign: () -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onEquals: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calcColors = LocalCalcColors.current
    val keyShape = RoundedCornerShape(50)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: AC, ⌫, %, ÷
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcKeypadButton(
                label = "AC",
                containerColor = calcColors.clearKeyContainer,
                contentColor = calcColors.clearKeyContent,
                onClick = onClearAll,
                shape = keyShape,
                fontSizeSp = 18f,
                testTag = "key_ac",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = "DEL",
                containerColor = calcColors.opKeyContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = onBackspace,
                shape = keyShape,
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(R.string.cd_backspace),
                testTag = "key_backspace",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = "%",
                containerColor = calcColors.opKeyContainer,
                contentColor = calcColors.opKeyContent,
                onClick = { onPostfix("%") },
                shape = keyShape,
                fontSizeSp = 20f,
                testTag = "key_percent",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = "÷",
                containerColor = calcColors.opKeyContainer,
                contentColor = calcColors.opKeyContent,
                onClick = { onOperator("÷") },
                shape = keyShape,
                fontSizeSp = 24f,
                testTag = "key_div",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("7", "8", "9").forEach { d ->
                CalcKeypadButton(
                    label = d,
                    containerColor = calcColors.numKeyContainer,
                    contentColor = calcColors.numKeyContent,
                    borderColor = calcColors.numKeyBorder,
                    onClick = { onDigit(d) },
                    shape = keyShape,
                    fontSizeSp = 22f,
                    testTag = "key_$d",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            CalcKeypadButton(
                label = "×",
                containerColor = calcColors.opKeyContainer,
                contentColor = calcColors.opKeyContent,
                onClick = { onOperator("×") },
                shape = keyShape,
                fontSizeSp = 24f,
                testTag = "key_mul",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("4", "5", "6").forEach { d ->
                CalcKeypadButton(
                    label = d,
                    containerColor = calcColors.numKeyContainer,
                    contentColor = calcColors.numKeyContent,
                    borderColor = calcColors.numKeyBorder,
                    onClick = { onDigit(d) },
                    shape = keyShape,
                    fontSizeSp = 22f,
                    testTag = "key_$d",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            CalcKeypadButton(
                label = "−",
                containerColor = calcColors.opKeyContainer,
                contentColor = calcColors.opKeyContent,
                onClick = { onOperator("−") },
                shape = keyShape,
                fontSizeSp = 24f,
                testTag = "key_sub",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("1", "2", "3").forEach { d ->
                CalcKeypadButton(
                    label = d,
                    containerColor = calcColors.numKeyContainer,
                    contentColor = calcColors.numKeyContent,
                    borderColor = calcColors.numKeyBorder,
                    onClick = { onDigit(d) },
                    shape = keyShape,
                    fontSizeSp = 22f,
                    testTag = "key_$d",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            CalcKeypadButton(
                label = "+",
                containerColor = calcColors.opKeyContainer,
                contentColor = calcColors.opKeyContent,
                onClick = { onOperator("+") },
                shape = keyShape,
                fontSizeSp = 24f,
                testTag = "key_add",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 5: ±, 0, ., =
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcKeypadButton(
                label = "±",
                containerColor = calcColors.numKeyContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                borderColor = calcColors.numKeyBorder,
                onClick = onToggleSign,
                shape = keyShape,
                fontSizeSp = 20f,
                testTag = "key_sign",
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
                shape = keyShape,
                fontSizeSp = 22f,
                testTag = "key_0",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = ".",
                containerColor = calcColors.numKeyContainer,
                contentColor = calcColors.numKeyContent,
                borderColor = calcColors.numKeyBorder,
                onClick = onDecimal,
                shape = keyShape,
                fontSizeSp = 24f,
                testTag = "key_dot",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalcKeypadButton(
                label = "=",
                containerColor = calcColors.equalsKeyContainer,
                contentColor = calcColors.equalsKeyContent,
                onClick = onEquals,
                shape = keyShape,
                fontSizeSp = 26f,
                fontWeight = FontWeight.Bold,
                testTag = "key_equals",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

private fun buildHighlightedExpression(
    expression: String,
    defaultColor: Color,
    operatorColor: Color,
    scientificColor: Color
): AnnotatedString = buildAnnotatedString {
    val operators = setOf('+', '−', '-', '×', '÷', '^', '%')
    val sciChars = setOf('π', '√', '∛', '!', '(', ')')
    var i = 0
    while (i < expression.length) {
        val c = expression[i]
        when {
            c in operators -> {
                withStyle(SpanStyle(color = operatorColor)) { append(c) }
                i++
            }
            c in sciChars -> {
                withStyle(SpanStyle(color = scientificColor)) { append(c) }
                i++
            }
            c.isLetter() -> {
                val start = i
                while (i < expression.length && (expression[i].isLetter() || expression[i] == '⁻' || expression[i] == '¹' || expression[i] == '₂')) {
                    i++
                }
                val token = expression.substring(start, i)
                val color = if (token == "E") defaultColor else scientificColor
                withStyle(SpanStyle(color = color)) { append(token) }
            }
            else -> {
                withStyle(SpanStyle(color = defaultColor)) { append(c) }
                i++
            }
        }
    }
}
