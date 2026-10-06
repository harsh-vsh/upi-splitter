package com.example.upisplitter.domain

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object QrCodeGenerator {
    fun generatePureQrBitmap(content: String, size: Int = 512): Bitmap? {
        return try {
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    fun generateBrandedQrBitmap(content: String, merchantName: String, amount: Long, sequenceNumber: Int, qrSize: Int = 512): Bitmap? {
        return try {
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize)
            val qrWidth = bitMatrix.width
            val qrHeight = bitMatrix.height
            val qrBmp = Bitmap.createBitmap(qrWidth, qrHeight, Bitmap.Config.RGB_565)
            for (x in 0 until qrWidth) {
                for (y in 0 until qrHeight) {
                    qrBmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }

            val padding = 40
            val headerHeight = 110
            val footerHeight = 180
            val totalWidth = qrWidth + (padding * 2)
            val totalHeight = qrHeight + headerHeight + footerHeight + (padding * 2)

            val brandedBmp = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(brandedBmp)
            canvas.drawColor(Color.WHITE)

            val paint = Paint().apply {
                isAntiAlias = true
            }

            // Draw App Title & QR Number: "UPI Splitter • QR #1"
            paint.color = Color.rgb(33, 33, 33)
            paint.textSize = 36f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("UPI Splitter • QR #$sequenceNumber", totalWidth / 2f, padding.toFloat() + 35f, paint)

            // Draw Merchant Name
            paint.textSize = 26f
            paint.isFakeBoldText = false
            paint.color = Color.rgb(100, 100, 100)
            canvas.drawText(merchantName, totalWidth / 2f, padding.toFloat() + 75f, paint)

            // Draw QR Code
            val qrTop = padding.toFloat() + headerHeight
            canvas.drawBitmap(qrBmp, padding.toFloat(), qrTop, null)

            // Draw Amount below QR
            paint.textSize = 35f
            paint.isFakeBoldText = true
            paint.color = Color.rgb(0, 128, 0)
            val amountText = "Amount: ₹ $amount"
            canvas.drawText(amountText, totalWidth / 2f, qrTop + qrHeight + 55f, paint)

            // Draw footer: Download text & GitHub link
            paint.textSize = 20f
            paint.isFakeBoldText = true
            paint.color = Color.rgb(33, 33, 33)
            canvas.drawText("Download UPI Splitter Today", totalWidth / 2f, qrTop + qrHeight + 95f, paint)

            paint.textSize = 16f
            paint.isFakeBoldText = false
            paint.color = Color.rgb(0, 102, 204)
            canvas.drawText("https://github.com/harsh-vsh/upi-splitter", totalWidth / 2f, qrTop + qrHeight + 125f, paint)

            brandedBmp
        } catch (e: Exception) {
            null
        }
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
        val filename = "UPI_Splitter_QR_${System.currentTimeMillis()}.png"
        val fos: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/UPISplitter")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(imagesDir, "UPISplitter").apply { mkdirs() }
            val imageFile = File(appDir, filename)
            FileOutputStream(imageFile)
        }

        return try {
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
