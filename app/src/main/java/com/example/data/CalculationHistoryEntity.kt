package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expression: String,
    val result: String,
    val rawResult: Double,
    val angleMode: String,
    val isScientific: Boolean,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
