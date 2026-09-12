package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.jizhangruanjian.data.model.TransactionImage
import kotlinx.coroutines.flow.Flow
@Dao
interface TransactionImageDao {
    @Insert suspend fun insert(image: TransactionImage): Long
    @Delete suspend fun delete(image: TransactionImage)
    @Query("SELECT * FROM transaction_image WHERE transaction_id = :transactionId") fun observeByTransaction(transactionId: Long): Flow<List<TransactionImage>>
    @Query("SELECT * FROM transaction_image WHERE transaction_id = :transactionId") suspend fun getByTransaction(transactionId: Long): List<TransactionImage>
    @Query("DELETE FROM transaction_image WHERE transaction_id = :transactionId") suspend fun deleteByTransaction(transactionId: Long)
    @Query("SELECT * FROM transaction_image") fun observeAll(): Flow<List<TransactionImage>>
    @Query("SELECT * FROM transaction_image WHERE transaction_id IN (:transactionIds)") suspend fun getByTransactions(transactionIds: List<Long>): List<TransactionImage>
}
