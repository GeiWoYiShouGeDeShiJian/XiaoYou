package com.example.jizhangruanjian.ui.account
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.jizhangruanjian.core.database.AppDatabase
import com.example.jizhangruanjian.core.database.CategoryDao
import com.example.jizhangruanjian.core.database.LedgerAccountDao
import com.example.jizhangruanjian.core.database.LedgerDao
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.Category
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.model.Ledger
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.LedgerDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val db: AppDatabase,
    private val ledgerDao: LedgerDao,
    private val ledgerAccountDao: LedgerAccountDao,
    private val txRepo: TransactionRepository,
    private val categoryDao: CategoryDao,
    private val currentLedgerHolder: CurrentLedgerHolder
) : ViewModel() {
    val ledgers = MutableStateFlow<List<LedgerDomain>>(emptyList())
    val counts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val currentId: StateFlow<Long> = currentLedgerHolder.id
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    init {
        viewModelScope.launch {
            ledgerDao.observeAll().collect { list ->
                ledgers.value = list.map { l -> LedgerDomain(l.id, l.name, l.color, l.icon, l.isDefault, l.sortOrder, l.cover, l.coverDark, l.currency, l.hidden) }
                counts.value = list.associate { it.id to txRepo.getByLedger(it.id).size }
            }
        }
        viewModelScope.launch {
            if (currentLedgerHolder.id.value == 0L) ensureDefault()
        }
    }
    private suspend fun ensureDefault() {
        val list = ledgerDao.observeAll().first()
        (list.firstOrNull { it.isDefault } ?: list.firstOrNull())?.let { currentLedgerHolder.set(it.id) }
    }
    fun ledgerById(id: Long): LedgerDomain? = ledgers.value.firstOrNull { it.id == id }
    fun create(name: String, cover: String, coverDark: Boolean, currency: String, hidden: Boolean, categories: List<String> = emptyList(), onCreated: (Long) -> Unit = {}) = viewModelScope.launch {
        val sort = (ledgerDao.observeAll().first().maxOfOrNull { it.sortOrder } ?: 0) + 1
        val id = ledgerDao.insert(Ledger(name = name, color = 0xFF4E79A7.toInt(), icon = "📖", isDefault = false, sortOrder = sort, createdAt = System.currentTimeMillis(), cover = cover, coverDark = coverDark, currency = currency, hidden = hidden))
        categories.forEachIndexed { i, n -> categoryDao.insert(Category(name = n, type = CategoryType.EXPENSE, icon = "📦", sortOrder = 2000 + i)) }
        currentLedgerHolder.set(id)
        onCreated(id)
    }
    fun update(id: Long, name: String, cover: String, coverDark: Boolean, currency: String, hidden: Boolean) = viewModelScope.launch { ledgerDao.getById(id)?.let { ledgerDao.update(it.copy(name = name, cover = cover, coverDark = coverDark, currency = currency, hidden = hidden)) } }
    fun rename(id: Long, name: String) = viewModelScope.launch { ledgerDao.getById(id)?.let { ledgerDao.update(it.copy(name = name)) } }
    fun updateAppearance(id: Long, name: String, color: Int, icon: String) = viewModelScope.launch { ledgerDao.getById(id)?.let { ledgerDao.update(it.copy(name = name, color = color, icon = icon)) } }
    fun switch(id: Long) { currentLedgerHolder.set(id) }
    fun setDefault(id: Long) = viewModelScope.launch {
        db.withTransaction { ledgerDao.clearDefault(); ledgerDao.getById(id)?.let { ledgerDao.update(it.copy(isDefault = true)) } }
        currentLedgerHolder.set(id)
    }
    fun delete(id: Long) = viewModelScope.launch {
        val l = ledgerDao.getById(id) ?: return@launch
        if (ledgers.value.size <= 1) { _message.value = "至少保留一个账本"; return@launch }
        db.withTransaction {
            ledgerDao.softDelete(id, System.currentTimeMillis())
            if (l.isDefault) {
                ledgerDao.clearDefault()
                ledgerDao.observeAll().first().firstOrNull()?.let { ns -> ledgerDao.update(ns.copy(isDefault = true)); currentLedgerHolder.set(ns.id) }
            } else if (currentLedgerHolder.id.value == id) {
                ledgerDao.observeAll().first().firstOrNull()?.let { currentLedgerHolder.set(it.id) }
            }
        }
    }
    fun clearMessage() { _message.value = null }
}
