package com.example.jizhangruanjian.ui.record
import com.example.jizhangruanjian.ui.components.AppCard
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.theme.AppSpacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import com.example.jizhangruanjian.core.database.TransactionDao
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.RecordPrefsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
// 记账偏好 ViewModel：共享 RecordPrefsStore
@HiltViewModel
class RecordPrefsViewModel @Inject constructor(private val store: RecordPrefsStore) : ViewModel() {
    val prefs = store.prefs
    fun set(key: String, value: String) = store.set(key, value)
}
// 快捷小类使用频率统计（当前账本内按分类计数）
@HiltViewModel
class QuickCatViewModel @Inject constructor(private val dao: TransactionDao, holder: CurrentLedgerHolder) : ViewModel() {
    private val ledgerId = holder.id
    suspend fun counts(type: String): Map<Long, Long> = if (ledgerId.value == 0L) emptyMap() else dao.countByCategory(ledgerId.value, type).associate { it.categoryId to it.total }
}
// 键盘按键大小映射
fun keypadKeySize(prefs: Map<String, String>): Dp = when (prefs["rp_key_size"]) {
    "LARGE" -> 60.dp
    "SMALL" -> 44.dp
    else -> 52.dp
}
// 图片压缩质量映射
fun imageQuality(prefs: Map<String, String>): Int = if (prefs["rp_img_quality"] == "NORMAL") 60 else 90
private val typeLabels = linkedMapOf("EXPENSE" to "支出", "INCOME" to "收入", "TRANSFER" to "转账", "LOAN" to "借贷")
private val row1Items = linkedMapOf("date" to "日期", "time" to "时间", "member" to "角色", "tag" to "标签", "merchant" to "商家", "discount" to "优惠", "fee" to "手续费/补贴", "due" to "还款日", "currency" to "币种")
private val row2Items = linkedMapOf("pay" to "应收应付", "reimburse" to "报销", "refund" to "退款", "budget" to "计入收支与预算")
private val resetItems = linkedMapOf("amount" to "金额", "category" to "分类", "note" to "备注", "tag" to "标签", "member" to "角色", "merchant" to "商家", "discount" to "优惠", "account" to "账户", "images" to "图片")
private fun normalizeOrder(pref: String, items: Map<String, String>): List<String> {
    val parsed = pref.split(",").filter { it.isNotBlank() && items.containsKey(it) }
    return parsed + items.keys.filter { it !in parsed }
}
// 偏好设置页（记账页右上角入口），与备注设置页同结构：Scaffold+大标题+分区
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPreferenceScreen(onBack: () -> Unit, onOpenNoteSettings: () -> Unit = {}, canRememberKeyword: Boolean = false, onRememberKeyword: () -> Unit = {}) {
    val vm: RecordPrefsViewModel = hiltViewModel()
    val prefs by vm.prefs.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf("") }
    var chipsPage by remember { mutableStateOf(false) }
    if (chipsPage) {
        BackHandler { chipsPage = false }
        RecordChipsOrderScreen(vm = vm, prefs = prefs, onBack = { chipsPage = false })
        return
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AppTopBar(title = "偏好设置", onBack = onBack) }) { pad ->
        Column(Modifier.padding(pad).fillMaxWidth().verticalScroll(rememberScrollState())) {
            Text("记账", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm))
            NavRow("初始选中类型", value = typeLabels[prefs["rp_init_type"] ?: "EXPENSE"] ?: "支出") { dialog = "type" }
            NavRow("常用类别数量", value = prefs["rp_fav_count"] ?: "8") { dialog = "fav" }
            NavRow("备注设置", subtitle = "历史备注、常用备注设置") { onOpenNoteSettings() }
            NavRow("记账选项", subtitle = "设置底部两排选项的显示与顺序") { chipsPage = true }
            NavRow("再记重置项", subtitle = "使用「再记」时需要重置的数据") { dialog = "reset" }
            SwitchRow("复制保留时间", "「复制」明细时，日期与时间保持不变", prefs["rp_copy_keep_time"] == "1") { vm.set("rp_copy_keep_time", if (it) "1" else "0") }
            SwitchRow("标签默认多选", "添加标签时默认使用「多选」模式", prefs["rp_tag_multi"] == "1") { vm.set("rp_tag_multi", if (it) "1" else "0") }
            NavRow("记忆备注关键词", subtitle = "将当前输入的备注记为分类关键词", enabled = canRememberKeyword) { onRememberKeyword() }
            Text("图片", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm))
            NavRow("账单图片质量", value = if (prefs["rp_img_quality"] == "NORMAL") "标清" else "高清") { dialog = "quality" }
            SwitchRow("自动删除相册图片", "从相册添加账单图片后，自动删除相册原图", prefs["rp_del_album"] == "1") { vm.set("rp_del_album", if (it) "1" else "0") }
            Text("记忆", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm))
            SwitchRow("自动记忆账户", "新建明细选择类别后，自动切换上次使用的账户", prefs["rp_mem_account"] != "0") { vm.set("rp_mem_account", if (it) "1" else "0") }
            SwitchRow("自动记忆角色", "新建明细选择类别后，自动切换上次使用的角色", prefs["rp_mem_role"] != "0") { vm.set("rp_mem_role", if (it) "1" else "0") }
            Text("键盘", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm))
            SwitchRow("数字键盘触感反馈", "", prefs["rp_haptic"] != "0") { vm.set("rp_haptic", if (it) "1" else "0") }
            SwitchRow("默认显示+-×÷按钮", "", prefs["rp_show_ops"] == "1") { vm.set("rp_show_ops", if (it) "1" else "0") }
            SwitchRow("倒序排列数字按键", "", prefs["rp_reverse"] == "1") { vm.set("rp_reverse", if (it) "1" else "0") }
            NavRow("按键大小", value = when (prefs["rp_key_size"]) { "LARGE" -> "较大"; "SMALL" -> "较小"; else -> "默认" }) { dialog = "keysize" }
            Text("定位", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { vm.set("rp_geo", if (prefs["rp_geo"] == "1") "0" else "1") }.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm)) {
                Text("保存地理位置信息", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Switch(checked = prefs["rp_geo"] == "1", onCheckedChange = { vm.set("rp_geo", if (it) "1" else "0") })
            }
        }
    }
    when (dialog) {
        "type" -> RadioDialog(title = "初始选中类型", options = typeLabels, selected = prefs["rp_init_type"] ?: "EXPENSE", onPick = { vm.set("rp_init_type", it); dialog = "" })
        "fav" -> RadioDialog(title = "常用类别数量", options = linkedMapOf("4" to "4", "6" to "6", "8" to "8", "12" to "12", "16" to "16"), selected = prefs["rp_fav_count"] ?: "8", onPick = { vm.set("rp_fav_count", it); dialog = "" })
        "quality" -> RadioDialog(title = "账单图片质量", options = linkedMapOf("HIGH" to "高清", "NORMAL" to "标清"), selected = prefs["rp_img_quality"] ?: "HIGH", onPick = { vm.set("rp_img_quality", it); dialog = "" })
        "keysize" -> RadioDialog(title = "按键大小", options = linkedMapOf("DEFAULT" to "默认", "LARGE" to "较大", "SMALL" to "较小"), selected = prefs["rp_key_size"] ?: "DEFAULT", onPick = { vm.set("rp_key_size", it); dialog = "" })
        "reset" -> CheckDialog(title = "再记重置项", items = resetItems, selected = (prefs["rp_reset"] ?: "amount,note,tag,merchant,discount,images").split(",").filter { it.isNotBlank() }, onDone = { vm.set("rp_reset", it.joinToString(",")); dialog = "" })
    }
}
// 记账选项页：两排选项的显隐（勾选）与排序（按住 ≡ 拖动），返回时保存
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordChipsOrderScreen(vm: RecordPrefsViewModel, prefs: Map<String, String>, onBack: () -> Unit) {
    var order1 by remember { mutableStateOf(normalizeOrder(prefs["rp_chip_order"] ?: "", row1Items)) }
    var hide1 by remember { mutableStateOf((prefs["rp_chip_hide"] ?: "").split(",").filter { it.isNotBlank() }) }
    var order2 by remember { mutableStateOf(normalizeOrder(prefs["rp_row2_order"] ?: "", row2Items)) }
    var hide2 by remember { mutableStateOf((prefs["rp_row2_hide"] ?: "").split(",").filter { it.isNotBlank() }) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { AppTopBar(title = "记账选项", onBack = {
        vm.set("rp_chip_order", order1.joinToString(",")); vm.set("rp_chip_hide", hide1.joinToString(","))
        vm.set("rp_row2_order", order2.joinToString(",")); vm.set("rp_row2_hide", hide2.joinToString(","))
        onBack()
    }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxWidth().verticalScroll(rememberScrollState())) {
            ChipRowCard("第 1 排", order1, hide1, row1Items, onMove = { f, t -> order1 = order1.toMutableList().apply { add(t, removeAt(f)) } }, onToggle = { k, c -> hide1 = if (c) hide1 - k else hide1 + k })
            ChipRowCard("第 2 排", order2, hide2, row2Items, onMove = { f, t -> order2 = order2.toMutableList().apply { add(t, removeAt(f)) } }, onToggle = { k, c -> hide2 = if (c) hide2 - k else hide2 + k })
            Text("按住右侧的\"≡\"上下拖动排序", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.lg))
        }
    }
}
@Composable
private fun ChipRowCard(title: String, order: List<String>, hidden: List<String>, items: Map<String, String>, onMove: (Int, Int) -> Unit, onToggle: (String, Boolean) -> Unit) {
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyColumnState(listState) { from, to -> onMove(from.index, to.index) }
    AppCard(shape = MaterialTheme.shapes.large, containerColor = MaterialTheme.colorScheme.surfaceContainerLow, contentPadding = PaddingValues(vertical = 6.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md, vertical = 6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm))
            LazyColumn(state = listState, modifier = Modifier.height((order.size * 56).dp), userScrollEnabled = false) {
                items(order, key = { it }) { key ->
                    ReorderableItem(reorderState, key = key) { dragging ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = AppSpacing.sm).zIndex(if (dragging) 1f else 0f)) {
                            Checkbox(checked = key !in hidden, onCheckedChange = { onToggle(key, it) })
                            Text(items[key] ?: key, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.Menu, contentDescription = "拖动排序", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp).longPressDraggableHandle())
                        }
                    }
                }
            }
    }
}
@Composable
private fun NavRow(title: String, subtitle: String = "", value: String = "", enabled: Boolean = true, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onClick() }.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (value.isNotBlank()) Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(AppSpacing.md))
        Switch(checked = checked, onCheckedChange = { onChange(it) })
    }
}
@Composable
private fun RadioDialog(title: String, options: Map<String, String>, selected: String, onPick: (String) -> Unit) {
    AlertDialog(onDismissRequest = { onPick(selected) }, title = { Text(title) }, text = {
        Column { options.forEach { (k, v) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onPick(k) }) {
                RadioButton(selected = k == selected, onClick = { onPick(k) })
                Text(v, style = MaterialTheme.typography.bodyLarge)
            }
        } }
    }, confirmButton = {})
}
@Composable
private fun CheckDialog(title: String, items: Map<String, String>, selected: List<String>, onDone: (List<String>) -> Unit) {
    var sel by remember { mutableStateOf(selected) }
    AlertDialog(onDismissRequest = { onDone(sel) }, title = { Text(title) }, text = {
        Column { items.forEach { (k, v) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { sel = if (k in sel) sel - k else sel + k }) {
                Checkbox(checked = k in sel, onCheckedChange = { checked -> sel = if (checked) sel + k else sel - k })
                Text(v, style = MaterialTheme.typography.bodyLarge)
            }
        } }
    }, confirmButton = { TextButton(onClick = { onDone(sel) }) { Text("完成") } })
}
