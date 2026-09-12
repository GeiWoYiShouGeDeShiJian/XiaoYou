package com.example.jizhangruanjian.core.work
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jizhangruanjian.MoneyBookApp
// R7 每日周期补跑（由 RecurringScheduler 触发，也用于启动补跑）
class RecurringWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? MoneyBookApp ?: return Result.failure()
        RecurringCatchUp(app.appDatabase).catchUp(System.currentTimeMillis())
        return Result.success()
    }
}