package com.example.jizhangruanjian.ui.recurring
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.RecurringTemplateDao
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.Frequency
import com.example.jizhangruanjian.data.model.RecurringTemplate
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.repository.AccountRepository
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.domain.model.RecurringTemplateDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val templateDao: RecurringTemplateDao,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val currentLedgerHolder: CurrentLedgerHolder
) : ViewModel() {
    private var ledgerId = 0L
    val templates = MutableStateFlow<List<RecurringTemplateDomain>>(emptyList())
    val accounts: StateFlow<List<AccountDomain>> = accountRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = MutableStateFlow<List<CategoryDomain>>(emptyList())
    init {
        viewModelScope.launch {
            ledgerId = currentLedgerHolder.id.value
            templateDao.observeAll().map { it.map { t -> t.toDomain() } }.collect { templates.value = it }
        }
        viewModelScope.launch { categoryRepository.observeAll().collect { categories.value = it } }
    }
    fun add(type: TransactionType, accountId: Long, categoryId: Long, amount: Long, frequency: Frequency, interval: Int, note: String, endInDays: Int?) {
        if (amount <= 0L || accountId == 0L || categoryId == 0L) return
        val today = todayStart()
        val endDate = endInDays?.let { todayStart().plusDays(it.toLong()) }
        viewModelScope.launch {
            templateDao.insert(RecurringTemplate(ledgerId = ledgerId, accountId = accountId, categoryId = categoryId, type = type, amount = amount, note = note, frequency = frequency, interval = interval.coerceAtLeast(1), nextExecuteDate = today, endDate = endDate))
        }
    }
    fun delete(id: Long) = viewModelScope.launch { templateDao.getById(id)?.let { templateDao.delete(it) } }
    private fun todayStart(): Long = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun Long.plusDays(n: Long): Long = java.time.Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate().plusDays(n).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun RecurringTemplate.toDomain() = RecurringTemplateDomain(id, ledgerId, accountId, categoryId, type, amount, note, frequency, interval, nextExecuteDate, endDate)
}