package com.example.jizhangruanjian.ui.calendar
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.R
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.core.util.ImageSaver
import com.example.jizhangruanjian.data.model.HomeConfig
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.components.ImageViewer
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.abs
private val PALETTE = listOf(0xFF2E7D32, 0xFFD32F2F, 0xFF1565C0, 0xFFEF6C00, 0xFF00897B, 0xFFD81B60, 0xFF6A1B9A, 0xFF455A64)
private val LUNAR_MONTHS = listOf("正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月")
private val LUNAR_DAYS = listOf("初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十")
private val SOLAR_FESTIVALS = mapOf(1 to 1 to "元旦节", 5 to 1 to "劳动节", 10 to 1 to "国庆节")
private val LUNAR_FESTIVALS = mapOf(1 to 1 to "春节", 1 to 15 to "元宵节", 5 to 5 to "端午节", 7 to 7 to "七夕", 8 to 15 to "中秋节", 9 to 9 to "重阳节", 12 to 8 to "腊八节")
private fun lunarText(date: LocalDate): String {
    val cal = android.icu.util.ChineseCalendar(android.icu.util.TimeZone.getTimeZone(ZoneId.systemDefault().id))
    cal.timeInMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val leap = cal.get(android.icu.util.ChineseCalendar.IS_LEAP_MONTH) == 1
    val m = cal.get(android.icu.util.ChineseCalendar.MONTH) + 1
    val d = cal.get(android.icu.util.ChineseCalendar.DAY_OF_MONTH)
    SOLAR_FESTIVALS[date.monthValue to date.dayOfMonth]?.let { return it }
    if (!leap) {
        LUNAR_FESTIVALS[m to d]?.let { return it }
        if (m == 12 && d == cal.getActualMaximum(android.icu.util.ChineseCalendar.DAY_OF_MONTH)) return "除夕"
    }
    return if (d == 1) (if (leap) "闰" else "") + LUNAR_MONTHS.getOrNull(m - 1).orEmpty() else LUNAR_DAYS.getOrNull(d - 1).orEmpty()
}
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel(), onBack: () -> Unit, onEditTransaction: (Long) -> Unit) {
    val month by viewModel.month.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val cells by viewModel.cells.collectAsState()
    val dayTx by viewModel.dayTx.collectAsState()
    val config by viewModel.store.config.collectAsState()
    var showAdv by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var viewerImages by remember { mutableStateOf<List<String>?>(null) }
    var showSort by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf(0) }
    // 每次进入日历页默认回到当前月份与今天
    LaunchedEffect(Unit) { viewModel.backToToday() }
    val weekStart = config.calendarWeekStart
    val profitC = Color(config.calendarProfitColor)
    val lossC = Color(config.calendarLossColor)
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        // 整页统一滚动：顶部栏、日历、筛选与当日明细作为一个整体
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 12.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                IconButton(onClick = viewModel::prevMonth) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上月") }
                Text("${month.year}年${month.monthValue}月 ▾", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { showMonthPicker = true })
                IconButton(onClick = viewModel::nextMonth) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下月") }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showAdv = true }) { Icon(painterResource(R.drawable.ic_filter_funnel), contentDescription = "高级筛选") }
                IconButton(onClick = { showSettings = true }) { Icon(Icons.Filled.Settings, contentDescription = "设置") }
            }
            val monthCells = cells.values
            val mIncome = monthCells.sumOf { it.income }
            val mExpense = monthCells.sumOf { it.expense }
            Text("收入 ${Formatters.yuanText(mIncome)}  支出 ${Formatters.yuanText(mExpense)}  结余 ${Formatters.yuanText(mIncome - mExpense)}", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            val dayNames = listOf("日", "一", "二", "三", "四", "五", "六")
            val weekLabels = (0..6).map { i -> dayNames[(weekStart + i) % 7] }
            Row(modifier = Modifier.fillMaxWidth()) {
                weekLabels.forEach { w ->
                    Text(w, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(4.dp))
            val today = LocalDate.now()
            val leading = (month.atDay(1).dayOfWeek.value % 7 + 7 - weekStart) % 7
            val daysInMonth = month.lengthOfMonth()
            val allDays: List<LocalDate?> = List(leading) { null } + (1..daysInMonth).map { month.atDay(it) }
            allDays.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { d ->
                        Box(modifier = Modifier.weight(1f).aspectRatio(0.82f).padding(1.5.dp)) {
                            if (d != null) DayCellView(d, today, selected, cells[d], filter, profitC, lossC, config.calendarShowLunar, onClick = { viewModel.select(d) })
                        }
                    }
                    repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                listOf("收支", "支出", "收入", "结余").forEachIndexed { i, label ->
                    FilterChip(selected = filter == i, onClick = { viewModel.setFilter(i) }, label = { Text(label, fontSize = 13.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary))
                }
                if (month != YearMonth.now()) {
                    FilterChip(selected = false, onClick = viewModel::backToToday, label = { Text("回到本月", fontSize = 13.sp) })
                }
            }
            Spacer(Modifier.height(8.dp))
            val weekCn = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")[selected.dayOfWeek.value % 7]
            val headText = if (selected == today) "今天 $weekCn" else "${selected.monthValue}月${selected.dayOfMonth}日 $weekCn"
            val selCell = cells[selected]
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(headText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                selCell?.let {
                    val net = it.income - it.expense
                    Text("结余 ${Formatters.yuanText(net)}", style = MaterialTheme.typography.bodyMedium, color = if (net < 0) lossC else profitC)
                }
                Box {
                    IconButton(onClick = { showSort = true }, modifier = Modifier.size(28.dp)) {
                        Icon(painterResource(R.drawable.ic_sort_arrows), contentDescription = "排序", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = showSort, onDismissRequest = { showSort = false }) {
                        listOf("时间最新优先", "时间最早优先", "金额从高到低", "金额从低到高").forEachIndexed { i, label ->
                            DropdownMenuItem(text = { Text(label, color = if (sortMode == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }, onClick = { sortMode = i; showSort = false })
                        }
                    }
                }
            }
            val txs = dayTx[selected].orEmpty().let { l -> when (sortMode) {
                1 -> l.sortedBy { it.tradeDate }
                2 -> l.sortedByDescending { it.amount }
                3 -> l.sortedBy { it.amount }
                else -> l.sortedByDescending { it.tradeDate }
            } }
            if (txs.isEmpty()) {
                Text("当日无交易", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp))
            } else {
                txs.forEach { t -> CalendarTxRow(t, profitC, lossC, onViewImages = { viewerImages = it }, onClick = { onEditTransaction(t.id) }) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    viewerImages?.let { ImageViewer(images = it, onDismiss = { viewerImages = null }) }
    if (showMonthPicker) MonthPickerSheet(month, onDismiss = { showMonthPicker = false }, onConfirm = { ym -> viewModel.select(if (ym == YearMonth.now()) LocalDate.now() else ym.atDay(1)); showMonthPicker = false }, onToday = viewModel::backToToday)
    if (showAdv) AdvancedFilterDialog(viewModel, month, onDismiss = { showAdv = false })
    if (showSettings) SettingsDialog(config, viewModel, onDismiss = { showSettings = false })
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthPickerSheet(initial: YearMonth, onDismiss: () -> Unit, onConfirm: (YearMonth) -> Unit, onToday: () -> Unit) {
    val years = remember(initial) { ((initial.year - 5)..(initial.year + 5)).toList() }
    var y by remember { mutableStateOf(initial.year) }
    var m by remember { mutableStateOf(initial.monthValue) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text("选择月份", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                WheelPicker(values = years, initialIndex = years.indexOf(initial.year), text = { "$it 年" }, onSnap = { y = years[it] })
                WheelPicker(values = (1..12).toList(), initialIndex = initial.monthValue - 1, text = { "%02d月".format(it) }, onSnap = { m = it + 1 })
            }
            OutlinedButton(onClick = { onToday(); onDismiss() }, modifier = Modifier.padding(vertical = 16.dp)) { Text("此刻") }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(44.dp)) { Text("取消") }
                Button(onClick = { onConfirm(YearMonth.of(y, m)); onDismiss() }, modifier = Modifier.weight(1f).height(44.dp)) { Text("确定") }
            }
        }
    }
}
@Composable
private fun WheelPicker(values: List<Int>, initialIndex: Int, text: (Int) -> String, onSnap: (Int) -> Unit) {
    val itemH = 44.dp
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) { listState.scrollToItem(initialIndex) }
    val centerIdx by remember { derivedStateOf {
        val info = listState.layoutInfo
        val c = info.viewportStartOffset + (info.viewportEndOffset - info.viewportStartOffset) / 2
        info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - c) }?.index ?: 0
    } }
    LaunchedEffect(centerIdx) { onSnap(centerIdx) }
    LazyColumn(state = listState, flingBehavior = rememberSnapFlingBehavior(listState), modifier = Modifier.height(itemH * 3).width(92.dp), contentPadding = PaddingValues(vertical = itemH)) {
        items(values.size) { i ->
            val sel = i == centerIdx
            Box(modifier = Modifier.height(itemH), contentAlignment = Alignment.Center) {
                Text(text(values[i]), fontSize = if (sel) 20.sp else 15.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            }
        }
    }
}
@Composable
private fun DayCellView(d: LocalDate, today: LocalDate, selected: LocalDate, cell: CalendarViewModel.DayCell?, filter: Int, profitC: Color, lossC: Color, showLunar: Boolean, onClick: () -> Unit) {
    val bg = when {
        d == selected -> MaterialTheme.colorScheme.secondaryContainer
        cell != null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else -> Color.Transparent
    }
    Column(modifier = Modifier.fillMaxSize().background(bg, RoundedCornerShape(8.dp)).border(width = if (d == today) 1.5.dp else 0.dp, color = if (d == today) MaterialTheme.colorScheme.primary else Color.Transparent, shape = RoundedCornerShape(8.dp)).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("${d.dayOfMonth}", fontSize = 13.sp, fontWeight = if (d == today) FontWeight.Bold else FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        if (showLunar) Text(lunarText(d), fontSize = 7.sp, lineHeight = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        cell?.let {
            if (filter == 0 || filter == 2) {
                if (it.income > 0) Text(Formatters.yuanText(it.income), fontSize = 8.sp, lineHeight = 9.sp, color = profitC, maxLines = 1)
            }
            if (filter == 0 || filter == 1) {
                if (it.expense > 0) Text(Formatters.yuanText(it.expense), fontSize = 8.sp, lineHeight = 9.sp, color = lossC, maxLines = 1)
            }
            if (filter == 3) {
                val net = it.income - it.expense
                if (net != 0L) Text(Formatters.yuanText(net), fontSize = 8.sp, lineHeight = 9.sp, color = if (net < 0) lossC else profitC, maxLines = 1)
            }
        }
    }
}
@Composable
private fun CalendarTxRow(t: TransactionDisplay, profitC: Color, lossC: Color, onViewImages: (List<String>) -> Unit = {}, onClick: () -> Unit) {
    val amountColor = when (t.type) {
        TransactionType.EXPENSE -> lossC
        TransactionType.INCOME -> profitC
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val sign = if (t.type == TransactionType.EXPENSE) "-" else if (t.type == TransactionType.INCOME) "+" else ""
    val time = Formatters.timeHM(t.tradeDate)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp)) {
        CategoryIcon(t.categoryIcon, size = 36.dp, fontSize = 16.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(t.note.ifBlank { t.categoryName }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (t.images.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    t.images.take(3).forEach { p -> ThumbnailImage(p, Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).clickable { onViewImages(t.images) }) }
                    if (t.images.size > 3) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onViewImages(t.images) }, contentAlignment = Alignment.Center) {
                            Text("+${t.images.size - 3}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$sign${Formatters.yuanText(t.amount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = amountColor)
            Text(if (t.type == TransactionType.TRANSFER) "转账" else t.accountName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable
private fun ThumbnailImage(path: String, modifier: Modifier) {
    val bmp by produceState<Bitmap?>(initialValue = null, key1 = path) { value = ImageSaver.loadBitmap(path, 128) }
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (bmp != null) Image(bitmap = bmp!!.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Text("图片", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
private fun FilterRow(label: String, value: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text("$value ›", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedFilterDialog(viewModel: CalendarViewModel, month: YearMonth, onDismiss: () -> Unit) {
    val current by viewModel.adv.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val ledgers by viewModel.ledgers.collectAsState()
    val members by viewModel.members.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val hasPreset by viewModel.hasAdvPreset.collectAsState()
    var ledgerId by remember { mutableStateOf(current.ledgerId) }
    var accountId by remember { mutableStateOf(current.accountId) }
    var categoryId by remember { mutableStateOf(current.categoryId) }
    var memberId by remember { mutableStateOf(current.memberId) }
    var tagId by remember { mutableStateOf(current.tagId) }
    var currency by remember { mutableStateOf(current.currency) }
    var minText by remember { mutableStateOf(current.minAmount?.let { Formatters.yuanText(it) } ?: "") }
    var maxText by remember { mutableStateOf(current.maxAmount?.let { Formatters.yuanText(it) } ?: "") }
    var note by remember { mutableStateOf(current.note) }
    var merchant by remember { mutableStateOf(current.merchant) }
    var withImage by remember { mutableStateOf(current.onlyWithImage) }
    var onlyRefund by remember { mutableStateOf(current.onlyRefund) }
    var onlyDiscount by remember { mutableStateOf(current.onlyDiscount) }
    var onlyNotInSummary by remember { mutableStateOf(current.onlyNotInSummary) }
    var withLocation by remember { mutableStateOf(current.onlyWithLocation) }
    var reimbursement by remember { mutableStateOf(current.reimbursement) }
    var paymentStatus by remember { mutableStateOf(current.paymentStatus) }
    var source by remember { mutableStateOf(current.source) }
    var includeInBudget by remember { mutableStateOf(current.includeInBudget) }
    var ledgerMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var accountMenu by remember { mutableStateOf(false) }
    var memberMenu by remember { mutableStateOf(false) }
    var currencyMenu by remember { mutableStateOf(false) }
    var reimbMenu by remember { mutableStateOf(false) }
    var payMenu by remember { mutableStateOf(false) }
    var sourceMenu by remember { mutableStateOf(false) }
    var budgetMenu by remember { mutableStateOf(false) }
    var locationMenu by remember { mutableStateOf(false) }
    var tagMenu by remember { mutableStateOf(false) }
    val ledgerLabel = ledgerId?.let { id -> ledgers.firstOrNull { it.id == id }?.name } ?: "全部"
    val categoryLabel = categoryId?.let { id -> categories.firstOrNull { it.id == id }?.name } ?: "全部"
    val accountLabel = accountId?.let { id -> accounts.firstOrNull { it.id == id }?.name } ?: "全部"
    val memberLabel = memberId?.let { id -> members.firstOrNull { it.id == id }?.name } ?: "全部"
    val tagLabel = tagId?.let { id -> tags.firstOrNull { it.id == id }?.name } ?: "全部"
    val reimbLabel = when (reimbursement) { 0 -> "可报销"; 1 -> "已报销"; 2 -> "未报销"; else -> "全部" }
    val payLabel = when (paymentStatus) { 0 -> "已支付"; 1 -> "未支付"; else -> "全部" }
    val sourceLabel = when (source) { 0 -> "手动"; 1 -> "语音"; 2 -> "自动"; else -> "全部" }
    val budgetLabel = when (includeInBudget) { 0 -> "计入预算"; 1 -> "不计入预算"; else -> "全部" }
    val locationLabel = if (withLocation) "有位置" else "全部"
    fun buildFilter() = CalendarViewModel.AdvFilter(type = current.type, ledgerId = ledgerId, accountId = accountId, categoryId = categoryId, memberId = memberId, tagId = tagId, merchant = merchant.trim(), currency = currency, reimbursement = reimbursement, paymentStatus = paymentStatus, minAmount = minText.toDoubleOrNull()?.let { (it * 100).toLong() }, maxAmount = maxText.toDoubleOrNull()?.let { (it * 100).toLong() }, onlyWithImage = withImage, onlyRefund = onlyRefund, onlyDiscount = onlyDiscount, onlyNotInSummary = onlyNotInSummary, onlyWithLocation = withLocation, note = note.trim(), source = source, includeInBudget = includeInBudget)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).statusBarsPadding().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                Text("高级筛选", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.saveAdvPreset(buildFilter()) }) { Text("存为常用", color = MaterialTheme.colorScheme.primary) }
            }
            Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("金额区间", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = minText, onValueChange = { minText = it }, placeholder = { Text("最低金额", fontSize = 13.sp) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.width(110.dp))
                        Text("  -  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(value = maxText, onValueChange = { maxText = it }, placeholder = { Text("最高金额", fontSize = 13.sp) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.width(110.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        FilterChip(selected = withImage, onClick = { withImage = !withImage }, label = { Text("有图片", fontSize = 13.sp) })
                        FilterChip(selected = onlyRefund, onClick = { onlyRefund = !onlyRefund }, label = { Text("有退款", fontSize = 13.sp) })
                        FilterChip(selected = onlyDiscount, onClick = { onlyDiscount = !onlyDiscount }, label = { Text("有优惠", fontSize = 13.sp) })
                        FilterChip(selected = onlyNotInSummary, onClick = { onlyNotInSummary = !onlyNotInSummary }, label = { Text("不计入收支", fontSize = 13.sp) })
                    }
                }
                Spacer(Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp)) {
                    Box {
                        FilterRow("账本", ledgerLabel, onClick = { ledgerMenu = true })
                        DropdownMenu(expanded = ledgerMenu, onDismissRequest = { ledgerMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { ledgerId = null; ledgerMenu = false })
                            ledgers.forEach { l -> DropdownMenuItem(text = { Text(l.name) }, onClick = { ledgerId = l.id; ledgerMenu = false }) }
                        }
                    }
                    Box {
                        FilterRow("类别", categoryLabel, onClick = { categoryMenu = true })
                        DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { categoryId = null; categoryMenu = false })
                            categories.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { categoryId = c.id; categoryMenu = false }) }
                        }
                    }
                    Box {
                        FilterRow("账户", accountLabel, onClick = { accountMenu = true })
                        DropdownMenu(expanded = accountMenu, onDismissRequest = { accountMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { accountId = null; accountMenu = false })
                            accounts.forEach { a -> DropdownMenuItem(text = { Text(a.name) }, onClick = { accountId = a.id; accountMenu = false }) }
                        }
                    }
                    Box {
                        FilterRow("标签", tagLabel, onClick = { tagMenu = true })
                        DropdownMenu(expanded = tagMenu, onDismissRequest = { tagMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { tagId = null; tagMenu = false })
                            tags.forEach { t -> DropdownMenuItem(text = { Text(t.name) }, onClick = { tagId = t.id; tagMenu = false }) }
                        }
                    }
                    Box {
                        FilterRow("角色", memberLabel, onClick = { memberMenu = true })
                        DropdownMenu(expanded = memberMenu, onDismissRequest = { memberMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { memberId = null; memberMenu = false })
                            members.forEach { mb -> DropdownMenuItem(text = { Text(mb.name) }, onClick = { memberId = mb.id; memberMenu = false }) }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text("商家", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = merchant, onValueChange = { merchant = it }, placeholder = { Text("输入商家关键词", fontSize = 13.sp) }, singleLine = true, modifier = Modifier.width(170.dp))
                    }
                    Box {
                        FilterRow("币种", currencyLabel(currency), onClick = { currencyMenu = true })
                        DropdownMenu(expanded = currencyMenu, onDismissRequest = { currencyMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { currency = null; currencyMenu = false })
                            DropdownMenuItem(text = { Text("CNY") }, onClick = { currency = "CNY"; currencyMenu = false })
                        }
                    }
                    Box {
                        FilterRow("报销", reimbLabel, onClick = { reimbMenu = true })
                        DropdownMenu(expanded = reimbMenu, onDismissRequest = { reimbMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { reimbursement = null; reimbMenu = false })
                            DropdownMenuItem(text = { Text("可报销") }, onClick = { reimbursement = 0; reimbMenu = false })
                            DropdownMenuItem(text = { Text("已报销") }, onClick = { reimbursement = 1; reimbMenu = false })
                            DropdownMenuItem(text = { Text("未报销") }, onClick = { reimbursement = 2; reimbMenu = false })
                        }
                    }
                    Box {
                        FilterRow("预算", budgetLabel, onClick = { budgetMenu = true })
                        DropdownMenu(expanded = budgetMenu, onDismissRequest = { budgetMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { includeInBudget = null; budgetMenu = false })
                            DropdownMenuItem(text = { Text("计入预算") }, onClick = { includeInBudget = 0; budgetMenu = false })
                            DropdownMenuItem(text = { Text("不计入预算") }, onClick = { includeInBudget = 1; budgetMenu = false })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text("备注", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = note, onValueChange = { note = it }, placeholder = { Text("输入备注关键词", fontSize = 13.sp) }, singleLine = true, modifier = Modifier.width(170.dp))
                    }
                    Box {
                        FilterRow("地理位置", locationLabel, onClick = { locationMenu = true })
                        DropdownMenu(expanded = locationMenu, onDismissRequest = { locationMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { withLocation = false; locationMenu = false })
                            DropdownMenuItem(text = { Text("有位置") }, onClick = { withLocation = true; locationMenu = false })
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp)) {
                    Box {
                        FilterRow("明细来源", sourceLabel, onClick = { sourceMenu = true })
                        DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { source = null; sourceMenu = false })
                            DropdownMenuItem(text = { Text("手动") }, onClick = { source = 0; sourceMenu = false })
                            DropdownMenuItem(text = { Text("语音") }, onClick = { source = 1; sourceMenu = false })
                            DropdownMenuItem(text = { Text("自动") }, onClick = { source = 2; sourceMenu = false })
                        }
                    }
                    Box {
                        FilterRow("收付状态", payLabel, onClick = { payMenu = true })
                        DropdownMenu(expanded = payMenu, onDismissRequest = { payMenu = false }) {
                            DropdownMenuItem(text = { Text("全部") }, onClick = { paymentStatus = null; payMenu = false })
                            DropdownMenuItem(text = { Text("已支付") }, onClick = { paymentStatus = 0; payMenu = false })
                            DropdownMenuItem(text = { Text("未支付") }, onClick = { paymentStatus = 1; payMenu = false })
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                if (hasPreset) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        AssistChip(onClick = { viewModel.applyAdvPreset() }, label = { Text("使用常用筛选") })
                        AssistChip(onClick = { viewModel.clearAdvPreset() }, label = { Text("清除常用") })
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                OutlinedButton(onClick = { viewModel.resetAdvFilter(); onDismiss() }, modifier = Modifier.weight(1f).height(44.dp)) { Text("重置") }
                Button(onClick = { viewModel.setAdvFilter(buildFilter()); onDismiss() }, modifier = Modifier.weight(1f).height(44.dp)) { Text("确定") }
            }
        }
    }
}
private fun currencyLabel(c: String?) = c ?: "全部"
@Composable
private fun SettingsDialog(config: HomeConfig, viewModel: CalendarViewModel, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("日历设置") }, text = {
        Column {
            Text("每周起始", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = config.calendarWeekStart == 1, onClick = { viewModel.setWeekStart(1) }, label = { Text("周一") })
                FilterChip(selected = config.calendarWeekStart == 6, onClick = { viewModel.setWeekStart(6) }, label = { Text("周六") })
                FilterChip(selected = config.calendarWeekStart == 0, onClick = { viewModel.setWeekStart(0) }, label = { Text("周日") })
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("显示农历", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = config.calendarShowLunar, onCheckedChange = { viewModel.setShowLunar(it) })
            }
            Spacer(Modifier.height(12.dp))
            Text("盈利颜色", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            ColorPaletteRow(config.calendarProfitColor) { viewModel.setProfitColor(it) }
            Spacer(Modifier.height(12.dp))
            Text("亏损颜色", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            ColorPaletteRow(config.calendarLossColor) { viewModel.setLossColor(it) }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } })
}
@Composable
private fun ColorPaletteRow(current: Long, onSelect: (Long) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PALETTE.forEach { c ->
            val picked = current == c
            Box(modifier = Modifier.size(28.dp).background(Color(c), CircleShape).border(width = if (picked) 2.5.dp else 0.dp, color = MaterialTheme.colorScheme.onSurface, shape = CircleShape).clickable { onSelect(c) })
        }
    }
}
