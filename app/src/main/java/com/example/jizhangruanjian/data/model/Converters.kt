package com.example.jizhangruanjian.data.model
import androidx.room.TypeConverter
class Converters {
    @TypeConverter fun fromAccountType(v: AccountType): String = v.name
    @TypeConverter fun toAccountType(v: String): AccountType = AccountType.valueOf(v)
    @TypeConverter fun fromCategoryType(v: CategoryType): String = v.name
    @TypeConverter fun toCategoryType(v: String): CategoryType = CategoryType.valueOf(v)
    @TypeConverter fun fromTransactionType(v: TransactionType): String = v.name
    @TypeConverter fun toTransactionType(v: String): TransactionType = TransactionType.valueOf(v)
    @TypeConverter fun fromBudgetPeriod(v: BudgetPeriod): String = v.name
    @TypeConverter fun toBudgetPeriod(v: String): BudgetPeriod = BudgetPeriod.valueOf(v)
    @TypeConverter fun fromFrequency(v: Frequency): String = v.name
    @TypeConverter fun toFrequency(v: String): Frequency = Frequency.valueOf(v)
    @TypeConverter fun fromPaymentStatus(v: PaymentStatus?): String? = v?.name
    @TypeConverter fun toPaymentStatus(v: String?): PaymentStatus? = v?.let { PaymentStatus.valueOf(it) }
    @TypeConverter fun fromReimbursementStatus(v: ReimbursementStatus?): String? = v?.name
    @TypeConverter fun toReimbursementStatus(v: String?): ReimbursementStatus? = v?.let { ReimbursementStatus.valueOf(it) }
    @TypeConverter fun fromRefundStatus(v: RefundStatus?): String? = v?.name
    @TypeConverter fun toRefundStatus(v: String?): RefundStatus? = v?.let { RefundStatus.valueOf(it) }
    @TypeConverter fun fromLoanDirection(v: LoanDirection?): String? = v?.name
    @TypeConverter fun toLoanDirection(v: String?): LoanDirection? = v?.let { LoanDirection.valueOf(it) }
    @TypeConverter fun fromTransactionSource(v: TransactionSource): String = v.name
    @TypeConverter fun toTransactionSource(v: String): TransactionSource = TransactionSource.valueOf(v)
}
