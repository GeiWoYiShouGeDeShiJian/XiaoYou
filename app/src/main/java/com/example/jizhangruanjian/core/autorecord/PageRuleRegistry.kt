package com.example.jizhangruanjian.core.autorecord
object PageRuleRegistry {
    val rules = listOf(
        // 微信：账单详情优先，红包/转账次之，支付成功兜底
        PageRule("wx_bill_detail", "com.tencent.mm", listOf("交易单号", "支付方式")),
        PageRule("wx_red_packet", "com.tencent.mm", listOf("微信红包", "已存入零钱"), billType = BillType.INCOME),
        PageRule("wx_transfer_in", "com.tencent.mm", listOf("朋友已收钱"), billType = BillType.INCOME),
        PageRule("wx_transfer_out", "com.tencent.mm", listOf("已转账")),
        PageRule("wx_pay_success", "com.tencent.mm", listOf("支付成功")),
        // 支付宝：账单详情优先，收款到账次之，支付成功兜底
        PageRule("alipay_bill_detail", "com.eg.android.AlipayGphone", listOf("创建时间", "付款方式")),
        PageRule("alipay_receive", "com.eg.android.AlipayGphone", listOf("收款", "到账"), billType = BillType.INCOME),
        PageRule("alipay_pay_success", "com.eg.android.AlipayGphone", listOf("支付成功", "付款方式"))
    )
}
