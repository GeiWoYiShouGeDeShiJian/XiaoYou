package com.example.jizhangruanjian.data.model
data class HomeConfig(
    val dataOne: HomeDataOption = HomeDataOption.MONTH_BALANCE,
    val dataTwo: HomeDataOption = HomeDataOption.MONTH_INCOME,
    val dataThree: HomeDataOption = HomeDataOption.MONTH_EXPENSE,
    val ledgerCover: String = "cover_pencils",
    val coverTextColor: CoverTextColor = CoverTextColor.LIGHT,
    val showStatCards: Boolean = true,
    val statCardA: HomeDataOption = HomeDataOption.TODAY_EXPENSE,
    val statCardB: HomeDataOption = HomeDataOption.WEEK_EXPENSE,
    val statCardC: HomeDataOption = HomeDataOption.MONTH_EXPENSE,
    val showCategoryRatio: Boolean = true,
    val showBudget: Boolean = true,
    val showQuickActions: Boolean = true,
    val showRecentTransactions: Boolean = true,
    val hideMoreBudgetButton: Boolean = false,
    val moduleOrder: List<String> = DEFAULT_MODULE_ORDER,
    val pullDownAction: PullDownAction = PullDownAction.REFRESH,
    val recentTransactionCount: Int = 4,
    val clickCoverToSwitchLedger: Boolean = false,
    val calendarWeekStart: Int = 1,
    val calendarShowLunar: Boolean = true,
    val calendarProfitColor: Long = 0xFF2E7D32,
    val calendarLossColor: Long = 0xFFD32F2F,
    val reportShowAssets: Boolean? = null,
    val reportShowDebts: Boolean? = null,
    val reportMaxSlices: Int? = null,
    val accountSortMode: String? = null,
    val accountOrder: String = "",
    val groupOrder: String = "",
    val creditSortInterest: Boolean? = null,
    val creditSortQuota: Boolean? = null,
    val customAccountTypes: String = "",
    val advPreset: String = ""
)
val DEFAULT_MODULE_ORDER = listOf("today_stats", "category_ratio", "budget", "quick_actions", "recent_transactions")
fun normalizeModuleOrder(order: List<String>): List<String> = (order.filter { it in DEFAULT_MODULE_ORDER } + DEFAULT_MODULE_ORDER.filterNot { it in order }).distinct()
enum class HomeDataOption { MONTH_BALANCE, MONTH_INCOME, MONTH_EXPENSE, WEEK_EXPENSE, TODAY_EXPENSE, WEEK_INCOME, TODAY_INCOME, YEAR_BALANCE, YEAR_EXPENSE, NONE }
enum class CoverTextColor { LIGHT, DARK }
enum class PullDownAction { REFRESH, QUICK_RECORD, SEARCH, NONE }
fun optionLabel(option: HomeDataOption): String = when (option) {
    HomeDataOption.MONTH_BALANCE -> "本月结余"
    HomeDataOption.MONTH_INCOME -> "本月收入"
    HomeDataOption.MONTH_EXPENSE -> "本月支出"
    HomeDataOption.WEEK_EXPENSE -> "本周支出"
    HomeDataOption.TODAY_EXPENSE -> "今日支出"
    HomeDataOption.WEEK_INCOME -> "本周收入"
    HomeDataOption.TODAY_INCOME -> "今日收入"
    HomeDataOption.YEAR_BALANCE -> "年度结余"
    HomeDataOption.YEAR_EXPENSE -> "年度支出"
    HomeDataOption.NONE -> "无"
}
fun optionValue(option: HomeDataOption, s: HomeSummary, todayExpense: Long, weekExpense: Long, todayIncome: Long, weekIncome: Long, yearIncome: Long, yearExpense: Long): Long = when (option) {
    HomeDataOption.MONTH_BALANCE -> s.balance
    HomeDataOption.MONTH_INCOME -> s.income
    HomeDataOption.MONTH_EXPENSE -> s.expense
    HomeDataOption.WEEK_EXPENSE -> weekExpense
    HomeDataOption.TODAY_EXPENSE -> todayExpense
    HomeDataOption.WEEK_INCOME -> weekIncome
    HomeDataOption.TODAY_INCOME -> todayIncome
    HomeDataOption.YEAR_BALANCE -> yearIncome - yearExpense
    HomeDataOption.YEAR_EXPENSE -> yearExpense
    HomeDataOption.NONE -> 0L
}
