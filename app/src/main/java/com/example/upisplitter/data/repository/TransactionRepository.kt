package com.example.upisplitter.data.repository

import com.example.upisplitter.data.local.TransactionDao
import com.example.upisplitter.data.local.TransactionEntity
import com.example.upisplitter.data.local.TransactionChunkEntity
import com.example.upisplitter.data.local.TransactionWithChunks
import com.example.upisplitter.data.local.UpiIdDao
import com.example.upisplitter.data.local.UpiIdEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val upiIdDao: UpiIdDao
) {
    val allTransactions: Flow<List<TransactionWithChunks>> = transactionDao.getAllTransactions()
    val allUpiIds: Flow<List<UpiIdEntity>> = upiIdDao.getAllUpiIds()

    suspend fun getTransactionById(id: Long): TransactionWithChunks? {
        return transactionDao.getTransactionById(id)
    }

    fun observeTransactionById(id: Long): Flow<TransactionWithChunks?> {
        return transactionDao.observeTransactionById(id)
    }

    suspend fun saveTransactionWithChunks(
        merchantName: String,
        merchantUpiId: String,
        originalAmount: Long,
        originalQrData: String,
        amounts: List<Long>
    ): Long {
        val transaction = TransactionEntity(
            merchantName = merchantName,
            merchantUpiId = merchantUpiId,
            originalAmount = originalAmount,
            originalQrData = originalQrData,
            createdAt = System.currentTimeMillis()
        )
        val transactionId = transactionDao.insertTransaction(transaction)

        val chunkEntities = amounts.mapIndexed { index, amount ->
            TransactionChunkEntity(
                transactionId = transactionId,
                amount = amount,
                sequenceNumber = index + 1,
                isCompleted = false
            )
        }
        transactionDao.insertChunks(chunkEntities)
        return transactionId
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun updateChunkCompletion(chunkId: Long, isCompleted: Boolean) {
        transactionDao.updateChunkCompletion(chunkId, isCompleted)
    }

    suspend fun saveUpiId(id: Long = 0L, merchantName: String, upiId: String) {
        val entity = UpiIdEntity(id = id, merchantName = merchantName, upiId = upiId)
        upiIdDao.insertUpiId(entity)
    }

    suspend fun deleteUpiId(upiId: UpiIdEntity) {
        upiIdDao.deleteUpiId(upiId)
    }

    suspend fun isDuplicate(upiId: String, currentId: Long): Boolean {
        return upiIdDao.checkDuplicate(upiId, currentId) > 0
    }
}
