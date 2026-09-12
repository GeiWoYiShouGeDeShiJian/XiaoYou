package com.example.jizhangruanjian.ui.theme
import androidx.compose.ui.graphics.Color
// ═══ 业务数据语义色 · 统一入口 ═══
// 这些色值不随主题变化，属于"数据/品牌/展示"固定色，从各页面硬编码收编至此（仅搬家，不改配色）：
//   CategoryColors ← CategoryIcon.kt      （17 支出 + 5 收入大类专属色，按路径分组号 01~17 映射）
//   AccountPresetColors ← AddAccountScreen.kt（账户预设 + 自定义类型色）
//   ImportBrandColors ← CsvScreen.kt      （微信/支付宝/通用模板品牌色）
//   NeutralColors ← TrashScreen.kt        （回收站中性灰阶）
//   ChartAuxColors ← AssetReportScreen.kt （报表图表辅助灰阶，0xFF64B5F6 已在 Color.kt.ChartPalette，未重复收）
//   OverlayColors ← HomeScreen / AccountTabScreen / CustomizeHomeScreen / LedgerScreens（封面字色 0xFF1A1A1A ×3、半透明黑遮罩）
//   TypeBadgeColors ← RecordScreen.kt     （转账/支出类型徽标底色）
//   BalanceCardColors ← AccountDetailScreen.kt（余额渐变卡专属色）
//   MonthDetailColors ← MonthDetailScreen.kt  （月度详情强调蓝）
//   KeypadColors ← NumericKeypad.kt       （计算器键盘按钮色板）
object CategoryColors {
    val expense01 = Color(0xFFE53935) // 支出分组 01
    val expense02 = Color(0xFFFF9800) // 支出分组 02
    val expense03 = Color(0xFFF06292) // 支出分组 03
    val expense04 = Color(0xFF1E88E5) // 支出分组 04
    val expense05 = Color(0xFF8E24AA) // 支出分组 05
    val expense06 = Color(0xFF00ACC1) // 支出分组 06
    val expense07 = Color(0xFFFF7043) // 支出分组 07
    val expense08 = Color(0xFF43A047) // 支出分组 08
    val expense09 = Color(0xFF3949AB) // 支出分组 09
    val expense10 = Color(0xFFD81B60) // 支出分组 10
    val expense11 = Color(0xFFF9A825) // 支出分组 11
    val expense12 = Color(0xFF5C6BC0) // 支出分组 12
    val expense13 = Color(0xFF26A69A) // 支出分组 13
    val expense14 = Color(0xFF78909C) // 支出分组 14
    val expense15 = Color(0xFFFBC02D) // 支出分组 15
    val expense16 = Color(0xFF8D6E63) // 支出分组 16
    val expense17 = Color(0xFF66BB6A) // 支出分组 17
    val income01 = Color(0xFF2E7D32) // 收入分组 01
    val income02 = Color(0xFF0288D1) // 收入分组 02
    val income03 = Color(0xFF00897B) // 收入分组 03
    val income04 = Color(0xFFF9A825) // 收入分组 04
    val income05 = Color(0xFF7B1FA2) // 收入分组 05
}
object AccountPresetColors {
    val cash = Color(0xFF2196F3)    // 现金
    val saving = Color(0xFF4CAF50)  // 储蓄卡
    val credit = Color(0xFFFF9800)  // 信用账户
    val online = Color(0xFFE91E63)  // 网络账户
    val invest = Color(0xFF9C27B0)  // 投资账户
    val stored = Color(0xFFF44336)  // 储值卡
    val virtual = Color(0xFF00BCD4) // 虚拟账户
    val loan = Color(0xFF009688)    // 借贷
    val custom = Color(0xFF607D8B)  // 自定义类型
}
object ImportBrandColors {
    val wechat = Color(0xFF1AAD19)    // 微信支付账单品牌绿
    val alipay = Color(0xFF1677FF)    // 支付宝账单品牌蓝
    val bankTemplate = Color(0xFF1D6F42) // 通用模板/银行绿
}
object NeutralColors {
    val textPrimary = Color(0xFF333333)   // 正文/主图标/复选框勾选
    val textSecondary = Color(0xFF666666) // 次级文字/描边按钮内容
    val textMuted = Color(0xFF9AA0A6)     // 弱提示文字
    val textDisabled = Color(0xFFBBBBBB)  // 禁用态文字
    val surfaceMuted = Color(0xFFF3F4F6)  // 卡片背景（CARD_BG）
}
object ChartAuxColors {
    val pieOther = Color(0xFFC9CDD4)   // 饼图"其他"饼块
    val ringTrack = Color(0xFFE0E0E0)  // 环形进度底轨
    val textAxis = Color(0xFF505866)   // 坐标轴数值文字
    val textTick = Color(0xFF9AA3AE)   // 刻度数字
    val grid = Color(0xFFDDE3EA)       // 横向网格线
    val verticalGrid = Color(0xFF90A4AE) // 竖网格线
    val textMonth = Color(0xFF37474F)  // 月份标注文字
}
object OverlayColors {
    val coverText = Color(0xFF1A1A1A)      // 封面深色主导文字（Home/AccountTab/CustomizeHome 三处重复）
    val highlightScrim = Color(0x66000000) // 半透明黑遮罩（LedgerScreens 标签文字底）
}
object TypeBadgeColors {
    val transfer = Color(0xFFE8E0F2) // 转账类型徽标底
    val expense = Color(0xFFF2E2D4)  // 支出类型徽标底
}
object BalanceCardColors {
    val gradientStart = Color(0xFFBBD7F2) // 余额卡渐变起色
    val gradientEnd = Color(0xFF9EC2EA)   // 余额卡渐变止色
    val label = Color(0xFF2C4E70)         // 余额卡标签文字
    val amount = Color(0xFF1A3A5C)        // 余额卡金额文字
}
object MonthDetailColors {
    val accentBlue = Color(0xFFB3C7E6) // 月度详情强调蓝
}
object KeypadColors {
    val deleteBg = Color(0xFFF7DCEA) // 删除键底
    val repeatBg = Color(0xFFD8EAF8) // "再记一笔"键底
    val saveBg = Color(0xFFCBDEF0)   // 保存键底
    val saveText = Color(0xFF2B5F8F) // 保存键文字
    val equalBg = Color(0xFF8FBFEF)  // 计算键底
}