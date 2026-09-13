package com.example.jizhangruanjian.ui.budget
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.foundation.layout.width
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.domain.model.BudgetState
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.ui.components.AnimatedMoney
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.components.MoneyBookCard
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.components.SectionTitle
import com.example.jizhangruanjian.ui.theme.AppSpacing
import java.util.Locale
@Composable
fun BudgetScreen(onBack: () -> Unit, onOpenDetail: (Long?, String) -> Unit, viewModel: BudgetViewModel = hiltViewModel()) {
    val states by viewModel.states.collectAsState()
    val total by viewModel.total.collectAsState()
    val categories by viewModel.expenseCategories.collectAsState()
    var addDialog by remember { mutableStateOf(false) }
    var editFor by remember { mutableStateOf<BudgetState?>(null) }
    var deleteFor by remember { mutableStateOf<BudgetState?>(null) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AppTopBar(title = "预算", onBack = onBack, actions = { IconButton(onClick = { addDialog = true }) { Icon(Icons.Filled.Add, contentDescription = "新建预算") } }) }) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = AppSpacing.lg)) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            total?.let { TotalBudgetCard(it, categories, onClick = { onOpenDetail(null, "总预算") }) }
            Spacer(Modifier.height(AppSpacing.md))
            Text("分类预算", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(AppSpacing.sm))
            MoneyBookCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.lg)) {
                    val perCategory = states.filter { !it.isTotal }
                    if (perCategory.isEmpty()) {
                        Text("暂无分类预算，点右上角新建", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    perCategory.forEach { s ->
                        BudgetRow(s, categories, onClick = { onOpenDetail(s.budget.categoryId, categories.firstOrNull { it.id == s.budget.categoryId }?.name ?: "分类预算") }, onEdit = { editFor = s }, onDelete = { deleteFor = s })
                    }
                }
            }
        }
    }
    if (addDialog) BudgetFormDialog(categories = categories, onConfirm = { cid, amount, rollover -> viewModel.add(cid, amount, rollover); addDialog = false }, onDismiss = { addDialog = false })
    editFor?.let { s -> AmountDialog("修改预算", Formatters.yuanText(s.budget.amount), onConfirm = { viewModel.update(s.budget.id, it); editFor = null }, onDismiss = { editFor = null }) }
    deleteFor?.let { s -> AlertDialog(onDismissRequest = { deleteFor = null }, confirmButton = { TextButton(onClick = { viewModel.delete(s.budget.id); deleteFor = null }) { Text("删除", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { deleteFor = null }) { Text("取消") } }, title = { Text("删除预算") }, text = { Text("确定删除该预算？") }) }
}
}
@Composable
private fun TotalBudgetCard(s: BudgetState, categories: List<CategoryDomain>, onClick: () -> Unit) {
    MoneyBookCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            SectionTitle("总预算（${monthLabel()}）")
        Spacer(Modifier.height(AppSpacing.sm))
        // R6 详情：本月预算 + 上月结余 = 本月可用
        val prevSurplus = s.budget.amount - s.prevSpent
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            BudgetChip("本月预算", Formatters.yuanText(s.budget.amount))
            Text("+", color = MaterialTheme.colorScheme.onSurfaceVariant)
            BudgetChip("上月结余", Formatters.yuanText(prevSurplus))
            Text("=", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("本月可用", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); AnimatedMoney(amount = s.available, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }
        }
        Spacer(Modifier.height(AppSpacing.md))
        val budgetProgress by animateFloatAsState(targetValue = (s.usagePercent / 100f).coerceIn(0f, 1f), label = "budgetProgress")
        LinearProgressIndicator(progress = { budgetProgress }, color = if (s.over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(AppSpacing.sm))
        Row {
            Text("已用 ${Formatters.yuanText(s.spent)}", style = MaterialTheme.typography.bodyMedium, color = if (s.over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            Text(if (s.over) "已超支 ${Formatters.yuanText(-s.remaining)}" else "剩余 ${Formatters.yuanText(s.remaining)}", style = MaterialTheme.typography.bodyMedium, color = if (s.over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
            }
    }
}
@Composable
private fun BudgetChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
@Composable
private fun BudgetRow(s: BudgetState, categories: List<CategoryDomain>, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val cat = categories.firstOrNull { it.id == s.budget.categoryId }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = AppSpacing.sm)) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(cat?.icon ?: "📁", size = 22.dp, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(cat?.name ?: "未分类", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(String.format(Locale.CHINA, "%.0f%%", s.usagePercent), style = MaterialTheme.typography.bodySmall, color = if (s.over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val itemProgress by animateFloatAsState(targetValue = (s.usagePercent / 100f).coerceIn(0f, 1f), label = "itemProgress")
            LinearProgressIndicator(progress = { itemProgress }, color = if (s.over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth().height(AppSpacing.sm))
            Spacer(Modifier.height(AppSpacing.xs))
            Text("可用 ${Formatters.yuanText(s.available)} · 已用 ${Formatters.yuanText(s.spent)}" + if (s.over) "（超支）" else "", style = MaterialTheme.typography.bodySmall, color = if (s.over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "编辑") }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "删除") }
    }
}
@Composable
private fun BudgetFormDialog(categories: List<CategoryDomain>, onConfirm: (Long?, Long, Boolean) -> Unit, onDismiss: () -> Unit) {
    var isTotal by remember { mutableStateOf(true) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var rollover by remember { mutableStateOf(true) }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(enabled = (amount.toDoubleOrNull() ?: 0.0) > 0.0, onClick = { onConfirm(if (isTotal) null else categoryId, (amount.toDoubleOrNull()!! * 100).toLong(), rollover) }) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }, title = { Text("新建预算") }, text = {
        Column(modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                FilterChip(selected = isTotal, onClick = { isTotal = true; categoryId = null }, label = { Text("总预算") })
                FilterChip(selected = !isTotal, onClick = { isTotal = false }, label = { Text("分类预算") })
            }
            if (!isTotal) {
                Spacer(Modifier.height(AppSpacing.sm))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    categories.forEach { c -> FilterChip(selected = categoryId == c.id, onClick = { categoryId = c.id }, label = { Row(verticalAlignment = Alignment.CenterVertically) { CategoryIcon(c.icon, size = 18.dp, fontSize = 12.sp); Spacer(Modifier.width(AppSpacing.xs)); Text(c.name) } }) }
                }
            }
            Spacer(Modifier.height(AppSpacing.md))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("预算金额(元)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(AppSpacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("结转上月结余", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = rollover, onCheckedChange = { rollover = it })
            }
        }
    })
}
@Composable
private fun AmountDialog(title: String, initial: String, onConfirm: (Long) -> Unit, onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(enabled = amount.toDoubleOrNull()?.let { it > 0 } == true, onClick = { onConfirm((amount.toDoubleOrNull()!! * 100).toLong()) }) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }, title = { Text(title) }, text = { OutlinedTextField(value = amount, onValueChange = { amount = it }, singleLine = true) })
}
private fun monthLabel(): String = String.format(Locale.CHINA, "%d月", java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1)