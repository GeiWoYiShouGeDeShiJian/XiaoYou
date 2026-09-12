package com.example.jizhangruanjian.data.model
// P4 首页统计：分类金额汇总（支出占比）
data class CategoryTotal(val categoryId: Long, val total: Long)
// P4 首页汇总数据
data class HomeSummary(
    val income: Long,
    val expense: Long,
    val prevIncome: Long,
    val prevExpense: Long,
    val categoryTotals: List<CategoryTotal>
) {
    val balance: Long get() = income - expense
    val prevBalance: Long get() = prevIncome - prevExpense
    // 环比涨跌百分比（上期为 0 时返回 null 占位）
    val changePercent: Float?
        get() = if (prevBalance == 0L) null else (balance - prevBalance) * 100f / prevBalance
}