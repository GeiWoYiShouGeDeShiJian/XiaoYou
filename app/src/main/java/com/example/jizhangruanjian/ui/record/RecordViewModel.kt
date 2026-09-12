package com.example.jizhangruanjian.ui.record
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.AccountGroupDao
import com.example.jizhangruanjian.core.database.CategoryDao
import com.example.jizhangruanjian.core.database.LedgerDao
import com.example.jizhangruanjian.core.database.MemberDao
import com.example.jizhangruanjian.core.database.MerchantDao
import com.example.jizhangruanjian.core.database.MerchantGroupDao
import com.example.jizhangruanjian.core.database.TransactionImageDao
import com.example.jizhangruanjian.core.database.TransactionTagDao
import com.example.jizhangruanjian.core.database.TagDao
import com.example.jizhangruanjian.core.database.TagGroupDao
import com.example.jizhangruanjian.data.model.Member
import com.example.jizhangruanjian.data.model.Merchant
import com.example.jizhangruanjian.data.model.MerchantGroup
import com.example.jizhangruanjian.data.model.Tag
import com.example.jizhangruanjian.data.model.TagGroup
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.Prefill
import com.example.jizhangruanjian.data.model.Transaction
import com.example.jizhangruanjian.data.model.AccountType
import com.example.jizhangruanjian.data.QuickRecordTrigger
import com.example.jizhangruanjian.data.RecordPrefsStore
import com.example.jizhangruanjian.data.repository.AccountRepository
import com.example.jizhangruanjian.data.repository.BudgetRepository
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.example.jizhangruanjian.domain.model.TransactionDomain
import com.example.jizhangruanjian.domain.usecase.SaveTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@HiltViewModel
class RecordViewModel @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val accountGroupDao: AccountGroupDao,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val categoryDao: CategoryDao,
    private val memberDao: MemberDao,
    private val tagDao: TagDao,
    private val tagGroupDao: TagGroupDao,
    private val merchantDao: MerchantDao,
    private val merchantGroupDao: MerchantGroupDao,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val transactionImageDao: TransactionImageDao,
    private val transactionTagDao: TransactionTagDao,
    private val quickRecordTrigger: QuickRecordTrigger,
    private val recordPrefs: RecordPrefsStore
) : ViewModel() {
    private val currentLedgerId = MutableStateFlow(0L)
    private val frequency = MutableStateFlow<Map<Long, Long>>(emptyMap())
    val selectedType = MutableStateFlow(TransactionType.EXPENSE)
    // P11 快捷记账 Widget 触发（事件时间戳，递增以触发重组）
    val quickRecordEpoch = MutableStateFlow(0L)
    // T10 URL Scheme 预填（事件时间戳）
    val prefillEpoch = MutableStateFlow(0L)
    // 保存成功后小浮窗提示文案（AppNavGraph Popup 展示）
    val saveMessage = MutableStateFlow<String?>(null)
    // S4 编辑目标（长按/右滑编辑填充到表单）
    val editingTarget = MutableStateFlow<Transaction?>(null)
    val accounts: StateFlow<List<AccountDomain>>
    val categories: StateFlow<List<CategoryDomain>>
    val recent: StateFlow<List<TransactionDisplay>>
    val members: StateFlow<List<Member>>
    val tags: StateFlow<List<Tag>>
    val tagGroups: StateFlow<List<TagGroup>>
    val merchants: StateFlow<List<Merchant>>
    val merchantGroups: StateFlow<List<MerchantGroup>>
    val balanceConfirmShown = MutableStateFlow(false)
    val duplicateConfirmShown = MutableStateFlow(false)
    // T7 交易图片：已选图片路径（内存态，保存时随 submitDraft 落库）
    val selectedImagePaths = MutableStateFlow<List<String>>(emptyList())
    val editingTagIds = MutableStateFlow<List<Long>>(emptyList())
    val ledgers: StateFlow<List<com.example.jizhangruanjian.data.model.Ledger>>
    val currentLedgerName: StateFlow<String>
    val ledgerIdFlow: StateFlow<Long> get() = currentLedgerId
    fun selectLedger(id: Long) { currentLedgerId.value = id }
    fun reorderCategories(ids: List<Long>) = viewModelScope.launch { categoryRepository.reorderCategories(ids) }
    fun createCategory(name: String, parentId: Long?, type: com.example.jizhangruanjian.data.model.CategoryType) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        val siblings = categoryRepository.getAll().filter { it.type == type && it.parentId == parentId }
        categoryDao.insert(com.example.jizhangruanjian.data.model.Category(parentId = parentId, name = name, type = type, icon = "🏷️", sortOrder = (siblings.maxOfOrNull { it.sortOrder } ?: -1) + 1))
    }
    private var pendingNew: TransactionDomain? = null
    private var pendingOldId: Long? = null
    private var pendingImages: List<String> = emptyList()
    init {
        viewModelScope.launch { currentLedgerId.value = ledgerDao.observeAll().first().first().id }
        viewModelScope.launch { quickRecordTrigger.events.collect { quickRecordEpoch.value = it } }
        accounts = accountRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        categories = combine(selectedType, frequency) { t, f -> t to f }
            .flatMapLatest { (t, f) -> categoryRepository.observeAll().map { cats -> cats.filter { c -> c.type.name == t.name }.sortedByDescending { f[it.id] ?: 0 } } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        recent = currentLedgerId.flatMapLatest { id -> transactionRepository.observeRecent(id) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        members = memberDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        tags = tagDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        tagGroups = tagGroupDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        merchants = merchantDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        merchantGroups = merchantGroupDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ledgers = ledgerDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        currentLedgerName = combine(ledgers, currentLedgerId) { ls, id -> ls.firstOrNull { it.id == id }?.name ?: "" }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
        viewModelScope.launch {
            combine(currentLedgerId, selectedType) { id, t -> id to t }.collectLatest { (id, t) ->
                if (id > 0L) frequency.value = transactionRepository.countByCategory(id, t.name).associate { it.categoryId to it.total }
            }
        }
    }
    fun setType(t: TransactionType) { selectedType.value = t }
    // T10 URL Scheme 预填：打开记账页并填充表单
    fun applyPrefill(prefill: Prefill) {
        launchedPrefill = prefill
        prefillEpoch.value++
        selectedType.value = prefill.type
    }
    fun consumePrefill(): Prefill? { val p = launchedPrefill; launchedPrefill = null; return p }
    internal var launchedPrefill: Prefill? = null
    fun saveMember(member: Member) {
        viewModelScope.launch {
            if (member.id == 0L) memberDao.insert(member.copy(sortOrder = (memberDao.observeAll().first().maxOfOrNull { it.sortOrder } ?: 0) + 1))
            else memberDao.update(member)
        }
    }
    fun deleteMember(id: Long) {
        viewModelScope.launch { memberDao.delete(id) }
    }
    fun saveTag(tag: Tag) {
        viewModelScope.launch {
            if (tag.id == 0L) tagDao.insert(tag)
            else tagDao.update(tag)
        }
    }
    fun deleteTag(id: Long) {
        viewModelScope.launch {
            transactionTagDao.deleteByTag(id)
            tagDao.getById(id)?.let { tagDao.delete(it) }
        }
    }
    fun saveTagGroup(group: TagGroup) {
        viewModelScope.launch {
            if (group.id == 0L) tagGroupDao.insert(group.copy(sortOrder = (tagGroupDao.observeAll().first().maxOfOrNull { it.sortOrder } ?: 0) + 1))
            else tagGroupDao.update(group)
        }
    }
    fun saveMerchant(merchant: Merchant) {
        viewModelScope.launch {
            if (merchant.id == 0L) merchantDao.insert(merchant.copy(sortOrder = (merchantDao.observeAll().first().filter { it.groupId == merchant.groupId }.maxOfOrNull { it.sortOrder } ?: -1) + 1))
            else merchantDao.update(merchant)
        }
    }
    fun saveMerchantGroup(group: MerchantGroup) {
        viewModelScope.launch {
            if (group.id == 0L) merchantGroupDao.insert(group.copy(sortOrder = (merchantGroupDao.observeAll().first().maxOfOrNull { it.sortOrder } ?: 0) + 1))
            else merchantGroupDao.update(group)
        }
    }
    fun reorderMerchants(ids: List<Long>) = viewModelScope.launch {
        ids.forEachIndexed { index, id -> merchantDao.updateSort(id, index) }
    }
    fun createAccount(name: String) {
        viewModelScope.launch {
            val groupId = accountGroupDao.observeAll().first().first().id
            accountRepository.createAccount(name, groupId, AccountType.CASH)
        }
    }
    // R13 记忆备注关键词到分类
    fun rememberKeyword(categoryId: Long, keyword: String) = viewModelScope.launch {
        categoryRepository.rememberKeyword(categoryId, keyword)
    }
    // T7 图片：添加/移除/编辑加载/清空
    fun addImage(path: String) {
        if (selectedImagePaths.value.size >= 4 || selectedImagePaths.value.contains(path)) return
        selectedImagePaths.value = selectedImagePaths.value + path
    }
    fun removeImage(path: String) {
        selectedImagePaths.value = selectedImagePaths.value - path
        com.example.jizhangruanjian.core.util.ImageSaver.deleteImage(path)
    }
    fun loadImagesForEdit(txId: Long) = viewModelScope.launch {
        selectedImagePaths.value = transactionImageDao.getByTransaction(txId).map { it.path }
    }
    fun clearImages() { selectedImagePaths.value = emptyList() }
    fun loadTagsForEdit(txId: Long) = viewModelScope.launch { editingTagIds.value = transactionTagDao.getTagIdsByTransaction(txId) }
    fun clearEditingTags() { editingTagIds.value = emptyList() }
    fun submitDraft(new: TransactionDomain, oldId: Long?, imagePaths: List<String>) {
        pendingImages = imagePaths
        viewModelScope.launch {
            val old = oldId?.let { transactionRepository.getDomain(it) }
            if (saveTransactionUseCase.needBalanceConfirm(old, new)) {
                pendingNew = new; pendingOldId = oldId; balanceConfirmShown.value = true; return@launch
            }
            checkDuplicateAndSave(new, old, true)
        }
    }
    fun onBalanceConfirm(apply: Boolean) {
        val n = pendingNew ?: return
        pendingNew = null
        viewModelScope.launch {
            val old = pendingOldId?.let { transactionRepository.getDomain(it) }
            pendingOldId = null
            balanceConfirmShown.value = false
            checkDuplicateAndSave(n, old, apply)
        }
    }
    fun cancelBalanceConfirm() { pendingNew = null; pendingOldId = null; balanceConfirmShown.value = false }
    fun onDuplicateConfirm(saveAnyway: Boolean) {
        val n = pendingNew ?: return
        pendingNew = null
        viewModelScope.launch {
            val old = pendingOldId?.let { transactionRepository.getDomain(it) }
            duplicateConfirmShown.value = false
            if (saveAnyway) { saveTransactionUseCase.execute(n, old, true, pendingImages); buildSaveMessage(n) }
        }
    }
    private suspend fun checkDuplicateAndSave(new: TransactionDomain, old: TransactionDomain?, sync: Boolean) {
        val dup = saveTransactionUseCase.checkDuplicate(new, old)
        if (dup != null) { pendingNew = new; pendingOldId = old?.id; duplicateConfirmShown.value = true; return }
        saveTransactionUseCase.execute(new, old, sync, pendingImages)
        buildSaveMessage(new)
    }
    // 保存成功小浮窗：预算有占用/超支时附加提醒
    private fun buildSaveMessage(new: TransactionDomain) {
        viewModelScope.launch {
            var msg = "保存成功"
            val b = budgetRepository.totalState(new.ledgerId)
            if (b != null) msg += if (b.over) "，本月预算已超支" else if (b.available > 0L) "，本月预算已用 ${String.format(java.util.Locale.CHINA, "%.0f", b.usagePercent)}%" else ""
            saveMessage.value = msg
        }
    }
    fun consumeSaveMessage() { saveMessage.value = null }
    // S4 交易列表操作
    fun startEdit(id: Long) = viewModelScope.launch { editingTarget.value = transactionRepository.getById(id) }
    fun clearEditTarget() { editingTarget.value = null; editingTagIds.value = emptyList() }
    fun delete(id: Long) = viewModelScope.launch { transactionRepository.softDelete(id) }
    fun copy(id: Long) = viewModelScope.launch {
        val t = transactionRepository.getById(id) ?: return@launch
        val keepTime = recordPrefs.get("rp_copy_keep_time") == "1"
        transactionRepository.save(t.copy(id = 0L, note = t.note + "（复制）", dedupHash = null, deletedAt = null, tradeDate = if (keepTime) t.tradeDate else System.currentTimeMillis(), createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()), null, true)
    }
    // 打开记账页时应用「初始选中类型」偏好（编辑/预填不覆盖）
    fun applyInitialType() {
        if (editingTarget.value != null) return
        selectedType.value = runCatching { TransactionType.valueOf(recordPrefs.get("rp_init_type", "EXPENSE")) }.getOrDefault(TransactionType.EXPENSE)
    }
    fun recategorize(id: Long, categoryId: Long) = viewModelScope.launch {
        val old = transactionRepository.getDomain(id) ?: return@launch
        saveTransactionUseCase.execute(old.copy(categoryId = categoryId), old, false)
    }
}