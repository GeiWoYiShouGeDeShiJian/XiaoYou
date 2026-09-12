package com.example.jizhangruanjian.ui.home
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.R
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.model.CoverTextColor
import com.example.jizhangruanjian.data.model.HomeConfig
import com.example.jizhangruanjian.data.model.HomeDataOption
import com.example.jizhangruanjian.data.model.HomeSummary
import com.example.jizhangruanjian.data.model.PullDownAction
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.model.optionLabel
import com.example.jizhangruanjian.data.model.optionValue
import com.example.jizhangruanjian.data.model.normalizeModuleOrder
import com.example.jizhangruanjian.domain.model.BudgetState
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.example.jizhangruanjian.ui.account.LedgerViewModel
import com.example.jizhangruanjian.ui.components.AppLoading
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.components.EmptyState
import com.example.jizhangruanjian.ui.components.ImageViewer
import com.example.jizhangruanjian.ui.components.MoneyBookCard
import com.example.jizhangruanjian.ui.components.SectionTitle
import com.example.jizhangruanjian.ui.components.SourceBadge
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
import com.example.jizhangruanjian.ui.theme.OverlayColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel(), ledgerViewModel: LedgerViewModel = hiltViewModel(), onEdit: (TransactionDisplay) -> Unit = {}, onDelete: (TransactionDisplay) -> Unit = {}, onCopy: (TransactionDisplay) -> Unit = {}, onRecategorize: (TransactionDisplay, Long) -> Unit = { _, _ -> }, onAddRecord: () -> Unit = {}, onMenuClick: () -> Unit = {}, onSearch: () -> Unit = {}, onOpenMonthDetail: () -> Unit = {}, onMoreBudget: () -> Unit = {}, onOpenBudgetDetail: () -> Unit = {}, onNavigate: (String) -> Unit = {}, onCustomizeHome: () -> Unit = {}, onOpenCalendar: () -> Unit = {}, onOpenLedgerPicker: () -> Unit = {}) {
    val summary by viewModel.summary.collectAsState()
    val recent by viewModel.recent.collectAsState()
    val totalBudget by viewModel.totalBudget.collectAsState()
    val todayExpense by viewModel.todayExpense.collectAsState()
    val weekExpense by viewModel.weekExpense.collectAsState()
    val todayIncome by viewModel.todayIncome.collectAsState()
    val weekIncome by viewModel.weekIncome.collectAsState()
    val yearIncome by viewModel.yearIncome.collectAsState()
    val yearExpense by viewModel.yearExpense.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val config by viewModel.config.collectAsState()
    val ledgers by ledgerViewModel.ledgers.collectAsState()
    val currentId by ledgerViewModel.currentId.collectAsState()
    val currentLedger = ledgers.firstOrNull { it.id == currentId } ?: ledgers.firstOrNull()
    val backupViewModel: com.example.jizhangruanjian.ui.settings.BackupViewModel = hiltViewModel()
    LaunchedEffect(Unit) { backupViewModel.maybeAutoBackup() }
    var recatTarget by remember { mutableStateOf<TransactionDisplay?>(null) }
    var viewerImages by remember { mutableStateOf<List<String>?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val ledgerTint = currentLedger?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
    val homeContent: @Composable () -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)) {
                IconButton(onClick = onMenuClick) { Icon(Icons.Filled.Menu, contentDescription = "菜单") }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { onOpenLedgerPicker() }) {
                    Text(currentLedger?.name ?: "账本", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ledgerTint)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "切换账本", tint = ledgerTint)
                }
                IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = AppSpacing.lg, end = AppSpacing.lg, bottom = AppSpacing.lg)) {
                summary?.let { s ->
                    item { DataOverviewCard(config = config, s = s, todayExpense = todayExpense, weekExpense = weekExpense, todayIncome = todayIncome, weekIncome = weekIncome, yearIncome = yearIncome, yearExpense = yearExpense, onClick = { if (config.clickCoverToSwitchLedger) onOpenLedgerPicker() else onCustomizeHome() }) }
                    normalizeModuleOrder(config.moduleOrder).forEach { module ->
                        when (module) {
                            "today_stats" -> item(key = "today_stats") { ModuleGap { TodayStatsBar(todayIncome, todayExpense, onOpenCalendar) } }
                            "budget" -> if (config.showBudget) item(key = "budget") { ModuleGap { BudgetCard(totalBudget, config.hideMoreBudgetButton, onMoreBudget, onOpenBudgetDetail) } }
                            "quick_actions" -> if (config.showQuickActions) item(key = "quick_actions") { ModuleGap { QuickEntranceCard(onNavigate = onNavigate) } }
                            "recent_transactions" -> if (config.showRecentTransactions) recentSection(recent, onEdit, onDelete, onCopy, { recatTarget = it }, { viewerImages = it }, onSearch, onOpenMonthDetail, onAddRecord)
                        }
                    }
                }
                if (summary == null) { item { AppLoading(modifier = Modifier.fillMaxWidth()) } }
            }
        }
    }
    if (config.pullDownAction != PullDownAction.NONE) {
        PullToRefreshBox(isRefreshing = refreshing, onRefresh = {
            when (config.pullDownAction) {
                PullDownAction.REFRESH -> { refreshing = true; viewModel.refresh(); scope.launch { delay(600); refreshing = false } }
                PullDownAction.QUICK_RECORD -> onAddRecord()
                PullDownAction.SEARCH -> onSearch()
                PullDownAction.NONE -> {}
            }
        }, modifier = Modifier.fillMaxSize()) { homeContent() }
    } else homeContent()
    recatTarget?.let { tx ->
        val expCategories = categories.filter { it.type == CategoryType.EXPENSE && it.parentId == null }
        AlertDialog(
            onDismissRequest = { recatTarget = null },
            title = { Text("归类到") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    expCategories.forEach { c ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onRecategorize(tx, c.id); recatTarget = null }.padding(vertical = AppSpacing.sm)) {
                            CategoryIcon(c.icon, size = 22.dp, fontSize = 14.sp)
                            Spacer(Modifier.width(AppSpacing.sm))
                            Text(c.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { recatTarget = null }) { Text("取消") } }
        )
    }
    viewerImages?.let { ImageViewer(images = it, onDismiss = { viewerImages = null }) }
}
@Composable
private fun DataOverviewCard(config: HomeConfig, s: HomeSummary, todayExpense: Long, weekExpense: Long, todayIncome: Long, weekIncome: Long, yearIncome: Long, yearExpense: Long, onClick: () -> Unit) {
    val textColor = if (config.coverTextColor == CoverTextColor.LIGHT) Color.White else OverlayColors.coverText
    val subColor = if (config.coverTextColor == CoverTextColor.LIGHT) Color.White.copy(alpha = 0.85f) else OverlayColors.coverText.copy(alpha = 0.75f)
    val coverRes = coverResId(config.ledgerCover)
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(MaterialTheme.shapes.large).clickable(onClick = onClick)) {
            if (coverRes != null) {
                Image(painter = painterResource(id = coverRes), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary))
            }
            Column(modifier = Modifier.fillMaxSize().padding(AppSpacing.lg)) {
                Text(optionLabel(config.dataOne), style = MaterialTheme.typography.bodyMedium, color = subColor)
                Spacer(Modifier.height(AppSpacing.xs))
                Text(Formatters.yuanText(optionValue(config.dataOne, s, todayExpense, weekExpense, todayIncome, weekIncome, yearIncome, yearExpense)), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("${optionLabel(config.dataTwo)} ${Formatters.yuanText(optionValue(config.dataTwo, s, todayExpense, weekExpense, todayIncome, weekIncome, yearIncome, yearExpense))}", style = MaterialTheme.typography.bodyMedium, color = subColor, modifier = Modifier.weight(1f))
                    Text("${optionLabel(config.dataThree)} ${Formatters.yuanText(optionValue(config.dataThree, s, todayExpense, weekExpense, todayIncome, weekIncome, yearIncome, yearExpense))}", style = MaterialTheme.typography.bodyMedium, color = subColor)
                }
            }
        }
    }
}
@Composable
private fun TodayStatsBar(todayIncome: Long, todayExpense: Long, onOpenCalendar: () -> Unit) {
    val now = LocalDate.now()
    val weekLabel = now.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.CHINA)
    MoneyBookCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenCalendar).padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)) {
            Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(AppSpacing.md))
            Text(now.format(DateTimeFormatter.ofPattern("MM-dd")) + " $weekLabel", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text("收 ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(Formatters.yuanText(todayIncome), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Text("支 ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(Formatters.yuanText(todayExpense), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error)
        }
    }
}
private fun coverResId(cover: String): Int? = when (cover) {
    "cover_pencils" -> R.drawable.cover_pencils
    else -> null
}
@Composable
private fun BudgetCard(totalBudget: BudgetState?, hideMoreButton: Boolean, onMoreBudget: () -> Unit, onOpenDetail: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        MoneyBookCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenDetail)) {
            Column(modifier = Modifier.padding(AppSpacing.lg)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(AppSize.iconXLarge).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("本月预算", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    totalBudget?.let { b ->
                        Text("剩余 ${Formatters.yuanText(b.available)}", style = MaterialTheme.typography.bodyMedium, color = if (b.over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(10.dp))
                totalBudget?.let { b ->
                    val budgetProgress by animateFloatAsState(targetValue = (b.usagePercent / 100f).coerceIn(0f, 1f), label = "budgetProgress")
                    LinearProgressIndicator(progress = { budgetProgress }, color = if (b.over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(AppSpacing.sm))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("${Formatters.yuanText(b.spent)} / ${Formatters.yuanText(b.available)}", style = MaterialTheme.typography.bodyMedium, color = if (b.over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        val nowDay = LocalDate.now()
                        val daysLeft = (nowDay.lengthOfMonth() - nowDay.dayOfMonth + 1).coerceAtLeast(1)
                        Text("剩余日均 ${Formatters.yuanText(if (b.over) 0L else b.available / daysLeft)}", style = MaterialTheme.typography.bodyMedium, color = if (b.over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } ?: Text("未设置预算", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (!hideMoreButton) {
            Spacer(Modifier.height(AppSpacing.sm))
            Box(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onMoreBudget).padding(vertical = AppSpacing.md), contentAlignment = Alignment.Center) {
                Text("更多预算 ›", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
private fun LazyListScope.recentSection(recent: List<TransactionDisplay>, onEdit: (TransactionDisplay) -> Unit, onDelete: (TransactionDisplay) -> Unit, onCopy: (TransactionDisplay) -> Unit, onRecategorize: (TransactionDisplay) -> Unit, onViewImages: (List<String>) -> Unit, onSearch: () -> Unit, onOpenMonthDetail: () -> Unit, onAddRecord: () -> Unit) {
    item {
        ModuleGap { SectionTitle("最近交易") }
        Spacer(Modifier.height(AppSpacing.sm))
    }
    if (recent.isEmpty()) {
        item {
            EmptyState(icon = Icons.Filled.Add, title = "还没有记录", subtitle = "点击下方 +，开始你的第一笔吧", ctaText = "记一笔", onCta = onAddRecord)
            Spacer(Modifier.height(AppSpacing.sm))
        }
    } else {
        val grouped = recent.groupBy { dateOf(it.tradeDate) }.toSortedMap(compareByDescending { it })
        grouped.forEach { (date, list) ->
            item(key = "date-$date") {
                DateHeader(date, list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount })
            }
            items(list, key = { it.id }) { tx ->
                DismissableTransactionItem(tx = tx, modifier = Modifier.animateItem(), onEdit = onEdit, onDelete = onDelete, onCopy = onCopy, onRecategorize = onRecategorize, onViewImages = onViewImages)
            }
        }
        item {
            Spacer(Modifier.height(AppSpacing.sm))
            Text("更多明细 >", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onOpenMonthDetail() })
        }
    }
}
@Composable
private fun ModuleGap(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 14.dp)) { content() }
}
@Composable
private fun TransactionRow(tx: TransactionDisplay, onViewImages: (List<String>) -> Unit = {}) {
    val isTransfer = tx.type == TransactionType.TRANSFER
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) {
        CategoryIcon(tx.categoryIcon, size = 40.dp, fontSize = 20.sp)
        Spacer(Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isTransfer) "账户互转" else tx.categoryName, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(6.dp))
                SourceBadge(tx.source)
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(Formatters.mdHM(tx.tradeDate), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (tx.note.isNotBlank()) {
                    Spacer(Modifier.width(6.dp))
                    Text(tx.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
        }
        if (tx.images.isNotEmpty()) {
            Box(modifier = Modifier.size(AppSize.iconLarge).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onViewImages(tx.images) }, contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Image, contentDescription = "查看图片", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(6.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            val negative = tx.type == TransactionType.EXPENSE
            Text(if (isTransfer) Formatters.yuanText(tx.amount) else "${if (negative) "-" else "+"}${Formatters.yuanText(tx.amount)}", fontWeight = FontWeight.Medium, color = if (isTransfer) MaterialTheme.colorScheme.onSurface else if (negative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text(if (isTransfer) "${tx.accountName}→${tx.toAccountName}" else tx.accountName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DismissableTransactionItem(tx: TransactionDisplay, modifier: Modifier, onEdit: (TransactionDisplay) -> Unit, onDelete: (TransactionDisplay) -> Unit, onCopy: (TransactionDisplay) -> Unit, onRecategorize: (TransactionDisplay) -> Unit, onViewImages: (List<String>) -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { value ->
        when (value) {
            SwipeToDismissBoxValue.EndToStart -> { onDelete(tx); true }
            SwipeToDismissBoxValue.StartToEnd -> { onEdit(tx); true }
            else -> false
        }
    })
    SwipeToDismissBox(state = dismissState, enableDismissFromStartToEnd = true, enableDismissFromEndToStart = true, modifier = modifier, backgroundContent = {
        val isEdit = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
        Box(modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium).background(if (isEdit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error), contentAlignment = if (isEdit) Alignment.CenterStart else Alignment.CenterEnd) {
            Icon(if (isEdit) Icons.Filled.Edit else Icons.Filled.Delete, contentDescription = if (isEdit) "编辑" else "删除", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }) {
        var menu by remember(tx.id) { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surface).combinedClickable(onClick = { onEdit(tx) }, onLongClick = { menu = true }).padding(horizontal = AppSpacing.md, vertical = 6.dp)) {
            TransactionRow(tx, onViewImages)
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("编辑") }, leadingIcon = { Icon(Icons.Filled.Edit, null) }, onClick = { menu = false; onEdit(tx) })
                DropdownMenuItem(text = { Text("复制") }, leadingIcon = { Icon(Icons.Filled.ContentCopy, null) }, onClick = { menu = false; onCopy(tx) })
                DropdownMenuItem(text = { Text("归类") }, leadingIcon = { Icon(Icons.Filled.Category, null) }, onClick = { menu = false; onRecategorize(tx) })
                DropdownMenuItem(text = { Text("删除") }, leadingIcon = { Icon(Icons.Filled.Delete, null) }, onClick = { menu = false; onDelete(tx) })
            }
        }
    }
}
@Composable
private fun DateHeader(date: LocalDate, expense: Long) {
    val today = LocalDate.now()
    val label = when (date) { today -> "今天"; today.minusDays(1) -> "昨天"; else -> date.format(DateTimeFormatter.ofPattern("M月d日")) }
    val week = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.CHINA)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.xs, bottom = AppSpacing.xs)) {
        Text("$label $week", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        if (expense > 0L) Text("支出 ${Formatters.yuanText(expense)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
private fun dateOf(ts: Long): LocalDate = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate()
private data class QuickEntry(val label: String, val icon: ImageVector, val route: String)
private val QUICK_ENTRIES = listOf(
    QuickEntry("周期记账", Icons.Filled.DateRange, "recurring"),
    QuickEntry("报销管理", Icons.Filled.ReceiptLong, "reimbursement"),
    QuickEntry("应收款", Icons.Filled.TrendingUp, "ar_ap"),
    QuickEntry("应付款", Icons.Filled.TrendingDown, "ar_ap"),
    QuickEntry("全部", Icons.Filled.Search, "search")
)
@Composable
private fun QuickEntranceCard(onNavigate: (String) -> Unit) {
    MoneyBookCard(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = 14.dp)) {
            QUICK_ENTRIES.forEach { entry ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { onNavigate(entry.route) }) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        Icon(entry.icon, contentDescription = entry.label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(entry.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
