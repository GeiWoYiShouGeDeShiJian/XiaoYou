package com.example.jizhangruanjian.ui.record
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import com.example.jizhangruanjian.data.model.Merchant
import com.example.jizhangruanjian.data.model.MerchantGroup
// 商家行：首字圆形图标 + 名称；highlighted 为拖拽中的高亮底色
@Composable
private fun MerchantRow(name: String, icon: String, onClick: () -> Unit, modifier: Modifier = Modifier, highlighted: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth().background(if (highlighted) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp)) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
            Text(icon.ifBlank { name.take(1) }, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.width(16.dp))
        Text(name, style = MaterialTheme.typography.bodyLarge)
    }
}
// 分组侧栏 Tab
@Composable
private fun GroupTab(name: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 12.dp)) {
        Text(name, style = MaterialTheme.typography.bodyMedium, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}
// 分组列表（选择器与管理页共用）：左侧分组 Tabs + 分组入口，右侧商家列表；onMerchantReorder 非 null 时支持长按拖拽排序
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupedMerchantList(modifier: Modifier = Modifier, groups: List<MerchantGroup>, merchants: List<Merchant>, selectedGroupId: Long?, onSelectGroup: (Long?) -> Unit, onAddGroup: () -> Unit, onAddMerchant: () -> Unit, onMerchantClick: (Merchant) -> Unit, onMerchantReorder: ((List<Long>) -> Unit)? = null) {
    val effGroupId = selectedGroupId ?: groups.firstOrNull()?.id
    val groupMerchants = merchants.filter { it.groupId == effGroupId }
    Row(modifier) {
        Column(Modifier.width(110.dp).fillMaxHeight()) {
            groups.forEach { g ->
                GroupTab(name = g.name, selected = g.id == effGroupId, onClick = {
                    if (selectedGroupId != null && selectedGroupId == g.id) onSelectGroup(null) else onSelectGroup(g.id)
                })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onAddGroup).padding(horizontal = 12.dp, vertical = 12.dp)) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("分组", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Column(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant)) {}
        if (onMerchantReorder != null) {
            var order by remember(groupMerchants) { mutableStateOf(groupMerchants) }
            val listState = rememberLazyListState()
            val reorderState = rememberReorderableLazyColumnState(listState) { from, to ->
                order = order.toMutableList().apply { add(to.index, removeAt(from.index)) }
                onMerchantReorder(order.map { it.id })
            }
            LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                items(order, key = { it.id }) { m ->
                    ReorderableItem(reorderState, key = m.id) { dragging ->
                        MerchantRow(name = m.name, icon = m.icon, highlighted = dragging, onClick = { onMerchantClick(m) }, modifier = Modifier.zIndex(if (dragging) 1f else 0f).longPressDraggableHandle())
                    }
                }
                item { AddMerchantRow(onAddMerchant) }
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(groupMerchants, key = { it.id }) { m ->
                    MerchantRow(name = m.name, icon = m.icon, onClick = { onMerchantClick(m) })
                }
                item { AddMerchantRow(onAddMerchant) }
            }
        }
    }
}
// 新增商家入口行
@Composable
private fun AddMerchantRow(onAddMerchant: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onAddMerchant).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Icon(Icons.Filled.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Text("新增商家", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
    }
}
// 新增/编辑分组弹窗
@Composable
private fun MerchantGroupNameDialog(title: String, initial: String?, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var groupName by remember { mutableStateOf(initial ?: "") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("分组名称") }, singleLine = true)
    }, confirmButton = {
        TextButton(onClick = { if (groupName.isNotBlank()) onConfirm(groupName.trim()); onDismiss() }) { Text("保存") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}
// 图一：商家选择页（底部弹层可拉全屏；点商家即选中返回，不使用商家清空）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantPickerScreen(merchants: List<Merchant>, groups: List<MerchantGroup>, onClose: () -> Unit, onPick: (String) -> Unit, onUseNone: () -> Unit, onAddMerchant: () -> Unit, onManage: () -> Unit, onSaveMerchantGroup: (MerchantGroup) -> Unit) {
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showGroupDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val searchFocus = remember { FocusRequester() }
    LaunchedEffect(showSearch) { if (showSearch) searchFocus.requestFocus() }
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, dragHandle = null) {
        Column(Modifier.fillMaxWidth().fillMaxHeight()) {
            Box(Modifier.fillMaxWidth()) {
                if (!showSearch) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                        Spacer(Modifier.width(8.dp))
                        Text("商家", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { showSearch = true }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
                        IconButton(onClick = onAddMerchant) { Icon(Icons.Filled.AddCircle, contentDescription = "新增商家") }
                        IconButton(onClick = onManage) { Icon(Icons.Filled.Settings, contentDescription = "商家管理") }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            BasicTextField(value = query, onValueChange = { query = it }, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), modifier = Modifier.fillMaxWidth().focusRequester(searchFocus))
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        }
                        IconButton(onClick = { if (query.isNotEmpty()) query = "" else { showSearch = false; query = "" } }) { Icon(Icons.Filled.Close, contentDescription = "清除", modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
            Text("不使用商家", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().clickable(onClick = onUseNone).padding(vertical = 12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            GroupedMerchantList(modifier = Modifier.weight(1f), groups = groups, merchants = merchants.filter { query.trim().isEmpty() || it.name.contains(query.trim()) }, selectedGroupId = selectedGroupId, onSelectGroup = { selectedGroupId = it }, onAddGroup = { showGroupDialog = true }, onAddMerchant = onAddMerchant, onMerchantClick = { onPick(it.name) })
        }
    }
    if (showGroupDialog) {
        MerchantGroupNameDialog(title = "新增分组", initial = null, onConfirm = { onSaveMerchantGroup(MerchantGroup(name = it)) }, onDismiss = { showGroupDialog = false })
    }
}
// 图二：新增/编辑商家页（名称 + 所属分组 + 首字图标 + 保存）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantEditScreen(initial: Merchant?, groups: List<MerchantGroup>, onSave: (Merchant) -> Unit, onClose: () -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var groupId by remember { mutableStateOf(initial?.groupId ?: 1L) }
    var showGroupPick by remember { mutableStateOf(false) }
    val groupName = groups.firstOrNull { it.id == groupId }?.name ?: "默认分组"
    val save = {
        if (name.isNotBlank()) {
            onSave(Merchant(id = initial?.id ?: 0L, name = name.trim(), icon = name.trim().take(1), groupId = if (groups.any { it.id == groupId }) groupId else (groups.firstOrNull()?.id ?: 1L), sortOrder = initial?.sortOrder ?: 0))
            onClose()
        }
        Unit
    }
    Scaffold(topBar = {
        TopAppBar(title = {}, navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }, actions = {
            IconButton(onClick = save) { Icon(Icons.Filled.Check, contentDescription = "保存") }
        })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(horizontal = 20.dp)) {
            Text(if (initial == null) "新增商家" else "编辑商家", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("商家名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = { showGroupPick = true }).padding(horizontal = 16.dp, vertical = 16.dp)) {
                Text("所属分组", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                Text("$groupName ›", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp, vertical = 16.dp)) {
                Text("图标", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                Text("首字图标 ›", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = save, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("保存", style = MaterialTheme.typography.titleMedium) }
        }
    }
    if (showGroupPick) {
        AlertDialog(onDismissRequest = { showGroupPick = false }, title = { Text("选择分组") }, text = {
            Column {
                groups.forEach { g ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = { groupId = g.id; showGroupPick = false }).padding(vertical = 8.dp)) {
                        RadioButton(selected = groupId == g.id, onClick = { groupId = g.id; showGroupPick = false })
                        Text(g.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { showGroupPick = false }) { Text("取消") } })
    }
}
// 图三：商家管理页（分组选中后再点可改名，长按商家拖拽排序）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantManageScreen(groups: List<MerchantGroup>, merchants: List<Merchant>, onBack: () -> Unit, onAddMerchant: () -> Unit, onAddGroup: (String) -> Unit, onRenameGroup: (MerchantGroup, String) -> Unit, onEditMerchant: (Merchant) -> Unit, onReorderMerchants: (List<Long>) -> Unit) {
    var showHint by remember { mutableStateOf(true) }
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showAddGroup by remember { mutableStateOf(false) }
    var renameFor by remember { mutableStateOf<MerchantGroup?>(null) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("商家管理") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }, actions = {
            IconButton(onClick = { showSearch = !showSearch; if (!showSearch) query = "" }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
        })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (showSearch) {
                OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("搜索商家") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
            }
            if (showHint) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("选中分组后，再次点击分组名称可编辑分组。长按商家可拖拽排序。", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showHint = false }, modifier = Modifier.size(24.dp)) { Icon(Icons.Filled.Close, contentDescription = "关闭提示", modifier = Modifier.size(16.dp)) }
                }
            }
            Spacer(Modifier.height(8.dp))
            GroupedMerchantList(modifier = Modifier.weight(1f), groups = groups, merchants = merchants.filter { query.isBlank() || it.name.contains(query.trim()) }, selectedGroupId = selectedGroupId, onSelectGroup = { gid ->
                val g = groups.firstOrNull { it.id == gid }
                if (gid != null && selectedGroupId == gid) renameFor = g else selectedGroupId = gid
            }, onAddGroup = { showAddGroup = true }, onAddMerchant = onAddMerchant, onMerchantClick = onEditMerchant, onMerchantReorder = if (query.isBlank()) onReorderMerchants else null)
        }
    }
    if (showAddGroup) {
        MerchantGroupNameDialog(title = "新增分组", initial = null, onConfirm = { onAddGroup(it) }, onDismiss = { showAddGroup = false })
    }
    if (renameFor != null) {
        val renameTarget = renameFor!!
        MerchantGroupNameDialog(title = "编辑分组", initial = renameTarget.name, onConfirm = { onRenameGroup(renameTarget, it) }, onDismiss = { renameFor = null })
    }
}
