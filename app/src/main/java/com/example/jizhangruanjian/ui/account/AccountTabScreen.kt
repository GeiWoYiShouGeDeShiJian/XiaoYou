package com.example.jizhangruanjian.ui.account
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.R
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.CoverTextColor
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.AccountGroupDomain
import com.example.jizhangruanjian.ui.theme.AppSpacing
import com.example.jizhangruanjian.ui.theme.OverlayColors
@Composable
fun AccountTabScreen(viewModel: AccountTabViewModel = hiltViewModel(), onMenuClick: () -> Unit, onNavigate: (String) -> Unit, onAddAccount: () -> Unit, onOpenReport: () -> Unit, onManage: () -> Unit, onPickLedger: () -> Unit, onOpenDetail: (Long) -> Unit = {}, onSort: () -> Unit = {}) {
    val groups by viewModel.groups.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val ledgerName by viewModel.ledgerName.collectAsState()
    val config by viewModel.store.config.collectAsState()
    var hideAmount by remember { mutableStateOf(false) }
    var moreMenu by remember { mutableStateOf(false) }
    var searchMode by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var showHidden by remember { mutableStateOf(false) }
    val collapsed = remember { androidx.compose.runtime.mutableStateMapOf<Long, Boolean>() }
    val all = accounts
    val visible = all.filter { !it.hidden && !(it.autoHideZero && it.balance == 0L) }
    val net = visible.filter { it.includeInNet }.sumOf { it.balance }
    val totalAsset = visible.filter { it.includeInNet && it.balance > 0 }.sumOf { it.balance }
    val totalDebt = visible.filter { it.includeInNet && it.balance < 0 }.sumOf { it.balance }
    val textColor = if (config.coverTextColor == CoverTextColor.LIGHT) Color.White else OverlayColors.coverText
    val subColor = if (config.coverTextColor == CoverTextColor.LIGHT) Color.White.copy(alpha = 0.85f) else OverlayColors.coverText.copy(alpha = 0.75f)
    val coverRes = coverResId(config.ledgerCover)
    val orderedGroups = orderGroups(groups, config)
    val sortedAll = sortAccounts(all, config)
    val q = query.trim()
    val filtered = if (q.isEmpty()) sortedAll else sortedAll.filter { it.name.contains(q) || it.note.contains(q) }
    val displayGroups = if (q.isEmpty()) orderedGroups else orderedGroups.filter { g -> filtered.any { it.groupId == g.id } }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs)) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Filled.Menu, contentDescription = "菜单") }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onPickLedger() }.padding(horizontal = AppSpacing.xs)) {
                Text(ledgerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onAddAccount) { Icon(Icons.Filled.AddCircle, contentDescription = "添加账户") }
            IconButton(onClick = onOpenReport) { Icon(Icons.Filled.PieChart, contentDescription = "资产报表") }
            Box {
                IconButton(onClick = { moreMenu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "更多") }
                DropdownMenu(expanded = moreMenu, onDismissRequest = { moreMenu = false }) {
                    DropdownMenuItem(text = { Text("搜索账户") }, onClick = { moreMenu = false; searchMode = !searchMode; if (!searchMode) query = "" })
                    DropdownMenuItem(text = { Text(if (showHidden) "显示全部账户" else "隐藏账户") }, onClick = { moreMenu = false; showHidden = !showHidden })
                    DropdownMenuItem(text = { Text("排序管理") }, onClick = { moreMenu = false; onSort() })
                    DropdownMenuItem(text = { Text("展开/收起所有") }, onClick = {
                        moreMenu = false
                        val target = orderedGroups.any { collapsed[it.id] != true }
                        orderedGroups.forEach { g -> collapsed[g.id] = target }
                    })
                }
            }
        }
        if (searchMode) {
            androidx.compose.material3.OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("搜索账户") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs))
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppSpacing.md)) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(MaterialTheme.shapes.large).clickable { hideAmount = !hideAmount }) {
                if (coverRes != null) {
                    Image(painter = painterResource(id = coverRes), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary))
                }
                Column(modifier = Modifier.fillMaxSize().padding(AppSpacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("净资产", style = MaterialTheme.typography.bodyMedium, color = subColor)
                            Text(if (hideAmount) "****" else Formatters.yuanText(net), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = textColor)
                        }
                        IconButton(onClick = { hideAmount = !hideAmount }) { Icon(if (hideAmount) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "显示/隐藏", tint = subColor) }
                    }
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("总资产 ${if (hideAmount) "****" else Formatters.yuanText(totalAsset)}", style = MaterialTheme.typography.bodyMedium, color = subColor, modifier = Modifier.weight(1f))
                        Text("总负债 ${if (hideAmount) "****" else Formatters.yuanText(totalDebt)}", style = MaterialTheme.typography.bodyMedium, color = subColor)
                    }
                }
            }
            Spacer(Modifier.height(AppSpacing.md))
            if (displayGroups.isEmpty()) {
                Text(if (q.isNotEmpty()) "未找到匹配的账户" else "暂无账户，点右上 ⊕ 创建", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp))
            }
            displayGroups.forEach { g ->
                var groupAccounts = filtered.filter { it.groupId == g.id }
                if (isCreditGroup(g)) groupAccounts = applyCreditSort(groupAccounts, config)
                val groupSum = groupAccounts.sumOf { it.balance }
                val isCollapsed = if (q.isNotEmpty()) false else collapsed[g.id] == true
                GroupCard(g, groupAccounts, groupSum, isCollapsed, hideAmount, showHidden, onClick = { collapsed[g.id] = !isCollapsed }, onAccount = onOpenDetail, onToggleHidden = { viewModel.toggleHidden(it) })
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(AppSpacing.xxl))
        }
    }
}
@Composable
private fun GroupCard(group: AccountGroupDomain, accounts: List<AccountDomain>, sum: Long, collapsed: Boolean, hideAmount: Boolean, showHidden: Boolean, onClick: () -> Unit, onAccount: (Long) -> Unit, onToggleHidden: (AccountDomain) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = AppSpacing.lg, vertical = 14.dp)) {
            Text(group.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(if (hideAmount) "****" else Formatters.yuanText(sum), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(if (collapsed) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!collapsed) {
            val list = if (showHidden) accounts else accounts.filter { !it.hidden && !(it.autoHideZero && it.balance == 0L) }
            if (list.isEmpty()) {
                Text("暂无账户", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = AppSpacing.lg, bottom = AppSpacing.md))
            }
            list.forEach { a ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onAccount(a.id) }.padding(start = AppSpacing.md, end = AppSpacing.lg, bottom = AppSpacing.sm)) {
                    Box(modifier = Modifier.size(34.dp).clip(MaterialTheme.shapes.small).background(Color(a.color).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color(a.color), modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(a.name, style = MaterialTheme.typography.bodyLarge, color = if (a.hidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                    if (a.note.isNotBlank()) {
                        Spacer(Modifier.width(6.dp))
                        Text(a.note, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(if (hideAmount) "****" else Formatters.yuanText(a.balance), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    if (showHidden) {
                        IconButton(onClick = { onToggleHidden(a) }) { Icon(if (a.hidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "显示/隐藏账户", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}
private fun coverResId(cover: String): Int? = when (cover) {
    "cover_pencils" -> R.drawable.cover_pencils
    else -> null
}
