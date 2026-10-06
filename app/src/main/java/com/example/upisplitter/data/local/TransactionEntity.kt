package com.example.upisplitter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val merchantName: String,
    val merchantUpiId: String,
    val originalAmount: Long,
    val originalQrData: String,
    val createdAt: Long
)
