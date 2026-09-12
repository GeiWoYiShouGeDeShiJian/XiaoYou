package com.example.jizhangruanjian.core.autorecord.overlay
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jizhangruanjian.core.autorecord.BillCandidate
import com.example.jizhangruanjian.core.autorecord.BillType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillConfirmContent(
    candidate: BillCandidate,
    accountName: String,
    categoryName: String,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(packageLabel(candidate.packageName), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(directionLabel(candidate.billType), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(6.dp))
            Text(candidate.merchant ?: "未知商户", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("¥ ${formatAmount(candidate.amount)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("分类: $categoryName    账户: $accountName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text("时间: ${formatTime(candidate.tradeTime)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onIgnore, modifier = Modifier.weight(1f)) { Text("忽略本笔") }
                Button(onClick = onConfirm, modifier = Modifier.weight(1f)) { Text("记一笔") }
            }
            TextButton(onClick = onEdit, modifier = Modifier.align(Alignment.End)) { Text("编辑") }
        }
    }
}
private fun packageLabel(pkg: String): String = when (pkg) {
    "com.tencent.mm" -> "微信支付"
    "com.eg.android.AlipayGphone" -> "支付宝"
    else -> "支付"
}
private fun directionLabel(type: BillType): String = when (type) {
    BillType.INCOME -> "收入"
    BillType.TRANSFER -> "转账"
    BillType.REFUND -> "退款"
    else -> "支出"
}
private fun formatAmount(amount: Long): String = String.format(Locale.CHINA, "%.2f", amount / 100.0)
private fun formatTime(ts: Long): String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(ts))
