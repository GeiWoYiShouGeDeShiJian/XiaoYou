package com.example.jizhangruanjian.ui.settings
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.backup.BackupManager
import com.example.jizhangruanjian.core.database.AppSettingDao
import com.example.jizhangruanjian.core.util.LocalStore
import com.example.jizhangruanjian.data.model.AppSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
// 数据备份与恢复：本地直存（Download/<路径>）加密备份 + 自动备份 + 备份文件数量管理
@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupManager: BackupManager,
    private val appSettingDao: AppSettingDao
) : ViewModel() {
    val storePath = MutableStateFlow("小柚记账")
    private val _dirUri = MutableStateFlow<String?>(null)
    val autoBackupEnabled = MutableStateFlow(false)
    val intervalHours = MutableStateFlow(12)
    val deviceName = MutableStateFlow("")
    val keepCount = MutableStateFlow(50)
    val busy = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)
    val needPassword = MutableStateFlow(false)
    val backupFiles = MutableStateFlow<List<LocalStore.LocalFile>>(emptyList())
    init {
        load()
    }
    // 每次进入页面刷新配置（设置页可能已修改路径）
    fun load() = viewModelScope.launch {
        _dirUri.value = appSettingDao.get("backup_dir_uri")?.value
        storePath.value = _dirUri.value?.let { u -> runCatching { LocalStore.dirName(context, android.net.Uri.parse(u)) }.getOrNull() } ?: appSettingDao.get("local_store_path")?.value?.takeIf { it.isNotBlank() } ?: "小柚记账"
        autoBackupEnabled.value = appSettingDao.get(KEY_AUTO)?.value == "1"
        intervalHours.value = appSettingDao.get(KEY_INTERVAL)?.value?.toIntOrNull() ?: 12
        deviceName.value = appSettingDao.get(KEY_DEVICE)?.value ?: Build.MODEL
        keepCount.value = appSettingDao.get(KEY_KEEP)?.value?.toIntOrNull() ?: 50
    }
    fun clearMessage() { message.value = null }
    fun setAutoBackup(v: Boolean) { autoBackupEnabled.value = v; viewModelScope.launch { if (v) appSettingDao.upsert(AppSetting(KEY_AUTO, "1")) else appSettingDao.delete(KEY_AUTO) } }
    fun setInterval(h: Int) { intervalHours.value = h; viewModelScope.launch { appSettingDao.upsert(AppSetting(KEY_INTERVAL, h.toString())) } }
    fun setDeviceName(n: String) { deviceName.value = n; viewModelScope.launch { appSettingDao.upsert(AppSetting(KEY_DEVICE, n)) } }
    fun setKeepCount(n: Int) { keepCount.value = n; viewModelScope.launch { appSettingDao.upsert(AppSetting(KEY_KEEP, n.toString())) } }
    fun backupNow() = viewModelScope.launch {
        busy.value = true
        val path = doBackup()
        busy.value = false
        message.value = if (path != null) "备份完成：$path" else "备份失败：无法写入 Download/${storePath.value}"
    }
    private suspend fun doBackup(): String? {
        val bytes = try { backupManager.export(localPwd()) } catch (e: Exception) { return null }
        val name = "backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(java.util.Date())}.bk"
        val saved = LocalStore.save(context, storePath.value, _dirUri.value, name, "application/octet-stream", bytes) ?: return null
        appSettingDao.upsert(AppSetting(KEY_LAST, System.currentTimeMillis().toString()))
        trimOldBackups()
        return saved
    }
    private fun trimOldBackups() {
        val files = LocalStore.list(context, storePath.value, _dirUri.value, ".bk")
        files.drop(keepCount.value).forEach { LocalStore.delete(context, it) }
    }
    fun loadBackupFiles() = viewModelScope.launch { backupFiles.value = LocalStore.list(context, storePath.value, _dirUri.value, ".bk") }
    private var lastBytes: ByteArray? = null
    fun restoreFile(file: LocalStore.LocalFile) = viewModelScope.launch {
        val bytes = LocalStore.readBytes(context, file) ?: run { message.value = "读取备份文件失败"; return@launch }
        lastBytes = bytes
        restore(bytes)
    }
    // 密码确认后重新还原
    fun confirmPassword(pwd: String) = viewModelScope.launch {
        val bytes = lastBytes ?: return@launch
        busy.value = true
        val err = backupManager.import(bytes, pwd)
        busy.value = false
        message.value = err ?: "数据已恢复"
    }
    private suspend fun localPwd(): String {
        appSettingDao.get(KEY_LOCAL_PWD)?.value?.let { return it }
        val pwd = UUID.randomUUID().toString()
        appSettingDao.upsert(AppSetting(KEY_LOCAL_PWD, pwd))
        return pwd
    }
    // 恢复：优先用本地备份密码自动还原，失败则要求输入密码
    fun restore(bytes: ByteArray, password: String? = null) = viewModelScope.launch {
        busy.value = true
        val err = backupManager.import(bytes, password ?: localPwd())
        busy.value = false
        if (err != null && password == null) { needPassword.value = true }
        else message.value = err ?: "数据已恢复"
    }
    fun dismissNeedPassword() { needPassword.value = false }
    // 进入首页时按间隔静默自动备份
    fun maybeAutoBackup() = viewModelScope.launch {
        if (autoBackupEnabled.value != true) return@launch
        val last = appSettingDao.get(KEY_LAST)?.value?.toLongOrNull() ?: 0L
        if (System.currentTimeMillis() - last < intervalHours.value * 3_600_000L) return@launch
        doBackup()
    }
    companion object {
        const val KEY_AUTO = "auto_backup_enabled"
        const val KEY_INTERVAL = "auto_backup_interval"
        const val KEY_DEVICE = "device_name"
        const val KEY_KEEP = "backup_keep_count"
        const val KEY_LAST = "last_local_backup"
        const val KEY_LOCAL_PWD = "local_backup_pwd"
    }
}
