package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "transaction_image",
    foreignKeys = [ForeignKey(entity = Transaction::class, parentColumns = ["id"], childColumns = ["transaction_id"])],
    indices = [Index("transaction_id")]
)
data class TransactionImage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "transaction_id") val transactionId: Long,
    val path: String
)
