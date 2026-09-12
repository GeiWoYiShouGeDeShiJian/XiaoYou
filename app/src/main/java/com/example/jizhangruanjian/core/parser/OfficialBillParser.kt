package com.example.jizhangruanjian.core.parser
import com.example.jizhangruanjian.data.model.TransactionType
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
// P1 微信/支付宝官方账单解析：先稳健识别编码，再按表头列名动态定位字段，兼容列顺序/表头行号差异；收/支非"收入/支出"的中性账（提现/充值/退款/转账等）跳过
data class OfficialParseResult(val rows: List<CsvRow>, val source: String?) // source: "wechat"/"alipay"/null(通用)
object OfficialBillParser {
    private val DATE_PATTERNS = arrayOf("yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm", "yyyy-MM-dd", "yyyy/MM/dd")
    // 编码识别：UTF-8 严格解码失败（如 GBK 编码的支付宝账单）则回退 GBK；兼容 BOM 与 UTF-16
    fun decodeBytes(bytes: ByteArray): String {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) return String(bytes, Charset.forName("UTF-8")).removePrefix("\uFEFF")
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) return String(bytes, Charset.forName("UTF-16LE"))
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) return String(bytes, Charset.forName("UTF-16BE"))
        return try {
            Charset.forName("UTF-8").newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
        } catch (e: Exception) {
            String(bytes, Charset.forName("GBK"))
        }
    }
    fun parseResult(text: String): OfficialParseResult {
        if (text.isBlank()) return OfficialParseResult(emptyList(), null)
        val records = try { CSVFormat.DEFAULT.parse(StringReader(text)).records } catch (e: Exception) { return OfficialParseResult(emptyList(), null) }
        val w = records.indexOfFirst { r -> hasCol(r, "交易时间") && hasCol(r, "交易对方") }
        if (w >= 0) return OfficialParseResult(parseRows(records[w], records, w, "交易时间"), "wechat")
        val a = records.indexOfFirst { r -> r.size() >= 12 && hasCol(r, "交易号") && hasCol(r, "收/支") }
        if (a >= 0) return OfficialParseResult(parseRows(records[a], records, a, "交易创建时间"), "alipay")
        return OfficialParseResult(CsvParser.parse(text), null)
    }
    private fun hasCol(r: CSVRecord, name: String): Boolean = (0 until r.size()).any { r.get(it).trim() == name }
    private fun parseRows(header: CSVRecord, records: List<CSVRecord>, headerIdx: Int, dateCol: String): List<CsvRow> {
        val idx = LinkedHashMap<String, Int>()
        for (i in 0 until header.size()) { val n = header.get(i).trim(); if (n.isNotEmpty() && !idx.containsKey(n)) idx[n] = i }
        fun cell(rec: CSVRecord, name: String): String { val c = idx[name]; return if (c != null && c < rec.size()) rec.get(c).trim() else "" }
        // 金额列名有半角/全角差异（微信"金额(元)"、支付宝"金额（元）"），统一按含"金额"定位
        fun amount(rec: CSVRecord): Long? {
            for ((name, c) in idx) { if (name.contains("金额") && c < rec.size()) { val v = rec.get(c).trim().replace("¥", "").replace("￥", "").replace(",", "").replace("，", "").replace(" ", "").replace("-", ""); return v.toDoubleOrNull()?.let { (it * 100).toLong() } } }
            return null
        }
        val commodityCol = if (idx.containsKey("商品")) "商品" else "商品名称"
        val result = mutableListOf<CsvRow>()
        records.drop(headerIdx + 1).forEach { rec ->
            val status = cell(rec, "交易状态")
            if (status.contains("失败") || status.contains("关闭")) return@forEach
            val type = when (cell(rec, "收/支")) { "支出" -> TransactionType.EXPENSE; "收入" -> TransactionType.INCOME; else -> return@forEach }
            val cents = amount(rec) ?: return@forEach
            val commodity = cell(rec, commodityCol).takeIf { it.isNotBlank() && it != "/" } ?: ""
            val remark = cell(rec, "备注").takeIf { it.isNotBlank() && it != "/" } ?: ""
            result.add(CsvRow(date = parseOfficialDate(cell(rec, dateCol)), type = type, category = null, account = null, amountCents = cents, note = listOf(commodity, remark).filter { it.isNotBlank() }.joinToString(" "), tags = emptyList(), merchant = cell(rec, "交易对方").takeIf { it.isNotBlank() && it != "/" }))
        }
        return result
    }
    private fun parseOfficialDate(text: String): Long? {
        val t = text.trim()
        DATE_PATTERNS.forEach { p -> try { return java.text.SimpleDateFormat(p, java.util.Locale.CHINA).parse(t)?.time } catch (e: Exception) { } }
        return null
    }
}