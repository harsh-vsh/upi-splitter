package com.example.upisplitter.ui.amount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upisplitter.data.repository.TransactionRepository
import com.example.upisplitter.domain.AmountSplitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AmountViewModel(
    private val repository: TransactionRepository,
    initialMerchantName: String,
    private val merchantUpiId: String,
    private val originalQrData: String,
    val isEditable: Boolean
) : ViewModel() {

    private val _merchantNameInput = MutableStateFlow(initialMerchantName)
    val merchantNameInput: StateFlow<String> = _merchantNameInput.asStateFlow()

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateMerchantName(name: String) {
        if (isEditable) {
            _merchantNameInput.value = name
        }
    }

    fun updateAmount(amount: String) {
        if (amount.all { it.isDigit() }) {
            _amountInput.value = amount
            _errorMessage.value = null
        }
    }

    fun continueToBreakdown(onContinue: (Long) -> Unit) {
        val amountVal = _amountInput.value.toLongOrNull() ?: 0L

        if (amountVal < AmountSplitter.MIN_SPLIT_AMOUNT) {
            _errorMessage.value = "Please enter the amount 2000 or more"
            return
        }

        if (amountVal > 500000L) {
            _errorMessage.value = "Maximum amount allowed is ₹5,00,000"
            return
        }

        val name = _merchantNameInput.value.trim().ifEmpty { "Merchant" }
        val chunks = AmountSplitter.split(amountVal)

        viewModelScope.launch {
            val transactionId = repository.saveTransactionWithChunks(
                merchantName = name,
                merchantUpiId = merchantUpiId,
                originalAmount = amountVal,
                originalQrData = originalQrData,
                amounts = chunks
            )
            onContinue(transactionId)
        }
    }
}
