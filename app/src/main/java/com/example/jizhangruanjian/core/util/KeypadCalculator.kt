package com.example.jizhangruanjian.core.util
object KeypadCalculator {
    // 键盘输入：追加数字/小数点/运算符，运算符自动替换连续运算符
    fun digest(current: String, key: String): String {
        if (current.length >= 12) return current
        if (key == ".") {
            val clean = current.removePrefix("-").substringAfterLast('/').substringAfterLast('*').substringAfterLast('-').substringAfterLast('+')
            return if (clean.contains(".")) current else current + "."
        }
        if (key == "+" || key == "-" || key == "*" || key == "/") {
            val trimmed = current.trimEnd()
            if (trimmed.isEmpty()) return current
            val last = trimmed.last()
            return if (last == '+' || last == '-' || last == '*' || last == '/') trimmed.dropLast(1) + key else trimmed + key
        }
        if (key == "=") {
            val trimmed = current.trimEnd().trimEnd('+', '-', '*', '/', '.')
            if (trimmed.isEmpty()) return current
            val cents = runCatching { AmountCalculator.toCents(trimmed) }.getOrNull() ?: return current
            return if (cents % 100L == 0L) (cents / 100L).toString() else String.format(java.util.Locale.CHINA, "%.2f", cents / 100.0)
        }
        if (key.length == 1 && key[0].isDigit()) {
            val segment = current.substringAfterLast('/').substringAfterLast('*').substringAfterLast('-').substringAfterLast('+')
            val dotIndex = segment.indexOf('.')
            if (dotIndex >= 0 && segment.length - dotIndex - 1 >= 2) return current
        }
        return current + key
    }
    fun toCents(expression: String): Long = AmountCalculator.toCents(expression)
    // 删除末位；小数位删空后连同小数点一起删除
    fun deleteLast(current: String): String {
        if (current.isEmpty()) return current
        var r = current.dropLast(1)
        if (r.endsWith(".")) r = r.dropLast(1)
        return r
    }
}