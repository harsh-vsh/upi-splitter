package com.example.upisplitter.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionWithChunks>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): TransactionWithChunks?

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    fun observeTransactionById(transactionId: Long): Flow<TransactionWithChunks?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<TransactionChunkEntity>)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("UPDATE transaction_chunks SET isCompleted = :isCompleted WHERE id = :chunkId")
    suspend fun updateChunkCompletion(chunkId: Long, isCompleted: Boolean)
}
