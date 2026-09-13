package com.example.jizhangruanjian.ui.statistics
import com.example.jizhangruanjian.ui.theme.SemanticColors
import com.example.jizhangruanjian.ui.theme.AppSpacing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.CategoryTotal
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.domain.model.TransactionDisplay
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.components.DonutChart
import java.time.YearMonth
import java.util.Locale

// ══════════════════════════════════════════════════════════════
//  统计页 —— 月度收支报表（8 模块）
//  信息层级：总 → 分 → 细（概览 → 趋势 → 构成 → 排行 → 明细）
// ══════════════════════════════════════════════════════════════

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onMenuClick: () -> Unit = {},
    onOpenLedgerPicker: () -> Unit = {},
    ledgerName: String = ""
) {
    val summary by viewModel.summary.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val calendarMonth by viewModel.calendarMonth.collectAsState()
    val monthlyExpense by viewModel.monthlyExpense.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val incomeCategoryTotals by viewModel.incomeCategoryTotals.collectAsState()
    val dailyExpenses by viewModel.dailyExpenses.collectAsState()
    val expenseType by viewModel.expenseType.collectAsState()
    val recentTxs by viewModel.recentTransactions.collectAsState()

    // ── 本地 UI 状态 ──
    var selectedPeriod by remember { mutableIntStateOf(1) } // 默认"月报"
    var selectedDay by remember { mutableStateOf<java.time.LocalDate?>(null) }

    val brand = MaterialTheme.colorScheme.primary
    val catColors = com.example.jizhangruanjian.ui.theme.ReportCategoryPalette
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ═══════════════════ ① 周期切换 + 标题栏 ═══════════════════
        ReportTopBar(
            ledgerName = ledgerName,
            onMenuClick = onMenuClick,
            onOpenLedgerPicker = onOpenLedgerPicker
        )
        Spacer(Modifier.height(AppSpacing.sm))
        PeriodCapsule(
            selectedPeriod = selectedPeriod,
            onPeriodChange = {
                selectedPeriod = it
                when (it) {
                    0 -> viewModel.setPeriod(com.example.jizhangruanjian.data.model.PeriodType.WEEK)
                    1 -> viewModel.setPeriod(com.example.jizhangruanjian.data.model.PeriodType.MONTH)
                    2 -> viewModel.setPeriod(com.example.jizhangruanjian.data.model.PeriodType.YEAR)
                }
            }
        )

        // ═══════════════════ 可滚动内容区 ═══════════════════
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg)
        ) {
            summary?.let { s ->
                // 当前类型的金额与分类
                val currentAmount = if (expenseType == TransactionType.EXPENSE) s.expense else s.income
                val categoryTotals = if (expenseType == TransactionType.EXPENSE)
                    s.categoryTotals else incomeCategoryTotals
                val rank = buildRank(categoryTotals, categories)

                // ═══════════════════ ② 月份切换 + 支出/收入切换 ═══════════════════
                MonthSwitchBar(
                    month = calendarMonth,
                    expenseType = expenseType,
                    onPrev = viewModel::prevMonth,
                    onNext = viewModel::nextMonth,
                    onTypeToggle = { viewModel.toggleType(it) }
                )

                Spacer(Modifier.height(AppSpacing.lg))

                // ═══════════════════ ③ KPI 区（2×2 数据概览卡）═══════════════════
                KpiGrid(
                    summary = s,
                    calendarMonth = calendarMonth,
                    dailyAverage = viewModel.dailyAverage(currentAmount),
                    expenseType = expenseType
                )

                Spacer(Modifier.height(AppSpacing.xl))

                // ═══════════════════ ④ 趋势折线图（面积样式）══════════════════
                val trendValues = if (expenseType == TransactionType.EXPENSE) monthlyExpense else monthlyIncome
                ReportCard {
                    Column(modifier = Modifier.padding(AppSpacing.xl)) {
                        Text(
                            text = "${calendarMonth.year}年${calendarMonth.monthValue}月${if (expenseType == TransactionType.EXPENSE) "支出" else "收入"}趋势",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurface
                        )
                        Spacer(Modifier.height(AppSpacing.lg))
                        TrendAreaChart(
                            values = trendValues,
                            labels = monthShortLabels(calendarMonth),
                            brandColor = brand
                        )
                    }
                }

                Spacer(Modifier.height(AppSpacing.xl))

                // ═══════════════════ ⑤ 支出分类构成环形图（中心总额）══════════════════
                ReportCard {
                    Column(modifier = Modifier.padding(AppSpacing.xl), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${calendarMonth.year}年${calendarMonth.monthValue}月${if (expenseType == TransactionType.EXPENSE) "支出" else "收入"}分类构成",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(AppSpacing.xl))
                        DonutWithCenterTotal(
                            rank = rank,
                            colors = catColors,
                            total = currentAmount,
                            label = if (expenseType == TransactionType.EXPENSE) "共支出(元)" else "共收入(元)",
                            emptyText = if (expenseType == TransactionType.EXPENSE) "暂无支出数据" else "暂无收入数据"
                        )
                    }
                }

                Spacer(Modifier.height(AppSpacing.xl))

                // ═══════════════════ ⑥ 分类排行列表 ═══════════════════
                if (rank.isNotEmpty() && currentAmount > 0L) {
                    ReportCard {
                        Column(modifier = Modifier.padding(AppSpacing.xl)) {
                            Text(
                                text = "${calendarMonth.year}年${calendarMonth.monthValue}月${if (expenseType == TransactionType.EXPENSE) "支出" else "收入"}排行",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = onSurface
                            )
                            Spacer(Modifier.height(AppSpacing.lg))
                            CategoryRankList(rank = rank, totals = currentAmount, colors = catColors)
                        }
                    }
                    Spacer(Modifier.height(AppSpacing.xl))
                }

                // ═══════════════════ ⑦ 月度对比柱状图（当前月高亮）══════════════════
                ReportCard {
                    Column(modifier = Modifier.padding(AppSpacing.xl)) {
                        Text(
                            text = "月${if (expenseType == TransactionType.EXPENSE) "支出" else "收入"}对比",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurface
                        )
                        Spacer(Modifier.height(AppSpacing.lg))
                        MonthlyComparisonChart(
                            values = if (expenseType == TransactionType.EXPENSE) monthlyExpense else monthlyIncome,
                            labels = monthComparisonLabels(calendarMonth),
                            currentIndex = 5, // 最近一个月在列表末尾（索引5）
                            brandColor = brand
                        )
                    }
                }

                Spacer(Modifier.height(AppSpacing.xl))

                // ═══════════════════ ⑧ 单笔支出/收入排行明细 ═══════════════════
                if (recentTxs.isNotEmpty()) {
                    ReportCard {
                        Column(modifier = Modifier.padding(AppSpacing.xl)) {
                            Text(
                                text = "${calendarMonth.year}年${calendarMonth.monthValue}月${if (expenseType == TransactionType.EXPENSE) "支出" else "收入"}排行",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = onSurface
                            )
                            Spacer(Modifier.height(AppSpacing.md))
                            TransactionRankList(transactions = recentTxs, type = expenseType)
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.xxxl))
            } ?: run {
                // 加载中
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = brand)
                }
            }
        }
    }

    // 日历日期点击弹窗（保留原有交互）
    selectedDay?.let { day ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedDay = null },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { selectedDay = null }) {
                    Text("关闭")
                }
            },
            title = { Text("${day.monthValue}月${day.dayOfMonth}日支出") },
            text = { Text(Formatters.yuanText(dailyExpenses[day.dayOfMonth] ?: 0L)) }
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  模块 ①：周期切换顶栏（周报/月报/年报/自定义）
// ══════════════════════════════════════════════════════════════
@Composable
private fun ReportTopBar(
    ledgerName: String,
    onMenuClick: () -> Unit,
    onOpenLedgerPicker: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Filled.Menu, contentDescription = "菜单", tint = MaterialTheme.colorScheme.onSurface)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).clickable { onOpenLedgerPicker() }
        ) {
            Text(
                text = ledgerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "切换账本", tint = MaterialTheme.colorScheme.onSurface)
        }
        // 右侧无图标
    }
}

@Composable
private fun PeriodCapsule(
    selectedPeriod: Int,
    onPeriodChange: (Int) -> Unit
) {
    val tabs = listOf("周报", "月报", "年报", "自定义")
    val brand = MaterialTheme.colorScheme.primary
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    Surface(
        shape = RoundedCornerShape(50),
        color = surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { i, label ->
                val selected = i == selectedPeriod
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (selected) brand else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .clickable { onPeriodChange(i) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  模块 ②：月份切换 + 支出/收入切换
// ══════════════════════════════════════════════════════════════
@Composable
private fun MonthSwitchBar(
    month: YearMonth,
    expenseType: TransactionType,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onTypeToggle: (TransactionType) -> Unit
) {
    val brand = MaterialTheme.colorScheme.primary
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)
    ) {
        // 左侧：月份切换
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onPrev, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "上个月", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = String.format(Locale.CHINA, "%d年%d月", month.year, month.monthValue),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "下个月", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // 右侧：支出/收入紧凑胶囊
        Surface(shape = RoundedCornerShape(12.dp), color = surfaceContainerHigh) {
            Row(
                modifier = Modifier.padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isExpense = expenseType == TransactionType.EXPENSE
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isExpense) brand else Color.Transparent,
                    modifier = Modifier.clickable { onTypeToggle(TransactionType.EXPENSE) }
                ) {
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isExpense) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (!isExpense) brand else Color.Transparent,
                    modifier = Modifier.clickable { onTypeToggle(TransactionType.INCOME) }
                ) {
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (!isExpense) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  模块 ③：KPI 区（2×2 数据概览卡片）
//  本月支出 | 日均支出 | 比上月支出 | 收支结余
// ══════════════════════════════════════════════════════════════
@Composable
private fun KpiGrid(
    summary: com.example.jizhangruanjian.data.model.StatisticsSummary,
    calendarMonth: YearMonth,
    dailyAverage: Float,
    expenseType: TransactionType
) {
    val brand = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerLowest = MaterialTheme.colorScheme.surfaceContainerLowest

    // 根据 expenseType 决定展示金额
    val currentAmount = if (expenseType == TransactionType.EXPENSE) summary.expense else summary.income
    val prevAmount = if (expenseType == TransactionType.EXPENSE) summary.prevExpense else 0L // 上期收入暂无独立字段，用 0 占位
    val balance = summary.income - summary.expense

    // 环比计算
    val changePercent = if (prevAmount != 0L) (currentAmount - prevAmount) * 100f / prevAmount else null
    val changeSign = if ((currentAmount - prevAmount) >= 0) "+" else ""

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 卡片 1：本月支出/收入
        KpiCard(
            modifier = Modifier.weight(1f),
            title = "${calendarMonth.monthValue}月${if (expenseType == TransactionType.EXPENSE) "支出" else "收入"}(元)",
            value = formatSignedAmount(currentAmount, expenseType == TransactionType.EXPENSE),
            valueColor = if (expenseType == TransactionType.EXPENSE && currentAmount > 0) onSurface else brand,
            containerColor = surfaceContainerLowest
        )
        // 卡片 2：日均支出/收入
        KpiCard(
            modifier = Modifier.weight(1f),
            title = "日均${if (expenseType == TransactionType.EXPENSE) "支出" else "收入"}(元)",
            value = formatSignedYuan(dailyAverage, expenseType == TransactionType.EXPENSE),
            valueColor = onSurface,
            containerColor = surfaceContainerLowest
        )
    }

    Spacer(Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 卡片 3：比上月变化
        KpiCard(
            modifier = Modifier.weight(1f),
            title = "比上月${if (expenseType == TransactionType.EXPENSE) "支出" else "收入"}(元)",
            value = if (changePercent != null) "$changeSign${String.format(Locale.CHINA, "%.2f", (currentAmount - prevAmount) / 100.0)}" else "--",
            subText = if (changePercent != null) "$changeSign${String.format("%.1f", changePercent)}%" else null,
            valueColor = if (changePercent != null && changePercent < 0) SemanticColors.IncomeGreen else onSurface,
            containerColor = surfaceContainerLowest
        )
        // 卡片 4：收支结余
        KpiCard(
            modifier = Modifier.weight(1f),
            title = "收支结余(元)",
            value = formatSignedAmount(balance, true), // 结余统一带符号
            valueColor = if (balance >= 0) SemanticColors.IncomeGreen else SemanticColors.ExpenseRed,
            containerColor = surfaceContainerLowest
        )
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subText: String? = null,
    valueColor: Color,
    containerColor: Color
) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            color = containerColor,
            tonalElevation = 0.dp
        ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp
                ),
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1
            )
            subText?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  模块 ④：趋势折线图（面积样式）—— Canvas 手绘，项目惯例
// ══════════════════════════════════════════════════════════════
@Composable
private fun TrendAreaChart(values: List<Float>, labels: List<String>, brandColor: Color) {
    if (values.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text("暂无数据", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val n = values.size
            val rawMax = values.maxOrNull() ?: 0f
            val minV = minOf(values.minOrNull() ?: 0f, 0f)
            val maxV = if (rawMax <= 0f) 1f else rawMax
            val range = (maxV - minV).coerceAtLeast(1f)
            val topPad = 8.dp.toPx()
            val chartH = size.height - topPad - 4.dp.toPx()
            fun y(v: Float): Float = topPad + (maxV - v) / range * chartH
            val step = if (n > 1) size.width / (n - 1) else size.width
            val pts = values.mapIndexed { i, v -> Offset(i * step, y(v)) }
            // 中点三次贝塞尔平滑曲线（C1 连续）
            val line = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val p0 = pts[i - 1]
                    val p1 = pts[i]
                    val midX = (p0.x + p1.x) / 2f
                    cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                }
            }
            // 面积填充（极浅品牌色，无渐变）
            val fillBottom = size.height
            val fill = Path().apply { addPath(line); lineTo(pts.last().x, fillBottom); lineTo(pts.first().x, fillBottom); close() }
            drawPath(fill, brandColor.copy(alpha = 0.08f))
            // 折线
            drawPath(line, brandColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            // 数据点
            pts.forEach { pt ->
                drawCircle(color = brandColor, radius = 3.5.dp.toPx(), center = pt)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)
            }
        }
        // X 轴标签
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  模块 ⑤：环形图 + 中心总金额（复用项目 DonutChart）
// ══════════════════════════════════════════════════════════════
@Composable
private fun DonutWithCenterTotal(
    rank: List<Pair<CategoryTotal, CategoryDomain?>>,
    colors: List<Color>,
    total: Long,
    label: String,
    emptyText: String
) {
    val empty = rank.isEmpty() || total <= 0L
    val brand = MaterialTheme.colorScheme.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 左侧：环形图
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            if (empty) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(color = brand.copy(alpha = 0.15f), style = Stroke(width = 18.dp.toPx()))
                }
            } else {
                DonutChart(
                    amounts = rank.map { it.first.total.toFloat() },
                    modifier = Modifier.fillMaxSize(),
                    colors = colors,
                    strokeDp = 22f
                )
            }
            // 中心文字：总金额
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (empty) {
                    Text(emptyText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        Formatters.yuanText(total),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 17.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 右侧：分类图例列表（名称 + 百分比）
        if (!empty) {
        Column(
            modifier = Modifier.weight(1f).padding(start = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
                rank.take(6).forEachIndexed { i, (c, cat) ->
                    val pct = if (total > 0L) c.total * 100f / total else 0f
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.size(AppSpacing.sm).background(colors[i % colors.size], CircleShape)
                        )
                        Spacer(Modifier.width(AppSpacing.sm))
                        Text(
                            text = cat?.name ?: "未分类",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = String.format(Locale.CHINA, "%.1f%%", pct),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  模块 ⑥：分类排行列表（序号、图标、笔数、金额、占比条）
// ══════════════════════════════════════════════════════════════
@Composable
private fun CategoryRankList(
    rank: List<Pair<CategoryTotal, CategoryDomain?>>,
    totals: Long,
    colors: List<Color>
) {
    val brand = MaterialTheme.colorScheme.primary

    rank.forEachIndexed { index, (c, cat) ->
        val pct = if (totals > 0L) c.total.toFloat() / totals else 0f
        val displayColor = colors[index % colors.size]

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: 跳转到分类详情 */ }
                .padding(vertical = AppSpacing.md)
        ) {
            // 序号
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (index < 3) brand else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(AppSpacing.xxl),
                textAlign = TextAlign.Center
            )

            // 分类图标
            CategoryIcon(
                icon = cat?.icon ?: "?",
                size = 36.dp,
                container = displayColor.copy(alpha = 0.12f)
            )

            Spacer(Modifier.width(10.dp))

            // 名称 + 笔数
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cat?.name ?: "未分类",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                // 占比进度条
                Spacer(Modifier.height(AppSpacing.xs))
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth().height(AppSpacing.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(pct.coerceIn(0f, 1f))
                            .height(AppSpacing.xs)
                            .background(displayColor.copy(alpha = 0.7f))
                    )
                }
            }

            // 金额
            Text(
                text = "-${Formatters.yuanText(c.total)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.width(AppSpacing.xs))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        }

        if (index < rank.lastIndex) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 58.dp) // 缩进，对齐内容区域
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  模块 ⑦：月度对比柱状图（当前月品牌色高亮，其余浅灰）
//  Canvas 自绘柱状图 —— 精确控制高亮色与圆角
// ══════════════════════════════════════════════════════════════
@Composable
private fun MonthlyComparisonChart(
    values: List<Float>,
    labels: List<String>,
    currentIndex: Int,
    brandColor: Color
) {
    if (values.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
            Text("暂无数据", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val inactiveColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            val n = values.size
            if (n == 0) return@Canvas
            val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
            // 柱子参数
            val barW = size.width / (n * 2f + 1f) // 柱宽 ≈ 总宽/(2N+1)
            val gap = barW * 0.5f
            val baseY = size.height - 16.dp.toPx()
            val maxH = baseY - 8.dp.toPx()

            values.forEachIndexed { i, v ->
                val x = gap + i * (barW + gap)
                val h = (v / maxVal * maxH).coerceAtLeast(2.dp.toPx())
                val topY = baseY - h
                val color = if (i == currentIndex) brandColor else inactiveColor
                // 圆角矩形柱子
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, topY),
                    size = androidx.compose.ui.geometry.Size(barW, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                // 当前月在柱顶显示金额
                if (i == currentIndex && v > 0) {
                    val amtText = String.format(Locale.CHINA, "%.0f", v / 100.0) // 分→元（近似）
                    // 简化：不在此处绘制文字（避免字体测量复杂度），金额由外部 KPI 卡展示即可
                }
            }
        }

        // X 轴月份标签
        Spacer(Modifier.height(AppSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { i, label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == currentIndex) brandColor else labelColor,
                    fontWeight = if (i == currentIndex) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  模块 ⑧：单笔支出/收入排行明细
// ══════════════════════════════════════════════════════════════
@Composable
private fun TransactionRankList(transactions: List<TransactionDisplay>, type: TransactionType) {
    val brand = MaterialTheme.colorScheme.primary

    transactions.take(10).forEachIndexed { index, tx ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: 跳转编辑 */ }
                .padding(vertical = 10.dp)
        ) {
            // 分类图标
            CategoryIcon(icon = tx.categoryIcon, size = 36.dp)

            Spacer(Modifier.width(10.dp))

            // 分类名 + 时间
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = Formatters.mdHM(tx.tradeDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                tx.note.takeIf { it.isNotBlank() }?.let { note ->
                    val display = if (note.length > 12) note.take(12) + "..." else note
                    Text(
                        text = display,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }

            // 金额（支出带负号）
            val prefix = if (type == TransactionType.EXPENSE) "-" else "+"
            Text(
                text = "$prefix${Formatters.yuanText(tx.amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (index < transactions.take(10).lastIndex) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 50.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  通用：报表卡片容器（极浅表面色 + 细边框，无阴影）
// ══════════════════════════════════════════════════════════════
@Composable
private fun ReportCard(content: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        content()
    }
}

// ══════════════════════════════════════════════════════════════
//  工具函数
// ══════════════════════════════════════════════════════════════

/** 格式化带符号的金额（分→元），支出默认带负号 */
private fun formatSignedAmount(cents: Long, showNegative: Boolean): String {
    val yuan = Formatters.yuanText(cents.coerceAtLeast(0L))
    return if (showNegative && cents > 0L) "-$yuan" else yuan
}

/** 格式化 float 元为带符号字符串 */
private fun formatSignedYuan(value: Float, showNegative: Boolean): String {
    return if (showNegative && value > 0) "-${String.format(Locale.CHINA, "%.2f", value)}"
    else String.format(Locale.CHINA, "%.2f", value.coerceAtLeast(0f))
}

/** 近 6 月短标签（用于趋势折线图 X 轴）：N月 */
private fun monthShortLabels(m: YearMonth): List<String> =
    (5 downTo 0).map { offset ->
        val ym = m.minusMonths(offset.toLong())
        "${ym.monthValue}月"
    }

/** 月度对比柱状图 X 轴标签：N月 */
private fun monthComparisonLabels(m: YearMonth): List<String> =
    (5 downTo 0).map { offset ->
        val ym = m.minusMonths(offset.toLong())
        "${ym.monthValue}月"
    }

/** 构建分类排行（关联分类名称和图标） */
private fun buildRank(totals: List<CategoryTotal>, categories: List<CategoryDomain>): List<Pair<CategoryTotal, CategoryDomain?>> {
    val map = categories.associateBy { it.id }
    return totals.sortedByDescending { it.total }.map { it to map[it.categoryId] }
}
