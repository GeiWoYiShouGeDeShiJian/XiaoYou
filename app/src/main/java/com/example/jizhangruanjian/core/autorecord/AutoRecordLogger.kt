package com.example.jizhangruanjian.core.autorecord
import com.example.jizhangruanjian.core.database.AppSettingDao
import com.example.jizhangruanjian.data.model.AppSetting
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoRecordLogger @Inject constructor(
    private val appSettingDao: AppSettingDao
) {
    companion object {
        const val KEY = "auto_record_log"
        const val MAX = 100
    }
    suspend fun log(entry: String) {
        val list = entries().toMutableList()
        list.add(entry)
        while (list.size > MAX) list.removeAt(0)
        appSettingDao.upsert(AppSetting(KEY, JSONArray(list).toString()))
    }
    suspend fun entries(): List<String> {
        val raw = appSettingDao.get(KEY)?.value ?: "[]"
        return try { val a = JSONArray(raw); (0 until a.length()).map { a.getString(it) } } catch (_: Exception) { emptyList() }
    }
    suspend fun clear() = appSettingDao.upsert(AppSetting(KEY, "[]"))
}
