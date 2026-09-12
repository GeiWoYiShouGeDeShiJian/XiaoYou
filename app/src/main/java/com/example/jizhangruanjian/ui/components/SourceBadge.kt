package com.example.jizhangruanjian.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.jizhangruanjian.data.model.TransactionSource

@Composable
fun SourceBadge(source: TransactionSource, modifier: Modifier = Modifier) {
    val label = when (source) {
        TransactionSource.VOICE -> "🎤 语音"
        TransactionSource.AUTO_ACCESSIBILITY, TransactionSource.AUTO_NOTIFICATION, TransactionSource.AUTO_SILENT -> "🤖 自动"
        else -> return
    }
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 6.dp, vertical = 2.dp))
}
