package com.example.jizhangruanjian.ui.account
import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.data.model.AccountType
import com.example.jizhangruanjian.data.repository.AccountManagerRepository
import com.example.jizhangruanjian.domain.model.AccountGroupDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@HiltViewModel
class AddAccountViewModel @Inject constructor(private val repo: AccountManagerRepository, private val store: com.example.jizhangruanjian.data.HomeUiStore, ledgerDao: com.example.jizhangruanjian.core.database.LedgerDao) : ViewModel() {
    val groups: StateFlow<List<AccountGroupDomain>> = repo.observeGroups().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val ledgers: StateFlow<List<com.example.jizhangruanjian.domain.model.LedgerDomain>> = ledgerDao.observeAll().map { list -> list.map { l -> com.example.jizhangruanjian.domain.model.LedgerDomain(l.id, l.name, l.color, l.icon, l.isDefault, l.sortOrder, l.cover, l.coverDark, l.currency, l.hidden) } }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val customTypes: StateFlow<List<Pair<String, String>>> = store.config.map { c ->
        c.customAccountTypes.split(";").filter { it.isNotBlank() }.map { item -> val parts = item.split("|"); (parts.getOrNull(0) ?: "") to (parts.getOrNull(1) ?: "") }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun addCustomType(name: String, desc: String) {
        store.updateConfig { c -> c.copy(customAccountTypes = (c.customAccountTypes.split(";").filter { it.isNotBlank() } + "$name|$desc").joinToString(";")) }
    }
    fun resolveGroupId(type: AccountType, groups: List<AccountGroupDomain>, onId: (Long) -> Unit) {
        viewModelScope.launch {
            val auto = autoGroupByType(type, groups)
            if (auto != null) { onId(auto.id); return@launch }
            val name = when (type) { AccountType.CASH -> "现金"; AccountType.BANK -> "储蓄卡"; AccountType.ALIPAY -> "网络账户"; AccountType.WECHAT -> "信用账户"; AccountType.OTHER -> "其他" }
            onId(repo.createGroup(name))
        }
    }
    fun create(groupId: Long, name: String, type: AccountType, initialYuan: String, includeNet: Boolean, hiddenAcc: Boolean, autoHide: Boolean, note: String, extra: String, ledgerIds: List<Long>?, onDone: () -> Unit) {
        viewModelScope.launch {
            val fen = try { BigDecimal(initialYuan).multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toLong() } catch (e: Exception) { 0L }
            repo.createAccount(groupId, name, type, fen, false, includeNet, hiddenAcc, autoHide, note, extra, ledgerIds)
            onDone()
        }
    }
}
private data class SubItem(val name: String, val desc: String, val short: String, val color: Long, val nameSuffix: String = "")
private data class AccountPreset(val emoji: String, val color: Color, val title: String, val desc: String, val type: AccountType, val defaultName: String, val children: List<SubItem>? = null)
private val PRESETS = listOf(
    AccountPreset("💳", Color(0xFF2196F3), "现金", "钱包里的现金", AccountType.CASH, "现金"),
    AccountPreset("🏦", Color(0xFF4CAF50), "储蓄卡", "银行储蓄卡、借记卡", AccountType.BANK, "储蓄卡", children = listOf(
        SubItem("工商银行", "", "工", 0xFFC7000B, "储蓄卡"), SubItem("农业银行", "", "农", 0xFF00926D, "储蓄卡"),
        SubItem("中国银行", "", "中", 0xFFA6093D, "储蓄卡"), SubItem("建设银行", "", "建", 0xFF0066B3, "储蓄卡"),
        SubItem("交通银行", "", "交", 0xFF003F8C, "储蓄卡"), SubItem("重庆银行", "", "渝", 0xFF0069B7, "储蓄卡"),
        SubItem("重庆农村商业银行", "", "农", 0xFF00843D, "储蓄卡"), SubItem("重庆三峡银行", "", "三", 0xFFC8102E, "储蓄卡")
    )),
    AccountPreset("🏧", Color(0xFFFF9800), "信用账户", "银行信用卡、花呗等", AccountType.OTHER, "信用账户"),
    AccountPreset("🌐", Color(0xFFE91E63), "网络账户", "支付宝、微信钱包等", AccountType.ALIPAY, "网络账户"),
    AccountPreset("💰", Color(0xFF9C27B0), "投资账户", "理财、基金、股票等", AccountType.OTHER, "投资账户"),
    AccountPreset("🎫", Color(0xFFF44336), "储值卡", "购物卡、餐卡、加油卡等", AccountType.OTHER, "储值卡"),
    AccountPreset("☁️", Color(0xFF00BCD4), "虚拟账户", "积分、游戏币等虚拟资产", AccountType.WECHAT, "虚拟账户"),
    AccountPreset("💱", Color(0xFF009688), "借贷", "借款还款、债务债权等", AccountType.OTHER, "借贷")
)
fun autoGroupByType(type: AccountType, groups: List<AccountGroupDomain>): AccountGroupDomain? {
    val keys = when (type) { AccountType.CASH -> listOf("现金"); AccountType.BANK -> listOf("储蓄卡", "银行卡"); AccountType.ALIPAY -> listOf("网络"); AccountType.WECHAT -> listOf("信用", "虚拟"); AccountType.OTHER -> listOf("其他") }
    return groups.firstOrNull { g -> keys.any { g.name.contains(it) } }
}
@Composable
fun AddAccountScreen(viewModel: AddAccountViewModel = hiltViewModel(), onBack: () -> Unit) {
    val groups by viewModel.groups.collectAsState()
    val ledgers by viewModel.ledgers.collectAsState()
    val customTypes by viewModel.customTypes.collectAsState()
    val context = LocalContext.current
    var page by remember { mutableStateOf("list") }
    var formPreset by remember { mutableStateOf<AccountPreset?>(null) }
    var fName by remember { mutableStateOf("") }
    var fInitial by remember { mutableStateOf("") }
    var fIncludeNet by remember { mutableStateOf(true) }
    var fHidden by remember { mutableStateOf(false) }
    var fAutoHide by remember { mutableStateOf(false) }
    var fNote by remember { mutableStateOf("") }
    var fExtra by remember { mutableStateOf("") }
    var selAll by remember { mutableStateOf(true) }
    var selLedgers by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var groupId by remember { mutableStateOf(0L) }
    var balanceDialog by remember { mutableStateOf(false) }
    var formReturn by remember { mutableStateOf("list") }
    var subPreset by remember { mutableStateOf<AccountPreset?>(null) }
    BackHandler(enabled = page != "list") {
        page = when (page) {
            "ledgers" -> "form"
            "form" -> formReturn
            else -> "list"
        }
    }
    when (page) {
        "sub" -> SubListScreen(preset = subPreset ?: return, groups = groups, onBack = { page = "list" }, onPick = { item ->
            formPreset = AccountPreset(subPreset?.emoji ?: "💳", subPreset?.color ?: Color.Gray, subPreset?.title ?: "账户", "子类型", subPreset?.type ?: AccountType.OTHER, item.name + item.nameSuffix)
            fName = item.name + item.nameSuffix; formReturn = "sub"; page = "form"
        })
        "custom" -> CustomTypeScreen(onBack = { page = "list" }, onSave = { name, desc -> viewModel.addCustomType(name, desc); page = "list" })
        "ledgers" -> LedgerSelectScreen(ledgers = ledgers, selAll = selAll, selLedgers = selLedgers, onBack = { page = "form" }, onAll = { selAll = it; if (it) selLedgers = emptySet() }, onToggle = { id -> selAll = false; selLedgers = if (id in selLedgers) selLedgers - id else selLedgers + id })
        "form" -> CreateAccountScreen(
            preset = formPreset ?: AccountPreset("💳", Color(0xFF2196F3), "账户", "", AccountType.OTHER, "账户"),
            name = fName, onName = { fName = it },
            initial = fInitial,
            includeNet = fIncludeNet, onIncludeNet = { fIncludeNet = it },
            hidden = fHidden, onHidden = { fHidden = it },
            autoHide = fAutoHide, onAutoHide = { fAutoHide = it },
            note = fNote, onNote = { fNote = it },
            extra = fExtra, onExtra = { fExtra = it },
            ledgerValue = if (selAll) "全部" else "${selLedgers.size}个账本",
            onPickLedgers = { page = "ledgers" },
            groups = groups, groupId = groupId, onGroup = { groupId = it },
            balanceDialog = balanceDialog, onBalanceDialog = { balanceDialog = it },
            onBack = { page = formReturn },
            onSave = {
                if (fName.isNotBlank()) {
                    val doCreate: (Long) -> Unit = { gid -> viewModel.create(gid, fName.trim(), formPreset?.type ?: AccountType.OTHER, fInitial, fIncludeNet, fHidden, fAutoHide, fNote.trim(), fExtra.trim(), ledgers.map { it.id }) { page = "list"; onBack() } }
                    if (groupId != 0L) doCreate(groupId) else viewModel.resolveGroupId(formPreset?.type ?: AccountType.OTHER, groups, doCreate)
                }
            }
        )
        else -> Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                Text("添加账户", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { page = "custom" }) { Icon(Icons.Filled.AddCircle, contentDescription = "自定义账户类型") }
            }
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface)) {
                PRESETS.forEach { p ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                        if (groups.isEmpty()) { Toast.makeText(context, "请先在账户管理中创建分组", Toast.LENGTH_SHORT).show(); return@clickable }
                        if (p.children != null) { subPreset = p; formReturn = "sub"; page = "sub" } else { formPreset = p; fName = p.defaultName; formReturn = "list"; page = "form" }
                    }.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(p.color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Text(p.emoji, fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(p.desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                customTypes.forEach { (name, desc) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                        if (groups.isEmpty()) { Toast.makeText(context, "请先在账户管理中创建分组", Toast.LENGTH_SHORT).show(); return@clickable }
                        formPreset = AccountPreset("🧩", Color(0xFF607D8B), name, desc.ifBlank { "自定义类型" }, AccountType.OTHER, name); fName = name; formReturn = "list"; page = "form"
                    }.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF607D8B).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Text("🧩", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            if (desc.isNotBlank()) Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    if (balanceDialog) {
        var input by remember { mutableStateOf(fInitial) }
        AlertDialog(onDismissRequest = { balanceDialog = false }, title = { Text("账户余额") }, text = {
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("余额（元）") }, singleLine = true)
        }, confirmButton = { TextButton(onClick = { fInitial = input; balanceDialog = false }) { Text("确定") } }, dismissButton = { TextButton(onClick = { balanceDialog = false }) { Text("取消") } })
    }
}
@Composable
private fun SubListScreen(preset: AccountPreset, groups: List<AccountGroupDomain>, onBack: () -> Unit, onPick: (SubItem) -> Unit) {
    var query by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
            if (showSearch) {
                OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("搜索") }, singleLine = true, modifier = Modifier.weight(1f).padding(end = 12.dp))
            } else {
                Text("添加${preset.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { showSearch = true }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
            }
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface)) {
            (preset.children ?: emptyList()).filter { it.name.contains(query.trim()) }.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onPick(item) }.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(17.dp)).background(Color(item.color)), contentAlignment = Alignment.Center) {
                        Text(item.short, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name + item.nameSuffix, style = MaterialTheme.typography.bodyLarge)
                        if (item.desc.isNotBlank()) Text(item.desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
@Composable
private fun CustomTypeScreen(onBack: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
            Text("自定义账户类型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("类型名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("类型说明（选填）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primary).clickable(enabled = name.isNotBlank()) { onSave(name.trim(), desc.trim()) }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                Text("保存", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
@Composable
private fun CreateAccountScreen(preset: AccountPreset, name: String, onName: (String) -> Unit, initial: String, includeNet: Boolean, onIncludeNet: (Boolean) -> Unit, hidden: Boolean, onHidden: (Boolean) -> Unit, autoHide: Boolean, onAutoHide: (Boolean) -> Unit, note: String, onNote: (String) -> Unit, extra: String, onExtra: (String) -> Unit, ledgerValue: String, onPickLedgers: () -> Unit, groups: List<AccountGroupDomain>, groupId: Long, onGroup: (Long) -> Unit, balanceDialog: Boolean, onBalanceDialog: (Boolean) -> Unit, onBack: () -> Unit, onSave: () -> Unit) {
    var groupMenu by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSave) { Icon(Icons.Filled.Check, contentDescription = "保存") }
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("添加${preset.title}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Text("基本信息", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = name, onValueChange = onName, label = { Text("账户名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Text("账户图标", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(preset.color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Text(preset.emoji, fontSize = 15.sp) }
                Text(" ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onBalanceDialog(true) }.padding(vertical = 12.dp)) {
                Text("账户余额", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text("${if (initial.isBlank()) "0.00" else initial} ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text("所属分组", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Box {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { groupMenu = true }.padding(vertical = 12.dp)) {
                    Text("所属分组", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text("${groups.firstOrNull { it.id == (if (groupId != 0L) groupId else autoGroupByType(preset.type, groups)?.id ?: 0L) }?.name ?: "未分组"} ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = groupMenu, onDismissRequest = { groupMenu = false }) {
                    groups.forEach { g -> DropdownMenuItem(text = { Text(g.name) }, onClick = { onGroup(g.id); groupMenu = false }) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("账户设置", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onPickLedgers() }.padding(vertical = 12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("适用账本", style = MaterialTheme.typography.bodyLarge)
                    Text("设置此账户可在哪些账本中使用", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("$ledgerValue ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("计入净资产", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = includeNet, onCheckedChange = onIncludeNet)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("隐藏账户", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = hidden, onCheckedChange = onHidden)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                androidx.compose.material3.Checkbox(checked = autoHide, onCheckedChange = onAutoHide)
                Text("余额为0时自动隐藏，非0时自动显示", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text("扩展信息（选填）", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = note, onValueChange = onNote, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = extra, onValueChange = onExtra, label = { Text("其他信息") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primary).clickable { onSave() }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                Text("保存", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
@Composable
private fun LedgerSelectScreen(ledgers: List<com.example.jizhangruanjian.domain.model.LedgerDomain>, selAll: Boolean, selLedgers: Set<Long>, onBack: () -> Unit, onAll: (Boolean) -> Unit, onToggle: (Long) -> Unit) {
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
            if (showSearch) {
                OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("搜索账本") }, singleLine = true, modifier = Modifier.weight(1f).padding(end = 12.dp))
            } else {
                Text("账本", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { showSearch = true }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
            }
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)).clickable { onAll(!selAll) }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                androidx.compose.material3.Checkbox(checked = selAll, onCheckedChange = { onAll(it) })
                Text("全部", style = MaterialTheme.typography.bodyLarge)
            }
            ledgers.filter { it.name.contains(query.trim()) }.forEach { l ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)).clickable { onToggle(l.id) }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    androidx.compose.material3.Checkbox(checked = !selAll && l.id in selLedgers, onCheckedChange = { onToggle(l.id) })
                    Text(l.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
