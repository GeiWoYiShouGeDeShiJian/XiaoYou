package com.example.jizhangruanjian.ui.theme
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
@Composable
fun MoneyBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: ThemeColor = ThemeColor.ORANGE, // 3 套预设主题色（设置页可切换）
    dynamicColor: Boolean = false, // P15 默认关闭，改为设置页可选项
    ledgerColor: Color? = null, // 账本个性化 color 接入点（P15 顶栏随账本变色）
    content: @Composable () -> Unit
) {
    val scheme = when (themeColor) {
        ThemeColor.ORANGE -> if (darkTheme) OrangeDark else OrangeLight
        ThemeColor.BLUE -> if (darkTheme) BlueDark else BlueLight
        ThemeColor.MINT -> if (darkTheme) MintDark else MintLight
    }
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else scheme
    MaterialTheme(
        colorScheme = if (ledgerColor != null) colorScheme.copy(primary = ledgerColor) else colorScheme,
        typography = Typography,
        shapes = Shapes(
            small = RoundedCornerShape(AppRadius.small),
            medium = RoundedCornerShape(AppRadius.medium),
            large = RoundedCornerShape(AppRadius.large),
            extraLarge = RoundedCornerShape(AppRadius.extraLarge)
        ),
        content = content
    )
}