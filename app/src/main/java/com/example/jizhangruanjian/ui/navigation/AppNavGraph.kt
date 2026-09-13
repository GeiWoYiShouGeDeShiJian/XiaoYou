package com.example.jizhangruanjian.ui.navigation
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.Prefill
import com.example.jizhangruanjian.R
import com.example.jizhangruanjian.ui.home.HomeScreen
import com.example.jizhangruanjian.ui.home.CustomizeHomeScreen
import com.example.jizhangruanjian.ui.statistics.StatisticsScreen
import com.example.jizhangruanjian.ui.record.RecordScreen
import com.example.jizhangruanjian.ui.record.RecordViewModel
import com.example.jizhangruanjian.ui.mine.AccountSection
import com.example.jizhangruanjian.ui.budget.BudgetScreen
import com.example.jizhangruanjian.ui.budget.BudgetDetailScreen
import com.example.jizhangruanjian.ui.budget.BudgetDetailViewModel
import com.example.jizhangruanjian.ui.calendar.CalendarScreen
import com.example.jizhangruanjian.ui.calendar.CalendarViewModel
import com.example.jizhangruanjian.ui.ledger.LedgerAddScreen
import com.example.jizhangruanjian.ui.ledger.LedgerEditScreen
import com.example.jizhangruanjian.ui.ledger.LedgerPickerScreen
import com.example.jizhangruanjian.ui.ledger.LedgerTemplate
import com.example.jizhangruanjian.ui.ledger.LedgerTemplateScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
// 返回栈页面：所有子页面入栈，返回时逐层弹出
private sealed interface NavPage {
    data class Drawer(val route: String, val fromDrawer: Boolean = false) : NavPage
    data object Budget : NavPage
    data class BudgetDetail(val categoryId: Long?, val title: String) : NavPage
    data object CustomizeHome : NavPage
    data object Calendar : NavPage
    data object MonthDetail : NavPage
    data object LedgerPicker : NavPage
    data object LedgerManage : NavPage
    data object LedgerManageHub : NavPage
    data class CategoryManagePage(val type: com.example.jizhangruanjian.data.model.CategoryType) : NavPage
    data object LedgerTemplatePage : NavPage
    data object AccountAdd : NavPage
    data class LedgerAdd(val template: LedgerTemplate?) : NavPage
    data class LedgerEdit(val id: Long) : NavPage
}
@Composable
fun AppNavGraph() {
    val recordViewModel: RecordViewModel = hiltViewModel()
    val accounts by recordViewModel.accounts.collectAsState()
    val categories by recordViewModel.categories.collectAsState()
    val selectedType by recordViewModel.selectedType.collectAsState()
    val quickRecordEpoch by recordViewModel.quickRecordEpoch.collectAsState()
    val prefillEpoch by recordViewModel.prefillEpoch.collectAsState()
    val saveMessage by recordViewModel.saveMessage.collectAsState()
    val editingTarget by recordViewModel.editingTarget.collectAsState()
    val members by recordViewModel.members.collectAsState()
    val tags by recordViewModel.tags.collectAsState()
    val selectedImagePaths by recordViewModel.selectedImagePaths.collectAsState()
    val editingTagIds by recordViewModel.editingTagIds.collectAsState()
    // 底部三个主 Tab 用 Pager 承载：可滑动切换（一次只滑一页，首页无法一滑直达统计页）
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = 0) { 3 }
    val currentTab = pagerState.currentPage
    var accountInSub by remember { mutableStateOf(false) }
    var showRecord by remember { mutableStateOf(false) }
    var receivedPrefill by remember { mutableStateOf<Prefill?>(null) }
    val navStack = remember { mutableStateListOf<NavPage>() }
    val ledgerRouteViewModel: com.example.jizhangruanjian.ui.account.LedgerViewModel = hiltViewModel()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val budgetDetailViewModel: BudgetDetailViewModel = hiltViewModel()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // 弹栈：若弹出的是抽屉菜单进入的页面，则返回时重新打开抽屉菜单
    val pop = {
        val removed = navStack.removeLastOrNull()
        if (removed is NavPage.Drawer && removed.fromDrawer) scope.launch { drawerState.open() }
    }
    // 系统返回键：优先关闭抽屉，其次有子页面时逐层返回，栈空时交给系统（退出应用）
    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }
    BackHandler(enabled = navStack.isNotEmpty() && !drawerState.isOpen) { pop() }
    LaunchedEffect(quickRecordEpoch) { if (quickRecordEpoch > 0L) showRecord = true }
    // T10 URL Scheme：MainActivity 触发 prefillEpoch → 打开记账页并用 URL 数据预填
    LaunchedEffect(prefillEpoch) {
        if (prefillEpoch > 0L) {
            receivedPrefill = recordViewModel.consumePrefill()
            if (receivedPrefill != null) showRecord = true
        }
    }
    LaunchedEffect(editingTarget) { if (editingTarget != null) showRecord = true }
    // 打开记账页时应用「初始选中类型」偏好（编辑/预填路径不覆盖）
    LaunchedEffect(showRecord) { if (showRecord && editingTarget == null && receivedPrefill == null) recordViewModel.applyInitialType() }
    // 保存成功小浮窗：显示 0.75s 后淡出，淡出结束再清除消息
    var pillShown by remember { mutableStateOf(false) }
    var pillMsg by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(saveMessage) {
        if (saveMessage != null) {
            pillMsg = saveMessage
            pillShown = true
            delay(750)
            pillShown = false
            delay(280)
            recordViewModel.consumeSaveMessage()
            pillMsg = null
        }
    }
    val pillAlpha by animateFloatAsState(if (pillShown) 1f else 0f, tween(if (pillShown) 120 else 250), label = "pillAlpha")
    // 抽屉关闭时禁用手势（不能滑开），打开时启用手势（可滑回关闭）
    ModalNavigationDrawer(drawerState = drawerState, gesturesEnabled = drawerState.isOpen, drawerContent = {
        AppDrawerContent(onNavigate = { route -> navStack.add(NavPage.Drawer(route, fromDrawer = true)); scope.launch { drawerState.close() } }, onDismiss = { scope.launch { drawerState.close() } })
    }) {
        Scaffold(bottomBar = {
            if (navStack.isEmpty() && !accountInSub) {
                BottomNavBar(currentTab, onTab = { scope.launch { pagerState.scrollToPage(it) }; navStack.clear() })
            }
        }, floatingActionButton = {
            if (currentTab == 0 && navStack.isEmpty()) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary).clickable { showRecord = true }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = "记账", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (val top = navStack.lastOrNull()) {
                    is NavPage.LedgerTemplatePage -> LedgerTemplateScreen(onBack = { pop() }, onPick = { navStack.add(NavPage.LedgerAdd(it)) })
                    is NavPage.LedgerAdd -> LedgerAddScreen(viewModel = ledgerRouteViewModel, template = top.template, onBack = { pop() })
                    is NavPage.LedgerEdit -> LedgerEditScreen(viewModel = ledgerRouteViewModel, ledgerId = top.id, onBack = { pop() })
                    is NavPage.LedgerPicker -> LedgerPickerScreen(viewModel = ledgerRouteViewModel, onBack = { pop() }, onAdd = { navStack.add(NavPage.LedgerTemplatePage) }, onEdit = { id -> navStack.add(NavPage.LedgerEdit(id)) })
                    is NavPage.LedgerManage -> com.example.jizhangruanjian.ui.account.LedgerScreen(onBack = { pop() }, viewModel = ledgerRouteViewModel)
                    is NavPage.LedgerManageHub -> com.example.jizhangruanjian.ui.ledger.LedgerManageHubScreen(onBack = { pop() }, onOpenCategoryManage = { t -> navStack.add(NavPage.CategoryManagePage(t)) })
                    is NavPage.CategoryManagePage -> com.example.jizhangruanjian.ui.category.CategoryManageScreen(onBack = { pop() }, initialType = top.type)
                    is NavPage.AccountAdd -> com.example.jizhangruanjian.ui.account.AddAccountScreen(onBack = { pop() })
                    is NavPage.CustomizeHome -> CustomizeHomeScreen(onBack = { pop() })
                    is NavPage.Calendar -> CalendarScreen(viewModel = calendarViewModel, onBack = { pop() }, onEditTransaction = { recordViewModel.startEdit(it) })
                    is NavPage.MonthDetail -> com.example.jizhangruanjian.ui.detail.MonthDetailScreen(onBack = { pop() }, onSearch = { navStack.add(NavPage.Drawer(DrawerRoutes.SEARCH)) }, onEdit = { recordViewModel.startEdit(it) }, onCopy = { recordViewModel.copy(it) }, onDelete = { recordViewModel.delete(it) }, onAddRecord = { showRecord = true })
                    is NavPage.BudgetDetail -> {
                        LaunchedEffect(top.categoryId) { budgetDetailViewModel.setCategory(top.categoryId) }
                        BudgetDetailScreen(viewModel = budgetDetailViewModel, title = top.title, onBack = { pop() }, onOpenManage = { navStack.removeLastOrNull(); navStack.add(NavPage.Budget) }, onEditTransaction = { recordViewModel.startEdit(it) })
                    }
                    is NavPage.Budget -> BudgetScreen(onBack = { pop() }, onOpenDetail = { cid, title -> navStack.add(NavPage.BudgetDetail(cid, title)) })
                    is NavPage.Drawer -> DrawerRouteContent(top.route, onBack = { pop() }, onCustomizeHome = { navStack.add(NavPage.CustomizeHome) }, onAddLedger = { navStack.add(NavPage.LedgerTemplatePage) }, onManageLedger = { navStack.add(NavPage.LedgerManage) }, onOpenSettings = { navStack.add(NavPage.Drawer(DrawerRoutes.SETTINGS)) }, onOpenLedgerManage = { navStack.add(NavPage.LedgerManageHub) })
                    null -> androidx.compose.foundation.pager.HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 2) { page ->
                        when (page) {
                            0 -> HomeScreen(
                                onEdit = { recordViewModel.startEdit(it.id) },
                                onDelete = { recordViewModel.delete(it.id) },
                                onCopy = { recordViewModel.copy(it.id) },
                                onRecategorize = { tx, cid -> recordViewModel.recategorize(tx.id, cid) },
                                onAddRecord = { showRecord = true },
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onSearch = { navStack.add(NavPage.Drawer(DrawerRoutes.SEARCH)) },
                                onOpenMonthDetail = { navStack.add(NavPage.MonthDetail) },
                                onMoreBudget = { navStack.add(NavPage.Budget) },
                                onOpenBudgetDetail = { navStack.add(NavPage.BudgetDetail(null, "总预算")) },
                                onNavigate = { route -> navStack.add(NavPage.Drawer(route)) },
                                onCustomizeHome = { navStack.add(NavPage.CustomizeHome) },
                                onOpenCalendar = { navStack.add(NavPage.Calendar) },
                                onOpenLedgerPicker = { navStack.add(NavPage.LedgerPicker) })
                            1 -> AccountSection(onMenuClick = { scope.launch { drawerState.open() } }, onNavigate = { route -> navStack.add(NavPage.Drawer(route)) }, onPickLedger = { navStack.add(NavPage.LedgerPicker) }, onSubPage = { accountInSub = it })
                            else -> StatisticsScreen(onMenuClick = { scope.launch { drawerState.open() } }, onOpenLedgerPicker = { navStack.add(NavPage.LedgerPicker) })
                        }
                    }
                }
            }
        }
    }
    if (showRecord) {
        RecordScreen(accounts = accounts, categories = categories, members = members, tags = tags, selectedType = selectedType,
            editing = editingTarget,
            initialPrefill = receivedPrefill,
            onPrefillConsumed = { receivedPrefill = null },
            selectedImagePaths = selectedImagePaths,
            editingTagIds = editingTagIds,
            currentLedgerName = recordViewModel.currentLedgerName.collectAsState().value,
            ledgers = recordViewModel.ledgers.collectAsState().value,
            selectedLedgerId = recordViewModel.ledgerIdFlow.collectAsState().value,
            onSelectLedger = { recordViewModel.selectLedger(it) },
            onCreateCategory = { name, parent -> recordViewModel.createCategory(name, parent, com.example.jizhangruanjian.data.model.CategoryType.valueOf(recordViewModel.selectedType.value.name)) },
            onReorderCategories = { recordViewModel.reorderCategories(it) },
            onTypeChange = { recordViewModel.selectedType.value = it },
            onDismiss = { showRecord = false; recordViewModel.clearEditTarget() },
            onDelete = { editingTarget?.let { recordViewModel.delete(it.id) } },
            onSave = { draft, oldId, images -> recordViewModel.submitDraft(draft, oldId, images) },
            onSaveFinished = { showRecord = false; recordViewModel.clearEditTarget(); if (navStack.contains(NavPage.Calendar)) calendarViewModel.reload(); if (navStack.any { it is NavPage.BudgetDetail }) budgetDetailViewModel.reload() },
            onCreateAccount = { name -> recordViewModel.createAccount(name) },
            onSaveMember = { member -> recordViewModel.saveMember(member) },
            onDeleteMember = { id -> recordViewModel.deleteMember(id) },
            tagGroups = recordViewModel.tagGroups.collectAsState().value,
            onSaveTag = { tag -> recordViewModel.saveTag(tag) },
            onDeleteTag = { id -> recordViewModel.deleteTag(id) },
            onSaveTagGroup = { group -> recordViewModel.saveTagGroup(group) },
            merchants = recordViewModel.merchants.collectAsState().value,
            merchantGroups = recordViewModel.merchantGroups.collectAsState().value,
            onSaveMerchant = { merchant -> recordViewModel.saveMerchant(merchant) },
            onSaveMerchantGroup = { group -> recordViewModel.saveMerchantGroup(group) },
            onSaveMerchantOrder = { ids -> recordViewModel.reorderMerchants(ids) },
            onAddImage = { path -> recordViewModel.addImage(path) },
            onRemoveImage = { path -> recordViewModel.removeImage(path) },
            onLoadImages = { id -> recordViewModel.loadImagesForEdit(id) },
            onClearImages = { recordViewModel.clearImages() },
            onLoadTags = { id -> recordViewModel.loadTagsForEdit(id) },
            onClearTags = { recordViewModel.clearEditingTags() },
            onRememberKeyword = { cid, kw -> recordViewModel.rememberKeyword(cid, kw) })
    }
    val balanceConfirm by recordViewModel.balanceConfirmShown.collectAsState()
    val duplicateConfirm by recordViewModel.duplicateConfirmShown.collectAsState()
    if (balanceConfirm) {
        AlertDialog(onDismissRequest = { recordViewModel.cancelBalanceConfirm() }, title = { Text("余额变动确认") }, text = { Text("这笔明细的记账日期是昨天，可能无法准确联动账户余额。请选择保存方式：") }, confirmButton = { TextButton(onClick = { recordViewModel.onBalanceConfirm(true) }) { Text("仍要保存") } }, dismissButton = { TextButton(onClick = { recordViewModel.onBalanceConfirm(false) }) { Text("保存但不影响余额") } })
    }
    if (duplicateConfirm) {
        AlertDialog(onDismissRequest = { recordViewModel.onDuplicateConfirm(false) }, title = { Text("疑似重复账单") }, text = { Text("检测到内容相同的账单，仍要保存吗？") }, confirmButton = { TextButton(onClick = { recordViewModel.onDuplicateConfirm(true) }) { Text("仍要保存") } }, dismissButton = { TextButton(onClick = { recordViewModel.onDuplicateConfirm(false) }) { Text("取消") } })
    }
    pillMsg?.let { msg ->
        Popup(alignment = Alignment.BottomCenter) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.Black.copy(alpha = 0.75f), shadowElevation = 6.dp, modifier = Modifier.padding(bottom = 120.dp).alpha(pillAlpha)) {
                Text(msg, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
            }
        }
    }
}
@Composable
private fun BottomNavBar(currentTab: Int, onTab: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        NavItem("首页", painterResource(R.drawable.ic_tab_home), currentTab == 0) { onTab(0) }
        NavItem("账户", painterResource(R.drawable.ic_tab_account), currentTab == 1) { onTab(1) }
        NavItem("统计", painterResource(R.drawable.ic_tab_stats), currentTab == 2) { onTab(2) }
    }
}
@Composable
private fun NavItem(label: String, icon: androidx.compose.ui.graphics.painter.Painter, selected: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(30.dp), tint = Color.Unspecified)
        Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
