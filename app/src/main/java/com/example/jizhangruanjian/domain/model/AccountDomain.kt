package com.example.jizhangruanjian.domain.model
import com.example.jizhangruanjian.data.model.AccountType
data class AccountDomain(
    val id: Long,
    val groupId: Long,
    val name: String,
    val type: AccountType,
    val balance: Long,
    val color: Int,
    val isShared: Boolean,
    val sortOrder: Int,
    val includeInNet: Boolean = true,
    val hidden: Boolean = false,
    val autoHideZero: Boolean = false,
    val note: String = "",
    val extraInfo: String = "",
    val createdAt: Long = 0L
)