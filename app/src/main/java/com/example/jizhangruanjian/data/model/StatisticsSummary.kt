package com.example.jizhangruanjian.data.model
// P5 统计：区间粒度/区间类型
enum class Granularity { DAY, WEEK, MONTH }
enum class PeriodType { WEEK, MONTH, YEAR, CUSTOM }
// P5 一次统计查询所需区间（本期 + 上期）+ 聚合粒度
data class StatisticsRange(
    val from: Long,
    val to: Long,
    val prevFrom: Long,
    val prevTo: Long,
    val granularity: Granularity
)
// P5 区间内某天支出明细（原始行，供代码内按粒度聚合）
data class AmountByDate(val tradeDate: Long, val amount: Long)
// P5 趋势折线的一个桶
data class BucketTotal(val label: String, val total: Long)
// P5 统计汇总
data class StatisticsSummary(
    val income: Long,
    val expense: Long,
    val prevExpense: Long,
    val categoryTotals: List<CategoryTotal>,
    val currentSeries: List<BucketTotal>,
    val prevSeries: List<BucketTotal>
) {
    val changePercent: Float?
        get() = if (prevExpense == 0L) null else (expense - prevExpense) * 100f / prevExpense
}