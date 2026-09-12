package com.example.jizhangruanjian.ui.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.HomeUiStore
import com.example.jizhangruanjian.data.model.HomeConfig
import com.example.jizhangruanjian.data.model.HomeSummary
import com.example.jizhangruanjian.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
enum class HomeDialogType { DATA_ONE, DATA_TWO, DATA_THREE, COVER, PULL_DOWN }
@HiltViewModel
class CustomizeHomeViewModel @Inject constructor(
    private val homeUiStore: HomeUiStore,
    private val transactionRepository: TransactionRepository,
    private val currentLedgerHolder: CurrentLedgerHolder
) : ViewModel() {
    val config: StateFlow<HomeConfig> = homeUiStore.config
    private val _currentDialog = MutableStateFlow<HomeDialogType?>(null)
    val currentDialog: StateFlow<HomeDialogType?> = _currentDialog
    val summary = MutableStateFlow<HomeSummary?>(null)
    val todayExpense = MutableStateFlow(0L)
    val weekExpense = MutableStateFlow(0L)
    val todayIncome = MutableStateFlow(0L)
    val weekIncome = MutableStateFlow(0L)
    val yearIncome = MutableStateFlow(0L)
    val yearExpense = MutableStateFlow(0L)
    init {
        viewModelScope.launch {
            currentLedgerHolder.id.collectLatest { ledgerId ->
                summary.value = transactionRepository.homeSummary(ledgerId)
                todayExpense.value = transactionRepository.todayExpense(ledgerId)
                weekExpense.value = transactionRepository.weekExpense(ledgerId)
                todayIncome.value = transactionRepository.todayIncome(ledgerId)
                weekIncome.value = transactionRepository.weekIncome(ledgerId)
                yearIncome.value = transactionRepository.yearIncome(ledgerId)
                yearExpense.value = transactionRepository.yearExpense(ledgerId)
            }
        }
    }
    fun showDialog(type: HomeDialogType) {
        android.util.Log.w("DIALOG_TRACE", "showDialog($type) triggered", RuntimeException("caller stack"))
        _currentDialog.value = type
    }
    fun dismissDialog() { _currentDialog.value = null }
    fun update(transform: (HomeConfig) -> HomeConfig) = homeUiStore.updateConfig(transform)
}
