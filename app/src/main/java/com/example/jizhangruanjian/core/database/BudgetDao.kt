package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.Budget
import kotlinx.coroutines.flow.Flow
@Dao
interface BudgetDao {
    @Insert suspend fun insert(budget: Budget): Long
    @Update suspend fun update(budget: Budget)
    @Delete suspend fun delete(budget: Budget)
    @Query("SELECT * FROM budget WHERE id = :id") suspend fun getById(id: Long): Budget?
    @Query("SELECT * FROM budget WHERE ledger_id = :ledgerId") fun observeByLedger(ledgerId: Long): Flow<List<Budget>>
    // R5/R6 预算使用率、结余计算后续补充
}
