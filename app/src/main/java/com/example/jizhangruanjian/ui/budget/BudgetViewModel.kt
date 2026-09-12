package com.example.jizhangruanjian.ui.budget
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.BudgetDao
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.Budget
import com.example.jizhangruanjian.data.model.BudgetPeriod
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.repository.BudgetRepository
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.domain.model.BudgetState
import com.example.jizhangruanjian.domain.model.CategoryDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetDao: BudgetDao,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val currentLedgerHolder: CurrentLedgerHolder
) : ViewModel() {
    private var ledgerId = 0L
    val states = MutableStateFlow<List<BudgetState>>(emptyList())
    val total = MutableStateFlow<BudgetState?>(null)
    // 可选作分类预算的支出分类（含一级与二级）
    val expenseCategories = MutableStateFlow<List<CategoryDomain>>(emptyList())
    init {
        viewModelScope.launch {
            ledgerId = currentLedgerHolder.id.value
            budgetRepository.observeStates(ledgerId).collect { states.value = it; total.value = it.firstOrNull { x -> x.isTotal } }
        }
        viewModelScope.launch {
            categoryRepository.observeAll().map { it.filter { c -> c.type == CategoryType.EXPENSE } }.collect { expenseCategories.value = it }
        }
    }
    fun add(categoryId: Long?, amount: Long, rollover: Boolean) {
        if (amount <= 0L) return
        viewModelScope.launch {
            budgetDao.insert(Budget(ledgerId = ledgerId, categoryId = categoryId, period = BudgetPeriod.MONTHLY, amount = amount, startDate = System.currentTimeMillis(), rolloverMode = if (rollover) 1 else 0))
        }
    }
    fun update(id: Long, amount: Long) {
        if (amount <= 0L) return
        viewModelScope.launch { budgetDao.getById(id)?.let { budgetDao.update(it.copy(amount = amount)) } }
    }
    fun delete(id: Long) = viewModelScope.launch { budgetDao.getById(id)?.let { budgetDao.delete(it) } }
}