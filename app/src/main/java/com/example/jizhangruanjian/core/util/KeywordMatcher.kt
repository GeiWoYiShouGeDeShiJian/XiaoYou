package com.example.jizhangruanjian.core.util
import com.example.jizhangruanjian.domain.model.CategoryDomain
import org.json.JSONArray
// R13 备注自动识别分类：遍历 keyword_match，取命中关键词最多的分类
object KeywordMatcher {
    fun match(note: String, categories: List<CategoryDomain>): CategoryDomain? {
        val n = note.trim()
        if (n.isEmpty()) return null
        var best: CategoryDomain? = null
        var bestCount = 0
        for (c in categories) {
            val keywords = parseKeywords(c.keywordMatch)
            val count = keywords.count { n.contains(it) }
            if (count > bestCount) { bestCount = count; best = c }
        }
        return best
    }
    private fun parseKeywords(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) { emptyList() }
    }
}