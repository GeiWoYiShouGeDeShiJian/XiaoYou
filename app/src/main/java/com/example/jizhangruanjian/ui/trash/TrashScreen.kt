package com.example.jizhangruanjian.ui.trash
import com.example.jizhangruanjian.ui.theme.SemanticColors
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.domain.model.TransactionDisplay
private val CARD_BG = Color(0xFFF3F4F6)
private val INCOME_GREEN = SemanticColors.IncomeGreen
private val EXPENSE_RED = SemanticColors.ExpenseRed
private enum class TrashSection(val title: String) {
    LEDGER("已删除账本"), ACCOUNT("已删除账户"), TX("已删除明细");
}
@Composable
fun TrashScreen(onBack: () -> Unit, viewModel: TrashViewModel = hiltViewModel()) {
    val trashLedgers by viewModel.trashLedgers.collectAsState()
    val trashAccounts by viewModel.trashAccounts.collectAsState()
    val trashTx by viewModel.trashTx.collectAsState()
    var section by remember { androidx.compose.runtime.mutableStateOf<TrashSection?>(null) }
    val current = section
    BackHandler(enabled = current != null) { section = null }
    if (current == null) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).statusBarsPadding().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                Spacer(Modifier.width(4.dp))
                Text("回收站", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            TrashEntryCard(Icons.Filled.Description, "账本", trashLedgers.size) { section = TrashSection.LEDGER }
            Spacer(Modifier.height(10.dp))
            TrashEntryCard(Icons.Filled.AccountBalanceWallet, "账户", trashAccounts.size) { section = TrashSection.ACCOUNT }
            Spacer(Modifier.height(10.dp))
            TrashEntryCard(Icons.Filled.ReceiptLong, "明细", trashTx.size) { section = TrashSection.TX }
            Spacer(Modifier.height(20.dp))
            Text("已删除的数据会被存放在回收站中", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9AA0A6), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    } else {
        when (current) {
            TrashSection.LEDGER -> TrashListScreen(title = current.title, onBack = { section = null }, items = trashLedgers.map { TrashRow(it.id, it.name, Formatters.formatTrashTime(it.deletedAt ?: 0L), null, null) }, onRestore = viewModel::restoreLedgers, onDelete = viewModel::deleteLedgersForever)
            TrashSection.ACCOUNT -> TrashListScreen(title = current.title, onBack = { section = null }, items = trashAccounts.map { TrashRow(it.id, it.name, Formatters.formatTrashTime(it.deletedAt ?: 0L), null, null) }, onRestore = viewModel::restoreAccounts, onDelete = viewModel::deleteAccountsForever)
            TrashSection.TX -> TrashListScreen(title = current.title, onBack = { section = null }, items = trashTx.map { tx -> TrashRow(tx.id, tx.note.ifBlank { tx.categoryName }, Formatters.formatTrashTime(tx.tradeDate), trashAmountText(tx), trashColor(tx)) }, onRestore = viewModel::restoreTx, onDelete = viewModel::deleteTxForever)
        }
    }
}
private fun trashAmountText(tx: TransactionDisplay): String = when (tx.type) {
    TransactionType.INCOME -> "+${Formatters.yuanText(tx.amount)}"
    TransactionType.EXPENSE -> "-${Formatters.yuanText(tx.amount)}"
    else -> Formatters.yuanText(tx.amount)
}
private fun trashColor(tx: TransactionDisplay): Color = when (tx.type) {
    TransactionType.INCOME -> INCOME_GREEN
    TransactionType.EXPENSE -> EXPENSE_RED
    else -> Color(0xFF333333)
}
@Composable
private fun TrashEntryCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, count: Int, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CARD_BG).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 18.dp)) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp), tint = Color(0xFF333333))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(count.toString(), style = MaterialTheme.typography.bodyLarge, color = Color(0xFF666666))
    }
}
private data class TrashRow(val id: Long, val title: String, val subtitle: String, val amount: String?, val amountColor: Color?)
@Composable
private fun TrashListScreen(title: String, onBack: () -> Unit, items: List<TrashRow>, onRestore: (List<Long>) -> Unit, onDelete: (List<Long>) -> Unit) {
    val selected = remember { mutableStateListOf<Long>() }
    val allSelected = items.isNotEmpty() && selected.size == items.size
    val hasSelection = selected.isNotEmpty()
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
            Spacer(Modifier.width(4.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items, key = { it.id }) { row ->
                val checked = row.id in selected
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CARD_BG).clickable { if (checked) selected.remove(row.id) else selected.add(row.id) }.padding(horizontal = 8.dp, vertical = 10.dp)) {
                    Checkbox(checked = checked, onCheckedChange = { if (it) selected.add(row.id) else selected.remove(row.id) }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF333333)))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(row.title, style = MaterialTheme.typography.bodyLarge)
                        Text(row.subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9AA0A6))
                    }
                    if (row.amount != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(row.amount, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = row.amountColor ?: Color.Unspecified)
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
            Checkbox(checked = allSelected, onCheckedChange = { if (it) { selected.clear(); items.forEach { r -> selected.add(r.id) } } else selected.clear() }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF333333)))
            Text("全选", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = { onDelete(selected.toList()); selected.clear() }, enabled = hasSelection, shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF666666), disabledContentColor = Color(0xFFBBBBBB))) { Text("彻底删除") }
            Spacer(Modifier.width(12.dp))
            Button(onClick = { onRestore(selected.toList()); selected.clear() }, enabled = hasSelection, shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))) { Text("还原数据") }
        }
    }
}
