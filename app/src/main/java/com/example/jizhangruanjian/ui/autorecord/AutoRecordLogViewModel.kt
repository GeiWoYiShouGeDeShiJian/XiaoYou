package com.example.jizhangruanjian.ui.autorecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.autorecord.AutoRecordLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AutoRecordLogViewModel @Inject constructor(
    private val logger: AutoRecordLogger
) : ViewModel() {
    val logs = MutableStateFlow<List<String>>(emptyList())
    fun load() {
        viewModelScope.launch { logs.value = logger.entries() }
    }
    fun clear() {
        viewModelScope.launch { logger.clear(); logs.value = emptyList() }
    }
}
