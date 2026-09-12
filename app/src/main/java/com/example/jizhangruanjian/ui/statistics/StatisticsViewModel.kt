package com.example.jizhangruanjian.ui.statistics
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.LedgerDao
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.CategoryTotal
import com.example.jizhangruanjian.data.model.Granularity
import com.example.jizhangruanjian.data.model.PeriodType
import com.example.jizhangruanjian.data.model.StatisticsRange
import com.example.jizhangruanjian.data.model.StatisticsSummary
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.CategoryDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
/**
 * 统计页 ViewModel —— MVI 单向数据流
 *
 * 状态一览：
 *  - period / customRange    → 周期切换（周/月/年/自定义）
 *  - calendarMonth           → 当前选中月份
 *  - expenseType             → 支出/收入切换（影响 KPI/趋势/环形图/排行）
 *  - summary                 → 本期统计汇总（收支总额、分类聚合、日维度序列）
 *  - categories              → 全部分类列表（供 UI 关联名称/图标）
 *  - monthlyExpense          → 近 6 月逐月支出（柱状图 + 环比）
 *  - monthlyIncome           → 近 6 月逐月收入
 *  - dailyExpenses           → 日历按天支出
 *  - recentTransactions      → 当月单笔明细排行（模块⑧）
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private var currentLedgerHolder: CurrentLedgerHolder
) : ViewModel() {
    // ── 周期 & 范围 ──
    private val _period = MutableStateFlow(PeriodType.MONTH)
    val period: StateFlow<PeriodType> = _period.asStateFlow()
    private val customRange = MutableStateFlow<Pair<Long, Long>?>(null)

    // ── 支出/收入切换（默认展示支出） ──
    private val _expenseType = MutableStateFlow(TransactionType.EXPENSE)
    val expenseType: StateFlow<TransactionType> = _expenseType.asStateFlow()

    // ── 核心汇总 ──
    val summary = MutableStateFlow<StatisticsSummary?>(null)
    val categories = MutableStateFlow<List<CategoryDomain>>(emptyList())

    // ── 月份 & 日历 ──
    val calendarMonth = MutableStateFlow(YearMonth.now())
    val dailyExpenses = MutableStateFlow<Map<Int, Long>>(emptyMap())

    // ── 报表：近 6 月逐月收支 + 收入分类占比 ──
    val monthlyExpense = MutableStateFlow<List<Float>>(emptyList())
    val monthlyIncome = MutableStateFlow<List<Float>>(emptyList())
    val incomeCategoryTotals = MutableStateFlow<List<CategoryTotal>>(emptyList())

    // ── 当月单笔明细排行（模块⑧）──
    private val _recentTransactions = MutableStateFlow(emptyList<com.example.jizhangruanjian.domain.model.TransactionDisplay>())
    val recentTransactions: StateFlow<List<com.example.jizhangruanjian.domain.model.TransactionDisplay>> = _recentTransactions.asStateFlow()

    init {
        viewModelScope.launch { if (currentLedgerHolder.id.value == 0L) ensureDefault() }
        // 周期/自定义范围变化 → 重载汇总
        viewModelScope.launch { combine(_period, customRange, calendarMonth) { p, c, _ -> p to c }.collect { (p, c) -> load(p, c) } }
        // 账本切换 → 全量重载
        viewModelScope.launch { currentLedgerHolder.id.collectLatest { load(_period.value, customRange.value); loadCalendar(it, calendarMonth.value); loadReport(it, calendarMonth.value) } }
        // 分类列表
        viewModelScope.launch { categoryRepository.observeAll().collect { categories.value = it } }
        // 月份变化 → 日历 + 报表 + 明细
        viewModelScope.launch { combine(currentLedgerHolder.id, calendarMonth) { l, m -> l to m }.collect { (l, m) -> loadCalendar(l, m); loadReport(l, m) } }
        // 任意记账变动 → 实时刷新全部
        viewModelScope.launch { transactionRepository.transactionsChanged.collectLatest {
            load(_period.value, customRange.value)
            loadCalendar(currentLedgerHolder.id.value, calendarMonth.value)
            loadReport(currentLedgerHolder.id.value, calendarMonth.value)
        }}
    }

    // ═══════════════════ 公开操作 ═══════════════════

    /** 切换周期（周/月/年） */
    fun setPeriod(p: PeriodType) { if (p != PeriodType.CUSTOM) _period.value = p }

    /** 设置自定义范围 */
    fun setCustom(from: Long, to: Long) { customRange.value = from to to; _period.value = PeriodType.CUSTOM }

    /** 切换支出 / 收入 */
    fun toggleType(type: TransactionType) { _expenseType.value = type }

    /** 上/下月 */
    fun prevMonth() { calendarMonth.value = calendarMonth.value.minusMonths(1) }
    fun nextMonth() { calendarMonth.value = calendarMonth.value.plusMonths(1) }

    /**
     * 日均支出/收入（当月天数按 calendarMonth 计算）
     * 返回值单位：分 → 外层用 Formatters.yuanText() 渲染
     */
    fun dailyAverage(total: Long): Float {
        val days = calendarMonth.value.lengthOfMonth().toFloat()
        return if (days > 0f) total / days else 0f
    }

    // ═══════════════════ 私有加载逻辑 ═══════════════════

    private suspend fun ensureDefault() {
        val list = ledgerDao.observeAll().first()
        (list.firstOrNull { it.isDefault } ?: list.firstOrNull())?.let { currentLedgerHolder.set(it.id) }
    }

    private suspend fun load(p: PeriodType, c: Pair<Long, Long>?) {
        val r = buildRange(p, c) ?: return
        summary.value = transactionRepository.statistics(currentLedgerHolder.id.value, r)
    }

    private fun buildRange(p: PeriodType, c: Pair<Long, Long>?): StatisticsRange? {
        val zone = ZoneId.systemDefault()
        val today = Instant.now().atZone(zone).toLocalDate()
        val (from, to) = when (p) {
            PeriodType.WEEK -> today.with(DayOfWeek.MONDAY) to today.with(DayOfWeek.MONDAY).plusWeeks(1)
            PeriodType.MONTH -> { val m = calendarMonth.value; m.atDay(1) to m.plusMonths(1).atDay(1) }
            PeriodType.YEAR -> today.withDayOfYear(1) to today.withDayOfYear(1).plusYears(1)
            PeriodType.CUSTOM -> c?.let { (f, t) -> Instant.ofEpochMilli(f).atZone(zone).toLocalDate() to Instant.ofEpochMilli(t).atZone(zone).toLocalDate().plusDays(1) } ?: return null
        }
        if (!from.isBefore(to)) return null
        val lenDays = ChronoUnit.DAYS.between(from, to)
        val granularity = when {
            lenDays >= 335 -> Granularity.MONTH
            lenDays >= 60 -> Granularity.WEEK
            else -> Granularity.DAY
        }
        val prevTo = from
        val prevFrom = when (p) {
            PeriodType.WEEK -> from.minusWeeks(1)
            PeriodType.MONTH -> from.minusMonths(1)
            PeriodType.YEAR -> from.minusYears(1)
            PeriodType.CUSTOM -> from.minusDays(lenDays)
        }
        val startOf = { d: java.time.LocalDate -> d.atStartOfDay(zone).toInstant().toEpochMilli() }
        return StatisticsRange(startOf(from), startOf(to), startOf(prevFrom), startOf(prevTo), granularity)
    }

    /** 按天聚合支出 → 日历热力 */
    private suspend fun loadCalendar(ledgerId: Long, m: YearMonth) {
        val zone = ZoneId.systemDefault()
        val from = m.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val to = m.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        dailyExpenses.value = transactionRepository.dailyExpenses(ledgerId, from, to)
    }

    /**
     * 报表数据：
     *  - 近 6 月逐月支出/收入（柱状图）
     *  - 当月分类占比（环形图 + 排行）
     *  - 当月单笔明细 Top N（列表）
     */
    private suspend fun loadReport(ledgerId: Long, m: YearMonth) {
        val zone = ZoneId.systemDefault()
        fun ms(ym: YearMonth): Long = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val months = (5 downTo 0).map { m.minusMonths(it.toLong()) }
        // 近 6 月逐月
        monthlyExpense.value = months.map { ym ->
            transactionRepository.sumType(ledgerId, TransactionType.EXPENSE, ms(ym), ms(ym.plusMonths(1))).toFloat()
        }
        monthlyIncome.value = months.map { ym ->
            transactionRepository.sumType(ledgerId, TransactionType.INCOME, ms(ym), ms(ym.plusMonths(1))).toFloat()
        }
        // 当月分类聚合
        incomeCategoryTotals.value = transactionRepository.incomeByCategory(ledgerId, ms(m), ms(m.plusMonths(1)))
        // 当月单笔明细（按金额降序，取当前类型的最近 20 笔）
        val currentType = _expenseType.value
        val txs = transactionRepository.search(
            ledgerId = ledgerId,
            minAmount = null, maxAmount = null, note = null,
            categoryIds = null, accountIds = null,
            type = currentType,
            fromDate = ms(m), toDate = ms(m.plusMonths(1))
        ).sortedByDescending { it.amount }.take(20)
        _recentTransactions.value = txs
    }

    fun utcToLocalDay(ms: Long): Long {
        val ld = Instant.ofEpochMilli(ms).atZone(ZoneId.of("UTC")).toLocalDate()
        return ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
