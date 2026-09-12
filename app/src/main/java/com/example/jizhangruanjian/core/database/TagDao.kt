package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.Tag
import kotlinx.coroutines.flow.Flow
@Dao
interface TagDao {
    @Insert suspend fun insert(tag: Tag): Long
    @Update suspend fun update(tag: Tag)
    @Delete suspend fun delete(tag: Tag)
    @Query("SELECT * FROM tag WHERE id = :id") suspend fun getById(id: Long): Tag?
    @Query("SELECT * FROM tag ORDER BY name ASC") fun observeAll(): Flow<List<Tag>>
}
