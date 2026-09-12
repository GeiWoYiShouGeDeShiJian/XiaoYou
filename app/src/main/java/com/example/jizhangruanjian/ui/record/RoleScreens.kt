package com.example.jizhangruanjian.ui.record
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.dp
import com.example.jizhangruanjian.data.model.Member
// 角色头像预设：emoji + 背景色
val roleAvatarPresets: List<Pair<String, Long>> = listOf(
    "🧑" to 0xFFE8C9A0, "🧒" to 0xFFF2C14E, "👶" to 0xFFF2A0A0, "👴" to 0xFFB8860B,
    "👵" to 0xFFB5A0C9, "🧓" to 0xFF3F6FB5, "🏠" to 0xFF7FC4E8, "💼" to 0xFF8C8C8C,
    "🛒" to 0xFF9CD08C, "🎓" to 0xFF6C8CE8, "❤️" to 0xFFE88C9C, "⭐" to 0xFFF2D14E
)
private val defaultRoleIcon = "🧑" to 0xFFE8C9A0
// 角色行：头像 + 名称 + 默认标记；多选模式前置勾选框，单选选中时尾部显示勾
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoleMemberRow(name: String, icon: String, color: Long, isDefault: Boolean, checked: Boolean? = null, isSelected: Boolean = false, onClick: () -> Unit, onLongClick: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick).padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (checked != null) {
            Checkbox(checked = checked, onCheckedChange = { onClick() })
            Spacer(Modifier.width(4.dp))
        }
        Box(Modifier.size(40.dp).clip(CircleShape).background(Color(color)), contentAlignment = Alignment.Center) { Text(icon, style = MaterialTheme.typography.titleMedium) }
        Spacer(Modifier.width(16.dp))
        Text(name, style = MaterialTheme.typography.bodyLarge)
        if (isDefault) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("默认", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.weight(1f))
        if (checked == null && isSelected) { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
    }
}
// 图一：角色选择页（底部弹层可拉全屏；单选点选即回，多选勾选实时生效）
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RolePickerScreen(members: List<Member>, selectedId: Long?, selectedMembers: List<Long?> = emptyList(), onPick: (Long?) -> Unit, onClose: () -> Unit, onAddNew: () -> Unit, onManage: () -> Unit, onLivePick: (List<Long?>) -> Unit = {}) {
    var multi by remember { mutableStateOf(false) }
    var multiSel by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) members else members.filter { it.name.contains(query.trim()) }
    val selfChecked = multi && multiSel.contains(-1L)
    val allSelected = multi && multiSel.contains(-1L) && filtered.all { multiSel.contains(it.id) }
    // 把当前勾选集合回传（-1 代表「自己」→ null）
    val pushSel: () -> Unit = { onLivePick(multiSel.toList().map { if (it == -1L) null else it }) }
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
                        Text("角色", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { showSearch = true }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
                        IconButton(onClick = onAddNew) { Icon(Icons.Filled.AddCircle, contentDescription = "新增角色") }
                        IconButton(onClick = onManage) { Icon(Icons.Filled.Settings, contentDescription = "角色管理") }
                    }
                } else {
                    // 搜索栏：点击搜索图标后直接切换（返回箭头直接关闭角色界面）
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(selected = !multi, onClick = { multi = false; multiSel = emptySet() }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)) { Text("单选") }
                    SegmentedButton(selected = multi, onClick = { multi = true; multiSel = selectedMembers.map { it ?: -1L }.toSet() }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)) { Text("多选") }
                }
            }
            if (multi) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).combinedClickable(onClick = { multiSel = if (allSelected) emptySet() else setOf(-1L) + filtered.map { it.id }; pushSel() }, onLongClick = null).padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Checkbox(checked = allSelected, onCheckedChange = { multiSel = if (it) setOf(-1L) + filtered.map { m -> m.id } else emptySet(); pushSel() })
                    Spacer(Modifier.width(4.dp))
                    Text("全选", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.weight(1f))
                    Text("已选 ${multiSel.size}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            LazyColumn(Modifier.weight(1f)) {
                if (query.isBlank()) {
                    item {
                        RoleMemberRow(name = "自己", icon = defaultRoleIcon.first, color = defaultRoleIcon.second, isDefault = true, checked = if (multi) selfChecked else null, isSelected = !multi && selectedId == null, onClick = {
                            if (multi) { val adding = !selfChecked; multiSel = if (adding) multiSel + -1L else multiSel - -1L; pushSel() }
                            else onPick(null)
                        })
                    }
                }
                items(filtered, key = { it.id }) { m ->
                    RoleMemberRow(name = m.name, icon = m.icon, color = m.color, isDefault = false, checked = if (multi) multiSel.contains(m.id) else null, isSelected = !multi && selectedId == m.id, onClick = {
                        if (multi) { val adding = !multiSel.contains(m.id); multiSel = if (adding) multiSel + m.id else multiSel - m.id; pushSel() }
                        else onPick(m.id)
                    })
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onAddNew, onLongClick = null).padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("新增角色", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
// 图二：新增/编辑角色页（头像 + 角色名称 + 保存）
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoleEditScreen(initial: Member?, onSave: (Member) -> Unit, onClose: () -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: defaultRoleIcon.first) }
    var color by remember { mutableStateOf(initial?.color ?: defaultRoleIcon.second) }
    var showAvatars by remember { mutableStateOf(false) }
    val save = {
        if (name.isNotBlank()) {
            onSave(Member(id = initial?.id ?: 0L, name = name.trim(), sortOrder = initial?.sortOrder ?: 0, icon = icon, color = color))
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
            Text(if (initial == null) "新增角色" else "编辑角色", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Box(Modifier.align(Alignment.CenterHorizontally).combinedClickable(onClick = { showAvatars = !showAvatars }, onLongClick = null)) {
                Box(Modifier.size(96.dp).clip(CircleShape).background(Color(color)), contentAlignment = Alignment.Center) { Text(icon, style = MaterialTheme.typography.displaySmall) }
                Box(Modifier.align(Alignment.BottomEnd).size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Settings, contentDescription = "选择头像", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(4.dp))
                }
            }
            if (showAvatars) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    roleAvatarPresets.forEach { (ic, cl) ->
                        Box(Modifier.size(48.dp).clip(CircleShape).background(Color(cl)).border(width = if (ic == icon && cl == color) 3.dp else 0.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape).combinedClickable(onClick = { icon = ic; color = cl }, onLongClick = null), contentAlignment = Alignment.Center) { Text(ic, style = MaterialTheme.typography.titleLarge) }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("角色名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Button(onClick = save, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("保存", style = MaterialTheme.typography.titleMedium) }
        }
    }
}
// 图三：角色管理页（点行编辑，长按删除）
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoleManageScreen(members: List<Member>, onBack: () -> Unit, onAddNew: () -> Unit, onEdit: (Member) -> Unit, onDelete: (Member) -> Unit) {
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var deleteFor by remember { mutableStateOf<Member?>(null) }
    val filtered = if (query.isBlank()) members else members.filter { it.name.contains(query.trim()) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("角色管理") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }, actions = {
            IconButton(onClick = { showSearch = !showSearch; if (!showSearch) query = "" }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
        })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (showSearch) {
                OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("搜索角色") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
            }
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    RoleMemberRow(name = "自己", icon = defaultRoleIcon.first, color = defaultRoleIcon.second, isDefault = true, onClick = {})
                }
                items(filtered, key = { it.id }) { m ->
                    RoleMemberRow(name = m.name, icon = m.icon, color = m.color, isDefault = false, onClick = { onEdit(m) }, onLongClick = { deleteFor = m })
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onAddNew, onLongClick = null).padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("新增角色", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
    if (deleteFor != null) {
        AlertDialog(onDismissRequest = { deleteFor = null }, title = { Text("删除角色") }, text = { Text("确定删除角色「${deleteFor?.name}」？使用该角色的记录将保留。") }, confirmButton = {
            TextButton(onClick = { deleteFor?.let { onDelete(it) }; deleteFor = null }) { Text("删除") }
        }, dismissButton = { TextButton(onClick = { deleteFor = null }) { Text("取消") } })
    }
}
