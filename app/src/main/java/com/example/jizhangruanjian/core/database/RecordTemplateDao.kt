package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.RecordTemplate
import kotlinx.coroutines.flow.Flow
@Dao
interface RecordTemplateDao {
    @Query("SELECT * FROM record_template ORDER BY id DESC")
    fun getAll(): Flow<List<RecordTemplate>>
    @Insert
    suspend fun insert(t: RecordTemplate): Long
    @Update
    suspend fun update(t: RecordTemplate)
    @Delete
    suspend fun delete(t: RecordTemplate)
}