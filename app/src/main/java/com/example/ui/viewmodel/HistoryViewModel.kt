package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationHistoryEntity
import com.example.data.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class HistoryFilter {
    ALL, PINNED, SCIENTIFIC
}

enum class HistoryGroup {
    TODAY, YESTERDAY, EARLIER
}

data class HistoryUiState(
    val searchQuery: String = "",
    val filter: HistoryFilter = HistoryFilter.ALL,
    val totalCount: Int = 0,
    val groupedEntries: Map<HistoryGroup, List<CalculationHistoryEntity>> = emptyMap(),
    val isClearDialogOpen: Boolean = false
)

class HistoryViewModel(
    private val repository: HistoryRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filter = MutableStateFlow(HistoryFilter.ALL)
    private val clearDialogOpen = MutableStateFlow(false)

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.historyFlow,
        searchQuery,
        filter,
        clearDialogOpen
    ) { entries, query, activeFilter, dialogOpen ->
        val trimmedQuery = query.trim().lowercase()
        val filtered = entries.filter { entry ->
            val matchesFilter = when (activeFilter) {
                HistoryFilter.ALL -> true
                HistoryFilter.PINNED -> entry.isPinned
                HistoryFilter.SCIENTIFIC -> entry.isScientific
            }
            val matchesQuery = trimmedQuery.isEmpty() ||
                entry.expression.lowercase().contains(trimmedQuery) ||
                entry.result.lowercase().contains(trimmedQuery) ||
                entry.angleMode.lowercase().contains(trimmedQuery)
            matchesFilter && matchesQuery
        }

        val grouped = linkedMapOf<HistoryGroup, MutableList<CalculationHistoryEntity>>()
        val startOfToday = startOfDayMillis(0)
        val startOfYesterday = startOfDayMillis(-1)

        for (item in filtered) {
            val bucket = when {
                item.timestamp >= startOfToday -> HistoryGroup.TODAY
                item.timestamp >= startOfYesterday -> HistoryGroup.YESTERDAY
                else -> HistoryGroup.EARLIER
            }
            grouped.getOrPut(bucket) { mutableListOf() }.add(item)
        }

        HistoryUiState(
            searchQuery = query,
            filter = activeFilter,
            totalCount = entries.size,
            groupedEntries = grouped,
            isClearDialogOpen = dialogOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onFilterSelected(selected: HistoryFilter) {
        filter.value = selected
    }

    fun openClearDialog() {
        clearDialogOpen.value = true
    }

    fun dismissClearDialog() {
        clearDialogOpen.value = false
    }

    fun togglePin(id: Long) {
        viewModelScope.launch { repository.togglePin(id) }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { repository.deleteById(id) }
    }

    fun clearUnpinned() {
        clearDialogOpen.value = false
        viewModelScope.launch { repository.clearUnpinned() }
    }

    fun clearAll() {
        clearDialogOpen.value = false
        viewModelScope.launch { repository.clearAll() }
    }

    private fun startOfDayMillis(dayOffset: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
