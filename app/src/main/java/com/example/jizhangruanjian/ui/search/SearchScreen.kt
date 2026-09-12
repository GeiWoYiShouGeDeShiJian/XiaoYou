package com.example.jizhangruanjian.ui.search
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.R
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.example.jizhangruanjian.ui.search.SearchViewModel.TagMode
import com.example.jizhangruanjian.ui.components.AppButton
import com.example.jizhangruanjian.ui.components.AppButtonVariant
import com.example.jizhangruanjian.ui.components.AppTextField
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
import java.time.Instant
import java.time.ZoneId
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBack: () -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val results by viewModel.results.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val minExpr by viewModel.minExpr.collectAsState()
    val maxExpr by viewModel.maxExpr.collectAsState()
    val note by viewModel.note.collectAsState()
    val selCategories by viewModel.selectedCategories.collectAsState()
    val selAccounts by viewModel.selectedAccounts.collectAsState()
    val selTags by viewModel.selectedTags.collectAsState()
    val type by viewModel.type.collectAsState()
    val tagMode by viewModel.tagMode.collectAsState()
    val from by viewModel.from.collectAsState()
    val to by viewModel.to.collectAsState()
    var pick by remember { mutableStateOf<Boolean?>(null) } // true=from, false=to
    var deleteFor by remember { mutableStateOf<TransactionDisplay?>(null) }
    var showFilter by remember { mutableStateOf(false) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AppTopBar(title = "搜索", onBack = onBack, actions = { IconButton(onClick = { showFilter = true }) { Icon(painterResource(R.drawable.ic_filter_funnel), contentDescription = "高级筛选") } }) }) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner)) {
            AppTextField(value = note, onValueChange = { viewModel.note.value = it }, placeholder = "请输入关键字", singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { viewModel.refresh() }), shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("搜索中…", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    results.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("可输入金额、类别、标签、商家和备注", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Text("点击右上角“漏斗”可进行高级筛选", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                    else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.lg)) {
                        items(results) { tx ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) {
                                CategoryIcon(tx.categoryIcon, size = AppSize.iconXLarge, fontSize = 18.sp)
                                Spacer(Modifier.width(AppSpacing.sm))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.categoryName, style = MaterialTheme.typography.bodyLarge)
                                    Text("${tx.accountName} · ${Formatters.dateYmdTime(tx.tradeDate)}${if (tx.note.isNotBlank()) " · ${tx.note}" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(Formatters.yuanText(tx.amount), fontWeight = FontWeight.Medium)
                                if (tx.images.isNotEmpty()) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Filled.Image, contentDescription = "含图片", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(AppSize.iconSize))
                                }
                                IconButton(onClick = { deleteFor = tx }) { Icon(Icons.Filled.Delete, contentDescription = "删除") }
                            }
                        }
                        if (!loading && results.isNotEmpty() && hasMore) {
                            item { AppButton(text = "加载更多", onClick = { viewModel.loadMore() }, variant = AppButtonVariant.Text, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) }
                        }
                    }
                }
            }
        }
    }
    if (showFilter) {
        Dialog(onDismissRequest = { showFilter = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).statusBarsPadding().padding(horizontal = AppSpacing.lg)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { showFilter = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                    Text("高级筛选", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.minExpr.value = ""; viewModel.maxExpr.value = ""; viewModel.from.value = null; viewModel.to.value = null; viewModel.type.value = null; viewModel.selectedCategories.value = emptySet(); viewModel.selectedAccounts.value = emptySet(); viewModel.selectedTags.value = emptySet(); viewModel.setTagMode(TagMode.OR) }) { Text("清除") }
                    TextButton(onClick = { viewModel.refresh(); showFilter = false }) { Text("确定", color = MaterialTheme.colorScheme.primary) }
                }
                Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        OutlinedTextField(value = minExpr, onValueChange = { viewModel.minExpr.value = it }, label = { Text("最低金额") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = maxExpr, onValueChange = { viewModel.maxExpr.value = it }, label = { Text("最高金额") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        FilterChip(selected = type == null, onClick = { viewModel.setType(null) }, label = { Text("全部") })
                        FilterChip(selected = type == TransactionType.EXPENSE, onClick = { viewModel.setType(TransactionType.EXPENSE) }, label = { Text("支出") })
                        FilterChip(selected = type == TransactionType.INCOME, onClick = { viewModel.setType(TransactionType.INCOME) }, label = { Text("收入") })
                        FilterChip(selected = type == TransactionType.TRANSFER, onClick = { viewModel.setType(TransactionType.TRANSFER) }, label = { Text("转账") })
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    Text("分类", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        categories.forEach { c -> FilterChip(selected = c.id in selCategories, onClick = { viewModel.toggleCategory(c.id) }, label = { Row(verticalAlignment = Alignment.CenterVertically) { CategoryIcon(c.icon, size = 18.dp, fontSize = 12.sp); Spacer(Modifier.width(AppSpacing.xs)); Text(c.name) } }) }
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    Text("账户", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        accounts.forEach { a -> FilterChip(selected = a.id in selAccounts, onClick = { viewModel.toggleAccount(a.id) }, label = { Text(a.name) }) }
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("标签", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        FilterChip(selected = tagMode == TagMode.OR, onClick = { viewModel.setTagMode(TagMode.OR) }, label = { Text("任一") })
                        Spacer(Modifier.width(AppSpacing.sm))
                        FilterChip(selected = tagMode == TagMode.AND, onClick = { viewModel.setTagMode(TagMode.AND) }, label = { Text("全部") })
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        tags.forEach { t -> FilterChip(selected = t.id in selTags, onClick = { viewModel.toggleTag(t.id) }, label = { Text(t.name) }) }
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        TextButton(onClick = { pick = true }) { Text(if (from != null) "起始 ${Formatters.dateMd(from!!)}" else "起始日期") }
                        TextButton(onClick = { pick = false }) { Text(if (to != null) "结束 ${Formatters.dateMd(to!!)}" else "结束日期") }
                    }
                    Spacer(Modifier.height(AppSpacing.lg))
                }
            }
        }
    }
    if (pick != null) {
        if (pick == true) FromDatePicker(initial = from ?: System.currentTimeMillis(), onPick = { viewModel.from.value = it; pick = null }, onDismiss = { pick = null })
        else ToDatePicker(initial = to ?: System.currentTimeMillis(), onPick = { viewModel.to.value = it; pick = null }, onDismiss = { pick = null })
    }
    deleteFor?.let { tx ->
        AlertDialog(onDismissRequest = { deleteFor = null }, confirmButton = { TextButton(onClick = { viewModel.softDelete(tx.id); deleteFor = null }) { Text("删除", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { deleteFor = null }) { Text("取消") } }, title = { Text("删除到回收站") }, text = { Text("确定删除该交易？可在回收站恢复。") })
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FromDatePicker(initial: Long, onPick: (Long) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial)
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { onPick(utcToLocalDay(it)) } }) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }) { DatePicker(state = state) }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToDatePicker(initial: Long, onPick: (Long) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial)
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { onPick(utcToLocalDay(it)) } }) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }) { DatePicker(state = state) }
}
private fun utcToLocalDay(ms: Long): Long {
    val ld = Instant.ofEpochMilli(ms).atZone(ZoneId.of("UTC")).toLocalDate()
    return ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}