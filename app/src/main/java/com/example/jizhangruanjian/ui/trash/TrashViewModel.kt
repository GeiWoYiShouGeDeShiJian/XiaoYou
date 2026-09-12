package com.example.jizhangruanjian.ui.trash
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.jizhangruanjian.core.database.AccountDao
import com.example.jizhangruanjian.core.database.AppDatabase
import com.example.jizhangruanjian.core.database.LedgerAccountDao
import com.example.jizhangruanjian.core.database.LedgerDao
import com.example.jizhangruanjian.data.model.Account
import com.example.jizhangruanjian.data.model.Ledger
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@HiltViewModel
class TrashViewModel @Inject constructor(
    private val db: AppDatabase,
    private val transactionRepository: TransactionRepository,
    private val ledgerDao: LedgerDao,
    private val accountDao: AccountDao,
    private val ledgerAccountDao: LedgerAccountDao
) : ViewModel() {
    val trashTx: StateFlow<List<TransactionDisplay>> = transactionRepository.observeTrash()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val trashLedgers: StateFlow<List<Ledger>> = ledgerDao.observeTrash()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val trashAccounts: StateFlow<List<Account>> = accountDao.observeTrash()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun restoreTx(ids: List<Long>) = viewModelScope.launch { ids.forEach { transactionRepository.restore(it) } }
    fun deleteTxForever(ids: List<Long>) = viewModelScope.launch {
        ids.forEach { id -> transactionRepository.getById(id)?.let { transactionRepository.deletePermanently(it) } }
    }
    fun restoreLedgers(ids: List<Long>) = viewModelScope.launch { ids.forEach { ledgerDao.restore(it) } }
    fun deleteLedgersForever(ids: List<Long>) = viewModelScope.launch {
        ids.forEach { id ->
            db.withTransaction {
                ledgerAccountDao.deleteByLedger(id)
                ledgerDao.getById(id)?.let { ledgerDao.delete(it) }
            }
        }
    }
    fun restoreAccounts(ids: List<Long>) = viewModelScope.launch { ids.forEach { accountDao.restore(it) } }
    fun deleteAccountsForever(ids: List<Long>) = viewModelScope.launch {
        ids.forEach { id ->
            db.withTransaction {
                ledgerAccountDao.deleteByAccount(id)
                accountDao.getById(id)?.let { accountDao.delete(it) }
            }
        }
    }
}
