package com.example.jizhangruanjian.ui.arap
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.domain.model.TransactionDisplay
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArApScreen(onBack: () -> Unit, viewModel: ArApViewModel = hiltViewModel()) {
    val receivables by viewModel.receivables.collectAsStateWithLifecycle()
    val payables by viewModel.payables.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    val list = if (tab == 0) receivables else payables
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(title = { Text("应收应付") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } })
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("应收款（借出）") })
            FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("应付款（借入）") })
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(list, key = { it.id }) { tx -> LoanRow(tx = tx, onSettle = { viewModel.markSettled(tx.id) }) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text("应收总额", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(Formatters.yuanText(viewModel.receivableTotal()), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
            Column { Text("应付总额", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(Formatters.yuanText(viewModel.payableTotal()), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) }
            Column(horizontalAlignment = Alignment.End) { Text("净额", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(Formatters.yuanText(viewModel.receivableTotal() - viewModel.payableTotal()), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary) }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanRow(tx: TransactionDisplay, onSettle: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { onSettle(); true } else false })
    SwipeToDismissBox(state = dismissState, enableDismissFromStartToEnd = false, enableDismissFromEndToStart = true, backgroundContent = {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiaryContainer).padding(end = 24.dp), contentAlignment = Alignment.CenterEnd) { Text("标记已还", color = MaterialTheme.colorScheme.onTertiaryContainer) }
    }) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("👤", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(if (tx.memberName.isBlank()) "对方成员" else tx.memberName)
                Text(Formatters.dateMd(tx.tradeDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(Formatters.yuanText(tx.amount), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
            Text("未还", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}