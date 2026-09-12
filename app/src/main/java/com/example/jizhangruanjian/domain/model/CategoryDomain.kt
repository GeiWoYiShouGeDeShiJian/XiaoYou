package com.example.jizhangruanjian.domain.model
import com.example.jizhangruanjian.data.model.CategoryType
data class CategoryDomain(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val type: CategoryType,
    val icon: String,
    val sortOrder: Int,
    val keywordMatch: String? = null,
    val isFavorite: Boolean = false
)