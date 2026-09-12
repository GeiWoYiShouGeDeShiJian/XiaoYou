package com.example.jizhangruanjian.core.widget
import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.compose.ui.unit.dp
import com.example.jizhangruanjian.MoneyBookApp
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.repository.BudgetRepository
import com.example.jizhangruanjian.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
// P11 收支概览 Widget：本月支出 + 预算进度
class OverviewWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = runBlocking { readData(context) }
        provideContent {
            Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
                Text("本月支出：${data.expense}")
                Text("本月收入：${data.income}")
                if (!data.budget.isNullOrBlank()) Text(data.budget)
            }
        }
    }
    private suspend fun readData(context: Context): Overview {
        val app = context.applicationContext as? MoneyBookApp ?: return Overview("--", "--", "")
        val db = app.appDatabase
        val ledgerId = db.ledgerDao().observeAll().first().firstOrNull { it.isDefault }?.id ?: db.ledgerDao().observeAll().first().firstOrNull()?.id ?: 0L
        if (ledgerId == 0L) return Overview("--", "--", "")
        val txRepo = TransactionRepository(db, db.transactionDao(), db.accountDao(), db.categoryDao(), db.memberDao(), db.transactionImageDao())
        val budgetRepo = BudgetRepository(db.budgetDao(), db.transactionDao())
        val summary = txRepo.homeSummary(ledgerId)
        val budget = budgetRepo.totalState(ledgerId)
        val budgetText = if (budget != null) String.format(Locale.CHINA, "预算已用 %.0f%%（可用 %s）", budget.usagePercent, Formatters.yuanText(budget.available)) else "未设置预算"
        return Overview(Formatters.yuanText(summary.expense), Formatters.yuanText(summary.income), budgetText)
    }
    private data class Overview(val expense: String, val income: String, val budget: String)
}
class OverviewWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OverviewWidget()
}
