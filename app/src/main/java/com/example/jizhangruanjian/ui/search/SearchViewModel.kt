package com.example.jizhangruanjian.ui.search
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.TagDao
import com.example.jizhangruanjian.core.database.TransactionTagDao
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.Tag
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.repository.AccountRepository
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val tagDao: TagDao,
    private val transactionTagDao: TransactionTagDao,
    private val currentLedgerHolder: CurrentLedgerHolder
) : ViewModel() {
    enum class TagMode { OR, AND }
    private val pageSize = 20
    private var ledgerId = 0L
    private var full = emptyList<TransactionDisplay>()
    private var visibleCount = 0
    var minExpr = MutableStateFlow("")
    var maxExpr = MutableStateFlow("")
    var note = MutableStateFlow("")
    var from: MutableStateFlow<Long?> = MutableStateFlow(null)
    var to: MutableStateFlow<Long?> = MutableStateFlow(null)
    var type: MutableStateFlow<TransactionType?> = MutableStateFlow(null)
    val selectedCategories = MutableStateFlow<Set<Long>>(emptySet())
    val selectedAccounts = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTags = MutableStateFlow<Set<Long>>(emptySet())
    var tagMode: MutableStateFlow<TagMode> = MutableStateFlow(TagMode.OR)
    val results = MutableStateFlow<List<TransactionDisplay>>(emptyList())
    val loading = MutableStateFlow(false)
    val hasMore = MutableStateFlow(false)
    val accounts: StateFlow<List<AccountDomain>> = accountRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories: StateFlow<List<CategoryDomain>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tags: StateFlow<List<Tag>> = tagDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    init { viewModelScope.launch { ledgerId = currentLedgerHolder.id.value } }
    fun toggleCategory(id: Long) { selectedCategories.value = toggle(selectedCategories.value, id) }
    fun toggleAccount(id: Long) { selectedAccounts.value = toggle(selectedAccounts.value, id) }
    fun toggleTag(id: Long) { selectedTags.value = toggle(selectedTags.value, id) }
    fun setType(t: TransactionType?) { type.value = t }
    fun setTagMode(m: TagMode) { tagMode.value = m }
    private fun toggle(set: Set<Long>, id: Long) = if (id in set) set - id else set + id
    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            val min = (minExpr.value.toDoubleOrNull()?.let { (it * 100).toLong() })
            val max = (maxExpr.value.toDoubleOrNull()?.let { (it * 100).toLong() })
            val cats = selectedCategories.value.takeIf { it.isNotEmpty() }
            val accs = selectedAccounts.value.takeIf { it.isNotEmpty() }
            val base = transactionRepository.search(ledgerId, min, max, note.value, cats?.toList(), accs?.toList(), type.value, from.value, to.value)
            full = applyTags(base)
            visibleCount = pageSize
            results.value = full.take(visibleCount)
            hasMore.value = full.size > visibleCount
            loading.value = false
        }
    }
    fun loadMore() {
        viewModelScope.launch {
            if (hasMore.value) { visibleCount += pageSize; results.value = full.take(visibleCount); hasMore.value = full.size > visibleCount }
        }
    }
    fun softDelete(id: Long) = viewModelScope.launch { transactionRepository.softDelete(id); refresh() }
    private suspend fun applyTags(list: List<TransactionDisplay>): List<TransactionDisplay> {
        if (selectedTags.value.isEmpty()) return list
        val sets = selectedTags.value.map { tagId -> transactionTagDao.getByTag(tagId).map { it.transactionId }.toSet() }
        return if (tagMode.value == TagMode.OR) {
            val union = sets.reduce { a, b -> a + b }
            list.filter { it.id in union }
        } else {
            list.filter { tx -> sets.all { s -> tx.id in s } }
        }
    }
}