package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "account",
    foreignKeys = [ForeignKey(entity = AccountGroup::class, parentColumns = ["id"], childColumns = ["group_id"])],
    indices = [Index("group_id")]
)
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "group_id") val groupId: Long,
    val name: String,
    val type: AccountType,
    val balance: Long,
    @ColumnInfo(name = "initial_balance") val initialBalance: Long,
    val currency: String = "CNY",
    val color: Int,
    @ColumnInfo(name = "is_shared") val isShared: Boolean = false,
    @ColumnInfo(name = "include_in_net") val includeInNet: Boolean = true,
    val hidden: Boolean = false,
    @ColumnInfo(name = "auto_hide_zero") val autoHideZero: Boolean = false,
    val note: String = "",
    @ColumnInfo(name = "extra_info") val extraInfo: String = "",
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null
)
