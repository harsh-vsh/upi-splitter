package com.example.upisplitter.ui.custom

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upisplitter.data.local.UpiIdEntity
import com.example.upisplitter.data.repository.TransactionRepository
import com.example.upisplitter.domain.QrCodeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder

sealed interface MerchantListState {
    data object Loading : MerchantListState
    data class Success(val list: List<UpiIdEntity>) : MerchantListState
}

class CustomQrViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    val merchantListState: StateFlow<MerchantListState> = repository.allUpiIds
        .map { list -> MerchantListState.Success(list) as MerchantListState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MerchantListState.Loading)

    private val _step = MutableStateFlow(1) // Step 1: Select UPI, Step 2: Amount, Step 3: QR Generated
    val step: StateFlow<Int> = _step.asStateFlow()

    private val _selectedUpiEntity = MutableStateFlow<UpiIdEntity?>(null)
    val selectedUpiEntity: StateFlow<UpiIdEntity?> = _selectedUpiEntity.asStateFlow()

    private val _merchantName = MutableStateFlow("")
    val merchantName: StateFlow<String> = _merchantName.asStateFlow()

    private val _upiId = MutableStateFlow("")
    val upiId: StateFlow<String> = _upiId.asStateFlow()

    private val _amountText = MutableStateFlow("")
    val amountText: StateFlow<String> = _amountText.asStateFlow()

    private val _generatedBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedBitmap: StateFlow<Bitmap?> = _generatedBitmap.asStateFlow()

    private val _currentTransactionId = MutableStateFlow<Long?>(null)
    private val _currentChunkId = MutableStateFlow<Long?>(null)

    private val _isPaymentCompleted = MutableStateFlow(false)
    val isPaymentCompleted: StateFlow<Boolean> = _isPaymentCompleted.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun selectSavedUpi(entity: UpiIdEntity?) {
        _selectedUpiEntity.value = entity
        if (entity != null) {
            _merchantName.value = entity.merchantName
            _upiId.value = entity.upiId
        } else {
            _merchantName.value = ""
            _upiId.value = ""
        }
    }

    fun proceedToAmountStep() {
        val entity = _selectedUpiEntity.value
        val name = entity?.merchantName ?: _merchantName.value.trim()
        val upi = entity?.upiId ?: _upiId.value.trim()

        if (entity == null && (name.isBlank() || upi.isBlank())) {
            _errorMessage.value = "Please select a saved merchant"
            return
        }

        _step.value = 2
    }

    fun backToSelectStep() {
        _step.value = 1
    }

    fun onAmountChange(amount: String) {
        _amountText.value = amount
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun generateQr() {
        viewModelScope.launch {
            val name = _merchantName.value.trim()
            val upi = _upiId.value.trim()
            val amount = _amountText.value.toLongOrNull() ?: 0L

            if (name.isBlank() || upi.isBlank()) {
                _errorMessage.value = "Please select a valid merchant"
                return@launch
            }

            if (amount <= 0) {
                _errorMessage.value = "Please enter a valid amount greater than 0"
                return@launch
            }

            val encodedName = URLEncoder.encode(name, "UTF-8")
            val uriString = "upi://pay?pa=$upi&pn=$encodedName&am=$amount&cu=INR"

            // Ultra-sharp 800px high-resolution QR bitmap
            val bitmap = QrCodeGenerator.generatePureQrBitmap(
                content = uriString,
                size = 800
            )

            if (bitmap != null) {
                // Save transaction & chunk to Room Database
                val txId = repository.saveTransactionWithChunks(
                    merchantName = name,
                    merchantUpiId = upi,
                    originalAmount = amount,
                    originalQrData = uriString,
                    amounts = listOf(amount)
                )
                _currentTransactionId.value = txId
                val txWithChunks = repository.getTransactionById(txId)
                _currentChunkId.value = txWithChunks?.chunks?.firstOrNull()?.id

                _generatedBitmap.value = bitmap
                _isPaymentCompleted.value = false
                _step.value = 3
            } else {
                _errorMessage.value = "Failed to generate QR Code. Please try again."
            }
        }
    }

    fun completePayment() {
        viewModelScope.launch {
            val chunkId = _currentChunkId.value
            if (chunkId != null) {
                repository.updateChunkCompletion(chunkId, true)
                _isPaymentCompleted.value = true
                _toastMessage.value = "Payment completed & saved to history!"
            }
        }
    }

    fun reset() {
        _step.value = 1
        _generatedBitmap.value = null
        _amountText.value = ""
        _currentTransactionId.value = null
        _currentChunkId.value = null
        _isPaymentCompleted.value = false
    }

    fun saveToGallery(context: Context) {
        val bitmap = _generatedBitmap.value ?: return
        val success = QrCodeGenerator.saveBitmapToGallery(context, bitmap)
        if (success) {
            _toastMessage.value = "QR Code saved to Pictures/UPISplitter"
        } else {
            _errorMessage.value = "Failed to save QR Code to gallery"
        }
    }

    fun shareQrCode(context: Context) {
        val bitmap = _generatedBitmap.value ?: return
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "custom_upi_qr.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val imageUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, "UPI Payment QR for ${_merchantName.value} - ₹${_amountText.value}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Single Custom QR"))
        } catch (e: Exception) {
            _errorMessage.value = "Failed to share QR Code: ${e.localizedMessage}"
        }
    }
}
