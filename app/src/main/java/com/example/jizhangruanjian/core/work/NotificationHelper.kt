package com.example.jizhangruanjian.core.work
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.example.jizhangruanjian.MainActivity
object NotificationHelper {
    const val CHANNEL_ID = "budget_alerts"
    const val CHANNEL_AUTO_RECORD = "auto_record"
    const val CHANNEL_AUTO_RECORD_KEEPALIVE = "auto_record_keepalive"
    const val NOTIFY_ID_KEEPALIVE = 0x41524B41
    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "预算提醒", NotificationManager.IMPORTANCE_DEFAULT)
        nm.createNotificationChannel(channel)
    }
    fun notify(context: Context, id: Int, title: String, text: String) {
        ensureChannel(context)
        val pi = PendingIntent.getActivity(context, id, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(id, notification)
    }
    fun ensureAutoRecordChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_AUTO_RECORD, "自动记账", NotificationManager.IMPORTANCE_HIGH)
        nm.createNotificationChannel(channel)
    }
    // 自动记账通知：点击跳转 url（模式B预填记账页 / 模式C撤销）
    fun notifyAutoRecord(context: Context, id: Int, title: String, text: String, url: String) {
        ensureAutoRecordChannel(context)
        val pi = PendingIntent.getActivity(context, id, Intent(Intent.ACTION_VIEW, Uri.parse(url), context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_AUTO_RECORD)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(id, notification)
    }
    // 常驻状态通知：无障碍连接后显示，断开后取消（增强进程存活 + 用户可见状态）
    fun notifyKeepAlive(context: Context, show: Boolean) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!show) {
            nm.cancel(NOTIFY_ID_KEEPALIVE)
            return
        }
        val channel = NotificationChannel(CHANNEL_AUTO_RECORD_KEEPALIVE, "自动记账运行状态", NotificationManager.IMPORTANCE_LOW)
        nm.createNotificationChannel(channel)
        val pi = PendingIntent.getActivity(context, NOTIFY_ID_KEEPALIVE, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_AUTO_RECORD_KEEPALIVE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("自动记账运行中")
            .setContentText("无障碍服务已连接，正在监听微信/支付宝")
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        nm.notify(NOTIFY_ID_KEEPALIVE, notification)
    }
}