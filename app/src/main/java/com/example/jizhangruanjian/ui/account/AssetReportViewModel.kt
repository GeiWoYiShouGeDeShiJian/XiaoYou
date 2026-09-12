package com.example.jizhangruanjian.ui.account
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.repository.AccountManagerRepository
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.AccountGroupDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@HiltViewModel
class AssetReportViewModel @Inject constructor(private val txRepo: TransactionRepository, accountRepo: AccountManagerRepository, private val holder: CurrentLedgerHolder, val store: com.example.jizhangruanjian.data.HomeUiStore) : ViewModel() {
    data class TrendPoint(val label: String, val end: Long, val change: Long)
    val groups: StateFlow<List<AccountGroupDomain>> = accountRepo.observeGroups().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val accounts: StateFlow<List<AccountDomain>> = accountRepo.observeAccounts().map { list -> list.filter { !it.hidden && !(it.autoHideZero && it.balance == 0L) } }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val _monthly = MutableStateFlow<List<TrendPoint>>(emptyList())
    val monthly: StateFlow<List<TrendPoint>> = _monthly
    private val _yearly = MutableStateFlow<List<TrendPoint>>(emptyList())
    val yearly: StateFlow<List<TrendPoint>> = _yearly
    private val reloadTick = MutableStateFlow(0)
    private val zone = ZoneId.systemDefault()
    init {
        viewModelScope.launch {
            combine(holder.id, accounts, reloadTick) { id, _, _ -> id }.collect { load(it) }
        }
    }
    fun refresh() { reloadTick.value++ }
    fun setShowAssets(v: Boolean) = store.updateConfig { it.copy(reportShowAssets = v) }
    fun setShowDebts(v: Boolean) = store.updateConfig { it.copy(reportShowDebts = v) }
    fun setMaxSlices(n: Int) = store.updateConfig { it.copy(reportMaxSlices = n) }
    private suspend fun load(ledgerId: Long) {
        val net = accountNet(ledgerId)
        val txs = txRepo.search(ledgerId, null, null, null, null, null, null, null, null)
        fun deltaOf(t: com.example.jizhangruanjian.domain.model.TransactionDisplay): Long = when (t.type) { TransactionType.INCOME -> t.amount; TransactionType.EXPENSE -> -t.amount; else -> 0L }
        val today = LocalDate.now()
        val now = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val months = (11 downTo 0).map { YearMonth.now().minusMonths(it.toLong()) }
        _monthly.value = months.map { m ->
            val endMs = m.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli().coerceAtMost(now)
            val after = txs.filter { it.tradeDate > endMs }.sumOf { deltaOf(it) }
            val inMonth = txs.filter { it.tradeDate > m.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() && it.tradeDate <= endMs }.sumOf { deltaOf(it) }
            TrendPoint("${m.monthValue}月", net - after, inMonth)
        }
        val thisYear = today.year
        val years = ((thisYear - 4)..thisYear).toList()
        _yearly.value = years.map { y ->
            val endMs = LocalDate.of(y + 1, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli().coerceAtMost(now)
            val after = txs.filter { it.tradeDate > endMs }.sumOf { deltaOf(it) }
            val inYear = txs.filter { it.tradeDate > LocalDate.of(y, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli() && it.tradeDate <= endMs }.sumOf { deltaOf(it) }
            TrendPoint("$y", net - after, inYear)
        }
    }
    private suspend fun accountNet(ledgerId: Long): Long = accounts.value.sumOf { it.balance }
}
