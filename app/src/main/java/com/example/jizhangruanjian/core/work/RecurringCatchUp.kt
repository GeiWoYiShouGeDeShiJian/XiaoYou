package com.example.jizhangruanjian.core.work
import androidx.room.withTransaction
import com.example.jizhangruanjian.core.database.AppDatabase
import com.example.jizhangruanjian.data.model.Frequency
import com.example.jizhangruanjian.data.model.RecurringTemplate
import com.example.jizhangruanjian.data.model.Transaction
import com.example.jizhangruanjian.data.model.TransactionType
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
// R7 周期补跑：把到期模板逐条生成交易并推进 next_execute_date
@Singleton
class RecurringCatchUp @Inject constructor(private val db: AppDatabase) {
    suspend fun catchUp(now: Long) {
        val due = db.recurringTemplateDao().findDue(now)
        due.forEach { t -> runTemplate(t, now) }
    }
    private suspend fun runTemplate(t: RecurringTemplate, now: Long) {
        var next = t.nextExecuteDate
        var count = 0
        val note = t.note + "（周期自动）"
        while (next <= now && (t.endDate == null || next <= t.endDate) && count < 1000) {
            val tx = Transaction(
                ledgerId = t.ledgerId, accountId = t.accountId, categoryId = t.categoryId,
                type = t.type, amount = t.amount, note = note, tradeDate = next,
                includeInSummary = true, isRecurringGenerated = true,
                createdAt = now, updatedAt = now, dedupHash = dedupHash(t.accountId, next, t.amount, t.type)
            )
            db.withTransaction {
                db.transactionDao().insert(tx)
                val delta = if (t.type == TransactionType.INCOME) t.amount else -t.amount // R1 余额联动
                db.accountDao().getById(t.accountId)?.let { a -> db.accountDao().update(a.copy(balance = a.balance + delta)) }
            }
            next = advance(next, t.frequency, t.interval)
            count++
        }
        // 已到 end_date 则停用（置为最大值避免再被扫描）
        val finalNext = if (t.endDate != null && next > t.endDate) Long.MAX_VALUE else next
        db.recurringTemplateDao().update(t.copy(nextExecuteDate = finalNext))
    }
    private fun advance(of: Long, f: Frequency, interval: Int): Long {
        val zone = ZoneId.systemDefault()
        val d = Instant.ofEpochMilli(of).atZone(zone).toLocalDate()
        val nd = when (f) {
            Frequency.DAILY -> d.plusDays(interval.toLong())
            Frequency.WEEKLY -> d.plusWeeks(interval.toLong())
            Frequency.MONTHLY -> d.plusMonths(interval.toLong())
            Frequency.YEARLY -> d.plusYears(interval.toLong())
        }
        return nd.atStartOfDay(zone).toInstant().toEpochMilli()
    }
    private fun dedupHash(accountId: Long, tradeDate: Long, amount: Long, type: TransactionType): String {
        val input = "$accountId$tradeDate$amount${type.name}"
        return MessageDigest.getInstance("MD5").digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}