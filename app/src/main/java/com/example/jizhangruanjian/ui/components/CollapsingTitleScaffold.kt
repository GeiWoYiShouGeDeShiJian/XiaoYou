package com.example.jizhangruanjian.ui.components
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jizhangruanjian.ui.theme.AppSpacing
// 可滚动设置页统一骨架：顶部大标题，页面向下滑动后标题缩到返回键旁并常驻
@Composable
fun CollapsingTitleScaffold(title: String, onBack: () -> Unit, trailing: @Composable RowScope.() -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    val scroll = rememberScrollState()
    val threshold = with(LocalDensity.current) { 64.dp.roundToPx() }
    val collapsed by remember { derivedStateOf { scroll.value > threshold } }
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                AnimatedVisibility(visible = collapsed, enter = slideInHorizontally(snap()) { -it / 2 } + fadeIn(snap()), exit = slideOutHorizontally(snap()) { -it / 2 } + fadeOut(snap())) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                trailing()
            }
            Column(Modifier.weight(1f).verticalScroll(scroll)) {
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm))
                content()
            }
        }
    }
}
