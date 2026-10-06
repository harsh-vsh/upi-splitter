package com.example.upisplitter.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upisplitter.data.local.TransactionWithChunks
import com.example.upisplitter.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val repository: TransactionRepository,
    transactionId: Long
) : ViewModel() {

    val transactionState: StateFlow<TransactionWithChunks?> = repository.observeTransactionById(transactionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun markChunkComplete(chunkId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateChunkCompletion(chunkId, isCompleted)
        }
    }
}
