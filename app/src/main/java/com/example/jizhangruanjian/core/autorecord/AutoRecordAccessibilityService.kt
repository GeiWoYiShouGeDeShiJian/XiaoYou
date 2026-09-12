package com.example.jizhangruanjian.core.autorecord
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.jizhangruanjian.core.work.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AutoRecordAccessibilityService : AccessibilityService() {
    @Inject lateinit var pipeline: AutoRecordPipeline
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pendingJob: Job? = null
    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            NotificationHelper.notifyKeepAlive(this, true)
        } catch (_: Exception) {
            // 通知失败不影响服务存活（防止系统判定服务故障）
        }
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            val pkg = event.packageName?.toString() ?: return
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    pendingJob?.cancel()
                    pendingJob = serviceScope.launch {
                        if (!pipeline.enabled()) return@launch
                        delay(600)
                        rootInActiveWindow?.let { pipeline.submitFromNodes(pkg, it) }
                    }
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    if (pipeline.isSamePageDebounced(pkg)) return
                    serviceScope.launch {
                        if (!pipeline.enabled()) return@launch
                        rootInActiveWindow?.let { pipeline.submitFromNodes(pkg, it) }
                    }
                }
            }
        } catch (_: Exception) {
            // 单次解析异常只记日志，不抛出（防止系统因崩溃禁用服务）
        }
    }
    override fun onInterrupt() {}
    override fun onUnbind(intent: Intent?): Boolean {
        pendingJob?.cancel()
        serviceScope.cancel()
        NotificationHelper.notifyKeepAlive(this, false)
        return super.onUnbind(intent)
    }
}
