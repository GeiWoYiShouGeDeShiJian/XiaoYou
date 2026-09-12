package com.example.jizhangruanjian.core.autorecord
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AutoRecordNotificationListener : NotificationListenerService() {
    @Inject lateinit var pipeline: AutoRecordPipeline
    @Inject lateinit var rebinder: ListenerRebinder
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override fun onListenerConnected() {
        super.onListenerConnected()
        rebinder.markConnected()
    }
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        rebinder.checkAndRebind()
    }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.isOngoing) return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString() ?: ""
        scope.launch { pipeline.submitFromNotification(sbn.packageName, title, text, sbn.postTime) }
    }
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
