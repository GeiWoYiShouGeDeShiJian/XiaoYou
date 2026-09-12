package com.example.jizhangruanjian.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "app_setting")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)
