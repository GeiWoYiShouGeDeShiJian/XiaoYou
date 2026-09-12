package com.example.jizhangruanjian.domain.model
import com.example.jizhangruanjian.data.model.Frequency
import com.example.jizhangruanjian.data.model.TransactionType
// P9 周期记账模板领域模型
data class RecurringTemplateDomain(
    val id: Long,
    val ledgerId: Long,
    val accountId: Long,
    val categoryId: Long,
    val type: TransactionType,
    val amount: Long,
    val note: String,
    val frequency: Frequency,
    val interval: Int,
    val nextExecuteDate: Long,
    val endDate: Long? = null
)