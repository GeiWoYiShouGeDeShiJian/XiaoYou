package com.example.jizhangruanjian.ui.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.LedgerDao
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.HomeUiStore
import com.example.jizhangruanjian.data.model.HomeConfig
import com.example.jizhangruanjian.data.model.HomeSummary
import com.example.jizhangruanjian.data.repository.BudgetRepository
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.BudgetState
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private var currentLedgerHolder: CurrentLedgerHolder,
    private val homeUiStore: HomeUiStore
) : ViewModel() {
    val hiddenSections: StateFlow<Set<HomeUiStore.HomeSection>> = homeUiStore.hidden
    val config: StateFlow<HomeConfig> = homeUiStore.config
    private val currentLedgerId = MutableStateFlow(0L)
    val summary = MutableStateFlow<HomeSummary?>(null)
    val recent = MutableStateFlow<List<TransactionDisplay>>(emptyList())
    val totalBudget = MutableStateFlow<BudgetState?>(null)
    val todayExpense = MutableStateFlow(0L)
    val weekExpense = MutableStateFlow(0L)
    val todayIncome = MutableStateFlow(0L)
    val weekIncome = MutableStateFlow(0L)
    val yearIncome = MutableStateFlow(0L)
    val yearExpense = MutableStateFlow(0L)
    val categories = MutableStateFlow<List<CategoryDomain>>(emptyList())
    init {
        viewModelScope.launch { if (currentLedgerHolder.id.value == 0L) ensureDefault() }
        viewModelScope.launch { categoryRepository.observeAll().collect { categories.value = it } }
        viewModelScope.launch {
            currentLedgerHolder.id.collectLatest { ledgerId ->
                currentLedgerId.value = ledgerId
                summary.value = transactionRepository.homeSummary(ledgerId)
                todayExpense.value = transactionRepository.todayExpense(ledgerId)
                weekExpense.value = transactionRepository.weekExpense(ledgerId)
                todayIncome.value = transactionRepository.todayIncome(ledgerId)
                weekIncome.value = transactionRepository.weekIncome(ledgerId)
                yearIncome.value = transactionRepository.yearIncome(ledgerId)
                yearExpense.value = transactionRepository.yearExpense(ledgerId)
                totalBudget.value = budgetRepository.totalState(ledgerId)
            }
        }
        viewModelScope.launch {
            combine(currentLedgerHolder.id, homeUiStore.config) { ledgerId, c -> ledgerId to c.recentTransactionCount }
                .collectLatest { (ledgerId, count) -> transactionRepository.observeRecent(ledgerId, count).collect { recent.value = it } }
        }
    }
    private suspend fun ensureDefault() {
        val list = ledgerDao.observeAll().first()
        (list.firstOrNull { it.isDefault } ?: list.firstOrNull())?.let { currentLedgerHolder.set(it.id) }
    }
    fun refresh() {
        viewModelScope.launch { summary.value = transactionRepository.homeSummary(currentLedgerId.value) }
    }
}
