package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.TagGroup
import kotlinx.coroutines.flow.Flow
@Dao
interface TagGroupDao {
    @Query("SELECT * FROM tag_group ORDER BY sort_order ASC") fun observeAll(): Flow<List<TagGroup>>
    @Insert suspend fun insert(group: TagGroup): Long
    @Update suspend fun update(group: TagGroup)
}
