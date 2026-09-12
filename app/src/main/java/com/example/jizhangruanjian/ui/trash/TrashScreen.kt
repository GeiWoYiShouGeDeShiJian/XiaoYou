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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.jizhangruanjian.ui.components.AppButton
import com.example.jizhangruanjian.ui.components.AppButtonVariant
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.theme.NeutralColors
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
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
        Scaffold(containerColor = MaterialTheme.colorScheme.surface, topBar = { AppTopBar(title = "回收站", onBack = onBack) }) { pad ->
            Column(modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = AppSpacing.lg)) {
                Spacer(Modifier.height(AppSpacing.lg))
                TrashEntryCard(Icons.Filled.Description, "账本", trashLedgers.size) { section = TrashSection.LEDGER }
                Spacer(Modifier.height(10.dp))
                TrashEntryCard(Icons.Filled.AccountBalanceWallet, "账户", trashAccounts.size) { section = TrashSection.ACCOUNT }
                Spacer(Modifier.height(10.dp))
                TrashEntryCard(Icons.Filled.ReceiptLong, "明细", trashTx.size) { section = TrashSection.TX }
                Spacer(Modifier.height(AppSpacing.xl))
                Text("已删除的数据会被存放在回收站中", style = MaterialTheme.typography.bodySmall, color = NeutralColors.textMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
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
    else -> NeutralColors.textPrimary
}
@Composable
private fun TrashEntryCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, count: Int, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(NeutralColors.surfaceMuted).clickable(onClick = onClick).padding(horizontal = AppSpacing.lg, vertical = 18.dp)) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(AppSize.iconLarge), tint = NeutralColors.textPrimary)
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(count.toString(), style = MaterialTheme.typography.bodyLarge, color = NeutralColors.textSecondary)
    }
}
private data class TrashRow(val id: Long, val title: String, val subtitle: String, val amount: String?, val amountColor: Color?)
@Composable
private fun TrashListScreen(title: String, onBack: () -> Unit, items: List<TrashRow>, onRestore: (List<Long>) -> Unit, onDelete: (List<Long>) -> Unit) {
    val selected = remember { mutableStateListOf<Long>() }
    val allSelected = items.isNotEmpty() && selected.size == items.size
    val hasSelection = selected.isNotEmpty()
    Scaffold(containerColor = MaterialTheme.colorScheme.surface, topBar = { AppTopBar(title = title, onBack = onBack) }) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad)) {
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items, key = { it.id }) { row ->
                val checked = row.id in selected
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(NeutralColors.surfaceMuted).clickable { if (checked) selected.remove(row.id) else selected.add(row.id) }.padding(horizontal = AppSpacing.sm, vertical = 10.dp)) {
                    Checkbox(checked = checked, onCheckedChange = { if (it) selected.add(row.id) else selected.remove(row.id) }, colors = CheckboxDefaults.colors(checkedColor = NeutralColors.textPrimary))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(row.title, style = MaterialTheme.typography.bodyLarge)
                        Text(row.subtitle, style = MaterialTheme.typography.bodySmall, color = NeutralColors.textMuted)
                    }
                    if (row.amount != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(row.amount, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = row.amountColor ?: Color.Unspecified)
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).navigationBarsPadding().padding(horizontal = AppSpacing.lg, vertical = 10.dp)) {
            Checkbox(checked = allSelected, onCheckedChange = { if (it) { selected.clear(); items.forEach { r -> selected.add(r.id) } } else selected.clear() }, colors = CheckboxDefaults.colors(checkedColor = NeutralColors.textPrimary))
            Text("全选", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            AppButton(text = "彻底删除", onClick = { onDelete(selected.toList()); selected.clear() }, enabled = hasSelection, variant = AppButtonVariant.Outlined)
            Spacer(Modifier.width(AppSpacing.md))
            AppButton(text = "还原数据", onClick = { onRestore(selected.toList()); selected.clear() }, enabled = hasSelection, variant = AppButtonVariant.Primary)
        }
    }
}
}
