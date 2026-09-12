package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "budget",
    foreignKeys = [
        ForeignKey(entity = Ledger::class, parentColumns = ["id"], childColumns = ["ledger_id"]),
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["category_id"])
    ],
    indices = [Index("ledger_id"), Index("category_id")]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "ledger_id") val ledgerId: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long? = null,
    val period: BudgetPeriod,
    val amount: Long,
    @ColumnInfo(name = "start_date") val startDate: Long,
    @ColumnInfo(name = "rollover_mode") val rolloverMode: Int = 1
)
