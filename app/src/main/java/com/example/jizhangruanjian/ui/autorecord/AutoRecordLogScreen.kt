package com.example.jizhangruanjian.ui.autorecord
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun AutoRecordLogScreen(onBack: () -> Unit, viewModel: AutoRecordLogViewModel = hiltViewModel()) {
    val logs by viewModel.logs.collectAsState()
    LaunchedEffect(viewModel) { viewModel.load() }
    Scaffold(topBar = {
        AppTopBar(title = "识别日志", onBack = onBack, actions = {
            if (logs.isNotEmpty()) TextButton(onClick = { viewModel.clear() }) { Text("清空") }
        })
    }) { padding ->
        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无识别记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(logs) { entry ->
                    Text(entry, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm))
                }
            }
        }
    }
}
