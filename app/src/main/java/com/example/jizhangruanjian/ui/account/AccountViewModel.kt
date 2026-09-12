package com.example.jizhangruanjian.ui.account
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.data.model.AccountType
import com.example.jizhangruanjian.data.repository.AccountManagerRepository
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.AccountGroupDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repo: AccountManagerRepository
) : ViewModel() {
    val groups = MutableStateFlow<List<AccountGroupDomain>>(emptyList())
    val accounts = MutableStateFlow<List<AccountDomain>>(emptyList())
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    init {
        viewModelScope.launch { repo.observeGroups().collect { groups.value = it } }
        viewModelScope.launch { repo.observeAccounts().collect { accounts.value = it } }
    }
    fun addGroup(name: String) = viewModelScope.launch { repo.createGroup(name) }
    fun renameGroup(id: Long, name: String) = viewModelScope.launch { repo.renameGroup(id, name) }
    fun deleteGroup(id: Long) = viewModelScope.launch { repo.deleteGroup(id)?.let { _message.value = it } }
    fun moveGroup(id: Long, down: Boolean) = viewModelScope.launch { repo.moveGroup(id, down) }
    fun addAccount(groupId: Long, name: String, type: AccountType, initialBalance: Long, isShared: Boolean) =
        viewModelScope.launch { repo.createAccount(groupId, name, type, initialBalance, isShared) }
    fun renameAccount(id: Long, name: String) = viewModelScope.launch { repo.renameAccount(id, name) }
    fun deleteAccount(id: Long) = viewModelScope.launch { repo.deleteAccount(id) }
    fun toggleShared(id: Long, isShared: Boolean) = viewModelScope.launch { repo.toggleShared(id, isShared) }
    fun clearMessage() { _message.value = null }
}