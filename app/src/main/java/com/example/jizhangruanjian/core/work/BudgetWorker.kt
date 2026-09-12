package com.example.jizhangruanjian.core.work
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jizhangruanjian.MoneyBookApp
import com.example.jizhangruanjian.data.model.AppSetting
import com.example.jizhangruanjian.data.repository.BudgetRepository
import kotlinx.coroutines.flow.first
// R5 预算阈值提醒：跨 50/80/100% 各通知一次，状态存 app_setting
class BudgetWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val ledgerId = inputData.getLong(KEY_LEDGER_ID, 0L)
        if (ledgerId == 0L) return Result.failure()
        val app = applicationContext as? MoneyBookApp ?: return Result.failure()
        val db = app.appDatabase
        val repo = BudgetRepository(db.budgetDao(), db.transactionDao())
        val budgets = db.budgetDao().observeByLedger(ledgerId).first()
        val states = repo.compute(budgets)
        states.forEach { s ->
            val pct = s.usagePercent
            listOf(50, 80, 100).forEach { threshold ->
                if (pct >= threshold) {
                    val key = "budget_notified_${s.budget.id}_$threshold"
                    if (db.appSettingDao().get(key) == null) {
                        val name = if (s.isTotal) "总预算" else "分类预算${s.budget.categoryId}"
                        NotificationHelper.notify(applicationContext, (s.budget.id * 10 + threshold / 10).toInt(), "预算提醒", "预算「$name」使用率已达 $pct%")
                        db.appSettingDao().upsert(AppSetting(key, "1"))
                    }
                }
            }
        }
        return Result.success()
    }
    companion object {
        const val KEY_LEDGER_ID = "ledger_id"
    }
}