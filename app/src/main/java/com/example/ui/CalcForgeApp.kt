package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.LocalHapticsEnabled
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.ConverterViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.ToolsViewModel
import kotlinx.coroutines.launch

enum class CalcDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CALCULATOR(
        route = "calculator",
        labelRes = R.string.nav_calculator,
        selectedIcon = Icons.Filled.Calculate,
        unselectedIcon = Icons.Outlined.Calculate,
        testTag = "nav_tab_calculator"
    ),
    CONVERTER(
        route = "converter",
        labelRes = R.string.nav_converter,
        selectedIcon = Icons.Filled.SwapHoriz,
        unselectedIcon = Icons.Outlined.SwapHoriz,
        testTag = "nav_tab_converter"
    ),
    TOOLS(
        route = "tools",
        labelRes = R.string.nav_tools,
        selectedIcon = Icons.Filled.GridView,
        unselectedIcon = Icons.Outlined.GridView,
        testTag = "nav_tab_tools"
    ),
    HISTORY(
        route = "history",
        labelRes = R.string.nav_history,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        testTag = "nav_tab_history"
    ),
    SETTINGS(
        route = "settings",
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_tab_settings"
    )
}

@Composable
fun CalcForgeApp(
    calculatorViewModel: CalculatorViewModel,
    converterViewModel: ConverterViewModel,
    toolsViewModel: ToolsViewModel,
    historyViewModel: HistoryViewModel
) {
    val calcState by calculatorViewModel.uiState.collectAsStateWithLifecycle()
    val converterState by converterViewModel.uiState.collectAsStateWithLifecycle()
    val toolsState by toolsViewModel.uiState.collectAsStateWithLifecycle()
    val historyState by historyViewModel.uiState.collectAsStateWithLifecycle()

    var currentDestination by rememberSaveable { mutableStateOf(CalcDestination.CALCULATOR) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val sentMsg = stringResource(R.string.sent_to_calculator)
    val memSavedMsg = stringResource(R.string.memory_saved)
    val memClearedMsg = stringResource(R.string.memory_cleared)

    fun showFeedback(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    fun copyToClipboard(text: String, label: String = "CalcForge Result") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
        val preview = if (text.length > 28) "${text.take(28)}…" else text
        showFeedback(context.getString(R.string.copied_value_to_clipboard, preview))
    }

    // Handle system Back button cleanly on secondary tabs
    BackHandler(enabled = currentDestination != CalcDestination.CALCULATOR) {
        currentDestination = CalcDestination.CALCULATOR
    }

    CompositionLocalProvider(LocalHapticsEnabled provides calcState.hapticsEnabled) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavRail = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useNavRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            CalcDestination.entries.forEach { dest ->
                                val selected = dest == currentDestination
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(dest.labelRes),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxHeight()
                                .statusBarsPadding()
                        ) {
                            CalcDestination.entries.forEach { dest ->
                                val selected = dest == currentDestination
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(dest.labelRes),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (currentDestination) {
                            CalcDestination.CALCULATOR -> CalculatorScreen(
                                state = calcState,
                                onSelectionChanged = calculatorViewModel::onSelectionChanged,
                                onDigit = calculatorViewModel::onDigit,
                                onDecimal = calculatorViewModel::onDecimalPoint,
                                onOperator = calculatorViewModel::onOperator,
                                onFunction = calculatorViewModel::onFunction,
                                onConstant = calculatorViewModel::onConstant,
                                onPostfix = calculatorViewModel::onPostfix,
                                onParenthesis = calculatorViewModel::onParenthesis,
                                onReciprocal = calculatorViewModel::onReciprocal,
                                onToggleSign = calculatorViewModel::onToggleSign,
                                onBackspace = calculatorViewModel::onBackspace,
                                onClearAll = calculatorViewModel::onClearAll,
                                onEquals = calculatorViewModel::onEquals,
                                onToggleAngleMode = calculatorViewModel::toggleAngleMode,
                                onToggleScientific = calculatorViewModel::toggleScientificExpanded,
                                onToggleInverse = calculatorViewModel::toggleInverseMode,
                                onToggleDarkMode = calculatorViewModel::toggleDarkMode,
                                onMemoryClear = {
                                    calculatorViewModel.onMemoryClear()
                                    showFeedback(memClearedMsg)
                                },
                                onMemoryRecall = calculatorViewModel::onMemoryRecall,
                                onMemoryAdd = {
                                    if (calculatorViewModel.onMemoryAdd()) showFeedback(memSavedMsg)
                                },
                                onMemorySubtract = {
                                    if (calculatorViewModel.onMemorySubtract()) showFeedback(memSavedMsg)
                                },
                                onMemoryStore = {
                                    if (calculatorViewModel.onMemoryStore()) showFeedback(memSavedMsg)
                                },
                                onCopyResult = {
                                    calculatorViewModel.currentCopyableResult()?.let {
                                        copyToClipboard(it, "CalcForge Result")
                                    }
                                }
                            )

                            CalcDestination.CONVERTER -> ConverterScreen(
                                state = converterState,
                                isDarkMode = calcState.isDarkMode,
                                onToggleDarkMode = calculatorViewModel::toggleDarkMode,
                                onSelectCategory = converterViewModel::selectCategory,
                                onOpenUnitPicker = converterViewModel::openUnitPicker,
                                onDismissUnitPicker = converterViewModel::dismissUnitPicker,
                                onSelectUnit = converterViewModel::selectUnit,
                                onSwapUnits = converterViewModel::swapUnits,
                                onToggleAllConversions = converterViewModel::toggleAllConversions,
                                onSelectionChanged = converterViewModel::onSelectionChanged,
                                onDigit = converterViewModel::onDigit,
                                onDecimal = converterViewModel::onDecimalPoint,
                                onToggleSign = converterViewModel::onToggleSign,
                                onBackspace = converterViewModel::onBackspace,
                                onClear = converterViewModel::onClear,
                                onCopyConverted = { copyToClipboard(it, "CalcForge Conversion") },
                                onSendToCalculator = { value ->
                                    calculatorViewModel.insertValueFromExternal(value)
                                    currentDestination = CalcDestination.CALCULATOR
                                    showFeedback(sentMsg)
                                }
                            )

                            CalcDestination.TOOLS -> ToolsScreen(
                                state = toolsState,
                                onSelectTab = toolsViewModel::selectTab,
                                onSelectDateMode = toolsViewModel::selectDateMode,
                                onOpenDatePicker = toolsViewModel::openDatePicker,
                                onDismissDatePicker = toolsViewModel::dismissDatePicker,
                                onDatePicked = toolsViewModel::onDatePicked,
                                onSwapDates = toolsViewModel::swapDates,
                                onToggleIncludeEndDate = toolsViewModel::toggleIncludeEndDate,
                                onApplyQuickDateRange = toolsViewModel::applyQuickDateRange,
                                onOffsetAmountChanged = toolsViewModel::onOffsetAmountChanged,
                                onSelectOffsetUnit = toolsViewModel::selectOffsetUnit,
                                onSetSubtractOffset = toolsViewModel::setSubtractOffset,
                                onToggleUnitSystem = toolsViewModel::toggleUnitSystem,
                                onSelectSex = toolsViewModel::selectSex,
                                onSelectActivityLevel = toolsViewModel::selectActivityLevel,
                                onAgeChanged = toolsViewModel::onAgeChanged,
                                onHeightPrimaryChanged = toolsViewModel::onHeightPrimaryChanged,
                                onHeightInchesChanged = toolsViewModel::onHeightInchesChanged,
                                onWeightChanged = toolsViewModel::onWeightChanged,
                                onWaistChanged = toolsViewModel::onWaistChanged,
                                onNeckChanged = toolsViewModel::onNeckChanged,
                                onHipChanged = toolsViewModel::onHipChanged,
                                onCopyValue = { copyToClipboard(it, "CalcForge Tool Result") },
                                onSendDaysToCalculator = { days ->
                                    calculatorViewModel.insertValueFromExternal(days)
                                    currentDestination = CalcDestination.CALCULATOR
                                    showFeedback(sentMsg)
                                }
                            )

                            CalcDestination.HISTORY -> HistoryScreen(
                                state = historyState,
                                onSearchQueryChanged = historyViewModel::onSearchQueryChanged,
                                onFilterSelected = historyViewModel::onFilterSelected,
                                onTogglePin = historyViewModel::togglePin,
                                onDeleteEntry = historyViewModel::deleteEntry,
                                onReuseExpression = { expr ->
                                    calculatorViewModel.loadExpressionFromExternal(expr)
                                    currentDestination = CalcDestination.CALCULATOR
                                },
                                onUseResult = { rawValue ->
                                    calculatorViewModel.insertValueFromExternal(rawValue)
                                    currentDestination = CalcDestination.CALCULATOR
                                    showFeedback(sentMsg)
                                },
                                onCopyResult = { resultText ->
                                    copyToClipboard(resultText, "CalcForge History Result")
                                },
                                onCopyEquation = { equationText ->
                                    copyToClipboard(equationText, "CalcForge Equation")
                                },
                                onOpenClearDialog = historyViewModel::openClearDialog,
                                onDismissClearDialog = historyViewModel::dismissClearDialog,
                                onClearUnpinned = historyViewModel::clearUnpinned,
                                onClearAll = historyViewModel::clearAll
                            )

                            CalcDestination.SETTINGS -> SettingsScreen(
                                calcState = calcState,
                                onToggleDarkMode = calculatorViewModel::toggleDarkMode,
                                onToggleAngleMode = calculatorViewModel::toggleAngleMode,
                                onToggleScientificDefault = calculatorViewModel::toggleScientificExpanded,
                                onToggleHaptics = calculatorViewModel::toggleHaptics
                            )
                        }
                    }
                }
            }
        }
    }
}
