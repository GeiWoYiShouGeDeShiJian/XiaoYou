package com.example.jizhangruanjian.core.util
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
object Formatters {
    private val ymd = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val md = SimpleDateFormat("M月d日", Locale.CHINA)
    private val hm = SimpleDateFormat("HH:mm", Locale.CHINA)
    private val ymdTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val mdTime = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
    fun centsToYuan(cents: Long): Double = cents / 100.0
    fun yuanText(cents: Long): String = String.format(Locale.CHINA, "%.2f", cents / 100.0)
    fun dateYmd(ts: Long): String = ymd.format(Date(ts))
    fun dateMd(ts: Long): String = md.format(Date(ts))
    fun timeHM(ts: Long): String = hm.format(Date(ts))
    fun mdHM(ts: Long): String = mdTime.format(Date(ts))
    fun dateYmdTime(ts: Long): String = ymdTime.format(Date(ts))
    // 回收站/交易时间：今天、昨天显示相对描述，其余 MM-dd HH:mm
    fun formatTrashTime(ts: Long): String {
        val d = java.time.Instant.ofEpochMilli(ts).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val today = java.time.LocalDate.now()
        return when (d) {
            today -> "今天 " + hm.format(Date(ts))
            today.minusDays(1) -> "昨天 " + hm.format(Date(ts))
            else -> mdTime.format(Date(ts))
        }
    }
    @Throws(java.text.ParseException::class)
    fun parseYmd(text: String): Long = ymd.parse(text)?.time ?: System.currentTimeMillis()
}