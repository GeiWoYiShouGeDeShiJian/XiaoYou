package com.example.jizhangruanjian.ui.components
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
// 支出 17 大类专属强调色，按路径分组号取色；字形用该色着色，无底框
private val EXPENSE_GROUP_COLORS = mapOf(
    "01" to Color(0xFFE53935), "02" to Color(0xFFFF9800), "03" to Color(0xFFF06292),
    "04" to Color(0xFF1E88E5), "05" to Color(0xFF8E24AA), "06" to Color(0xFF00ACC1),
    "07" to Color(0xFFFF7043), "08" to Color(0xFF43A047), "09" to Color(0xFF3949AB),
    "10" to Color(0xFFD81B60), "11" to Color(0xFFF9A825), "12" to Color(0xFF5C6BC0),
    "13" to Color(0xFF26A69A), "14" to Color(0xFF78909C), "15" to Color(0xFFFBC02D),
    "16" to Color(0xFF8D6E63), "17" to Color(0xFF66BB6A)
)
// 收入 5 大类专属强调色
private val INCOME_GROUP_COLORS = mapOf(
    "01" to Color(0xFF2E7D32), "02" to Color(0xFF0288D1), "03" to Color(0xFF00897B),
    "04" to Color(0xFFF9A825), "05" to Color(0xFF7B1FA2)
)
private var svgLoader: ImageLoader? = null
@Composable
private fun rememberSvgLoader(): ImageLoader {
    val context = LocalContext.current
    return remember { svgLoader ?: ImageLoader.Builder(context.applicationContext).components { add(SvgDecoder.Factory()) }.build().also { svgLoader = it } }
}
// 类别图标：expense/income 双样式(选中_fill/未选_line) + 大类专属色着色，无底框；其余类型单文件；emoji 回退纯文本
@Composable
fun CategoryIcon(
    icon: String,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    container: Color? = null,
    fallbackContainer: Color = Color.Transparent,
    selected: Boolean = false,
    unselectedTint: Color? = null
) {
    if (icon.contains('/')) {
        val parts = icon.split('/')
        val base = when (parts[0]) {
            "expense" -> EXPENSE_GROUP_COLORS[parts.getOrNull(1)]
            "income" -> INCOME_GROUP_COLORS[parts.getOrNull(1)]
            else -> null
        }
        val color = if (base != null && !selected && unselectedTint != null) unselectedTint else base
        val styled = color != null
        val model = if (styled) "file:///android_asset/cat/${icon}_${if (selected) "fill" else "line"}.svg" else "file:///android_asset/cat/$icon.svg"
        AsyncImage(
            model = model,
            contentDescription = null,
            imageLoader = rememberSvgLoader(),
            contentScale = ContentScale.Fit,
            colorFilter = if (color != null) ColorFilter.tint(color) else null,
            modifier = modifier.size(size)
        )
    } else {
        Text(icon, fontSize = fontSize, maxLines = 1, modifier = modifier)
    }
}