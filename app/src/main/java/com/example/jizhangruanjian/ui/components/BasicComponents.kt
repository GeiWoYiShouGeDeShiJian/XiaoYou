package com.example.jizhangruanjian.ui.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jizhangruanjian.ui.theme.AppSpacing
@Composable
fun MoneyBookCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
        content()
    }
}
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = modifier)
}
@Composable
fun MoneyBookFab(onClick: () -> Unit, icon: ImageVector, contentDescription: String) {
    FloatingActionButton(onClick = onClick, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
@Composable
fun EmptyState(modifier: Modifier = Modifier, icon: ImageVector, title: String, subtitle: String? = null, ctaText: String? = null, onCta: (() -> Unit)? = null) {
    Column(modifier = modifier.fillMaxWidth().padding(AppSpacing.xl), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        val containerColor = MaterialTheme.colorScheme.primaryContainer
        val iconColor = MaterialTheme.colorScheme.primary
        Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(80.dp)) { drawCircle(color = containerColor, style = androidx.compose.ui.graphics.drawscope.Fill) }
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(44.dp))
        }
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        subtitle?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
        if (ctaText != null && onCta != null) {
            TextButton(onClick = onCta) { Text(ctaText) }
        }
    }
}
@Composable
fun AppLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 36.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
@Composable
fun AppError(icon: ImageVector, title: String, onRetry: () -> Unit, modifier: Modifier = Modifier, subtitle: String? = null) {
    EmptyState(modifier = modifier, icon = icon, title = title, subtitle = subtitle, ctaText = "重试", onCta = onRetry)
}