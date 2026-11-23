package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

enum class AngleMode {
    DEG, RAD
}

sealed class EvalResult {
    data class Success(val value: Double, val formatted: String) : EvalResult()
    data class Error(val message: String) : EvalResult()
}

/**
 * Recursive-descent scientific expression parser and evaluator.
 * Supports:
 * - Basic ops: +, -, ×, ÷, mod, ^ (right-associative)
 * - Implicit multiplication: 2π, 2(3+4), (1+2)(3+4), 3sin(30), 2√9
 * - Contextual percentage: 200+10% -> 220, 50% -> 0.5
 * - Unary +/-, postfix factorial ! (with Lanczos gamma for non-integers)
 * - Functions: sin, cos, tan, asin, acos, atan, sinh, cosh, tanh, ln, log, log2, sqrt, cbrt, abs, exp
 * - Constants: π, e, Ans
 * - Auto-closing trailing parentheses for smooth live preview
 */
object MathEvaluator {

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Func(val name: String) : Token()
        data class Const(val name: String) : Token()
        data class Op(val symbol: String) : Token()
        data object LParen : Token()
        data object RParen : Token()
        data object Factorial : Token()
        data object Percent : Token()
    }

    private val scientificIndicators = listOf(
        "sin", "cos", "tan", "asin", "acos", "atan",
        "sinh", "cosh", "tanh", "ln", "log", "√", "∛",
        "^", "!", "π", "e", "abs", "mod", "exp"
    )

    fun isScientificExpression(expression: String): Boolean {
        val lower = expression.lowercase(Locale.US)
        return scientificIndicators.any { indicator ->
            if (indicator == "e") {
                // Match standalone constant 'e' (not scientific notation like 1E-5)
                Regex("(?<![0-9a-zA-Z])e(?![a-zA-Z])").containsMatchIn(expression)
            } else {
                lower.contains(indicator)
            }
        }
    }

    fun evaluate(
        expression: String,
        angleMode: AngleMode = AngleMode.DEG,
        lastAnswer: Double = 0.0
    ): EvalResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return EvalResult.Error("")

        return try {
            val rawTokens = tokenize(trimmed)
            if (rawTokens.isEmpty()) return EvalResult.Error("")
            val tokens = insertImplicitMultiplication(balanceParentheses(rawTokens))
            val parser = Parser(tokens, angleMode, lastAnswer)
            val result = parser.parseExpression()
            if (parser.hasRemaining()) {
                return EvalResult.Error("Syntax error")
            }
            if (result.isNaN()) {
                return EvalResult.Error("Undefined")
            }
            if (result.isInfinite()) {
                return EvalResult.Error("Overflow")
            }
            val cleaned = cleanFloatingPointNoise(result)
            EvalResult.Success(cleaned, formatDisplayNumber(cleaned))
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (e: IllegalArgumentException) {
            EvalResult.Error(e.message ?: "Syntax error")
        } catch (_: Exception) {
            EvalResult.Error("Invalid expression")
        }
    }

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val s = input
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace(",", "")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("log₂", "log2")

        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    var hasDot = c == '.'
                    i++
                    while (i < s.length) {
                        val ch = s[i]
                        if (ch.isDigit()) {
                            i++
                        } else if (ch == '.' && !hasDot) {
                            hasDot = true
                            i++
                        } else if ((ch == 'E' || ch == 'e') && i + 1 < s.length &&
                            (s[i + 1].isDigit() || ((s[i + 1] == '+' || s[i + 1] == '-') && i + 2 < s.length && s[i + 2].isDigit()))
                        ) {
                            // Scientific notation literal e.g. 1.2E-5
                            i++
                            if (s[i] == '+' || s[i] == '-') i++
                            while (i < s.length && s[i].isDigit()) i++
                            break
                        } else {
                            break
                        }
                    }
                    val numStr = s.substring(start, i)
                    val value = numStr.toDoubleOrNull()
                        ?: throw IllegalArgumentException("Invalid number")
                    tokens.add(Token.Number(value))
                }
                c == '(' -> {
                    tokens.add(Token.LParen)
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.RParen)
                    i++
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '^' -> {
                    tokens.add(Token.Op(c.toString()))
                    i++
                }
                c == '!' -> {
                    tokens.add(Token.Factorial)
                    i++
                }
                c == '%' -> {
                    tokens.add(Token.Percent)
                    i++
                }
                c == 'π' -> {
                    tokens.add(Token.Const("pi"))
                    i++
                }
                c == '√' -> {
                    tokens.add(Token.Func("sqrt"))
                    i++
                }
                c == '∛' -> {
                    tokens.add(Token.Func("cbrt"))
                    i++
                }
                c.isLetter() -> {
                    val start = i
                    while (i < s.length && (s[i].isLetter() || (s[i].isDigit() && s.substring(start, i).equals("log", true)))) {
                        i++
                    }
                    val word = s.substring(start, i)
                    when (val lower = word.lowercase(Locale.US)) {
                        "mod" -> tokens.add(Token.Op("mod"))
                        "pi" -> tokens.add(Token.Const("pi"))
                        "e" -> tokens.add(Token.Const("e"))
                        "ans" -> tokens.add(Token.Const("ans"))
                        "sin", "cos", "tan",
                        "asin", "acos", "atan",
                        "sinh", "cosh", "tanh",
                        "ln", "log", "log2",
                        "sqrt", "cbrt", "abs", "exp" -> tokens.add(Token.Func(lower))
                        else -> throw IllegalArgumentException("Unknown symbol: $word")
                    }
                }
                else -> throw IllegalArgumentException("Unexpected character: $c")
            }
        }
        return tokens
    }

    private fun balanceParentheses(tokens: List<Token>): List<Token> {
        var openCount = 0
        for (t in tokens) {
            if (t is Token.LParen) openCount++
            else if (t is Token.RParen) openCount--
        }
        if (openCount <= 0) return tokens
        val balanced = tokens.toMutableList()
        repeat(openCount) { balanced.add(Token.RParen) }
        return balanced
    }

    private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
        if (tokens.size < 2) return tokens
        val out = mutableListOf<Token>()
        for (idx in tokens.indices) {
            val current = tokens[idx]
            if (out.isNotEmpty()) {
                val prev = out.last()
                val prevCanMultiply = prev is Token.Number || prev is Token.Const ||
                    prev is Token.RParen || prev is Token.Factorial || prev is Token.Percent
                val currCanBeMultiplied = current is Token.LParen || current is Token.Func ||
                    current is Token.Const || (current is Token.Number && (prev is Token.RParen || prev is Token.Const || prev is Token.Factorial || prev is Token.Percent))
                if (prevCanMultiply && currCanBeMultiplied) {
                    out.add(Token.Op("*"))
                }
            }
            out.add(current)
        }
        return out
    }

    private class Parser(
        private val tokens: List<Token>,
        private val angleMode: AngleMode,
        private val lastAnswer: Double
    ) {
        private var pos = 0

        fun hasRemaining(): Boolean = pos < tokens.size

        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun consume(): Token = tokens[pos++]

        fun parseExpression(): Double {
            var left = parseTerm()
            while (true) {
                val op = peek()
                if (op is Token.Op && (op.symbol == "+" || op.symbol == "-")) {
                    consume()
                    val rightIsPercent = isNextTermDirectPercent()
                    val right = parseTerm()
                    left = if (op.symbol == "+") {
                        if (rightIsPercent) left + (left * right) else left + right
                    } else {
                        if (rightIsPercent) left - (left * right) else left - right
                    }
                } else {
                    break
                }
            }
            return left
        }

        private fun isNextTermDirectPercent(): Boolean {
            // Detects simple pattern like "+ 10%" for intuitive calculator percentage behavior
            val t0 = tokens.getOrNull(pos)
            val t1 = tokens.getOrNull(pos + 1)
            val t2 = tokens.getOrNull(pos + 2)
            return t0 is Token.Number && t1 is Token.Percent &&
                (t2 == null || t2 is Token.RParen || (t2 is Token.Op && (t2.symbol == "+" || t2.symbol == "-")))
        }

        private fun parseTerm(): Double {
            var left = parsePower()
            while (true) {
                val op = peek()
                if (op is Token.Op && (op.symbol == "*" || op.symbol == "/" || op.symbol == "mod")) {
                    consume()
                    val right = parsePower()
                    left = when (op.symbol) {
                        "*" -> left * right
                        "/" -> {
                            if (right == 0.0) throw ArithmeticException("Division by zero")
                            left / right
                        }
                        "mod" -> {
                            if (right == 0.0) throw ArithmeticException("Division by zero")
                            left % right
                        }
                        else -> left
                    }
                } else {
                    break
                }
            }
            return left
        }

        private fun parsePower(): Double {
            val base = parseUnary()
            val next = peek()
            return if (next is Token.Op && next.symbol == "^") {
                consume()
                val exponent = parsePower() // Right-associative
                if (base < 0.0 && exponent != exponent.toLong().toDouble()) {
                    throw ArithmeticException("Domain error")
                }
                base.pow(exponent)
            } else {
                base
            }
        }

        private fun parseUnary(): Double {
            val next = peek()
            if (next is Token.Op && next.symbol == "+") {
                consume()
                return parseUnary()
            }
            if (next is Token.Op && next.symbol == "-") {
                consume()
                return -parseUnary()
            }
            return parsePostfix()
        }

        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (true) {
                when (peek()) {
                    is Token.Factorial -> {
                        consume()
                        value = computeFactorial(value)
                    }
                    is Token.Percent -> {
                        consume()
                        value /= 100.0
                    }
                    else -> break
                }
            }
            return value
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Incomplete expression")
            return when (token) {
                is Token.Number -> {
                    consume()
                    token.value
                }
                is Token.Const -> {
                    consume()
                    when (token.name) {
                        "pi" -> PI
                        "e" -> Math.E
                        "ans" -> lastAnswer
                        else -> throw IllegalArgumentException("Unknown constant")
                    }
                }
                is Token.LParen -> {
                    consume()
                    val value = parseExpression()
                    if (peek() !is Token.RParen) {
                        throw IllegalArgumentException("Missing ')'")
                    }
                    consume()
                    value
                }
                is Token.Func -> {
                    consume()
                    val arg = if (peek() is Token.LParen) {
                        consume()
                        val v = parseExpression()
                        if (peek() !is Token.RParen) {
                            throw IllegalArgumentException("Missing ')'")
                        }
                        consume()
                        v
                    } else {
                        parseUnary()
                    }
                    applyFunction(token.name, arg)
                }
                else -> throw IllegalArgumentException("Unexpected token")
            }
        }

        private fun applyFunction(name: String, arg: Double): Double {
            fun toRadians(v: Double): Double =
                if (angleMode == AngleMode.DEG) Math.toRadians(v) else v

            fun fromRadians(v: Double): Double =
                if (angleMode == AngleMode.DEG) Math.toDegrees(v) else v

            return when (name) {
                "sin" -> {
                    val res = sin(toRadians(arg))
                    if (abs(res) < 1e-15) 0.0 else res
                }
                "cos" -> {
                    val res = cos(toRadians(arg))
                    if (abs(res) < 1e-15) 0.0 else res
                }
                "tan" -> {
                    if (angleMode == AngleMode.DEG) {
                        val mod180 = ((arg % 180.0) + 180.0) % 180.0
                        if (abs(mod180 - 90.0) < 1e-9) {
                            throw ArithmeticException("Undefined")
                        }
                    }
                    val res = tan(toRadians(arg))
                    if (abs(res) < 1e-15) 0.0 else res
                }
                "asin" -> {
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error")
                    fromRadians(asin(arg))
                }
                "acos" -> {
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error")
                    fromRadians(acos(arg))
                }
                "atan" -> fromRadians(atan(arg))
                "sinh" -> sinh(arg)
                "cosh" -> cosh(arg)
                "tanh" -> tanh(arg)
                "ln" -> {
                    if (arg <= 0.0) throw ArithmeticException("Domain error")
                    ln(arg)
                }
                "log" -> {
                    if (arg <= 0.0) throw ArithmeticException("Domain error")
                    log10(arg)
                }
                "log2" -> {
                    if (arg <= 0.0) throw ArithmeticException("Domain error")
                    log2(arg)
                }
                "sqrt" -> {
                    if (arg < 0.0) throw ArithmeticException("Domain error")
                    sqrt(arg)
                }
                "cbrt" -> cbrt(arg)
                "abs" -> abs(arg)
                "exp" -> exp(arg)
                else -> throw IllegalArgumentException("Unknown function: $name")
            }
        }

        private fun computeFactorial(x: Double): Double {
            if (x < 0.0 && x == x.toLong().toDouble()) {
                throw ArithmeticException("Domain error")
            }
            if (x > 170.0) throw ArithmeticException("Overflow")
            if (x >= 0.0 && x == x.toLong().toDouble()) {
                var acc = 1.0
                for (n in 2..x.toInt()) {
                    acc *= n
                }
                return acc
            }
            return gamma(x + 1.0)
        }

        private fun gamma(z: Double): Double {
            val g = 7.0
            val p = doubleArrayOf(
                0.99999999999980993,
                676.5203681218851,
                -1259.1392167224028,
                771.32342877765313,
                -176.61502916214059,
                12.507343278686905,
                -0.13857109526572012,
                9.9843695780195716e-6,
                1.5056327351493116e-7
            )
            if (z < 0.5) {
                return PI / (sin(PI * z) * gamma(1.0 - z))
            }
            val zm1 = z - 1.0
            var x = p[0]
            for (i in 1 until p.size) {
                x += p[i] / (zm1 + i)
            }
            val t = zm1 + g + 0.5
            return sqrt(2.0 * PI) * t.pow(zm1 + 0.5) * exp(-t) * x
        }
    }

    private fun cleanFloatingPointNoise(value: Double): Double {
        if (value == 0.0 || abs(value) < 1e-15) return 0.0
        if (value.isNaN() || value.isInfinite()) return value
        return try {
            BigDecimal.valueOf(value)
                .round(MathContext(12, RoundingMode.HALF_UP))
                .toDouble()
                .let { if (it == -0.0) 0.0 else it }
        } catch (_: Exception) {
            value
        }
    }

    fun formatDisplayNumber(value: Double): String {
        if (value.isNaN()) return "Undefined"
        if (value.isInfinite()) return "Overflow"
        val clean = if (value == -0.0 || abs(value) < 1e-15) 0.0 else value
        if (clean == 0.0) return "0"

        val absVal = abs(clean)
        val symbols = DecimalFormatSymbols(Locale.US)

        if (absVal >= 1e12 || absVal < 1e-6) {
            val sciFormat = DecimalFormat("0.########E0", symbols)
            return sciFormat.format(clean)
        }

        val bd = BigDecimal.valueOf(clean)
            .round(MathContext(12, RoundingMode.HALF_UP))
            .stripTrailingZeros()
        val df = DecimalFormat("#,##0.##########", symbols)
        df.maximumFractionDigits = 10
        return df.format(bd)
    }

    fun formatRawNumber(value: Double): String {
        val clean = if (value == -0.0 || abs(value) < 1e-15) 0.0 else value
        if (clean == 0.0) return "0"
        val absVal = abs(clean)
        if (absVal >= 1e12 || absVal < 1e-6) {
            return DecimalFormat("0.########E0", DecimalFormatSymbols(Locale.US)).format(clean)
        }
        return BigDecimal.valueOf(clean)
            .round(MathContext(12, RoundingMode.HALF_UP))
            .stripTrailingZeros()
            .toPlainString()
    }
}
