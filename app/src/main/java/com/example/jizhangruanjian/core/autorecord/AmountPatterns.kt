package com.example.jizhangruanjian.core.autorecord
object AmountPatterns {
    val MONEY = Regex("""[¥￥]\s*\d{1,3}(?:[,，]\d{3})*(?:\.\d{1,2})|\d{1,3}(?:[,，]\d{3})*(?:\.\d{1,2})\s*元""")
    val PAGE_MONEY = Regex("""[¥￥]\s*\d{1,3}(?:[,，]\d{3})*(?:\.\d{1,2})|\d{1,3}(?:[,，]\d{3})*\.\d{1,2}""")
    val ORDER_NO = Regex("""\d{10,32}""")
    val TRADE_TIME = Regex("""\d{4}[-/年]\d{1,2}[-/月]\d{1,2}(?:\s+\d{1,2}:\d{2}(?::\d{2})?)?""")
}
