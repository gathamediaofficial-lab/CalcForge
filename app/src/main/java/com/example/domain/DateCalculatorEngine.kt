package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

data class DateDiffResult(
    val totalDays: Long,
    val years: Int,
    val months: Int,
    val days: Int,
    val weeks: Long,
    val remainingDaysAfterWeeks: Long,
    val totalHours: Long,
    val totalMinutes: Long,
    val weekdays: Long,
    val weekendDays: Long,
    val isReversed: Boolean
)

enum class DateOffsetUnit(val label: String) {
    DAYS("Days"),
    WEEKS("Weeks"),
    MONTHS("Months"),
    YEARS("Years")
}

object DateCalculatorEngine {

    private val utcZone: TimeZone = TimeZone.getTimeZone("UTC")

    fun todayUtcMillis(): Long {
        val local = Calendar.getInstance()
        return utcDateMillis(
            local.get(Calendar.YEAR),
            local.get(Calendar.MONTH),
            local.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun utcDateMillis(year: Int, monthZeroBased: Int, dayOfMonth: Int): Long {
        val cal = Calendar.getInstance(utcZone)
        cal.clear()
        cal.set(year, monthZeroBased, dayOfMonth, 0, 0, 0)
        return cal.timeInMillis
    }

    fun normalizeToUtcMidnight(millis: Long): Long {
        val cal = Calendar.getInstance(utcZone)
        cal.timeInMillis = millis
        return utcDateMillis(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun formatDate(utcMillis: Long): String {
        val sdf = SimpleDateFormat("EEE, MMM d, yyyy", Locale.US)
        sdf.timeZone = utcZone
        return sdf.format(Date(utcMillis))
    }

    fun formatShortDate(utcMillis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        sdf.timeZone = utcZone
        return sdf.format(Date(utcMillis))
    }

    fun daysBetween(
        startUtcMillis: Long,
        endUtcMillis: Long,
        includeEndDate: Boolean = false
    ): DateDiffResult {
        val sNorm = normalizeToUtcMidnight(startUtcMillis)
        val eNorm = normalizeToUtcMidnight(endUtcMillis)
        val isReversed = eNorm < sNorm
        val earlier = if (isReversed) eNorm else sNorm
        val later = if (isReversed) sNorm else eNorm

        val millisPerDay = 86_400_000L
        val rawDays = (later - earlier) / millisPerDay
        val totalDays = rawDays + if (includeEndDate) 1L else 0L

        // Exact calendar breakdown (years, months, days)
        val startCal = Calendar.getInstance(utcZone).apply { timeInMillis = earlier }
        val endCal = Calendar.getInstance(utcZone).apply {
            timeInMillis = later
            if (includeEndDate) add(Calendar.DAY_OF_MONTH, 1)
        }

        var years = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
        var months = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)
        var days = endCal.get(Calendar.DAY_OF_MONTH) - startCal.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            val prevMonthCal = endCal.clone() as Calendar
            prevMonthCal.add(Calendar.MONTH, -1)
            days += prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            months--
        }
        if (months < 0) {
            months += 12
            years--
        }

        // Fast O(1) weekday & weekend count
        val fullWeeks = totalDays / 7L
        val remDays = (totalDays % 7L).toInt()
        var weekdays = fullWeeks * 5L
        var weekendDays = fullWeeks * 2L
        val cursor = Calendar.getInstance(utcZone).apply { timeInMillis = earlier }
        repeat(remDays) {
            val dow = cursor.get(Calendar.DAY_OF_WEEK)
            if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) {
                weekendDays++
            } else {
                weekdays++
            }
            cursor.add(Calendar.DAY_OF_MONTH, 1)
        }

        return DateDiffResult(
            totalDays = totalDays,
            years = years,
            months = months,
            days = days,
            weeks = totalDays / 7L,
            remainingDaysAfterWeeks = totalDays % 7L,
            totalHours = totalDays * 24L,
            totalMinutes = totalDays * 1440L,
            weekdays = weekdays,
            weekendDays = weekendDays,
            isReversed = isReversed
        )
    }

    fun addOffset(
        baseUtcMillis: Long,
        amount: Int,
        unit: DateOffsetUnit,
        subtract: Boolean = false
    ): Long {
        val delta = if (subtract) -abs(amount) else abs(amount)
        val cal = Calendar.getInstance(utcZone).apply {
            timeInMillis = normalizeToUtcMidnight(baseUtcMillis)
        }
        when (unit) {
            DateOffsetUnit.DAYS -> cal.add(Calendar.DAY_OF_MONTH, delta)
            DateOffsetUnit.WEEKS -> cal.add(Calendar.DAY_OF_MONTH, delta * 7)
            DateOffsetUnit.MONTHS -> cal.add(Calendar.MONTH, delta)
            DateOffsetUnit.YEARS -> cal.add(Calendar.YEAR, delta)
        }
        return cal.timeInMillis
    }
}
