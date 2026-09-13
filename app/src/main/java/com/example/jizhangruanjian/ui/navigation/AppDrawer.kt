package com.example.jizhangruanjian.ui.navigation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jizhangruanjian.R
import com.example.jizhangruanjian.ui.theme.AppSpacing
private data class DrawerEntry(val route: String, val icon: ImageVector, val label: String)
private val FUNCTION_ITEMS = listOf(
    DrawerEntry(DrawerRoutes.AUTO_BOOKKEEPING, Icons.Filled.Bolt, "自动记账"),
    DrawerEntry(DrawerRoutes.RECURRING, Icons.Filled.DateRange, "周期记账"),
    DrawerEntry(DrawerRoutes.REIMBURSEMENT, Icons.Filled.ReceiptLong, "报销管理"),
    DrawerEntry(DrawerRoutes.AR_AP, Icons.Filled.SwapHoriz, "应收应付"),
    DrawerEntry(DrawerRoutes.SETTINGS, Icons.Filled.Settings, "设置"))
private val DATA_ITEMS = listOf(
    DrawerEntry(DrawerRoutes.BACKUP, Icons.Filled.Backup, "数据备份与恢复"),
    DrawerEntry(DrawerRoutes.CSV, Icons.Filled.FileOpen, "账单导入与导出"),
    DrawerEntry(DrawerRoutes.TRASH, Icons.Filled.DeleteOutline, "回收站"))
@Composable
fun AppDrawerContent(onNavigate: (String) -> Unit, onDismiss: () -> Unit = {}) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(vertical = AppSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)) {
                Icon(painterResource(R.drawable.ic_logo), contentDescription = "柚子记账", modifier = Modifier.size(34.dp), tint = Color.Unspecified)
                Spacer(Modifier.width(10.dp))
                Text("小柚记账", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(AppSpacing.sm))
            SectionGroup(null, FUNCTION_ITEMS, onNavigate)
            HorizontalDivider()
            SectionGroup("数据", DATA_ITEMS, onNavigate)
        }
    }
}
@Composable
private fun SectionGroup(title: String?, items: List<DrawerEntry>, onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm))
        items.forEach { DrawerItem(it, onNavigate) }
    }
}
@Composable
private fun DrawerItem(entry: DrawerEntry, onNavigate: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onNavigate(entry.route) }.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)) {
        Icon(entry.icon, contentDescription = entry.label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(AppSpacing.lg))
        Text(entry.label, style = MaterialTheme.typography.bodyLarge)
    }
}