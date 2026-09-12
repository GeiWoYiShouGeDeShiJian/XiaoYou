package com.example.jizhangruanjian.domain.model
import com.example.jizhangruanjian.data.model.BudgetPeriod
// P8 预算领域模型
data class BudgetDomain(
    val id: Long,
    val ledgerId: Long,
    val categoryId: Long? = null,
    val period: BudgetPeriod,
    val amount: Long,
    val startDate: Long,
    val rolloverMode: Int = 1
)