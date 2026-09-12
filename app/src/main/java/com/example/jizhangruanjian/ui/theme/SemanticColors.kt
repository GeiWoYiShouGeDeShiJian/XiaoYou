package com.example.jizhangruanjian.ui.theme
import androidx.compose.ui.graphics.Color
/**
 * 业务语义色：不同页面共享的固定语义色，统一定义于此，避免各文件散落色值不一致。
 * 不随主题变化（业务/数据语义色），"收入=绿、支出=红、图表主序列=蓝"。
 */
object SemanticColors {
    val IncomeGreen = Color(0xFF00875A)
    val ExpenseRed = Color(0xFFB3261E)
    val ChartBlue = Color(0xFF1E88E5)
}