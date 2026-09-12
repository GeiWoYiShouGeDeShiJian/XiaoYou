package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.MerchantGroup
import kotlinx.coroutines.flow.Flow
@Dao
interface MerchantGroupDao {
    @Query("SELECT * FROM merchant_group ORDER BY sort_order ASC") fun observeAll(): Flow<List<MerchantGroup>>
    @Insert suspend fun insert(group: MerchantGroup): Long
    @Update suspend fun update(group: MerchantGroup)
}
