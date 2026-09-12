package com.example.jizhangruanjian.ui.autorecord
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.jizhangruanjian.core.autorecord.AutoRecordSettings
import com.example.jizhangruanjian.ui.components.CollapsingTitleScaffold
import com.example.jizhangruanjian.ui.navigation.PlaceholderScreen
@Composable
fun AutoRecordSettingsScreen(onBack: () -> Unit, viewModel: AutoRecordSettingsViewModel = hiltViewModel()) {
    val enabled by viewModel.enabled.collectAsState()
    val showFavCategory by viewModel.showFavCategory.collectAsState()
    val defaultNoteMode by viewModel.defaultNoteMode.collectAsState()
    val defaultRoleMode by viewModel.defaultRoleMode.collectAsState()
    val defaultMerchantMode by viewModel.defaultMerchantMode.collectAsState()
    val defaultTagMode by viewModel.defaultTagMode.collectAsState()
    val channelTag by viewModel.channelTag.collectAsState()
    val insufficientTip by viewModel.insufficientTip.collectAsState()
    val checkOnLaunch by viewModel.checkOnLaunch.collectAsState()
    val dupEdit by viewModel.dupEdit.collectAsState()
    val hideBackground by viewModel.hideBackground.collectAsState()
    var placeholder by remember { mutableStateOf<String?>(null) }
    var showHelp by remember { mutableStateOf(false) }
    val overlayMissing by viewModel.overlayMissing.collectAsState()
    val missingPerms by viewModel.missingPerms.collectAsState()
    val testHint by viewModel.testHint.collectAsState()
    val ctx = LocalContext.current
    LaunchedEffect(testHint) {
        if (testHint != null) {
            Toast.makeText(ctx, testHint, Toast.LENGTH_SHORT).show()
            viewModel.testHint.value = null
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadExtended()
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    placeholder?.let { title ->
        PlaceholderScreen(title, onBack = { placeholder = null })
        return
    }
    CollapsingTitleScaffold(title = "自动记账", onBack = onBack, trailing = {
        IconButton(onClick = { showHelp = true }) { Icon(Icons.Filled.HelpOutline, contentDescription = "帮助") }
    }) {
        SettingCard {
            SwitchRow("开启自动记账", enabled) { viewModel.setEnabled(it) }
        }
        SettingSection("数据")
        if (missingPerms.isNotEmpty()) {
            SettingCard {
                Text("缺少以下运行权限", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 6.dp))
                Text("自动记账将降级为通知提醒，无法弹出悬浮球", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                missingPerms.forEach { key ->
                    val label = when (key) { "overlay" -> "悬浮窗（显示在其他应用上层）"; "acc" -> "无障碍服务"; else -> "通知使用权" }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            when (key) {
                                "overlay" -> viewModel.openOverlaySettings()
                                "acc" -> viewModel.openAccessibilitySettings()
                                else -> viewModel.openNotificationListenerSettings()
                            }
                        }) { Text("去开启", color = MaterialTheme.colorScheme.primary) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        SettingCard {
            DescRow("草稿箱", "来不及记账时可先存入草稿，后续再编辑") { placeholder = "草稿箱" }
            DescRow("已保存的明细", "") { placeholder = "已保存的明细" }
        }
        SettingSection("功能设置")
        SettingCard {
            DescRow("常驻通知设置", "自定义常驻通知显示的数据与样式") { placeholder = "常驻通知设置" }
            DescRow("应用开关", "配置自动记账支持的应用") { placeholder = "应用开关" }
        }
        SettingSection("记账设置")
        SettingCard {
            SwitchRow("显示常用类别", showFavCategory) { viewModel.setShowFavCategory(it) }
            ValueRow("默认账本", "当前账本") { placeholder = "默认账本" }
            DescRow("默认类别", "首次识别将填充默认类别") { placeholder = "默认类别" }
            DescRow("默认账户", "未识别到账户时将填充默认账户") { placeholder = "默认账户" }
            ValueRow("默认备注", if (defaultNoteMode == AutoRecordSettings.NOTE_MODE_MERCHANT) "商户或商品说明" else "同商户记忆上次所选") { placeholder = "默认备注" }
            ValueRow("默认角色", modeLabel(defaultRoleMode)) { placeholder = "默认角色" }
            ValueRow("默认商家", modeLabel(defaultMerchantMode)) { placeholder = "默认商家" }
            ValueRow("默认标签", modeLabel(defaultTagMode)) { placeholder = "默认标签" }
            SwitchRow("自动添加「交易渠道」标签", channelTag) { viewModel.setChannelTag(it) }
            SwitchDescRow("余额不足提示", "当账户余额不足以覆盖交易金额时显示提示", insufficientTip) { viewModel.setInsufficientTip(it) }
        }
        SettingSection("其他")
        SettingCard {
            SwitchDescRow("启动应用时检查服务状态", "如果无障碍服务被关闭会显示弹窗提醒", checkOnLaunch) { viewModel.setCheckOnLaunch(it) }
            SwitchDescRow("重复账单显示编辑弹窗", "关闭后，对于已添加的账单将只显示提示，不显示弹窗", dupEdit) { viewModel.setDupEdit(it) }
            SwitchDescRow("隐藏后台", "使用「侧滑/返回键」退出应用后，应用将不显示在多任务管理界面中", hideBackground) { viewModel.setHideBackground(it) }
        }
        Spacer(Modifier.height(8.dp))
        SettingCard {
            DescRow("▶ 测试自动记账", "生成一笔模拟账单触发：识别→入账→悬浮球，可验证分类面板与本次更新") { viewModel.testRecord() }
        }
        Spacer(Modifier.height(24.dp))
    }
    if (showHelp) HelpDialog { showHelp = false }
    if (overlayMissing) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOverlayMissing() },
            title = { Text("需要悬浮窗权限") },
            text = { Text("自动记账悬浮球需要「显示在其他应用上层」权限才能弹出。当前未开启，因此只降级为通知。是否前往开启？") },
            confirmButton = { TextButton(onClick = { viewModel.openOverlaySettings() }) { Text("去开启") } },
            dismissButton = { TextButton(onClick = { viewModel.dismissOverlayMissing() }) { Text("稍后再说") } }
        )
    }
}
private fun modeLabel(mode: String): String = when (mode) {
    AutoRecordSettings.MODE_MEM_LAST -> "同商户记忆上次所选"
    AutoRecordSettings.MERCHANT_MODE_NONE -> "不要商家（默认）"
    else -> mode
}
@Composable
private fun SettingSection(title: String) {
    Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
}
@Composable
private fun SettingCard(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp, vertical = 4.dp), content = content)
}
@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
@Composable
private fun SwitchDescRow(label: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (desc.isNotBlank()) Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
@Composable
private fun DescRow(label: String, desc: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (desc.isNotBlank()) Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
private fun ValueRow(label: String, value: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("自动记账说明") },
        text = { Text("开启自动记账后，在白名单应用支付成功页将自动识别账单并通过浮窗/通知确认入账。\n\n· 三级去重：页面防抖 → 指纹防抖 → 入库查重，重复账单可弹窗编辑或仅提示") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("知道了") } },
        dismissButton = {})
}
