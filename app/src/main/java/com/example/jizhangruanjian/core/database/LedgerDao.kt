package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.Ledger
import kotlinx.coroutines.flow.Flow
@Dao
interface LedgerDao {
    @Insert suspend fun insert(ledger: Ledger): Long
    @Update suspend fun update(ledger: Ledger)
    @Delete suspend fun delete(ledger: Ledger)
    @Query("SELECT * FROM ledger WHERE id = :id") suspend fun getById(id: Long): Ledger?
    @Query("SELECT * FROM ledger WHERE deleted_at IS NULL ORDER BY sort_order ASC") fun observeAll(): Flow<List<Ledger>>
    // 回收站：软删除列表/恢复
    @Query("SELECT * FROM ledger WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC") fun observeTrash(): Flow<List<Ledger>>
    @Query("UPDATE ledger SET deleted_at = :deletedAt WHERE id = :id") suspend fun softDelete(id: Long, deletedAt: Long)
    @Query("UPDATE ledger SET deleted_at = NULL WHERE id = :id") suspend fun restore(id: Long)
    // P6 设置唯一默认账本：清除旧默认
    @Query("UPDATE ledger SET is_default = 0") suspend fun clearDefault()
}
