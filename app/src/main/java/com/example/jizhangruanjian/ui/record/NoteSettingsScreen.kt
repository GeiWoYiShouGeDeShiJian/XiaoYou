package com.example.jizhangruanjian.ui.record
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import com.example.jizhangruanjian.ui.components.CollapsingTitleScaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jizhangruanjian.core.database.AppSettingDao
import com.example.jizhangruanjian.data.model.AppSetting
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteSettingsViewModel @Inject constructor(
    private val appSettingDao: AppSettingDao,
    private val transactionRepository: TransactionRepository,
    private val currentLedgerHolder: CurrentLedgerHolder
) : ViewModel() {
    val showHistory = MutableStateFlow(true)
    val historyAfterInput = MutableStateFlow(false)
    val showCommon = MutableStateFlow(false)
    val commonNotes = MutableStateFlow<List<String>>(emptyList())
    val remarkHistory: StateFlow<List<Pair<TransactionType, String>>> = transactionRepository.observeNoteHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val commonNoteScopes = MutableStateFlow<Map<String, Set<TransactionType>>>(emptyMap())
    init {
        viewModelScope.launch {
            showHistory.value = appSettingDao.get("note_show_history")?.value != "0"
            historyAfterInput.value = appSettingDao.get("note_history_after_input")?.value == "1"
            showCommon.value = appSettingDao.get("note_show_common")?.value == "1"
            commonNotes.value = appSettingDao.get("common_notes")?.value?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
        }
        viewModelScope.launch {
            appSettingDao.observeAll().map { l -> l.filter { it.key.startsWith("common_note_scope:") }.associate { it.key.removePrefix("common_note_scope:") to it.value.split("|").mapNotNull { s -> runCatching { TransactionType.valueOf(s) }.getOrNull() }.toSet() } }.collect { commonNoteScopes.value = it }
        }
    }
    private fun save(key: String, v: Boolean) = viewModelScope.launch { appSettingDao.upsert(AppSetting(key, if (v) "1" else "0")) }
    fun setShowHistory(v: Boolean) { showHistory.value = v; save("note_show_history", v) }
    fun setHistoryAfterInput(v: Boolean) { historyAfterInput.value = v; save("note_history_after_input", v) }
    fun setShowCommon(v: Boolean) { showCommon.value = v; save("note_show_common", v) }
    fun addCommonNote(text: String) = viewModelScope.launch { if (text.isNotBlank() && text !in commonNotes.value) { val l = commonNotes.value + text.trim(); commonNotes.value = l; appSettingDao.upsert(AppSetting("common_notes", l.joinToString("\n"))) } }
    fun removeCommonNote(text: String) = viewModelScope.launch { val l = commonNotes.value - text; commonNotes.value = l; appSettingDao.upsert(AppSetting("common_notes", l.joinToString("\n"))) }
    fun scopesOf(note: String): Set<TransactionType> = commonNoteScopes.value[note] ?: TransactionType.entries.toSet()
    fun setCommonNoteScope(note: String, scopes: Set<TransactionType>) = viewModelScope.launch { commonNoteScopes.value = commonNoteScopes.value + (note to scopes); appSettingDao.upsert(AppSetting("common_note_scope:$note", scopes.joinToString("|") { it.name })) }
    suspend fun noteForCategory(categoryId: Long): String? = currentLedgerHolder.id.first().let { lid -> transactionRepository.observeByLedger(lid).first().filter { it.categoryId == categoryId }.maxByOrNull { it.tradeDate }?.note }
    private fun slotOf(tradeDate: Long): Int { val t = java.time.Instant.ofEpochMilli(tradeDate).atZone(java.time.ZoneId.systemDefault()); val m = t.hour * 60 + t.minute; return when { m in 301..660 -> 0; m in 661..960 -> 1; m in 961..1260 -> 2; else -> 3 } }
    fun rememberNoteCategory(type: com.example.jizhangruanjian.data.model.TransactionType, note: String, categoryId: Long, tradeDate: Long) {
        val n = note.trim()
        if (n.isBlank() || categoryId == 0L) return
        viewModelScope.launch { appSettingDao.upsert(AppSetting("ncm:${type.name}|${slotOf(tradeDate)}|$n", categoryId.toString())) }
    }
    suspend fun categoryForNote(note: String, type: com.example.jizhangruanjian.data.model.TransactionType?, tradeDate: Long, categories: List<com.example.jizhangruanjian.domain.model.CategoryDomain>): Long? {
        val n = note.trim()
        if (n.isEmpty() || type == null) return null
        val cur = slotOf(tradeDate)
        val base = appSettingDao.get("ncm:${type.name}|$cur|$n")?.value?.toLongOrNull()
            ?: (0..3).firstNotNullOfOrNull { s -> if (s != cur) appSettingDao.get("ncm:${type.name}|$s|$n")?.value?.toLongOrNull() else null }
            ?: run {
                val list = currentLedgerHolder.id.first().let { lid -> transactionRepository.observeByLedger(lid).first() }.filter { it.note == n && it.type == type }
                if (list.isEmpty()) return null
                val same = list.filter { slotOf(it.tradeDate) == cur }
                (same.maxByOrNull { it.tradeDate } ?: list.maxByOrNull { it.tradeDate })?.categoryId
            } ?: return null
        if (base == 0L) return null
        val slotName = listOf("早餐", "午餐", "晚餐", "夜宵")[cur]
        val baseCat = categories.firstOrNull { it.id == base } ?: return base
        val parentId = baseCat.parentId ?: baseCat.id
        return categories.firstOrNull { it.parentId == parentId && it.name == slotName }?.id ?: base
    }
}
@Composable
fun NoteSettingsScreen(onBack: () -> Unit, viewModel: NoteSettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    var manage by remember { mutableStateOf(false) }
    if (manage) { BackHandler { manage = false }; CommonNotesManageScreen(onBack = { manage = false }, viewModel = viewModel); return }
    val showHistory by viewModel.showHistory.collectAsStateWithLifecycle()
    val afterInput by viewModel.historyAfterInput.collectAsStateWithLifecycle()
    val showCommon by viewModel.showCommon.collectAsStateWithLifecycle()
    CollapsingTitleScaffold(title = "备注设置", onBack = onBack) {
            Text("历史备注", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            SettingRow("显示历史备注", "输入备注时，自动显示相关历史备注以供选择", showHistory) { viewModel.setShowHistory(it) }
            SettingRow("仅输入后显示历史备注", "未输入备注时不显示最近备注记录", afterInput) { viewModel.setHistoryAfterInput(it) }
            Text("常用备注", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            SettingRow("显示常用备注", "未输入备注时，优先显示常用备注以供选择", showCommon) { viewModel.setShowCommon(it) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { manage = true }.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text("常用备注管理", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
    }
}
@Composable
private fun SettingRow(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
@Composable
private fun CommonNotesManageScreen(onBack: () -> Unit, viewModel: NoteSettingsViewModel) {
    val notes by viewModel.commonNotes.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var scopeEditFor by remember { mutableStateOf<String?>(null) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, floatingActionButton = {
        ExtendedFloatingActionButton(onClick = { input = ""; showAdd = true }, containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
            Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("添加备注")
        }
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                Text("常用备注", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            notes.forEach { n ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(horizontal = 16.dp, vertical = 18.dp)) {
                    Text(n, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), maxLines = 1)
                    Icon(Icons.Filled.Settings, "使用范围", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp).clickable { scopeEditFor = n })
                    Spacer(Modifier.width(14.dp))
                    Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp).clickable { viewModel.removeCommonNote(n) })
                }
            }
            if (notes.isEmpty()) Text("暂无常用备注", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(20.dp))
        }
    }
    if (showAdd) {
        AlertDialog(onDismissRequest = { showAdd = false }, containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            title = { Text("添加", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { OutlinedTextField(value = input, onValueChange = { input = it }, placeholder = { Text("请输入备注内容", color = MaterialTheme.colorScheme.onSurfaceVariant) }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { viewModel.addCommonNote(input); showAdd = false }) { Text("确定", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("取消", color = MaterialTheme.colorScheme.primary) } })
    }
    if (scopeEditFor != null) {
        val target = scopeEditFor ?: ""
        var sel by remember(target) { mutableStateOf(viewModel.scopesOf(target)) }
        AlertDialog(onDismissRequest = { scopeEditFor = null }, containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            title = { Text("使用范围", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(TransactionType.EXPENSE to "支出", TransactionType.INCOME to "收入", TransactionType.TRANSFER to "转账", TransactionType.LOAN to "借贷").forEach { (t, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { sel = if (t in sel) sel - t else sel + t }) {
                            Checkbox(checked = t in sel, onCheckedChange = { if (it) sel = sel + t else sel = sel - t })
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.setCommonNoteScope(target, sel); scopeEditFor = null }) { Text("确定", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { scopeEditFor = null }) { Text("取消", color = MaterialTheme.colorScheme.primary) } })
    }
}
