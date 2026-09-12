package com.example.jizhangruanjian.domain.usecase
import com.example.jizhangruanjian.core.work.BudgetNotificationScheduler
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.TransactionDomain
import java.security.MessageDigest
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class SaveTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val transactionImageDao: com.example.jizhangruanjian.core.database.TransactionImageDao,
    private val transactionTagDao: com.example.jizhangruanjian.core.database.TransactionTagDao,
    private val budgetScheduler: BudgetNotificationScheduler
) {
    sealed class Gate { object Ok : Gate(); data class Duplicate(val hash: String) : Gate(); object BalanceConfirm : Gate() }
    sealed class Result { data class Success(val txId: Long) : Result(); object Cancelled : Result() }
    // R10 防重复：仅新增时检查相同 dedup_hash 且未删除
    suspend fun checkDuplicate(new: TransactionDomain, old: TransactionDomain?): String? {
        if (old != null) return null
        val hash = dedupHash(new)
        return if (transactionRepository.findByDedupHash(hash) != null) hash else null
    }
    // R2 修改且交易日期=昨天 时需用户确认是否联动余额（今天/未来/前天及更早直接联动）
    fun needBalanceConfirm(old: TransactionDomain?, new: TransactionDomain): Boolean =
        old != null && isYesterday(new.tradeDate)
    suspend fun execute(new: TransactionDomain, old: TransactionDomain?, syncBalance: Boolean, imagePaths: List<String> = emptyList()): Result {
        val entity = with(com.example.jizhangruanjian.data.model.Transaction(
            id = new.id, ledgerId = new.ledgerId, accountId = new.accountId, toAccountId = new.toAccountId,
            categoryId = new.categoryId,
            type = new.type, amount = new.amount, note = new.note, tradeDate = new.tradeDate,
            includeInSummary = new.includeInSummary, merchant = new.merchant, paymentStatus = new.paymentStatus,
            currency = new.currency, discount = new.discount,
            fee = new.fee, feePayer = new.feePayer ?: "NONE", dueDate = new.dueDate,
            reimbursementStatus = new.reimbursementStatus, refundStatus = new.refundStatus, memberId = new.memberId,
            includeInBudget = new.includeInBudget, refundAmount = new.refundAmount,
            refundAccountId = new.refundAccountId, refundDate = new.refundDate, refundNote = new.refundNote,
            memberIds = new.memberIds.joinToString(",").ifEmpty { null },
            loanDirection = new.loanDirection,
            isRecurringGenerated = new.isRecurringGenerated,
            deletedAt = new.deletedAt, createdAt = new.createdAt, updatedAt = new.updatedAt,
            dedupHash = dedupHash(new),
            source = new.source
        )) { this }
        val oldEntity = old?.let { o -> transactionRepository.getById(o.id) }
        val savedId = transactionRepository.save(entity, oldEntity, syncBalance)
        // T7 交易图片：先清旧再写新（编辑=替换），新增时自然为空
        transactionImageDao.deleteByTransaction(savedId)
        imagePaths.forEach { transactionImageDao.insert(com.example.jizhangruanjian.data.model.TransactionImage(transactionId = savedId, path = it)) }
        transactionTagDao.deleteByTransaction(savedId)
        new.tagIds.forEach { transactionTagDao.insert(com.example.jizhangruanjian.data.model.TransactionTag(transactionId = savedId, tagId = it)) }
        budgetScheduler.schedule(new.ledgerId) // R5 触发预算阈值检查
        return Result.Success(savedId)
    }
    fun dedupHash(new: TransactionDomain): String {
        val input = "${new.accountId}${new.tradeDate}${new.amount}${new.type.name}"
        val md = MessageDigest.getInstance("MD5")
        return md.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
    private fun isYesterday(tradeDate: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val tx = Calendar.getInstance().apply { timeInMillis = tradeDate; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        return tx == cal.timeInMillis
    }
}