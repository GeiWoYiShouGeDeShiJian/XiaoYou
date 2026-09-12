package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.jizhangruanjian.data.model.TransactionTag
import kotlinx.coroutines.flow.Flow
@Dao
interface TransactionTagDao {
    @Insert suspend fun insert(link: TransactionTag)
    @Delete suspend fun delete(link: TransactionTag)
    @Query("SELECT * FROM transaction_tag WHERE transaction_id = :transactionId") fun observeByTransaction(transactionId: Long): Flow<List<TransactionTag>>
    @Query("SELECT * FROM transaction_tag WHERE tag_id = :tagId") suspend fun getByTag(tagId: Long): List<TransactionTag>
    @Query("DELETE FROM transaction_tag WHERE transaction_id = :transactionId") suspend fun deleteByTransaction(transactionId: Long)
    @Query("DELETE FROM transaction_tag WHERE tag_id = :tagId") suspend fun deleteByTag(tagId: Long)
    @Query("SELECT tag_id FROM transaction_tag WHERE transaction_id = :transactionId") suspend fun getTagIdsByTransaction(transactionId: Long): List<Long>
}
