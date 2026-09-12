package com.example.jizhangruanjian.data.repository
import androidx.room.withTransaction
import com.example.jizhangruanjian.core.database.AccountDao
import com.example.jizhangruanjian.core.database.CategoryDao
import com.example.jizhangruanjian.core.database.AppDatabase
import com.example.jizhangruanjian.core.database.MemberDao
import com.example.jizhangruanjian.core.database.TransactionDao
import com.example.jizhangruanjian.data.model.Transaction
import com.example.jizhangruanjian.data.model.Member
import com.example.jizhangruanjian.data.model.ReimbursementStatus
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.model.CategoryTotal
import com.example.jizhangruanjian.data.model.HomeSummary
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.example.jizhangruanjian.data.model.AmountByDate
import com.example.jizhangruanjian.data.model.BucketTotal
import com.example.jizhangruanjian.data.model.Granularity
import com.example.jizhangruanjian.data.model.StatisticsRange
import com.example.jizhangruanjian.data.model.StatisticsSummary
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class TransactionRepository @Inject constructor(
    private val db: AppDatabase,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val memberDao: MemberDao,
    private val imageDao: com.example.jizhangruanjian.core.database.TransactionImageDao
) {
    // 数据变化信号：transactions 表任何变更都会发射（统计页实时刷新）
    val transactionsChanged: Flow<Unit> = transactionDao.observeAll().map { }
    // R1 余额联动把「新增/修改」包装成一个数据库事务，保证原子性
    suspend fun save(new: Transaction, old: Transaction?, balanceCommit: Boolean): Long {
        var resultId = new.id
        db.withTransaction {
            if (old != null && balanceCommit) reverseBalance(old)
            if (balanceCommit) applyBalance(new)
            if (new.id == 0L) {
                resultId = transactionDao.insert(new)
            } else {
                transactionDao.update(new)
            }
        }
        return resultId
    }
    fun observeRecent(ledgerId: Long, limit: Int = 5): Flow<List<TransactionDisplay>> =
        combine(transactionDao.observeRecent(ledgerId, limit), accountDao.observeAll(), categoryDao.observeAll(), memberDao.observeAll(), imageDao.observeAll()) { ts, accs, cats, mems, imgs ->
            val map = imgs.groupBy { it.transactionId }.mapValues { (_, list) -> list.map { it.path } }
            ts.map { it.toDisplay(accs.associateBy { a -> a.id }, cats.associateBy { c -> c.id }, mems.associateBy { m -> m.id }, map[it.id].orEmpty()) }
        }
    fun observeByLedger(ledgerId: Long): Flow<List<TransactionDisplay>> =
        combine(transactionDao.observeByLedger(ledgerId), accountDao.observeAll(), categoryDao.observeAll(), memberDao.observeAll(), imageDao.observeAll()) { ts, accs, cats, mems, imgs ->
            val map = imgs.groupBy { it.transactionId }.mapValues { (_, list) -> list.map { it.path } }
            ts.map { it.toDisplay(accs.associateBy { a -> a.id }, cats.associateBy { c -> c.id }, mems.associateBy { m -> m.id }, map[it.id].orEmpty()) }
        }
    suspend fun getByLedger(ledgerId: Long): List<TransactionDisplay> = observeByLedger(ledgerId).first()
    fun observeNoteHistory(): Flow<List<Pair<TransactionType, String>>> =
        transactionDao.observeAll().map { l -> l.filter { it.deletedAt == null }.sortedByDescending { it.tradeDate }.map { it.type to it.note }.filter { it.second.isNotBlank() }.distinct() }
    // P13 导入单笔：R10 按 dedup_hash 去重，非重则入库并 R1 联动余额
    suspend fun importOne(tx: Transaction): Boolean {
        if (tx.dedupHash != null && transactionDao.findByDedupHash(tx.dedupHash) != null) return false
        db.withTransaction {
            transactionDao.insert(tx)
            applyBalance(tx)
        }
        return true
    }
    fun observeTrash(): Flow<List<TransactionDisplay>> =
        combine(transactionDao.observeTrash(), accountDao.observeAll(), categoryDao.observeAll(), memberDao.observeAll(), imageDao.observeAll()) { ts, accs, cats, mems, imgs ->
            val map = imgs.groupBy { it.transactionId }.mapValues { (_, list) -> list.map { it.path } }
            ts.map { it.toDisplay(accs.associateBy { a -> a.id }, cats.associateBy { c -> c.id }, mems.associateBy { m -> m.id }, map[it.id].orEmpty()) }
        }
    // R3 软删除进回收站（不动余额）、恢复
    suspend fun softDelete(id: Long) { transactionDao.softDelete(id, System.currentTimeMillis()) }
    // T6 报销/应收应付：标记报销状态、结算借贷（转为已软删进入回收站）
    suspend fun updateReimbursement(id: Long, status: ReimbursementStatus?) {
        transactionDao.getById(id)?.let { transactionDao.update(it.copy(reimbursementStatus = status, updatedAt = System.currentTimeMillis())) }
    }
    // 报销关联：更新报销状态并记录关联的报销款收入 id
    suspend fun setReimburseLink(id: Long, status: ReimbursementStatus, linkId: Long) {
        transactionDao.getById(id)?.let { transactionDao.update(it.copy(reimbursementStatus = status, reimburseLinkId = linkId, updatedAt = System.currentTimeMillis())) }
    }
    suspend fun settleLoan(id: Long) { softDelete(id) }
    suspend fun restore(id: Long) { transactionDao.restore(id) }
    // P10 R4 多选搜索（含账/分类多选），其余在 VM 按标签等内存过滤
    suspend fun search(ledgerId: Long, minAmount: Long?, maxAmount: Long?, note: String?, categoryIds: List<Long>?, accountIds: List<Long>?, type: TransactionType?, fromDate: Long?, toDate: Long?): List<TransactionDisplay> {
        val accs = accountDao.observeAll().first().associateBy { it.id }
        val cats = categoryDao.observeAll().first().associateBy { it.id }
        val mems = memberDao.observeAll().first().associateBy { it.id }
        val imgs = imageDao.observeAll().first().groupBy { it.transactionId }.mapValues { (_, list) -> list.map { it.path } }
        return transactionDao.searchV2(ledgerId, minAmount, maxAmount, note?.takeIf { it.isNotBlank() }, categoryIds?.takeIf { it.isNotEmpty() }, accountIds?.takeIf { it.isNotEmpty() }, type?.name, fromDate, toDate).map { it.toDisplay(accs, cats, mems, imgs[it.id].orEmpty()) }
    }
    suspend fun getById(id: Long): Transaction? = transactionDao.getById(id)
    suspend fun getDomain(id: Long): com.example.jizhangruanjian.domain.model.TransactionDomain? =
        transactionDao.getById(id)?.let {
            com.example.jizhangruanjian.domain.model.TransactionDomain(
                id = it.id, ledgerId = it.ledgerId, accountId = it.accountId, toAccountId = it.toAccountId,
                categoryId = it.categoryId,
                type = it.type, amount = it.amount, note = it.note, tradeDate = it.tradeDate,
                includeInSummary = it.includeInSummary, merchant = it.merchant, paymentStatus = it.paymentStatus,
                currency = it.currency, discount = it.discount,
                fee = it.fee, feePayer = it.feePayer.takeIf { p -> p != "NONE" }, dueDate = it.dueDate,
                reimbursementStatus = it.reimbursementStatus, refundStatus = it.refundStatus, memberId = it.memberId,
                includeInBudget = it.includeInBudget, refundAmount = it.refundAmount,
                refundAccountId = it.refundAccountId, refundDate = it.refundDate, refundNote = it.refundNote,
                loanDirection = it.loanDirection,
                isRecurringGenerated = it.isRecurringGenerated,
                deletedAt = it.deletedAt, createdAt = it.createdAt, updatedAt = it.updatedAt,
                source = it.source
            )
        }
    suspend fun findByDedupHash(hash: String): Transaction? = transactionDao.findByDedupHash(hash)
    // P4 首页：本月结余/收支/环比 + 支出分类占比
    suspend fun homeSummary(ledgerId: Long): HomeSummary {
        val now = Calendar.getInstance()
        val curFrom = now.clone() as Calendar
        curFrom.set(Calendar.DAY_OF_MONTH, 1); curFrom.set(Calendar.HOUR_OF_DAY, 0); curFrom.set(Calendar.MINUTE, 0); curFrom.set(Calendar.SECOND, 0); curFrom.set(Calendar.MILLISECOND, 0)
        val curTo = curFrom.clone() as Calendar
        curTo.add(Calendar.MONTH, 1)
        val prevFrom = (curFrom.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        return HomeSummary(
            income = transactionDao.sumByType(ledgerId, TransactionType.INCOME.name, curFrom.timeInMillis, curTo.timeInMillis),
            expense = transactionDao.sumByType(ledgerId, TransactionType.EXPENSE.name, curFrom.timeInMillis, curTo.timeInMillis),
            prevIncome = transactionDao.sumByType(ledgerId, TransactionType.INCOME.name, prevFrom.timeInMillis, curFrom.timeInMillis),
            prevExpense = transactionDao.sumByType(ledgerId, TransactionType.EXPENSE.name, prevFrom.timeInMillis, curFrom.timeInMillis),
            categoryTotals = transactionDao.sumExpenseByCategory(ledgerId, curFrom.timeInMillis, curTo.timeInMillis)
        )
    }
    // P15-S2 快捷统计：今日支出（仅 EXPENSE，排除转账 R8）
    suspend fun todayExpense(ledgerId: Long): Long {
        val now = Calendar.getInstance()
        val start = now.clone() as Calendar
        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0)
        val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        return transactionDao.sumByType(ledgerId, TransactionType.EXPENSE.name, start.timeInMillis, end.timeInMillis)
    }
    // P15-S2 快捷统计：本周支出（周一为周起始）
    suspend fun weekExpense(ledgerId: Long): Long {
        val now = Calendar.getInstance()
        val start = now.clone() as Calendar
        start.firstDayOfWeek = Calendar.MONDAY; start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0)
        val end = now.clone() as Calendar
        end.add(Calendar.MILLISECOND, 1)
        return transactionDao.sumByType(ledgerId, TransactionType.EXPENSE.name, start.timeInMillis, end.timeInMillis)
    }
    // 自定义首页：今日收入（仅 INCOME，排除转账 R8）
    suspend fun todayIncome(ledgerId: Long): Long {
        val now = Calendar.getInstance()
        val start = now.clone() as Calendar
        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0)
        val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        return transactionDao.sumByType(ledgerId, TransactionType.INCOME.name, start.timeInMillis, end.timeInMillis)
    }
    // 自定义首页：本周收入（周一为周起始）
    suspend fun weekIncome(ledgerId: Long): Long {
        val now = Calendar.getInstance()
        val start = now.clone() as Calendar
        start.firstDayOfWeek = Calendar.MONDAY; start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0)
        val end = now.clone() as Calendar
        end.add(Calendar.MILLISECOND, 1)
        return transactionDao.sumByType(ledgerId, TransactionType.INCOME.name, start.timeInMillis, end.timeInMillis)
    }
    // 自定义首页：本年收入（1月1日起）
    suspend fun yearIncome(ledgerId: Long): Long {
        val now = Calendar.getInstance()
        val start = now.clone() as Calendar
        start.set(Calendar.DAY_OF_YEAR, 1); start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0)
        val end = now.clone() as Calendar
        end.add(Calendar.MILLISECOND, 1)
        return transactionDao.sumByType(ledgerId, TransactionType.INCOME.name, start.timeInMillis, end.timeInMillis)
    }
    // 自定义首页：本年支出（1月1日起）
    suspend fun yearExpense(ledgerId: Long): Long {
        val now = Calendar.getInstance()
        val start = now.clone() as Calendar
        start.set(Calendar.DAY_OF_YEAR, 1); start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0)
        val end = now.clone() as Calendar
        end.add(Calendar.MILLISECOND, 1)
        return transactionDao.sumByType(ledgerId, TransactionType.EXPENSE.name, start.timeInMillis, end.timeInMillis)
    }
    // P15-S5 常用分类置顶：某类型各分类使用次数
    suspend fun countByCategory(ledgerId: Long, type: String): List<CategoryTotal> = transactionDao.countByCategory(ledgerId, type)
    // 报表：区间内某类型总额（仅 INCOME/EXPENSE，排除转账）
    suspend fun sumType(ledgerId: Long, type: TransactionType, from: Long, to: Long): Long = transactionDao.sumByType(ledgerId, type.name, from, to)
    // 报表：区间内收入分类占比
    suspend fun incomeByCategory(ledgerId: Long, from: Long, to: Long): List<CategoryTotal> = transactionDao.sumIncomeByCategory(ledgerId, from, to)
    // S7 收支日历：某月按天聚合支出总额（排除转账 R8）
    suspend fun dailyExpenses(ledgerId: Long, from: Long, to: Long): Map<Int, Long> {
        val zone = ZoneId.systemDefault()
        return transactionDao.expenseInRange(ledgerId, from, to)
            .groupBy { Instant.ofEpochMilli(it.tradeDate).atZone(zone).toLocalDate().dayOfMonth }
            .mapValues { (_, rows) -> rows.sumOf { it.amount } }
    }
    // P5 统计：按区间聚合（本期 vs 上期），账务均排除转账（R8）
    suspend fun statistics(ledgerId: Long, r: StatisticsRange): StatisticsSummary =
        StatisticsSummary(
            income = transactionDao.sumByType(ledgerId, TransactionType.INCOME.name, r.from, r.to),
            expense = transactionDao.sumByType(ledgerId, TransactionType.EXPENSE.name, r.from, r.to),
            prevExpense = transactionDao.sumByType(ledgerId, TransactionType.EXPENSE.name, r.prevFrom, r.prevTo),
            categoryTotals = transactionDao.sumExpenseByCategory(ledgerId, r.from, r.to),
            currentSeries = bucket(transactionDao.expenseInRange(ledgerId, r.from, r.to), r.from, r.to, r.granularity),
            prevSeries = bucket(transactionDao.expenseInRange(ledgerId, r.prevFrom, r.prevTo), r.prevFrom, r.prevTo, r.granularity)
        )
    // P5 按粒度（日/周/月）把区间内支出聚合成等长子序列，标签为桶起点的中文短格式
    private fun bucket(rows: List<AmountByDate>, from: Long, to: Long, g: Granularity): List<BucketTotal> {
        val zone = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(from).atZone(zone).toLocalDate()
        val end = Instant.ofEpochMilli(to).atZone(zone).toLocalDate()
        val counts = when (g) {
            Granularity.DAY -> ChronoUnit.DAYS.between(start, end).toInt()
            Granularity.WEEK -> ChronoUnit.WEEKS.between(start, end).toInt()
            Granularity.MONTH -> ChronoUnit.MONTHS.between(start, end).toInt().coerceAtLeast(1)
        }.coerceAtLeast(1)
        val totals = LongArray(counts)
        val fmt = when (g) {
            Granularity.DAY -> SimpleDateFormat("M/d", java.util.Locale.CHINA)
            Granularity.WEEK -> SimpleDateFormat("M/d", java.util.Locale.CHINA)
            Granularity.MONTH -> SimpleDateFormat("M月", java.util.Locale.CHINA)
        }
        rows.forEach { r ->
            val ld = Instant.ofEpochMilli(r.tradeDate).atZone(zone).toLocalDate()
            val idx = when (g) {
                Granularity.DAY -> ChronoUnit.DAYS.between(start, ld).toInt()
                Granularity.WEEK -> ChronoUnit.WEEKS.between(start, ld).toInt()
                Granularity.MONTH -> ChronoUnit.MONTHS.between(start, ld).toInt().coerceAtLeast(0)
            }
            if (idx in 0 until counts) totals[idx] += r.amount
        }
        return (0 until counts).map { i ->
            val bucketStart = when (g) {
                Granularity.DAY -> start.plusDays(i.toLong())
                Granularity.WEEK -> start.plusWeeks(i.toLong())
                Granularity.MONTH -> start.plusMonths(i.toLong())
            }
            BucketTotal(fmt.format(Date.from(bucketStart.atStartOfDay(zone).toInstant())), totals[i])
        }
    }
    suspend fun deletePermanently(tx: Transaction) {
        db.withTransaction {
            transactionDao.delete(tx)
            reverseBalance(tx) // R1 永久清除才回滚
        }
    }
    private suspend fun applyBalance(tx: Transaction) {
        balanceDeltas(tx, forward = true).forEach { (accountId, delta) ->
            if (delta != 0L) accountDao.getById(accountId)?.let { accountDao.update(it.copy(balance = it.balance + delta)) }
        }
    }
    private suspend fun reverseBalance(tx: Transaction) {
        balanceDeltas(tx, forward = false).forEach { (accountId, delta) ->
            if (delta != 0L) accountDao.getById(accountId)?.let { accountDao.update(it.copy(balance = it.balance + delta)) }
        }
    }
    // R1 转账联动：from 减、to 加；反向时符号翻转
    private fun balanceDeltas(tx: Transaction, forward: Boolean): List<Pair<Long, Long>> {
        val sign = if (forward) 1 else -1
        return when (tx.type) {
            TransactionType.INCOME -> listOf(tx.accountId to tx.amount * sign)
            TransactionType.EXPENSE -> listOf(tx.accountId to -tx.amount * sign)
            TransactionType.TRANSFER -> {
                val base = listOf(tx.accountId to -tx.amount * sign) + listOfNotNull(tx.toAccountId?.let { it to tx.amount * sign })
                when {
                    tx.fee > 0L -> if (tx.feePayer == "SELF") base + (tx.accountId to -tx.fee * sign) else base + listOfNotNull(tx.toAccountId?.let { it to -tx.fee * sign })
                    tx.fee < 0L -> { val s = -tx.fee; if (tx.feePayer == "SELF") base + (tx.accountId to -s * sign) else base + listOfNotNull(tx.toAccountId?.let { it to s * sign }) }
                    else -> base
                }
            }
            TransactionType.LOAN -> emptyList()
        }
    }
    private fun Transaction.toDisplay(accounts: Map<Long, com.example.jizhangruanjian.data.model.Account>, categories: Map<Long, com.example.jizhangruanjian.data.model.Category>, members: Map<Long, Member>, images: List<String> = emptyList()) =
        TransactionDisplay(
            id, ledgerId, accountId, categoryId, type, amount, note, tradeDate, includeInSummary,
            accounts[accountId]?.name ?: "未知账户",
            categories[categoryId]?.name ?: "未知",
            categories[categoryId]?.icon ?: "💰",
            toAccountId = toAccountId,
            toAccountName = accounts[toAccountId]?.name ?: "",
            merchant = merchant, paymentStatus = paymentStatus, reimbursementStatus = reimbursementStatus, refundStatus = refundStatus,
            memberId = memberId, memberName = memberId?.let { members[it]?.name } ?: "",
            memberIdList = memberIds?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList(),
            memberNames = memberIds?.split(",")?.mapNotNull { it.toLongOrNull() }?.mapNotNull { members[it]?.name }?.joinToString("，") ?: "",
            loanDirection = loanDirection,
            images = images,
            discount = discount,
            locationName = locationName,
            currency = currency,
            includeInBudget = includeInBudget,
            source = source
        )
}