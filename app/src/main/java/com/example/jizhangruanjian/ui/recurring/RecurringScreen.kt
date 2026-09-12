package com.example.jizhangruanjian.ui.recurring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.Frequency
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.domain.model.RecurringTemplateDomain
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(onBack: () -> Unit, viewModel: RecurringViewModel = hiltViewModel()) {
    val templates by viewModel.templates.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var addDialog by remember { mutableStateOf(false) }
    var deleteFor by remember { mutableStateOf<RecurringTemplateDomain?>(null) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AppTopBar(title = "周期记账", onBack = onBack, actions = { IconButton(onClick = { addDialog = true }) { Icon(Icons.Filled.Add, contentDescription = "新建模板") } }) }) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = AppSpacing.lg)) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("到账后自动生成交易并按 R7 补跑，账户余额自动联动", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(AppSpacing.md))
                if (templates.isEmpty()) { Text("暂无周期模板，点右上角新建", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                templates.forEach { t ->
                    val acc = accounts.firstOrNull { it.id == t.accountId }?.name ?: "未知账户"
                    val cat = categories.firstOrNull { it.id == t.categoryId }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CategoryIcon(cat?.icon ?: "💰", size = AppSize.iconSize, fontSize = 13.sp)
                                Spacer(Modifier.width(6.dp))
                                Text("${cat?.name ?: "未知分类"} · ${Formatters.yuanText(t.amount)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            }
                            Text("$acc · ${frequencyLabel(t.frequency)}${if (t.interval > 1) "/${t.interval}" else ""} · 下次 ${Formatters.dateMd(t.nextExecuteDate)}" + (t.endDate?.let { " · 截止 ${Formatters.dateMd(it)}" } ?: "") + (if (t.note.isNotBlank()) " · ${t.note}" else ""), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { deleteFor = t }) { Icon(Icons.Filled.Delete, contentDescription = "删除") }
                    }
                }
            }
        }
    }
    if (addDialog) TemplateFormDialog(accounts = accounts, categories = categories, onConfirm = { type, accId, catId, amount, freq, interval, note, endDays -> viewModel.add(type, accId, catId, amount, freq, interval, note, endDays); addDialog = false }, onDismiss = { addDialog = false })
    deleteFor?.let { t -> AlertDialog(onDismissRequest = { deleteFor = null }, confirmButton = { TextButton(onClick = { viewModel.delete(t.id); deleteFor = null }) { Text("删除", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { deleteFor = null }) { Text("取消") } }, title = { Text("删除模板") }, text = { Text("确定删除该周期模板？") }) }
}
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TemplateFormDialog(accounts: List<com.example.jizhangruanjian.domain.model.AccountDomain>, categories: List<com.example.jizhangruanjian.domain.model.CategoryDomain>, onConfirm: (TransactionType, Long, Long, Long, Frequency, Int, String, Int?) -> Unit, onDismiss: () -> Unit) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var accountId by remember { mutableStateOf(0L) }
    var categoryId by remember { mutableStateOf(0L) }
    var amount by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(Frequency.DAILY) }
    var interval by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var endEnabled by remember { mutableStateOf(false) }
    var endDays by remember { mutableStateOf("30") }
    val typeCategories = categories.filter { it.type.name == type.name }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(enabled = (amount.toDoubleOrNull() ?: 0.0) > 0.0 && accountId != 0L && categoryId != 0L, onClick = { onConfirm(type, accountId, categoryId, (amount.toDoubleOrNull()!! * 100).toLong(), frequency, interval.toIntOrNull() ?: 1, note, if (endEnabled) endDays.toIntOrNull() else null) }) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }, title = { Text("新建周期模板") }, text = {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                FilterChip(selected = type == TransactionType.EXPENSE, onClick = { type = TransactionType.EXPENSE; categoryId = 0L }, label = { Text("支出") })
                FilterChip(selected = type == TransactionType.INCOME, onClick = { type = TransactionType.INCOME; categoryId = 0L }, label = { Text("收入") })
            }
            Spacer(Modifier.height(AppSpacing.sm))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("金额(元)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(AppSpacing.sm))
            Text("账户", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                accounts.forEach { a -> FilterChip(selected = accountId == a.id, onClick = { accountId = a.id }, label = { Text(a.name) }) }
            }
            Spacer(Modifier.height(AppSpacing.xs))
            Text("分类", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                typeCategories.forEach { c -> FilterChip(selected = categoryId == c.id, onClick = { categoryId = c.id }, label = { Row(verticalAlignment = Alignment.CenterVertically) { CategoryIcon(c.icon, size = 18.dp, fontSize = 12.sp); Spacer(Modifier.width(AppSpacing.xs)); Text(c.name) } }) }
            }
            Spacer(Modifier.height(AppSpacing.xs))
            Text("频率", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Frequency.entries.forEach { f -> FilterChip(selected = frequency == f, onClick = { frequency = f }, label = { Text(frequencyLabel(f)) }) }
            }
            Spacer(Modifier.height(AppSpacing.sm))
            OutlinedTextField(value = interval, onValueChange = { interval = it }, label = { Text("间隔(每N期)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(AppSpacing.sm))
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(AppSpacing.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("设置结束日期", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = endEnabled, onCheckedChange = { endEnabled = it })
            }
            if (endEnabled) {
                OutlinedTextField(value = endDays, onValueChange = { endDays = it }, label = { Text("从今天起持续天数") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }
    })
}
private fun frequencyLabel(f: Frequency): String = when (f) { Frequency.DAILY -> "每天"; Frequency.WEEKLY -> "每周"; Frequency.MONTHLY -> "每月"; Frequency.YEARLY -> "每年" }