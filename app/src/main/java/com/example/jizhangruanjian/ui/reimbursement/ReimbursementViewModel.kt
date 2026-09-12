package com.example.jizhangruanjian.ui.reimbursement
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.model.ReimbursementStatus
import com.example.jizhangruanjian.data.model.Transaction
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.core.database.CategoryDao
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ReimbursementViewModel @Inject constructor(
    private val currentLedgerHolder: CurrentLedgerHolder,
    private val transactionRepository: TransactionRepository,
    private val categoryDao: CategoryDao
) : ViewModel() {
    private val statusFilter = MutableStateFlow(ReimbursementStatus.REIMBURSABLE)
    private val all = MutableStateFlow<List<TransactionDisplay>>(emptyList())
    val filtered = combine(all, statusFilter) { list, s ->
        list.filter { it.reimbursementStatus == s }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    init {
        viewModelScope.launch {
            currentLedgerHolder.id.collectLatest { id ->
                transactionRepository.observeByLedger(id).collect { list ->
                    all.value = list.filter { it.type == TransactionType.EXPENSE }
                }
            }
        }
    }
    fun filterByStatus(s: ReimbursementStatus) { statusFilter.value = s }
    // 标记已报销：自动生成关联的「报销款」收入并双向记录关联 id
    fun markReimbursed(id: Long) = viewModelScope.launch {
        val tx = transactionRepository.getById(id) ?: return@launch
        if (tx.reimbursementStatus == ReimbursementStatus.REIMBURSED) return@launch
        val cats = categoryDao.getAll()
        val incomeCat = cats.filter { it.type == CategoryType.INCOME }.let { l ->
            l.firstOrNull { it.parentId != null && it.name == "其他收入" } ?: l.firstOrNull { it.parentId != null } ?: l.firstOrNull()
        } ?: return@launch
        val now = System.currentTimeMillis()
        val catName = cats.firstOrNull { it.id == tx.categoryId }?.name ?: "支出"
        val date = SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(Date(tx.tradeDate))
        val yuan = Formatters.yuanText(tx.amount)
        val income = Transaction(
            ledgerId = tx.ledgerId, accountId = tx.accountId, categoryId = incomeCat.id,
            type = TransactionType.INCOME, amount = tx.amount,
            note = "${date}的${catName}，可报销${yuan}，实际报销${yuan}",
            tradeDate = now, createdAt = now, updatedAt = now
        )
        val incomeId = transactionRepository.save(income, null, true)
        transactionRepository.setReimburseLink(tx.id, ReimbursementStatus.REIMBURSED, incomeId)
    }
    // 取消报销：软删关联的报销款收入，状态退回待报销
    fun cancelReimbursed(id: Long) = viewModelScope.launch {
        val tx = transactionRepository.getById(id) ?: return@launch
        if (tx.reimburseLinkId > 0) transactionRepository.softDelete(tx.reimburseLinkId)
        transactionRepository.setReimburseLink(tx.id, ReimbursementStatus.REIMBURSABLE, 0)
    }
    fun reimbursableTotal(): Long = all.value.filter { it.reimbursementStatus == ReimbursementStatus.REIMBURSABLE }.sumOf { it.amount }
    fun reimbursedTotal(): Long = all.value.filter { it.reimbursementStatus == ReimbursementStatus.REIMBURSED }.sumOf { it.amount }
}
