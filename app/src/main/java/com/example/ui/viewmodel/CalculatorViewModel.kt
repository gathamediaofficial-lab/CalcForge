package com.example.ui.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HistoryRepository
import com.example.data.UserPreferencesRepository
import com.example.domain.AngleMode
import com.example.domain.EvalResult
import com.example.domain.MathEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val selection: TextRange = TextRange.Zero,
    val previewResult: String = "",
    val evaluatedExpression: String = "",
    val errorMessage: String? = null,
    val justEvaluated: Boolean = false,
    val lastAnswer: Double = 0.0,
    val angleMode: AngleMode = AngleMode.DEG,
    val isInverseMode: Boolean = false,
    val isScientificExpanded: Boolean = true,
    val isDarkMode: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false
)

class CalculatorViewModel(
    private val historyRepository: HistoryRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.update { state ->
                    val updated = state.copy(
                        isDarkMode = prefs.isDarkMode,
                        angleMode = prefs.angleMode,
                        isScientificExpanded = prefs.isScientificExpanded,
                        hapticsEnabled = prefs.hapticsEnabled,
                        memoryValue = prefs.memoryValue,
                        hasMemory = prefs.hasMemory
                    )
                    recomputePreview(updated)
                }
            }
        }
    }

    fun toggleDarkMode() {
        val next = !_uiState.value.isDarkMode
        viewModelScope.launch { preferencesRepository.setDarkMode(next) }
    }

    fun toggleAngleMode() {
        val next = if (_uiState.value.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        viewModelScope.launch { preferencesRepository.setAngleMode(next) }
    }

    fun toggleScientificExpanded() {
        val next = !_uiState.value.isScientificExpanded
        viewModelScope.launch { preferencesRepository.setScientificExpanded(next) }
    }

    fun toggleHaptics() {
        val next = !_uiState.value.hapticsEnabled
        viewModelScope.launch { preferencesRepository.setHapticsEnabled(next) }
    }

    fun toggleInverseMode() {
        _uiState.update { it.copy(isInverseMode = !it.isInverseMode) }
    }

    fun onSelectionChanged(selection: TextRange) {
        _uiState.update { state ->
            val clamped = clampSelection(selection, state.expression.length)
            state.copy(
                selection = clamped,
                justEvaluated = false
            )
        }
    }

    fun onDigit(digit: String) {
        _uiState.update { state ->
            val (base, sel) = if (state.justEvaluated) {
                "" to TextRange.Zero
            } else {
                state.expression to clampSelection(state.selection, state.expression.length)
            }
            val (nextExpr, nextSel) = insertAtSelection(base, sel, digit)
            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = nextSel,
                    evaluatedExpression = if (state.justEvaluated) "" else state.evaluatedExpression,
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onDecimalPoint() {
        _uiState.update { state ->
            val (base, sel) = if (state.justEvaluated) {
                "" to TextRange.Zero
            } else {
                state.expression to clampSelection(state.selection, state.expression.length)
            }
            val start = sel.min
            val end = sel.max
            val leftPart = base.substring(0, start)
            val rightPart = base.substring(end)
            val leftNum = leftPart.takeLastWhile { it.isDigit() || it == '.' }
            val rightNum = rightPart.takeWhile { it.isDigit() || it == '.' }
            if (leftNum.contains('.') || rightNum.contains('.')) return@update state

            val insertion = if (leftNum.isEmpty()) "0." else "."
            val (nextExpr, nextSel) = insertAtSelection(base, sel, insertion)
            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = nextSel,
                    evaluatedExpression = if (state.justEvaluated) "" else state.evaluatedExpression,
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onOperator(op: String) {
        _uiState.update { state ->
            val base = state.expression
            val sel = clampSelection(state.selection, base.length)
            val start = sel.min
            val end = sel.max

            if (base.isEmpty() || (start == 0 && end == base.length)) {
                return@update if (op == "−") {
                    recomputePreview(
                        state.copy(
                            expression = "−",
                            selection = TextRange(1),
                            justEvaluated = false,
                            errorMessage = null
                        )
                    )
                } else {
                    state
                }
            }

            val operators = setOf('+', '−', '×', '÷', '^')
            val before = base.substring(0, start)
            val after = base.substring(end)

            val (nextExpr, nextCursor) = when {
                before.isEmpty() -> {
                    if (op == "−") {
                        ("−" + after) to 1
                    } else {
                        base to start
                    }
                }
                before.last() in operators -> {
                    if (op == "−" && before.last() in setOf('×', '÷', '^')) {
                        (before + op + after) to (before.length + op.length)
                    } else if (before.length == 1 && before[0] == '−') {
                        base to start
                    } else {
                        val replaced = before.dropLast(1) + op + after
                        replaced to (before.length - 1 + op.length)
                    }
                }
                before.last() == '(' && op != "−" -> base to start
                else -> (before + op + after) to (before.length + op.length)
            }

            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = TextRange(nextCursor),
                    evaluatedExpression = if (state.justEvaluated) "" else state.evaluatedExpression,
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onFunction(funcToken: String) {
        _uiState.update { state ->
            val (base, sel) = if (state.justEvaluated) {
                "" to TextRange.Zero
            } else {
                state.expression to clampSelection(state.selection, state.expression.length)
            }
            val (nextExpr, nextSel) = insertAtSelection(base, sel, funcToken)
            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = nextSel,
                    evaluatedExpression = if (state.justEvaluated) "" else state.evaluatedExpression,
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onConstant(constant: String) {
        _uiState.update { state ->
            val (base, sel) = if (state.justEvaluated) {
                "" to TextRange.Zero
            } else {
                state.expression to clampSelection(state.selection, state.expression.length)
            }
            val (nextExpr, nextSel) = insertAtSelection(base, sel, constant)
            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = nextSel,
                    evaluatedExpression = if (state.justEvaluated) "" else state.evaluatedExpression,
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onPostfix(symbol: String) {
        _uiState.update { state ->
            val base = state.expression
            val sel = clampSelection(state.selection, base.length)
            val start = sel.min
            if (start == 0) return@update state
            val charBefore = base[start - 1]
            if (charBefore in setOf('+', '−', '×', '÷', '^', '(')) return@update state
            val (nextExpr, nextSel) = insertAtSelection(base, sel, symbol)
            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = nextSel,
                    evaluatedExpression = if (state.justEvaluated) "" else state.evaluatedExpression,
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onParenthesis(paren: String) {
        _uiState.update { state ->
            val (base, sel) = if (state.justEvaluated && paren == "(") {
                "" to TextRange.Zero
            } else {
                state.expression to clampSelection(state.selection, state.expression.length)
            }
            if (paren == ")") {
                val beforeCursor = base.substring(0, sel.min)
                val open = beforeCursor.count { it == '(' }
                val close = beforeCursor.count { it == ')' }
                if (open <= close) return@update state
            }
            val (nextExpr, nextSel) = insertAtSelection(base, sel, paren)
            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = nextSel,
                    evaluatedExpression = if (state.justEvaluated && paren == "(") "" else state.evaluatedExpression,
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onReciprocal() {
        _uiState.update { state ->
            val base = state.expression
            val nextExpr = if (base.isEmpty()) "1÷(" else "1÷($base)"
            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = TextRange(nextExpr.length),
                    evaluatedExpression = "",
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onToggleSign() {
        _uiState.update { state ->
            val expr = state.expression
            val sel = clampSelection(state.selection, expr.length)
            if (expr.isEmpty()) {
                return@update state.copy(
                    expression = "−",
                    selection = TextRange(1),
                    justEvaluated = false,
                    errorMessage = null
                )
            }
            val (nextExpr, nextSel) = if (
                expr.startsWith("−") &&
                expr.drop(1).all { it.isDigit() || it == '.' || it == 'E' || it == '-' || it == '+' }
            ) {
                expr.drop(1) to TextRange(
                    (sel.start - 1).coerceAtLeast(0),
                    (sel.end - 1).coerceAtLeast(0)
                )
            } else if (expr.all { it.isDigit() || it == '.' }) {
                "−$expr" to TextRange(sel.start + 1, sel.end + 1)
            } else {
                val wrapped = "−($expr)"
                wrapped to TextRange(wrapped.length)
            }
            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = clampSelection(nextSel, nextExpr.length),
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.expression.isEmpty()) return@update state
            val expr = state.expression
            val sel = clampSelection(state.selection, expr.length)

            // If user highlighted a range, delete that exact range
            if (!sel.collapsed) {
                val nextExpr = expr.removeRange(sel.min, sel.max)
                return@update recomputePreview(
                    state.copy(
                        expression = nextExpr,
                        selection = TextRange(sel.min),
                        justEvaluated = false,
                        errorMessage = null
                    )
                )
            }

            val cursor = sel.start
            if (cursor == 0) return@update state

            val before = expr.substring(0, cursor)
            val after = expr.substring(cursor)
            val multiTokens = listOf(
                "sin⁻¹(", "cos⁻¹(", "tan⁻¹(",
                "sinh(", "cosh(", "tanh(",
                "log₂(", "sin(", "cos(", "tan(",
                "log(", "abs(", "ln(", "mod", "Ans", "10^(", "e^("
            )
            val matched = multiTokens.firstOrNull { before.endsWith(it) }
            val deleteLen = matched?.length ?: 1
            val nextBefore = before.dropLast(deleteLen)
            val nextExpr = nextBefore + after
            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = TextRange(nextBefore.length),
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onClearAll() {
        _uiState.update { state ->
            state.copy(
                expression = "",
                selection = TextRange.Zero,
                previewResult = "",
                evaluatedExpression = "",
                errorMessage = null,
                justEvaluated = false
            )
        }
    }

    fun onEquals() {
        val state = _uiState.value
        val expr = state.expression.trim()
        if (expr.isEmpty()) return

        when (val result = MathEvaluator.evaluate(expr, state.angleMode, state.lastAnswer)) {
            is EvalResult.Success -> {
                val rawFormatted = MathEvaluator.formatRawNumber(result.value)
                val isSci = MathEvaluator.isScientificExpression(expr)
                _uiState.update {
                    it.copy(
                        evaluatedExpression = expr,
                        expression = rawFormatted,
                        selection = TextRange(rawFormatted.length),
                        previewResult = result.formatted,
                        lastAnswer = result.value,
                        justEvaluated = true,
                        errorMessage = null
                    )
                }
                viewModelScope.launch {
                    historyRepository.recordCalculation(
                        expression = expr,
                        result = result.formatted,
                        rawResult = result.value,
                        angleMode = state.angleMode.name,
                        isScientific = isSci
                    )
                }
            }
            is EvalResult.Error -> {
                _uiState.update {
                    it.copy(errorMessage = result.message.ifEmpty { "Invalid expression" })
                }
            }
        }
    }

    // Memory Operations
    fun onMemoryClear() {
        viewModelScope.launch { preferencesRepository.setMemory(0.0, false) }
    }

    fun onMemoryRecall() {
        val state = _uiState.value
        if (!state.hasMemory) return
        val formatted = MathEvaluator.formatRawNumber(state.memoryValue)
        _uiState.update { s ->
            val (base, sel) = if (s.justEvaluated) {
                "" to TextRange.Zero
            } else {
                s.expression to clampSelection(s.selection, s.expression.length)
            }
            val (nextExpr, nextSel) = insertAtSelection(base, sel, formatted)
            recomputePreview(
                s.copy(
                    expression = nextExpr,
                    selection = nextSel,
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onMemoryStore(): Boolean {
        val currentVal = resolveCurrentNumericValue() ?: return false
        viewModelScope.launch { preferencesRepository.setMemory(currentVal, true) }
        return true
    }

    fun onMemoryAdd(): Boolean {
        val currentVal = resolveCurrentNumericValue() ?: return false
        val next = _uiState.value.memoryValue + currentVal
        viewModelScope.launch { preferencesRepository.setMemory(next, true) }
        return true
    }

    fun onMemorySubtract(): Boolean {
        val currentVal = resolveCurrentNumericValue() ?: return false
        val next = _uiState.value.memoryValue - currentVal
        viewModelScope.launch { preferencesRepository.setMemory(next, true) }
        return true
    }

    fun loadExpressionFromExternal(expression: String) {
        _uiState.update { state ->
            recomputePreview(
                state.copy(
                    expression = expression,
                    selection = TextRange(expression.length),
                    evaluatedExpression = "",
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun insertValueFromExternal(value: Double) {
        val formatted = MathEvaluator.formatRawNumber(value)
        _uiState.update { state ->
            val (base, sel) = if (state.justEvaluated) {
                "" to TextRange.Zero
            } else {
                state.expression to clampSelection(state.selection, state.expression.length)
            }
            val (nextExpr, nextSel) = insertAtSelection(base, sel, formatted)
            recomputePreview(
                state.copy(
                    expression = nextExpr,
                    selection = nextSel,
                    evaluatedExpression = if (state.justEvaluated) "" else state.evaluatedExpression,
                    justEvaluated = false,
                    errorMessage = null
                )
            )
        }
    }

    fun currentCopyableResult(): String? {
        val s = _uiState.value
        return when {
            s.previewResult.isNotEmpty() -> s.previewResult
            s.expression.isNotEmpty() -> s.expression
            else -> null
        }
    }

    private fun clampSelection(sel: TextRange, length: Int): TextRange = TextRange(
        start = sel.start.coerceIn(0, length),
        end = sel.end.coerceIn(0, length)
    )

    private fun insertAtSelection(
        text: String,
        selection: TextRange,
        insertion: String
    ): Pair<String, TextRange> {
        val start = selection.min.coerceIn(0, text.length)
        val end = selection.max.coerceIn(0, text.length)
        val next = text.substring(0, start) + insertion + text.substring(end)
        return next to TextRange(start + insertion.length)
    }

    private fun resolveCurrentNumericValue(): Double? {
        val s = _uiState.value
        if (s.expression.isEmpty()) return null
        val res = MathEvaluator.evaluate(s.expression, s.angleMode, s.lastAnswer)
        return (res as? EvalResult.Success)?.value
    }

    private fun recomputePreview(state: CalculatorUiState): CalculatorUiState {
        if (state.expression.isBlank()) {
            return state.copy(previewResult = "")
        }
        val eval = MathEvaluator.evaluate(state.expression, state.angleMode, state.lastAnswer)
        return if (eval is EvalResult.Success) {
            state.copy(previewResult = eval.formatted)
        } else {
            state.copy(previewResult = "")
        }
    }
}
