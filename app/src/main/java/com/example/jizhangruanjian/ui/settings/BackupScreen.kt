package com.example.jizhangruanjian.ui.settings
import android.os.Build
import com.example.jizhangruanjian.ui.components.CollapsingTitleScaffold
import com.example.jizhangruanjian.ui.theme.AppSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// 数据备份与恢复：本地直存备份 + 自动备份设置 + 恢复/立即备份
@Composable
fun BackupScreen(onBack: () -> Unit, onOpenSettings: () -> Unit = {}, viewModel: BackupViewModel = hiltViewModel()) {
    val storePath by viewModel.storePath.collectAsState()
    val autoEnabled by viewModel.autoBackupEnabled.collectAsState()
    val interval by viewModel.intervalHours.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()
    val keepCount by viewModel.keepCount.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val message by viewModel.message.collectAsState()
    val needPassword by viewModel.needPassword.collectAsState()
    val backupFiles by viewModel.backupFiles.collectAsState()
    var showHelp by remember { mutableStateOf(false) }
    var showInterval by remember { mutableStateOf(false) }
    var showKeep by remember { mutableStateOf(false) }
    var showDevice by remember { mutableStateOf(false) }
    var showRestoreList by remember { mutableStateOf(false) }
    var deviceInput by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    androidx.compose.runtime.LaunchedEffect(viewModel) { viewModel.load() }
    CollapsingTitleScaffold(title = "数据备份与恢复", onBack = onBack, trailing = {
        IconButton(onClick = { showHelp = true }) { Icon(Icons.Filled.HelpOutline, contentDescription = "帮助") }
    }) {
            Text("本地", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(AppSpacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp, vertical = AppSpacing.sm)) {
                Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Download/$storePath", style = MaterialTheme.typography.bodyLarge)
                    Text("备份、模板与导出的账单均保存到这里", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onOpenSettings) { Text("去修改") }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("自动备份", style = MaterialTheme.typography.bodyLarge)
                    Text("进入应用首页时自动执行本地备份", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = autoEnabled, onCheckedChange = { viewModel.setAutoBackup(it) })
            }
            Spacer(Modifier.height(AppSpacing.xl))
            Text("设置", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(AppSpacing.xs))
            SettingRow("自动备份的时间间隔", "$interval 小时") { showInterval = true }
            SettingRow("本机名称", deviceName.ifBlank { Build.MODEL }) { deviceInput = deviceName.ifBlank { Build.MODEL }; showDevice = true }
            SettingRow("自动管理备份文件", "保留 $keepCount 份") { showKeep = true }
        Spacer(Modifier.height(AppSpacing.md))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            OutlinedButton(onClick = { viewModel.loadBackupFiles(); showRestoreList = true }, enabled = !busy, modifier = Modifier.weight(1f)) {
                if (busy) CircularProgressIndicator(modifier = Modifier.size(18.dp)) else Text("恢复数据")
            }
            Button(onClick = { viewModel.backupNow() }, enabled = !busy, modifier = Modifier.weight(1f)) {
                Text("立即备份")
            }
        }
    if (showHelp) {
        AlertDialog(onDismissRequest = { showHelp = false }, confirmButton = { TextButton(onClick = { showHelp = false }) { Text("知道了") } }, title = { Text("数据备份与恢复") }, text = {
            Text("「立即备份」将全部数据加密后保存到 Download 下的存储路径（默认 Download/小柚记账），并按「自动管理备份文件」清理过多备份；「恢复数据」从该目录选择 .bk 备份文件即可还原；开启「自动备份」后进入首页时会按设定间隔自动备份。", style = MaterialTheme.typography.bodySmall)
        })
    }
    if (showInterval) {
        AlertDialog(onDismissRequest = { showInterval = false }, confirmButton = { TextButton(onClick = { showInterval = false }) { Text("取消") } }, title = { Text("自动备份的时间间隔") }, text = {
            Column {
                listOf(3, 6, 12, 24, 72).forEach { h ->
                    Text("$h 小时", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clickable { viewModel.setInterval(h); showInterval = false }.padding(vertical = 10.dp))
                }
            }
        })
    }
    if (showKeep) {
        AlertDialog(onDismissRequest = { showKeep = false }, confirmButton = { TextButton(onClick = { showKeep = false }) { Text("取消") } }, title = { Text("自动管理备份文件") }, text = {
            Column {
                listOf(20, 50, 100).forEach { n ->
                    Text("保留 $n 份", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clickable { viewModel.setKeepCount(n); showKeep = false }.padding(vertical = 10.dp))
                }
            }
        })
    }
    if (showDevice) {
        AlertDialog(onDismissRequest = { showDevice = false }, confirmButton = { TextButton(onClick = { viewModel.setDeviceName(deviceInput.trim().ifBlank { Build.MODEL }); showDevice = false }) { Text("确定") } }, dismissButton = { TextButton(onClick = { showDevice = false }) { Text("取消") } }, title = { Text("本机名称") }, text = {
            OutlinedTextField(value = deviceInput, onValueChange = { deviceInput = it }, singleLine = true)
        })
    }
    if (needPassword) {
        AlertDialog(onDismissRequest = { viewModel.dismissNeedPassword() }, confirmButton = { TextButton(enabled = pwd.isNotBlank(), onClick = { viewModel.dismissNeedPassword(); viewModel.confirmPassword(pwd) }) { Text("确定") } }, dismissButton = { TextButton(onClick = { viewModel.dismissNeedPassword() }) { Text("取消") } }, title = { Text("输入备份密码") }, text = {
            Column {
                Text("所选备份文件不是本机生成的本地备份，请输入备份密码。", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(AppSpacing.sm))
                OutlinedTextField(value = pwd, onValueChange = { pwd = it }, label = { Text("密码") }, singleLine = true)
            }
        })
    }
    if (showRestoreList) {
        AlertDialog(onDismissRequest = { showRestoreList = false }, confirmButton = { TextButton(onClick = { showRestoreList = false }) { Text("取消") } }, title = { Text("选择备份文件") }, text = {
            if (backupFiles.isEmpty()) Text("Download/$storePath 中没有备份文件，请先「立即备份」。", style = MaterialTheme.typography.bodySmall)
            else Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                backupFiles.forEach { f ->
                    Text(f.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().clickable { showRestoreList = false; viewModel.restoreFile(f) }.padding(vertical = 10.dp))
                }
            }
        })
    }
    message?.let {
        AlertDialog(onDismissRequest = viewModel::clearMessage, confirmButton = { TextButton(onClick = viewModel::clearMessage) { Text("确定") } }, title = { Text("结果") }, text = { Text(it) })
    }
    }
}
@Composable
private fun SettingRow(label: String, value: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = AppSpacing.md)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
