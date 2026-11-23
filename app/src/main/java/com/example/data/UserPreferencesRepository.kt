package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.AngleMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calcforge_prefs")

data class UserPreferences(
    val isDarkMode: Boolean = true,
    val angleMode: AngleMode = AngleMode.DEG,
    val isScientificExpanded: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val ANGLE_MODE = stringPreferencesKey("angle_mode")
        val IS_SCIENTIFIC_EXPANDED = booleanPreferencesKey("is_scientific_expanded")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val MEMORY_VALUE = doublePreferencesKey("memory_value")
        val HAS_MEMORY = booleanPreferencesKey("has_memory")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val angleModeName = prefs[Keys.ANGLE_MODE] ?: AngleMode.DEG.name
        val angleMode = runCatching { AngleMode.valueOf(angleModeName) }.getOrDefault(AngleMode.DEG)
        UserPreferences(
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: true,
            angleMode = angleMode,
            isScientificExpanded = prefs[Keys.IS_SCIENTIFIC_EXPANDED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            memoryValue = prefs[Keys.MEMORY_VALUE] ?: 0.0,
            hasMemory = prefs[Keys.HAS_MEMORY] ?: false
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_MODE] = enabled }
    }

    suspend fun setAngleMode(mode: AngleMode) {
        context.dataStore.edit { it[Keys.ANGLE_MODE] = mode.name }
    }

    suspend fun setScientificExpanded(expanded: Boolean) {
        context.dataStore.edit { it[Keys.IS_SCIENTIFIC_EXPANDED] = expanded }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setMemory(value: Double, hasMemory: Boolean) {
        context.dataStore.edit {
            it[Keys.MEMORY_VALUE] = value
            it[Keys.HAS_MEMORY] = hasMemory
        }
    }
}
