package com.example.jizhangruanjian.domain.model
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.model.PaymentStatus
import com.example.jizhangruanjian.data.model.ReimbursementStatus
import com.example.jizhangruanjian.data.model.RefundStatus
import com.example.jizhangruanjian.data.model.LoanDirection
import com.example.jizhangruanjian.data.model.TransactionSource
data class TransactionDisplay(
    val id: Long,
    val ledgerId: Long,
    val accountId: Long,
    val categoryId: Long,
    val type: TransactionType,
    val amount: Long,
    val note: String,
    val tradeDate: Long,
    val includeInSummary: Boolean,
    val accountName: String,
    val categoryName: String,
    val categoryIcon: String,
    val toAccountId: Long? = null,
    val toAccountName: String = "",
    val merchant: String? = null,
    val paymentStatus: PaymentStatus? = null,
    val reimbursementStatus: ReimbursementStatus? = null,
    val refundStatus: RefundStatus? = null,
    val memberId: Long? = null,
    val memberName: String = "",
    val memberIdList: List<Long> = emptyList(),
    val memberNames: String = "",
    val loanDirection: LoanDirection? = null,
    val images: List<String> = emptyList(),
    val discount: Long = 0,
    val locationName: String? = null,
    val currency: String = "CNY",
    val includeInBudget: Boolean = true,
    val source: TransactionSource = TransactionSource.MANUAL
)