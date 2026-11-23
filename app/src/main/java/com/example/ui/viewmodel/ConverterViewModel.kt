package com.example.ui.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import com.example.domain.ConversionEntry
import com.example.domain.MeasurementUnit
import com.example.domain.UnitCategory
import com.example.domain.UnitConverterEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class UnitPickerTarget {
    FROM, TO
}

data class ConverterUiState(
    val selectedCategory: UnitCategory = UnitCategory.LENGTH,
    val fromUnit: MeasurementUnit = UnitConverterEngine.defaultPair(UnitCategory.LENGTH).first,
    val toUnit: MeasurementUnit = UnitConverterEngine.defaultPair(UnitCategory.LENGTH).second,
    val inputText: String = "1",
    val inputSelection: TextRange = TextRange(1),
    val convertedFormatted: String = "",
    val convertedRaw: Double = 0.0,
    val formulaSummary: String = "",
    val allConversions: List<ConversionEntry> = emptyList(),
    val pickerTarget: UnitPickerTarget? = null,
    val showAllConversions: Boolean = false
)

class ConverterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(recompute(ConverterUiState()))
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    fun selectCategory(category: UnitCategory) {
        val (defaultFrom, defaultTo) = UnitConverterEngine.defaultPair(category)
        _uiState.update { state ->
            recompute(
                state.copy(
                    selectedCategory = category,
                    fromUnit = defaultFrom,
                    toUnit = defaultTo,
                    pickerTarget = null
                )
            )
        }
    }

    fun openUnitPicker(target: UnitPickerTarget) {
        _uiState.update { it.copy(pickerTarget = target) }
    }

    fun dismissUnitPicker() {
        _uiState.update { it.copy(pickerTarget = null) }
    }

    fun selectUnit(unit: MeasurementUnit) {
        _uiState.update { state ->
            val updated = when (state.pickerTarget) {
                UnitPickerTarget.FROM -> state.copy(fromUnit = unit, pickerTarget = null)
                UnitPickerTarget.TO -> state.copy(toUnit = unit, pickerTarget = null)
                null -> state
            }
            recompute(updated)
        }
    }

    fun swapUnits() {
        _uiState.update { state ->
            recompute(
                state.copy(
                    fromUnit = state.toUnit,
                    toUnit = state.fromUnit
                )
            )
        }
    }

    fun toggleAllConversions() {
        _uiState.update { it.copy(showAllConversions = !it.showAllConversions) }
    }

    fun onSelectionChanged(selection: TextRange) {
        _uiState.update { state ->
            state.copy(inputSelection = clampSelection(selection, state.inputText.length))
        }
    }

    fun onDigit(digit: String) {
        _uiState.update { state ->
            val text = state.inputText
            val sel = clampSelection(state.inputSelection, text.length)
            val start = sel.min
            val end = sel.max

            // Replace standalone "0" or "-0" cleanly when typing at the end
            if (text == "0" && start == 1 && end == 1) {
                return@update recompute(state.copy(inputText = digit, inputSelection = TextRange(digit.length)))
            }
            if (text == "-0" && start == 2 && end == 2) {
                val next = "-$digit"
                return@update recompute(state.copy(inputText = next, inputSelection = TextRange(next.length)))
            }

            val next = text.substring(0, start) + digit + text.substring(end)
            if (next.length > 16) return@update state
            recompute(
                state.copy(
                    inputText = next,
                    inputSelection = TextRange(start + digit.length)
                )
            )
        }
    }

    fun onDecimalPoint() {
        _uiState.update { state ->
            val text = state.inputText
            val sel = clampSelection(state.inputSelection, text.length)
            val start = sel.min
            val end = sel.max
            val remainingWithoutSelection = text.substring(0, start) + text.substring(end)
            if (remainingWithoutSelection.contains('.')) return@update state

            val prefix = text.substring(0, start)
            val insertion = if (prefix.isEmpty() || prefix == "-") "0." else "."
            val next = prefix + insertion + text.substring(end)
            recompute(
                state.copy(
                    inputText = next,
                    inputSelection = TextRange(start + insertion.length)
                )
            )
        }
    }

    fun onToggleSign() {
        _uiState.update { state ->
            val text = state.inputText
            val sel = clampSelection(state.inputSelection, text.length)
            val (next, nextSel) = if (text.startsWith("-")) {
                text.drop(1) to TextRange(
                    (sel.start - 1).coerceAtLeast(0),
                    (sel.end - 1).coerceAtLeast(0)
                )
            } else if (text == "0") {
                "-" to TextRange(1)
            } else {
                "-$text" to TextRange(sel.start + 1, sel.end + 1)
            }
            recompute(
                state.copy(
                    inputText = next,
                    inputSelection = clampSelection(nextSel, next.length)
                )
            )
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            val text = state.inputText
            val sel = clampSelection(state.inputSelection, text.length)
            val (afterDelete, nextCursor) = if (!sel.collapsed) {
                text.removeRange(sel.min, sel.max) to sel.min
            } else {
                val cursor = sel.start
                if (cursor == 0) return@update state
                text.removeRange(cursor - 1, cursor) to (cursor - 1)
            }

            if (afterDelete.isEmpty() || afterDelete == "-") {
                recompute(state.copy(inputText = "0", inputSelection = TextRange(1)))
            } else {
                recompute(
                    state.copy(
                        inputText = afterDelete,
                        inputSelection = TextRange(nextCursor.coerceIn(0, afterDelete.length))
                    )
                )
            }
        }
    }

    fun onClear() {
        _uiState.update { state ->
            recompute(state.copy(inputText = "0", inputSelection = TextRange(1)))
        }
    }

    private fun clampSelection(sel: TextRange, length: Int): TextRange = TextRange(
        start = sel.start.coerceIn(0, length),
        end = sel.end.coerceIn(0, length)
    )

    private fun recompute(state: ConverterUiState): ConverterUiState {
        val numeric = state.inputText.toDoubleOrNull() ?: 0.0
        val converted = UnitConverterEngine.convert(numeric, state.fromUnit, state.toUnit)
        return state.copy(
            convertedRaw = converted,
            convertedFormatted = UnitConverterEngine.formatConvertedValue(converted),
            formulaSummary = UnitConverterEngine.formulaSummary(state.fromUnit, state.toUnit),
            allConversions = UnitConverterEngine.convertToAll(numeric, state.fromUnit)
        )
    }
}
