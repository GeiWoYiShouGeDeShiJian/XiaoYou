package com.example.jizhangruanjian.ui.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.jizhangruanjian.ui.theme.ChartPalette
// P15-S3 首页/统计页共用的环形饼图（Canvas drawArc，扇区间留缝，支持点击高亮）
@Composable
fun DonutChart(amounts: List<Float>, modifier: Modifier = Modifier, colors: List<Color> = ChartPalette, strokeDp: Float = 30f, center: @Composable () -> Unit = {}) {
    val total = amounts.sum().coerceAtLeast(0.0001f)
    var selected by remember { mutableIntStateOf(-1) }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize().pointerInput(amounts) {
            detectTapGestures { tap ->
                val c = Offset(size.width / 2f, size.height / 2f)
                val dx = tap.x - c.x
                val dy = tap.y - c.y
                val maxR = size.width / 2f
                selected = -1
                if (dx * dx + dy * dy <= maxR * maxR) {
                    var deg = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                    while (deg < 0f) deg += 360f
                    deg %= 360f
                    var start = 0f
                    amounts.forEachIndexed { i, amt ->
                        val sweep = amt * 360f / total
                        if (deg >= start && deg < start + sweep) { selected = i; return@forEachIndexed }
                        start += sweep
                    }
                }
            }
        }) {
            val stroke = strokeDp.dp.toPx()
            var start = -90f
            amounts.forEachIndexed { i, amt ->
                val raw = amt * 360f / total
                val sweep = if (i == amounts.lastIndex) raw else (raw - 3f)
                val alpha = if (selected >= 0 && selected != i) 0.35f else 1f
                drawArc(color = colors[i % colors.size].copy(alpha = alpha), startAngle = start, sweepAngle = sweep, useCenter = false, style = Stroke(width = stroke, cap = StrokeCap.Butt))
                start += raw
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) { center() }
    }
}