package com.example.jizhangruanjian.ui.account
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.LedgerDao
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.HomeUiStore
import com.example.jizhangruanjian.data.repository.AccountManagerRepository
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.AccountGroupDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@HiltViewModel
class AccountTabViewModel @Inject constructor(private val repo: AccountManagerRepository, val store: HomeUiStore, ledgerDao: LedgerDao, currentLedgerHolder: CurrentLedgerHolder) : ViewModel() {
    val groups: StateFlow<List<AccountGroupDomain>> = repo.observeGroups().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val accounts: StateFlow<List<AccountDomain>> = repo.observeAccounts().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val ledgerName: StateFlow<String> = combine(ledgerDao.observeAll(), currentLedgerHolder.id) { list, id ->
        list.firstOrNull { it.id == id }?.name ?: "账本"
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "账本")
    fun toggleHidden(account: AccountDomain) {
        viewModelScope.launch { repo.setAccountHidden(account.id, !account.hidden) }
    }
}
