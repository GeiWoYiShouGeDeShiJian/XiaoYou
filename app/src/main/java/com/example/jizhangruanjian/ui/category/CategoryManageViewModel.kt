package com.example.jizhangruanjian.ui.category
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.domain.model.CategoryDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryManageViewModel @Inject constructor(private val repo: CategoryRepository, private val appSettingDao: com.example.jizhangruanjian.core.database.AppSettingDao) : ViewModel() {
    private val _currentType = MutableStateFlow(CategoryType.EXPENSE)
    val currentType: StateFlow<CategoryType> = _currentType.asStateFlow()
    private val _selectedMainId = MutableStateFlow<Long?>(null)
    val selectedMainId: StateFlow<Long?> = _selectedMainId.asStateFlow()
    val mainCategories: StateFlow<List<CategoryDomain>> = _currentType.flatMapLatest { type ->
        repo.observeByType(type).map { list -> list.filter { it.parentId == null } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favorites: StateFlow<List<CategoryDomain>> = repo.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val incomeParent: StateFlow<Boolean> = appSettingDao.observeAll().map { l -> l.firstOrNull { it.key == "flag_income_parent" }?.value == "1" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val subCategories: StateFlow<List<CategoryDomain>> = combine(_currentType, _selectedMainId) { type, pid -> type to pid }
        .flatMapLatest { (type, pid) ->
            if (pid == null) flowOf(emptyList()) else repo.observeByTypeAndParent(type, pid)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun selectMain(id: Long) { _selectedMainId.value = id }
    fun switchType(type: CategoryType) { _currentType.value = type; _selectedMainId.value = null }
    fun toggleFavorite(id: Long) = viewModelScope.launch { repo.toggleFavorite(id) }
    fun moveCategory(from: Int, to: Int, current: List<CategoryDomain>) {
        val reordered = current.toMutableList()
        if (from in reordered.indices && to in reordered.indices && from != to) {
            val item = reordered.removeAt(from)
            reordered.add(to, item)
            viewModelScope.launch { repo.reorderCategories(reordered.map { it.id }) }
        }
    }
    fun resetCategories() = viewModelScope.launch { repo.resetToSeed(_currentType.value); _selectedMainId.value = null }
    fun addMainCategory(name: String) = viewModelScope.launch {
        if (name.isNotBlank()) repo.addCategory(name.trim(), _currentType.value, null)
    }
    fun toggleIncomeParent() = viewModelScope.launch {
        appSettingDao.upsert(com.example.jizhangruanjian.data.model.AppSetting("flag_income_parent", if (incomeParent.value) "0" else "1"))
    }
}