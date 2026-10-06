package com.example.upisplitter.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UpiIdDao {
    @Query("SELECT * FROM upi_ids ORDER BY createdAt DESC")
    fun getAllUpiIds(): Flow<List<UpiIdEntity>>

    @Query("SELECT COUNT(*) FROM upi_ids WHERE LOWER(TRIM(upiId)) = LOWER(TRIM(:upiId)) AND id != :currentId")
    suspend fun checkDuplicate(upiId: String, currentId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpiId(upiId: UpiIdEntity)

    @Delete
    suspend fun deleteUpiId(upiId: UpiIdEntity)
}
