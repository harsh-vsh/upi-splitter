package com.example.upisplitter.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upisplitter.data.local.TransactionWithChunks
import com.example.upisplitter.data.local.UpiIdEntity
import com.example.upisplitter.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    repository: TransactionRepository
) : ViewModel() {

    val savedUpiList: StateFlow<List<UpiIdEntity>> = repository.allUpiIds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentTransactions: StateFlow<List<TransactionWithChunks>> = repository.allTransactions
        .map { list -> list.take(5) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
