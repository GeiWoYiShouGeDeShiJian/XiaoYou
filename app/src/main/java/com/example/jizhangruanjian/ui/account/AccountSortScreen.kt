package com.example.jizhangruanjian.ui.account
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.HomeConfig
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.AccountGroupDomain
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
fun isCreditGroup(g: AccountGroupDomain): Boolean = g.name.contains("信用")
fun parseInterestDays(extra: String): Int = Regex("免息期[:：]?\\s*(\\d+)").find(extra)?.groupValues?.get(1)?.toIntOrNull() ?: 0
fun parseQuota(extra: String): Long = Regex("额度[:：]?\\s*(\\d+)").find(extra)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
fun orderGroups(groups: List<AccountGroupDomain>, config: HomeConfig): List<AccountGroupDomain> {
    val ids = config.groupOrder.split(",").mapNotNull { it.toLongOrNull() }
    val ordered = ids.mapNotNull { id -> groups.firstOrNull { it.id == id } }
    val rest = groups.filter { it.id !in ids }.sortedBy { it.sortOrder }
    return ordered + rest
}
fun sortAccounts(accounts: List<AccountDomain>, config: HomeConfig): List<AccountDomain> {
    val orderIds = config.accountOrder.split(",").mapNotNull { it.toLongOrNull() }
    val base = when (config.accountSortMode) {
        "custom" -> compareBy<AccountDomain> { a -> orderIds.indexOf(a.id).let { if (it < 0) Int.MAX_VALUE else it } }.thenBy { a -> a.createdAt }
        "balance_desc" -> compareByDescending<AccountDomain> { it.balance }
        "balance_asc" -> compareBy<AccountDomain> { it.balance }
        else -> compareBy<AccountDomain> { it.createdAt }
    }
    return accounts.sortedWith(base)
}
fun applyCreditSort(groupAccounts: List<AccountDomain>, config: HomeConfig): List<AccountDomain> = when {
    config.creditSortInterest == true -> groupAccounts.sortedByDescending { parseInterestDays(it.extraInfo) }
    config.creditSortQuota == true -> groupAccounts.sortedByDescending { parseQuota(it.extraInfo) }
    else -> groupAccounts
}
private fun sortModeLabel(mode: String?): String = when (mode) {
    "custom" -> "自定义顺序"
    "balance_desc" -> "余额从高到低"
    "balance_asc" -> "余额从低到高"
    else -> "按添加顺序"
}
private val SORT_MODES = listOf("custom", "balance_desc", "balance_asc")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSortScreen(onBack: () -> Unit, viewModel: AccountTabViewModel = hiltViewModel()) {
    val groups by viewModel.groups.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val config by viewModel.store.config.collectAsState()
    var page by remember { mutableStateOf("main") }
    var modeDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = page != "main") { page = "main" }
    when (page) {
        "groups" -> GroupOrderPage(groups, config, viewModel, onBack = { page = "main" })
        "accounts" -> AccountOrderPage(accounts, groups, config, viewModel, onBack = { page = "main" })
        else -> Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AppTopBar(title = "排序管理", onBack = onBack) }) { pad ->
            Column(modifier = Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppSpacing.xl)) {
                Spacer(Modifier.height(AppSpacing.xxl))
                Text("账户类型", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(AppSpacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { page = "groups" }.padding(vertical = 14.dp)) {
                    Text("自定义类型顺序", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("账户", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { modeDialog = true }.padding(vertical = 14.dp)) {
                    Text("排序方式", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(sortModeLabel(config.accountSortMode), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { page = "accounts" }.padding(vertical = 14.dp)) {
                    Text("自定义账户顺序", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("信用账户额外配置", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("按免息期自动排序", style = MaterialTheme.typography.bodyLarge)
                        Text("当天消费时可享受的免息期越长，排在越前", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = config.creditSortInterest == true, onCheckedChange = { v -> viewModel.store.updateConfig { c -> c.copy(creditSortInterest = v) } })
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("按可用额度自动排序", style = MaterialTheme.typography.bodyLarge)
                        Text("账户当前的可用额度越多，排在越前", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = config.creditSortQuota == true, onCheckedChange = { v -> viewModel.store.updateConfig { c -> c.copy(creditSortQuota = v) } })
                }
            }
        }
    }
    if (modeDialog) {
        AlertDialog(onDismissRequest = { modeDialog = false }, title = { Text("排序方式") }, text = {
            Column {
                SORT_MODES.forEach { m ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                        viewModel.store.updateConfig { c -> c.copy(accountSortMode = m) }
                        modeDialog = false
                    }) {
                        RadioButton(selected = config.accountSortMode == m, onClick = null)
                        Spacer(Modifier.width(AppSpacing.sm))
                        Text(sortModeLabel(m))
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { modeDialog = false }) { Text("取消") } })
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupOrderPage(groups: List<AccountGroupDomain>, config: HomeConfig, viewModel: AccountTabViewModel, onBack: () -> Unit) {
    val ordered = orderGroups(groups, config)
    val ids = ordered.map { it.id }
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyColumnState(listState) { from, to ->
        viewModel.store.updateConfig { c ->
            val base = orderGroups(groups, c).map { it.id }.toMutableList()
            base.add(to.index, base.removeAt(from.index))
            c.copy(groupOrder = base.joinToString(","))
        }
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AppTopBar(title = "自定义类型顺序", onBack = onBack) }) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.xl)) {
                Text("长按拖动调整账户类型（分组）的显示顺序", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(AppSpacing.md))
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                    items(ids, key = { it }) { id ->
                        val g = ordered.firstOrNull { it.id == id } ?: return@items
                        ReorderableItem(reorderState, key = id) { dragging ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(AppSize.buttonHeight).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (dragging) 1f else 0.6f)).padding(horizontal = AppSpacing.lg).zIndex(if (dragging) 1f else 0f).longPressDraggableHandle()) {
                                Text(g.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                Icon(Icons.Filled.Menu, contentDescription = "拖动", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(AppSize.iconSize))
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountOrderPage(accounts: List<AccountDomain>, groups: List<AccountGroupDomain>, config: HomeConfig, viewModel: AccountTabViewModel, onBack: () -> Unit) {
    val orderedGroups = orderGroups(groups, config)
    val known = accounts.map { it.id }.toSet()
    val saved = config.accountOrder.split(",").mapNotNull { it.toLongOrNull() }.filter { it in known }
    val base = saved + accounts.map { it.id }.filter { it !in saved }
    fun groupList(g: AccountGroupDomain): List<AccountDomain> = accounts.filter { it.groupId == g.id }.sortedBy { a -> base.indexOf(a.id).let { if (it < 0) Int.MAX_VALUE else it } }
    val rows = mutableListOf<Pair<AccountGroupDomain, AccountDomain?>>()
    val keyToGroup = mutableMapOf<String, Long>()
    orderedGroups.forEach { g ->
        val list = groupList(g)
        if (list.isEmpty()) return@forEach
        rows.add(g to null)
        keyToGroup["g_${g.id}"] = g.id
        list.forEach { a ->
            rows.add(g to a)
            keyToGroup["a_${a.id}"] = g.id
        }
    }
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyColumnState(listState) { from, to ->
        val fromGroup = keyToGroup[from.key as? String ?: return@rememberReorderableLazyColumnState] ?: return@rememberReorderableLazyColumnState
        val toKey = to.key as? String ?: return@rememberReorderableLazyColumnState
        if (!toKey.startsWith("a_")) return@rememberReorderableLazyColumnState
        val toGroup = keyToGroup[toKey] ?: return@rememberReorderableLazyColumnState
        if (fromGroup != toGroup) return@rememberReorderableLazyColumnState
        val fromId = from.key.toString().removePrefix("a_").toLongOrNull() ?: return@rememberReorderableLazyColumnState
        val toId = toKey.removePrefix("a_").toLongOrNull() ?: return@rememberReorderableLazyColumnState
        viewModel.store.updateConfig { c ->
            val s = c.accountOrder.split(",").mapNotNull { it.toLongOrNull() }.filter { it in known }
            val newIds = orderedGroups.flatMap { g ->
                val l = groupList(g).map { it.id }.toMutableList()
                if (g.id == fromGroup) {
                    val fi = l.indexOf(fromId)
                    if (fi >= 0) {
                        l.removeAt(fi)
                        val ti = l.indexOf(toId)
                        l.add(if (ti < 0) l.size else ti, fromId)
                    }
                }
                l
            } + accounts.filter { a -> orderedGroups.none { it.id == a.groupId } }.map { it.id }
            c.copy(accountOrder = newIds.joinToString(","))
        }
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AppTopBar(title = "账户排序", onBack = onBack) }) { pad ->
        LazyColumn(state = listState, modifier = Modifier.padding(pad).fillMaxSize().padding(horizontal = AppSpacing.md), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppSpacing.sm)) {
            items(rows.size, key = { keyFor(rows[it]) }) { i ->
                val (g, a) = rows[i]
                if (a == null) {
                    ReorderableItem(reorderState, key = "g_${g.id}") {
                        Text(g.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(horizontal = AppSpacing.lg, vertical = 14.dp))
                    }
                } else {
                    ReorderableItem(reorderState, key = "a_${a.id}") { dragging ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = if (dragging) 1f else 0.8f)).padding(horizontal = AppSpacing.md, vertical = 10.dp).zIndex(if (dragging) 1f else 0f)) {
                            Box(modifier = Modifier.size(34.dp).clip(MaterialTheme.shapes.small).background(Color(a.color).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color(a.color), modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(AppSpacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(a.name, style = MaterialTheme.typography.bodyLarge)
                                if (a.note.isNotBlank()) Text(a.note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Text(Formatters.yuanText(a.balance), style = MaterialTheme.typography.bodyLarge, color = if (a.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(AppSpacing.md))
                            Icon(Icons.Filled.Menu, contentDescription = "拖动排序", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.longPressDraggableHandle())
                        }
                    }
                }
            }
            item {
                Text("按住右侧的\"=\"上下拖动排序", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xl))
            }
        }
    }
}
private fun keyFor(row: Pair<AccountGroupDomain, AccountDomain?>): String = if (row.second == null) "g_${row.first.id}" else "a_${row.second!!.id}"
