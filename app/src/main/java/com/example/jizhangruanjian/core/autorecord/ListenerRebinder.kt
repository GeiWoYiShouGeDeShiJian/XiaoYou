package com.example.jizhangruanjian.core.autorecord
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Singleton
class ListenerRebinder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var connectedAt = 0L
    fun markConnected() { connectedAt = System.currentTimeMillis() }
    // 掉线恢复：先 requestRebind（温和），2 分钟未恢复再组件 disable→enable 强制重绑
    fun checkAndRebind() {
        if (connectedAt > 0L && System.currentTimeMillis() - connectedAt < 2 * 60 * 1000L) return
        val cn = ComponentName(context, AutoRecordNotificationListener::class.java)
        NotificationListenerService.requestRebind(cn)
        scope.launch {
            delay(2 * 60 * 1000L)
            if (connectedAt > 0L && System.currentTimeMillis() - connectedAt < 2 * 60 * 1000L) return@launch
            val pm = context.packageManager
            pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }
}
