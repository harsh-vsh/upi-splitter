package com.example.upisplitter.ui.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upisplitter.data.local.UpiIdEntity
import com.example.upisplitter.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface MerchantListState {
    data object Loading : MerchantListState
    data class Success(val list: List<UpiIdEntity>) : MerchantListState
}

class ManualUpiViewModel(
    repository: TransactionRepository
) : ViewModel() {

    val merchantListState: StateFlow<MerchantListState> = repository.allUpiIds
        .map { list -> MerchantListState.Success(list) as MerchantListState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MerchantListState.Loading
        )

    private val _selectedUpiEntity = MutableStateFlow<UpiIdEntity?>(null)
    val selectedUpiEntity: StateFlow<UpiIdEntity?> = _selectedUpiEntity.asStateFlow()

    fun selectSavedUpi(entity: UpiIdEntity?) {
        _selectedUpiEntity.value = entity
    }
}
