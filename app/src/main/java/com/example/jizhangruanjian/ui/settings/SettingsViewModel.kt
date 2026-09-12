package com.example.jizhangruanjian.ui.settings
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.AccountDao
import com.example.jizhangruanjian.core.database.AppSettingDao
import com.example.jizhangruanjian.core.database.CategoryDao
import com.example.jizhangruanjian.core.database.LedgerDao
import com.example.jizhangruanjian.core.parser.CsvParser
import com.example.jizhangruanjian.core.parser.CsvRow
import com.example.jizhangruanjian.core.parser.ExportRow
import com.example.jizhangruanjian.core.parser.OfficialBillParser
import com.example.jizhangruanjian.core.util.KeywordMatcher
import com.example.jizhangruanjian.core.backup.BackupManager
import com.example.jizhangruanjian.core.security.AppLockManager
import com.example.jizhangruanjian.core.util.LocalStore
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.HomeUiStore
import com.example.jizhangruanjian.data.ThemeStore
import com.example.jizhangruanjian.data.model.AppSetting
import com.example.jizhangruanjian.data.model.Category
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.model.Ledger
import com.example.jizhangruanjian.data.model.Transaction
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.ui.theme.ThemeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val ledgerDao: LedgerDao,
    private val appSettingDao: AppSettingDao,
    private val currentLedgerHolder: CurrentLedgerHolder,
    private val backupManager: BackupManager,
    private val themeStore: ThemeStore,
    private val appLockManager: AppLockManager,
    private val homeUiStore: HomeUiStore
) : ViewModel() {
    val homeHiddenSections = homeUiStore.hidden
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
    private val _backupBytes = MutableStateFlow<ByteArray?>(null)
    val backupBytes: StateFlow<ByteArray?> = _backupBytes
    private val _preview = MutableStateFlow<List<PreviewRow>?>(null)
    val preview: StateFlow<List<PreviewRow>?> = _preview
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    val importing = MutableStateFlow(false)
    // P1 官方账单：待导入的解析结果与来源（"wechat"/"alipay"/null），供预览与确认导入共用
    private var pendingRows: List<CsvRow> = emptyList()
    private var pendingSource: String? = null
    // P15 主题设置
    val themeMode = themeStore.mode
    val dynamicColor = themeStore.dynamicColor
    val themeColor = themeStore.color
    fun setThemeMode(m: ThemeStore.Mode) = themeStore.setMode(m)
    fun setDynamicColor(v: Boolean) = themeStore.setDynamicColor(v)
    fun setThemeColor(c: ThemeColor) = themeStore.setColor(c)
    // T10 应用锁
    val appLockEnabled = MutableStateFlow(false)
    fun loadAppLock() = viewModelScope.launch { appLockEnabled.value = appLockManager.isAppLockEnabled() }
    fun setAppLock(v: Boolean) = viewModelScope.launch { appLockManager.setAppLockEnabled(v); appLockEnabled.value = v }
    // 报表：每月起始日（1-28，存 app_setting）
    val monthStart = MutableStateFlow(1)
    fun loadMonthStart() = viewModelScope.launch { monthStart.value = appSettingDao.get("report_month_start")?.value?.toIntOrNull() ?: 1 }
    fun setMonthStart(d: Int) = viewModelScope.launch { monthStart.value = d; appSettingDao.upsert(AppSetting("report_month_start", d.toString())) }
    fun setHomeSectionHidden(s: HomeUiStore.HomeSection, hidden: Boolean) = homeUiStore.toggleHidden(s, hidden)
    // 当前账本名称（账单导入导出页展示）
    val ledgerName = MutableStateFlow("")
    val currentLedgerId: StateFlow<Long> = currentLedgerHolder.id
    val ledgers = MutableStateFlow<List<Ledger>>(emptyList())
    // 本地存储路径：默认 Download/小柚记账，可在设置中选择自定义目录（SAF）
    val storePath = MutableStateFlow("小柚记账")
    private val _dirUri = MutableStateFlow<String?>(null)
    init {
        viewModelScope.launch { currentLedgerHolder.id.collect { id -> ledgerName.value = if (id > 0L) ledgerDao.getById(id)?.name ?: "" else "" } }
        viewModelScope.launch { ledgerDao.observeAll().collect { ledgers.value = it } }
        viewModelScope.launch {
            _dirUri.value = appSettingDao.get("backup_dir_uri")?.value
            storePath.value = _dirUri.value?.let { u -> runCatching { LocalStore.dirName(context, android.net.Uri.parse(u)) }.getOrNull() } ?: appSettingDao.get("local_store_path")?.value?.takeIf { it.isNotBlank() } ?: "小柚记账"
        }
    }
    // 设置路径时弹出系统文件夹选择器，选择后持久化授权
    fun saveDir(uri: android.net.Uri) = viewModelScope.launch {
        LocalStore.takePersist(context, uri)
        val name = LocalStore.dirName(context, uri)
        _dirUri.value = uri.toString()
        storePath.value = name
        appSettingDao.upsert(AppSetting("backup_dir_uri", uri.toString()))
        appSettingDao.upsert(AppSetting("backup_dir_name", name))
    }
    // 恢复默认 Download/小柚记账
    fun resetDir() = viewModelScope.launch {
        _dirUri.value = null
        storePath.value = "小柚记账"
        appSettingDao.delete("backup_dir_uri")
        appSettingDao.delete("backup_dir_name")
        appSettingDao.delete("local_store_path")
    }
    fun exportCsvToLocal(start: Long? = null, end: Long? = null, ledgerId: Long? = null, fields: List<String>? = null) = viewModelScope.launch {
        val txs = transactionRepository.getByLedger(ledgerId ?: currentLedgerHolder.id.value).filter { (start == null || it.tradeDate >= start) && (end == null || it.tradeDate <= end) }
        val rows = txs.map { ExportRow(dateFmt.format(java.util.Date(it.tradeDate)), CsvParser.typeLabel(it.type), it.categoryName, it.accountName, java.lang.String.format(java.util.Locale.CHINA, "%.2f", it.amount / 100.0), it.note, "") }
        val name = "账单_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(java.util.Date())}.csv"
        val path = LocalStore.save(context, storePath.value, _dirUri.value, name, "text/csv", CsvParser.export(rows, fields ?: CsvParser.fieldNames()).toByteArray())
        _message.value = if (path != null) "已导出到 $path" else "导出失败：无法写入存储目录"
    }
    fun writeLocal(name: String, bytes: ByteArray) = viewModelScope.launch {
        val path = LocalStore.save(context, storePath.value, _dirUri.value, name, "text/csv", bytes)
        _message.value = if (path != null) "已保存到 $path" else "保存失败：无法写入存储目录"
    }
    // P14 加密备份导出
    fun exportBackup(password: String) = viewModelScope.launch {
        if (password.isBlank()) return@launch
        _backupBytes.value = backupManager.export(password)
    }
    fun clearBackup() { _backupBytes.value = null }
    // P14 加密备份导入（密码错误返回提示）
    fun importBackup(bytes: ByteArray, password: String) = viewModelScope.launch {
        val err = backupManager.import(bytes, password)
        _message.value = err ?: "备份已还原"
    }
    fun loadPreview(text: String) = viewModelScope.launch {
        val accounts = accountDao.observeAll().first()
        val cats = categoryDao.getAll()
        val res = OfficialBillParser.parseResult(text)
        pendingRows = res.rows
        pendingSource = res.source
        _preview.value = res.rows.map { row ->
            val acc = resolveAccount(row, accounts)
            val cat = matchCategory(row, cats, res.source != null)
            val dup = row.date != null && row.type != null && row.amountCents != null && acc != null && cat != null &&
                transactionRepository.findByDedupHash(dedupHash(acc.id, row.date!!, row.amountCents!!, row.type!!)) != null
            val valid = row.date != null && row.type != null && row.amountCents != null && acc != null && cat != null
            PreviewRow(
                date = row.date?.let { dateFmt.format(java.util.Date(it)) } ?: "-",
                typeLabel = row.type?.let { CsvParser.typeLabel(it) } ?: "-",
                category = cat?.name ?: row.subcategory ?: row.category ?: "-",
                account = acc?.name ?: row.account ?: "-",
                amountYuan = row.amountCents?.let { java.lang.String.format(java.util.Locale.CHINA, "%.2f", it / 100.0) } ?: "-",
                note = row.note,
                valid = valid, duplicate = dup
            )
        }
    }
    fun clearPreview() { _preview.value = null }
    fun importCurrent(text: String, ledgerId: Long? = null) = viewModelScope.launch {
        importing.value = true
        val ledgerId2 = ledgerId ?: currentLedgerHolder.id.value
        val accounts = accountDao.observeAll().first()
        val cats = categoryDao.getAll()
        val res = OfficialBillParser.parseResult(text)
        val rows = res.rows.ifEmpty { pendingRows }
        val source = res.source ?: pendingSource
        val now = System.currentTimeMillis()
        var added = 0; var skippedDup = 0; var skippedInvalid = 0
        rows.forEach { row ->
            val acc = resolveAccount(row, accounts)
            val toAcc = row.toAccount?.let { a -> accounts.firstOrNull { it.name == a } }
            val cat = matchCategory(row, cats, source != null)
            if (row.date == null || row.type == null || row.amountCents == null || acc == null || cat == null) { skippedInvalid++; return@forEach }
            val tx = Transaction(ledgerId = ledgerId2, accountId = acc.id, toAccountId = toAcc?.id, categoryId = cat.id, type = row.type!!, amount = row.amountCents!!, note = row.note, tradeDate = row.date!!, includeInSummary = true, isRecurringGenerated = false, dedupHash = dedupHash(acc.id, row.date!!, row.amountCents!!, row.type!!), merchant = row.merchant, loanDirection = row.loanDirection, createdAt = now, updatedAt = now)
            if (transactionRepository.importOne(tx)) added++ else skippedDup++
        }
        importing.value = false
        _preview.value = null
        pendingRows = emptyList()
        pendingSource = null
        _message.value = "导入完成：新增 $added，重复跳过 $skippedDup，无效 $skippedInvalid"
    }
    fun clearMessage() { _message.value = null }
    private fun dedupHash(accountId: Long, tradeDate: Long, amount: Long, type: com.example.jizhangruanjian.data.model.TransactionType): String {
        val input = "$accountId$tradeDate$amount${type.name}"
        return MessageDigest.getInstance("MD5").digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
    // P1 账户解析：名称精确匹配优先；官方账单未指明账户时按来源关键词匹配，再兜底首个账户
    private fun resolveAccount(row: CsvRow, accounts: List<com.example.jizhangruanjian.data.model.Account>): com.example.jizhangruanjian.data.model.Account? {
        row.account?.let { a -> accounts.firstOrNull { it.name == a } }?.let { return it }
        if (pendingSource == null) return null
        val kw = if (pendingSource == "wechat") "微信" else "支付宝"
        accounts.firstOrNull { it.name.contains(kw) }?.let { return it }
        return accounts.firstOrNull()
    }
    // 分类匹配：优先显式小类/大类，其次按名称；官方账单（无显式分类）按 商家/备注 关键词匹配，再兜底该类型首个分类
    private fun matchCategory(row: CsvRow, cats: List<Category>, official: Boolean = false): Category? {
        val t = row.type ?: return null
        val catTypeName = if (t == com.example.jizhangruanjian.data.model.TransactionType.INCOME) CategoryType.INCOME.name else CategoryType.EXPENSE.name
        val candidates = cats.filter { it.type.name == catTypeName }
        if (official && row.category == null && row.subcategory == null) {
            val keyword = row.merchant ?: row.note
            if (keyword.isNotBlank()) {
                KeywordMatcher.match(keyword, candidates.map { com.example.jizhangruanjian.domain.model.CategoryDomain(it.id, it.parentId, it.name, it.type, it.icon, it.sortOrder, it.keywordMatch, it.isFavorite) })?.let { m -> cats.firstOrNull { it.id == m.id }?.let { return it } }
            }
            return candidates.firstOrNull()
        }
        val parent = row.category?.let { p -> candidates.firstOrNull { it.name == p && it.parentId == null } }
        val sub = row.subcategory?.let { s -> candidates.firstOrNull { it.name == s && (parent == null || it.parentId == parent.id) } }
        return sub ?: row.category?.let { c -> candidates.firstOrNull { it.name == c } } ?: parent
            ?: if (t == com.example.jizhangruanjian.data.model.TransactionType.TRANSFER || t == com.example.jizhangruanjian.data.model.TransactionType.LOAN) candidates.firstOrNull() else null
    }
    data class PreviewRow(val date: String, val typeLabel: String, val category: String, val account: String, val amountYuan: String, val note: String, val valid: Boolean, val duplicate: Boolean)
}