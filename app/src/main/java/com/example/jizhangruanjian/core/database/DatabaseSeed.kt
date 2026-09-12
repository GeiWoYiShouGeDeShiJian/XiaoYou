package com.example.jizhangruanjian.core.database
import androidx.room.withTransaction
import com.example.jizhangruanjian.data.model.Account
import com.example.jizhangruanjian.data.model.AccountGroup
import com.example.jizhangruanjian.data.model.AccountType
import com.example.jizhangruanjian.data.model.AppSetting
import com.example.jizhangruanjian.data.model.Category
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.model.Ledger
import com.example.jizhangruanjian.data.model.LedgerAccount
import com.example.jizhangruanjian.data.model.MerchantGroup
import com.example.jizhangruanjian.data.model.TagGroup
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
@Singleton
class DatabaseSeed @Inject constructor(
    private val db: AppDatabase,
    private val ledgerDao: LedgerDao,
    private val accountGroupDao: AccountGroupDao,
    private val accountDao: AccountDao,
    private val ledgerAccountDao: LedgerAccountDao,
    private val categoryDao: CategoryDao,
    private val appSettingDao: AppSettingDao,
    private val tagGroupDao: TagGroupDao,
    private val merchantGroupDao: MerchantGroupDao
) {
    private companion object {
        const val SEED_VERSION_KEY = "seed_version"
        const val SEED_VERSION = 5
        val SEED_COLOR = 0xFF2E7D32.toInt()
    }
    fun seedIfNeeded() = runBlocking {
        val current = appSettingDao.get(SEED_VERSION_KEY)?.value?.toIntOrNull() ?: -1
        if (current >= SEED_VERSION) return@runBlocking
        db.withTransaction {
            if (current < 1) {
                seedDefaultLedger()
                seedAccountGroups()
                insertGroups(CategoryType.EXPENSE, EXPENSE_GROUPS)
                insertGroups(CategoryType.INCOME, INCOME_GROUPS)
                insertFlat(CategoryType.TRANSFER, TRANSFER_ITEMS)
                insertFlat(CategoryType.LOAN, LOAN_ITEMS)
                seedTagGroup()
                seedMerchantGroup()
            } else {
                // v1 -> v2 升级：强制重置四类型为新默认分类，旧分类引用智能迁移
                reseedType(CategoryType.EXPENSE) { insertGroups(CategoryType.EXPENSE, EXPENSE_GROUPS) }
                reseedType(CategoryType.INCOME) { insertGroups(CategoryType.INCOME, INCOME_GROUPS) }
                reseedType(CategoryType.TRANSFER) { insertFlat(CategoryType.TRANSFER, TRANSFER_ITEMS) }
                reseedType(CategoryType.LOAN) { insertFlat(CategoryType.LOAN, LOAN_ITEMS) }
            }
            seedCashGroup()
            seedCashAccount()
            appSettingDao.upsert(AppSetting(SEED_VERSION_KEY, SEED_VERSION.toString()))
        }
    }
    private suspend fun seedCashGroup() {
        if (accountGroupDao.observeAll().first().any { it.name == "现金" }) return
        accountGroupDao.insert(AccountGroup(name = "现金", icon = "💵", sortOrder = 0, isDefault = true, createdAt = System.currentTimeMillis()))
    }
    private suspend fun seedCashAccount() {
        if (accountDao.observeAll().first().any { it.name == "现金" }) return
        val groupId = accountGroupDao.observeAll().first().firstOrNull { it.name == "现金" }?.id
            ?: accountGroupDao.observeAll().first().firstOrNull { it.name == "银行卡" }?.id ?: 0L
        val aid = accountDao.insert(Account(groupId = groupId, name = "现金", type = AccountType.CASH, balance = 0L, initialBalance = 0L, color = 0xFF2196F3.toInt(), sortOrder = 0, createdAt = System.currentTimeMillis()))
        ledgerDao.observeAll().first().forEach { ledgerAccountDao.insertIgnore(LedgerAccount(ledgerId = it.id, accountId = aid)) }
    }
    private suspend fun seedDefaultLedger() {
        ledgerDao.insert(Ledger(name = "默认账本", color = SEED_COLOR, isDefault = true, sortOrder = 1, createdAt = System.currentTimeMillis()))
    }
    private suspend fun seedAccountGroups() {
        val now = System.currentTimeMillis()
        accountGroupDao.insert(AccountGroup(name = "银行卡", icon = "🏦", sortOrder = 1, isDefault = true, createdAt = now))
        accountGroupDao.insert(AccountGroup(name = "网络账户", icon = "📱", sortOrder = 2, isDefault = true, createdAt = now))
    }
    private suspend fun seedTagGroup() {
        tagGroupDao.insert(TagGroup(id = 1, name = "默认分组", sortOrder = 0))
    }
    private suspend fun seedMerchantGroup() {
        merchantGroupDao.insert(MerchantGroup(id = 1, name = "默认分组", sortOrder = 0))
    }
    private data class SeedCat(val id: Long, val name: String, val keywords: List<String>)
    // 两级分类：大类图标取 NN/00，小类各自带图标
    private suspend fun insertGroups(type: CategoryType, groups: List<PresetGroup>): List<SeedCat> {
        val out = mutableListOf<SeedCat>()
        groups.forEachIndexed { gi, group ->
            val gid = categoryDao.insert(Category(parentId = null, name = group.name, type = type, icon = group.icon, sortOrder = gi + 1))
            out.add(SeedCat(gid, group.name, emptyList()))
            group.subs.forEachIndexed { si, sub ->
                val sid = categoryDao.insert(Category(parentId = gid, name = sub.name, type = type, icon = sub.icon, sortOrder = si + 1, keywordMatch = jsonArray(sub.keywords)))
                out.add(SeedCat(sid, sub.name, sub.keywords))
            }
        }
        return out
    }
    // 平铺分类：无大类，每项自身带图标
    private suspend fun insertFlat(type: CategoryType, items: List<PresetFlat>): List<SeedCat> {
        return items.mapIndexed { i, item ->
            val id = categoryDao.insert(Category(parentId = null, name = item.name, type = type, icon = item.icon, sortOrder = i + 1, keywordMatch = jsonArray(item.keywords)))
            SeedCat(id, item.name, item.keywords)
        }
    }
    // 强制重置某类型分类：先插新分类，再把旧分类的全部引用（账单/周期模板/预算/备注学习映射）
    // 按优先级迁移：同名 > 关键词命中 > 兜底第一个，最后删除旧分类
    private suspend fun reseedType(type: CategoryType, insert: suspend () -> List<SeedCat>) {
        val oldCats = categoryDao.getAll().filter { it.type == type }
        val newCats = insert()
        val sql = db.openHelper.writableDatabase
        oldCats.forEach { old ->
            val target = newCats.firstOrNull { it.name == old.name }
                ?: newCats.firstOrNull { nc -> nc.keywords.any { it.isNotEmpty() && old.name.contains(it) } }
                ?: newCats.first()
            listOf("transactions", "recurring_template", "budget").forEach { table ->
                sql.execSQL("UPDATE $table SET category_id = ${target.id} WHERE category_id = ${old.id}")
            }
            sql.execSQL("UPDATE app_setting SET value = '${target.id}' WHERE value = '${old.id}' AND key LIKE 'ncm:%'")
            categoryDao.delete(old)
        }
    }
    private fun jsonArray(keywords: List<String>) =
        keywords.joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" }
    suspend fun resetCategories(type: CategoryType) {
        db.withTransaction {
            when (type) {
                CategoryType.EXPENSE -> reseedType(type) { insertGroups(type, EXPENSE_GROUPS) }
                CategoryType.INCOME -> reseedType(type) { insertGroups(type, INCOME_GROUPS) }
                CategoryType.TRANSFER -> reseedType(type) { insertFlat(type, TRANSFER_ITEMS) }
                CategoryType.LOAN -> reseedType(type) { insertFlat(type, LOAN_ITEMS) }
            }
        }
    }
    private data class PresetSub(val name: String, val icon: String, val keywords: List<String> = emptyList())
    private data class PresetGroup(val name: String, val icon: String, val subs: List<PresetSub>)
    private data class PresetFlat(val name: String, val icon: String, val keywords: List<String> = emptyList())
    // 默认支出分类：17 大类，大类 icon = NN/00（黑图标 + 浅色圆底），只有 00 的无子类（消费/通讯）
    private val EXPENSE_GROUPS = listOf(
        PresetGroup("消费", "expense/01/00", emptyList()),
        PresetGroup("餐饮", "expense/02/00", listOf(
            PresetSub("早中晚餐", "expense/02/01", listOf("早餐", "午餐", "晚餐", "外卖", "快餐", "盒饭", "面条")),
            PresetSub("烟酒茶叶", "expense/02/02", listOf("烟", "酒", "香烟", "白酒", "啤酒", "茶叶")),
            PresetSub("水果", "expense/02/03", listOf("水果", "果切")),
            PresetSub("零食", "expense/02/04", listOf("零食", "薯片", "饼干", "小吃")),
            PresetSub("咖啡奶茶", "expense/02/05", listOf("咖啡", "奶茶", "饮料", "可乐")),
            PresetSub("烹饪食材", "expense/02/06", listOf("买菜", "菜市场", "生鲜", "食材", "调料")),
            PresetSub("特产美食", "expense/02/07", listOf("特产", "伴手礼"))
        )),
        PresetGroup("购物", "expense/03/00", listOf(
            PresetSub("服饰鞋包", "expense/03/01", listOf("衣服", "鞋", "包", "服饰", "帽子")),
            PresetSub("运费", "expense/03/02", listOf("运费", "快递费")),
            PresetSub("生活家电", "expense/03/03", listOf("家电", "电器", "洗衣机", "冰箱")),
            PresetSub("美妆个护", "expense/03/04", listOf("美妆", "护肤", "口红", "洗护", "日用")),
            PresetSub("手机数码", "expense/03/05", listOf("手机", "电脑", "数码", "耳机", "配件"))
        )),
        PresetGroup("交通", "expense/04/00", listOf(
            PresetSub("公交地铁", "expense/04/01", listOf("公交", "地铁", "巴士", "公交卡")),
            PresetSub("打车租车", "expense/04/02", listOf("打车", "出租", "滴滴", "租车", "出行")),
            PresetSub("火车飞机", "expense/04/03", listOf("火车", "高铁", "机票", "飞机", "航班")),
            PresetSub("加油充电", "expense/04/04", listOf("加油", "汽油", "柴油", "充电桩")),
            PresetSub("保养修车", "expense/04/05", listOf("保养", "维修", "修车", "机油")),
            PresetSub("停车费", "expense/04/06", listOf("停车"))
        )),
        PresetGroup("住房", "expense/05/00", listOf(
            PresetSub("水电燃气", "expense/05/01", listOf("水电", "水费", "电费", "燃气", "煤气")),
            PresetSub("房租物业", "expense/05/02", listOf("房租", "物业", "月供")),
            PresetSub("维修清洁", "expense/05/03", listOf("维修", "清洁", "保洁", "家政"))
        )),
        PresetGroup("通讯", "expense/06/00", emptyList()),
        PresetGroup("娱乐", "expense/07/00", listOf(
            PresetSub("运动健身", "expense/07/01", listOf("健身", "运动", "游泳", "羽毛球")),
            PresetSub("电影演出", "expense/07/02", listOf("电影", "演出", "演唱会", "剧院")),
            PresetSub("约会", "expense/07/03", listOf("约会")),
            PresetSub("电子游戏", "expense/07/04", listOf("游戏", "点券", "网吧")),
            PresetSub("KTV", "expense/07/05", listOf("ktv", "唱歌", "欢唱")),
            PresetSub("棋牌桌游", "expense/07/06", listOf("棋牌", "麻将", "桌游", "剧本杀"))
        )),
        PresetGroup("医疗", "expense/08/00", listOf(
            PresetSub("药店买药", "expense/08/01", listOf("药", "药品", "药店", "买药")),
            PresetSub("门诊挂号", "expense/08/02", listOf("挂号", "门诊", "就诊")),
            PresetSub("体检", "expense/08/03", listOf("体检")),
            PresetSub("牙科", "expense/08/04", listOf("牙科", "看牙", "洗牙")),
            PresetSub("疫苗", "expense/08/05", listOf("疫苗", "接种"))
        )),
        PresetGroup("教育", "expense/09/00", listOf(
            PresetSub("学费", "expense/09/01", listOf("学费", "学杂费")),
            PresetSub("培训考试", "expense/09/02", listOf("培训", "考试", "报名费", "课程")),
            PresetSub("家教补习", "expense/09/03", listOf("家教", "补习")),
            PresetSub("书报杂志", "expense/09/04", listOf("书", "报刊", "杂志", "报纸"))
        )),
        PresetGroup("美容", "expense/10/00", listOf(
            PresetSub("美发护理", "expense/10/01", listOf("理发", "美发", "染发", "烫发")),
            PresetSub("SPA按摩", "expense/10/02", listOf("spa", "按摩", "足浴", "洗浴")),
            PresetSub("美甲美睫", "expense/10/03", listOf("美甲", "美睫", "睫毛"))
        )),
        PresetGroup("亲子", "expense/11/00", listOf(
            PresetSub("奶粉尿布", "expense/11/01", listOf("奶粉", "尿布", "尿不湿", "婴儿")),
            PresetSub("玩具绘本", "expense/11/02", listOf("玩具", "绘本", "早教"))
        )),
        PresetGroup("旅行", "expense/12/00", listOf(
            PresetSub("旅行交通", "expense/12/01", listOf("机票", "火车票", "船票")),
            PresetSub("酒店住宿", "expense/12/02", listOf("酒店", "民宿", "住宿", "订房")),
            PresetSub("景点门票", "expense/12/03", listOf("门票", "景点", "游乐园")),
            PresetSub("签证", "expense/12/04", listOf("签证"))
        )),
        PresetGroup("宠物", "expense/13/00", listOf(
            PresetSub("宠物食品", "expense/13/01", listOf("狗粮", "猫粮", "猫砂")),
            PresetSub("宠物看病", "expense/13/02", listOf("宠物医院")),
            PresetSub("宠物用品", "expense/13/03", listOf("宠物用品"))
        )),
        PresetGroup("投资", "expense/14/00", listOf(
            PresetSub("保险", "expense/14/01", listOf("保险", "保费")),
            PresetSub("基金股票", "expense/14/02", listOf("基金", "股票", "理财", "买入")),
            PresetSub("金属", "expense/14/03", listOf("黄金", "金属")),
            PresetSub("定期存款", "expense/14/04", listOf("存款", "定存"))
        )),
        PresetGroup("资金往来", "expense/15/00", listOf(
            PresetSub("红包", "expense/15/01", listOf("红包", "礼金"))
        )),
        PresetGroup("还款", "expense/16/00", listOf(
            PresetSub("信用卡", "expense/16/01", listOf("信用卡", "贷记卡")),
            PresetSub("车贷", "expense/16/02", listOf("车贷")),
            PresetSub("房贷", "expense/16/03", listOf("房贷", "月供"))
        )),
        PresetGroup("人情社交", "expense/17/00", listOf(
            PresetSub("请客送礼", "expense/17/01", listOf("请客", "送礼", "礼物", "宴请")),
            PresetSub("孝敬长辈", "expense/17/02", listOf("孝敬", "长辈", "爸妈"))
        ))
    )
    // 默认收入分类：5 大类，大类 icon = NN/00，无子类的用空列表（资金往来）
    private val INCOME_GROUPS = listOf(
        PresetGroup("薪资", "income/01/00", listOf(
            PresetSub("工资", "income/01/01", listOf("工资", "薪资", "发工资")),
            PresetSub("奖金", "income/01/02", listOf("奖金", "年终奖")),
            PresetSub("兼职副业", "income/01/03", listOf("兼职", "副业", "外快")),
            PresetSub("补贴", "income/01/04", listOf("补贴", "福利"))
        )),
        PresetGroup("投资", "income/02/00", listOf(
            PresetSub("利息", "income/02/01", listOf("利息", "分红")),
            PresetSub("基金股票收益", "income/02/02", listOf("基金收益", "股票", "理财收益"))
        )),
        PresetGroup("收款", "income/03/00", listOf(
            PresetSub("闲置出售", "income/03/01", listOf("闲置", "出售", "二手"))
        )),
        PresetGroup("资金往来", "income/04/00", emptyList()),
        PresetGroup("其他收入", "income/05/00", listOf(
            PresetSub("生活费", "income/05/01", listOf("生活费")),
            PresetSub("中奖", "income/05/02", listOf("中奖", "彩票")),
            PresetSub("赔偿金", "income/05/03", listOf("赔偿", "赔付", "理赔"))
        ))
    )
    // 默认转账分类（assets/cat/transfer，浅雾紫底 + 深紫罗兰）
    private val TRANSFER_ITEMS = listOf(
        PresetFlat("账户互转", "transfer/01"),
        PresetFlat("信用还款", "transfer/02", listOf("还款", "信用卡")),
        PresetFlat("理财买入", "transfer/03", listOf("买入")),
        PresetFlat("理财赎回", "transfer/04", listOf("赎回"))
    )
    // 默认借贷分类（assets/cat/loan，浅陶土底 + 深赭石棕）
    private val LOAN_ITEMS = listOf(
        PresetFlat("借入", "loan/01"),
        PresetFlat("借出", "loan/02"),
        PresetFlat("还款", "loan/03", listOf("还款")),
        PresetFlat("收款", "loan/04", listOf("收款"))
    )
}