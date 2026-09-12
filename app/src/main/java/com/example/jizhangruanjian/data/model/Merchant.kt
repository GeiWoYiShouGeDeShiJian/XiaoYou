package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "merchant")
data class Merchant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "icon") val icon: String = "",
    @ColumnInfo(name = "group_id") val groupId: Long = 1L,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)
