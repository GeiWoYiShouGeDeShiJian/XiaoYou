package com.example.jizhangruanjian.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "category",
    foreignKeys = [ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["parent_id"])],
    indices = [Index("parent_id")]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "parent_id") val parentId: Long? = null,
    val name: String,
    val type: CategoryType,
    val icon: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "keyword_match") val keywordMatch: String? = null,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false
)
