package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.Transaction
import com.example.jizhangruanjian.data.model.CategoryTotal
import com.example.jizhangruanjian.data.model.AmountByDate
import kotlinx.coroutines.flow.Flow
@Dao
interface TransactionDao {
    @Insert suspend fun insert(transaction: Transaction): Long
    @Update suspend fun update(transaction: Transaction)
    @Delete suspend fun delete(transaction: Transaction)
    @Query("SELECT * FROM transactions WHERE id = :id") suspend fun getById(id: Long): Transaction?
    // R3 软删除（进回收站，不动余额）与恢复
    @Query("UPDATE transactions SET deleted_at = :deletedAt WHERE id = :id") suspend fun softDelete(id: Long, deletedAt: Long)
    @Query("UPDATE transactions SET deleted_at = NULL WHERE id = :id") suspend fun restore(id: Long)
    @Query("SELECT * FROM transactions ORDER BY trade_date DESC") fun observeAll(): Flow<List<Transaction>>
    // R14 首页最近交易5条
    @Query("SELECT * FROM transactions WHERE ledger_id = :ledgerId AND deleted_at IS NULL ORDER BY trade_date DESC, id DESC LIMIT :limit")
    fun observeRecent(ledgerId: Long, limit: Int = 5): Flow<List<Transaction>>
    // P3 交易列表：按账本取未删除记录，按交易日倒序
    @Query("SELECT * FROM transactions WHERE ledger_id = :ledgerId AND deleted_at IS NULL ORDER BY trade_date DESC, id DESC")
    fun observeByLedger(ledgerId: Long): Flow<List<Transaction>>
    // R3 回收站列表
    @Query("SELECT * FROM transactions WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC")
    fun observeTrash(): Flow<List<Transaction>>
    // R3 回收站清理用
    @Query("SELECT * FROM transactions WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC")
    suspend fun getTrash(): List<Transaction>
    // R4 多维度组合搜索（金额/备注/分类/账户/日期范围）
    @Query(
        "SELECT * FROM transactions WHERE deleted_at IS NULL AND id IN (:ids)" +
            " AND (:minAmount IS NULL OR amount >= :minAmount)" +
            " AND (:maxAmount IS NULL OR amount <= :maxAmount)" +
            " AND (:note IS NULL OR note LIKE '%' || :note || '%')" +
            " AND (:categoryId IS NULL OR category_id = :categoryId)" +
            " AND (:accountId IS NULL OR account_id = :accountId)" +
            " AND (:type IS NULL OR type = :type)" +
            " AND (:fromDate IS NULL OR trade_date >= :fromDate)" +
            " AND (:toDate IS NULL OR trade_date <= :toDate)" +
            " ORDER BY trade_date DESC"
    )
    fun search(
        ids: List<Long>, minAmount: Long?, maxAmount: Long?, note: String?,
        categoryId: Long?, accountId: Long?, type: String?, fromDate: Long?, toDate: Long?
    ): Flow<List<Transaction>>
    // P10 R4 多选组合搜索（分类/账户多选，列表为 null 表示不过滤；调用方保证非空或 null）
    @Query(
        "SELECT * FROM transactions WHERE deleted_at IS NULL AND ledger_id = :ledgerId" +
            " AND (:minAmount IS NULL OR amount >= :minAmount)" +
            " AND (:maxAmount IS NULL OR amount <= :maxAmount)" +
            " AND (:note IS NULL OR note LIKE '%' || :note || '%')" +
            " AND (:categoryIds IS NULL OR category_id IN (:categoryIds))" +
            " AND (:accountIds IS NULL OR account_id IN (:accountIds))" +
            " AND (:type IS NULL OR type = :type)" +
            " AND (:fromDate IS NULL OR trade_date >= :fromDate)" +
            " AND (:toDate IS NULL OR trade_date <= :toDate)" +
            " ORDER BY trade_date DESC, id DESC"
    )
    suspend fun searchV2(
        ledgerId: Long, minAmount: Long?, maxAmount: Long?, note: String?,
        categoryIds: List<Long>?, accountIds: List<Long>?, type: String?,
        fromDate: Long?, toDate: Long?
    ): List<Transaction>
    // R10 防重复
    @Query("SELECT * FROM transactions WHERE dedup_hash = :hash AND deleted_at IS NULL LIMIT 1")
    suspend fun findByDedupHash(hash: String): Transaction?
    // P4 首页：区间内某类型金额（R8 排除转账=仅 INCOME/EXPENSE；R12 include_in_summary=1）
    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE ledger_id = :ledgerId AND deleted_at IS NULL AND type = :type AND include_in_summary = 1 AND trade_date >= :from AND trade_date < :to")
    suspend fun sumByType(ledgerId: Long, type: String, from: Long, to: Long): Long
    // P4 首页：区间内支出分类占比（按 category_id 聚合）
    @Query("SELECT category_id AS categoryId, SUM(amount) AS total FROM transactions WHERE ledger_id = :ledgerId AND deleted_at IS NULL AND type = 'EXPENSE' AND include_in_summary = 1 AND trade_date >= :from AND trade_date < :to GROUP BY category_id")
    suspend fun sumExpenseByCategory(ledgerId: Long, from: Long, to: Long): List<CategoryTotal>
    // 报表：区间内收入分类占比（按 category_id 聚合）
    @Query("SELECT category_id AS categoryId, SUM(amount) AS total FROM transactions WHERE ledger_id = :ledgerId AND deleted_at IS NULL AND type = 'INCOME' AND include_in_summary = 1 AND trade_date >= :from AND trade_date < :to GROUP BY category_id")
    suspend fun sumIncomeByCategory(ledgerId: Long, from: Long, to: Long): List<CategoryTotal>
    // P5 统计：区间内支出原始行（排除转账，供代码按粒度聚合）
    @Query("SELECT trade_date AS tradeDate, amount FROM transactions WHERE ledger_id = :ledgerId AND deleted_at IS NULL AND type = 'EXPENSE' AND include_in_summary = 1 AND trade_date >= :from AND trade_date < :to")
    suspend fun expenseInRange(ledgerId: Long, from: Long, to: Long): List<AmountByDate>
    // P15-S5 常用分类置顶：统计某类型各分类使用次数（降序权重）
    @Query("SELECT category_id AS categoryId, COUNT(*) AS total FROM transactions WHERE ledger_id = :ledgerId AND deleted_at IS NULL AND type = :type GROUP BY category_id")
    suspend fun countByCategory(ledgerId: Long, type: String): List<CategoryTotal>
    // P8 预算：区间内支出总额（category 可选，排除转账 R8）
    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE ledger_id = :ledgerId AND deleted_at IS NULL AND type = 'EXPENSE' AND include_in_summary = 1 AND (:categoryId IS NULL OR category_id = :categoryId) AND trade_date >= :from AND trade_date < :to")
    suspend fun sumExpense(ledgerId: Long, categoryId: Long?, from: Long, to: Long): Long
    // R8 收支统计（排除转账）：fun summarizeIncomeExpense(...)
    // R5/R6 预算使用率与结余统计：fun sumExpenseByCategory(...)
}
