package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = Ledger::class, parentColumns = ["id"], childColumns = ["ledger_id"]),
        ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["account_id"]),
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["category_id"]),
        ForeignKey(entity = Member::class, parentColumns = ["id"], childColumns = ["member_id"])
    ],
    indices = [
        Index(value = ["ledger_id", "trade_date"]),
        Index(value = ["account_id", "trade_date"]),
        Index("deleted_at"),
        Index("dedup_hash"),
        Index("category_id"),
        Index("member_id")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "ledger_id") val ledgerId: Long,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "to_account_id") val toAccountId: Long? = null, // P7 转账目标账户
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val type: TransactionType,
    val amount: Long,
    val note: String,
    @ColumnInfo(name = "trade_date") val tradeDate: Long,
    @ColumnInfo(name = "location_lat") val locationLat: Double? = null,
    @ColumnInfo(name = "location_lng") val locationLng: Double? = null,
    @ColumnInfo(name = "location_name") val locationName: String? = null,
    val currency: String = "CNY",
    @ColumnInfo(name = "discount") val discount: Long = 0,
    @ColumnInfo(name = "fee", defaultValue = "0") val fee: Long = 0, // 转账手续费(正)/补贴(负)，分
    @ColumnInfo(name = "fee_payer", defaultValue = "NONE") val feePayer: String = "NONE", // SELF=转出方承担 OTHER=到账方承担
    @ColumnInfo(name = "due_date") val dueDate: Long? = null, // 借贷还款日
    @ColumnInfo(name = "reimburse_link_id") val reimburseLinkId: Long = 0, // 报销关联的报销款收入 id
    @ColumnInfo(name = "include_in_budget") val includeInBudget: Boolean = true, // 是否计入预算
    @ColumnInfo(name = "refund_amount") val refundAmount: Long = 0, // 退款金额（分）
    @ColumnInfo(name = "refund_account_id") val refundAccountId: Long? = null, // 退款转入账户
    @ColumnInfo(name = "refund_date") val refundDate: Long? = null, // 退款时间
    @ColumnInfo(name = "refund_note") val refundNote: String? = null, // 退款备注
    @ColumnInfo(name = "include_in_summary") val includeInSummary: Boolean = true,
    @ColumnInfo(name = "is_recurring_generated") val isRecurringGenerated: Boolean = false,
    @ColumnInfo(name = "dedup_hash") val dedupHash: String? = null,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    val merchant: String? = null,
    @ColumnInfo(name = "payment_status") val paymentStatus: PaymentStatus? = null,
    @ColumnInfo(name = "reimbursement_status") val reimbursementStatus: ReimbursementStatus? = null,
    @ColumnInfo(name = "refund_status") val refundStatus: RefundStatus? = null,
    @ColumnInfo(name = "member_id") val memberId: Long? = null,
    @ColumnInfo(name = "member_ids") val memberIds: String? = null, // 多选成员 id，逗号分隔
    @ColumnInfo(name = "loan_direction") val loanDirection: LoanDirection? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "source", defaultValue = "MANUAL") val source: TransactionSource = TransactionSource.MANUAL // 交易来源（手动/语音/自动记账）
)
