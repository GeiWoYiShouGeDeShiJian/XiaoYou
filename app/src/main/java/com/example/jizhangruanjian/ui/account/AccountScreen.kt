package com.example.jizhangruanjian.ui.account
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.AccountType
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.AccountGroupDomain
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
@Composable
fun AccountScreen(onBack: () -> Unit, viewModel: AccountViewModel = hiltViewModel()) {
    val groups by viewModel.groups.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val message by viewModel.message.collectAsState()
    var addGroup by remember { mutableStateOf(false) }
    var addAccountFor by remember { mutableStateOf<AccountGroupDomain?>(null) }
    var renameGroup by remember { mutableStateOf<AccountGroupDomain?>(null) }
    var renameAccount by remember { mutableStateOf<AccountDomain?>(null) }
    var deleteGroup by remember { mutableStateOf<AccountGroupDomain?>(null) }
    var deleteAccount by remember { mutableStateOf<AccountDomain?>(null) }
    LaunchedEffect(message) { if (message != null) { viewModel.clearMessage() } }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AppTopBar(title = "账户管理", onBack = onBack, actions = { IconButton(onClick = { addGroup = true }) { Icon(Icons.Filled.Add, contentDescription = "新建分组") } }) }) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = AppSpacing.lg)) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            if (groups.isEmpty()) { Text("暂无账户分组，点右上角新建", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            groups.forEach { group ->
                val groupAccounts = accounts.filter { it.groupId == group.id }
                GroupCard(
                    group = group,
                    accountCount = groupAccounts.size,
                    onRename = { renameGroup = group },
                    onDelete = { deleteGroup = group },
                    onMoveUp = { viewModel.moveGroup(group.id, false) },
                    onMoveDown = { viewModel.moveGroup(group.id, true) },
                    onAddAccount = { addAccountFor = group }
                )
                groupAccounts.forEach { account ->
                    AccountRow(account = account, onRename = { renameAccount = account }, onDelete = { deleteAccount = account }, onToggleShared = { viewModel.toggleShared(account.id, !account.isShared) })
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
    message?.let { AlertDialog(onDismissRequest = {}, confirmButton = { TextButton(onClick = { viewModel.clearMessage() }) { Text("确定") } }, title = { Text("提示") }, text = { Text(it) }) }
    if (addGroup) GroupNameDialog("新建分组", "", onConfirm = { viewModel.addGroup(it); addGroup = false }, onDismiss = { addGroup = false })
    addAccountFor?.let { g -> AccountFormDialog(defaultGroup = g, onConfirm = { name, type, initial, shared -> viewModel.addAccount(g.id, name, type, initial, shared); addAccountFor = null }, onDismiss = { addAccountFor = null }) }
    renameGroup?.let { g -> GroupNameDialog("重命名分组", g.name, onConfirm = { viewModel.renameGroup(g.id, it); renameGroup = null }, onDismiss = { renameGroup = null }) }
    renameAccount?.let { a -> GroupNameDialog("重命名账户", a.name, onConfirm = { viewModel.renameAccount(a.id, it); renameAccount = null }, onDismiss = { renameAccount = null }) }
    deleteGroup?.let { g -> AlertDialog(onDismissRequest = { deleteGroup = null }, confirmButton = { TextButton(onClick = { viewModel.deleteGroup(g.id); deleteGroup = null }) { Text("删除", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { deleteGroup = null }) { Text("取消") } }, title = { Text("删除分组") }, text = { Text("确定删除分组「${g.name}」？") }) }
    deleteAccount?.let { a -> AlertDialog(onDismissRequest = { deleteAccount = null }, confirmButton = { TextButton(onClick = { viewModel.deleteAccount(a.id); deleteAccount = null }) { Text("删除", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { deleteAccount = null }) { Text("取消") } }, title = { Text("删除账户") }, text = { Text("确定删除账户「${a.name}」？") }) }
}
}
@Composable
private fun GroupCard(group: AccountGroupDomain, accountCount: Int, onRename: () -> Unit, onDelete: () -> Unit, onMoveUp: () -> Unit, onMoveDown: () -> Unit, onAddAccount: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(group.icon, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (group.isDefault) { Spacer(Modifier.width(6.dp)); Text("预设", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text("$accountCount 个账户", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.ArrowUpward, contentDescription = "上移", modifier = Modifier.size(16.dp)) }
        IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.ArrowDownward, contentDescription = "下移", modifier = Modifier.size(16.dp)) }
        IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Edit, contentDescription = "重命名", modifier = Modifier.size(16.dp)) }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp)) }
        IconButton(onClick = onAddAccount, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Add, contentDescription = "新建账户", modifier = Modifier.size(16.dp)) }
    }
}
@Composable
private fun AccountRow(account: AccountDomain, onRename: () -> Unit, onDelete: () -> Unit, onToggleShared: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
        Box(modifier = Modifier.size(10.dp).background(Color(account.color), CircleShape))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(account.name, style = MaterialTheme.typography.bodyLarge)
                if (account.isShared) { Spacer(Modifier.width(6.dp)); Text("共享", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
            }
            Text(account.type.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(Formatters.yuanText(account.balance), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        IconButton(onClick = onToggleShared, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Share, contentDescription = "共享", modifier = Modifier.size(16.dp), tint = if (account.isShared) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
        IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Edit, contentDescription = "重命名", modifier = Modifier.size(16.dp)) }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp)) }
    }
}
@Composable
private fun GroupNameDialog(title: String, initial: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name.trim()) }) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }, title = { Text(title) }, text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") }, singleLine = true) })
}
@Composable
private fun AccountFormDialog(defaultGroup: AccountGroupDomain, onConfirm: (String, AccountType, Long, Boolean) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.BANK) }
    var initial by remember { mutableStateOf("") }
    var shared by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name.trim(), type, FormattersToLong(initial), shared) }) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }, title = { Text("新建账户到「${defaultGroup.name}」") }, text = {
        Column {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("账户名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AccountType.entries.forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t.label) }) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = initial, onValueChange = { initial = it }, label = { Text("初始余额(元)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("共享账户（关联所有账本，R9）", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = shared, onCheckedChange = { shared = it })
            }
        }
    })
}
private fun FormattersToLong(yuan: String): Long = (yuan.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() }
private val AccountType.label: String
    get() = when (this) { AccountType.CASH -> "现金"; AccountType.BANK -> "银行卡"; AccountType.ALIPAY -> "支付宝"; AccountType.WECHAT -> "微信"; AccountType.OTHER -> "其他" }