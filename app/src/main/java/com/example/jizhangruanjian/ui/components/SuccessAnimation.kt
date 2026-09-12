package com.example.jizhangruanjian.ui.components
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
@Composable
fun SuccessCheckmark(visible: Boolean, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val scale by animateFloatAsState(targetValue = if (visible) 1f else 0.3f, animationSpec = tween(300), label = "scale")
    val progress by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(600), label = "check")
    val pm = remember { PathMeasure() }
    Canvas(modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        drawCircle(color = primary, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
        val path = Path().apply {
            moveTo(size.width * 0.24f, size.height * 0.50f)
            lineTo(size.width * 0.44f, size.height * 0.70f)
            lineTo(size.width * 0.78f, size.height * 0.32f)
        }
        pm.setPath(path, false)
        val partial = Path()
        pm.getSegment(0f, pm.length * progress, partial, true)
        drawPath(partial, color = onPrimary, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
    }
}