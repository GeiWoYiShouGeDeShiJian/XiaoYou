package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.jizhangruanjian.data.model.LedgerAccount
import kotlinx.coroutines.flow.Flow
@Dao
interface LedgerAccountDao {
    @Insert suspend fun insert(link: LedgerAccount)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertIgnore(link: LedgerAccount)
    @Delete suspend fun delete(link: LedgerAccount)
    @Query("SELECT * FROM ledger_account WHERE ledger_id = :ledgerId") fun observeByLedger(ledgerId: Long): Flow<List<LedgerAccount>>
    @Query("SELECT * FROM ledger_account WHERE account_id = :accountId") suspend fun getByAccount(accountId: Long): List<LedgerAccount>
    // P6 R9 共享账户：按账户重置关联
    @Query("DELETE FROM ledger_account WHERE account_id = :accountId") suspend fun deleteByAccount(accountId: Long)
    @Query("DELETE FROM ledger_account WHERE ledger_id = :ledgerId") suspend fun deleteByLedger(ledgerId: Long)
    @Query("DELETE FROM ledger_account WHERE account_id = :accountId AND ledger_id = :ledgerId") suspend fun deleteByAccountAndLedger(accountId: Long, ledgerId: Long)
}
