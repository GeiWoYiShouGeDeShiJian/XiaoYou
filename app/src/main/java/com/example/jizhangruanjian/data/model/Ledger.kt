package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "ledger")
data class Ledger(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    // P15 账本个性化：emoji 图标
    val icon: String = "📖",
    @ColumnInfo(name = "is_default") val isDefault: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val cover: String = "cover_pencils",
    @ColumnInfo(name = "cover_dark") val coverDark: Boolean = false,
    val currency: String = "CNY",
    val hidden: Boolean = false,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null
)
