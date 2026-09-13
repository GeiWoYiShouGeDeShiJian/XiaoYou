package com.example.jizhangruanjian.ui.autorecord
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.ui.components.AppTopBar
import com.example.jizhangruanjian.ui.theme.AppSpacing
import java.util.Locale

@Composable
fun AutoRecordIgnoreScreen(onBack: () -> Unit, viewModel: AutoRecordSettingsViewModel = hiltViewModel()) {
    val ignored by viewModel.ignoredList.collectAsState()
    LaunchedEffect(viewModel) { viewModel.loadIgnored() }
    Scaffold(topBar = {
        AppTopBar(title = "忽略名单", onBack = onBack, actions = {
            if (ignored.isNotEmpty()) TextButton(onClick = { viewModel.clearIgnored() }) { Text("清空") }
        })
    }) { padding ->
        if (ignored.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无忽略记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(ignored) { fp ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)) {
                        Text(readable(fp), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removeIgnored(fp) }) { Icon(Icons.Filled.Close, contentDescription = "移除") }
                    }
                }
            }
        }
    }
}
// 指纹可读化：pkg|orderNo 或 pkg|amount|merchant|yyyyMMdd
private fun readable(fp: String): String {
    val parts = fp.split("|", limit = 4)
    val app = when (parts.getOrNull(0)) {
        "com.tencent.mm" -> "微信"
        "com.eg.android.AlipayGphone" -> "支付宝"
        else -> parts.getOrNull(0) ?: fp
    }
    return when {
        parts.size == 2 -> "$app · 单号 ${parts[1]}"
        parts.size >= 4 -> {
            val cents = parts[1].toLongOrNull() ?: 0L
            val amount = String.format(Locale.CHINA, "%.2f", cents / 100.0)
            val date = parts[3]
            val d = if (date.length == 8) "${date.substring(0, 4)}-${date.substring(4, 6)}-${date.substring(6, 8)}" else date
            "$app · ¥$amount ${parts[2]} $d"
        }
        else -> fp
    }
}
