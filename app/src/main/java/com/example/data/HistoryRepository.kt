package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: CalculationHistoryDao) {
    val historyFlow: Flow<List<CalculationHistoryEntity>> = dao.observeAll()

    suspend fun recordCalculation(
        expression: String,
        result: String,
        rawResult: Double,
        angleMode: String,
        isScientific: Boolean
    ) {
        dao.insert(
            CalculationHistoryEntity(
                expression = expression,
                result = result,
                rawResult = rawResult,
                angleMode = angleMode,
                isScientific = isScientific
            )
        )
    }

    suspend fun togglePin(id: Long) = dao.togglePin(id)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearUnpinned() = dao.clearUnpinned()

    suspend fun clearAll() = dao.clearAll()
}
