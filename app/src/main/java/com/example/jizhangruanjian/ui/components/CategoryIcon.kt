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
import com.example.jizhangruanjian.ui.theme.CategoryColors
// 支出 17 大类专属强调色，按路径分组号取色；字形用该色着色，无底框
private val EXPENSE_GROUP_COLORS = mapOf(
    "01" to CategoryColors.expense01, "02" to CategoryColors.expense02, "03" to CategoryColors.expense03,
    "04" to CategoryColors.expense04, "05" to CategoryColors.expense05, "06" to CategoryColors.expense06,
    "07" to CategoryColors.expense07, "08" to CategoryColors.expense08, "09" to CategoryColors.expense09,
    "10" to CategoryColors.expense10, "11" to CategoryColors.expense11, "12" to CategoryColors.expense12,
    "13" to CategoryColors.expense13, "14" to CategoryColors.expense14, "15" to CategoryColors.expense15,
    "16" to CategoryColors.expense16, "17" to CategoryColors.expense17
)
// 收入 5 大类专属强调色
private val INCOME_GROUP_COLORS = mapOf(
    "01" to CategoryColors.income01, "02" to CategoryColors.income02, "03" to CategoryColors.income03,
    "04" to CategoryColors.income04, "05" to CategoryColors.income05
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
        val model = if (styled) "file:///android_asset/cat/${icon}_fill.svg" else "file:///android_asset/cat/$icon.svg"
        AsyncImage(
            model = model,
            contentDescription = null,
            imageLoader = rememberSvgLoader(),
            contentScale = ContentScale.Fit,
            colorFilter = if (color != null) ColorFilter.tint(color.copy(alpha = 0.75f)) else null,
            modifier = modifier.size(size)
        )
    } else {
        Text(icon, fontSize = fontSize, maxLines = 1, modifier = modifier)
    }
}