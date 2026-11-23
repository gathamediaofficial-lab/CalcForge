package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.domain.ActivityLevel
import com.example.domain.BiologicalSex
import com.example.domain.DateCalculatorEngine
import com.example.domain.DateDiffResult
import com.example.domain.DateOffsetUnit
import com.example.domain.HealthCalculatorEngine
import com.example.domain.HealthMetricsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ToolsSubTab {
    DATE, HEALTH
}

enum class DateCalcMode {
    BETWEEN_DATES, ADD_SUBTRACT
}

enum class DatePickerTarget {
    START, END, OFFSET_BASE
}

data class ToolsUiState(
    val activeTab: ToolsSubTab = ToolsSubTab.DATE,
    // Date Calculator
    val dateCalcMode: DateCalcMode = DateCalcMode.BETWEEN_DATES,
    val startDateMillis: Long = DateCalculatorEngine.todayUtcMillis(),
    val endDateMillis: Long = DateCalculatorEngine.addOffset(
        DateCalculatorEngine.todayUtcMillis(), 30, DateOffsetUnit.DAYS
    ),
    val includeEndDate: Boolean = false,
    val dateDiff: DateDiffResult = DateCalculatorEngine.daysBetween(
        DateCalculatorEngine.todayUtcMillis(),
        DateCalculatorEngine.addOffset(DateCalculatorEngine.todayUtcMillis(), 30, DateOffsetUnit.DAYS),
        false
    ),
    val offsetBaseMillis: Long = DateCalculatorEngine.todayUtcMillis(),
    val offsetAmountText: String = "45",
    val offsetUnit: DateOffsetUnit = DateOffsetUnit.DAYS,
    val isSubtractOffset: Boolean = false,
    val offsetResultMillis: Long = DateCalculatorEngine.addOffset(
        DateCalculatorEngine.todayUtcMillis(), 45, DateOffsetUnit.DAYS, false
    ),
    val activeDatePicker: DatePickerTarget? = null,
    // Health Calculator
    val useMetricUnits: Boolean = true,
    val sex: BiologicalSex = BiologicalSex.MALE,
    val ageText: String = "28",
    val heightPrimaryText: String = "175", // cm when metric, feet when imperial
    val heightInchesText: String = "9",    // inches when imperial
    val weightText: String = "72",         // kg when metric, lb when imperial
    val waistText: String = "82",          // cm or in
    val neckText: String = "38",           // cm or in
    val hipText: String = "96",            // cm or in (female US Navy)
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val healthResult: HealthMetricsResult? = null
)

class ToolsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(recomputeHealth(ToolsUiState()))
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    fun selectTab(tab: ToolsSubTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // --- Date Calculator Actions ---

    fun selectDateMode(mode: DateCalcMode) {
        _uiState.update { it.copy(dateCalcMode = mode) }
    }

    fun openDatePicker(target: DatePickerTarget) {
        _uiState.update { it.copy(activeDatePicker = target) }
    }

    fun dismissDatePicker() {
        _uiState.update { it.copy(activeDatePicker = null) }
    }

    fun onDatePicked(utcMillis: Long) {
        val normalized = DateCalculatorEngine.normalizeToUtcMidnight(utcMillis)
        _uiState.update { state ->
            when (state.activeDatePicker) {
                DatePickerTarget.START -> recomputeDateDiff(
                    state.copy(startDateMillis = normalized, activeDatePicker = null)
                )
                DatePickerTarget.END -> recomputeDateDiff(
                    state.copy(endDateMillis = normalized, activeDatePicker = null)
                )
                DatePickerTarget.OFFSET_BASE -> recomputeOffset(
                    state.copy(offsetBaseMillis = normalized, activeDatePicker = null)
                )
                null -> state
            }
        }
    }

    fun swapDates() {
        _uiState.update { state ->
            recomputeDateDiff(
                state.copy(
                    startDateMillis = state.endDateMillis,
                    endDateMillis = state.startDateMillis
                )
            )
        }
    }

    fun toggleIncludeEndDate() {
        _uiState.update { state ->
            recomputeDateDiff(state.copy(includeEndDate = !state.includeEndDate))
        }
    }

    fun applyQuickDateRange(amount: Int, unit: DateOffsetUnit) {
        val today = DateCalculatorEngine.todayUtcMillis()
        val end = DateCalculatorEngine.addOffset(today, amount, unit, subtract = false)
        _uiState.update { state ->
            recomputeDateDiff(state.copy(startDateMillis = today, endDateMillis = end))
        }
    }

    fun onOffsetAmountChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(5)
        _uiState.update { state ->
            recomputeOffset(state.copy(offsetAmountText = filtered))
        }
    }

    fun selectOffsetUnit(unit: DateOffsetUnit) {
        _uiState.update { state ->
            recomputeOffset(state.copy(offsetUnit = unit))
        }
    }

    fun setSubtractOffset(subtract: Boolean) {
        _uiState.update { state ->
            recomputeOffset(state.copy(isSubtractOffset = subtract))
        }
    }

    private fun recomputeDateDiff(state: ToolsUiState): ToolsUiState {
        return state.copy(
            dateDiff = DateCalculatorEngine.daysBetween(
                state.startDateMillis,
                state.endDateMillis,
                state.includeEndDate
            )
        )
    }

    private fun recomputeOffset(state: ToolsUiState): ToolsUiState {
        val amount = state.offsetAmountText.toIntOrNull() ?: 0
        return state.copy(
            offsetResultMillis = DateCalculatorEngine.addOffset(
                state.offsetBaseMillis,
                amount,
                state.offsetUnit,
                state.isSubtractOffset
            )
        )
    }

    // --- Health Calculator Actions ---

    fun toggleUnitSystem(metric: Boolean) {
        _uiState.update { state ->
            if (state.useMetricUnits == metric) return@update state
            if (metric) {
                // Convert imperial inputs to metric
                val ft = state.heightPrimaryText.toDoubleOrNull() ?: 5.0
                val inches = state.heightInchesText.toDoubleOrNull() ?: 9.0
                val cm = ((ft * 12.0 + inches) * 2.54).toInt().toString()
                val kg = state.weightText.toDoubleOrNull()?.let { String.format(java.util.Locale.US, "%.1f", it * 0.45359237) } ?: "72"
                val waistCm = state.waistText.toDoubleOrNull()?.let { String.format(java.util.Locale.US, "%.1f", it * 2.54) } ?: ""
                val neckCm = state.neckText.toDoubleOrNull()?.let { String.format(java.util.Locale.US, "%.1f", it * 2.54) } ?: ""
                val hipCm = state.hipText.toDoubleOrNull()?.let { String.format(java.util.Locale.US, "%.1f", it * 2.54) } ?: ""
                recomputeHealth(
                    state.copy(
                        useMetricUnits = true,
                        heightPrimaryText = cm,
                        weightText = kg,
                        waistText = waistCm,
                        neckText = neckCm,
                        hipText = hipCm
                    )
                )
            } else {
                // Convert metric inputs to imperial
                val cm = state.heightPrimaryText.toDoubleOrNull() ?: 175.0
                val totalIn = cm / 2.54
                val ft = (totalIn / 12.0).toInt()
                val remIn = (totalIn - ft * 12.0).toInt()
                val lb = state.weightText.toDoubleOrNull()?.let { String.format(java.util.Locale.US, "%.1f", it / 0.45359237) } ?: "158"
                val waistIn = state.waistText.toDoubleOrNull()?.let { String.format(java.util.Locale.US, "%.1f", it / 2.54) } ?: ""
                val neckIn = state.neckText.toDoubleOrNull()?.let { String.format(java.util.Locale.US, "%.1f", it / 2.54) } ?: ""
                val hipIn = state.hipText.toDoubleOrNull()?.let { String.format(java.util.Locale.US, "%.1f", it / 2.54) } ?: ""
                recomputeHealth(
                    state.copy(
                        useMetricUnits = false,
                        heightPrimaryText = ft.toString(),
                        heightInchesText = remIn.toString(),
                        weightText = lb,
                        waistText = waistIn,
                        neckText = neckIn,
                        hipText = hipIn
                    )
                )
            }
        }
    }

    fun selectSex(sex: BiologicalSex) {
        _uiState.update { recomputeHealth(it.copy(sex = sex)) }
    }

    fun selectActivityLevel(level: ActivityLevel) {
        _uiState.update { recomputeHealth(it.copy(activityLevel = level)) }
    }

    fun onAgeChanged(value: String) {
        val clean = value.filter { it.isDigit() }.take(3)
        _uiState.update { recomputeHealth(it.copy(ageText = clean)) }
    }

    fun onHeightPrimaryChanged(value: String) {
        val clean = sanitizeDecimal(value, 5)
        _uiState.update { recomputeHealth(it.copy(heightPrimaryText = clean)) }
    }

    fun onHeightInchesChanged(value: String) {
        val clean = sanitizeDecimal(value, 4)
        _uiState.update { recomputeHealth(it.copy(heightInchesText = clean)) }
    }

    fun onWeightChanged(value: String) {
        val clean = sanitizeDecimal(value, 6)
        _uiState.update { recomputeHealth(it.copy(weightText = clean)) }
    }

    fun onWaistChanged(value: String) {
        val clean = sanitizeDecimal(value, 5)
        _uiState.update { recomputeHealth(it.copy(waistText = clean)) }
    }

    fun onNeckChanged(value: String) {
        val clean = sanitizeDecimal(value, 5)
        _uiState.update { recomputeHealth(it.copy(neckText = clean)) }
    }

    fun onHipChanged(value: String) {
        val clean = sanitizeDecimal(value, 5)
        _uiState.update { recomputeHealth(it.copy(hipText = clean)) }
    }

    private fun sanitizeDecimal(raw: String, maxLen: Int): String {
        var dotSeen = false
        return buildString {
            for (c in raw) {
                if (length >= maxLen) break
                if (c.isDigit()) {
                    append(c)
                } else if (c == '.' && !dotSeen) {
                    dotSeen = true
                    append(c)
                }
            }
        }
    }

    private fun recomputeHealth(state: ToolsUiState): ToolsUiState {
        val age = state.ageText.toIntOrNull() ?: return state.copy(healthResult = null)
        val heightCm = if (state.useMetricUnits) {
            state.heightPrimaryText.toDoubleOrNull() ?: return state.copy(healthResult = null)
        } else {
            val ft = state.heightPrimaryText.toDoubleOrNull() ?: 0.0
            val inches = state.heightInchesText.toDoubleOrNull() ?: 0.0
            (ft * 12.0 + inches) * 2.54
        }
        val weightKg = if (state.useMetricUnits) {
            state.weightText.toDoubleOrNull() ?: return state.copy(healthResult = null)
        } else {
            (state.weightText.toDoubleOrNull() ?: return state.copy(healthResult = null)) * 0.45359237
        }
        val lenFactor = if (state.useMetricUnits) 1.0 else 2.54
        val waistCm = state.waistText.toDoubleOrNull()?.times(lenFactor)
        val neckCm = state.neckText.toDoubleOrNull()?.times(lenFactor)
        val hipCm = state.hipText.toDoubleOrNull()?.times(lenFactor)

        val result = HealthCalculatorEngine.calculate(
            sex = state.sex,
            ageYears = age,
            heightCm = heightCm,
            weightKg = weightKg,
            waistCm = waistCm,
            neckCm = neckCm,
            hipCm = hipCm,
            activityLevel = state.activityLevel
        )
        return state.copy(healthResult = result)
    }
}
