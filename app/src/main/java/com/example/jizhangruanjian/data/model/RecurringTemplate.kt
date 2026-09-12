package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "recurring_template",
    foreignKeys = [
        ForeignKey(entity = Ledger::class, parentColumns = ["id"], childColumns = ["ledger_id"]),
        ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["account_id"]),
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["category_id"])
    ],
    indices = [Index("next_execute_date"), Index("ledger_id"), Index("account_id"), Index("category_id")]
)
data class RecurringTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "ledger_id") val ledgerId: Long,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val type: TransactionType,
    val amount: Long,
    val note: String,
    val frequency: Frequency,
    val interval: Int,
    @ColumnInfo(name = "next_execute_date") val nextExecuteDate: Long,
    @ColumnInfo(name = "end_date") val endDate: Long? = null
)
