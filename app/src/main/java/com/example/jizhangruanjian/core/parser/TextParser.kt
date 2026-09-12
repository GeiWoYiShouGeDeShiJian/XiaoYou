package com.example.jizhangruanjian.core.parser
import com.example.jizhangruanjian.core.util.KeywordMatcher
import com.example.jizhangruanjian.domain.model.CategoryDomain
// P12 文本解析：正则提取金额 + R13 关键词自动归类
data class ParsedRecord(val amountCents: Long?, val note: String, val category: CategoryDomain?)
object TextParser {
    private val moneyRegex = Regex("""(\d+(?:\.\d+)?)\s*(元|块)?""")
    fun parse(text: String, categories: List<CategoryDomain>): ParsedRecord {
        val m = moneyRegex.find(text)
        val amountCents = m?.groupValues?.get(1)?.toDoubleOrNull()?.let { (it * 100).toLong() }
        val note = text.removeRange(m?.range?.first ?: 0, m?.range?.last?.plus(1) ?: 0).trim().ifBlank { text.trim() }
        return ParsedRecord(amountCents, note, KeywordMatcher.match(note, categories))
    }
}