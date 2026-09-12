package com.example.jizhangruanjian.core.database
import android.content.Context
import androidx.room.Room
import com.example.jizhangruanjian.core.security.KeystoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context, keystoreManager: KeystoreManager): AppDatabase {
        SQLiteDatabase.loadLibs(context)
        val factory = SqlCipherSupportFactory(keystoreManager.deriveSqlCipherPassphrase())
        return Room.databaseBuilder(context, AppDatabase::class.java, "jizhang.db")
            .openHelperFactory(factory)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_13_14, AppDatabase.MIGRATION_14_15, AppDatabase.MIGRATION_15_16, AppDatabase.MIGRATION_16_17, AppDatabase.MIGRATION_17_18, AppDatabase.MIGRATION_18_19, AppDatabase.MIGRATION_19_20, AppDatabase.MIGRATION_20_21, AppDatabase.MIGRATION_21_22)
            .build()
    }
    @Provides fun provideLedgerDao(db: AppDatabase): LedgerDao = db.ledgerDao()
    @Provides fun provideRecordTemplateDao(db: AppDatabase): RecordTemplateDao = db.recordTemplateDao()
    @Provides fun provideAccountGroupDao(db: AppDatabase): AccountGroupDao = db.accountGroupDao()
    @Provides fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()
    @Provides fun provideLedgerAccountDao(db: AppDatabase): LedgerAccountDao = db.ledgerAccountDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()
    @Provides fun provideTagGroupDao(db: AppDatabase): TagGroupDao = db.tagGroupDao()
    @Provides fun provideTransactionTagDao(db: AppDatabase): TransactionTagDao = db.transactionTagDao()
    @Provides fun provideTransactionImageDao(db: AppDatabase): TransactionImageDao = db.transactionImageDao()
    @Provides fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideRecurringTemplateDao(db: AppDatabase): RecurringTemplateDao = db.recurringTemplateDao()
    @Provides fun provideAppSettingDao(db: AppDatabase): AppSettingDao = db.appSettingDao()
    @Provides fun provideMemberDao(db: AppDatabase): MemberDao = db.memberDao()
    @Provides fun provideMerchantDao(db: AppDatabase): MerchantDao = db.merchantDao()
    @Provides fun provideMerchantGroupDao(db: AppDatabase): MerchantGroupDao = db.merchantGroupDao()
}
