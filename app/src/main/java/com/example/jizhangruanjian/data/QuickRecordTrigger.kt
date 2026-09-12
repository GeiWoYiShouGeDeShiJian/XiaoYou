package com.example.jizhangruanjian.data
// P11 快捷记账触发器：主界面到 ViewModel 的事件桥
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
@Singleton
class QuickRecordTrigger @Inject constructor() {
    private val channel = Channel<Long>(Channel.CONFLATED)
    val events = channel.receiveAsFlow()
    fun fire() { channel.trySend(System.currentTimeMillis()) }
}