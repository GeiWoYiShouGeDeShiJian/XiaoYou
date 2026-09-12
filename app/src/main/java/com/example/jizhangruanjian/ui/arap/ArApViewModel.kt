package com.example.jizhangruanjian.ui.arap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.LoanDirection
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ArApViewModel @Inject constructor(
    private val currentLedgerHolder: CurrentLedgerHolder,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    val receivables = MutableStateFlow<List<TransactionDisplay>>(emptyList())
    val payables = MutableStateFlow<List<TransactionDisplay>>(emptyList())
    init {
        viewModelScope.launch {
            currentLedgerHolder.id.collectLatest { id ->
                transactionRepository.observeByLedger(id).collect { list ->
                    val loans = list.filter { it.type == TransactionType.LOAN }
                    receivables.value = loans.filter { it.loanDirection == LoanDirection.OUT }
                    payables.value = loans.filter { it.loanDirection == LoanDirection.IN }
                }
            }
        }
    }
    fun markSettled(id: Long) = viewModelScope.launch { transactionRepository.settleLoan(id) }
    fun receivableTotal(): Long = receivables.value.sumOf { it.amount }
    fun payableTotal(): Long = payables.value.sumOf { it.amount }
}