package com.example.jizhangruanjian.ui.detail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.TransactionDao
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.TransactionSource
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
private val AUTO_SOURCES = setOf(TransactionSource.AUTO_ACCESSIBILITY, TransactionSource.AUTO_NOTIFICATION, TransactionSource.AUTO_SILENT)
private data class FilterQuad(val ledgerId: Long, val month: YearMonth, val sort: Int, val source: Int)
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MonthDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val transactionDao: TransactionDao,
    currentLedgerHolder: CurrentLedgerHolder
) : ViewModel() {
    val month = MutableStateFlow(YearMonth.now())
    val sortMode = MutableStateFlow(0)
    // 0 全部, 1 手动, 2 语音, 3 自动
    val sourceFilter = MutableStateFlow(0)
    val transactions: StateFlow<List<TransactionDisplay>> = combine(currentLedgerHolder.id, month, sortMode, sourceFilter) { lid, m, s, sf -> FilterQuad(lid, m, s, sf) }
        .flatMapLatest { q ->
            transactionRepository.observeByLedger(q.ledgerId).map { list ->
                val zone = ZoneId.systemDefault()
                val start = q.month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = q.month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val f = list.filter { it.tradeDate >= start && it.tradeDate < end }
                val filtered = when (q.source) {
                    1 -> f.filter { it.source == TransactionSource.MANUAL }
                    2 -> f.filter { it.source == TransactionSource.VOICE }
                    3 -> f.filter { it.source in AUTO_SOURCES }
                    else -> f
                }
                when (q.sort) { 1 -> filtered.sortedBy { it.tradeDate }; 2 -> filtered.sortedByDescending { it.amount }; else -> filtered.sortedByDescending { it.tradeDate } }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun prevMonth() { month.value = month.value.minusMonths(1) }
    fun nextMonth() { month.value = month.value.plusMonths(1) }
    fun setMonth(year: Int, monthValue: Int) { month.value = YearMonth.of(year, monthValue) }
    fun backToNow() { month.value = YearMonth.now() }
    fun setSourceFilter(v: Int) { sourceFilter.value = v }
    fun deleteMany(ids: List<Long>) = viewModelScope.launch { ids.forEach { transactionRepository.softDelete(it) } }
    fun split(id: Long, parts: Int) = viewModelScope.launch {
        if (parts < 2) return@launch
        val raw = transactionDao.getById(id) ?: return@launch
        val per = raw.amount / parts
        val first = raw.copy(amount = raw.amount - per * (parts - 1))
        transactionRepository.save(first, raw, true)
        for (i in 1 until parts) transactionRepository.save(raw.copy(id = 0L, amount = per, tradeDate = raw.tradeDate + i), null, true)
    }
}
