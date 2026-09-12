package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.Merchant
import kotlinx.coroutines.flow.Flow
@Dao
interface MerchantDao {
    @Insert suspend fun insert(merchant: Merchant): Long
    @Update suspend fun update(merchant: Merchant)
    @Query("SELECT * FROM merchant WHERE id = :id") suspend fun getById(id: Long): Merchant?
    @Query("SELECT * FROM merchant ORDER BY sort_order ASC") fun observeAll(): Flow<List<Merchant>>
    @Query("UPDATE merchant SET sort_order = :sort WHERE id = :id") suspend fun updateSort(id: Long, sort: Int)
}
