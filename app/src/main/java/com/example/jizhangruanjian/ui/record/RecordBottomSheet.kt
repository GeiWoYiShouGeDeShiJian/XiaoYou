package com.example.jizhangruanjian.ui.record
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jizhangruanjian.core.parser.SpeechController
import com.example.jizhangruanjian.core.parser.TextParser
import com.example.jizhangruanjian.core.util.AmountCalculator
import java.util.Locale
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.core.util.KeywordMatcher
import com.example.jizhangruanjian.ui.components.SuccessCheckmark
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.data.model.Transaction
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.domain.model.TransactionDomain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecordBottomSheet(
    accounts: List<AccountDomain>,
    categories: List<CategoryDomain>,
    selectedType: TransactionType,
    editing: Transaction?,
    onTypeChange: (TransactionType) -> Unit,
    onSave: (TransactionDomain, Long?) -> Unit,
    onDismiss: () -> Unit,
    onSaveFinished: () -> Unit,
    onCreateAccount: (String) -> Unit,
    onRememberKeyword: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var amountExpr by remember { mutableStateOf(editing?.let { Formatters.yuanText(it.amount) } ?: "") }
    var note by remember { mutableStateOf(editing?.note ?: "") }
    var categoryId by remember { mutableStateOf(editing?.categoryId ?: 0L) }
    var accountId by remember { mutableStateOf(editing?.accountId ?: (accounts.firstOrNull()?.id ?: 0L)) }
    var toAccountId by remember { mutableStateOf(editing?.toAccountId ?: 0L) }
    var tradeDate by remember { mutableStateOf(editing?.tradeDate ?: System.currentTimeMillis()) }
    var include by remember { mutableStateOf(editing?.includeInSummary ?: (selectedType != TransactionType.TRANSFER)) }
    var newAccountName by remember { mutableStateOf("") }
    var showNewAccount by remember { mutableStateOf(false) }
    var advancedExpanded by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var rememberDialog by remember { mutableStateOf(false) }
    var showCheck by remember { mutableStateOf(false) }
    val checkScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    // 打开时自动聚焦金额输入框并弹出数字键盘
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    // P12 语音识别生命周期：关闭表单即销毁
    var speech: SpeechController? by remember { mutableStateOf(null) }
    DisposableEffect(Unit) {
        onDispose { speech?.destroy() }
    }
    fun applySpeech(text: String) {
        val parsed = TextParser.parse(text, categories.filter { it.type.name == selectedType.name })
        parsed.amountCents?.let { amountExpr = Formatters.yuanText(it) }
        if (parsed.note.isNotBlank()) note = parsed.note
        parsed.category?.let { categoryId = it.id }
        listening = false
    }
    lateinit var startSpeech: () -> Unit
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) startSpeech() }
    startSpeech = {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            val c = speech ?: SpeechController(context, onResult = { applySpeech(it) }, onError = { listening = false }).also { speech = it }
            listening = true; c.start()
        }
    }
    val expenseParents = categories.filter { it.parentId == null }
    val children = categories.filter { it.parentId == categoryId }
    // R13 备注自动识别分类：仅在尚未选分类时命中
    LaunchedEffect(note) {
        if (categoryId == 0L) KeywordMatcher.match(note, categories.filter { it.type.name == selectedType.name })?.let { categoryId = it.id }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            // P12 语音入口
            Button(onClick = { startSpeech() }, enabled = !listening) { Text(if (listening) "正在聆听…" else "🎤 语音记账") }
            Spacer(Modifier.height(12.dp))
            // 顶部类型切换（含转账）
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER).forEach { t ->
                    FilterChip(selected = selectedType == t, onClick = { onTypeChange(t); include = (t != TransactionType.TRANSFER); toAccountId = 0L }, label = { Text(if (t == TransactionType.EXPENSE) "支出" else if (t == TransactionType.INCOME) "收入" else "转账") })
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = amountExpr, onValueChange = { amountExpr = it }, label = { Text("金额") }, singleLine = true, textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().focusRequester(focusRequester))
            Spacer(Modifier.height(4.dp))
            Text("= ${AmountCalculator.evaluate(amountExpr)?.let { String.format(Locale.CHINA, "¥%.2f", it) } ?: "—"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            // R11 计算器快捷键：清空 / 回退 / 四则运算
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { amountExpr = "" }, contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) { Text("C") }
                Button(onClick = { if (amountExpr.isNotBlank()) amountExpr = amountExpr.dropLast(1) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) { Text("⌫") }
                listOf("+", "-", "*", "/", ".", "00").forEach { op -> Button(onClick = { amountExpr += op }, contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) { Text(op) } }
            }
            Spacer(Modifier.height(12.dp))
            if (selectedType != TransactionType.TRANSFER) {
                // 二级分类选择：先一级后二级（P15-S5 圆形网格）
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), maxItemsInEachRow = 4) {
                    expenseParents.forEach { c -> CategoryGridItem(icon = c.icon, label = c.name, selected = (categoryId == c.id || categories.any { e -> e.parentId == c.id && e.id == categoryId }), onClick = { categoryId = c.id }) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), maxItemsInEachRow = 4) {
                    children.forEach { c -> CategoryGridItem(icon = c.icon, label = c.name, selected = categoryId == c.id, onClick = { categoryId = c.id }) }
                }
                // R13 手动选分类后记忆备注为关键词
                if (categoryId != 0L && note.isNotBlank()) {
                    TextButton(onClick = { rememberDialog = true }) { Text("记忆备注为关键词") }
                }
                Spacer(Modifier.height(12.dp))
            }
            if (selectedType == TransactionType.TRANSFER) {
                // 转账：转出账户、转入账户
                Text("转出账户", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.filter { it.id != toAccountId }.forEach { a -> FilterChip(selected = accountId == a.id, onClick = { accountId = a.id }, label = { Text(a.name) }) }
                }
                Spacer(Modifier.height(8.dp))
                Text("转入账户", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.filter { it.id != accountId }.forEach { a -> FilterChip(selected = toAccountId == a.id, onClick = { toAccountId = a.id }, label = { Text(a.name) }) }
                }
            } else {
                // P15-S5 账户/备注折叠区（默认折叠）
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { advancedExpanded = !advancedExpanded }) {
                    Text(if (advancedExpanded) "收起账户 / 备注" else "账户 / 备注", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    Text(if (advancedExpanded) "▾" else "▸", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (advancedExpanded) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        accounts.forEach { a -> FilterChip(selected = accountId == a.id, onClick = { accountId = a.id }, label = { Text(a.name) }) }
                        FilterChip(selected = showNewAccount, onClick = { showNewAccount = !showNewAccount }, label = { Text("➕ 新建账户") })
                    }
                    if (showNewAccount) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(value = newAccountName, onValueChange = { newAccountName = it }, label = { Text("账户名称") }, singleLine = true, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { if (newAccountName.isNotBlank()) { onCreateAccount(newAccountName); newAccountName = ""; showNewAccount = false } }) { Text("添加") }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("计入结余", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        androidx.compose.material3.Switch(checked = include, onCheckedChange = { include = it })
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
            Spacer(Modifier.height(16.dp))
            if (accounts.isEmpty() && !showNewAccount) {
                Text("请先新建账户", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            Button(onClick = {
                val cents = if (amountExpr.isBlank()) 0L else AmountCalculator.toCents(amountExpr)
                val isTransfer = selectedType == TransactionType.TRANSFER
                if (cents < 0L || accountId == 0L) return@Button
                if (!isTransfer && categoryId == 0L) return@Button
                if (isTransfer && (toAccountId == 0L || toAccountId == accountId)) return@Button
                onSave(TransactionDomain(id = editing?.id ?: 0L, ledgerId = editing?.ledgerId ?: 1L, accountId = accountId, toAccountId = if (isTransfer) toAccountId else null, categoryId = if (isTransfer) 0L else categoryId, type = selectedType, amount = cents, note = note, tradeDate = tradeDate, includeInSummary = include), editing?.id)
                showCheck = true
                checkScope.launch { delay(700); onSaveFinished() }
            }, enabled = amountExpr.isNotBlank() && accountId != 0L && (selectedType != TransactionType.TRANSFER && categoryId != 0L || selectedType == TransactionType.TRANSFER && toAccountId != 0L && toAccountId != accountId), modifier = Modifier.fillMaxWidth()) { Text("保存") }
            Spacer(Modifier.height(24.dp))
        }
        if (showCheck) {
            Box(modifier = Modifier.matchParentSize().background(MaterialTheme.colorScheme.surfaceContainer), contentAlignment = Alignment.Center) {
                var appear by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { appear = true }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SuccessCheckmark(visible = appear, modifier = Modifier.size(96.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("保存成功", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        }
    }
    // R13 记忆备注为关键词
    if (rememberDialog) {
        val cat = categories.firstOrNull { it.id == categoryId }
        AlertDialog(
            onDismissRequest = { rememberDialog = false },
            confirmButton = { TextButton(onClick = { onRememberKeyword(categoryId, note); rememberDialog = false }) { Text("记住") } },
            dismissButton = { TextButton(onClick = { rememberDialog = false }) { Text("取消") } },
            title = { Text("记忆关键词") },
            text = { Text("将备注「${note}」作为关键词记忆到分类「${cat?.name ?: ""}」？之后输入会自动归类。") }
        )
    }
}
@Composable
private fun CategoryGridItem(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(2.dp)) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            CategoryIcon(icon, size = 40.dp, fontSize = 20.sp, selected = selected)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}