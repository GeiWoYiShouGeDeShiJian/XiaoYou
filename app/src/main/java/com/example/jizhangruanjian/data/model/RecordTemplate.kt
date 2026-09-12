package com.example.jizhangruanjian.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey
// 快捷记账模板：保存当前记账表单快照，点击即可套用
@Entity(tableName = "record_template")
data class RecordTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val amountCents: Long,
    val categoryId: Long,
    val accountId: Long,
    val toAccountId: Long,
    val note: String,
    val memberIds: String,
    val merchant: String,
    val feeCents: Long = 0,
    val currency: String = "CNY",
    val discountCents: Long = 0,
    val tagIds: String = "",
    val paymentUnpaid: Boolean = false,
    val reimbursable: Boolean = false,
    val includeBudget: Boolean = true,
    val includeSummary: Boolean = true,
    val quickSave: Boolean = false
)
