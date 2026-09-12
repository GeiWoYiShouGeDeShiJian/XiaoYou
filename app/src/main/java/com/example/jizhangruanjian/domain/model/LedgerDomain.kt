package com.example.jizhangruanjian.domain.model
// P6 账本领域模型
data class LedgerDomain(val id: Long, val name: String, val color: Int, val icon: String, val isDefault: Boolean, val sortOrder: Int, val cover: String = "cover_pencils", val coverDark: Boolean = false, val currency: String = "CNY", val hidden: Boolean = false)