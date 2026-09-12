package com.example.jizhangruanjian.ui.theme
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
// ═══ 设计令牌 · 数值依据（全项目 app/src/main 全量 .kt 扫描）═══
// 间距（.dp 全局出现次数，top25）：8=282 / 12=240 / 16=204 / 4=118 / 10=105
//   / 6=88 / 20=87 / 14=68 / 24=67 / 18=34 / 2=26 / 32=20 / 40=16 / 52=17
// 圆角（RoundedCornerShape(x.dp) 出现次数）：12=35 / 8=16 / 16=14 / 24=13
//   / 10=10 / 14=10 / 20=8 / 6=7
// 尺寸：size(…)=20 高频；按钮与顶栏均为 height(52.dp)（Tag/Role/Merchant/Record 保存钮、
//   AccountSort/CustomizeHome/CollapsingTitleScaffold 顶栏行），故 buttonHeight=topBarHeight=52
object AppSpacing {
    val xs = 4.dp   // 出现 118 次
    val sm = 8.dp   // 出现 282 次（最高频）
    val md = 12.dp  // 出现 240 次
    val lg = 16.dp  // 出现 204 次
    val xl = 20.dp  // 出现 87 次
    val xxl = 24.dp // 出现 67 次
    val xxxl = 32.dp // 出现 20 次
}
object AppRadius {
    val small = 8.dp       // shape 内出现 16 次
    val medium = 12.dp     // shape 内出现 35 次（最高频）
    val large = 16.dp      // shape 内出现 14 次
    val extraLarge = 20.dp // shape 内出现 8 次
}
object AppSize {
    val buttonHeight = 52.dp // 全项目保存/主按钮统一高度 height(52.dp)
    val topBarHeight = 52.dp // 自绘顶栏 + CollapsingTitleScaffold 统一高度
    val iconSmall = 16.dp    // size(16) 出现 15 次
    val iconSize = 20.dp     // size(20) 出现 16 次，Icon 常用
    val iconLarge = 24.dp    // size(24) 出现 9 次；全局 24=67 次
    val iconXLarge = 32.dp   // 列表项类别图标 size(32)（SearchScreen 使用）
    val cardMinHeight = 48.dp // 无强高频依据，取 M3 常规最小触摸高度，按需覆盖
}