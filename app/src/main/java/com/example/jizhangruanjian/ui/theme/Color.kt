package com.example.jizhangruanjian.ui.theme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
// 主题色枚举：key 持久化到 app_setting，preview 用于设置页色块预览
enum class ThemeColor(val key: String, val preview: Long) {
    ORANGE("orange", 0xFFFF9800), BLUE("blue", 0xFF2196F3), MINT("mint", 0xFF26A69A)
}
// 图表通用色板（Vico / Canvas 均引用）—— 保留兼容
val ChartPalette = listOf(
    Color(0xFFFFB74D), Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFBA68C8),
    Color(0xFFFFD54F), Color(0xFFA1887F), Color(0xFF4DD0E1), Color(0xFFF06292)
)

// ── 报表页专用色板 ──
// 品牌蓝（从截图提取的主色，作为唯一品牌强调色）
val ReportBrandColor = Color(0xFF4A7CFF)
// 分类图例低饱和色板（最多 6 个分类 + 灰色"其他"），禁止高饱和/多色
val ReportCategoryPalette = listOf(
    Color(0xFF5B9BD5), // 蓝
    Color(0xFF70AD47), // 绿
    Color(0xFFED7D31), // 橙
    Color(0xFF9E7CC3), // 紫
    Color(0xFF4FC3C3), // 青
    Color(0xFFF4B183), // 浅橙
    Color(0xFFBFBFBF)  // 灰色（其他）
)
// ===== 方案 A 暖橘（默认，鲨鱼风格）=====
val OrangeLight = lightColorScheme(
    primary = Color(0xFFFF9800), onPrimary = Color(0xFFFFFFFF), primaryContainer = Color(0xFFFFE0B2), onPrimaryContainer = Color(0xFF3E2A00),
    secondary = Color(0xFF8D6E63), onSecondary = Color(0xFFFFFFFF), secondaryContainer = Color(0xFFD7CCC8), onSecondaryContainer = Color(0xFF33231B),
    tertiary = Color(0xFF6D4C41), onTertiary = Color(0xFFFFFFFF), tertiaryContainer = Color(0xFFD7CCC8), onTertiaryContainer = Color(0xFF241813),
    background = Color(0xFFFFFBF0), onBackground = Color(0xFF1C1B1A), surface = Color(0xFFFFFBF0), onSurface = Color(0xFF1C1B1A),
    surfaceVariant = Color(0xFFFFF3E0), onSurfaceVariant = Color(0xFF5F5140), surfaceContainer = Color(0xFFFFF8E1), surfaceContainerLow = Color(0xFFFFF8E1), surfaceContainerHigh = Color(0xFFFFEED1), surfaceContainerHighest = Color(0xFFFFE9C0),
    outline = Color(0xFFBCAAA4), error = Color(0xFFD32F2F)
)
val OrangeDark = darkColorScheme(
    primary = Color(0xFFFFB74D), onPrimary = Color(0xFF3A2500), primaryContainer = Color(0xFF6B3F00), onPrimaryContainer = Color(0xFFFFE0B2),
    secondary = Color(0xFFD7CCC8), onSecondary = Color(0xFF33231B), secondaryContainer = Color(0xFF5D4E4B), onSecondaryContainer = Color(0xFFD7CCC8),
    tertiary = Color(0xFFC8A99B), onTertiary = Color(0xFF40241A), tertiaryContainer = Color(0xFF5A3A30), onTertiaryContainer = Color(0xFFE6CDC2),
    background = Color(0xFF1A1714), onBackground = Color(0xFFEFE8E0), surface = Color(0xFF1A1714), onSurface = Color(0xFFEFE8E0),
    surfaceVariant = Color(0xFF4A4441), onSurfaceVariant = Color(0xFFCAC2B9), surfaceContainer = Color(0xFF211E1B), surfaceContainerLow = Color(0xFF201D1A), surfaceContainerHigh = Color(0xFF2A2622), surfaceContainerHighest = Color(0xFF35312C),
    outline = Color(0xFF948B84), outlineVariant = Color(0xFF4A4441),
    inverseSurface = Color(0xFFEFE8E0), inverseOnSurface = Color(0xFF211E1B), inversePrimary = Color(0xFFE07B00),
    error = Color(0xFFFFB4A9), errorContainer = Color(0xFF5B1A14), onErrorContainer = Color(0xFFFFDAD6), scrim = Color(0xFF000000)
)
// ===== 方案 B 清爽蓝 =====
val BlueLight = lightColorScheme(
    primary = Color(0xFF2196F3), onPrimary = Color(0xFFFFFFFF), primaryContainer = Color(0xFFBBDEFB), onPrimaryContainer = Color(0xFF002171),
    secondary = Color(0xFF607D8B), onSecondary = Color(0xFFFFFFFF), secondaryContainer = Color(0xFFCFD8DC), onSecondaryContainer = Color(0xFF1F2B32),
    tertiary = Color(0xFF5C6BC0), onTertiary = Color(0xFFFFFFFF), tertiaryContainer = Color(0xFFC5CAE9), onTertiaryContainer = Color(0xFF1A237E),
    background = Color(0xFFF5FAFE), onBackground = Color(0xFF131C22), surface = Color(0xFFF5FAFE), onSurface = Color(0xFF131C22),
    surfaceVariant = Color(0xFFE3F2FD), onSurfaceVariant = Color(0xFF42526A), surfaceContainer = Color(0xFFE9F1F9), surfaceContainerLow = Color(0xFFEDF4FB), surfaceContainerHigh = Color(0xFFE3EFF8), surfaceContainerHighest = Color(0xFFD8E9F5),
    outline = Color(0xFF90A4AE), error = Color(0xFFD32F2F)
)
val BlueDark = darkColorScheme(
    primary = Color(0xFF82C8FF), onPrimary = Color(0xFF05386B), primaryContainer = Color(0xFF1763B5), onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFFB0BEC5), onSecondary = Color(0xFF2C363B), secondaryContainer = Color(0xFF465763), onSecondaryContainer = Color(0xFFCFD8DC),
    tertiary = Color(0xFF9FA8DA), onTertiary = Color(0xFF2D3A9E), tertiaryContainer = Color(0xFF424C97), onTertiaryContainer = Color(0xFFC5CAE9),
    background = Color(0xFF0E1419), onBackground = Color(0xFFD7E3EB), surface = Color(0xFF0E1419), onSurface = Color(0xFFD7E3EB),
    surfaceVariant = Color(0xFF3E4A52), onSurfaceVariant = Color(0xFFBFC9D0), surfaceContainer = Color(0xFF182228), surfaceContainerLow = Color(0xFF141E24), surfaceContainerHigh = Color(0xFF212D34), surfaceContainerHighest = Color(0xFF2B363D),
    outline = Color(0xFF7F8C93), outlineVariant = Color(0xFF3E4A52),
    inverseSurface = Color(0xFFD7E3EB), inverseOnSurface = Color(0xFF212D34), inversePrimary = Color(0xFF0B78E0),
    error = Color(0xFFFFB4A9), errorContainer = Color(0xFF5B1A14), onErrorContainer = Color(0xFFFFDAD6), scrim = Color(0xFF000000)
)
// ===== 方案 C 薄荷绿（当前绿色优化版）=====
val MintLight = lightColorScheme(
    primary = Color(0xFF26A69A), onPrimary = Color(0xFFFFFFFF), primaryContainer = Color(0xFFB2DFDB), onPrimaryContainer = Color(0xFF003733),
    secondary = Color(0xFF80CBC4), onSecondary = Color(0xFF0F302D), secondaryContainer = Color(0xFFE0F2F1), onSecondaryContainer = Color(0xFF123C3A),
    tertiary = Color(0xFF66BB6A), onTertiary = Color(0xFFFFFFFF), tertiaryContainer = Color(0xFFC8E6C9), onTertiaryContainer = Color(0xFF0F2E14),
    background = Color(0xFFF0FBF9), onBackground = Color(0xFF10211F), surface = Color(0xFFF0FBF9), onSurface = Color(0xFF10211F),
    surfaceVariant = Color(0xFFE0F2F1), onSurfaceVariant = Color(0xFF3F4B47), surfaceContainer = Color(0xFFE8F5F3), surfaceContainerLow = Color(0xFFECF7F5), surfaceContainerHigh = Color(0xFFDDF0EE), surfaceContainerHighest = Color(0xFFD1EBE8),
    outline = Color(0xFF80CBC4), error = Color(0xFFD32F2F)
)
val MintDark = darkColorScheme(
    primary = Color(0xFF66D9CD), onPrimary = Color(0xFF00332F), primaryContainer = Color(0xFF0E6A61), onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFFA6DFD9), onSecondary = Color(0xFF0F302D), secondaryContainer = Color(0xFF3A5F5B), onSecondaryContainer = Color(0xFFE0F2F1),
    tertiary = Color(0xFF8FD996), onTertiary = Color(0xFF143A19), tertiaryContainer = Color(0xFF3E6A44), onTertiaryContainer = Color(0xFFC8E6C9),
    background = Color(0xFF0E1615), onBackground = Color(0xFFD7E3E1), surface = Color(0xFF0E1615), onSurface = Color(0xFFD7E3E1),
    surfaceVariant = Color(0xFF3E4846), onSurfaceVariant = Color(0xFFC2CCCA), surfaceContainer = Color(0xFF182221), surfaceContainerLow = Color(0xFF141E1D), surfaceContainerHigh = Color(0xFF232D2B), surfaceContainerHighest = Color(0xFF2D3735),
    outline = Color(0xFF8FA39F), outlineVariant = Color(0xFF3E4846),
    inverseSurface = Color(0xFFD7E3E1), inverseOnSurface = Color(0xFF232D2B), inversePrimary = Color(0xFF0E8B80),
    error = Color(0xFFFFB4A9), errorContainer = Color(0xFF5B1A14), onErrorContainer = Color(0xFFFFDAD6), scrim = Color(0xFF000000)
)