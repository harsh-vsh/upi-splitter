package com.example.upisplitter.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upisplitter.data.local.UpiIdEntity
import com.example.upisplitter.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ManageUpiViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val upiIds: StateFlow<List<UpiIdEntity>> = repository.allUpiIds
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun isDuplicate(upiId: String, currentId: Long): Boolean {
        return repository.isDuplicate(upiId, currentId)
    }

    fun saveUpiId(id: Long = 0L, merchantName: String, upiId: String) {
        viewModelScope.launch {
            repository.saveUpiId(id, merchantName, upiId)
        }
    }

    fun deleteUpiId(upiId: UpiIdEntity) {
        viewModelScope.launch {
            repository.deleteUpiId(upiId)
        }
    }
}
