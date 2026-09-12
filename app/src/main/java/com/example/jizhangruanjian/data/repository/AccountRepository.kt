package com.example.jizhangruanjian.data.repository
import com.example.jizhangruanjian.core.database.AccountDao
import com.example.jizhangruanjian.data.model.Account
import com.example.jizhangruanjian.data.model.AccountType
import com.example.jizhangruanjian.domain.model.AccountDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class AccountRepository @Inject constructor(private val accountDao: AccountDao) {
    fun observeAll(): Flow<List<AccountDomain>> = accountDao.observeAll().map { list -> list.map { it.toDomain() } }
    suspend fun getById(id: Long): AccountDomain? = accountDao.getById(id)?.toDomain()
    suspend fun createAccount(name: String, groupId: Long, type: AccountType, initialBalance: Long = 0): Long =
        accountDao.insert(Account(groupId = groupId, name = name, type = type, balance = initialBalance, initialBalance = initialBalance, color = 0xFF2E7D32.toInt(), sortOrder = Int.MAX_VALUE, createdAt = System.currentTimeMillis()))
    suspend fun updateBalance(accountId: Long, delta: Long) {
        val account = accountDao.getById(accountId) ?: return
        accountDao.update(account.copy(balance = account.balance + delta))
    }
    private fun Account.toDomain() = AccountDomain(id, groupId, name, type, balance, color, isShared, sortOrder)
}