package com.example.jizhangruanjian.data.model
enum class AccountType { CASH, BANK, ALIPAY, WECHAT, OTHER }
enum class CategoryType { INCOME, EXPENSE, TRANSFER, LOAN }
enum class TransactionType { INCOME, EXPENSE, TRANSFER, LOAN }
enum class BudgetPeriod { MONTHLY, YEARLY }
enum class Frequency { DAILY, WEEKLY, MONTHLY, YEARLY }
enum class PaymentStatus { PAID, UNPAID }
enum class ReimbursementStatus { NONE, REIMBURSABLE, REIMBURSED }
enum class RefundStatus { NONE, HAS_REFUND }
enum class LoanDirection { OUT, IN }
enum class TransactionSource { MANUAL, VOICE, AUTO_ACCESSIBILITY, AUTO_NOTIFICATION, AUTO_SILENT }
