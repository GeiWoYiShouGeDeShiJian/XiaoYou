package com.example.jizhangruanjian.ui.mine
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.jizhangruanjian.ui.account.AccountScreen
import com.example.jizhangruanjian.ui.account.AccountTabScreen
import com.example.jizhangruanjian.ui.account.AddAccountScreen
import com.example.jizhangruanjian.ui.account.AccountDetailScreen
import com.example.jizhangruanjian.ui.account.AssetReportScreen
import com.example.jizhangruanjian.ui.account.EditAccountScreen
import com.example.jizhangruanjian.ui.account.LedgerScreen
import com.example.jizhangruanjian.ui.account.AccountSortScreen
// 「账户」Tab：账户总览 / 账户管理 / 账本管理（其余入口迁入抽屉）
@Composable
fun AccountSection(onMenuClick: () -> Unit, onNavigate: (String) -> Unit, onPickLedger: () -> Unit = {}, onSubPage: (Boolean) -> Unit = {}) {
    var screen by remember { mutableStateOf("account") }
    var detailId by remember { mutableStateOf(0L) }
    LaunchedEffect(screen) { onSubPage(screen != "account") }
    BackHandler(enabled = screen != "account") { screen = if (screen == "edit") "detail" else "account" }
    when (screen) {
        "manage" -> AccountScreen(onBack = { screen = "account" })
        "add" -> AddAccountScreen(onBack = { screen = "account" })
        "report" -> AssetReportScreen(onBack = { screen = "account" })
        "detail" -> AccountDetailScreen(accountId = detailId, onBack = { screen = "account" }, onEdit = { screen = "edit" })
        "edit" -> EditAccountScreen(accountId = detailId, onBack = { screen = "detail" }, onDeleted = { screen = "account" })
        "sort" -> AccountSortScreen(onBack = { screen = "account" })
        else -> AccountTabScreen(onMenuClick = onMenuClick, onNavigate = onNavigate, onAddAccount = { screen = "add" }, onOpenReport = { screen = "report" }, onManage = { screen = "manage" }, onPickLedger = onPickLedger, onOpenDetail = { detailId = it; screen = "detail" }, onSort = { screen = "sort" })
    }
}
