package com.example.upisplitter.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upisplitter.data.local.TransactionEntity
import com.example.upisplitter.data.local.TransactionWithChunks
import com.example.upisplitter.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDateMillis = MutableStateFlow<Long?>(null)
    val selectedDateMillis: StateFlow<Long?> = _selectedDateMillis.asStateFlow()

    private val _selectedTransactionIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTransactionIds: StateFlow<Set<Long>> = _selectedTransactionIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredTransactions: StateFlow<List<TransactionWithChunks>> = combine(
        repository.allTransactions,
        _searchQuery,
        _selectedDateMillis
    ) { transactions, query, dateMillis ->
        _isLoading.value = false
        var filtered = transactions

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.transaction.merchantName.contains(query, ignoreCase = true) ||
                        it.transaction.merchantUpiId.contains(query, ignoreCase = true)
            }
        }

        if (dateMillis != null) {
            filtered = filtered.filter {
                isSameDay(it.transaction.createdAt, dateMillis)
            }
        }

        filtered.sortedByDescending { it.transaction.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedDate(millis: Long?) {
        _selectedDateMillis.value = millis
    }

    fun toggleSelection(id: Long) {
        val current = _selectedTransactionIds.value
        val updated = if (current.contains(id)) current - id else current + id
        _selectedTransactionIds.value = updated
        _isSelectionMode.value = updated.isNotEmpty()
    }

    fun selectAll(ids: List<Long>) {
        _selectedTransactionIds.value = ids.toSet()
        _isSelectionMode.value = true
    }

    fun clearSelection() {
        _selectedTransactionIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            val ids = _selectedTransactionIds.value
            val items = filteredTransactions.value.filter { it.transaction.id in ids }
            for (item in items) {
                repository.deleteTransaction(item.transaction)
            }
            clearSelection()
        }
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
