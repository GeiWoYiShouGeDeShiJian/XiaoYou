package com.example.jizhangruanjian.core.work
import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
// 交易保存后触发预算阈值检查（R5）
@Singleton
class BudgetNotificationScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    fun schedule(ledgerId: Long) {
        val request = OneTimeWorkRequestBuilder<BudgetWorker>()
            .setInputData(workDataOf(BudgetWorker.KEY_LEDGER_ID to ledgerId))
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}