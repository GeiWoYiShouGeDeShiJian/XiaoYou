package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.jizhangruanjian.data.model.AppSetting
import kotlinx.coroutines.flow.Flow
@Dao
interface AppSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(setting: AppSetting)
    @Query("SELECT * FROM app_setting WHERE key = :key") suspend fun get(key: String): AppSetting?
    @Query("SELECT * FROM app_setting") fun observeAll(): Flow<List<AppSetting>>
    @Query("DELETE FROM app_setting WHERE key = :key") suspend fun delete(key: String)
}
