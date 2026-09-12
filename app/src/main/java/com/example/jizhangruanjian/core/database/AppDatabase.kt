package com.example.jizhangruanjian.core.database
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.jizhangruanjian.data.model.Account
import com.example.jizhangruanjian.data.model.AccountGroup
import com.example.jizhangruanjian.data.model.AppSetting
import com.example.jizhangruanjian.data.model.Budget
import com.example.jizhangruanjian.data.model.Category
import com.example.jizhangruanjian.data.model.Converters
import com.example.jizhangruanjian.data.model.Ledger
import com.example.jizhangruanjian.data.model.LedgerAccount
import com.example.jizhangruanjian.data.model.Merchant
import com.example.jizhangruanjian.data.model.MerchantGroup
import com.example.jizhangruanjian.data.model.Member
import com.example.jizhangruanjian.data.model.RecurringTemplate
import com.example.jizhangruanjian.data.model.RecordTemplate
import com.example.jizhangruanjian.data.model.Tag
import com.example.jizhangruanjian.data.model.TagGroup
import com.example.jizhangruanjian.data.model.Transaction
import com.example.jizhangruanjian.data.model.TransactionImage
import com.example.jizhangruanjian.data.model.TransactionTag
@Database(
    entities = [
        Ledger::class, AccountGroup::class, Account::class, LedgerAccount::class,
        Category::class, Transaction::class, Tag::class, TagGroup::class, TransactionTag::class,
        TransactionImage::class, Budget::class, RecurringTemplate::class, AppSetting::class,
        Member::class, RecordTemplate::class, Merchant::class, MerchantGroup::class
    ],
    version = 22,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        // P7 转账目标账户字段
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN to_account_id INTEGER")
            }
        }
        // P15 账本个性化图标字段
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ledger ADD COLUMN icon TEXT NOT NULL DEFAULT '📖'")
            }
        }
        // T4 分类收藏字段
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE category ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
            }
        }
        // T6 应收应付：4→6 一次性补齐 T5 扩展属性列 + 成员表 + 借贷方向列（此前 4→5 未注册）
        val MIGRATION_4_6 = object : Migration(4, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN merchant TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN payment_status TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN reimbursement_status TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN refund_status TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN member_id INTEGER")
                db.execSQL("CREATE TABLE IF NOT EXISTS `member` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `sort_order` INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("ALTER TABLE transactions ADD COLUMN loan_direction TEXT")
            }
        }
        // 账本个性化：封面/封面文字颜色/本位币/隐藏
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ledger ADD COLUMN cover TEXT NOT NULL DEFAULT 'cover_pencils'")
                db.execSQL("ALTER TABLE ledger ADD COLUMN cover_dark INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE ledger ADD COLUMN currency TEXT NOT NULL DEFAULT 'CNY'")
                db.execSQL("ALTER TABLE ledger ADD COLUMN hidden INTEGER NOT NULL DEFAULT 0")
            }
        }
        // 账户扩展：计入净资产/隐藏/零余额自动隐藏/备注/扩展信息
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE account ADD COLUMN include_in_net INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE account ADD COLUMN hidden INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE account ADD COLUMN auto_hide_zero INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE account ADD COLUMN note TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE account ADD COLUMN extra_info TEXT NOT NULL DEFAULT ''")
            }
        }
        // 回收站：账本/账户软删除字段
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ledger ADD COLUMN deleted_at INTEGER")
                db.execSQL("ALTER TABLE account ADD COLUMN deleted_at INTEGER")
            }
        }
        // 角色系统：成员头像/颜色列 + 预置角色
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE member ADD COLUMN icon TEXT NOT NULL DEFAULT '🧑'")
                db.execSQL("ALTER TABLE member ADD COLUMN color INTEGER NOT NULL DEFAULT 0xFFE8C9A0")
                db.execSQL("INSERT INTO member(name, sort_order, icon, color) VALUES ('孩子', 1, '🧒', 0xFFF2C14E)")
                db.execSQL("INSERT INTO member(name, sort_order, icon, color) VALUES ('父母', 2, '👴', 0xFFB8860B)")
                db.execSQL("INSERT INTO member(name, sort_order, icon, color) VALUES ('亲戚', 3, '🧓', 0xFF3F6FB5)")
                db.execSQL("INSERT INTO member(name, sort_order, icon, color) VALUES ('朋友', 4, '🧑', 0xFF1E8C8C)")
                db.execSQL("INSERT INTO member(name, sort_order, icon, color) VALUES ('家庭公共', 5, '🏠', 0xFF7FC4E8)")
            }
        }
        // 标签系统：标签颜色/分组列 + 标签分组表（默认分组 id=1）
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `tag_group` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `sort_order` INTEGER NOT NULL)")
                db.execSQL("INSERT INTO tag_group(id, name, sort_order) VALUES (1, '默认分组', 0)")
                db.execSQL("ALTER TABLE tag ADD COLUMN color INTEGER NOT NULL DEFAULT 0xFF33608C")
                db.execSQL("ALTER TABLE tag ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1")
            }
        }
        // 多选成员：交易成员 id 逗号分隔列
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN member_ids TEXT")
            }
        }
        // 快捷记账模板
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `record_template` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `amountCents` INTEGER NOT NULL, `categoryId` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `toAccountId` INTEGER NOT NULL, `note` TEXT NOT NULL, `memberIds` TEXT NOT NULL, `merchant` TEXT NOT NULL)")
            }
        }
        // 商家系统：商家分组表（默认分组 id=1）+ 商家表
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `merchant_group` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `sort_order` INTEGER NOT NULL)")
                db.execSQL("INSERT INTO merchant_group(id, name, sort_order) VALUES (1, '默认分组', 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `merchant` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, `group_id` INTEGER NOT NULL DEFAULT 1, `sort_order` INTEGER NOT NULL DEFAULT 0)")
            }
        }
        // 优惠金额（分）
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN discount INTEGER NOT NULL DEFAULT 0")
            }
        }
        // 报销关联：支出 ↔ 报销款收入的关联 id
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN reimburse_link_id INTEGER NOT NULL DEFAULT 0")
            }
        }
        // 退款明细字段 + 预算统计独立开关
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN include_in_budget INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE transactions ADD COLUMN refund_amount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN refund_account_id INTEGER")
                db.execSQL("ALTER TABLE transactions ADD COLUMN refund_date INTEGER")
                db.execSQL("ALTER TABLE transactions ADD COLUMN refund_note TEXT")
            }
        }
        // 转账手续费/补贴 + 借贷还款日
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN fee INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN fee_payer TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN due_date INTEGER")
            }
        }
        // 自动记账：交易来源字段
        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN source TEXT NOT NULL DEFAULT 'MANUAL'")
            }
        }
        // 模板扩展字段：币种/优惠/标签/应付款/待报销/预算收支/快速保存
        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE record_template ADD COLUMN currency TEXT NOT NULL DEFAULT 'CNY'")
                db.execSQL("ALTER TABLE record_template ADD COLUMN discountCents INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE record_template ADD COLUMN tagIds TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE record_template ADD COLUMN paymentUnpaid INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE record_template ADD COLUMN reimbursable INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE record_template ADD COLUMN includeBudget INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE record_template ADD COLUMN includeSummary INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE record_template ADD COLUMN quickSave INTEGER NOT NULL DEFAULT 0")
            }
        }
        // 模板：转账手续费字段
        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE record_template ADD COLUMN feeCents INTEGER NOT NULL DEFAULT 0")
            }
        }
        // 默认角色补充：'自己' 作为默认首项
        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("INSERT INTO member(name, sort_order, icon, color) SELECT '自己', 0, '🧑', 0xFFE8C9A0 WHERE NOT EXISTS (SELECT 1 FROM member WHERE name = '自己')")
            }
        }
    }
    abstract fun ledgerDao(): LedgerDao
    abstract fun recordTemplateDao(): RecordTemplateDao
    abstract fun accountGroupDao(): AccountGroupDao
    abstract fun accountDao(): AccountDao
    abstract fun ledgerAccountDao(): LedgerAccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun tagDao(): TagDao
    abstract fun tagGroupDao(): TagGroupDao
    abstract fun transactionTagDao(): TransactionTagDao
    abstract fun transactionImageDao(): TransactionImageDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringTemplateDao(): RecurringTemplateDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun memberDao(): MemberDao
    abstract fun merchantDao(): MerchantDao
    abstract fun merchantGroupDao(): MerchantGroupDao
}
