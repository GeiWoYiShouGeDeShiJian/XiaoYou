package com.example.jizhangruanjian
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.activity.viewModels
import com.example.jizhangruanjian.data.QuickRecordTrigger
import com.example.jizhangruanjian.data.ThemeStore
import com.example.jizhangruanjian.core.autorecord.AutoRecordSettings
import com.example.jizhangruanjian.core.security.AppLockManager
import com.example.jizhangruanjian.ui.navigation.AppNavGraph
import com.example.jizhangruanjian.ui.record.RecordViewModel
import com.example.jizhangruanjian.ui.theme.MoneyBookTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var quickRecordTrigger: QuickRecordTrigger
    @Inject lateinit var themeStore: ThemeStore
    @Inject lateinit var appLockManager: AppLockManager
    @Inject lateinit var autoRecordSettings: AutoRecordSettings
    private val scope = CoroutineScope(Dispatchers.Main)
    private val recordViewModel by viewModels<RecordViewModel>()
    private val healthAlert = MutableStateFlow<String?>(null)
    private var hideBackground = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hideBackground) finishAndRemoveTask() else finish()
            }
        })
        setContent {
            val mode by themeStore.mode.collectAsState()
            val dynamic by themeStore.dynamicColor.collectAsState()
            val ledgerColor by themeStore.ledgerColor.collectAsState()
            val themeColor by themeStore.color.collectAsState()
            val dark = when (mode) { ThemeStore.Mode.SYSTEM -> isSystemInDarkTheme(); ThemeStore.Mode.LIGHT -> false; ThemeStore.Mode.DARK -> true }
            MoneyBookTheme(darkTheme = dark, themeColor = themeColor, dynamicColor = dynamic, ledgerColor = ledgerColor?.let { Color(it) }) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // P4 底部导航骨架 + 首页
                    AppNavGraph()
                }
            }
            val alert by this@MainActivity.healthAlert.collectAsState()
            alert?.let { msg ->
                AlertDialog(
                    onDismissRequest = { this@MainActivity.healthAlert.value = null },
                    title = { Text("自动记账服务已掉线") },
                    text = { Text(msg) },
                    confirmButton = { TextButton(onClick = { this@MainActivity.healthAlert.value = null }) { Text("知道了") } },
                    dismissButton = {})
            }
        }
        checkQuickRecord(intent)
        scope.launch(Dispatchers.Default) { if (appLockManager.isAppLockEnabled()) lockApp { checkRecordUrl(intent) } else checkRecordUrl(intent) }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkQuickRecord(intent)
        scope.launch(Dispatchers.Default) { if (appLockManager.isAppLockEnabled()) lockApp { checkRecordUrl(intent) } else checkRecordUrl(intent) }
    }
    override fun onResume() {
        super.onResume()
        scope.launch {
            hideBackground = autoRecordSettings.hideBackground()
            checkAutoRecordHealth()
        }
    }
    // 掉线自检：开关开启但两通道均未连接时弹窗提醒（1 小时内不重复）
    private suspend fun checkAutoRecordHealth() {
        if (!autoRecordSettings.isEnabled()) return
        if (!autoRecordSettings.checkOnLaunch()) return
        val accOn = autoRecordSettings.isAccEnabled() && AutoRecordSettings.isAccessibilityEnabled(this)
        val notifyOn = autoRecordSettings.isNotifyEnabled() && AutoRecordSettings.isListenerEnabled(this)
        if (accOn || notifyOn) return
        if (System.currentTimeMillis() - autoRecordSettings.lastHealthWarn() < 60 * 60 * 1000L) return
        autoRecordSettings.setLastHealthWarn(System.currentTimeMillis())
        healthAlert.value = "无障碍与通知监听均未连接，可能原因：进程被杀 / 系统重启 / 开启了高级保护模式（APM）"
    }
    // T10 应用锁：开启时生物识别通过后处理 URL
    private fun lockApp(onSuccess: () -> Unit) {
        if (appLockManager.isHardwareSupported(this)) appLockManager.authenticate(this, onSuccess, onSuccess)
    }
    // T10 URL Scheme：jizhang://record?amount=35&type=EXPENSE&category=餐饮&note=午餐&account=现金&merchant=&source=
    private fun checkRecordUrl(intent: Intent) {
        val url = intent.data ?: return
        if (url.scheme != "jizhang") return
        if (url.host == "undo") {
            runCatching {
                url.getQueryParameter("txId")?.toLongOrNull()?.let { recordViewModel.delete(it) }
            }
            return
        }
        if (url.host != "record") return
        val typeName = url.getQueryParameter("type")
        val type = typeName?.let { runCatching { com.example.jizhangruanjian.data.model.TransactionType.valueOf(it) }.getOrNull() } ?: com.example.jizhangruanjian.data.model.TransactionType.EXPENSE
        val amountYuan = url.getQueryParameter("amount")?.toDoubleOrNull() ?: return
        val cents = (amountYuan * 100).toLong()
        if (cents <= 0L) return
        val sourceName = url.getQueryParameter("source")
        val source = sourceName?.let { runCatching { com.example.jizhangruanjian.data.model.TransactionSource.valueOf(it) }.getOrNull() } ?: com.example.jizhangruanjian.data.model.TransactionSource.MANUAL
        recordViewModel.applyPrefill(Prefill(type = type, amountCents = cents, category = url.getQueryParameter("category"), note = url.getQueryParameter("note"), account = url.getQueryParameter("account"), merchant = url.getQueryParameter("merchant"), source = source))
    }
    // P11 快捷记账 Widget：点击带 QUICK_RECORD 标志启动，通知表单打开
    private fun checkQuickRecord(intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_QUICK_RECORD, false)) {
            scope.launch { delay(300); quickRecordTrigger.fire() }
        }
    }
    companion object {
        const val EXTRA_QUICK_RECORD = "quick_record"
    }
}
// T10 URL Scheme 预填数据
data class Prefill(
    val type: com.example.jizhangruanjian.data.model.TransactionType,
    val amountCents: Long,
    val category: String?,
    val note: String?,
    val account: String?,
    val merchant: String? = null,
    val source: com.example.jizhangruanjian.data.model.TransactionSource = com.example.jizhangruanjian.data.model.TransactionSource.MANUAL
)