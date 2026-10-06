package com.example.upisplitter.ui.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.example.upisplitter.domain.UpiQrParser

class MlKitQrAnalyzer(
    private val onValidUpiQr: (String) -> Unit,
    private val onInvalidQr: () -> Unit
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )
    private var isProcessing = false
    private var hasScannedValid = false
    private var lastInvalidTime = 0L

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (hasScannedValid || isProcessing) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            isProcessing = true
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (hasScannedValid) return@addOnSuccessListener

                    var foundValid = false
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            val parsed = UpiQrParser.parse(rawValue)
                            if (parsed != null) {
                                foundValid = true
                                hasScannedValid = true
                                onValidUpiQr(rawValue)
                                break
                            }
                        }
                    }

                    if (!foundValid && barcodes.isNotEmpty() && !hasScannedValid) {
                        val now = System.currentTimeMillis()
                        if (now - lastInvalidTime > 2500L) {
                            lastInvalidTime = now
                            onInvalidQr()
                        }
                    }
                }
                .addOnFailureListener {
                    // Ignore frame failure
                }
                .addOnCompleteListener {
                    isProcessing = false
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
