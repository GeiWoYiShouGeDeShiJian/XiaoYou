package com.example.jizhangruanjian.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jizhangruanjian.core.util.Formatters
import java.time.LocalDate
import java.time.YearMonth
sealed interface CalendarCell
data class CalendarDay(val date: LocalDate, val amount: Long) : CalendarCell
@Composable
fun ExpenseCalendar(yearMonth: YearMonth, dailyExpenses: Map<Int, Long>, modifier: Modifier = Modifier, onDayClick: (LocalDate) -> Unit = {}) {
    val max = (dailyExpenses.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val leading = yearMonth.atDay(1).dayOfWeek.value - 1
    val dayCount = yearMonth.lengthOfMonth()
    val rows = (leading + dayCount + 6) / 7
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { w ->
                Text(w, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            }
        }
        for (row in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0..6) {
                    val idx = row * 7 + col
                    val cell = if (idx >= leading && idx < leading + dayCount) CalendarDay(yearMonth.atDay(idx - leading + 1), dailyExpenses[idx - leading + 1] ?: 0L) else null
                    if (cell == null) Spacer(Modifier.weight(1f).height(52.dp))
                    else Box(Modifier.weight(1f)) { DayCell(cell, max, onDayClick) }
                }
            }
        }
    }
}
@Composable
private fun DayCell(cell: CalendarDay, max: Long, onDayClick: (LocalDate) -> Unit) {
    val ratio = if (cell.amount > 0) cell.amount.toFloat() / max else 0f
    val alpha = if (ratio > 0) 0.1f + 0.7f * ratio else 0f
    val bg = if (ratio > 0) MaterialTheme.colorScheme.primary.copy(alpha = alpha) else Color.Transparent
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.height(52.dp).fillMaxWidth().background(bg, RoundedCornerShape(8.dp)).clickable { onDayClick(cell.date) }) {
        Text(cell.date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = if (ratio > 0) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
        if (ratio > 0) Text("¥${Formatters.yuanText(cell.amount)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.fillMaxWidth())
    }
}