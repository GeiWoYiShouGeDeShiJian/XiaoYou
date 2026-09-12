package com.example.jizhangruanjian.core.autorecord
data class PageRule(
    val id: String,
    val packageName: String,
    val requiredMarkers: List<String>,
    val anyMarkers: List<String> = emptyList(),
    val billType: BillType = BillType.EXPENSE
)
