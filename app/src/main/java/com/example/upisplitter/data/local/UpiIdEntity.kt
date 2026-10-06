package com.example.upisplitter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upi_ids")
data class UpiIdEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val merchantName: String,
    val upiId: String,
    val createdAt: Long = System.currentTimeMillis()
)
