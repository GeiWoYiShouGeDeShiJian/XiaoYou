package com.example.jizhangruanjian.core.work
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
// R7 周期任务调度：每日 00:30（非低电量）+ 一次性补跑
object RecurringScheduler {
    private const val PERIODIC_NAME = "recurring_catchup_periodic"
    private val TRIGGER_TIME = LocalTime.of(0, 30)
    fun schedule(context: Context) {
        val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()
        val zone = ZoneId.systemDefault()
        val now = LocalDate.now()
        val target = now.plusDays(1).atTime(TRIGGER_TIME)
        val initialDelay = target.atZone(zone).toInstant().toEpochMilli() - System.currentTimeMillis()
        val request = PeriodicWorkRequestBuilder<RecurringWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
    fun enqueueCatchUp(context: Context) {
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<RecurringWorker>().build())
    }
}