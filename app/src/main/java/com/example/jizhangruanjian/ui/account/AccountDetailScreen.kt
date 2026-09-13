package com.example.jizhangruanjian.ui.account
import com.example.jizhangruanjian.ui.theme.SemanticColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.repository.AccountManagerRepository
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.AccountGroupDomain
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.ui.components.AppButton
import com.example.jizhangruanjian.ui.components.AppButtonVariant
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.theme.BalanceCardColors
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
data class AccMonthGroup(val ym: YearMonth, val inflow: Long, val outflow: Long, val txs: List<Pair<TransactionDisplay, Long>>)
@HiltViewModel
class AccountDetailViewModel @Inject constructor(private val accRepo: AccountManagerRepository, private val txRepo: TransactionRepository, private val holder: CurrentLedgerHolder) : ViewModel() {
    private fun currentLedgerId() = holder.id.value
    private val _account = MutableStateFlow<AccountDomain?>(null)
    val account: StateFlow<AccountDomain?> = _account
    private val _groups = MutableStateFlow<List<AccMonthGroup>>(emptyList())
    val groups: StateFlow<List<AccMonthGroup>> = _groups
    private val _totalIn = MutableStateFlow(0L)
    val totalIn: StateFlow<Long> = _totalIn
    private val _totalOut = MutableStateFlow(0L)
    val totalOut: StateFlow<Long> = _totalOut
    fun load(accountId: Long) {
        viewModelScope.launch {
            val acc = accRepo.getAccount(accountId) ?: return@launch
            _account.value = acc
            val ledgerIds = accRepo.ledgerIdsOf(accountId).ifEmpty { listOf(currentLedgerId()) }
            val txs = ledgerIds.flatMap { lid -> txRepo.search(lid, null, null, null, null, listOf(accountId), null, null, null) }.distinctBy { it.id }.sortedByDescending { it.tradeDate }
            var running = acc.balance
            val map = linkedMapOf<YearMonth, MutableList<Pair<TransactionDisplay, Long>>>()
            var tin = 0L
            var tout = 0L
            for (t in txs) {
                val inflow = t.type == TransactionType.INCOME
                if (inflow) tin += t.amount else tout += t.amount
                map.getOrPut(YearMonth.from(Instant.ofEpochMilli(t.tradeDate).atZone(ZoneId.systemDefault()).toLocalDate())) { mutableListOf() }.add(t to running)
                running -= if (inflow) t.amount else -t.amount
            }
            _groups.value = map.map { (ym, list) ->
                val fin = list.filter { it.first.type == TransactionType.INCOME }.sumOf { it.first.amount }
                AccMonthGroup(ym, fin, list.sumOf { it.first.amount } - fin, list)
            }
            _totalIn.value = tin
            _totalOut.value = tout
        }
    }
    fun updateBalance(accountId: Long, balance: Long, onDone: () -> Unit) {
        viewModelScope.launch { accRepo.updateBalance(accountId, balance); load(accountId); onDone() }
    }
}
fun typeLabel(t: com.example.jizhangruanjian.data.model.AccountType): String = when (t) {
    com.example.jizhangruanjian.data.model.AccountType.CASH -> "现金"
    com.example.jizhangruanjian.data.model.AccountType.BANK -> "储蓄卡"
    com.example.jizhangruanjian.data.model.AccountType.ALIPAY -> "网络账户"
    com.example.jizhangruanjian.data.model.AccountType.WECHAT -> "虚拟账户"
    com.example.jizhangruanjian.data.model.AccountType.OTHER -> "其他"
}
@Composable
fun AccountDetailScreen(viewModel: AccountDetailViewModel = hiltViewModel(), accountId: Long, onBack: () -> Unit, onEdit: () -> Unit) {
    val account by viewModel.account.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val totalIn by viewModel.totalIn.collectAsState()
    val totalOut by viewModel.totalOut.collectAsState()
    var balanceDialog by remember { mutableStateOf(false) }
    LaunchedEffect(accountId) { viewModel.load(accountId) }
    val acc = account ?: return
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AppTopBar(title = acc.name, onBack = onBack, actions = { TextButton(onClick = onEdit) { Text("编辑", color = MaterialTheme.colorScheme.primary) } }) }) { pad ->
            Column(modifier = Modifier.fillMaxSize().padding(pad)) {
                Box(modifier = Modifier.padding(horizontal = AppSpacing.lg).fillMaxWidth().clip(MaterialTheme.shapes.extraLarge).background(Brush.linearGradient(listOf(BalanceCardColors.gradientStart, BalanceCardColors.gradientEnd))).padding(AppSpacing.xl)) {
                    Column {
                        Text("人民币余额", color = BalanceCardColors.label, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(Formatters.yuanText(acc.balance), color = BalanceCardColors.amount, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(AppSpacing.sm))
                            Icon(Icons.Filled.Edit, contentDescription = "修改余额", tint = BalanceCardColors.label, modifier = Modifier.size(AppSize.iconSmall).clickable { balanceDialog = true })
                        }
                        Spacer(Modifier.height(AppSpacing.lg))
                        Row {
                            Text("流入 ", color = BalanceCardColors.label, fontSize = 14.sp)
                            Text(Formatters.yuanText(totalIn), color = BalanceCardColors.amount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(AppSpacing.lg))
                            Text("流出 ", color = BalanceCardColors.label, fontSize = 14.sp)
                            Text(Formatters.yuanText(totalOut), color = BalanceCardColors.amount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(AppSpacing.md))
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.md), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    groups.forEach { g ->
                        item(key = "h${g.ym}") {
                            MonthHeader(g)
                        }
                    }
                    item { Spacer(Modifier.height(30.dp)) }
                }
            }
        }
    }
    if (balanceDialog) {
        var input by remember { mutableStateOf(Formatters.yuanText(acc.balance)) }
        AlertDialog(onDismissRequest = { balanceDialog = false }, title = { Text("修改余额") }, text = {
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("余额（元）") }, singleLine = true)
        }, confirmButton = { TextButton(onClick = { viewModel.updateBalance(accountId, runCatching { java.math.BigDecimal(input).multiply(java.math.BigDecimal(100)).setScale(0, java.math.RoundingMode.HALF_UP).toLong() }.getOrDefault(acc.balance)) { balanceDialog = false } }) { Text("确定") } }, dismissButton = { TextButton(onClick = { balanceDialog = false }) { Text("取消") } })
    }
}
@Composable
private fun MonthHeader(g: AccMonthGroup) {
    var expanded by remember { mutableStateOf(true) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainerLow).clickable { expanded = !expanded }.padding(horizontal = AppSpacing.lg, vertical = 10.dp)) {
        Text("${g.ym.year}-${g.ym.monthValue.toString().padStart(2, '0')}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Row {
                Text("流入 ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(Formatters.yuanText(g.inflow), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Row {
                Text("流出 ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(Formatters.yuanText(g.outflow), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(Modifier.width(AppSpacing.md))
        Text(Formatters.yuanText(g.inflow - g.outflow), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Icon(if (expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (expanded) {
        g.txs.forEach { (t, after) -> TxRow(t, after) }
    }
}
@Composable
private fun TxRow(t: TransactionDisplay, balanceAfter: Long) {
    val dt = Instant.ofEpochMilli(t.tradeDate).atZone(ZoneId.systemDefault())
    val inflow = t.type == TransactionType.INCOME
    val amtColor = if (inflow) SemanticColors.IncomeGreen else SemanticColors.ExpenseRed
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainerLow).padding(horizontal = 14.dp, vertical = 10.dp)) {
        CategoryIcon(t.categoryIcon.ifBlank { "💵" }, size = 36.dp, fontSize = 16.sp, fallbackContainer = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        Spacer(Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(t.note.ifBlank { t.categoryName.ifBlank { t.type.name } }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("%02d-%02d %02d:%02d".format(dt.monthValue, dt.dayOfMonth, dt.hour, dt.minute), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text((if (inflow) "+" else "-") + Formatters.yuanText(t.amount), color = amtColor, fontWeight = FontWeight.SemiBold)
            Text("余额 ${Formatters.yuanText(balanceAfter)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@HiltViewModel
class EditAccountViewModel @Inject constructor(private val accRepo: AccountManagerRepository, private val ledgerDao: com.example.jizhangruanjian.core.database.LedgerDao) : ViewModel() {
    private val _account = MutableStateFlow<AccountDomain?>(null)
    val account: StateFlow<AccountDomain?> = _account
    private val _groups = MutableStateFlow<List<AccountGroupDomain>>(emptyList())
    val groups: StateFlow<List<AccountGroupDomain>> = _groups
    private val _ledgers = MutableStateFlow<List<com.example.jizhangruanjian.domain.model.LedgerDomain>>(emptyList())
    val ledgers: StateFlow<List<com.example.jizhangruanjian.domain.model.LedgerDomain>> = _ledgers
    private val _selLedgers = MutableStateFlow<Set<Long>>(emptySet())
    val selLedgers: StateFlow<Set<Long>> = _selLedgers
    fun load(accountId: Long) {
        viewModelScope.launch {
            _account.value = accRepo.getAccount(accountId)
            _ledgers.value = ledgerDao.observeAll().first().map { l -> com.example.jizhangruanjian.domain.model.LedgerDomain(l.id, l.name, l.color, l.icon, l.isDefault, l.sortOrder, l.cover, l.coverDark, l.currency, l.hidden) }
            _selLedgers.value = accRepo.ledgerIdsOf(accountId).toSet()
        }
    }
    init {
        viewModelScope.launch {
            accRepo.observeGroups().collect { _groups.value = it }
        }
    }
    fun save(accountId: Long, name: String, groupId: Long, includeNet: Boolean, hidden: Boolean, autoHide: Boolean, note: String, extra: String, balanceYuan: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val fen = runCatching { java.math.BigDecimal(balanceYuan).multiply(java.math.BigDecimal(100)).setScale(0, java.math.RoundingMode.HALF_UP).toLong() }.getOrDefault(0L)
            accRepo.updateAccount(accountId, name.trim(), groupId, includeNet, hidden, autoHide, note.trim(), extra.trim(), fen)
            accRepo.updateLedgers(accountId, _ledgers.value.map { it.id })
            onDone()
        }
    }
    fun delete(accountId: Long, onDone: () -> Unit) {
        viewModelScope.launch { accRepo.deleteAccount(accountId); onDone() }
    }
    fun toggleLedger(id: Long) { _selLedgers.value = if (id in _selLedgers.value) _selLedgers.value - id else _selLedgers.value + id }
}
@Composable
fun EditAccountScreen(viewModel: EditAccountViewModel = hiltViewModel(), accountId: Long, onBack: () -> Unit, onDeleted: () -> Unit) {
    val account by viewModel.account.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val ledgers by viewModel.ledgers.collectAsState()
    val selLedgers by viewModel.selLedgers.collectAsState()
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(accountId) { viewModel.load(accountId); loaded = true }
    val acc = account
    if (!loaded || acc == null) return
    var fName by remember(acc.id) { mutableStateOf(acc.name) }
    var fInitial by remember(acc.id) { mutableStateOf(Formatters.yuanText(acc.balance)) }
    var fIncludeNet by remember(acc.id) { mutableStateOf(acc.includeInNet) }
    var fHidden by remember(acc.id) { mutableStateOf(acc.hidden) }
    var fAutoHide by remember(acc.id) { mutableStateOf(acc.autoHideZero) }
    var fNote by remember(acc.id) { mutableStateOf(acc.note) }
    var fExtra by remember(acc.id) { mutableStateOf(acc.extraInfo) }
    var groupId by remember(acc.id) { mutableStateOf(acc.groupId) }
    var groupMenu by remember { mutableStateOf(false) }
    var balanceDialog by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf(false) }
    var pickLedgers by remember { mutableStateOf(false) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        AppTopBar(title = "", onBack = onBack, actions = { IconButton(onClick = { viewModel.save(accountId, fName, groupId, fIncludeNet, fHidden, fAutoHide, fNote, fExtra, fInitial) { onBack() } }) { Icon(Icons.Filled.Check, contentDescription = "保存") } })
    }) { pad ->
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(pad).padding(horizontal = AppSpacing.xl)) {
            Text("编辑账户", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(AppSpacing.xxl))
            Text("基本信息", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = fName, onValueChange = { fName = it }, label = { Text("账户名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.md)) {
                Text("账户类型", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text("${typeLabel(acc.type)} ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.md)) {
                Text("账户币种", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text("人民币 ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { balanceDialog = true }.padding(vertical = AppSpacing.md)) {
                Text("账户余额", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text("$fInitial ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(AppSpacing.sm))
            Text("账户设置", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(AppSpacing.xs))
            Box {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { groupMenu = true }.padding(vertical = AppSpacing.md)) {
                    Text("所属分组", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text("${groups.firstOrNull { it.id == groupId }?.name ?: "未分组"} ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = groupMenu, onDismissRequest = { groupMenu = false }) {
                    groups.forEach { g -> DropdownMenuItem(text = { Text(g.name) }, onClick = { groupId = g.id; groupMenu = false }) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { pickLedgers = true }.padding(vertical = AppSpacing.md)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("适用账本", style = MaterialTheme.typography.bodyLarge)
                    Text("设置此账户可在哪些账本中使用", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${if (selLedgers.size == ledgers.size) "全部" else "${selLedgers.size}个账本"} ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) {
                Text("计入净资产", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = fIncludeNet, onCheckedChange = { fIncludeNet = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) {
                Text("隐藏账户", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = fHidden, onCheckedChange = { fHidden = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) {
                Checkbox(checked = fAutoHide, onCheckedChange = { fAutoHide = it })
                Text("余额为0时自动隐藏，非0时自动显示", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(AppSpacing.sm))
            Text("扩展信息（选填）", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(AppSpacing.sm))
            OutlinedTextField(value = fNote, onValueChange = { fNote = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(AppSpacing.md))
            OutlinedTextField(value = fExtra, onValueChange = { fExtra = it }, label = { Text("其他信息") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(28.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                AppButton(text = "删除", onClick = { deleteConfirm = true }, variant = AppButtonVariant.Outlined, modifier = Modifier.weight(1f))
                AppButton(text = "保存", onClick = { viewModel.save(accountId, fName, groupId, fIncludeNet, fHidden, fAutoHide, fNote, fExtra, fInitial) { onBack() } }, variant = AppButtonVariant.Primary, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(AppSpacing.xxl))
        }
    }
    if (balanceDialog) {
        var input by remember { mutableStateOf(fInitial) }
        AlertDialog(onDismissRequest = { balanceDialog = false }, title = { Text("账户余额") }, text = {
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("余额（元）") }, singleLine = true)
        }, confirmButton = { TextButton(onClick = { fInitial = input; balanceDialog = false }) { Text("确定") } }, dismissButton = { TextButton(onClick = { balanceDialog = false }) { Text("取消") } })
    }
    if (deleteConfirm) {
        AlertDialog(onDismissRequest = { deleteConfirm = false }, title = { Text("删除账户") }, text = { Text("确定删除该账户吗？删除后不可恢复。") }, confirmButton = { TextButton(onClick = { viewModel.delete(accountId) { onDeleted() } }) { Text("删除", color = SemanticColors.ExpenseRed) } }, dismissButton = { TextButton(onClick = { deleteConfirm = false }) { Text("取消") } })
    }
    if (pickLedgers) {
        AlertDialog(onDismissRequest = { pickLedgers = false }, title = { Text("适用账本") }, text = {
            Column {
                ledgers.forEach { l ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleLedger(l.id) }.padding(vertical = 6.dp)) {
                        Checkbox(checked = l.id in selLedgers, onCheckedChange = { viewModel.toggleLedger(l.id) })
                        Text(l.name)
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { pickLedgers = false }) { Text("确定") } })
    }
}
