package com.example.jizhangruanjian.data.repository
import com.example.jizhangruanjian.core.database.BudgetDao
import com.example.jizhangruanjian.core.database.TransactionDao
import com.example.jizhangruanjian.data.model.Budget
import com.example.jizhangruanjian.domain.model.BudgetDomain
import com.example.jizhangruanjian.domain.model.BudgetState
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) {
    private val zone = ZoneId.systemDefault()
    fun observeBudgets(ledgerId: Long): Flow<List<Budget>> = budgetDao.observeByLedger(ledgerId)
    fun observeStates(ledgerId: Long): Flow<List<BudgetState>> =
        budgetDao.observeByLedger(ledgerId).flatMapLatest { budgets -> flow { emit(compute(budgets)) } }
    suspend fun compute(budgets: List<Budget>): List<BudgetState> = budgets.map { computeOne(it) }
    // 总预算（MONTHLY 且无分类）
    suspend fun totalState(ledgerId: Long): BudgetState? =
        budgetDao.observeByLedger(ledgerId).flatMapLatest { budgets ->
            val total = budgets.firstOrNull { it.period.name == "MONTHLY" && it.categoryId == null }
            flow { emit(total?.let { computeOne(it) }) }
        }.first()
    suspend fun computeOne(b: Budget): BudgetState {
        val (curStart, curEnd, prevStart) = ranges(b.period.name)
        val spent = transactionDao.sumExpense(b.ledgerId, b.categoryId, curStart, curEnd)
        val prevSpent = transactionDao.sumExpense(b.ledgerId, b.categoryId, prevStart, curStart)
        // R6：可用 = 本月预算 + 上月结余；上月结余 = 上月预算 - 上月支出（负则扣减）
        val available = if (b.rolloverMode == 1) b.amount + (b.amount - prevSpent) else b.amount
        val remaining = available - spent
        return BudgetState(
            budget = b.toDomain(), spent = spent, prevSpent = prevSpent,
            available = available, remaining = remaining,
            usagePercent = if (b.amount > 0) spent * 100f / b.amount else 0f,
            over = remaining < 0
        )
    }
    // 返回 [本期起, 本期止, 上期起)（毫秒）
    private fun ranges(period: String): Triple<Long, Long, Long> {
        val now = LocalDate.now()
        return if (period == "YEARLY") {
            val curStart = now.withDayOfYear(1)
            Triple(startMs(curStart), startMs(curStart.plusYears(1)), startMs(curStart.minusYears(1)))
        } else {
            val curStart = now.withDayOfMonth(1)
            Triple(startMs(curStart), startMs(curStart.plusMonths(1)), startMs(curStart.minusMonths(1)))
        }
    }
    private fun startMs(d: LocalDate): Long = d.atStartOfDay(zone).toInstant().toEpochMilli()
    private fun Budget.toDomain() = BudgetDomain(id, ledgerId, categoryId, period, amount, startDate, rolloverMode)
}