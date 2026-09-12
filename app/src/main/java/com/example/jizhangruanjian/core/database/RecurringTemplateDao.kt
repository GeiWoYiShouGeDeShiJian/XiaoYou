package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.RecurringTemplate
import kotlinx.coroutines.flow.Flow
@Dao
interface RecurringTemplateDao {
    @Insert suspend fun insert(template: RecurringTemplate): Long
    @Update suspend fun update(template: RecurringTemplate)
    @Delete suspend fun delete(template: RecurringTemplate)
    @Query("SELECT * FROM recurring_template WHERE id = :id") suspend fun getById(id: Long): RecurringTemplate?
    @Query("SELECT * FROM recurring_template ORDER BY next_execute_date ASC") fun observeAll(): Flow<List<RecurringTemplate>>
    // R7 补跑扫描：取所有到期模板
    @Query("SELECT * FROM recurring_template WHERE next_execute_date <= :now ORDER BY next_execute_date ASC") suspend fun findDue(now: Long): List<RecurringTemplate>
}
