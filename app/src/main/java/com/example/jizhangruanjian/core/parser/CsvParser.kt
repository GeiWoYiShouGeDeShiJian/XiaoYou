package com.example.jizhangruanjian.core.parser
import com.example.jizhangruanjian.data.model.LoanDirection
import com.example.jizhangruanjian.data.model.TransactionType
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.StringReader
import java.io.StringWriter
// P13 CSV 导出/导入；支持自有格式（日期/类型/分类/账户/金额/备注/标签）与通用模板格式（类型/日期/大类/小类/金额/账户/账户2/备注/图片/颜色/标签/账本/商家）
data class ExportRow(
    val date: String, val type: String, val category: String,
    val account: String, val amountYuan: String, val note: String, val tags: String
)
data class CsvRow(
    val date: Long?, val type: TransactionType?,
    val category: String?, val account: String?, val amountCents: Long?,
    val note: String, val tags: List<String>,
    val subcategory: String? = null, val toAccount: String? = null,
    val loanDirection: LoanDirection? = null, val merchant: String? = null
)
object CsvParser {
    private val HEADERS = arrayOf("日期", "类型", "分类", "账户", "金额", "备注", "标签")
    private val DATE_PATTERNS = arrayOf("yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm", "yyyy-MM-dd", "yyyy/MM/dd")
    fun fieldNames(): List<String> = HEADERS.toList()
    fun export(rows: List<ExportRow>, fields: List<String> = HEADERS.toList()): String {
        val out = StringWriter()
        CSVPrinter(out, CSVFormat.DEFAULT).use { p ->
            p.printRecord(*fields.toTypedArray())
            rows.forEach { r -> p.printRecord(fields.map { f -> when (f) { "日期" -> r.date; "类型" -> r.type; "分类" -> r.category; "账户" -> r.account; "金额" -> r.amountYuan; "备注" -> r.note; "标签" -> r.tags; else -> "" } }) }
        }
        return out.toString()
    }
    fun parse(text: String): List<CsvRow> {
        val records = CSVFormat.DEFAULT.parse(StringReader(text)).records
        val templateIdx = records.indexOfFirst { rec -> rec.size() > 5 && rec.get(0).trim() == "类型" && (0 until rec.size()).any { rec.get(it).trim() == "账户2" } }
        return if (templateIdx >= 0) parseTemplate(records, templateIdx) else parseLegacy(records)
    }
    private fun parseLegacy(records: List<CSVRecord>): List<CsvRow> {
        val result = mutableListOf<CsvRow>()
        records.forEachIndexed { index, rec ->
            if (index == 0) return@forEachIndexed // 跳过表头
            if (rec.size() < 6) return@forEachIndexed
            val amountYuan = rec.get(4)
            result.add(
                CsvRow(
                    date = parseDateCell(rec.get(0)),
                    type = typeOf(rec.get(1)),
                    category = rec.get(2).takeIf { it.isNotBlank() },
                    account = rec.get(3).takeIf { it.isNotBlank() },
                    amountCents = amountYuan.toDoubleOrNull()?.let { (it * 100).toLong() },
                    note = rec.get(5),
                    tags = if (rec.size() >= 7) rec.get(6).split(",").map { it.trim() }.filter { it.isNotBlank() } else emptyList()
                )
            )
        }
        return result
    }
    private fun parseTemplate(records: List<CSVRecord>, headerIdx: Int): List<CsvRow> {
        val header = records[headerIdx]
        fun col(name: String): Int = (0 until header.size()).firstOrNull { header.get(it).trim() == name } ?: -1
        val cType = col("类型"); val cDate = col("日期"); val cBig = col("大类"); val cSub = col("小类"); val cAmount = col("金额"); val cAcc = col("账户"); val cAcc2 = col("账户2"); val cNote = col("备注"); val cTag = col("标签"); val cMerchant = col("商家")
        val result = mutableListOf<CsvRow>()
        records.drop(headerIdx + 1).forEach { rec ->
            val typeRaw = if (cType in 0 until rec.size()) rec.get(cType).trim() else ""
            if (typeRaw !in setOf("支出", "收入", "转账", "借", "贷", "应付款", "应收款")) return@forEach
            val type = when (typeRaw) { "支出" -> TransactionType.EXPENSE; "收入" -> TransactionType.INCOME; "转账" -> TransactionType.TRANSFER; else -> TransactionType.LOAN }
            val direction = when (typeRaw) { "借", "应付款" -> LoanDirection.IN; "贷", "应收款" -> LoanDirection.OUT; else -> null }
            fun cell(idx: Int): String = if (idx >= 0 && idx < rec.size()) rec.get(idx).trim() else ""
            result.add(
                CsvRow(
                    date = parseDateCell(cell(cDate)),
                    type = type,
                    category = cell(cBig).takeIf { it.isNotBlank() },
                    subcategory = cell(cSub).takeIf { it.isNotBlank() },
                    account = cell(cAcc).takeIf { it.isNotBlank() },
                    amountCents = cell(cAmount).toDoubleOrNull()?.let { (it * 100).toLong() },
                    note = cell(cNote),
                    tags = cell(cTag).split(" ", "　").map { it.trim() }.filter { it.isNotBlank() },
                    toAccount = cell(cAcc2).takeIf { it.isNotBlank() },
                    loanDirection = direction,
                    merchant = cell(cMerchant).takeIf { it.isNotBlank() }
                )
            )
        }
        return result
    }
    private fun parseDateCell(text: String): Long? {
        val t = text.trim()
        DATE_PATTERNS.forEach { p -> try { return java.text.SimpleDateFormat(p, java.util.Locale.CHINA).parse(t)?.time } catch (_: Exception) { } }
        return null
    }
    fun typeLabel(t: TransactionType): String = when (t) { TransactionType.EXPENSE -> "支出"; TransactionType.INCOME -> "收入"; TransactionType.TRANSFER -> "转账"; TransactionType.LOAN -> "借贷" }
    private fun typeOf(label: String?): TransactionType? = when (label) { "支出" -> TransactionType.EXPENSE; "收入" -> TransactionType.INCOME; "转账" -> TransactionType.TRANSFER; else -> null }
}
