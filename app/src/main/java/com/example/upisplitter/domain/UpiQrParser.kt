package com.example.upisplitter.domain

import android.net.Uri
import java.net.URLDecoder

data class UpiPaymentInfo(
    val upiId: String,
    val merchantName: String,
    val originalQrData: String
)

object UpiQrParser {
    fun parse(qrData: String): UpiPaymentInfo? {
        val trimmed = qrData.trim()
        if (trimmed.isEmpty()) return null

        val uri = try {
            Uri.parse(trimmed)
        } catch (e: Exception) {
            null
        }

        val pa = uri?.getQueryParameter("pa") ?: run {
            if (trimmed.contains("@") && !trimmed.contains(" ")) {
                trimmed
            } else {
                null
            }
        }

        if (pa.isNullOrBlank()) {
            return null
        }

        val pnRaw = uri?.getQueryParameter("pn") ?: run {
            val defaultName = pa.substringBefore("@")
            if (defaultName.isNotBlank()) {
                defaultName.replaceFirstChar { it.uppercase() } + " Store"
            } else {
                "Merchant"
            }
        }

        val merchantName = try {
            URLDecoder.decode(pnRaw, "UTF-8")
        } catch (e: Exception) {
            pnRaw
        }

        return UpiPaymentInfo(
            upiId = pa,
            merchantName = merchantName,
            originalQrData = trimmed
        )
    }
}
