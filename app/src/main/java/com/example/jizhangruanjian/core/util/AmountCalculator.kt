package com.example.jizhangruanjian.core.util
// R11 计算器精度：四则运算、结果最小 1 分、保留两位小数四舍五入
object AmountCalculator {
    fun evaluate(expression: String): Double? {
        val trimmed = expression.replace(" ", "")
        if (trimmed.isBlank() || !TRIM.matches(trimmed)) return null
        val tokens = Regex("\\d+(\\.\\d+)?|[+\\-*/]").findAll(trimmed).map { it.value }.toList().toMutableList()
        if (tokens.isEmpty() || !tokens.first().isNumber() || !tokens.last().isNumber()) return null
        var i = 0
        while (i + 2 < tokens.size) {
            if (tokens[i].isNumber() && (tokens[i + 1] == "*" || tokens[i + 1] == "/")) {
                val a = tokens.removeAt(i).toDouble()
                val op = tokens.removeAt(i)
                val b = tokens.removeAt(i).toDouble()
                val r = if (op == "*") a * b else if (b == 0.0) return null else a / b
                tokens.add(i, format(r))
            } else i += 2
        }
        var result = tokens[0].toDouble()
        i = 1
        while (i + 1 < tokens.size) {
            val op = tokens[i]; val v = tokens[i + 1].toDouble()
            result = when (op) { "+" -> result + v; "-" -> result - v; else -> result }
            i += 2
        }
        return result
    }
    // 表达式（元）→ 分；保留两位小数四舍五入，0 元合法；空/无效表达式返回 -1
    fun toCents(expression: String): Long {
        val v = evaluate(expression) ?: return -1L
        return Math.round(v * 100)
    }
    private fun String.isNumber() = firstOrNull()?.isDigit() == true
    private fun format(v: Double): String {
        return if (v == Math.floor(v) && !v.isInfinite()) v.toLong().toString() else v.toString()
    }
    private val TRIM = Regex("^[0-9.\\s+\\-*/]*$")
}