package com.example.jizhangruanjian.domain.model
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.model.PaymentStatus
import com.example.jizhangruanjian.data.model.ReimbursementStatus
import com.example.jizhangruanjian.data.model.RefundStatus
import com.example.jizhangruanjian.data.model.LoanDirection
import com.example.jizhangruanjian.data.model.TransactionSource
data class TransactionDomain(
    val id: Long = 0,
    val ledgerId: Long,
    val accountId: Long,
    val toAccountId: Long? = null, // P7 转账目标账户
    val categoryId: Long,
    val type: TransactionType,
    val amount: Long,
    val note: String = "",
    val tradeDate: Long,
    val includeInSummary: Boolean = true,
    val merchant: String? = null,
    val currency: String = "CNY",
    val discount: Long = 0,
    val fee: Long = 0,
    val feePayer: String? = null,
    val dueDate: Long? = null,
    val paymentStatus: PaymentStatus? = null,
    val reimbursementStatus: ReimbursementStatus? = null,
    val refundStatus: RefundStatus? = null,
    val includeInBudget: Boolean = true,
    val refundAmount: Long = 0,
    val refundAccountId: Long? = null,
    val refundDate: Long? = null,
    val refundNote: String? = null,
    val memberId: Long? = null,
    val memberIds: List<Long> = emptyList(),
    val loanDirection: LoanDirection? = null,
    val isRecurringGenerated: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tagIds: List<Long> = emptyList(),
    val source: TransactionSource = TransactionSource.MANUAL
)