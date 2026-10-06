package com.example.upisplitter.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithChunks(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val chunks: List<TransactionChunkEntity>
)
