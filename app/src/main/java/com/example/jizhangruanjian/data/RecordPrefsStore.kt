package com.example.jizhangruanjian.data
import com.example.jizhangruanjian.core.database.AppSettingDao
import com.example.jizhangruanjian.data.model.AppSetting
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
// 记账偏好设置存储：rp_ 前缀键，同步加载供组合期直接读取
@Singleton
class RecordPrefsStore @Inject constructor(private val dao: AppSettingDao) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _prefs = MutableStateFlow<Map<String, String>>(emptyMap())
    val prefs: StateFlow<Map<String, String>> = _prefs
    init {
        runBlocking { runCatching { _prefs.value = dao.observeAll().first().filter { it.key.startsWith("rp_") }.associate { it.key to it.value } } }
    }
    fun get(key: String, def: String = ""): String = _prefs.value[key] ?: def
    fun set(key: String, value: String) {
        _prefs.value = _prefs.value + (key to value)
        scope.launch { dao.upsert(AppSetting(key, value)) }
    }
}
