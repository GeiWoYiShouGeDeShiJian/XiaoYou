package com.example.jizhangruanjian.core.autorecord
import android.content.Context
import android.net.Uri
import android.util.LruCache
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jizhangruanjian.core.autorecord.overlay.BillBubbleOverlay
import com.example.jizhangruanjian.core.autorecord.overlay.BillConfirmOverlay
import com.example.jizhangruanjian.core.database.TagDao
import com.example.jizhangruanjian.core.database.TransactionTagDao
import com.example.jizhangruanjian.core.util.KeywordMatcher
import com.example.jizhangruanjian.core.work.NotificationHelper
import com.example.jizhangruanjian.data.CurrentLedgerHolder
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.model.PaymentStatus
import com.example.jizhangruanjian.data.model.ReimbursementStatus
import com.example.jizhangruanjian.data.model.RefundStatus
import com.example.jizhangruanjian.data.model.Tag
import com.example.jizhangruanjian.data.model.TransactionSource
import com.example.jizhangruanjian.data.model.TransactionTag
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.repository.AccountRepository
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.data.repository.TransactionRepository
import com.example.jizhangruanjian.domain.model.TransactionDomain
import com.example.jizhangruanjian.domain.usecase.SaveTransactionUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.first

@Singleton
class AutoRecordPipeline @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: AutoRecordSettings,
    private val transactionRepository: TransactionRepository,
    private val currentLedgerHolder: CurrentLedgerHolder,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val overlay: BillConfirmOverlay,
    private val bubble: BillBubbleOverlay,
    private val tagDao: TagDao,
    private val transactionTagDao: TransactionTagDao,
    private val logger: AutoRecordLogger
) {
    private val pageDebounce = object : LruCache<String, Long>(64) {}
    private val fingerprintDebounce = object : LruCache<String, Long>(64) {}
    private val pageWindowMs = 1000L
    private val fingerprintWindowMs = 10 * 60 * 1000L

    suspend fun enabled(): Boolean = settings.isEnabled()
    suspend fun notifyWhitelist(): List<String> = settings.apps()
    fun isSamePageDebounced(pkg: String): Boolean {
        val now = System.currentTimeMillis()
        val last = pageDebounce.get(pkg) ?: 0L
        if (now - last < pageWindowMs) return true
        pageDebounce.put(pkg, now)
        return false
    }
    suspend fun isIgnored(fp: String): Boolean = settings.ignoredFingerprints().contains(fp)
    fun fingerprint(c: BillCandidate): String =
        if (c.orderNo != null) "${c.packageName}|${c.orderNo}" else "${c.packageName}|${c.amount}|${c.merchant}|${dateStr(c.tradeTime)}"
    fun autoDedupHash(c: BillCandidate): String {
        val input = if (c.orderNo != null) "AUTO|${c.packageName}|${c.orderNo}" else "AUTO|${c.packageName}|${c.amount}|${c.merchant}|${dateStr(c.tradeTime)}|${c.billType.name}"
        return md5(input)
    }
    // 三层防线：页面防抖 → 用户忽略 → 入库查重；返回 null 表示通过，非 null 为拦截原因
    suspend fun dedupGuard(c: BillCandidate): String? {
        val fp = fingerprint(c)
        if (isFingerprintDebounced(fp)) return "页面防抖"
        if (isIgnored(fp)) return "用户忽略"
        if (transactionRepository.findByDedupHash(autoDedupHash(c)) != null) return "入库去重"
        fingerprintDebounce.put(fp, System.currentTimeMillis())
        return null
    }
    // 重复账单处理：开关开 → 弹窗编辑确认；关 → 仅提示通知
    private suspend fun handleDuplicate(c: BillCandidate) {
        if (settings.dupEdit()) {
            log("重复账单 → 弹窗编辑")
            overlay.show(c)
        } else {
            log("重复账单 → 仅提示")
            notifyConfirm(c, TransactionSource.AUTO_NOTIFICATION)
        }
    }
    // 无障碍通道入口：开关 → 白名单 → 页面规则匹配 → 字段提取 → 三层防线 → 分发
    suspend fun submitFromNodes(pkg: String, rootNode: AccessibilityNodeInfo) {
        if (!enabled() || !settings.isAccEnabled()) return
        if (pkg !in settings.apps()) return
        val snap = collectNodes(rootNode)
        val rule = matchRule(pkg, snap) ?: return
        val amount = extractPageAmount(snap) ?: return
        val merchant = extractMerchant(snap)
        val orderNo = extractOrderNo(snap)
        val tradeTime = extractTradeTime(snap)
        val c = BillCandidate(
            packageName = pkg,
            amount = amount,
            merchant = merchant,
            orderNo = orderNo,
            tradeTime = tradeTime,
            billType = extractDirection(snap.fullText.toString(), rule.billType),
            note = merchant ?: ""
        )
        val blocked = dedupGuard(c)
        if (blocked != null) {
            log("${rule.id} 命中 ¥${fmt(amount)} ${merchant ?: ""} → $blocked 丢弃")
            if (blocked == "入库去重") handleDuplicate(c)
            return
        }
        settings.setLastHit(System.currentTimeMillis())
        log("${rule.id} 命中 ¥${fmt(amount)} ${merchant ?: ""}")
        dispatch(c)
    }
    private class NodeSnapshot {
        val moneyNodes = mutableListOf<Pair<String, Float>>()
        val fullText = StringBuilder()
        val labelValues = mutableMapOf<String, String>()
        var firstText: String? = null
    }
    private fun collectNodes(root: AccessibilityNodeInfo): NodeSnapshot {
        val snap = NodeSnapshot()
        dfs(root, 0, 0, snap)
        return snap
    }
    private fun dfs(node: AccessibilityNodeInfo, depth: Int, count: Int, snap: NodeSnapshot) {
        if (depth > 25 || count > 800) return
        val text = node.text?.toString() ?: node.contentDescription?.toString() ?: ""
        if (text.isNotBlank()) {
            if (snap.firstText == null) snap.firstText = text
            snap.fullText.append(text).append(' ')
            if (AmountPatterns.PAGE_MONEY.containsMatchIn(text)) {
                val size = getTextSize(node)
                snap.moneyNodes.add(text to size)
            }
            for (label in LABELS) {
                if (text.contains(label) && !snap.labelValues.containsKey(label)) {
                    valueFromNode(node, text, label)?.let { snap.labelValues[label] = it }
                }
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            dfs(child, depth + 1, count + 1, snap)
        }
    }
    private fun getTextSize(node: AccessibilityNodeInfo): Float {
        return try {
            val f = AccessibilityNodeInfo::class.java.getDeclaredField("mTextSize")
            f.isAccessible = true
            f.getFloat(node)
        } catch (_: Exception) {
            16f
        }
    }
    private fun valueFromNode(node: AccessibilityNodeInfo, text: String, label: String): String? {
        val idx = text.indexOfFirst { it == '：' || it == ':' }
        if (idx > 0 && text.substring(0, idx).contains(label)) {
            val v = text.substring(idx + 1).trim()
            if (v.isNotBlank()) return v
        }
        if (text.startsWith(label)) {
            val v = text.removePrefix(label).trim()
            if (v.isNotBlank()) return v
        }
        val parent = node.parent ?: return null
        for (i in 0 until parent.childCount) {
            val sib = parent.getChild(i) ?: continue
            val t = sib.text?.toString()?.trim().orEmpty()
            if (t.isNotBlank() && t != text) return t
        }
        return null
    }
    private fun matchRule(pkg: String, snap: NodeSnapshot): PageRule? {
        val full = snap.fullText.toString()
        for (rule in PageRuleRegistry.rules) {
            if (rule.packageName != pkg) continue
            if (rule.requiredMarkers.any { !full.contains(it) }) continue
            if (rule.anyMarkers.isNotEmpty() && rule.anyMarkers.none { full.contains(it) }) continue
            return rule
        }
        return null
    }
    private fun extractPageAmount(snap: NodeSnapshot): Long? {
        var best: Double? = null
        var bestSize = -1f
        var bestSymbol = false
        for ((text, size) in snap.moneyNodes) {
            if (text.contains("退款") || text.contains("优惠") || text.contains("券")) continue
            val m = AmountPatterns.PAGE_MONEY.find(text) ?: continue
            val v = m.value.replace(",", "").replace("，", "").replace("¥", "").replace("￥", "").replace("元", "").trim().toDoubleOrNull() ?: continue
            if (v <= 0.0 || v > 1_000_000) continue
            val symbol = text.contains("¥") || text.contains("￥")
            if (best == null || size > bestSize || (size == bestSize && symbol && !bestSymbol)) {
                best = v
                bestSize = size
                bestSymbol = symbol
            }
        }
        return best?.let { (it * 100).roundToLong() }
    }
    private fun extractDirection(fullText: String, default: BillType): BillType = when {
        fullText.contains("退款") -> BillType.REFUND
        fullText.contains("已存入") || fullText.contains("到账") || fullText.contains("收入") || fullText.contains("已收钱") -> BillType.INCOME
        else -> default
    }
    private fun extractMerchant(snap: NodeSnapshot): String? {
        for (label in listOf("商户全称", "对方", "收款方")) {
            val v = snap.labelValues[label] ?: continue
            if (v.isNotBlank()) return v
        }
        return snap.firstText?.takeIf { it.isNotBlank() }
    }
    private fun extractOrderNo(snap: NodeSnapshot): String? {
        for (label in listOf("交易单号", "订单号")) {
            val v = snap.labelValues[label] ?: continue
            val m = AmountPatterns.ORDER_NO.find(v) ?: continue
            return m.value
        }
        return null
    }
    private fun extractTradeTime(snap: NodeSnapshot): Long {
        for (label in listOf("交易时间", "创建时间")) {
            val v = snap.labelValues[label] ?: continue
            val m = AmountPatterns.TRADE_TIME.find(v) ?: continue
            parseTime(m.value)?.let { return it }
        }
        return System.currentTimeMillis()
    }
    private fun parseTime(s: String): Long? {
        val cleaned = s.replace("年", "-").replace("月", "-").replace("日", " ").replace(Regex("\\s+"), " ").trim()
        for (f in listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd")) {
            try {
                return SimpleDateFormat(f, Locale.CHINA).parse(cleaned)?.time
            } catch (_: Exception) {}
        }
        return null
    }
    // 通知通道入口：开关 → 白名单 → 解析 → 三层防线 → 分发
    suspend fun submitFromNotification(pkg: String, title: String, text: String, postTime: Long) {
        if (!enabled()) return
        if (pkg !in notifyWhitelist()) return
        val content = "$title $text"
        if (content.contains("退款") || content.contains("退货")) {
            log("$pkg 通知含退款，跳过")
            return
        }
        val c = parseNotification(pkg, title, text, postTime)
        if (c == null) {
            log("$pkg 通知无金额，丢弃")
            return
        }
        val blocked = dedupGuard(c)
        if (blocked != null) {
            log("$pkg 通知命中 ¥${fmt(c.amount)} → $blocked 丢弃")
            if (blocked == "入库去重") handleDuplicate(c)
            return
        }
        settings.setLastHit(System.currentTimeMillis())
        log("$pkg 通知命中 ¥${fmt(c.amount)}")
        dispatch(c)
    }
    // 模式分发：退款不自动入账只发通知；overlay=悬浮窗确认，silent=静默入账，其余走通知确认
    private suspend fun dispatch(c: BillCandidate) {
        if (c.billType == BillType.REFUND) {
            log("退款候选 ¥${fmt(c.amount)} → 仅通知提醒")
            notifyConfirm(c)
            return
        }
        when (settings.mode()) {
            AutoRecordSettings.MODE_SILENT -> { log("→ 静默入账"); silentRecord(c) }
            AutoRecordSettings.MODE_OVERLAY -> { log("→ 弹窗确认"); overlay.show(c) }
            else -> { log("→ 通知确认"); notifyConfirm(c) }
        }
    }
    // 模式B：通知确认 → 点击预填记账页
    suspend fun notifyConfirm(c: BillCandidate, source: TransactionSource = TransactionSource.AUTO_NOTIFICATION) {
        val type = if (c.billType == BillType.INCOME) TransactionType.INCOME else TransactionType.EXPENSE
        val amountYuan = String.format(Locale.CHINA, "%.2f", c.amount / 100.0)
        val url = "jizhang://record?amount=$amountYuan&type=${type.name}&merchant=${Uri.encode(c.merchant ?: "")}&source=${source.name}"
        NotificationHelper.notifyAutoRecord(context, c.packageName.hashCode(), "自动记账识别", "¥$amountYuan ${c.merchant ?: ""}，点击确认入账", url)
    }
    // 模式C：静默入账 + 可撤销通知
    private suspend fun silentRecord(c: BillCandidate): Long {
        val txId = saveRecord(c, TransactionSource.AUTO_SILENT)
        if (txId > 0L) {
            log("已入账 #$txId，弹悬浮球")
            val initialCategoryId = resolveCategoryId(c)
            val shown = runCatching { bubble.show(txId, c, initialCategoryId) }.getOrDefault(false)
            if (!shown) {
                val amountYuan = String.format(Locale.CHINA, "%.2f", c.amount / 100.0)
                NotificationHelper.notifyAutoRecord(context, c.packageName.hashCode(), "已自动记账", "¥$amountYuan ${c.merchant ?: ""}，点击撤销", "jizhang://undo?txId=$txId")
            }
        } else {
            log("测试未入账（txId=$txId）")
        }
        return txId
    }
    // 悬浮窗"记一笔"：入账 source=AUTO_ACCESSIBILITY
    suspend fun confirmRecord(c: BillCandidate): Long {
        val txId = saveRecord(c, TransactionSource.AUTO_ACCESSIBILITY)
        if (txId > 0L) log("已入账 #$txId")
        return txId
    }
    // 悬浮窗"忽略本笔"：指纹入忽略名单
    suspend fun ignore(c: BillCandidate) {
        settings.addIgnored(fingerprint(c))
        log("已加入忽略名单")
    }
    // 悬浮球"确认更新"：改分类/金额后更新该笔
    suspend fun updateRecorded(
        txId: Long,
        categoryId: Long,
        amountCents: Long,
        accountId: Long = 0L,
        note: String? = null,
        merchant: String? = null,
        paymentStatus: PaymentStatus? = null,
        reimbursementStatus: ReimbursementStatus? = null,
        refundStatus: RefundStatus? = null,
        includeInSummary: Boolean? = null,
        includeInBudget: Boolean? = null,
        tagIds: List<Long>? = null,
        discount: Long? = null,
        memberId: Long? = null
    ) {
        if (categoryId <= 0L || amountCents < 0L) return
        val old = transactionRepository.getDomain(txId) ?: return
        val updated = old.copy(
            id = txId,
            categoryId = categoryId,
            amount = amountCents,
            accountId = if (accountId > 0L) accountId else old.accountId,
            note = note ?: old.note,
            merchant = merchant ?: old.merchant,
            paymentStatus = paymentStatus ?: old.paymentStatus,
            reimbursementStatus = reimbursementStatus ?: old.reimbursementStatus,
            refundStatus = refundStatus ?: old.refundStatus,
            includeInSummary = includeInSummary ?: old.includeInSummary,
            includeInBudget = includeInBudget ?: old.includeInBudget,
            tagIds = tagIds ?: old.tagIds,
            discount = discount ?: old.discount,
            memberId = memberId ?: old.memberId,
            memberIds = if (memberId != null && memberId > 0L) listOf(memberId) else old.memberIds
        )
        saveTransactionUseCase.execute(updated, old, true)
        log("悬浮球更新 #$txId 分类=$categoryId 金额=$amountCents 账户=$accountId")
        if (tagIds != null) settings.setMemTags(tagIds.filter { it > 0L })
        if (!merchant.isNullOrBlank()) settings.setMemMerchant(merchant)
        if (memberId != null) settings.setMemMembers(if (memberId > 0L) listOf(memberId) else emptyList())
    }
    suspend fun getTransaction(txId: Long): TransactionDomain? = transactionRepository.getDomain(txId)
    // 悬浮球"撤销"：软删除该笔进回收站
    suspend fun removeRecorded(txId: Long) {
        transactionRepository.softDelete(txId)
        log("悬浮球撤销 #$txId")
    }
    // 自动记账测试：造一笔模拟账单走静默入账+悬浮球链路
    suspend fun simulateHit(): Long {
        val c = BillCandidate(packageName = "com.tencent.mm", amount = 1550, merchant = "模拟测试·自动记账", billType = BillType.EXPENSE)
        log("测试触发生成候选 ¥15.50")
        return silentRecord(c)
    }
    private suspend fun saveRecord(c: BillCandidate, source: TransactionSource): Long {
        val ledgerId = settings.defaultLedgerId().takeIf { it > 0L } ?: currentLedgerHolder.id.value
        if (ledgerId == 0L) return 0L
        val accountId = resolveAccountId(c.packageName)
        if (accountId == 0L) return 0L
        val categoryId = resolveCategoryId(c)
        if (categoryId == 0L) return 0L
        val note = if (settings.defaultNoteMode() == AutoRecordSettings.NOTE_MODE_MERCHANT && c.merchant.isNullOrBlank().not()) c.merchant else c.note.ifBlank { c.merchant ?: "" }
        val merchant = if (settings.defaultMerchantMode() == AutoRecordSettings.MODE_MEM_LAST) settings.memMerchant().takeIf { it.isNotBlank() } else null
        if (c.billType == BillType.EXPENSE) checkInsufficientBalance(accountId, c)
        val tx = TransactionDomain(
            ledgerId = ledgerId,
            accountId = accountId,
            categoryId = categoryId,
            type = if (c.billType == BillType.INCOME) TransactionType.INCOME else TransactionType.EXPENSE,
            amount = c.amount,
            note = note,
            merchant = merchant,
            tradeDate = c.tradeTime,
            memberIds = settings.memMembers(),
            tagIds = settings.memTags(),
            source = source
        )
        val result = saveTransactionUseCase.execute(tx, null, true)
        val txId = (result as? SaveTransactionUseCase.Result.Success)?.txId ?: 0L
        if (txId > 0L && settings.channelTag()) applyChannelTag(txId, c.packageName)
        return txId
    }
    // 余额不足提醒：支出金额超过账户余额时通知提示（不阻断入账）
    private suspend fun checkInsufficientBalance(accountId: Long, c: BillCandidate) {
        if (!settings.insufficientTip()) return
        val acc = accountRepository.observeAll().first().firstOrNull { it.id == accountId } ?: return
        if (acc.balance < c.amount) {
            val amountYuan = String.format(Locale.CHINA, "%.2f", c.amount / 100.0)
            val balanceYuan = String.format(Locale.CHINA, "%.2f", acc.balance / 100.0)
            NotificationHelper.notify(context, c.amount.toInt(), "余额不足提醒", "${acc.name} 余额 ¥$balanceYuan 不足以覆盖本笔 ¥$amountYuan")
        }
    }
    // 自动添加「交易渠道」标签：按来源打渠道标签，不存在则创建
    private suspend fun applyChannelTag(txId: Long, pkg: String) {
        val name = channelName(pkg)
        val tag = tagDao.observeAll().first().firstOrNull { it.name == name } ?: run {
            val newId = tagDao.insert(Tag(name = name))
            tagDao.getById(newId) ?: return
        }
        transactionTagDao.insert(TransactionTag(txId, tag.id))
    }
    private fun channelName(pkg: String): String = when (pkg) {
        "com.tencent.mm" -> "微信"
        "com.eg.android.AlipayGphone" -> "支付宝"
        "com.unionpay" -> "云闪付"
        else -> "交易渠道"
    }
    suspend fun resolveAccountId(pkg: String): Long {
        val preset = when (pkg) {
            "com.tencent.mm" -> settings.accountWechat()
            "com.eg.android.AlipayGphone" -> settings.accountAlipay()
            else -> 0L
        }
        if (preset > 0L) return preset
        settings.defaultAccountId().takeIf { it > 0L }?.let { return it }
        return accountRepository.observeAll().first().firstOrNull()?.id ?: 0L
    }
    suspend fun resolveCategoryId(c: BillCandidate): Long {
        val type = if (c.billType == BillType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
        val cats = categoryRepository.getAll().filter { it.type == type }
        val keyword = c.merchant ?: c.note
        if (keyword.isNotBlank()) KeywordMatcher.match(keyword, cats)?.let { return it.id }
        settings.defaultCategoryId().takeIf { it > 0L }?.let { def -> cats.firstOrNull { it.id == def }?.let { return it.id } }
        return cats.firstOrNull()?.id ?: 0L
    }
    private fun parseNotification(pkg: String, title: String, text: String, postTime: Long): BillCandidate? {
        val content = "$title $text"
        val amount = extractAmount(content) ?: return null
        val billType = when {
            content.contains("收入") || content.contains("到账") || content.contains("收款") || content.contains("收到") -> BillType.INCOME
            else -> BillType.EXPENSE
        }
        return BillCandidate(packageName = pkg, amount = amount, tradeTime = postTime, billType = billType)
    }
    private fun extractAmount(content: String): Long? {
        val values = AmountPatterns.MONEY.findAll(content).map { m ->
            m.value.replace(",", "").replace("，", "").replace("¥", "").replace("￥", "").replace("元", "").trim().toDoubleOrNull()
        }.filterNotNull()
        val v = values.maxOrNull() ?: return null
        if (v <= 0.0 || v > 1_000_000) return null
        return (v * 100).roundToLong()
    }
    private fun isFingerprintDebounced(fp: String): Boolean {
        val now = System.currentTimeMillis()
        val last = fingerprintDebounce.get(fp) ?: 0L
        return now - last < fingerprintWindowMs
    }
    private fun dateStr(ts: Long): String = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date(ts))
    private fun fmt(cents: Long): String = String.format(Locale.CHINA, "%.2f", cents / 100.0)
    private suspend fun log(msg: String) {
        logger.log("[${SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(Date())}] $msg")
    }
    private fun md5(input: String): String = MessageDigest.getInstance("MD5").digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    companion object {
        private val LABELS = listOf("交易单号", "订单号", "商户全称", "对方", "收款方", "交易时间", "创建时间")
    }
}
