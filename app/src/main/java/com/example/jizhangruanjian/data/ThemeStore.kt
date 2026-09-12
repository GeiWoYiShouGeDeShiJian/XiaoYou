package com.example.jizhangruanjian.data
// P15 全局主题状态：深色模式(跟随系统/强制浅/强制深) + 动态色彩 + 当前账本颜色（顶栏随账本变色）
import com.example.jizhangruanjian.core.database.AppSettingDao
import com.example.jizhangruanjian.core.database.LedgerDao
import com.example.jizhangruanjian.data.model.AppSetting
import com.example.jizhangruanjian.ui.theme.ThemeColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class ThemeStore @Inject constructor(
    private val appSettingDao: AppSettingDao,
    private val ledgerDao: LedgerDao,
    private val currentLedgerHolder: CurrentLedgerHolder
) {
    enum class Mode { SYSTEM, LIGHT, DARK }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _mode = MutableStateFlow(Mode.SYSTEM)
    val mode: StateFlow<Mode> = _mode
    private val _dynamicColor = MutableStateFlow(false)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor
    private val _color = MutableStateFlow(ThemeColor.ORANGE)
    val color: StateFlow<ThemeColor> = _color
    private val _ledgerColor = MutableStateFlow<Int?>(null)
    val ledgerColor: StateFlow<Int?> = _ledgerColor
    init {
        scope.launch {
            _mode.value = when (appSettingDao.get(KEY_MODE)?.value) { "light" -> Mode.LIGHT; "dark" -> Mode.DARK; else -> Mode.SYSTEM }
            _dynamicColor.value = appSettingDao.get(KEY_DYNAMIC)?.value != "0"
            _color.value = ThemeColor.entries.firstOrNull { it.key == appSettingDao.get(KEY_COLOR)?.value } ?: ThemeColor.ORANGE
        }
        scope.launch {
            combine(ledgerDao.observeAll(), currentLedgerHolder.id) { ledgers, cid ->
                ledgers.firstOrNull { it.id == cid }?.color ?: ledgers.firstOrNull()?.color
            }.collectLatest { _ledgerColor.value = it }
        }
    }
    fun setMode(m: Mode) { scope.launch { appSettingDao.upsert(AppSetting(KEY_MODE, when (m) { Mode.LIGHT -> "light"; Mode.DARK -> "dark"; Mode.SYSTEM -> "system" })) }; _mode.value = m }
    fun setDynamicColor(v: Boolean) { scope.launch { appSettingDao.upsert(AppSetting(KEY_DYNAMIC, if (v) "1" else "0")) }; _dynamicColor.value = v }
    fun setColor(c: ThemeColor) { scope.launch { appSettingDao.upsert(AppSetting(KEY_COLOR, c.key)) }; _color.value = c }
    companion object {
        const val KEY_MODE = "theme_mode"
        const val KEY_DYNAMIC = "dynamic_color"
        const val KEY_COLOR = "theme_color"
    }
}