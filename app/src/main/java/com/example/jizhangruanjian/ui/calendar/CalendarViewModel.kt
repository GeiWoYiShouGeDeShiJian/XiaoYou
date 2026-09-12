package com.example.jizhangruanjian.ui.calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.LedgerDao
import com.example.jizhangruanjian.core.database.MemberDao
import com.example.jizhangruanjian.core.database.TagDao
import com.example.jizhangruanjian.core.database.TransactionTagDao
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.HomeUiStore
import com.example.jizhangruanjian.data.model.Ledger
import com.example.jizhangruanjian.data.model.Member
import com.example.jizhangruanjian.data.model.Tag
import com.example.jizhangruanjian.data.model.PaymentStatus
import com.example.jizhangruanjian.data.model.ReimbursementStatus
import com.example.jizhangruanjian.data.model.RefundStatus
import com.example.jizhangruanjian.data.model.TransactionSource
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.repository.AccountRepository
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@HiltViewModel
class CalendarViewModel @Inject constructor(private val repo: TransactionRepository, private val holder: CurrentLedgerHolder, val store: HomeUiStore, accountRepo: AccountRepository, categoryRepo: CategoryRepository, ledgerDao: LedgerDao, memberDao: MemberDao, tagDao: TagDao, private val transactionTagDao: TransactionTagDao) : ViewModel() {
    private val gson = Gson()
    data class DayCell(val income: Long, val expense: Long)
    data class AdvFilter(val type: TransactionType? = null, val ledgerId: Long? = null, val accountId: Long? = null, val categoryId: Long? = null, val memberId: Long? = null, val tagId: Long? = null, val merchant: String = "", val currency: String? = null, val reimbursement: Int? = null, val paymentStatus: Int? = null, val minAmount: Long? = null, val maxAmount: Long? = null, val startDate: LocalDate? = null, val endDate: LocalDate? = null, val onlyWithImage: Boolean = false, val onlyRefund: Boolean = false, val onlyDiscount: Boolean = false, val onlyNotInSummary: Boolean = false, val onlyWithLocation: Boolean = false, val note: String = "", val source: Int? = null, val includeInBudget: Int? = null) {
        val hasAny: Boolean get() = type != null || ledgerId != null || accountId != null || categoryId != null || memberId != null || tagId != null || merchant.isNotBlank() || currency != null || reimbursement != null || paymentStatus != null || minAmount != null || maxAmount != null || startDate != null || endDate != null || onlyWithImage || onlyRefund || onlyDiscount || onlyNotInSummary || onlyWithLocation || note.isNotBlank() || source != null || includeInBudget != null
    }
    data class AdvPreset(val type: String? = null, val ledgerId: Long? = null, val accountId: Long? = null, val categoryId: Long? = null, val memberId: Long? = null, val tagId: Long? = null, val merchant: String = "", val currency: String? = null, val reimbursement: Int? = null, val paymentStatus: Int? = null, val minAmount: Long? = null, val maxAmount: Long? = null, val startDay: Long? = null, val endDay: Long? = null, val onlyWithImage: Boolean = false, val onlyRefund: Boolean = false, val onlyDiscount: Boolean = false, val onlyNotInSummary: Boolean = false, val onlyWithLocation: Boolean = false, val note: String = "", val source: Int? = null, val includeInBudget: Int? = null)
    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month
    private val _selected = MutableStateFlow(LocalDate.now())
    val selected: StateFlow<LocalDate> = _selected
    private val _filter = MutableStateFlow(0)
    val filter: StateFlow<Int> = _filter
    private val _cells = MutableStateFlow<Map<LocalDate, DayCell>>(emptyMap())
    val cells: StateFlow<Map<LocalDate, DayCell>> = _cells
    private val _dayTx = MutableStateFlow<Map<LocalDate, List<TransactionDisplay>>>(emptyMap())
    val dayTx: StateFlow<Map<LocalDate, List<TransactionDisplay>>> = _dayTx
    private val _adv = MutableStateFlow(AdvFilter())
    val adv: StateFlow<AdvFilter> = _adv
    private val reloadTick = MutableStateFlow(0)
    val accounts: StateFlow<List<AccountDomain>> = accountRepo.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val categories: StateFlow<List<CategoryDomain>> = categoryRepo.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val ledgers: StateFlow<List<Ledger>> = ledgerDao.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val members: StateFlow<List<Member>> = memberDao.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val tags: StateFlow<List<Tag>> = tagDao.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val hasAdvPreset: StateFlow<Boolean> = store.config.map { it.advPreset.isNotBlank() }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    private val zone: ZoneId = ZoneId.systemDefault()
    init {
        viewModelScope.launch {
            combine(_month, holder.id, reloadTick, _adv) { m, _, _, f -> m to f }.collectLatest { (m, f) -> load(m, f) }
        }
    }
    fun prevMonth() = changeMonth(_month.value.minusMonths(1))
    fun nextMonth() = changeMonth(_month.value.plusMonths(1))
    fun backToToday() { _month.value = YearMonth.now(); _selected.value = LocalDate.now() }
    fun select(d: LocalDate) { _selected.value = d }
    fun setFilter(v: Int) { _filter.value = v }
    fun setAdvFilter(f: AdvFilter) { _adv.value = f }
    fun resetAdvFilter() { _adv.value = AdvFilter() }
    fun saveAdvPreset(f: AdvFilter) = store.updateConfig { it.copy(advPreset = gson.toJson(f.toPreset())) }
    fun applyAdvPreset() {
        val cfg = store.config.value
        if (cfg.advPreset.isBlank()) return
        runCatching { gson.fromJson(cfg.advPreset, AdvPreset::class.java) }.getOrNull()?.let { _adv.value = it.toAdvFilter() }
    }
    fun clearAdvPreset() = store.updateConfig { it.copy(advPreset = "") }
    private fun AdvFilter.toPreset() = AdvPreset(type = type?.name, ledgerId = ledgerId, accountId = accountId, categoryId = categoryId, memberId = memberId, tagId = tagId, merchant = merchant, currency = currency, reimbursement = reimbursement, paymentStatus = paymentStatus, minAmount = minAmount, maxAmount = maxAmount, startDay = startDate?.toEpochDay(), endDay = endDate?.toEpochDay(), onlyWithImage = onlyWithImage, onlyRefund = onlyRefund, onlyDiscount = onlyDiscount, onlyNotInSummary = onlyNotInSummary, onlyWithLocation = onlyWithLocation, note = note, source = source, includeInBudget = includeInBudget)
    private fun AdvPreset.toAdvFilter() = AdvFilter(type = type?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() }, ledgerId = ledgerId, accountId = accountId, categoryId = categoryId, memberId = memberId, tagId = tagId, merchant = merchant, currency = currency, reimbursement = reimbursement, paymentStatus = paymentStatus, minAmount = minAmount, maxAmount = maxAmount, startDate = startDay?.let { LocalDate.ofEpochDay(it) }, endDate = endDay?.let { LocalDate.ofEpochDay(it) }, onlyWithImage = onlyWithImage, onlyRefund = onlyRefund, onlyDiscount = onlyDiscount, onlyNotInSummary = onlyNotInSummary, onlyWithLocation = onlyWithLocation, note = note, source = source, includeInBudget = includeInBudget)
    fun reload() { reloadTick.value++ }
    fun setWeekStart(v: Int) = store.updateConfig { it.copy(calendarWeekStart = v) }
    fun setShowLunar(v: Boolean) = store.updateConfig { it.copy(calendarShowLunar = v) }
    fun setProfitColor(v: Long) = store.updateConfig { it.copy(calendarProfitColor = v) }
    fun setLossColor(v: Long) = store.updateConfig { it.copy(calendarLossColor = v) }
    private fun changeMonth(m: YearMonth) { _month.value = m; if (_selected.value.let { YearMonth.of(it.year, it.month) } != m) _selected.value = m.atDay(1) }
    private suspend fun load(m: YearMonth, adv: AdvFilter) {
        if (_selected.value.let { YearMonth.of(it.year, it.month) } != m) _selected.value = m.atDay(1)
        val monthFrom = m.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val monthTo = m.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val advFrom = adv.startDate?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
        val advTo = adv.endDate?.plusDays(1)?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
        val from = maxOf(monthFrom, advFrom ?: monthFrom)
        val to = minOf(monthTo, advTo ?: monthTo)
        val base = repo.search(adv.ledgerId ?: holder.id.value, adv.minAmount, adv.maxAmount, adv.note.ifBlank { null }, adv.categoryId?.let { listOf(it) }, adv.accountId?.let { listOf(it) }, adv.type, from, to)
        val tagTxIds = adv.tagId?.let { transactionTagDao.getByTag(it).map { l -> l.transactionId }.toSet() }
        val rows = base.filter { r ->
            (adv.merchant.isBlank() || (r.merchant ?: "").contains(adv.merchant)) &&
                (adv.memberId == null || r.memberId == adv.memberId || r.memberIdList.contains(adv.memberId)) &&
                (adv.tagId == null || tagTxIds.orEmpty().contains(r.id)) &&
                (adv.currency == null || r.currency == adv.currency) &&
                (!adv.onlyWithImage || r.images.isNotEmpty()) &&
                (!adv.onlyRefund || r.refundStatus == RefundStatus.HAS_REFUND) &&
                (!adv.onlyDiscount || r.discount > 0) &&
                (!adv.onlyNotInSummary || !r.includeInSummary) &&
                (!adv.onlyWithLocation || r.locationName != null) &&
                (adv.reimbursement == null || when (adv.reimbursement) { 0 -> r.reimbursementStatus == ReimbursementStatus.REIMBURSABLE; 1 -> r.reimbursementStatus == ReimbursementStatus.REIMBURSED; else -> r.reimbursementStatus == ReimbursementStatus.NONE }) &&
                (adv.paymentStatus == null || when (adv.paymentStatus) { 0 -> r.paymentStatus == PaymentStatus.PAID; else -> r.paymentStatus == PaymentStatus.UNPAID }) &&
                (adv.source == null || when (adv.source) { 0 -> r.source == TransactionSource.MANUAL; 1 -> r.source == TransactionSource.VOICE; else -> r.source == TransactionSource.AUTO_ACCESSIBILITY || r.source == TransactionSource.AUTO_NOTIFICATION || r.source == TransactionSource.AUTO_SILENT }) &&
                (adv.includeInBudget == null || (if (adv.includeInBudget == 0) r.includeInBudget else !r.includeInBudget))
        }
        val cells = mutableMapOf<LocalDate, DayCell>()
        val txs = mutableMapOf<LocalDate, MutableList<TransactionDisplay>>()
        rows.forEach { r ->
            val d = Instant.ofEpochMilli(r.tradeDate).atZone(zone).toLocalDate()
            txs.getOrPut(d) { mutableListOf() }.add(r)
            if (r.includeInSummary && (r.type == TransactionType.INCOME || r.type == TransactionType.EXPENSE)) {
                val c = cells[d] ?: DayCell(0, 0)
                cells[d] = if (r.type == TransactionType.INCOME) c.copy(income = c.income + r.amount) else c.copy(expense = c.expense + r.amount)
            }
        }
        _cells.value = cells
        _dayTx.value = txs
    }
}
