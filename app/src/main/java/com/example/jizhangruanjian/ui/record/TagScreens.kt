package com.example.jizhangruanjian.ui.record
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.jizhangruanjian.data.model.Tag
import com.example.jizhangruanjian.data.model.TagGroup
import com.example.jizhangruanjian.ui.components.AppButton
import com.example.jizhangruanjian.ui.components.AppButtonVariant
import com.example.jizhangruanjian.ui.components.AppTextField
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
// 标签颜色预设
private val tagColorPresets = listOf(0xFF33608C, 0xFFD3382F, 0xFF1B8A5A, 0xFF7E57C2, 0xFFB8860B, 0xFFC2559E, 0xFF4A4A6A, 0xFF4C6FBF)
// 标签行：颜色圆点 + 名称；checked 非 null 时为多选模式（选中＝浅色底 + 尾部勾）
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TagRow(name: String, color: Long, checked: Boolean? = null, onClick: () -> Unit, onLongClick: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick).background(if (checked == true) Color(color).copy(alpha = 0.12f) else Color.Transparent).padding(horizontal = AppSpacing.lg, vertical = 10.dp)) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(Color(color)))
        Spacer(Modifier.width(AppSpacing.lg))
        Text(name, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.weight(1f))
        if (checked == true) { Icon(Icons.Filled.Check, contentDescription = null, tint = Color(color)) }
    }
}
// 分组侧栏 Tab
@Composable
private fun GroupTab(name: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent).clickable(onClick = onClick).padding(horizontal = AppSpacing.md, vertical = AppSpacing.md)) {
        Text(name, style = MaterialTheme.typography.bodyMedium, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}
// 分组列表（选择器与管理页共用）：左侧分组 Tabs + 新增标签/分组入口，右侧标签列表
@Composable
private fun GroupedTagList(modifier: Modifier = Modifier, groups: List<TagGroup>, tags: List<Tag>, selectedGroupId: Long?, onSelectGroup: (Long?) -> Unit, onAddGroup: () -> Unit, onAddTag: () -> Unit, tagChecked: ((Tag) -> Boolean?)? = null, onTagClick: (Tag) -> Unit = {}, onTagLongClick: ((Tag) -> Unit)? = null) {
    val effGroupId = selectedGroupId ?: groups.firstOrNull()?.id
    val groupTags = tags.filter { it.groupId == effGroupId }
    Row(modifier) {
        Column(Modifier.width(110.dp).fillMaxHeight()) {
            groups.forEach { g ->
                GroupTab(name = g.name, selected = g.id == effGroupId, onClick = {
                    if (selectedGroupId != null && selectedGroupId == g.id) onSelectGroup(null) else onSelectGroup(g.id)
                })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onAddGroup).padding(horizontal = AppSpacing.md, vertical = AppSpacing.md)) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppSize.iconSize))
                Spacer(Modifier.width(6.dp))
                Text("分组", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Column(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant)) {}
        LazyColumn(Modifier.weight(1f)) {
            items(groupTags, key = { it.id }) { t ->
                TagRow(name = t.name, color = t.color, checked = tagChecked?.invoke(t), onClick = { onTagClick(t) }, onLongClick = onTagLongClick?.let { f -> { f(t) } })
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onAddTag).padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)) {
                    Icon(Icons.Filled.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(AppSpacing.md))
                    Text("新增标签", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
// 新增/编辑分组弹窗
@Composable
private fun GroupNameDialog(title: String, initial: String?, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var groupName by remember { mutableStateOf(initial ?: "") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("分组名称") }, singleLine = true)
    }, confirmButton = {
        TextButton(onClick = { if (groupName.isNotBlank()) onConfirm(groupName.trim()); onDismiss() }) { Text("保存") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}
// 图一：标签选择页（底部弹层可拉全屏；单选点选即回，多选点选实时生效）
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun TagPickerScreen(tags: List<Tag>, groups: List<TagGroup>, selectedIds: List<Long>, onSelectionChange: (List<Long>) -> Unit, onClose: () -> Unit, onAddTag: () -> Unit, onManage: () -> Unit, onSaveTagGroup: (TagGroup) -> Unit, defaultMulti: Boolean = false) {
    var multi by remember { mutableStateOf(defaultMulti) }
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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm)) {
                        IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                        Spacer(Modifier.width(AppSpacing.sm))
                        Text("标签", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { showSearch = true }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
                        IconButton(onClick = onAddTag) { Icon(Icons.Filled.AddCircle, contentDescription = "新增标签") }
                        IconButton(onClick = onManage) { Icon(Icons.Filled.Settings, contentDescription = "标签管理") }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm)) {
                        IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(AppSize.iconSize))
                        Spacer(Modifier.width(AppSpacing.sm))
                        Column(Modifier.weight(1f)) {
                            BasicTextField(value = query, onValueChange = { query = it }, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), modifier = Modifier.fillMaxWidth().focusRequester(searchFocus))
                            Spacer(Modifier.height(AppSpacing.xs))
                            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        }
                        IconButton(onClick = { if (query.isNotEmpty()) query = "" else { showSearch = false; query = "" } }) { Icon(Icons.Filled.Close, contentDescription = "清除", modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(selected = !multi, onClick = { multi = false }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)) { Text("单选") }
                    SegmentedButton(selected = multi, onClick = { multi = true }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)) { Text("多选") }
                }
            }
            // 已选标签区：无选择时仅提示「无标签」，有选择时显示可移除的标签 chip
            if (selectedIds.isEmpty()) {
                Text("无标签", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md))
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)) {
                    selectedIds.mapNotNull { id -> tags.firstOrNull { it.id == id } }.forEach { t ->
                        SelectedTagChip(name = t.name, color = t.color, onRemove = { onSelectionChange(selectedIds - t.id) })
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            GroupedTagList(modifier = Modifier.weight(1f), groups = groups, tags = tags.filter { query.trim().isEmpty() || it.name.contains(query.trim()) }, selectedGroupId = selectedGroupId, onSelectGroup = { selectedGroupId = it }, onAddGroup = { showGroupDialog = true }, onAddTag = onAddTag,
                tagChecked = if (multi) ({ t -> selectedIds.contains(t.id) }) else null,
                onTagClick = { t ->
                    if (multi) onSelectionChange(if (selectedIds.contains(t.id)) selectedIds - t.id else selectedIds + t.id)
                    else { onSelectionChange(listOf(t.id)); onClose() }
                })
        }
    }
    if (showGroupDialog) {
        GroupNameDialog(title = "新增分组", initial = null, onConfirm = { onSaveTagGroup(TagGroup(name = it)) }, onDismiss = { showGroupDialog = false })
    }
}
// 已选标签 chip：标签名（标签色）+ ✕，点击移除
@Composable
private fun SelectedTagChip(name: String, color: Long, onRemove: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(MaterialTheme.shapes.small).border(1.dp, Color(color), MaterialTheme.shapes.small).clickable(onClick = onRemove).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(name, color = Color(color), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Filled.Close, contentDescription = "移除", tint = Color(color), modifier = Modifier.size(14.dp))
    }
}
// 图二：新增/编辑标签页（名称 + 所属分组 + 颜色 + 保存）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagEditScreen(initial: Tag?, groups: List<TagGroup>, onSave: (Tag) -> Unit, onClose: () -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: tagColorPresets.first()) }
    var groupId by remember { mutableStateOf(initial?.groupId ?: 1L) }
    var showGroupPick by remember { mutableStateOf(false) }
    val groupName = groups.firstOrNull { it.id == groupId }?.name ?: "默认分组"
    val save = {
        if (name.isNotBlank()) {
            onSave(Tag(id = initial?.id ?: 0L, name = name.trim(), color = color, groupId = if (groups.any { it.id == groupId }) groupId else (groups.firstOrNull()?.id ?: 1L)))
            onClose()
        }
        Unit
    }
    Scaffold(topBar = {
        AppTopBar(title = "", onBack = onClose, actions = {
            IconButton(onClick = save) { Icon(Icons.Filled.Check, contentDescription = "保存") }
        })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(horizontal = AppSpacing.xl)) {
            Text(if (initial == null) "新增标签" else "编辑标签", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(AppSpacing.xxl))
            AppTextField(value = name, onValueChange = { name = it }, label = "标签名称", singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(AppSpacing.lg))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = { showGroupPick = true }).padding(horizontal = AppSpacing.lg, vertical = AppSpacing.lg)) {
                Text("所属分组", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                Text("$groupName ›", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(AppSpacing.md))
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant).padding(AppSpacing.lg)) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    tagColorPresets.forEach { c ->
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(c)).border(width = if (color == c) 3.dp else 0.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape).clickable(onClick = { color = c }), contentAlignment = Alignment.Center) {
                            if (color == c) Box(Modifier.size(10.dp).clip(CircleShape).background(Color.White))
                        }
                    }
                }
            }
            Spacer(Modifier.height(AppSpacing.xxl))
            AppButton(text = "保存", onClick = save, variant = AppButtonVariant.Primary, modifier = Modifier.fillMaxWidth())
        }
    }
    if (showGroupPick) {
        AlertDialog(onDismissRequest = { showGroupPick = false }, title = { Text("选择分组") }, text = {
            Column {
                groups.forEach { g ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = { groupId = g.id; showGroupPick = false }).padding(vertical = AppSpacing.sm)) {
                        RadioButton(selected = groupId == g.id, onClick = { groupId = g.id; showGroupPick = false })
                        Text(g.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { showGroupPick = false }) { Text("取消") } })
    }
}
// 图三：标签管理页（分组 Tab 选中后再点可改名，长按标签删除）
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TagManageScreen(groups: List<TagGroup>, tags: List<Tag>, onBack: () -> Unit, onAddTag: () -> Unit, onAddGroup: (String) -> Unit, onRenameGroup: (TagGroup, String) -> Unit, onEditTag: (Tag) -> Unit, onDeleteTag: (Long) -> Unit) {
    var showHint by remember { mutableStateOf(true) }
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showAddGroup by remember { mutableStateOf(false) }
    var renameFor by remember { mutableStateOf<TagGroup?>(null) }
    var deleteFor by remember { mutableStateOf<Tag?>(null) }
    val effGroupId = selectedGroupId ?: groups.firstOrNull()?.id
    val filteredTags = tags.filter { it.groupId == effGroupId && (query.isBlank() || it.name.contains(query.trim())) }
    Scaffold(topBar = {
        AppTopBar(title = "标签管理", onBack = onBack, actions = {
            IconButton(onClick = { showSearch = !showSearch; if (!showSearch) query = "" }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
        })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (showSearch) {
                AppTextField(value = query, onValueChange = { query = it }, placeholder = "搜索标签", singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm))
            }
            if (showHint) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppSize.iconSize))
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text("选中分组后，再次点击分组名称可编辑分组。长按标签可删除标签。", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showHint = false }, modifier = Modifier.size(AppSize.iconLarge)) { Icon(Icons.Filled.Close, contentDescription = "关闭提示", modifier = Modifier.size(AppSize.iconSmall)) }
                }
            }
            Spacer(Modifier.height(AppSpacing.sm))
            GroupedTagList(modifier = Modifier.weight(1f), groups = groups, tags = tags, selectedGroupId = selectedGroupId, onSelectGroup = { gid ->
                val g = groups.firstOrNull { it.id == gid }
                if (gid != null && selectedGroupId == gid) renameFor = g else selectedGroupId = gid
            }, onAddGroup = { showAddGroup = true }, onAddTag = onAddTag,
                onTagClick = { onEditTag(it) }, onTagLongClick = { deleteFor = it })
        }
    }
    if (showAddGroup) {
        GroupNameDialog(title = "新增分组", initial = null, onConfirm = { onAddGroup(it) }, onDismiss = { showAddGroup = false })
    }
    if (renameFor != null) {
        val renameTarget = renameFor!!
        GroupNameDialog(title = "编辑分组", initial = renameTarget.name, onConfirm = { onRenameGroup(renameTarget, it) }, onDismiss = { renameFor = null })
    }
    if (deleteFor != null) {
        AlertDialog(onDismissRequest = { deleteFor = null }, title = { Text("删除标签") }, text = { Text("确定删除标签「${deleteFor?.name}」？相关记录将保留，但会移除该标签。") }, confirmButton = {
            TextButton(onClick = { deleteFor?.let { onDeleteTag(it.id) }; deleteFor = null }) { Text("删除") }
        }, dismissButton = { TextButton(onClick = { deleteFor = null }) { Text("取消") } })
    }
}
