package com.example.domain

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

enum class BiologicalSex(val label: String) {
    MALE("Male"),
    FEMALE("Female")
}

enum class ActivityLevel(val label: String, val multiplier: Double) {
    SEDENTARY("Sedentary", 1.2),
    LIGHT("Light (1–3d/wk)", 1.375),
    MODERATE("Moderate (3–5d/wk)", 1.55),
    ACTIVE("Active (6–7d/wk)", 1.725),
    VERY_ACTIVE("Athlete", 1.9)
}

enum class HealthCategoryLevel {
    LOW, OPTIMAL, ELEVATED, HIGH
}

data class HealthMetricsResult(
    val bmi: Double,
    val bmiCategory: String,
    val bmiLevel: HealthCategoryLevel,
    val healthyWeightMinKg: Double,
    val healthyWeightMaxKg: Double,
    val bodyFatPercent: Double,
    val bodyFatCategory: String,
    val bodyFatLevel: HealthCategoryLevel,
    val bodyFatMethod: String,
    val fatMassKg: Double,
    val leanBodyMassKg: Double,
    val visceralFatRating: Int,
    val visceralFatCategory: String,
    val visceralFatLevel: HealthCategoryLevel,
    val waistToHeightRatio: Double,
    val estimatedBodyAge: Int,
    val bodyAgeDelta: Int,
    val bmrKcal: Int,
    val tdeeKcal: Int
)

/**
 * Offline body composition engine implementing standard published clinical equations:
 * - WHO Body Mass Index (BMI)
 * - US Navy Circumference Body Fat % (with Deurenberg BMI fallback)
 * - Waist-to-Height Ratio (WHtR) & Visceral Fat Index (1–59 scale)
 * - Mifflin-St Jeor BMR / TDEE & Metabolic Body Age estimate
 */
object HealthCalculatorEngine {

    fun calculate(
        sex: BiologicalSex,
        ageYears: Int,
        heightCm: Double,
        weightKg: Double,
        waistCm: Double?,
        neckCm: Double?,
        hipCm: Double?,
        activityLevel: ActivityLevel
    ): HealthMetricsResult? {
        if (heightCm < 50.0 || weightKg < 15.0 || ageYears < 10 || ageYears > 120) return null

        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)

        val (bmiCategory, bmiLevel) = when {
            bmi < 18.5 -> "Underweight" to HealthCategoryLevel.LOW
            bmi < 25.0 -> "Normal" to HealthCategoryLevel.OPTIMAL
            bmi < 30.0 -> "Overweight" to HealthCategoryLevel.ELEVATED
            else -> "Obese" to HealthCategoryLevel.HIGH
        }

        val healthyMinKg = 18.5 * heightM * heightM
        val healthyMaxKg = 24.9 * heightM * heightM

        // Body Fat %: Prefer US Navy circumference method when valid inputs exist, else Deurenberg equation
        val navyBf = computeUsNavyBodyFat(sex, heightCm, waistCm, neckCm, hipCm)
        val (rawBf, bfMethod) = if (navyBf != null && navyBf in 3.0..65.0) {
            navyBf to "US Navy Method"
        } else {
            val sexFactor = if (sex == BiologicalSex.MALE) 1 else 0
            val deurenberg = (1.20 * bmi) + (0.23 * ageYears) - (10.8 * sexFactor) - 5.4
            deurenberg.coerceIn(3.0, 65.0) to "Deurenberg Formula"
        }

        val (bfCategory, bfLevel) = classifyBodyFat(sex, rawBf)
        val fatMassKg = weightKg * (rawBf / 100.0)
        val leanMassKg = (weightKg - fatMassKg).coerceAtLeast(0.0)

        // Waist-to-Height Ratio & Visceral Fat Rating (1–59 scale, healthy <= 12)
        val effectiveWaistCm = waistCm?.takeIf { it > 35.0 }
            ?: estimateWaistFromBmi(sex, bmi, heightCm)
        val whtr = effectiveWaistCm / heightCm

        val visceralRaw = when (sex) {
            BiologicalSex.MALE ->
                (0.55 * bmi) + (0.14 * ageYears) + (28.0 * (whtr - 0.45)) - 9.5
            BiologicalSex.FEMALE ->
                (0.48 * bmi) + (0.12 * ageYears) + (26.0 * (whtr - 0.43)) - 9.8
        }
        val visceralRating = visceralRaw.roundToInt().coerceIn(1, 59)
        val (visceralCategory, visceralLevel) = when {
            visceralRating <= 9 -> "Healthy" to HealthCategoryLevel.OPTIMAL
            visceralRating <= 12 -> "Acceptable" to HealthCategoryLevel.OPTIMAL
            visceralRating <= 15 -> "Elevated" to HealthCategoryLevel.ELEVATED
            else -> "High Risk" to HealthCategoryLevel.HIGH
        }

        // Mifflin-St Jeor BMR & TDEE
        val bmr = if (sex == BiologicalSex.MALE) {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * ageYears) + 5.0
        } else {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * ageYears) - 161.0
        }.coerceAtLeast(800.0)
        val tdee = bmr * activityLevel.multiplier

        // Estimated Metabolic / Body Age (compares body fat, visceral fat, and BMI against optimal age norms)
        val idealBf = if (sex == BiologicalSex.MALE) 15.0 + (ageYears - 20).coerceAtLeast(0) * 0.12
        else 23.0 + (ageYears - 20).coerceAtLeast(0) * 0.12
        val bfPenalty = (rawBf - idealBf) * 0.65
        val bmiPenalty = when {
            bmi < 18.5 -> (18.5 - bmi) * 1.1
            bmi > 24.9 -> (bmi - 24.9) * 0.85
            else -> -1.0
        }
        val visceralPenalty = ((visceralRating - 8).coerceAtLeast(0)) * 0.7
        val activityBonus = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 2.0
            ActivityLevel.LIGHT -> 0.5
            ActivityLevel.MODERATE -> -1.5
            ActivityLevel.ACTIVE -> -3.0
            ActivityLevel.VERY_ACTIVE -> -4.5
        }
        val bodyAge = (ageYears + bfPenalty + bmiPenalty + visceralPenalty + activityBonus)
            .roundToInt()
            .coerceIn(14, 95)

        return HealthMetricsResult(
            bmi = bmi,
            bmiCategory = bmiCategory,
            bmiLevel = bmiLevel,
            healthyWeightMinKg = healthyMinKg,
            healthyWeightMaxKg = healthyMaxKg,
            bodyFatPercent = rawBf,
            bodyFatCategory = bfCategory,
            bodyFatLevel = bfLevel,
            bodyFatMethod = bfMethod,
            fatMassKg = fatMassKg,
            leanBodyMassKg = leanMassKg,
            visceralFatRating = visceralRating,
            visceralFatCategory = visceralCategory,
            visceralFatLevel = visceralLevel,
            waistToHeightRatio = whtr,
            estimatedBodyAge = bodyAge,
            bodyAgeDelta = bodyAge - ageYears,
            bmrKcal = bmr.roundToInt(),
            tdeeKcal = tdee.roundToInt()
        )
    }

    private fun computeUsNavyBodyFat(
        sex: BiologicalSex,
        heightCm: Double,
        waistCm: Double?,
        neckCm: Double?,
        hipCm: Double?
    ): Double? {
        if (waistCm == null || neckCm == null || waistCm <= 35.0 || neckCm <= 20.0) return null
        return when (sex) {
            BiologicalSex.MALE -> {
                val diff = waistCm - neckCm
                if (diff <= 1.0) return null
                495.0 / (1.0324 - 0.19077 * log10(diff) + 0.15456 * log10(heightCm)) - 450.0
            }
            BiologicalSex.FEMALE -> {
                if (hipCm == null || hipCm <= 45.0) return null
                val sum = waistCm + hipCm - neckCm
                if (sum <= 1.0) return null
                495.0 / (1.29579 - 0.35004 * log10(sum) + 0.22100 * log10(heightCm)) - 450.0
            }
        }
    }

    private fun estimateWaistFromBmi(sex: BiologicalSex, bmi: Double, heightCm: Double): Double {
        val ratio = if (sex == BiologicalSex.MALE) {
            0.34 + 0.0072 * bmi
        } else {
            0.31 + 0.0070 * bmi
        }
        return (ratio * heightCm).coerceIn(50.0, 180.0)
    }

    private fun classifyBodyFat(sex: BiologicalSex, bf: Double): Pair<String, HealthCategoryLevel> {
        return when (sex) {
            BiologicalSex.MALE -> when {
                bf < 6.0 -> "Essential" to HealthCategoryLevel.LOW
                bf < 14.0 -> "Athletic" to HealthCategoryLevel.OPTIMAL
                bf < 18.0 -> "Fitness" to HealthCategoryLevel.OPTIMAL
                bf < 25.0 -> "Average" to HealthCategoryLevel.ELEVATED
                else -> "High" to HealthCategoryLevel.HIGH
            }
            BiologicalSex.FEMALE -> when {
                bf < 14.0 -> "Essential" to HealthCategoryLevel.LOW
                bf < 21.0 -> "Athletic" to HealthCategoryLevel.OPTIMAL
                bf < 25.0 -> "Fitness" to HealthCategoryLevel.OPTIMAL
                bf < 32.0 -> "Average" to HealthCategoryLevel.ELEVATED
                else -> "High" to HealthCategoryLevel.HIGH
            }
        }
    }
}
