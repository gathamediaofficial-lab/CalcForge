package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY isPinned DESC, timestamp DESC")
    fun observeAll(): Flow<List<CalculationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CalculationHistoryEntity): Long

    @Query("UPDATE calculation_history SET isPinned = NOT isPinned WHERE id = :id")
    suspend fun togglePin(id: Long)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history WHERE isPinned = 0")
    suspend fun clearUnpinned()

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
}
