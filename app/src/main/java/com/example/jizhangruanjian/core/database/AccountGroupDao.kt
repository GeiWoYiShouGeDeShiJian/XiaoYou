package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.AccountGroup
import kotlinx.coroutines.flow.Flow
@Dao
interface AccountGroupDao {
    @Insert suspend fun insert(group: AccountGroup): Long
    @Update suspend fun update(group: AccountGroup)
    @Delete suspend fun delete(group: AccountGroup)
    @Query("SELECT * FROM account_group WHERE id = :id") suspend fun getById(id: Long): AccountGroup?
    @Query("SELECT * FROM account_group ORDER BY sort_order ASC") fun observeAll(): Flow<List<AccountGroup>>
}
