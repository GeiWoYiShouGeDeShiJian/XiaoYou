package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "member")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "icon") val icon: String = "🧑",
    @ColumnInfo(name = "color") val color: Long = 0xFFE8C9A0
)