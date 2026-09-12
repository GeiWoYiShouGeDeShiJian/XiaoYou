package com.example.jizhangruanjian.ui.reimbursement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.jizhangruanjian.data.model.ReimbursementStatus
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.components.grayscale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReimbursementScreen(onBack: () -> Unit, viewModel: ReimbursementViewModel = hiltViewModel()) {
    val list by viewModel.filtered.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(ReimbursementStatus.REIMBURSABLE) }
    val tabs = listOf(ReimbursementStatus.REIMBURSABLE to "待报销", ReimbursementStatus.REIMBURSED to "已报销")
    val sel = tabs.indexOfFirst { it.first == tab }.coerceAtLeast(0)
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(title = { Text("报销管理") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } })
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        TabRow(selectedTabIndex = sel, containerColor = MaterialTheme.colorScheme.background) {
            tabs.forEachIndexed { i, (st, label) ->
                Tab(selected = sel == i, onClick = { tab = st; viewModel.filterByStatus(st) }, text = { Text(label, color = if (sel == i) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (sel == i) FontWeight.Bold else FontWeight.Normal) })
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (list.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 120.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (tab == ReimbursementStatus.REIMBURSABLE) "当前账本没有待报销的数据" else "当前账本没有已报销的数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(list, key = { it.id }) { tx ->
                ReimbursementRow(tx = tx, reimbursed = tab == ReimbursementStatus.REIMBURSED, onReimburse = { viewModel.markReimbursed(tx.id) }, onCancel = { viewModel.cancelReimbursed(tx.id) })
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text("可报销总额", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(Formatters.yuanText(viewModel.reimbursableTotal()), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
            Column(horizontalAlignment = Alignment.End) { Text("已报销总额", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(Formatters.yuanText(viewModel.reimbursedTotal()), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary) }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReimbursementRow(tx: TransactionDisplay, reimbursed: Boolean, onReimburse: () -> Unit, onCancel: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it == SwipeToDismissBoxValue.EndToStart) { if (reimbursed) onCancel() else onReimburse(); true } else false
    })
    SwipeToDismissBox(state = dismissState, enableDismissFromStartToEnd = false, enableDismissFromEndToStart = true, backgroundContent = {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer).padding(end = 24.dp), contentAlignment = Alignment.CenterEnd) { Text(if (reimbursed) "取消报销" else "标记已报销", color = MaterialTheme.colorScheme.onPrimaryContainer) }
    }) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).grayscale().padding(horizontal = 16.dp, vertical = 12.dp)) {
                CategoryIcon(tx.categoryIcon, size = 32.dp, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(tx.categoryName)
                    Text(Formatters.dateMd(tx.tradeDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(Formatters.yuanText(tx.amount), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(end = 8.dp))
                Text(statusLabel(tx.reimbursementStatus), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
    }
}
private fun statusLabel(s: ReimbursementStatus?): String = when (s) {
    ReimbursementStatus.REIMBURSABLE -> "待报销"
    ReimbursementStatus.REIMBURSED -> "已报销"
    else -> "非报销"
}
