package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "merchant_group")
data class MerchantGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)
