package com.example.jizhangruanjian.ui.account
import com.example.jizhangruanjian.ui.theme.SemanticColors
import com.example.jizhangruanjian.ui.theme.AppSpacing
import com.example.jizhangruanjian.ui.theme.ChartAuxColors
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.CornerRadius
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.data.model.AccountType
import com.example.jizhangruanjian.domain.model.AccountDomain
@Composable
fun AssetReportScreen(viewModel: AssetReportViewModel = hiltViewModel(), onBack: () -> Unit) {
    val accounts by viewModel.accounts.collectAsState()
    val monthly by viewModel.monthly.collectAsState()
    val yearly by viewModel.yearly.collectAsState()
    val config by viewModel.store.config.collectAsState()
    var assetDim by remember { mutableIntStateOf(0) }
    var debtDim by remember { mutableIntStateOf(0) }
    var trendDim by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    val netAccounts = accounts.filter { it.includeInNet }
    val assets = netAccounts.filter { it.balance > 0 }
    val debts = netAccounts.filter { it.balance < 0 }
    val totalAsset = assets.sumOf { it.balance }
    val totalDebt = debts.sumOf { it.balance }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
            Text("资产报表", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { showSettings = true }) { Icon(Icons.Filled.Settings, contentDescription = "设置") }
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppSpacing.md)) {
            val maxSlices = config.reportMaxSlices ?: 10
            val entries = if (assetDim == 0) netAccounts.map { it.name to it.balance } else netAccounts.groupBy { it.type }.map { (t, list) -> typeLabel(t) + "·" + (if (list.size == 1) list[0].name else list.take(2).joinToString("、") { it.name } + if (list.size > 2) "等" else "") to list.sumOf { it.balance } }
            if (config.reportShowAssets != false) {
                val assetParts = composeParts(entries, maxSlices, positiveDominant = true)
                ReportCard("资产构成") {
                    DonutChart(parts = assetParts, centerLabel = "总资产", centerValue = Formatters.yuanText(totalAsset))
                    Spacer(Modifier.height(AppSpacing.md))
                    CenterChip("账户", "类型", assetDim, modifier = Modifier.align(Alignment.CenterHorizontally)) { assetDim = it }
                }
                Spacer(Modifier.height(AppSpacing.md))
            }
            if (config.reportShowDebts != false) {
                val debtParts = composeParts(entries, maxSlices, positiveDominant = false)
                ReportCard("负债构成") {
                    DonutChart(parts = debtParts, centerLabel = "总负债", centerValue = Formatters.yuanText(totalDebt))
                    Spacer(Modifier.height(AppSpacing.md))
                    CenterChip("账户", "类型", debtDim, modifier = Modifier.align(Alignment.CenterHorizontally)) { debtDim = it }
                }
                Spacer(Modifier.height(AppSpacing.md))
            }
            ReportCard("净资产趋势") {
                val trend = if (trendDim == 0) monthly else yearly
                TrendChart(trend)
                Spacer(Modifier.height(AppSpacing.sm))
                Text("净资产 ${Formatters.yuanText(trend.lastOrNull()?.end ?: 0L)}", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                CenterChip("月统计", "年统计", trendDim, modifier = Modifier.align(Alignment.CenterHorizontally)) { trendDim = it }
                Spacer(Modifier.height(14.dp))
                var showAllRows by remember { mutableStateOf(false) }
                val rows = trend.filter { it.change != 0L }.sortedByDescending { it.label }
                if (rows.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("截止日期", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("本期变化", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("净资产", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(6.dp))
                    rows.take(if (showAllRows) rows.size else 6).forEach { p ->
                        Row(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)).padding(vertical = 10.dp)) {
                            Text(p.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(Formatters.yuanText(p.change), style = MaterialTheme.typography.bodyMedium, color = if (p.change >= 0) SemanticColors.IncomeGreen else MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(Formatters.yuanText(p.end), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    if (!showAllRows && rows.size > 6) {
                        TextButton(onClick = { showAllRows = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("点击加载更多", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
            Spacer(Modifier.height(AppSpacing.xxl))
        }
    }
    if (showSettings) ReportSettingsDialog(viewModel = viewModel, onDismiss = { showSettings = false })
}
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ReportSettingsDialog(viewModel: AssetReportViewModel, onDismiss: () -> Unit) {
    val config by viewModel.store.config.collectAsState()
    var sliceMenu by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xl).padding(bottom = 28.dp)) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                Text("显示资产构成", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = config.reportShowAssets != false, onCheckedChange = { viewModel.setShowAssets(it) })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                Text("显示负债构成", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = config.reportShowDebts != false, onCheckedChange = { viewModel.setShowDebts(it) })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                Text("饼图扇区的最大数量", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Box {
                    TextButton(onClick = { sliceMenu = true }) {
                        Text("${config.reportMaxSlices ?: 10}")
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = sliceMenu, onDismissRequest = { sliceMenu = false }) {
                        (4..10).forEach { n -> DropdownMenuItem(text = { Text("$n") }, onClick = { viewModel.setMaxSlices(n); sliceMenu = false }) }
                    }
                }
            }
        }
    }
}
@Composable
private fun ReportCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surface).padding(AppSpacing.lg)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        content()
    }
}
private val DONUT_COLORS = listOf(0xFF4E79A7, 0xFFF28E2B, 0xFFE15759, 0xFF76B7B2, 0xFF59A14F, 0xFFEDC948, 0xFFB07AA1, 0xFFFF9DA7, 0xFF9C755F, 0xFFBAB0AC).map { Color(it) }
private val OTHER_COLOR = ChartAuxColors.pieOther
private data class DonutPart(val label: String, val amount: Long, val color: Color, val sweep: Float)
private const val SMALL_SLICE_DEG = 4f
private fun composeParts(entries: List<Pair<String, Long>>, maxSlices: Int, positiveDominant: Boolean): List<DonutPart> {
    if (entries.isEmpty()) return emptyList()
    val w = { b: Long -> (if (positiveDominant) b else -b).coerceAtLeast(0L) }
    val sorted = entries.sortedWith(compareByDescending<Pair<String, Long>> { w(it.second) > 0 }.thenByDescending { w(it.second) })
    val totalW = sorted.sumOf { w(it.second) }
    if (totalW <= 0L) {
        val n = sorted.size
        return sorted.take(maxSlices).mapIndexed { i, (label, amt) -> DonutPart(label, amt, DONUT_COLORS[i % DONUT_COLORS.size], 360f / n) }
    }
    val big = sorted.filter { w(it.second) > 0 }
    val small = sorted.filter { w(it.second) <= 0 }
    val smallDeg = min(SMALL_SLICE_DEG * small.size, 120f)
    val bigDeg = 360f - smallDeg
    val shownBig = big.take(maxSlices)
    val restBig = big.drop(maxSlices)
    val bigSumW = sorted.filter { w(it.second) > 0 }.sumOf { w(it.second) }.toFloat()
    val parts = mutableListOf<DonutPart>()
    var colorIdx = 0
    shownBig.forEach { (label, amt) -> parts += DonutPart(label, amt, DONUT_COLORS[colorIdx++ % DONUT_COLORS.size], bigDeg * (w(amt) / bigSumW)) }
    if (restBig.isNotEmpty()) parts += DonutPart("其他", restBig.sumOf { w(it.second) }, OTHER_COLOR, bigDeg * (restBig.sumOf { w(it.second) } / bigSumW))
    small.forEach { (label, amt) -> parts += DonutPart(label, amt, DONUT_COLORS[colorIdx++ % DONUT_COLORS.size], SMALL_SLICE_DEG) }
    return parts
}
@Composable
private fun DonutChart(parts: List<DonutPart>, centerLabel: String, centerValue: String) {
    var rotation by remember { mutableFloatStateOf(0f) }
    var showPercent by remember { mutableStateOf(true) }
    val textMeasurer = rememberTextMeasurer()
    val pointerDeg = (((-135f - rotation) % 360f) + 360f) % 360f
    var accS = 0f
    var selectedIdx = -1
    parts.forEachIndexed { i, p ->
        val s = p.sweep.coerceAtLeast(0.5f)
        if (pointerDeg >= accS && pointerDeg < accS + s) selectedIdx = i
        accS += s
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(240.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(240.dp).pointerInput(parts) {
            var lastAngle = 0f
            detectDragGestures(onDragStart = { offset ->
                lastAngle = Math.toDegrees(atan2(offset.y - size.height / 2.0, offset.x - size.width / 2.0)).toFloat()
            }) { change, _ ->
                change.consume()
                val a = Math.toDegrees(atan2(change.position.y - size.height / 2.0, change.position.x - size.width / 2.0)).toFloat()
                var d = a - lastAngle
                if (d > 180f) d -= 360f
                if (d < -180f) d += 360f
                rotation += d
                lastAngle = a
            }
        }) {
            val cx = center.x
            val cy = center.y
            val ringR = size.minDimension / 2f * 0.52f
            val stroke = ringR * 0.45f
            val arcTopLeft = Offset(cx - ringR - stroke / 2f, cy - ringR - stroke / 2f)
            val arcSize = Size((ringR + stroke / 2f) * 2f, (ringR + stroke / 2f) * 2f)
            if (parts.isEmpty()) {
                drawArc(color = ChartAuxColors.ringTrack, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = arcTopLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Butt))
            } else {
                var start = 45f + rotation
                parts.forEachIndexed { i, p ->
                    drawArc(color = p.color, startAngle = start, sweepAngle = p.sweep.coerceAtLeast(0.5f), useCenter = false, topLeft = arcTopLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Butt))
                    start += p.sweep
                }
                var acc = 45f + rotation
                parts.forEach { p ->
                    val mid = acc + p.sweep / 2f
                    val rad = Math.toRadians(mid.toDouble())
                    val cosv = cos(rad).toFloat()
                    val sinv = sin(rad).toFloat()
                    val sx = cx + cosv * (ringR + stroke / 2f)
                    val sy = cy + sinv * (ringR + stroke / 2f)
                    val ex = cx + cosv * (ringR + stroke / 2f + 22.dp.toPx())
                    val ey = (cy + sinv * (ringR + stroke / 2f + 22.dp.toPx())).coerceIn(14f, size.height - 14f)
                    val text = p.label + " " + if (showPercent) "%.1f%%".format(p.sweep * 100.0 / 360.0) else Formatters.yuanText(p.amount)
                    val measured = textMeasurer.measure(text, TextStyle(fontSize = 11.sp, color = ChartAuxColors.textAxis))
                    drawLine(p.color, Offset(sx, sy), Offset(ex, ey), strokeWidth = 1.dp.toPx())
                    val tail = 8.dp.toPx()
                    if (cosv >= 0) {
                        drawLine(p.color, Offset(ex, ey), Offset(ex + tail, ey), strokeWidth = 1.dp.toPx())
                        drawText(measured, topLeft = Offset(ex + tail + 3.dp.toPx(), ey - measured.size.height / 2f))
                    } else {
                        drawLine(p.color, Offset(ex, ey), Offset(ex - tail, ey), strokeWidth = 1.dp.toPx())
                        drawText(measured, topLeft = Offset(ex - tail - 3.dp.toPx() - measured.size.width, ey - measured.size.height / 2f))
                    }
                    acc += p.sweep
                }
            }
        }
        TextButton(onClick = { showPercent = !showPercent }, contentPadding = PaddingValues(horizontal = 10.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (selectedIdx >= 0) Text(parts[selectedIdx].label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (showPercent) {
                    Text("占比", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Filled.SwapHoriz, contentDescription = "切换", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(centerValue, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(centerLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
@Composable
private fun CenterChip(left: String, right: String, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(selected = selected == 0, onClick = { onSelect(0) }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text(left) }
        SegmentedButton(selected = selected == 1, onClick = { onSelect(1) }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text(right) }
    }
}
@Composable
private fun TrendChart(points: List<AssetReportViewModel.TrendPoint>) {
    if (points.size < 2) {
        Text("数据不足", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xxl), textAlign = TextAlign.Center)
        return
    }
    var sel by remember { mutableIntStateOf(-1) }
    val textMeasurer = rememberTextMeasurer()
    val values = points.map { it.end.toFloat() }
    val minV = (values.min() - 1f).coerceAtMost(0f)
    val maxV = values.max() + 1f
    val range = (maxV - minV).coerceAtLeast(1f)
    Canvas(modifier = Modifier.fillMaxWidth().height(170.dp).pointerInput(points) {
        detectTapGestures { off -> sel = ((off.x / size.width * (points.size - 1)).roundToInt()).coerceIn(0, points.size - 1) }
    }.pointerInput(points) {
        detectDragGestures(onDragStart = { off -> sel = ((off.x / size.width * (points.size - 1)).roundToInt()).coerceIn(0, points.size - 1) }) { c, _ ->
            c.consume()
            sel = ((c.position.x / size.width * (points.size - 1)).roundToInt()).coerceIn(0, points.size - 1)
        }
    }) {
        val padL = 4.dp.toPx()
        fun yF(v: Float) = size.height * (1f - (v - minV) / range)
        for (i in 0..4) {
            val gy = size.height * i / 4f
            drawLine(ChartAuxColors.grid, Offset(padL, gy), Offset(size.width, gy), strokeWidth = 1.dp.toPx(), pathEffect = null)
            val gv = maxV - range * i / 4f
            val gTxt = textMeasurer.measure("%.0f".format(gv), TextStyle(fontSize = 9.sp, color = ChartAuxColors.textTick))
            drawText(gTxt, topLeft = Offset(0f, gy - gTxt.size.height / 2f))
        }
        val pts = values.mapIndexed { i, v -> Offset(padL + (size.width - padL) * i / (values.size - 1f), yF(v)) }
        val baseline = yF(0f).coerceIn(0f, size.height)
        val area = Path().apply {
            moveTo(pts[0].x, baseline)
            pts.forEach { lineTo(it.x, it.y) }
            lineTo(pts.last().x, baseline)
            close()
        }
        drawPath(area, brush = Brush.verticalGradient(listOf(Color(0xFF64B5F6).copy(alpha = 0.85f), Color(0xFF64B5F6).copy(alpha = 0.12f)), startY = 0f, endY = size.height))
        for (i in 0 until pts.lastIndex) drawLine(SemanticColors.ChartBlue, pts[i], pts[i + 1], strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        if (sel in points.indices) {
            val sp = pts[sel]
            drawLine(ChartAuxColors.verticalGrid, Offset(sp.x, 0f), Offset(sp.x, size.height), strokeWidth = 1.dp.toPx())
            drawCircle(SemanticColors.ChartBlue, radius = 5.dp.toPx(), center = sp)
            drawCircle(Color.White, radius = 2.5.dp.toPx(), center = sp)
            val p = points[sel]
            val txt = "${p.label}  净资产 ${Formatters.yuanText(p.end.toLong())}  变化 ${Formatters.yuanText(p.change)}"
            val m = textMeasurer.measure(txt, TextStyle(fontSize = 11.sp, color = ChartAuxColors.textMonth))
            val bw = m.size.width + 24f
            val bh = m.size.height + 16f
            val bx = (sp.x - bw / 2f).coerceIn(0f, size.width - bw)
            drawRoundRect(Color.White.copy(alpha = 0.95f), topLeft = Offset(bx, 4f), size = Size(bw, bh), cornerRadius = CornerRadius(10f, 10f))
            drawText(m, topLeft = Offset(bx + 12f, 4f + (bh - m.size.height) / 2f))
        }
    }
    val isMonthly = points.firstOrNull()?.label?.endsWith("月") == true
    val step = if (isMonthly) 2 else ((points.size - 1) / 6).coerceAtLeast(1)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        points.mapIndexed { i, p -> i to p.label }.filter { (i, _) -> i % step == 0 }.forEach { (_, lb) ->
            Text(lb, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
