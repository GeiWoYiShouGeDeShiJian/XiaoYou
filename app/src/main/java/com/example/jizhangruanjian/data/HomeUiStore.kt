package com.example.jizhangruanjian.data
import com.example.jizhangruanjian.core.database.AppSettingDao
import com.example.jizhangruanjian.data.model.AppSetting
import com.example.jizhangruanjian.data.model.HomeConfig
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class HomeUiStore @Inject constructor(private val appSettingDao: AppSettingDao) {
    enum class HomeSection(val key: String, val title: String) {
        BALANCE("balance", "本月结余"),
        STATS("stats", "今日支出"),
        CATEGORY("category", "支出分类占比"),
        BUDGET("budget", "本期预算"),
        QUICK_ENTRANCE("quick", "快捷入口"),
        RECENT("recent", "最近交易")
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()
    private val _hidden = MutableStateFlow<Set<HomeSection>>(emptySet())
    val hidden: StateFlow<Set<HomeSection>> = _hidden
    private val _config = MutableStateFlow(HomeConfig())
    val config: StateFlow<HomeConfig> = _config
    init {
        scope.launch {
            val keys = appSettingDao.get(KEY_HIDDEN)?.value?.split("|")?.toSet().orEmpty()
            _hidden.value = HomeSection.entries.filter { keys.contains(it.key) }.toSet()
            val saved = appSettingDao.get(KEY_CONFIG)?.value
            _config.value = if (saved == null) migrateFromHidden(_hidden.value) else runCatching { gson.fromJson(saved, HomeConfig::class.java) }.getOrDefault(HomeConfig()).let { migrateOldCover(it) }
        }
    }
    private fun migrateFromHidden(hidden: Set<HomeSection>): HomeConfig = HomeConfig().copy(
        showCategoryRatio = !hidden.contains(HomeSection.CATEGORY),
        showBudget = !hidden.contains(HomeSection.BUDGET),
        showQuickActions = !hidden.contains(HomeSection.QUICK_ENTRANCE),
        showRecentTransactions = !hidden.contains(HomeSection.RECENT)
    )
    private fun migrateOldCover(config: HomeConfig): HomeConfig = if (config.ledgerCover.startsWith("default_")) config.copy(ledgerCover = "cover_pencils") else config
    fun toggleHidden(section: HomeSection, hidden: Boolean) {
        scope.launch {
            val next = if (hidden) _hidden.value + section else _hidden.value - section
            _hidden.value = next
            appSettingDao.upsert(AppSetting(KEY_HIDDEN, next.joinToString("|") { it.key }))
        }
    }
    fun updateConfig(transform: (HomeConfig) -> HomeConfig) {
        val next = transform(_config.value)
        _config.value = next
        scope.launch { appSettingDao.upsert(AppSetting(KEY_CONFIG, gson.toJson(next))) }
    }
    companion object {
        const val KEY_HIDDEN = "home_hidden_sections"
        const val KEY_CONFIG = "home_config"
    }
}