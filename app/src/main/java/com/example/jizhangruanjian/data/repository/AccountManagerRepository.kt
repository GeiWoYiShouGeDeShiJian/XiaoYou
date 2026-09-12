package com.example.jizhangruanjian.data.repository
import androidx.room.withTransaction
import com.example.jizhangruanjian.core.database.AccountDao
import com.example.jizhangruanjian.core.database.AccountGroupDao
import com.example.jizhangruanjian.core.database.AppDatabase
import com.example.jizhangruanjian.core.database.LedgerAccountDao
import com.example.jizhangruanjian.core.database.LedgerDao
import com.example.jizhangruanjian.data.model.Account
import com.example.jizhangruanjian.data.model.AccountGroup
import com.example.jizhangruanjian.data.model.AccountType
import com.example.jizhangruanjian.data.model.LedgerAccount
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.AccountGroupDomain
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
@Singleton
class AccountManagerRepository @Inject constructor(
    private val db: AppDatabase,
    private val accountDao: AccountDao,
    private val accountGroupDao: AccountGroupDao,
    private val ledgerDao: LedgerDao,
    private val ledgerAccountDao: LedgerAccountDao
) {
    fun observeGroups(): Flow<List<AccountGroupDomain>> = accountGroupDao.observeAll().map { list -> list.map { it.toDomain() } }
    fun observeAccounts(): Flow<List<AccountDomain>> = accountDao.observeAll().map { list -> list.map { it.toDomain() } }
    private suspend fun allLedgerIds() = ledgerDao.observeAll().first()
    // R15 新增自定义大类
    suspend fun createGroup(name: String): Long = accountGroupDao.insert(AccountGroup(name = name, icon = "📁", sortOrder = Int.MAX_VALUE, isDefault = false, createdAt = System.currentTimeMillis()))
    suspend fun renameGroup(id: Long, name: String) { accountGroupDao.getById(id)?.let { accountGroupDao.update(it.copy(name = name)) } }
    // R15 调序：与上/下相邻交换 sort_order
    suspend fun moveGroup(id: Long, down: Boolean) {
        val all = accountGroupDao.observeAll().first().sortedBy { it.sortOrder }
        val idx = all.indexOfFirst { it.id == id }; if (idx < 0) return
        val otherIdx = if (down) idx + 1 else idx - 1
        if (otherIdx < 0 || otherIdx >= all.size) return
        val a = all[idx]; val b = all[otherIdx]
        accountGroupDao.update(a.copy(sortOrder = b.sortOrder)); accountGroupDao.update(b.copy(sortOrder = a.sortOrder))
    }
    // R15 删除大类：预设不可删；下有账户需先迁移
    suspend fun deleteGroup(id: Long): String? {
        val g = accountGroupDao.getById(id) ?: return "分组不存在"
        if (g.isDefault) return "预设大类不可删除"
        if (accountDao.countByGroup(id) > 0) return "该分组下还有账户，请先迁移或删除账户"
        db.withTransaction { accountGroupDao.delete(g) }
        return null
    }
    // P6 新增账户：共享关联全部账本，非共享仅关联当前（首个/默认）账本（R9）
    suspend fun createAccount(groupId: Long, name: String, type: AccountType, initialBalance: Long, isShared: Boolean, includeInNet: Boolean = true, hidden: Boolean = false, autoHideZero: Boolean = false, note: String = "", extraInfo: String = "", ledgerIds: List<Long>? = null): Long {
        val now = System.currentTimeMillis()
        var newId = 0L
        db.withTransaction {
            newId = accountDao.insert(Account(groupId = groupId, name = name, type = type, balance = initialBalance, initialBalance = initialBalance, currency = "CNY", color = 0xFF2E7D32.toInt(), isShared = isShared, includeInNet = includeInNet, hidden = hidden, autoHideZero = autoHideZero, note = note, extraInfo = extraInfo, sortOrder = Int.MAX_VALUE, createdAt = now))
            val targets = ledgerIds ?: linkTargets(isShared)
            targets.forEach { ledgerAccountDao.insertIgnore(LedgerAccount(it, newId)) }
        }
        return newId
    }
    suspend fun renameAccount(id: Long, name: String) { accountDao.getById(id)?.let { accountDao.update(it.copy(name = name)) } }
    suspend fun getAccount(id: Long): AccountDomain? = accountDao.getById(id)?.toDomain()
    suspend fun ledgerIdsOf(accountId: Long): List<Long> = ledgerAccountDao.getByAccount(accountId).map { it.ledgerId }
    suspend fun updateBalance(id: Long, balance: Long) { accountDao.getById(id)?.let { accountDao.update(it.copy(balance = balance)) } }
    suspend fun setAccountHidden(id: Long, hidden: Boolean) { accountDao.getById(id)?.let { accountDao.update(it.copy(hidden = hidden)) } }
    suspend fun updateAccount(id: Long, name: String, groupId: Long, includeInNet: Boolean, hidden: Boolean, autoHideZero: Boolean, note: String, extraInfo: String, balance: Long) {
        accountDao.getById(id)?.let { old -> accountDao.update(old.copy(name = name, groupId = groupId, includeInNet = includeInNet, hidden = hidden, autoHideZero = autoHideZero, note = note, extraInfo = extraInfo, balance = balance)) }
    }
    suspend fun updateLedgers(accountId: Long, ledgerIds: List<Long>) {
        db.withTransaction {
            ledgerAccountDao.deleteByAccount(accountId)
            ledgerIds.forEach { ledgerAccountDao.insertIgnore(LedgerAccount(it, accountId)) }
        }
    }
    // R9 切换共享：共享=关联全部账本；非共享=仅关联当前账本
    suspend fun toggleShared(accountId: Long, isShared: Boolean) {
        db.withTransaction {
            accountDao.getById(accountId)?.let { accountDao.update(it.copy(isShared = isShared)) } ?: return@withTransaction
            ledgerAccountDao.deleteByAccount(accountId)
            linkTargets(isShared).forEach { ledgerAccountDao.insertIgnore(LedgerAccount(it, accountId)) }
        }
    }
    suspend fun deleteAccount(accountId: Long) {
        accountDao.getById(accountId)?.let { accountDao.softDelete(accountId, System.currentTimeMillis()) }
    }
    private suspend fun linkTargets(isShared: Boolean): List<Long> {
        val ids = allLedgerIds().map { it.id }
        return if (isShared) ids else listOf((ids.firstOrNull() ?: 0L))
    }
    private fun AccountGroup.toDomain() = AccountGroupDomain(id, name, icon, sortOrder, isDefault)
    private fun Account.toDomain() = AccountDomain(id, groupId, name, type, balance, color, isShared, sortOrder, includeInNet, hidden, autoHideZero, note, extraInfo, createdAt)
}