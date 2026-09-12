package com.example.jizhangruanjian.core.autorecord
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.example.jizhangruanjian.core.database.AppSettingDao
import com.example.jizhangruanjian.data.model.AppSetting
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoRecordSettings @Inject constructor(
    private val appSettingDao: AppSettingDao
) {
    companion object {
        const val KEY_ENABLED = "auto_record_enabled"
        const val KEY_MODE = "auto_record_mode"
        const val KEY_ACC_ENABLED = "auto_record_acc_enabled"
        const val KEY_NOTIFY_ENABLED = "auto_record_notify_enabled"
        const val KEY_APPS = "auto_record_apps"
        const val KEY_ACCOUNT_WECHAT = "auto_record_account_wechat"
        const val KEY_ACCOUNT_ALIPAY = "auto_record_account_alipay"
        const val KEY_IGNORED = "auto_record_ignored"
        const val KEY_LAST_HIT = "auto_record_last_hit"
        const val KEY_LAST_HEALTH_WARN = "auto_record_last_health_warn"
        const val MODE_OVERLAY = "overlay"
        const val MODE_NOTIFICATION = "notification"
        const val MODE_SILENT = "silent"
        const val DEFAULT_APPS = "com.tencent.mm,com.eg.android.AlipayGphone"
        const val IGNORED_MAX = 500
        const val KEY_SHOW_FAV_CATEGORY = "auto_record_show_fav_category"
        const val KEY_DEFAULT_LEDGER_ID = "auto_record_default_ledger_id"
        const val KEY_DEFAULT_CATEGORY_ID = "auto_record_default_category_id"
        const val KEY_DEFAULT_ACCOUNT_ID = "auto_record_default_account_id"
        const val KEY_DEFAULT_NOTE_MODE = "auto_record_default_note_mode"
        const val KEY_DEFAULT_ROLE_MODE = "auto_record_default_role_mode"
        const val KEY_DEFAULT_MERCHANT_MODE = "auto_record_default_merchant_mode"
        const val KEY_DEFAULT_TAG_MODE = "auto_record_default_tag_mode"
        const val KEY_CHANNEL_TAG = "auto_record_channel_tag"
        const val KEY_INSUFFICIENT_TIP = "auto_record_insufficient_tip"
        const val KEY_CHECK_ON_LAUNCH = "auto_record_check_on_launch"
        const val KEY_DUP_EDIT = "auto_record_dup_edit"
        const val KEY_HIDE_BACKGROUND = "auto_record_hide_background"
        const val KEY_MEM_TAGS = "auto_record_mem_tags"
        const val KEY_MEM_MEMBERS = "auto_record_mem_members"
        const val KEY_MEM_MERCHANT = "auto_record_mem_merchant"
        const val NOTE_MODE_MERCHANT = "merchant"
        const val MODE_MEM_LAST = "mem_last"
        const val MERCHANT_MODE_NONE = "none"
        fun isAccessibilityEnabled(context: Context): Boolean {
            val expected = ComponentName(context, AutoRecordAccessibilityService::class.java)
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager ?: return false
            return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
                .any { info -> info.resolveInfo?.serviceInfo?.let { s -> ComponentName(s.packageName, s.name) } == expected }
        }
        fun isListenerEnabled(context: Context): Boolean =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")?.contains(context.packageName) == true
        fun hasSecureSettings(context: Context): Boolean = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            true
        } catch (_: SecurityException) {
            false
        }
    }
    suspend fun isEnabled(): Boolean = getBool(KEY_ENABLED, false)
    suspend fun setEnabled(v: Boolean) = put(KEY_ENABLED, v.toString())
    suspend fun mode(): String = getString(KEY_MODE, MODE_OVERLAY)
    suspend fun setMode(v: String) = put(KEY_MODE, v)
    suspend fun isAccEnabled(): Boolean = getBool(KEY_ACC_ENABLED, true)
    suspend fun setAccEnabled(v: Boolean) = put(KEY_ACC_ENABLED, v.toString())
    suspend fun isNotifyEnabled(): Boolean = getBool(KEY_NOTIFY_ENABLED, true)
    suspend fun setNotifyEnabled(v: Boolean) = put(KEY_NOTIFY_ENABLED, v.toString())
    suspend fun apps(): List<String> = getString(KEY_APPS, DEFAULT_APPS).split(",").filter { it.isNotBlank() }
    suspend fun setApps(v: List<String>) = put(KEY_APPS, v.joinToString(","))
    suspend fun accountWechat(): Long = getLong(KEY_ACCOUNT_WECHAT, 0L)
    suspend fun setAccountWechat(v: Long) = put(KEY_ACCOUNT_WECHAT, v.toString())
    suspend fun accountAlipay(): Long = getLong(KEY_ACCOUNT_ALIPAY, 0L)
    suspend fun setAccountAlipay(v: Long) = put(KEY_ACCOUNT_ALIPAY, v.toString())
    suspend fun ignoredFingerprints(): List<String> {
        val raw = getString(KEY_IGNORED, "[]")
        return try { val a = JSONArray(raw); (0 until a.length()).map { a.getString(it) } } catch (_: Exception) { emptyList() }
    }
    suspend fun addIgnored(fp: String) {
        val list = ignoredFingerprints().toMutableList()
        list.remove(fp)
        list.add(fp)
        while (list.size > IGNORED_MAX) list.removeAt(0)
        put(KEY_IGNORED, JSONArray(list).toString())
    }
    suspend fun removeIgnored(fp: String) {
        val list = ignoredFingerprints().toMutableList()
        if (list.remove(fp)) put(KEY_IGNORED, JSONArray(list).toString())
    }
    suspend fun clearIgnored() = put(KEY_IGNORED, "[]")
    suspend fun lastHit(): Long = getLong(KEY_LAST_HIT, 0L)
    suspend fun setLastHit(v: Long) = put(KEY_LAST_HIT, v.toString())
    suspend fun lastHealthWarn(): Long = getLong(KEY_LAST_HEALTH_WARN, 0L)
    suspend fun setLastHealthWarn(v: Long) = put(KEY_LAST_HEALTH_WARN, v.toString())
    suspend fun showFavCategory(): Boolean = getBool(KEY_SHOW_FAV_CATEGORY, true)
    suspend fun setShowFavCategory(v: Boolean) = put(KEY_SHOW_FAV_CATEGORY, v.toString())
    suspend fun defaultLedgerId(): Long = getLong(KEY_DEFAULT_LEDGER_ID, 0L)
    suspend fun setDefaultLedgerId(v: Long) = put(KEY_DEFAULT_LEDGER_ID, v.toString())
    suspend fun defaultCategoryId(): Long = getLong(KEY_DEFAULT_CATEGORY_ID, 0L)
    suspend fun setDefaultCategoryId(v: Long) = put(KEY_DEFAULT_CATEGORY_ID, v.toString())
    suspend fun defaultAccountId(): Long = getLong(KEY_DEFAULT_ACCOUNT_ID, 0L)
    suspend fun setDefaultAccountId(v: Long) = put(KEY_DEFAULT_ACCOUNT_ID, v.toString())
    suspend fun defaultNoteMode(): String = getString(KEY_DEFAULT_NOTE_MODE, NOTE_MODE_MERCHANT)
    suspend fun setDefaultNoteMode(v: String) = put(KEY_DEFAULT_NOTE_MODE, v)
    suspend fun defaultRoleMode(): String = getString(KEY_DEFAULT_ROLE_MODE, MODE_MEM_LAST)
    suspend fun setDefaultRoleMode(v: String) = put(KEY_DEFAULT_ROLE_MODE, v)
    suspend fun defaultMerchantMode(): String = getString(KEY_DEFAULT_MERCHANT_MODE, MERCHANT_MODE_NONE)
    suspend fun setDefaultMerchantMode(v: String) = put(KEY_DEFAULT_MERCHANT_MODE, v)
    suspend fun defaultTagMode(): String = getString(KEY_DEFAULT_TAG_MODE, MODE_MEM_LAST)
    suspend fun setDefaultTagMode(v: String) = put(KEY_DEFAULT_TAG_MODE, v)
    suspend fun channelTag(): Boolean = getBool(KEY_CHANNEL_TAG, false)
    suspend fun setChannelTag(v: Boolean) = put(KEY_CHANNEL_TAG, v.toString())
    suspend fun insufficientTip(): Boolean = getBool(KEY_INSUFFICIENT_TIP, true)
    suspend fun setInsufficientTip(v: Boolean) = put(KEY_INSUFFICIENT_TIP, v.toString())
    suspend fun checkOnLaunch(): Boolean = getBool(KEY_CHECK_ON_LAUNCH, true)
    suspend fun setCheckOnLaunch(v: Boolean) = put(KEY_CHECK_ON_LAUNCH, v.toString())
    suspend fun dupEdit(): Boolean = getBool(KEY_DUP_EDIT, true)
    suspend fun setDupEdit(v: Boolean) = put(KEY_DUP_EDIT, v.toString())
    suspend fun hideBackground(): Boolean = getBool(KEY_HIDE_BACKGROUND, false)
    suspend fun setHideBackground(v: Boolean) = put(KEY_HIDE_BACKGROUND, v.toString())
    suspend fun memTags(): List<Long> = getLongList(KEY_MEM_TAGS)
    suspend fun setMemTags(v: List<Long>) = put(KEY_MEM_TAGS, v.joinToString(","))
    suspend fun memMembers(): List<Long> = getLongList(KEY_MEM_MEMBERS)
    suspend fun setMemMembers(v: List<Long>) = put(KEY_MEM_MEMBERS, v.joinToString(","))
    suspend fun memMerchant(): String = getString(KEY_MEM_MERCHANT, "")
    suspend fun setMemMerchant(v: String) = put(KEY_MEM_MERCHANT, v)
    private suspend fun getString(key: String, def: String): String = appSettingDao.get(key)?.value ?: def
    private suspend fun getLongList(key: String): List<Long> = getString(key, "").split(",").filter { it.isNotBlank() }.mapNotNull { it.toLongOrNull() }
    private suspend fun getBool(key: String, def: Boolean): Boolean = appSettingDao.get(key)?.value?.toBooleanStrictOrNull() ?: def
    private suspend fun getLong(key: String, def: Long): Long = appSettingDao.get(key)?.value?.toLongOrNull() ?: def
    private suspend fun put(key: String, value: String) = appSettingDao.upsert(AppSetting(key, value))
}
