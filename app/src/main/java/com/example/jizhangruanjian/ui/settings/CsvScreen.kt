package com.example.jizhangruanjian.ui.settings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.core.parser.OfficialBillParser
import com.example.jizhangruanjian.data.model.Ledger
import com.example.jizhangruanjian.ui.components.AppButton
import com.example.jizhangruanjian.ui.components.AppButtonVariant
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.theme.ImportBrandColors
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
// 账单导入与导出：双 Tab（导入来源选择 + 导出时间范围）
private data class ImportSource(val label: String, val button: String, val icon: ImageVector, val color: Color, val help: String)
private val SOURCES = listOf(
    ImportSource("微信支付账单", "导入微信支付账单", Icons.Filled.Chat, ImportBrandColors.wechat, "账单获取：微信 - 我 - 服务 - 钱包 - 账单 - 右上角\"...\" - 下载账单 - 用于个人对账。接收方式选择微信，下载后直接长按文件 - 发送 - 导入到小柚记账。"),
    ImportSource("支付宝账单", "导入支付宝账单", Icons.Filled.AccountBalanceWallet, ImportBrandColors.alipay, "账单获取：支付宝 - 我的 - 账单 - 右上角\"...\" - 开具交易流水证明，下载后导入。"),
    ImportSource("通用模板导入（推荐）", "导入模板文件", Icons.Filled.TableChart, ImportBrandColors.bankTemplate, "列固定：类型/日期/大类/小类/金额/账户/账户2/备注/图片/颜色/标签/账本/商家；类型支持支出、收入、转账、借、贷、应付款、应收款；导入按去重规则自动跳过重复记录。"))
private const val TEMPLATE_CSV = "说明：可以使用Excel等进行编辑，请将对应应用的数据按要求复制进本模板中，注意不要新建文件，否则可能缺少必要的合格模板字段导致导入失败。\n" +
    "注意：【必填字段】类型、日期、金额、账户不能为空；选填字段可用\"/\"代替，也可以不填。\n" +
    "【类型】只允许填写：支出、收入、转账、借、贷、应付款、应收款；日期格式如 2021/10/10 06:10:01，也可只写 2021/10/01。\n" +
    "类型,日期,大类,小类,金额,账户,账户2,备注,图片,颜色,标签,账本,商家\n" +
    "支出,2021/10/10 12:00,餐饮,午饭,16,支付宝,,,,,家人,,示例商家\n" +
    "收入,2021/10/10 10:46,,工资收入,5000,微信钱包,,,,,,,\n" +
    "转账,2021/10/10 19:08,,账户间转账,1000,支付宝,微信钱包,,示例备注：转账,,,,\n" +
    "借,2021/10/10 16:51,,借款,500,支付宝,,,,,,,\n" +
    "贷,2021/10/10 12:46,,收款,500,微信钱包,,,,,,,\n" +
    "应付款,2025/5/30 12:00,服饰,衣服,100,微信钱包,,,,,,,\n" +
    "应收款,2025/5/30 12:00,,工资收入,5000,支付宝,,,,,,,\n"
private fun parseDate(text: String): Long? = try { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA).parse(text)?.time } catch (_: Exception) { null }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvScreen(onBack: () -> Unit, onAddLedger: () -> Unit = {}, onManageLedger: () -> Unit = {}, viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val preview by viewModel.preview.collectAsState()
    val message by viewModel.message.collectAsState()
    val importing by viewModel.importing.collectAsState()
    val ledgerName by viewModel.ledgerName.collectAsState()
    val ledgers by viewModel.ledgers.collectAsState()
    val currentId by viewModel.currentLedgerId.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var sourceIndex by remember { mutableIntStateOf(0) }
    var onlyIncomeExpense by remember { mutableStateOf(true) }
    var rangeIndex by remember { mutableIntStateOf(2) }
    var customStart by remember { mutableStateOf("") }
    var customEnd by remember { mutableStateOf("") }
    var showHelp by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showLedgerPicker by remember { mutableStateOf(false) }
    var showLedgerSearch by remember { mutableStateOf(false) }
    var ledgerSearch by remember { mutableStateOf("") }
    var targetLedgerId by remember { mutableStateOf(0L) }
    var exportLedgerId by remember { mutableStateOf(0L) }
    var pickerTarget by remember { mutableIntStateOf(0) }
    var showAdvanced by remember { mutableStateOf(false) }
    var showBanner by remember { mutableStateOf(true) }
    var showFieldDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    val exportFields = remember { mutableStateListOf("日期", "类型", "分类", "账户", "金额", "备注", "标签") }
    var importedText by remember { mutableStateOf<String?>(null) }
    val importHistory = remember { mutableStateListOf<String>() }
    val targetId = if (targetLedgerId > 0L) targetLedgerId else currentId
    val targetName = ledgers.firstOrNull { it.id == targetId }?.name ?: ledgerName.ifBlank { "当前账本" }
    val exportId = if (exportLedgerId > 0L) exportLedgerId else currentId
    val exportName = ledgers.firstOrNull { it.id == exportId }?.name ?: ledgerName.ifBlank { "当前账本" }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { u ->
            val bytes = context.contentResolver.openInputStream(u)?.use { it.readBytes() } ?: return@let
            val text = OfficialBillParser.decodeBytes(bytes)
            importedText = text
            viewModel.loadPreview(text)
        }
    }
    LaunchedEffect(message) { message?.let { importHistory.add(it) } }
    val range: () -> Pair<Long?, Long?> = {
        when (rangeIndex) {
            0 -> Pair(java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), System.currentTimeMillis())
            1 -> Pair(java.time.LocalDate.now().withDayOfYear(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), System.currentTimeMillis())
            3 -> Pair(parseDate(customStart), parseDate(customEnd))
            else -> Pair(null, null)
        }
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.surface, topBar = {
        Column {
            AppTopBar(title = "账单导入与导出", onBack = onBack, actions = { IconButton(onClick = { showHelp = true }) { Icon(Icons.Filled.HelpOutline, contentDescription = "帮助") } })
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("账单导入") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("账单导出") })
            }
        }
    }) { pad ->
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(pad).padding(AppSpacing.lg)) {
            if (tab == 0) {
                val source = SOURCES[sourceIndex]
                Column(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    SOURCES.forEachIndexed { i, s ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { sourceIndex = i }.padding(horizontal = AppSpacing.md, vertical = 6.dp)) {
                            RadioButton(selected = sourceIndex == i, onClick = { sourceIndex = i })
                            Spacer(Modifier.width(AppSpacing.sm))
                            Icon(s.icon, contentDescription = s.label, tint = s.color, modifier = Modifier.size(AppSize.iconLarge))
                            Spacer(Modifier.width(10.dp))
                            Text(s.label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Text(source.help, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = AppSpacing.md))
                    if (sourceIndex == SOURCES.lastIndex) {
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            AppButton(text = "获取模板文件", onClick = { viewModel.writeLocal("通用模板_${System.currentTimeMillis()}.csv", TEMPLATE_CSV.toByteArray()) }, variant = AppButtonVariant.Outlined)
                        }
                        Text("点击获取后分享至电脑，请根据模板文件内的要求将数据填入模板（不要新建文件），再传回手机进行导入。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = AppSpacing.md))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showLedgerPicker = true }.padding(horizontal = AppSpacing.md)) {
                        Text("导入至", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(targetName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (sourceIndex != SOURCES.lastIndex) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md)) {
                            Checkbox(checked = onlyIncomeExpense, onCheckedChange = { onlyIncomeExpense = it })
                            Text("只导入支出和收入类型", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    AppButton(text = source.button, onClick = { importLauncher.launch(arrayOf("text/*", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth().padding(AppSpacing.md))
                    if (importing) { Row(modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.md), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator(modifier = Modifier.size(AppSize.iconLarge)) } }
                }
                Spacer(Modifier.height(AppSpacing.md))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showHistory = true }.padding(horizontal = AppSpacing.lg, vertical = 14.dp)) {
                    Text("已导入的账单", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 1; showLedgerPicker = true }.padding(horizontal = AppSpacing.md, vertical = AppSpacing.md)) {
                        Text("导出账本", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(exportName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("时间范围", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = AppSpacing.md))
                    Spacer(Modifier.height(AppSpacing.sm))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        listOf("本月", "本年", "全部", "自定义").forEachIndexed { i, label ->
                            FilterChip(selected = rangeIndex == i, onClick = { rangeIndex = i }, label = { Text(label) })
                        }
                    }
                    if (rangeIndex == 3) {
                        Spacer(Modifier.height(AppSpacing.sm))
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            OutlinedTextField(value = customStart, onValueChange = { customStart = it }, label = { Text("开始日期") }, placeholder = { Text("2026-01-01") }, singleLine = true, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = customEnd, onValueChange = { customEnd = it }, label = { Text("结束日期") }, placeholder = { Text("2026-12-31") }, singleLine = true, modifier = Modifier.weight(1f))
                        }
                    }
                    AppButton(text = "导出为Excel", onClick = { val (s, e) = range(); viewModel.exportCsvToLocal(s, e, exportId, if (exportFields.size == 7) null else exportFields.toList()) }, modifier = Modifier.fillMaxWidth().padding(AppSpacing.md))
                }
                Spacer(Modifier.height(AppSpacing.md))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showBanner = true; showAdvanced = true }.padding(horizontal = AppSpacing.lg, vertical = 14.dp)) {
                    Text("高级设置", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(AppSpacing.lg))
                Text("1. 导出的账单文件中包含 6 张表格，分别为支出、收入、转账、借贷、应付款、应收款。文件格式为xls，可通过Excel或WPS另存为csv等其他格式。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text("2. 对于仅需导出特定数据的场景，可通过首页搜索、筛选等方式查询明细，长按明细 - 批量操作 - 导出。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text("3. 账单导出功能适用于浏览账单与对账，若您需要备份完整数据以及同步数据到其他设备，请使用「数据备份与恢复」功能。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    preview?.let { rows ->
        AlertDialog(onDismissRequest = { viewModel.clearPreview() }, confirmButton = {
            TextButton(enabled = !importing, onClick = { importedText?.let { viewModel.importCurrent(it, targetId.takeIf { id -> id > 0L }) } }) { Text("确认导入") }
        }, dismissButton = { TextButton(onClick = { viewModel.clearPreview() }) { Text("取消") } }, title = { Text("导入预览") }, text = {
            LazyColumn(modifier = Modifier.height(320.dp)) {
                items(rows) { r ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("${r.date} ${r.typeLabel} ${r.category}/${r.account} ¥${r.amountYuan}${if (r.note.isNotBlank()) " · ${r.note}" else ""}", style = MaterialTheme.typography.bodySmall)
                        Text(if (r.duplicate) "重复，将跳过" else if (r.valid) "可导入" else "无效", style = MaterialTheme.typography.labelSmall, color = if (r.duplicate || !r.valid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                }
            }
        })
    }
    message?.let {
        AlertDialog(onDismissRequest = viewModel::clearMessage, confirmButton = { TextButton(onClick = viewModel::clearMessage) { Text("确定") } }, title = { Text("结果") }, text = { Text(it) })
    }
    if (showHelp) {
        AlertDialog(onDismissRequest = { showHelp = false }, confirmButton = { TextButton(onClick = { showHelp = false }) { Text("知道了") } }, title = { Text("使用说明") }, text = {
            Column {
                Text("1. 账单导入支持通用模板 CSV，导入时自动按去重规则跳过重复记录。", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Text("2. 账单导出生成 CSV 文件，适用于浏览账单与对账。", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Text("3. 完整数据备份与换机迁移请使用「数据备份与恢复」功能。", style = MaterialTheme.typography.bodySmall)
            }
        })
    }
    if (showHistory) {
        AlertDialog(onDismissRequest = { showHistory = false }, confirmButton = { TextButton(onClick = { showHistory = false }) { Text("确定") } }, title = { Text("已导入的账单") }, text = {
            if (importHistory.isEmpty()) Text("本次使用暂无导入记录", style = MaterialTheme.typography.bodySmall)
            else LazyColumn(modifier = Modifier.height(240.dp)) { items(importHistory.toList()) { h -> Text(h, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp)) } }
        })
    }
    if (showLedgerPicker) {
        ModalBottomSheet(onDismissRequest = { showLedgerPicker = false }) {
            Column(modifier = Modifier.padding(bottom = AppSpacing.xxl)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm)) {
                    IconButton(onClick = { showLedgerPicker = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                    Text("账本", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showLedgerSearch = !showLedgerSearch }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
                    IconButton(onClick = { showLedgerPicker = false; onAddLedger() }) { Icon(Icons.Filled.AddCircle, contentDescription = "选择账本模板") }
                    IconButton(onClick = { showLedgerPicker = false; onManageLedger() }) { Icon(Icons.Filled.Settings, contentDescription = "账本管理") }
                }
                if (showLedgerSearch) {
                    OutlinedTextField(value = ledgerSearch, onValueChange = { ledgerSearch = it }, label = { Text("搜索账本") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg))
                }
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(ledgers.filter { ledgerSearch.isBlank() || it.name.contains(ledgerSearch) }, key = { it.id }) { l ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { if (pickerTarget == 0) targetLedgerId = l.id else exportLedgerId = l.id; showLedgerPicker = false }.padding(horizontal = AppSpacing.lg, vertical = 10.dp)) {
                            Box(modifier = Modifier.size(36.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(AppSize.iconSize))
                            }
                            Spacer(Modifier.width(AppSpacing.md))
                            Text(l.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showLedgerPicker = false; onAddLedger() }.padding(horizontal = AppSpacing.lg, vertical = 10.dp)) {
                            Icon(Icons.Filled.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(AppSpacing.md))
                            Text("添加账本", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
    if (showAdvanced) {
        ModalBottomSheet(onDismissRequest = { showAdvanced = false }) {
            Column(modifier = Modifier.padding(start = AppSpacing.lg, end = AppSpacing.lg, bottom = AppSpacing.xxl)) {
                if (showBanner) {
                    Spacer(Modifier.height(AppSpacing.md))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = 10.dp, vertical = 10.dp)) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(AppSpacing.sm))
                        Text("以下设置对「长按明细 - 批量操作 - 导出」同样生效", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.weight(1f))
                        IconButton(onClick = { showBanner = false }, modifier = Modifier.size(AppSize.iconSize)) { Icon(Icons.Filled.Close, contentDescription = "关闭", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(AppSize.iconSmall)) }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showFieldDialog = true }.padding(vertical = 14.dp)) {
                    Text("导出的字段", style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 6.dp).size(14.dp))
                    Spacer(Modifier.weight(1f))
                    Text(if (exportFields.size == 7) "全部" else exportFields.joinToString("/"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showImageDialog = true }.padding(vertical = 14.dp)) {
                    Text("账单图片", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.weight(1f))
                    Text("不导出", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    if (showFieldDialog) {
        AlertDialog(onDismissRequest = { showFieldDialog = false }, confirmButton = { TextButton(onClick = { showFieldDialog = false }) { Text("确定") } }, title = { Text("导出的字段") }, text = {
            Column {
                listOf("日期", "类型", "分类", "账户", "金额", "备注", "标签").forEach { f ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { if (f in exportFields) { if (exportFields.size > 1) exportFields.remove(f) } else exportFields.add(f) }.padding(vertical = 6.dp)) {
                        Checkbox(checked = f in exportFields, onCheckedChange = { c -> if (c) exportFields.add(f) else if (exportFields.size > 1) exportFields.remove(f) })
                        Text(f, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        })
    }
    if (showImageDialog) {
        AlertDialog(onDismissRequest = { showImageDialog = false }, confirmButton = { TextButton(onClick = { showImageDialog = false }) { Text("确定") } }, title = { Text("账单图片") }, text = { Text("CSV 导出暂不包含图片附件；图片保存在应用内部存储，完整备份请使用「数据备份与恢复」。", style = MaterialTheme.typography.bodySmall) })
    }
}
