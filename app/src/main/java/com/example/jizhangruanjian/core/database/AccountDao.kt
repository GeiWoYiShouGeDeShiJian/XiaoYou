package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.Account
import kotlinx.coroutines.flow.Flow
@Dao
interface AccountDao {
    @Insert suspend fun insert(account: Account): Long
    @Update suspend fun update(account: Account)
    @Delete suspend fun delete(account: Account)
    @Query("SELECT * FROM account WHERE id = :id") suspend fun getById(id: Long): Account?
    @Query("SELECT * FROM account WHERE deleted_at IS NULL ORDER BY sort_order ASC") fun observeAll(): Flow<List<Account>>
    // 回收站：软删除列表/恢复
    @Query("SELECT * FROM account WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC") fun observeTrash(): Flow<List<Account>>
    @Query("UPDATE account SET deleted_at = :deletedAt WHERE id = :id") suspend fun softDelete(id: Long, deletedAt: Long)
    @Query("UPDATE account SET deleted_at = NULL WHERE id = :id") suspend fun restore(id: Long)
    // R15 删除分组前统计分组下账户数
    @Query("SELECT COUNT(*) FROM account WHERE group_id = :groupId") suspend fun countByGroup(groupId: Long): Int
    // R15 按账户大类分组查询后续补充：fun observeGrouped(): Flow<List<AccountGroupWithAccounts>>
    // R9 共享账户查询后续补充
}
