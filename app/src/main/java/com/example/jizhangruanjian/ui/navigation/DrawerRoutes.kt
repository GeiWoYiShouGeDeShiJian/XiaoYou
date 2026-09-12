package com.example.jizhangruanjian.ui.navigation
import androidx.compose.runtime.Composable
import com.example.jizhangruanjian.ui.recurring.RecurringScreen
import com.example.jizhangruanjian.ui.search.SearchScreen
import com.example.jizhangruanjian.ui.trash.TrashScreen
import com.example.jizhangruanjian.ui.settings.SettingsScreen
import com.example.jizhangruanjian.ui.settings.CsvScreen
import com.example.jizhangruanjian.ui.settings.BackupScreen
import com.example.jizhangruanjian.ui.category.CategoryManageScreen
import com.example.jizhangruanjian.ui.reimbursement.ReimbursementScreen
import com.example.jizhangruanjian.ui.arap.ArApScreen
import com.example.jizhangruanjian.ui.autorecord.AutoRecordSettingsScreen
object DrawerRoutes {
    const val AUTO_BOOKKEEPING = "auto_bookkeeping"
    const val CATEGORY_MANAGE = "category_manage"
    const val RECURRING = "recurring"
    const val REIMBURSEMENT = "reimbursement"
    const val AR_AP = "ar_ap"
    const val SETTINGS = "settings"
    const val BACKUP = "backup"
    const val CSV = "csv"
    const val TRASH = "trash"
    const val SEARCH = "search"
    private val titles = mapOf(
        AUTO_BOOKKEEPING to "自动记账",
        RECURRING to "周期记账",
        REIMBURSEMENT to "报销管理",
        AR_AP to "应收应付",
        SETTINGS to "设置",
        BACKUP to "数据备份与恢复",
        CSV to "账单导入与导出",
        TRASH to "回收站",
        SEARCH to "交易搜索",
        CATEGORY_MANAGE to "分类管理")
    fun titleOf(route: String): String = titles[route] ?: route
}
@Composable
internal fun DrawerRouteContent(route: String, onBack: () -> Unit, onCustomizeHome: () -> Unit = {}, onAddLedger: () -> Unit = {}, onManageLedger: () -> Unit = {}, onOpenSettings: () -> Unit = {}, onOpenLedgerManage: () -> Unit = {}) {
    when (route) {
        DrawerRoutes.AUTO_BOOKKEEPING -> AutoRecordSettingsScreen(onBack = onBack)
        DrawerRoutes.RECURRING -> RecurringScreen(onBack = onBack)
        DrawerRoutes.SEARCH -> SearchScreen(onBack = onBack)
        DrawerRoutes.TRASH -> TrashScreen(onBack = onBack)
        DrawerRoutes.SETTINGS -> SettingsScreen(onBack = onBack, onCustomizeHome = onCustomizeHome, onOpenLedgerManage = onOpenLedgerManage)
        DrawerRoutes.BACKUP -> BackupScreen(onBack = onBack, onOpenSettings = onOpenSettings)
        DrawerRoutes.CSV -> CsvScreen(onBack = onBack, onAddLedger = onAddLedger, onManageLedger = onManageLedger)
        DrawerRoutes.CATEGORY_MANAGE -> CategoryManageScreen(onBack = onBack)
        DrawerRoutes.REIMBURSEMENT -> ReimbursementScreen(onBack = onBack)
        DrawerRoutes.AR_AP -> ArApScreen(onBack = onBack)
        else -> PlaceholderScreen(DrawerRoutes.titleOf(route), onBack = onBack)
    }
}