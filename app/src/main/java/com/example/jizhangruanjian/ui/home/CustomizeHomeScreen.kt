package com.example.jizhangruanjian.ui.home
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.CoverTextColor
import com.example.jizhangruanjian.data.model.HomeConfig
import com.example.jizhangruanjian.data.model.HomeDataOption
import com.example.jizhangruanjian.data.model.HomeSummary
import com.example.jizhangruanjian.data.model.PullDownAction
import com.example.jizhangruanjian.data.model.optionLabel
import com.example.jizhangruanjian.data.model.optionValue
import com.example.jizhangruanjian.data.model.normalizeModuleOrder
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
private val COVER_OPTIONS = listOf("cover_pencils")
private val COUNT_OPTIONS = listOf(3, 4, 5, 10)
@Composable
fun CustomizeHomeScreen(onBack: () -> Unit, viewModel: CustomizeHomeViewModel = hiltViewModel()) {
    val config by viewModel.config.collectAsState()
    val currentDialog by viewModel.currentDialog.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val todayExpense by viewModel.todayExpense.collectAsState()
    val weekExpense by viewModel.weekExpense.collectAsState()
    val todayIncome by viewModel.todayIncome.collectAsState()
    val weekIncome by viewModel.weekIncome.collectAsState()
    val yearIncome by viewModel.yearIncome.collectAsState()
    val yearExpense by viewModel.yearExpense.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Text("自定义首页", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
            PreviewCard(config, summary, todayExpense, weekExpense, todayIncome, weekIncome, yearIncome, yearExpense)
            Spacer(Modifier.height(12.dp))
            SettingRow("数据一", optionLabel(config.dataOne), onClick = { viewModel.showDialog(HomeDialogType.DATA_ONE) })
            SettingRow("数据二", optionLabel(config.dataTwo), onClick = { viewModel.showDialog(HomeDialogType.DATA_TWO) })
            SettingRow("数据三", optionLabel(config.dataThree), onClick = { viewModel.showDialog(HomeDialogType.DATA_THREE) })
            SettingRow("账本封面", onClick = { viewModel.showDialog(HomeDialogType.COVER) }, trailing = {
                val res = coverResId(config.ledgerCover)
                if (res != null) Image(painterResource(id = res), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(width = 40.dp, height = 24.dp).clip(RoundedCornerShape(6.dp)))
                else Box(modifier = Modifier.size(width = 40.dp, height = 24.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primary))
            })
            InlineChoiceRow("封面文字", listOf("浅色", "深色"), if (config.coverTextColor == CoverTextColor.LIGHT) 0 else 1) { i ->
                viewModel.update { it.copy(coverTextColor = if (i == 0) CoverTextColor.LIGHT else CoverTextColor.DARK) }
            }
            Spacer(Modifier.height(12.dp))
            Text("全局设置", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            SettingRow("首页下拉操作", pullDownLabel(config.pullDownAction), onClick = { viewModel.showDialog(HomeDialogType.PULL_DOWN) })
            InlineChoiceRow("最近明细数量", COUNT_OPTIONS.map { "${it}条" }, COUNT_OPTIONS.indexOf(config.recentTransactionCount).coerceAtLeast(0)) { i ->
                viewModel.update { it.copy(recentTransactionCount = COUNT_OPTIONS[i]) }
            }
            SettingRow("显示快捷入口", trailing = { Switch(checked = config.showQuickActions, onCheckedChange = { checked -> viewModel.update { it.copy(showQuickActions = checked) } }) })
            SettingRow("显示预算", trailing = { Switch(checked = config.showBudget, onCheckedChange = { checked -> viewModel.update { it.copy(showBudget = checked) } }) })
            SettingRow("隐藏「更多预算」按钮", subtitle = "隐藏后可通过长按预算进入更多预算界面", trailing = { Switch(checked = config.hideMoreBudgetButton, onCheckedChange = { checked -> viewModel.update { it.copy(hideMoreBudgetButton = checked) } }) })
            SettingRow("点击封面切换账本", subtitle = "开启后点击封面可直接切换账本，「自定义首页」则通过「设置-主题与显示」进入", trailing = { Switch(checked = config.clickCoverToSwitchLedger, onCheckedChange = { checked -> viewModel.update { it.copy(clickCoverToSwitchLedger = checked) } }) })
            Spacer(Modifier.height(12.dp))
            Text("卡片顺序", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            CardOrderList(config, viewModel)
            Text("长按拖动排序", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp))
        }
    }
    currentDialog?.let { type ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = { Text(dialogTitle(type)) },
            text = {
                Column {
                    when (type) {
                        HomeDialogType.DATA_ONE -> HomeDataOptionList(config.dataOne) { opt -> viewModel.update { it.copy(dataOne = opt) }; viewModel.dismissDialog() }
                        HomeDialogType.DATA_TWO -> HomeDataOptionList(config.dataTwo) { opt -> viewModel.update { it.copy(dataTwo = opt) }; viewModel.dismissDialog() }
                        HomeDialogType.DATA_THREE -> HomeDataOptionList(config.dataThree) { opt -> viewModel.update { it.copy(dataThree = opt) }; viewModel.dismissDialog() }
                        HomeDialogType.COVER -> CoverOptionList(config.ledgerCover) { c -> viewModel.update { it.copy(ledgerCover = c) }; viewModel.dismissDialog() }
                        HomeDialogType.PULL_DOWN -> PullDownOptionList(config.pullDownAction) { p -> viewModel.update { it.copy(pullDownAction = p) }; viewModel.dismissDialog() }
                    }
                }
            },
            confirmButton = { TextButton(onClick = viewModel::dismissDialog) { Text("取消") } }
        )
    }
}
@Composable
private fun InlineChoiceRow(title: String, items: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val safeIndex = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(items[safeIndex], style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(start = 2.dp)) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "展开", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp).offset(x = 6.dp, y = 6.dp))
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                items.forEachIndexed { i, label ->
                    DropdownMenuItem(text = { Text(if (i == safeIndex) "$label ✓" else label) }, onClick = { onSelect(i); expanded = false })
                }
            }
        }
    }
}
@Composable
private fun PreviewCard(config: HomeConfig, s: HomeSummary?, todayExpense: Long, weekExpense: Long, todayIncome: Long, weekIncome: Long, yearIncome: Long, yearExpense: Long) {
    val textColor = if (config.coverTextColor == CoverTextColor.LIGHT) Color.White else Color(0xFF1A1A1A)
    val subColor = if (config.coverTextColor == CoverTextColor.LIGHT) Color.White.copy(alpha = 0.85f) else Color(0xFF1A1A1A).copy(alpha = 0.75f)
    val coverRes = coverResId(config.ledgerCover)
    val one = if (s != null) optionValue(config.dataOne, s, todayExpense, weekExpense, todayIncome, weekIncome, yearIncome, yearExpense) else 0L
    val two = if (s != null) optionValue(config.dataTwo, s, todayExpense, weekExpense, todayIncome, weekIncome, yearIncome, yearExpense) else 0L
    val three = if (s != null) optionValue(config.dataThree, s, todayExpense, weekExpense, todayIncome, weekIncome, yearIncome, yearExpense) else 0L
    Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp))) {
        if (coverRes != null) {
            Image(painter = painterResource(id = coverRes), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary))
        }
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(optionLabel(config.dataOne), style = MaterialTheme.typography.bodyMedium, color = subColor)
            Spacer(Modifier.height(4.dp))
            Text(Formatters.yuanText(one), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("${optionLabel(config.dataTwo)} ${Formatters.yuanText(two)}", style = MaterialTheme.typography.bodyMedium, color = subColor, modifier = Modifier.weight(1f))
                Text("${optionLabel(config.dataThree)} ${Formatters.yuanText(three)}", style = MaterialTheme.typography.bodyMedium, color = subColor)
            }
        }
    }
}
@Composable
private fun CardOrderList(config: HomeConfig, viewModel: CustomizeHomeViewModel) {
    val order = normalizeModuleOrder(config.moduleOrder)
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyColumnState(listState) { from, to ->
        viewModel.update { c ->
            val base = normalizeModuleOrder(c.moduleOrder).toMutableList()
            base.add(to.index, base.removeAt(from.index))
            c.copy(moduleOrder = base)
        }
    }
    LazyColumn(state = listState, modifier = Modifier.height((order.size * 62 - 10).dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(order, key = { it }) { module ->
            ReorderableItem(reorderState, key = module) { isDragging ->
                val enabled = moduleEnabled(config, module)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(52.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDragging || enabled) 1f else 0.5f)).clip(RoundedCornerShape(14.dp)).padding(horizontal = 16.dp).zIndex(if (isDragging) 1f else 0f).longPressDraggableHandle()) {
                    Text(moduleLabel(module), style = MaterialTheme.typography.bodyLarge, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.Menu, contentDescription = "拖动手柄", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
@Composable
private fun SettingRow(title: String, value: String? = null, subtitle: String? = null, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (value != null) Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        trailing?.invoke()
        if (onClick != null) Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
private fun dialogTitle(type: HomeDialogType): String = when (type) {
    HomeDialogType.DATA_ONE -> "数据一"
    HomeDialogType.DATA_TWO -> "数据二"
    HomeDialogType.DATA_THREE -> "数据三"
    HomeDialogType.COVER -> "账本封面"
    HomeDialogType.PULL_DOWN -> "首页下拉操作"
}
@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}
@Composable
private fun HomeDataOptionList(current: HomeDataOption, onSelect: (HomeDataOption) -> Unit) {
    HomeDataOption.entries.forEach { opt -> OptionRow(optionLabel(opt), opt == current) { onSelect(opt) } }
}
@Composable
private fun CoverOptionList(current: String, onSelect: (String) -> Unit) {
    COVER_OPTIONS.forEach { c ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onSelect(c) }.padding(vertical = 8.dp)) {
            val res = coverResId(c)
            if (res != null) Image(painter = painterResource(id = res), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp, 24.dp).clip(RoundedCornerShape(4.dp)))
            else Box(Modifier.size(40.dp, 24.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary))
            Spacer(Modifier.width(12.dp))
            Text(coverLabel(c), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (c == current) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
@Composable
private fun PullDownOptionList(current: PullDownAction, onSelect: (PullDownAction) -> Unit) {
    PullDownAction.entries.forEach { p -> OptionRow(pullDownLabel(p), p == current) { onSelect(p) } }
}
private fun pullDownLabel(action: PullDownAction): String = when (action) {
    PullDownAction.REFRESH -> "刷新界面"
    PullDownAction.QUICK_RECORD -> "快速记账"
    PullDownAction.SEARCH -> "搜索"
    PullDownAction.NONE -> "无"
}
private fun coverResId(cover: String): Int? = when (cover) {
    "cover_pencils" -> com.example.jizhangruanjian.R.drawable.cover_pencils
    else -> null
}
private fun coverLabel(cover: String): String = when (cover) {
    "cover_pencils" -> "彩色铅笔"
    else -> cover
}
private fun moduleLabel(module: String): String = when (module) {
    "today_stats" -> "今日统计"
    "data_overview" -> "数据概览"
    "category_ratio" -> "支出分类占比"
    "budget" -> "预算"
    "quick_actions" -> "快捷入口"
    "recent_transactions" -> "最近交易"
    else -> module
}
private fun moduleEnabled(config: HomeConfig, module: String): Boolean = when (module) {
    "today_stats" -> true
    "category_ratio" -> config.showCategoryRatio
    "budget" -> config.showBudget
    "quick_actions" -> config.showQuickActions
    "recent_transactions" -> config.showRecentTransactions
    else -> true
}