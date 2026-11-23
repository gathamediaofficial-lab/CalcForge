package com.example

import com.example.domain.ActivityLevel
import com.example.domain.AngleMode
import com.example.domain.BiologicalSex
import com.example.domain.DateCalculatorEngine
import com.example.domain.DateOffsetUnit
import com.example.domain.EvalResult
import com.example.domain.HealthCalculatorEngine
import com.example.domain.MathEvaluator
import com.example.domain.UnitCategory
import com.example.domain.UnitConverterEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class CalcForgeDomainTest {

    @Test
    fun basicArithmeticAndPrecedence() {
        val r1 = MathEvaluator.evaluate("2+3×4") as EvalResult.Success
        assertEquals(14.0, r1.value, 1e-9)

        val r2 = MathEvaluator.evaluate("(2+3)×4−10÷2") as EvalResult.Success
        assertEquals(15.0, r2.value, 1e-9)
    }

    @Test
    fun scientificFunctionsDegAndRad() {
        val sin30Deg = MathEvaluator.evaluate("sin(30)", AngleMode.DEG) as EvalResult.Success
        assertEquals(0.5, sin30Deg.value, 1e-9)

        val sin180Deg = MathEvaluator.evaluate("sin(180)", AngleMode.DEG) as EvalResult.Success
        assertEquals(0.0, sin180Deg.value, 1e-9)

        val cosPiRad = MathEvaluator.evaluate("cos(π)", AngleMode.RAD) as EvalResult.Success
        assertEquals(-1.0, cosPiRad.value, 1e-9)

        val asinHalf = MathEvaluator.evaluate("sin⁻¹(0.5)", AngleMode.DEG) as EvalResult.Success
        assertEquals(30.0, asinHalf.value, 1e-9)
    }

    @Test
    fun implicitMultiplicationAndPowersAndFactorial() {
        val twoPi = MathEvaluator.evaluate("2π") as EvalResult.Success
        assertEquals(2 * PI, twoPi.value, 1e-9)

        val powerRightAssoc = MathEvaluator.evaluate("2^3^2") as EvalResult.Success
        assertEquals(512.0, powerRightAssoc.value, 1e-9)

        val fact = MathEvaluator.evaluate("5!") as EvalResult.Success
        assertEquals(120.0, fact.value, 1e-9)

        val sqrtImplicit = MathEvaluator.evaluate("3√(16)") as EvalResult.Success
        assertEquals(12.0, sqrtImplicit.value, 1e-9)

        val autoClose = MathEvaluator.evaluate("log(1000") as EvalResult.Success
        assertEquals(3.0, autoClose.value, 1e-9)
    }

    @Test
    fun contextualPercentageAndErrorHandling() {
        val addPercent = MathEvaluator.evaluate("200+10%") as EvalResult.Success
        assertEquals(220.0, addPercent.value, 1e-9)

        val divZero = MathEvaluator.evaluate("10÷0")
        assertTrue(divZero is EvalResult.Error)

        val tan90 = MathEvaluator.evaluate("tan(90)", AngleMode.DEG)
        assertTrue(tan90 is EvalResult.Error)
    }

    @Test
    fun unitConversionsIncludingCurrencyTimePhysicsChemistry() {
        // Currency
        val cur = UnitConverterEngine.unitsByCategory.getValue(UnitCategory.CURRENCY)
        val usd = cur.first { it.id == "usd" }
        val eur = cur.first { it.id == "eur" }
        assertEquals(92.0, UnitConverterEngine.convert(100.0, usd, eur), 1e-6)

        // Expanded Time (week, fortnight, decade, century)
        val time = UnitConverterEngine.unitsByCategory.getValue(UnitCategory.TIME)
        val wk = time.first { it.id == "wk" }
        val day = time.first { it.id == "d" }
        val dec = time.first { it.id == "dec" }
        val yr = time.first { it.id == "yr" }
        assertEquals(14.0, UnitConverterEngine.convert(2.0, wk, day), 1e-6)
        assertEquals(10.0, UnitConverterEngine.convert(1.0, dec, yr), 1e-6)

        // Physics (Force & Power & Charge)
        val force = UnitConverterEngine.unitsByCategory.getValue(UnitCategory.FORCE)
        val kn = force.first { it.id == "kn" }
        val n = force.first { it.id == "n" }
        assertEquals(1000.0, UnitConverterEngine.convert(1.0, kn, n), 1e-6)

        val charge = UnitConverterEngine.unitsByCategory.getValue(UnitCategory.ELECTRIC_CHARGE)
        val ah = charge.first { it.id == "ah" }
        val c = charge.first { it.id == "c" }
        assertEquals(3600.0, UnitConverterEngine.convert(1.0, ah, c), 1e-6)

        // Chemistry (Moles, Molarity, Density)
        val mols = UnitConverterEngine.unitsByCategory.getValue(UnitCategory.AMOUNT_OF_SUBSTANCE)
        assertEquals(1000.0, UnitConverterEngine.convert(1.0, mols.first { it.id == "mol" }, mols.first { it.id == "mmol" }), 1e-6)

        val density = UnitConverterEngine.unitsByCategory.getValue(UnitCategory.DENSITY)
        assertEquals(1000.0, UnitConverterEngine.convert(1.0, density.first { it.id == "g_cm3" }, density.first { it.id == "kg_m3" }), 1e-6)

        // Every category has valid default pairs
        UnitCategory.entries.forEach { category ->
            val (from, to) = UnitConverterEngine.defaultPair(category)
            val result = UnitConverterEngine.convert(1.0, from, to)
            assertTrue("Expected finite result for $category", result.isFinite())
        }
    }

    @Test
    fun dateCalculatorExactDaysAndOffsets() {
        // 2024 is a leap year -> Jan 1, 2024 to Jan 1, 2025 = 366 days
        val jan1_2024 = DateCalculatorEngine.utcDateMillis(2024, 0, 1)
        val jan1_2025 = DateCalculatorEngine.utcDateMillis(2025, 0, 1)
        val diff = DateCalculatorEngine.daysBetween(jan1_2024, jan1_2025, includeEndDate = false)
        assertEquals(366L, diff.totalDays)
        assertEquals(1, diff.years)
        assertEquals(0, diff.months)
        assertEquals(0, diff.days)
        assertEquals(366L, diff.weekdays + diff.weekendDays)

        // Add 2 weeks to Jan 1, 2025 -> Jan 15, 2025
        val plus2Weeks = DateCalculatorEngine.addOffset(jan1_2025, 2, DateOffsetUnit.WEEKS, subtract = false)
        assertEquals(DateCalculatorEngine.utcDateMillis(2025, 0, 15), plus2Weeks)
    }

    @Test
    fun healthCalculatorMetricsValid() {
        val metrics = HealthCalculatorEngine.calculate(
            sex = BiologicalSex.MALE,
            ageYears = 28,
            heightCm = 175.0,
            weightKg = 72.0,
            waistCm = 82.0,
            neckCm = 38.0,
            hipCm = null,
            activityLevel = ActivityLevel.MODERATE
        )
        assertNotNull(metrics)
        metrics!!
        assertEquals(23.51, metrics.bmi, 0.05)
        assertEquals("Normal", metrics.bmiCategory)
        assertTrue(metrics.bodyFatPercent in 8.0..25.0)
        assertTrue(metrics.visceralFatRating in 1..15)
        assertTrue(metrics.estimatedBodyAge in 18..40)
        assertTrue(metrics.bmrKcal > 1400)
        assertTrue(metrics.tdeeKcal > metrics.bmrKcal)
    }
}
