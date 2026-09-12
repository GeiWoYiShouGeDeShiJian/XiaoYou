package com.example.jizhangruanjian.ui.detail
import com.example.jizhangruanjian.ui.theme.SemanticColors
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.components.SourceBadge
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
private val expenseRed = SemanticColors.ExpenseRed
private val incomeGreen = SemanticColors.IncomeGreen
private val accentBlue = Color(0xFFB3C7E6)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthDetailScreen(onBack: () -> Unit, onSearch: () -> Unit, onEdit: (Long) -> Unit, onCopy: (Long) -> Unit, onDelete: (Long) -> Unit, onAddRecord: () -> Unit, viewModel: MonthDetailViewModel = hiltViewModel()) {
    val list by viewModel.transactions.collectAsStateWithLifecycle()
    val month by viewModel.month.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val sourceFilter by viewModel.sourceFilter.collectAsStateWithLifecycle()
    var batch by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var splitId by remember { mutableStateOf<Long?>(null) }
    var picker by remember { mutableStateOf(false) }
    var topMenu by remember { mutableStateOf(false) }
    var sourceMenu by remember { mutableStateOf(false) }
    val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = income + expense
    val zone = ZoneId.systemDefault()
    val days = if (month == YearMonth.now()) LocalDate.now().dayOfMonth else month.lengthOfMonth()
    val sortLabels = listOf("时间最新优先", "时间最早优先", "金额从大到小")
    Scaffold(containerColor = MaterialTheme.colorScheme.background, floatingActionButton = { SmallFloatingActionButton(onClick = onAddRecord, containerColor = accentBlue, shape = RoundedCornerShape(16.dp)) { Icon(Icons.Filled.Add, "记一笔", tint = MaterialTheme.colorScheme.primary) } }, bottomBar = {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 24.dp, vertical = 10.dp)) {
            Text("←", style = MaterialTheme.typography.titleLarge, modifier = Modifier.clickable { viewModel.prevMonth() }.padding(8.dp))
            Text("%04d-%02d".format(month.year, month.monthValue), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f).clickable { picker = true })
            Text("→", style = MaterialTheme.typography.titleLarge, modifier = Modifier.clickable { viewModel.nextMonth() }.padding(8.dp))
        }
    }) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            Column(Modifier.fillMaxSize()) {
            if (batch) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Text("已选 ${selected.size} 项", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = { selected = list.map { it.id }.toSet() }) { Text("全选") }
                    TextButton(onClick = { viewModel.deleteMany(selected.toList()); selected = setOf(); batch = false }) { Text("删除", color = expenseRed) }
                    IconButton(onClick = { batch = false; selected = setOf() }) { Icon(Icons.Filled.Close, null) }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text("${month.year}年${month.monthValue}月", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, "搜索") }
                    Box { IconButton(onClick = { topMenu = true }) { Icon(Icons.Filled.MoreVert, "更多") }
                        DropdownMenu(expanded = topMenu, onDismissRequest = { topMenu = false }) { DropdownMenuItem(text = { Text("回到本月") }, onClick = { topMenu = false; viewModel.backToNow() }) }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard(Modifier.weight(1f), "收入", Formatters.yuanText(income), incomeGreen, "日均 ${Formatters.yuanText(income / days)}")
                SummaryCard(Modifier.weight(1f), "支出", Formatters.yuanText(expense), expenseRed, "日均 ${Formatters.yuanText(expense / days)}")
                SummaryCard(Modifier.weight(1f), "结余", Formatters.yuanText(balance), if (balance < 0) expenseRed else Color.Unspecified, "日均 ${Formatters.yuanText(balance / days)}")
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text("明细列表", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("共${list.size}笔", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Box {
                    Text("来源：${sourceFilterLabel(sourceFilter)} ▾", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { sourceMenu = true })
                    DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) {
                        DropdownMenuItem(text = { Text("全部") }, onClick = { viewModel.setSourceFilter(0); sourceMenu = false })
                        DropdownMenuItem(text = { Text("手动") }, onClick = { viewModel.setSourceFilter(1); sourceMenu = false })
                        DropdownMenuItem(text = { Text("语音") }, onClick = { viewModel.setSourceFilter(2); sourceMenu = false })
                        DropdownMenuItem(text = { Text("自动") }, onClick = { viewModel.setSourceFilter(3); sourceMenu = false })
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(sortLabels[sortMode] + " ⇅", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { viewModel.sortMode.value = (sortMode + 1) % 3 })
            }
            val grouped = list.groupBy { Instant.ofEpochMilli(it.tradeDate).atZone(zone).toLocalDate() }
            LazyColumn(Modifier.fillMaxSize()) {
                grouped.forEach { (date, items) ->
                    item(key = "d-${date.toEpochDay()}") {
                        val today = LocalDate.now()
                        val label = when (date) { today -> "今天"; today.minusDays(1) -> "昨天"; else -> "${date.monthValue}月${date.dayOfMonth}日" }
                        Text("$label ${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 16.dp, vertical = 6.dp))
                    }
                    items(items, key = { it.id }) { tx ->
                        MonthTxRow(tx, batch, tx.id in selected, onClick = { if (batch) { selected = if (tx.id in selected) selected - tx.id else selected + tx.id } else onEdit(tx.id) }, onLongClick = { if (!batch) selected = setOf(tx.id).also { batch = true } }, menuContent = { dismiss ->
                            DropdownMenuItem(text = { Text("批量操作") }, onClick = { dismiss(); batch = true; selected = setOf(tx.id) })
                            DropdownMenuItem(text = { Text("复制") }, onClick = { dismiss(); onCopy(tx.id) })
                            DropdownMenuItem(text = { Text("拆分") }, onClick = { dismiss(); splitId = tx.id })
                            DropdownMenuItem(text = { Text("更多操作") }, onClick = { dismiss(); onEdit(tx.id) })
                            DropdownMenuItem(text = { Text("删除") }, onClick = { dismiss(); onDelete(tx.id) })
                        })
                    }
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
            }
        }
        if (picker) MonthPickerDialog(month) { viewModel.setMonth(it.year, it.monthValue); picker = false }
        splitId?.let { id -> SplitDialog(onDismiss = { splitId = null }) { n -> viewModel.split(id, n); splitId = null } }
    }
}
@Composable
private fun SummaryCard(modifier: Modifier, title: String, value: String, valueColor: Color, sub: String) {
    Column(Modifier.background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp)).padding(vertical = 12.dp).then(modifier), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MonthTxRow(tx: TransactionDisplay, batch: Boolean, checked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, menuContent: @Composable ((() -> Unit) -> Unit)) {
    var menu by remember { mutableStateOf(false) }
    val isTransfer = tx.type == TransactionType.TRANSFER
    val amountColor = when { isTransfer || tx.type == TransactionType.LOAN -> MaterialTheme.colorScheme.onSurface; tx.amount < 0 -> expenseRed; else -> incomeGreen }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).combinedClickable(onClick = onClick, onLongClick = onLongClick).padding(horizontal = 12.dp, vertical = 10.dp)) {
        if (batch) Checkbox(checked = checked, onCheckedChange = { onClick() })
        CategoryIcon(if (isTransfer || tx.type == TransactionType.LOAN) "🔁" else tx.categoryIcon, size = 36.dp, fontSize = 16.sp, fallbackContainer = SemanticColors.ExpenseRed.copy(alpha = 0.1f))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(if (isTransfer) "转账" else if (tx.type == TransactionType.LOAN) "借贷" else tx.categoryName, style = MaterialTheme.typography.bodyLarge)
            Text(if (isTransfer) "${tx.accountName} → ${tx.toAccountName}" else Formatters.timeHM(tx.tradeDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(Formatters.yuanText(tx.amount), color = amountColor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            if (!isTransfer) Text(tx.accountName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SourceBadge(tx.source)
        }
        Box { Text("⋮", modifier = Modifier.clickable { menu = true }.padding(4.dp)); DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) { menuContent { menu = false } } }
    }
}
private fun sourceFilterLabel(v: Int) = when (v) { 1 -> "手动"; 2 -> "语音"; 3 -> "自动"; else -> "全部" }
@Composable
private fun MonthPickerDialog(current: YearMonth, onPick: (YearMonth) -> Unit) {
    var year by remember { mutableStateOf(current.year) }
    var sel by remember { mutableStateOf(current.monthValue) }
    AlertDialog(onDismissRequest = { }, title = { Text("选择月份") }, text = {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("‹", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.clickable { year-- }.padding(horizontal = 16.dp))
                Text("$year", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
                Text("›", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.clickable { year++ }.padding(horizontal = 16.dp))
            }
            Spacer(Modifier.height(8.dp))
            for (r in 0 until 4) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (c in 1..3) {
                        val m = r * 3 + c
                        Box(Modifier.size(44.dp).background(if (m == sel) accentBlue else Color.Transparent, RoundedCornerShape(10.dp)).clickable { sel = m }, contentAlignment = Alignment.Center) { Text("${m}月", color = if (m == sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
                    }
                }
            }
        }
    }, confirmButton = { Button(onClick = { onPick(YearMonth.of(year, sel)) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("确定") } }, dismissButton = { TextButton(onClick = { }) { Text("取消") } })
}
@Composable
private fun SplitDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var n by remember { mutableStateOf("2") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("拆分交易") }, text = { Column { Text("将这笔交易按金额平均拆分为多笔，各笔时间依次顺延。"); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = n, onValueChange = { v -> n = v.filter { it.isDigit() }.take(2) }, label = { Text("拆分笔数（2-99）") }) } }, confirmButton = { Button(onClick = { n.toIntOrNull()?.let { if (it >= 2) onConfirm(it) } }) { Text("拆分") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}
