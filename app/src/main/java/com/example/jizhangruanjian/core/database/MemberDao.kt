package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.Member
import kotlinx.coroutines.flow.Flow
@Dao
interface MemberDao {
    @Query("SELECT * FROM member ORDER BY sort_order ASC") fun observeAll(): Flow<List<Member>>
    @Insert suspend fun insert(member: Member): Long
    @Update suspend fun update(member: Member)
    @Query("DELETE FROM member WHERE id = :id") suspend fun delete(id: Long)
}