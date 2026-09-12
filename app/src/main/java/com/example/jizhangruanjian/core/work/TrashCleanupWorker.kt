package com.example.jizhangruanjian.core.work
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jizhangruanjian.MoneyBookApp
import com.example.jizhangruanjian.data.repository.TransactionRepository
// R3 回收站清理：每日 03:00 执行，超 7 天永久删除并回滚余额；临近清理(≈3天)每日通知
class TrashCleanupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? MoneyBookApp ?: return Result.failure()
        val db = app.appDatabase
        val repo = TransactionRepository(db, db.transactionDao(), db.accountDao(), db.categoryDao(), db.memberDao(), db.transactionImageDao())
        val now = System.currentTimeMillis()
        val dayMs = 24 * 3600 * 1000L
        val trash = db.transactionDao().getTrash()
        var warnCount = 0
        trash.forEach { tx ->
            val age = now - (tx.deletedAt ?: now)
            if (age > 7 * dayMs) repo.deletePermanently(tx) // R3 永久删除 + 反向回滚余额
            else if (age >= 4 * dayMs) warnCount++ // 距 7 天清理剩约 3 天
        }
        if (warnCount > 0) NotificationHelper.notify(applicationContext, 9000, "回收站", "回收站有 $warnCount 条记录将在 3 天内被永久清除")
        return Result.success()
    }
}
object TrashCleanupScheduler {
    private const val PERIODIC_NAME = "trash_cleanup_periodic"
    fun schedule(context: Context) {
        val constraints = androidx.work.Constraints.Builder().setRequiresBatteryNotLow(true).build()
        val zone = java.time.ZoneId.systemDefault()
        val now = java.time.LocalDate.now()
        val target = now.plusDays(1).atTime(java.time.LocalTime.of(3, 0))
        val initialDelay = target.atZone(zone).toInstant().toEpochMilli() - System.currentTimeMillis()
        val request = androidx.work.PeriodicWorkRequestBuilder<TrashCleanupWorker>(1, java.util.concurrent.TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC_NAME, androidx.work.ExistingPeriodicWorkPolicy.KEEP, request)
    }
}