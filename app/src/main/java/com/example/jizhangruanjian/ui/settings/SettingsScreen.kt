package com.example.jizhangruanjian.ui.settings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.core.security.AppLockManager
import com.example.jizhangruanjian.core.parser.OfficialBillParser
import com.example.jizhangruanjian.data.ThemeStore
import com.example.jizhangruanjian.ui.components.CollapsingTitleScaffold
import com.example.jizhangruanjian.ui.record.NoteSettingsScreen
import com.example.jizhangruanjian.ui.record.RecordPreferenceScreen
import com.example.jizhangruanjian.ui.theme.ThemeColor
// P13 设置中心：分类入口 hub + 二级面板
@Composable
fun SettingsScreen(onBack: () -> Unit, onCustomizeHome: () -> Unit = {}, onOpenLedgerManage: () -> Unit = {}, viewModel: SettingsViewModel = hiltViewModel()) {
    var panel by remember { mutableStateOf<String?>(null) }
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CollapsingTitleScaffold(title = "设置", onBack = onBack) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                SettingsCard(Icons.AutoMirrored.Filled.MenuBook, "账本管理", "收支类别、标签等数据管理") { onOpenLedgerManage() }
                SettingsCard(Icons.Filled.Palette, "主题与显示", "界面外观、功能显示等选项") { panel = "theme" }
                SettingsCard(Icons.Filled.Edit, "记账设置", "记账选项、报销、币种") { panel = "record" }
                SettingsCard(Icons.Filled.PieChart, "报表与统计", "报表显示、每月起始日") { panel = "report" }
                SettingsCard(Icons.Filled.Notifications, "通知与提醒", "记账提醒、还款提醒") { panel = "notify" }
                SettingsCard(Icons.Filled.Lock, "应用锁", "指纹解锁、手势解锁") { panel = "lock" }
                SettingsCard(Icons.Filled.Widgets, "扩展功能", "桌面小组件、URL Scheme") { panel = "extend" }
                Spacer(Modifier.height(16.dp))
            }
        }
        when (panel) {
            "theme" -> ThemePanel(onBack = { panel = null }, onCustomizeHome = onCustomizeHome, viewModel = viewModel)
            "record" -> RecordPanel(onBack = { panel = null }, onOpenPref = { panel = "recordPref" })
            "recordPref" -> RecordPreferenceScreen(onBack = { panel = "record" }, onOpenNoteSettings = { panel = "note" })
            "note" -> NoteSettingsScreen(onBack = { panel = "recordPref" })
            "report" -> ReportPanel(onBack = { panel = null }, viewModel = viewModel)
            "notify" -> NotifyPanel(onBack = { panel = null })
            "lock" -> LockPanel(onBack = { panel = null }, viewModel = viewModel)
            "extend" -> ExtendPanel(onBack = { panel = null }, viewModel = viewModel)
        }
    }
    viewModel.message.collectAsState().value?.let {
        AlertDialog(onDismissRequest = viewModel::clearMessage, confirmButton = { TextButton(onClick = viewModel::clearMessage) { Text("确定") } }, title = { Text("结果") }, text = { Text(it) })
    }
}
@Composable
private fun SettingsCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable
private fun PanelScaffold(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    CollapsingTitleScaffold(title = title, onBack = onBack) {
        Column(Modifier.padding(horizontal = 16.dp)) { content() }
    }
}
@Composable
private fun PanelRow(title: String, subtitle: String? = null, value: String? = null, onClick: (() -> Unit)? = null, trailing: @Composable (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 16.dp, vertical = if (subtitle != null) 13.dp else 15.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (value != null) Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (trailing != null) trailing() else if (onClick != null) Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.padding(start = 6.dp).size(15.dp).rotate(180f), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
private fun ThemePanel(onBack: () -> Unit, onCustomizeHome: () -> Unit, viewModel: SettingsViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    PanelScaffold(title = "主题与显示", onBack = onBack) {
        Text("界面外观", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        PanelRow(title = "深色模式", trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(selected = themeMode == ThemeStore.Mode.SYSTEM, onClick = { viewModel.setThemeMode(ThemeStore.Mode.SYSTEM) }, label = { Text("系统") })
                Spacer(Modifier.width(5.dp))
                FilterChip(selected = themeMode == ThemeStore.Mode.LIGHT, onClick = { viewModel.setThemeMode(ThemeStore.Mode.LIGHT) }, label = { Text("浅色") })
                Spacer(Modifier.width(5.dp))
                FilterChip(selected = themeMode == ThemeStore.Mode.DARK, onClick = { viewModel.setThemeMode(ThemeStore.Mode.DARK) }, label = { Text("深色") })
            }
        })
        PanelRow(title = "主题色", subtitle = "暖橘 / 清爽蓝 / 薄荷绿", trailing = {
            Row {
                ThemeColor.entries.forEach { c ->
                    Box(modifier = Modifier.padding(start = 8.dp).size(30.dp).clip(CircleShape).background(Color(c.preview)).border(if (themeColor == c) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape).clickable { viewModel.setThemeColor(c) }, contentAlignment = Alignment.Center) {
                        if (themeColor == c) Icon(Icons.Filled.Check, contentDescription = c.key, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        })
        PanelRow(title = "动态色彩", subtitle = "Android 12+ 根据壁纸取色", trailing = { Switch(checked = dynamicColor, onCheckedChange = viewModel::setDynamicColor) })
        Spacer(Modifier.height(16.dp))
        Text("功能显示", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        PanelRow(title = "自定义首页", subtitle = "配置首页数据概览、模块显隐与排序", onClick = onCustomizeHome)
    }
}
@Composable
private fun RecordPanel(onBack: () -> Unit, onOpenPref: () -> Unit) {
    PanelScaffold(title = "记账设置", onBack = onBack) {
        PanelRow(title = "偏好设置", subtitle = "初始类型、键盘、图片质量、记忆等", onClick = onOpenPref)
        PanelRow(title = "报销管理", subtitle = "待报销/已报销，从侧边栏进入")
        PanelRow(title = "币种", subtitle = "跟随账本设置，在账本管理中配置")
    }
}
@Composable
private fun ReportPanel(onBack: () -> Unit, viewModel: SettingsViewModel) {
    val monthStart by viewModel.monthStart.collectAsState()
    var showStartDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.loadMonthStart() }
    PanelScaffold(title = "报表与统计", onBack = onBack) {
        PanelRow(title = "每月起始日", subtitle = "月度报表与预算周期的起始日", value = "${monthStart}日", onClick = { showStartDialog = true })
        PanelRow(title = "报表显示", subtitle = "图表样式与维度筛选（开发中）")
        if (showStartDialog) {
            AlertDialog(onDismissRequest = { showStartDialog = false }, confirmButton = {}, dismissButton = { TextButton(onClick = { showStartDialog = false }) { Text("取消") } }, title = { Text("每月起始日") }, text = {
                Column {
                    listOf(1, 5, 10, 15, 20, 25, 28).forEach { d ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { viewModel.setMonthStart(d); showStartDialog = false }.padding(vertical = 12.dp)) {
                            Text("${d}日", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            if (monthStart == d) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            })
        }
    }
}
@Composable
private fun NotifyPanel(onBack: () -> Unit) {
    PanelScaffold(title = "通知与提醒", onBack = onBack) {
        PanelRow(title = "预算提醒", subtitle = "预算使用达 50% / 80% / 100% 时自动通知（已启用）")
        PanelRow(title = "周期记账", subtitle = "周期模板按计划自动入账（已启用）")
        PanelRow(title = "记账提醒", subtitle = "每日固定时间提醒记账（开发中）")
        PanelRow(title = "还款提醒", subtitle = "借贷还款日到期提醒（开发中）")
    }
}
@Composable
private fun LockPanel(onBack: () -> Unit, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    var showLockDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.loadAppLock() }
    PanelScaffold(title = "应用锁", onBack = onBack) {
        val lockSupported = AppLockManager.isSupported(context)
        PanelRow(title = "指纹解锁", subtitle = if (lockSupported) "开启后每次启动需指纹/面容验证" else "此设备不支持指纹/面容", trailing = { Switch(checked = appLockEnabled, enabled = lockSupported, onCheckedChange = { enable -> if (enable && !lockSupported) showLockDialog = true else viewModel.setAppLock(enable) }) })
        PanelRow(title = "手势解锁", subtitle = "绘制图案解锁（开发中）")
        if (showLockDialog) {
            AlertDialog(onDismissRequest = { showLockDialog = false }, confirmButton = { TextButton(onClick = { showLockDialog = false }) { Text("知道了") } }, title = { Text("无法开启") }, text = { Text("此设备未设置指纹或面容，暂无法启用应用锁。") })
        }
    }
}
@Composable
private fun ExtendPanel(onBack: () -> Unit, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val storePath by viewModel.storePath.collectAsState()
    val importing by viewModel.importing.collectAsState()
    val preview by viewModel.preview.collectAsState()
    var importedText by remember { mutableStateOf<String?>(null) }
    var showPathDialog by remember { mutableStateOf(false) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { u ->
            val text = context.contentResolver.openInputStream(u)?.use { OfficialBillParser.decodeBytes(it.readBytes()) } ?: return@let
            importedText = text
            viewModel.loadPreview(text)
        }
    }
    val dirPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? -> uri?.let { viewModel.saveDir(it) } }
    PanelScaffold(title = "扩展功能", onBack = onBack) {
        PanelRow(title = "桌面小组件", subtitle = "总览与快速记账两枚小组件（已内置）")
        PanelRow(title = "URL Scheme", subtitle = "外部唤起快速记账（开发中）")
        Spacer(Modifier.height(16.dp))
        Text("账单导入 / 导出", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        PanelRow(title = "存储路径", subtitle = "备份/模板/导出均保存到该目录", value = if (storePath.contains("/")) storePath else "Download/$storePath", onClick = { showPathDialog = true })
        Spacer(Modifier.height(12.dp))
        Button(onClick = { viewModel.exportCsvToLocal() }) { Text("导出 CSV") }
        Spacer(Modifier.height(10.dp))
        Button(onClick = { importLauncher.launch(arrayOf("text/*", "application/octet-stream")) }) { Text("导入 CSV") }
        if (importing) { Spacer(Modifier.height(10.dp)); CircularProgressIndicator() }
        if (showPathDialog) {
            AlertDialog(onDismissRequest = { showPathDialog = false }, confirmButton = { TextButton(onClick = { showPathDialog = false; dirPicker.launch(null) }) { Text("选择文件夹") } }, dismissButton = { TextButton(onClick = { viewModel.resetDir(); showPathDialog = false }) { Text("恢复默认") } }, title = { Text("存储路径") }, text = {
                Text("当前：${if (storePath.contains("/")) storePath else "Download/$storePath"}\n选择文件夹后，备份/模板/导出均保存到该目录；恢复默认则保存到 Download/小柚记账。", style = MaterialTheme.typography.bodySmall)
            })
        }
        preview?.let { rows ->
            AlertDialog(onDismissRequest = { viewModel.clearPreview() }, confirmButton = {
                TextButton(enabled = !importing, onClick = { importedText?.let { viewModel.importCurrent(it) } }) { Text("确认导入") }
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
    }
}
