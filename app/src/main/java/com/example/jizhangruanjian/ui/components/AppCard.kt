package com.example.jizhangruanjian.ui.components
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.example.jizhangruanjian.ui.theme.AppRadius
import com.example.jizhangruanjian.ui.theme.AppSpacing
import com.example.jizhangruanjian.ui.theme.MoneyBookTheme
// 统一卡片：基于 M3 Card，颜色/圆角/阴影全部走主题与 token
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AppRadius.medium),
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(AppSpacing.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        contentPadding = contentPadding
    ) { content() }
}
@Composable
fun AppCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AppRadius.medium),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(AppSpacing.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        contentPadding = contentPadding
    ) { content() }
}
@Preview(showBackground = true)
@Composable
private fun AppCardPreview() {
    MoneyBookTheme {
        Box(modifier = Modifier.padding(AppSpacing.lg)) {
            AppCard(onClick = {}) { Text("可点击卡片") }
        }
    }
}