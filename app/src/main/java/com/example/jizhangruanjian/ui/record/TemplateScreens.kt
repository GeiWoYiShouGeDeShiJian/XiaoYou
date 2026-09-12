package com.example.jizhangruanjian.ui.record
import com.example.jizhangruanjian.ui.theme.SemanticColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jizhangruanjian.core.database.RecordTemplateDao
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.core.util.KeypadCalculator
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.model.Member
import com.example.jizhangruanjian.data.model.Merchant
import com.example.jizhangruanjian.data.model.RecordTemplate
import com.example.jizhangruanjian.data.model.Tag
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.ui.components.AppButton
import com.example.jizhangruanjian.ui.components.AppButtonVariant
import com.example.jizhangruanjian.ui.components.AppTextField
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.components.AppCard
import com.example.jizhangruanjian.ui.theme.AppSpacing
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
@HiltViewModel
class RecordTemplateViewModel @Inject constructor(private val dao: RecordTemplateDao) : ViewModel() {
    val templates: StateFlow<List<RecordTemplate>> = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun add(t: RecordTemplate) = viewModelScope.launch { dao.insert(t) }
    fun update(t: RecordTemplate) = viewModelScope.launch { dao.update(t) }
    fun delete(t: RecordTemplate) = viewModelScope.launch { dao.delete(t) }
}
private val templateTypes = listOf("EXPENSE" to "支出", "INCOME" to "收入", "TRANSFER" to "转账", "LOAN" to "借贷")
private val templateCurrencies = listOf("CNY" to "人民币", "HKD" to "港币", "MOP" to "澳门元", "TWD" to "新台币", "USD" to "美元", "EUR" to "欧元", "GBP" to "英镑", "JPY" to "日元", "KRW" to "韩元")
private fun typeTextOf(type: String): String = templateTypes.firstOrNull { it.first == type }?.second ?: "支出"
// 模板面板：与商家/标签同款底部弹层，半屏可上拉全屏，仅全屏时列表可滚动
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateSheet(templates: List<RecordTemplate>, iconOf: (Long) -> String, catNameOf: (Long) -> String, accNameOf: (Long) -> String, onClose: () -> Unit, onApply: (RecordTemplate) -> Unit, onDelete: (RecordTemplate) -> Unit, onEdit: (RecordTemplate) -> Unit, onAddNew: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var pendingDelete by remember { mutableStateOf<RecordTemplate?>(null) }
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, dragHandle = null) {
        Column(Modifier.fillMaxWidth().fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm)) {
                IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                Spacer(Modifier.width(AppSpacing.sm))
                Text("模板", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onAddNew) { Icon(Icons.Filled.AddCircle, contentDescription = "新增模板") }
            }
            if (templates.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("日常交易可以通过模板来快捷记账哦", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    item { Spacer(Modifier.height(6.dp)) }
                    items(templates, key = { it.id }) { tpl ->
                        AppCard(shape = MaterialTheme.shapes.large, containerColor = MaterialTheme.colorScheme.surfaceContainerLow, contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = 10.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { onApply(tpl) }, onLongClick = { pendingDelete = tpl })) {
                                CategoryIcon(iconOf(tpl.categoryId), size = 36.dp, fontSize = 20.sp)
                                Spacer(Modifier.width(AppSpacing.md))
                                Column(Modifier.weight(1f)) {
                                    Text(tpl.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1)
                                    Text(listOf(typeTextOf(tpl.type), catNameOf(tpl.categoryId), accNameOf(tpl.accountId)).filter { it.isNotBlank() }.joinToString(" · "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                                Text("编辑", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable { onEdit(tpl) }.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm))
                            }
                        }
                    }
                    item { Spacer(Modifier.height(AppSpacing.xl)) }
                }
            }
        }
    }
    pendingDelete?.let { tpl ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("删除模板") }, text = { Text("确定删除「${tpl.name}」吗？") }, confirmButton = {
            TextButton(onClick = { onDelete(tpl); pendingDelete = null }) { Text("删除") }
        }, dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("取消") } })
    }
}
// 添加/编辑模板页：快照当前草稿，逐项修改后保存
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditScreen(initial: RecordTemplate, categories: List<CategoryDomain>, accounts: List<AccountDomain>, members: List<Member>, tags: List<Tag>, merchants: List<Merchant>, onSave: (RecordTemplate) -> Unit, onDelete: (RecordTemplate) -> Unit, onBack: () -> Unit) {
    var tpl by remember { mutableStateOf(initial) }
    var type by remember { mutableStateOf(TransactionType.valueOf(initial.type)) }
    var dialog by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf(if (initial.amountCents > 0L) Formatters.yuanText(initial.amountCents) else "") }
    var discountText by remember { mutableStateOf(if (initial.discountCents > 0L) Formatters.yuanText(initial.discountCents) else "") }
    val typeCats = categories.filter { it.type == CategoryType.valueOf(type.name) }
    val catName = categories.firstOrNull { it.id == tpl.categoryId }?.let { c -> (c.parentId?.let { p -> categories.firstOrNull { it.id == p }?.name?.plus("·") } ?: "") + c.name } ?: ""
    val accName = accounts.firstOrNull { it.id == tpl.accountId }?.name ?: ""
    val toAccName = accounts.firstOrNull { it.id == tpl.toAccountId }?.name ?: ""
    val tagList = tpl.tagIds.split(",").mapNotNull { it.toLongOrNull() }
    val memberList = tpl.memberIds.split(",").mapNotNull { it.toLongOrNull() }
    val typeEnum = type
    val isTransfer = type == TransactionType.TRANSFER
    val isLoan = type == TransactionType.LOAN
    val amountCentsNow = KeypadCalculator.toCents(amountText) ?: 0L
    val discountCentsNow = KeypadCalculator.toCents(discountText) ?: 0L
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        AppTopBar(title = "", onBack = onBack, actions = {
            IconButton(onClick = { onSave(tpl.copy(type = typeEnum.name, amountCents = amountCentsNow, discountCents = if (typeEnum == TransactionType.EXPENSE) discountCentsNow else 0L)) }) { Icon(Icons.Filled.Check, contentDescription = "保存") }
        })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().imePadding()) {
            Row(Modifier.fillMaxWidth()) {
            templateTypes.forEach { (k, label) ->
                val sel = type.name == k
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable {
                    type = TransactionType.valueOf(k)
                    val newTypeCats = categories.filter { it.type == CategoryType.valueOf(k) }
                    val defCat = newTypeCats.firstOrNull { it.parentId != null } ?: newTypeCats.firstOrNull()
                    tpl = tpl.copy(type = k, categoryId = if (categories.firstOrNull { it.id == tpl.categoryId }?.type == CategoryType.valueOf(k)) tpl.categoryId else (defCat?.id ?: 0L))
                }) {
                    Text(label, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 10.dp))
                    Box(Modifier.width(28.dp).height(3.dp).background(if (sel) MaterialTheme.colorScheme.primary else Color.Transparent))
                }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            AppTextField(value = tpl.name, onValueChange = { tpl = tpl.copy(name = it) }, label = "模板名称", singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm))
            if (isTransfer) {
                FormRow("类别", "账户互转", enabled = false, onClick = {})
                TransferAccountRow(accName.ifBlank { "未选择" }, toAccName.ifBlank { "未选择" }, { dialog = "account" }, { dialog = "toaccount" })
            } else {
                FormRow("类别", catName.ifBlank { "未选择" }, muted = catName.isBlank()) { dialog = "cat" }
                if (isLoan) TransferAccountRow(accName.ifBlank { "未选择" }, toAccName.ifBlank { "未选择" }, { dialog = "account" }, { dialog = "toaccount" })
                else FormRow("账户", accName.ifBlank { "未选择" }, muted = accName.isBlank()) { dialog = "account" }
            }
            FormRow("币种", templateCurrencies.firstOrNull { it.first == tpl.currency }?.second ?: tpl.currency) { dialog = "currency" }
            FormRow("金额", if (amountCentsNow > 0L) "¥${Formatters.yuanText(amountCentsNow)}" else "选填", muted = amountCentsNow <= 0L) { dialog = "amount" }
            if (type == TransactionType.EXPENSE) FormRow("优惠", if (discountCentsNow > 0L) "¥${Formatters.yuanText(discountCentsNow)}" else "无优惠", muted = discountCentsNow <= 0L) { dialog = "discount" }
            if (isTransfer) FormRow("手续费", if (tpl.feeCents > 0L) "¥${Formatters.yuanText(tpl.feeCents)}" else "无手续费/补贴", muted = tpl.feeCents <= 0L) { dialog = "fee" }
            FormRow("备注", tpl.note.ifBlank { "选填" }, muted = tpl.note.isBlank()) { dialog = "note" }
            FormRow("标签", if (tagList.isEmpty()) "无标签" else tagList.mapNotNull { id -> tags.firstOrNull { it.id == id }?.name }.joinToString(" "), muted = tagList.isEmpty()) { dialog = "tag" }
            if (!isTransfer && !isLoan) {
                FormRow("角色", if (memberList.isEmpty()) "自己" else memberList.mapNotNull { id -> members.firstOrNull { it.id == id }?.name }.joinToString("，"), muted = memberList.isEmpty()) { dialog = "member" }
                FormRow("商家", tpl.merchant.ifBlank { "无商家" }, muted = tpl.merchant.isBlank()) { dialog = "merchant" }
            }
            if (type == TransactionType.EXPENSE) {
                SwitchRow("待报销", "标记后进入报销管理", tpl.reimbursable) { tpl = tpl.copy(reimbursable = it) }
                SwitchRow("应付款", "未收付款状态", tpl.paymentUnpaid) { tpl = tpl.copy(paymentUnpaid = it) }
                SwitchRow("不计入预算", "该笔支出不占用预算", !tpl.includeBudget) { tpl = tpl.copy(includeBudget = !it) }
                SwitchRow("不计入收支", "该笔不参与收支统计", !tpl.includeSummary) { tpl = tpl.copy(includeSummary = !it) }
            }
            if (type == TransactionType.INCOME) {
                SwitchRow("应收款", "未收付款状态", tpl.paymentUnpaid) { tpl = tpl.copy(paymentUnpaid = it) }
                SwitchRow("不计入预算", "该笔收入不占用预算", !tpl.includeBudget) { tpl = tpl.copy(includeBudget = !it) }
                SwitchRow("不计入收支", "该笔不参与收支统计", !tpl.includeSummary) { tpl = tpl.copy(includeSummary = !it) }
            }
            SwitchRow("快速保存", "已设置金额的模板，使用时可直接保存", tpl.quickSave) { tpl = tpl.copy(quickSave = it) }
            if (initial.id != 0L) {
                Text("删除模板", color = SemanticColors.ExpenseRed, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().clickable { onDelete(tpl) }.padding(vertical = AppSpacing.lg))
            }
            Spacer(Modifier.height(AppSpacing.sm))
            AppButton(text = "保存", onClick = { onSave(tpl.copy(type = type.name, amountCents = amountCentsNow, discountCents = if (type == TransactionType.EXPENSE) discountCentsNow else 0L)) }, variant = AppButtonVariant.Primary, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg))
            Spacer(Modifier.height(AppSpacing.lg))
        }
        }
    }
    when (dialog) {
        "cat" -> PickerDialog(title = "选择类别", onClose = { dialog = "" }) {
            val rows = buildList {
                typeCats.filter { it.parentId == null }.sortedBy { it.sortOrder }.forEach { p ->
                    add(p)
                    addAll(typeCats.filter { it.parentId == p.id }.sortedBy { it.sortOrder })
                }
            }
            LazyColumn(Modifier.height(380.dp)) {
                items(rows, key = { it.id }) { c ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { tpl = tpl.copy(categoryId = c.id); dialog = "" }.padding(vertical = 10.dp)) {
                        CategoryIcon(c.icon, size = 28.dp, fontSize = 15.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(if (c.parentId != null) "　${c.name}" else c.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        "account" -> PickerDialog(title = if (isTransfer) "选择转出账户" else if (isLoan) "选择账户" else "选择账户", onClose = { dialog = "" }) {
            LazyColumn(Modifier.height(380.dp)) {
                items(accounts.filter { !it.hidden }, key = { it.id }) { a ->
                    Text(a.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clickable { tpl = tpl.copy(accountId = a.id); dialog = "" }.padding(vertical = AppSpacing.md))
                }
            }
        }
        "toaccount" -> PickerDialog(title = if (isLoan) "选择对方账户" else "选择到账账户", onClose = { dialog = "" }) {
            LazyColumn(Modifier.height(380.dp)) {
                items(accounts.filter { !it.hidden }, key = { "t_${it.id}" }) { a ->
                    Text(a.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clickable { tpl = tpl.copy(toAccountId = a.id); dialog = "" }.padding(vertical = AppSpacing.md))
                }
            }
        }
        "currency" -> PickerDialog(title = "选择币种", onClose = { dialog = "" }) {
            LazyColumn(Modifier.height(380.dp)) {
                items(templateCurrencies) { (code, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { tpl = tpl.copy(currency = code); dialog = "" }.padding(vertical = 10.dp)) {
                        RadioButton(selected = tpl.currency == code, onClick = { tpl = tpl.copy(currency = code); dialog = "" })
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        "amount" -> TextDialog(title = "金额", value = amountText, onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } }, onDone = { dialog = "" })
        "discount" -> TextDialog(title = "优惠金额", value = discountText, onValueChange = { discountText = it.filter { ch -> ch.isDigit() || ch == '.' } }, onDone = { dialog = "" })
        "fee" -> TextDialog(title = "手续费金额", value = if (tpl.feeCents > 0L) Formatters.yuanText(tpl.feeCents) else "", onValueChange = { tpl = tpl.copy(feeCents = KeypadCalculator.toCents(it) ?: 0L) }, onDone = { dialog = "" })
        "note" -> TextDialog(title = "备注", value = tpl.note, onValueChange = { tpl = tpl.copy(note = it) }, onDone = { dialog = "" })
        "tag" -> CheckPickerDialog(title = "选择标签", options = tags.map { Triple(it.id, it.name, it.id in tagList) }, onToggle = { id, checked -> tpl = tpl.copy(tagIds = (if (checked) tagList + id else tagList - id).joinToString(",")) }, onDone = { dialog = "" })
        "member" -> CheckPickerDialog(title = "选择角色", options = members.map { Triple(it.id, it.name, it.id in memberList) }, onToggle = { id, checked -> tpl = tpl.copy(memberIds = (if (checked) memberList + id else memberList - id).joinToString(",")) }, onDone = { dialog = "" })
        "merchant" -> PickerDialog(title = "选择商家", onClose = { dialog = "" }) {
            LazyColumn(Modifier.height(380.dp)) {
                item { Text("无商家", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().clickable { tpl = tpl.copy(merchant = ""); dialog = "" }.padding(vertical = AppSpacing.md)) }
                items(merchants, key = { it.id }) { m ->
                    Text(m.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clickable { tpl = tpl.copy(merchant = m.name); dialog = "" }.padding(vertical = AppSpacing.md))
                }
            }
        }
    }
}
// 转账/借贷：左右两端账户 + 中间箭头
@Composable
private fun TransferAccountRow(leftName: String, rightName: String, onLeft: () -> Unit, onRight: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg)) {
        Text(leftName, style = MaterialTheme.typography.bodyMedium, color = if (leftName == "未选择") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f).clickable { onLeft() })
        Text("→", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = AppSpacing.md))
        Text(rightName, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End, color = if (rightName == "未选择") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f).clickable { onRight() })
    }
}
@Composable
private fun FormRow(label: String, value: String, muted: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onClick() }.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
private fun SwitchRow(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(AppSpacing.md))
        Switch(checked = checked, onCheckedChange = { onChange(it) })
    }
}
@Composable
private fun PickerDialog(title: String, onClose: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(onDismissRequest = onClose, title = { Text(title) }, text = { content() }, confirmButton = { TextButton(onClick = onClose) { Text("关闭") } })
}
@Composable
private fun TextDialog(title: String, value: String, onValueChange: (String) -> Unit, onDone: () -> Unit) {
    AlertDialog(onDismissRequest = onDone, title = { Text(title) }, text = {
        OutlinedTextField(value = value, onValueChange = onValueChange, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
    }, confirmButton = { TextButton(onClick = onDone) { Text("确定") } }, dismissButton = { TextButton(onClick = onDone) { Text("取消") } })
}
@Composable
private fun CheckPickerDialog(title: String, options: List<Triple<Long, String, Boolean>>, onToggle: (Long, Boolean) -> Unit, onDone: () -> Unit) {
    AlertDialog(onDismissRequest = onDone, title = { Text(title) }, text = {
        Column(Modifier.height(380.dp).verticalScroll(rememberScrollState())) {
            options.forEach { (id, name, checked) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onToggle(id, !checked) }) {
                    Checkbox(checked = checked, onCheckedChange = { onToggle(id, it) })
                    Text(name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }, confirmButton = { TextButton(onClick = onDone) { Text("完成") } })
}
