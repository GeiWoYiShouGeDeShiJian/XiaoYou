package com.example.jizhangruanjian.data
// P6 全局当前选中账本（首页/统计/登录页共享状态）
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
@Singleton
class CurrentLedgerHolder @Inject constructor() {
    val id = MutableStateFlow(0L)
    fun set(id: Long) { this.id.value = id }
}