package com.example.jizhangruanjian.ui.account
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.domain.model.LedgerDomain
@Composable
fun LedgerScreen(onBack: () -> Unit, viewModel: LedgerViewModel = hiltViewModel()) {
    val ledgers by viewModel.ledgers.collectAsState()
    val currentId by viewModel.currentId.collectAsState()
    val message by viewModel.message.collectAsState()
    var addDialog by remember { mutableStateOf(false) }
    var editFor by remember { mutableStateOf<LedgerDomain?>(null) }
    var deleteFor by remember { mutableStateOf<LedgerDomain?>(null) }
    LaunchedEffect(message) { if (message != null) viewModel.clearMessage() }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
            Text("账本管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { addDialog = true }) { Icon(Icons.Filled.Add, contentDescription = "新建账本") }
        }
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            ledgers.forEach { l ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(if (l.id == currentId) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent).clickable { viewModel.switch(l.id) }.padding(horizontal = 12.dp, vertical = 12.dp)) {
                    Text(l.icon, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(l.color), CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(l.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            if (l.id == currentId) { Spacer(Modifier.width(6.dp)); Text("当前", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                            if (l.isDefault) { Spacer(Modifier.width(6.dp)); Text("默认", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        Text("点击切换账本", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!l.isDefault) {
                        IconButton(onClick = { viewModel.setDefault(l.id) }) { Icon(Icons.Filled.Star, contentDescription = "设为默认", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    IconButton(onClick = { editFor = l }) { Icon(Icons.Filled.Edit, contentDescription = "编辑") }
                    IconButton(onClick = { deleteFor = l }) { Icon(Icons.Filled.Delete, contentDescription = "删除") }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
    message?.let { AlertDialog(onDismissRequest = {}, confirmButton = { TextButton(onClick = viewModel::clearMessage) { Text("确定") } }, title = { Text("提示") }, text = { Text(it) }) }
    if (addDialog) LedgerFormDialog(onConfirm = { name, color, icon -> viewModel.create(name, "cover_pencils", false, "CNY", false); addDialog = false }, onDismiss = { addDialog = false })
    editFor?.let { l -> LedgerEditDialog(initial = l, onConfirm = { name, color, icon -> viewModel.updateAppearance(l.id, name, color, icon); editFor = null }, onDismiss = { editFor = null }) }
    deleteFor?.let { l -> AlertDialog(onDismissRequest = { deleteFor = null }, confirmButton = { TextButton(onClick = { viewModel.delete(l.id); deleteFor = null }) { Text("删除", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { deleteFor = null }) { Text("取消") } }, title = { Text("删除账本") }, text = { Text("确定删除账本「${l.name}」？此操作会移除账本关联。") }) }
}
@Composable
private fun LedgerPickeField(name: String, color: Int, icon: String, onName: (String) -> Unit, onColor: (Int) -> Unit, onIcon: (String) -> Unit) {
    OutlinedTextField(value = name, onValueChange = onName, label = { Text("账本名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    Text("图标", style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ICON_OPTS.forEach { ic ->
            Box(modifier = Modifier.size(36.dp).background(if (ic == icon) Color(color) else MaterialTheme.colorScheme.surfaceVariant, CircleShape).clickable { onIcon(ic) }, contentAlignment = Alignment.Center) {
                Text(ic, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    Text("颜色", style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        palette.forEach { c -> Box(modifier = Modifier.size(32.dp).background(Color(c), CircleShape).clickable { onColor(c) }) }
    }
}
@Composable
private fun LedgerFormDialog(onConfirm: (String, Int, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(0xFF2E7D32.toInt()) }
    var icon by remember { mutableStateOf(ICON_OPTS.first()) }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name.trim(), color, icon) }) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }, title = { Text("新建账本") }, text = { Column { LedgerPickeField(name, color, icon, { name = it }, { color = it }, { icon = it }) } })
}
@Composable
private fun LedgerEditDialog(initial: LedgerDomain, onConfirm: (String, Int, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var color by remember { mutableStateOf(initial.color) }
    var icon by remember { mutableStateOf(initial.icon) }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name.trim(), color, icon) }) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }, title = { Text("编辑账本") }, text = { Column { LedgerPickeField(name, color, icon, { name = it }, { color = it }, { icon = it }) } })
}
private val palette = listOf(0xFF2E7D32.toInt(), 0xFF0D47A1.toInt(), 0xFF7B1FA2.toInt(), 0xFFD32F2F.toInt(), 0xFFF57C00.toInt(), 0xFF455A64.toInt())
private val ICON_OPTS = listOf("📖", "🏦", "💼", "🛍️", "🏠", "✈️", "💰", "🎯")