package com.example.jizhangruanjian.ui.budget
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.domain.model.BudgetState
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.example.jizhangruanjian.ui.components.CategoryIcon
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
@Composable
fun BudgetDetailScreen(viewModel: BudgetDetailViewModel = hiltViewModel(), onBack: () -> Unit, onOpenManage: () -> Unit, onEditTransaction: (Long) -> Unit) {
    val budget by viewModel.budget.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val context = LocalContext.current
    val today = LocalDate.now()
    val month = YearMonth.now()
    val zone = ZoneId.systemDefault()
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
            Text("本月预算", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        val b = budget
        if (b == null) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("未设置本月预算", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onOpenManage) { Text("去创建预算") }
            }
            return@Column
        }
        val daysInMonth = month.lengthOfMonth()
        val dayOfMonth = today.dayOfMonth
        val dailyQuota = b.remaining.coerceAtLeast(0) / (daysInMonth - dayOfMonth + 1)
        val todayExpense = groups.firstOrNull { it.date == today }?.total ?: 0L
        val quotaLeft = (dailyQuota - todayExpense).coerceAtLeast(0)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
            Text("%02d.%02d - %02d.%02d".format(month.monthValue, 1, month.monthValue, daysInMonth), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text("预算剩余", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(Formatters.yuanText(b.remaining), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = if (b.remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp))
            Text("今日额度 ${Formatters.yuanText(dailyQuota)}，剩余 ${Formatters.yuanText(quotaLeft)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(20.dp))
            BoxWithProgress(b.usagePercent, dayOfMonth, daysInMonth)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton(Icons.Filled.Edit, "编辑", Modifier.weight(1f)) { onOpenManage() }
                ActionButton(Icons.Filled.SwapHoriz, "转移", Modifier.weight(1f)) { Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show() }
                ActionButton(Icons.Filled.PieChart, "报表", Modifier.weight(1f)) { Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show() }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            StatCard("总预算", Formatters.yuanText(b.available), "日均 ${Formatters.yuanText(b.available / daysInMonth)}", Modifier.weight(1f))
            StatCard("已支出", Formatters.yuanText(b.spent), "日均 ${Formatters.yuanText(b.spent / dayOfMonth)}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        val totalCount = groups.sumOf { it.items.size }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text("明细列表", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("共${totalCount}笔 时间最新优先", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp)) {
            if (groups.isEmpty()) {
                item { Text("本月暂无支出", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)) }
            }
            groups.forEach { g ->
                item(key = "head_${g.date}") {
                    val weekCn = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")[g.date.dayOfWeek.value % 7]
                    val headText = if (g.date == today) "今天 $weekCn" else "${g.date.monthValue}月${g.date.dayOfMonth}日 $weekCn"
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(headText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("支 ${Formatters.yuanText(g.total)}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                    }
                }
                items(g.items, key = { it.id }) { t -> BudgetTxRow(t, onClick = { onEditTransaction(t.id) }) }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
@Composable
private fun BoxWithProgress(usagePercent: Float, dayOfMonth: Int, daysInMonth: Int) {
    val barColor = if (usagePercent >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val todayFraction = (dayOfMonth.toFloat() / daysInMonth).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("剩余%.2f%%".format(((1f - usagePercent).coerceIn(0f, 1f)) * 100), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.End))
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxWidth(usagePercent.coerceIn(0f, 1f)).height(14.dp).clip(RoundedCornerShape(7.dp)).background(barColor))
            Box(modifier = Modifier.matchParentSize()) {
                Box(modifier = Modifier.fillMaxWidth(todayFraction).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                    Box(modifier = Modifier.width(1.5.dp).height(22.dp).background(MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth(todayFraction), contentAlignment = Alignment.CenterEnd) {
                Text("今天", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
@Composable
private fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label)
    }
}
@Composable
private fun StatCard(title: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
private fun BudgetTxRow(t: TransactionDisplay, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp)) {
        CategoryIcon(t.categoryIcon, size = 36.dp, fontSize = 16.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(t.note.ifBlank { t.categoryName }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(Formatters.timeHM(t.tradeDate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("-${Formatters.yuanText(t.amount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
    }
}
