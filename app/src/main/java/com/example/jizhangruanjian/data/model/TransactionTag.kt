package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
@Entity(
    tableName = "transaction_tag",
    primaryKeys = ["transaction_id", "tag_id"],
    foreignKeys = [
        ForeignKey(entity = Transaction::class, parentColumns = ["id"], childColumns = ["transaction_id"]),
        ForeignKey(entity = Tag::class, parentColumns = ["id"], childColumns = ["tag_id"])
    ],
    indices = [Index("transaction_id"), Index("tag_id")]
)
data class TransactionTag(
    @ColumnInfo(name = "transaction_id") val transactionId: Long,
    @ColumnInfo(name = "tag_id") val tagId: Long
)
