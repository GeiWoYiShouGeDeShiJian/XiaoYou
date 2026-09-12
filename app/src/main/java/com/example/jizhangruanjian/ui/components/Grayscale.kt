package com.example.jizhangruanjian.ui.components
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
private val grayPaint = Paint().apply { colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
// 灰度滤镜：内容整体黑白（用于报销/转账等中性显示）
fun Modifier.grayscale(): Modifier = drawWithContent {
    drawIntoCanvas { canvas ->
        canvas.saveLayer(Rect(0f, 0f, size.width, size.height), grayPaint)
        drawContent()
        canvas.restore()
    }
}
