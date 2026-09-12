package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
@Entity(
    tableName = "ledger_account",
    primaryKeys = ["ledger_id", "account_id"],
    foreignKeys = [
        ForeignKey(entity = Ledger::class, parentColumns = ["id"], childColumns = ["ledger_id"]),
        ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["account_id"])
    ],
    indices = [Index("ledger_id"), Index("account_id")]
)
data class LedgerAccount(
    @ColumnInfo(name = "ledger_id") val ledgerId: Long,
    @ColumnInfo(name = "account_id") val accountId: Long
)
