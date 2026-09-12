package com.example.jizhangruanjian.domain.model
// P8 预算使用状态（R6 已纳入上月结余）
data class BudgetState(
    val budget: BudgetDomain,
    val spent: Long,
    val prevSpent: Long,
    val available: Long,
    val remaining: Long,
    val usagePercent: Float,
    val over: Boolean
) {
    val isTotal: Boolean get() = budget.categoryId == null
}