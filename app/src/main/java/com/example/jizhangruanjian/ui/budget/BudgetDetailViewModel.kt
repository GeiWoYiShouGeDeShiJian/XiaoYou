package com.example.jizhangruanjian.ui.budget
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.repository.BudgetRepository
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.BudgetState
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
@HiltViewModel
class BudgetDetailViewModel @Inject constructor(private val repo: TransactionRepository, private val budgetRepo: BudgetRepository, private val holder: CurrentLedgerHolder) : ViewModel() {
    data class DayGroup(val date: LocalDate, val total: Long, val items: List<TransactionDisplay>)
    private val _budget = MutableStateFlow<BudgetState?>(null)
    val budget: StateFlow<BudgetState?> = _budget
    private val _groups = MutableStateFlow<List<DayGroup>>(emptyList())
    val groups: StateFlow<List<DayGroup>> = _groups
    private val reloadTick = MutableStateFlow(0)
    private val _categoryId = MutableStateFlow<Long?>(null)
    private val zone = ZoneId.systemDefault()
    private val month = YearMonth.now()
    init {
        viewModelScope.launch {
            combine(holder.id, reloadTick, _categoryId) { id, _, catId -> id to catId }
                .collectLatest { (id, catId) -> load(id, catId) }
        }
    }
    fun reload() { reloadTick.value++ }
    fun setCategory(categoryId: Long?) { _categoryId.value = categoryId }
    private suspend fun load(ledgerId: Long, categoryId: Long?) {
        _budget.value = budgetRepo.stateFor(ledgerId, categoryId)
        val from = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val to = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val rows = repo.search(ledgerId, null, null, null, null, null, TransactionType.EXPENSE, from, to).sortedByDescending { it.tradeDate }.filter { categoryId == null || it.categoryId == categoryId }
        _groups.value = rows.groupBy { Instant.ofEpochMilli(it.tradeDate).atZone(zone).toLocalDate() }.map { (d, list) -> DayGroup(d, list.sumOf { it.amount }, list) }.sortedByDescending { it.date }
    }
}
