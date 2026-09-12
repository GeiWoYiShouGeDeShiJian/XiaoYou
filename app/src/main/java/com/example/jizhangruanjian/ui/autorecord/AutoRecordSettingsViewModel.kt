package com.example.jizhangruanjian.ui.autorecord
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.autorecord.AutoRecordAccessibilityService
import com.example.jizhangruanjian.core.autorecord.AutoRecordLogger
import com.example.jizhangruanjian.core.autorecord.AutoRecordPipeline
import com.example.jizhangruanjian.core.autorecord.AutoRecordSettings
import com.example.jizhangruanjian.data.repository.AccountRepository
import com.example.jizhangruanjian.domain.model.AccountDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AutoRecordSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: AutoRecordSettings,
    private val logger: AutoRecordLogger,
    private val accountRepository: AccountRepository,
    private val pipeline: AutoRecordPipeline
) : ViewModel() {
    val enabled = MutableStateFlow(false)
    val mode = MutableStateFlow(AutoRecordSettings.MODE_OVERLAY)
    val accEnabled = MutableStateFlow(true)
    val notifyEnabled = MutableStateFlow(true)
    val apps = MutableStateFlow<List<String>>(emptyList())
    val accountWechat = MutableStateFlow(0L)
    val accountAlipay = MutableStateFlow(0L)
    val lastHit = MutableStateFlow(0L)
    val logCount = MutableStateFlow(0)
    val ignoredCount = MutableStateFlow(0)
    val ignoredList = MutableStateFlow<List<String>>(emptyList())
    val secureGranted = MutableStateFlow(false)
    val healthWarn = MutableStateFlow<String?>(null)
    val showFavCategory = MutableStateFlow(true)
    val defaultLedgerId = MutableStateFlow(0L)
    val defaultCategoryId = MutableStateFlow(0L)
    val defaultAccountId = MutableStateFlow(0L)
    val defaultNoteMode = MutableStateFlow(AutoRecordSettings.NOTE_MODE_MERCHANT)
    val defaultRoleMode = MutableStateFlow(AutoRecordSettings.MODE_MEM_LAST)
    val defaultMerchantMode = MutableStateFlow(AutoRecordSettings.MERCHANT_MODE_NONE)
    val defaultTagMode = MutableStateFlow(AutoRecordSettings.MODE_MEM_LAST)
    val channelTag = MutableStateFlow(false)
    val insufficientTip = MutableStateFlow(true)
    val checkOnLaunch = MutableStateFlow(true)
    val dupEdit = MutableStateFlow(true)
    val hideBackground = MutableStateFlow(false)
    val accounts: StateFlow<List<AccountDomain>> = accountRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun load() {
        viewModelScope.launch {
            enabled.value = settings.isEnabled()
            mode.value = settings.mode()
            accEnabled.value = settings.isAccEnabled()
            notifyEnabled.value = settings.isNotifyEnabled()
            apps.value = settings.apps()
            accountWechat.value = settings.accountWechat()
            accountAlipay.value = settings.accountAlipay()
            lastHit.value = settings.lastHit()
            logCount.value = logger.entries().size
            ignoredCount.value = settings.ignoredFingerprints().size
            secureGranted.value = AutoRecordSettings.hasSecureSettings(context)
            loadExtended()
            refreshPermissions()
        }
    }
    fun loadExtended() {
        viewModelScope.launch {
            showFavCategory.value = settings.showFavCategory()
            defaultLedgerId.value = settings.defaultLedgerId()
            defaultCategoryId.value = settings.defaultCategoryId()
            defaultAccountId.value = settings.defaultAccountId()
            defaultNoteMode.value = settings.defaultNoteMode()
            defaultRoleMode.value = settings.defaultRoleMode()
            defaultMerchantMode.value = settings.defaultMerchantMode()
            defaultTagMode.value = settings.defaultTagMode()
            channelTag.value = settings.channelTag()
            insufficientTip.value = settings.insufficientTip()
            checkOnLaunch.value = settings.checkOnLaunch()
            dupEdit.value = settings.dupEdit()
            hideBackground.value = settings.hideBackground()
        }
    }
    fun setShowFavCategory(v: Boolean) = viewModelScope.launch { settings.setShowFavCategory(v); showFavCategory.value = v }
    fun setDefaultLedgerId(v: Long) = viewModelScope.launch { settings.setDefaultLedgerId(v); defaultLedgerId.value = v }
    fun setDefaultCategoryId(v: Long) = viewModelScope.launch { settings.setDefaultCategoryId(v); defaultCategoryId.value = v }
    fun setDefaultAccountId(v: Long) = viewModelScope.launch { settings.setDefaultAccountId(v); defaultAccountId.value = v }
    fun setDefaultNoteMode(v: String) = viewModelScope.launch { settings.setDefaultNoteMode(v); defaultNoteMode.value = v }
    fun setDefaultRoleMode(v: String) = viewModelScope.launch { settings.setDefaultRoleMode(v); defaultRoleMode.value = v }
    fun setDefaultMerchantMode(v: String) = viewModelScope.launch { settings.setDefaultMerchantMode(v); defaultMerchantMode.value = v }
    fun setDefaultTagMode(v: String) = viewModelScope.launch { settings.setDefaultTagMode(v); defaultTagMode.value = v }
    fun setChannelTag(v: Boolean) = viewModelScope.launch { settings.setChannelTag(v); channelTag.value = v }
    fun setInsufficientTip(v: Boolean) = viewModelScope.launch { settings.setInsufficientTip(v); insufficientTip.value = v }
    fun setCheckOnLaunch(v: Boolean) = viewModelScope.launch { settings.setCheckOnLaunch(v); checkOnLaunch.value = v }
    fun setDupEdit(v: Boolean) = viewModelScope.launch { settings.setDupEdit(v); dupEdit.value = v }
    fun setHideBackground(v: Boolean) = viewModelScope.launch { settings.setHideBackground(v); hideBackground.value = v }
    fun setEnabled(v: Boolean) = viewModelScope.launch { settings.setEnabled(v); enabled.value = v }
    fun setMode(v: String) = viewModelScope.launch { settings.setMode(v); mode.value = v }
    fun setAccEnabled(v: Boolean) = viewModelScope.launch { settings.setAccEnabled(v); accEnabled.value = v }
    fun setNotifyEnabled(v: Boolean) = viewModelScope.launch { settings.setNotifyEnabled(v); notifyEnabled.value = v }
    fun setApps(v: List<String>) = viewModelScope.launch { settings.setApps(v); apps.value = v }
    fun setAccountWechat(id: Long) = viewModelScope.launch { settings.setAccountWechat(id); accountWechat.value = id }
    fun setAccountAlipay(id: Long) = viewModelScope.launch { settings.setAccountAlipay(id); accountAlipay.value = id }
    fun isAccessibilityEnabled(): Boolean {
        val expected = ComponentName(context, AutoRecordAccessibilityService::class.java)
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager ?: return false
        return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            .any { info -> info.resolveInfo?.serviceInfo?.let { s -> ComponentName(s.packageName, s.name) } == expected }
    }
    fun isListenerEnabled(): Boolean =
        Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")?.contains(context.packageName) == true
    fun isSideloaded(): Boolean = try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName == null
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName) == null
        }
    } catch (_: Exception) { true }
    fun openAccessibilitySettings() { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    fun openNotificationListenerSettings() { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    fun openAppDetails() { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    fun openBatteryOptimizationRequest() { context.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    fun isIgnoringBatteryOptimizations(): Boolean =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(context.packageName)
    // 掉线自检：总开关开启但两通道均未连接时提示
    fun checkHealth() {
        viewModelScope.launch {
            if (!settings.isEnabled()) return@launch
            val accOn = settings.isAccEnabled() && isAccessibilityEnabled()
            val notifyOn = settings.isNotifyEnabled() && isListenerEnabled()
            healthWarn.value = if (accOn || notifyOn) null else "无障碍与通知监听均未连接，可能原因：进程被杀 / 系统重启 / 开启了高级保护模式（APM）"
        }
    }
    fun loadIgnored() {
        viewModelScope.launch {
            val list = settings.ignoredFingerprints()
            ignoredList.value = list
            ignoredCount.value = list.size
        }
    }
    fun removeIgnored(fp: String) {
        viewModelScope.launch {
            settings.removeIgnored(fp)
            val list = settings.ignoredFingerprints()
            ignoredList.value = list
            ignoredCount.value = list.size
        }
    }
    fun clearIgnored() {
        viewModelScope.launch {
            settings.clearIgnored()
            ignoredList.value = emptyList()
            ignoredCount.value = 0
        }
    }
    // 自动记账测试按钮：生成一笔模拟账单触发自动记账链路；无悬浮窗权限时提示引导开启
    val overlayMissing = MutableStateFlow(false)
    val testHint = MutableStateFlow<String?>(null)
    val missingPerms = MutableStateFlow<List<String>>(emptyList())
    fun refreshPermissions() {
        missingPerms.value = buildList {
            if (!Settings.canDrawOverlays(context)) add("overlay")
            if (!isAccessibilityEnabled()) add("acc")
            if (!isListenerEnabled()) add("notify")
        }
    }
    fun testRecord() {
        if (!Settings.canDrawOverlays(context)) {
            overlayMissing.value = true
            refreshPermissions()
        }
        viewModelScope.launch {
            val txId = pipeline.simulateHit()
            testHint.value = if (txId > 0L) null else "测试未入账：请先确认已创建账本、至少一个账户，并配置默认账本/默认账户"
        }
    }
    fun dismissOverlayMissing() { overlayMissing.value = false }
    fun openOverlaySettings() {
        overlayMissing.value = false
        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    // 高级修复：已授权 WRITE_SECURE_SETTINGS 时直接写入无障碍服务列表
    fun enableAccessibility() {
        try {
            val component = ComponentName(context, AutoRecordAccessibilityService::class.java)
            val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
            val services = enabled.split(':').filter { it.isNotBlank() }.toMutableList()
            if (component.flattenToString() !in services) services.add(component.flattenToString())
            Settings.Secure.putString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, services.joinToString(":"))
            Settings.Secure.putString(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, "1")
            secureGranted.value = true
            accEnabled.value = true
        } catch (_: SecurityException) {
            secureGranted.value = false
        }
    }
}
