package com.example.jizhangruanjian.core.autorecord
enum class BillType { EXPENSE, INCOME, TRANSFER, REFUND }
data class BillCandidate(
    val packageName: String,
    val amount: Long,
    val merchant: String? = null,
    val orderNo: String? = null,
    val tradeTime: Long = System.currentTimeMillis(),
    val billType: BillType = BillType.EXPENSE,
    val note: String = ""
)
