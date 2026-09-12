package com.example.jizhangruanjian.core.autorecord.overlay
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.jizhangruanjian.core.autorecord.AutoRecordPipeline
import com.example.jizhangruanjian.core.autorecord.BillCandidate
import com.example.jizhangruanjian.core.autorecord.BillType
import com.example.jizhangruanjian.core.database.AppSettingDao
import com.example.jizhangruanjian.core.database.MemberDao
import com.example.jizhangruanjian.core.database.MerchantDao
import com.example.jizhangruanjian.core.database.TagDao
import com.example.jizhangruanjian.core.util.KeypadCalculator
import com.example.jizhangruanjian.core.work.NotificationHelper
import com.example.jizhangruanjian.data.model.AppSetting
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.model.Member
import com.example.jizhangruanjian.data.model.Merchant
import com.example.jizhangruanjian.data.model.PaymentStatus
import com.example.jizhangruanjian.data.model.ReimbursementStatus
import com.example.jizhangruanjian.data.model.RefundStatus
import com.example.jizhangruanjian.data.model.Tag
import com.example.jizhangruanjian.data.repository.AccountRepository
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.components.KeypadAction
import com.example.jizhangruanjian.ui.components.NumericKeypad
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BubbleEntryPoint {
    fun pipeline(): AutoRecordPipeline
    fun categoryRepository(): CategoryRepository
    fun accountRepository(): AccountRepository
    fun tagDao(): TagDao
    fun memberDao(): MemberDao
    fun appSettingDao(): AppSettingDao
    fun merchantDao(): MerchantDao
}
// 自动记账悬浮球：识别入账后显示可拖动小球；点开展开底部面板(可拉全屏)，含分类/金额/账户/备注/两排功能，确认后更新这笔，或撤销
@Singleton
class BillBubbleOverlay @Inject constructor(@ApplicationContext private val context: Context) {
    private val entryPoint = EntryPointAccessors.fromApplication(context, BubbleEntryPoint::class.java)
    private val pipeline get() = entryPoint.pipeline()
    private val categoryRepository get() = entryPoint.categoryRepository()
    private val accountRepository get() = entryPoint.accountRepository()
    private val tagDao get() = entryPoint.tagDao()
    private val memberDao get() = entryPoint.memberDao()
    private val appSettingDao get() = entryPoint.appSettingDao()
    private val merchantDao get() = entryPoint.merchantDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val density = context.resources.displayMetrics.density
    private val screenW = context.resources.displayMetrics.widthPixels
    private val screenH = context.resources.displayMetrics.heightPixels
    private val ballSize = (80 * density).toInt()
    private var composeView: ComposeView? = null
    private var owner: OverlayLifecycleOwner? = null
    private var dismissJob: Job? = null
    private var pendingTxId = 0L
    private var pendingCandidate: BillCandidate? = null
    private var expandedState by mutableStateOf(false)
    private var fullScreenState by mutableStateOf(false)
    private var ballAnimator: ValueAnimator? = null
    private var ballX = -1
    private var ballY = -1
    private companion object {
        const val KEY_BALL_X = "bubble_ball_x"
        const val KEY_BALL_Y = "bubble_ball_y"
    }
    private val params = WindowManager.LayoutParams(
        screenW, screenH,
        if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 0
        y = 0
    }
    // 入账成功后显示悬浮球；无悬浮窗权限返回 false（调用方降级通知）
    fun show(txId: Long, c: BillCandidate, initialCategoryId: Long): Boolean {
        if (!Settings.canDrawOverlays(context)) return false
        dismiss()
        expandedState = false
        fullScreenState = false
        val cats = runCatching { kotlinx.coroutines.runBlocking { categoryRepository.getAll() } }.getOrDefault(emptyList())
        val accounts = runCatching { kotlinx.coroutines.runBlocking { accountRepository.observeAll().first() } }.getOrDefault(emptyList())
        val tags = runCatching { kotlinx.coroutines.runBlocking { tagDao.observeAll().first() } }.getOrDefault(emptyList())
        val members = runCatching { kotlinx.coroutines.runBlocking { memberDao.observeAll().first() } }.getOrDefault(emptyList())
        val merchants = runCatching { kotlinx.coroutines.runBlocking { merchantDao.observeAll().first() } }.getOrDefault(emptyList())
        val accountId = runCatching { kotlinx.coroutines.runBlocking { pipeline.resolveAccountId(c.packageName) } }.getOrDefault(0L)
        val txTagIds = runCatching { kotlinx.coroutines.runBlocking { pipeline.getTransaction(txId) } }.getOrNull()?.tagIds ?: emptyList()
        val view = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            val o = OverlayLifecycleOwner()
            this@BillBubbleOverlay.owner = o
            setViewTreeLifecycleOwner(o)
            setViewTreeViewModelStoreOwner(o)
            setViewTreeSavedStateRegistryOwner(o)
            setContent {
                BubbleContent(
                    c = c,
                    categories = cats,
                    accounts = accounts,
                    tags = tags,
                    members = members,
                    merchants = merchants,
                    initialCategoryId = initialCategoryId,
                    initialAccountId = accountId,
                    initialTagIds = txTagIds,
                    expanded = expandedState,
                    fullScreen = fullScreenState,
                    onDragBall = { dx, dy -> moveBall(dx, dy) },
                    onBallDragEnd = { snapBallToEdge() },
                    onExpand = { expand() },
                    onCollapse = { collapse() },
                    onToggleFull = { toggleFull() },
                    onConfirm = { changes -> confirm(txId, changes) },
                    onUndo = { undo(txId) }
                )
            }
        }
        composeView = view
        loadBallPos()
        setBallParams()
        val added = runCatching { windowManager.addView(view, params) }.isSuccess
        if (!added) {
            owner?.destroy(); owner = null; composeView = null
            return false
        }
        pendingTxId = txId
        pendingCandidate = c
        startAutoDismiss(txId, c)
        return true
    }
    private fun loadBallPos() {
        val x = runCatching { kotlinx.coroutines.runBlocking { appSettingDao.get(KEY_BALL_X)?.value?.toIntOrNull() } }.getOrNull()
        val y = runCatching { kotlinx.coroutines.runBlocking { appSettingDao.get(KEY_BALL_Y)?.value?.toIntOrNull() } }.getOrNull()
        if (x != null) ballX = x
        if (y != null) ballY = y
    }
    private fun persistBallPos() {
        scope.launch {
            appSettingDao.upsert(AppSetting(KEY_BALL_X, ballX.toString()))
            appSettingDao.upsert(AppSetting(KEY_BALL_Y, ballY.toString()))
        }
    }
    private fun moveBall(dx: Float, dy: Float) {
        dismissJob?.cancel()
        val v = composeView ?: return
        params.x = (params.x + dx).roundToInt().coerceIn(0, screenW - ballSize)
        params.y = (params.y + dy).roundToInt().coerceIn(0, screenH - ballSize)
        ballX = params.x; ballY = params.y
        runCatching { windowManager.updateViewLayout(v, params) }
    }
    private fun snapBallToEdge() {
        pendingCandidate?.let { startAutoDismiss(pendingTxId, it) }
        persistBallPos()
        val v = composeView ?: return
        ballAnimator?.cancel()
        val maxX = screenW - ballSize
        val targetX = if (params.x < maxX - params.x) 0 else maxX
        if (params.x == targetX) return
        ballAnimator = ValueAnimator.ofInt(params.x, targetX).apply {
            duration = 200L
            interpolator = DecelerateInterpolator()
            addUpdateListener { a ->
                params.x = a.animatedValue as Int
                ballX = params.x
                runCatching { windowManager.updateViewLayout(v, params) }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { persistBallPos() }
            })
            start()
        }
    }
    private fun setFocusable(focusable: Boolean) {
        val v = composeView ?: return
        params.flags = if (focusable) params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv() else params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        runCatching { windowManager.updateViewLayout(v, params) }
    }
    private fun setBallParams() {
        params.width = ballSize
        params.height = ballSize
        params.gravity = Gravity.TOP or Gravity.START
        params.x = if (ballX >= 0) ballX else screenW - ballSize - (12 * density).toInt()
        params.y = if (ballY >= 0) ballY else (100 * density).toInt()
    }
    private fun fitBall() {
        val v = composeView ?: return
        setBallParams()
        runCatching { windowManager.updateViewLayout(v, params) }
    }
    private fun fitFull() {
        val v = composeView ?: return
        params.width = screenW
        params.height = screenH
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        runCatching { windowManager.updateViewLayout(v, params) }
    }
    private fun expand() {
        if (expandedState) return
        expandedState = true
        setFocusable(true)
        fitFull()
        dismissJob?.cancel()
    }
    private fun collapse() {
        if (!expandedState) return
        expandedState = false
        fullScreenState = false
        ballAnimator?.cancel()
        setFocusable(false)
        fitBall()
        pendingCandidate?.let { startAutoDismiss(pendingTxId, it) }
    }
    private fun toggleFull() {
        fullScreenState = !fullScreenState
    }
    private fun confirm(txId: Long, changes: OverlayChanges) {
        dismissJob?.cancel()
        scope.launch {
            if (changes.categoryId > 0L) pipeline.updateRecorded(
                txId, changes.categoryId, changes.amountCents, changes.accountId, changes.note,
                changes.merchant, changes.paymentStatus, changes.reimbursementStatus, changes.refundStatus,
                changes.includeInSummary, changes.includeInBudget, changes.tagIds, changes.discount, changes.memberId
            )
            dismiss()
        }
    }
    private fun undo(txId: Long) {
        dismissJob?.cancel()
        scope.launch { pipeline.removeRecorded(txId); dismiss() }
    }
    private fun startAutoDismiss(txId: Long, c: BillCandidate) {
        dismissJob?.cancel()
        dismissJob = scope.launch { delay(5_000); if (!expandedState) { sendUndoNotify(txId, c); dismiss() } }
    }
    private fun sendUndoNotify(txId: Long, c: BillCandidate) {
        val amountYuan = String.format(Locale.CHINA, "%.2f", c.amount / 100.0)
        runCatching { NotificationHelper.notifyAutoRecord(context, c.packageName.hashCode(), "已自动记账", "¥$amountYuan ${c.merchant ?: ""}，点击查看或撤销", "jizhang://undo?txId=$txId") }
    }
    private fun dismiss() {
        dismissJob?.cancel()
        ballAnimator?.cancel()
        composeView?.let { runCatching { windowManager.removeView(it) } }
        composeView = null
        owner?.destroy(); owner = null
        expandedState = false
        fullScreenState = false
    }
}
data class OverlayChanges(
    val categoryId: Long = 0L,
    val amountCents: Long = 0L,
    val accountId: Long = 0L,
    val note: String = "",
    val merchant: String = "",
    val paymentStatus: PaymentStatus? = null,
    val reimbursementStatus: ReimbursementStatus? = null,
    val refundStatus: RefundStatus? = null,
    val includeInSummary: Boolean = true,
    val includeInBudget: Boolean = true,
    val tagIds: List<Long> = emptyList(),
    val discount: Long = 0L,
    val memberId: Long? = null
)
private val GRAY_999 = Color(0xFF999999)
private val expenseRed = Color(0xFFE53935)
private val incomeGreen = Color(0xFF2E7D32)
@Composable
private fun BubbleContent(
    c: BillCandidate,
    categories: List<CategoryDomain>,
    accounts: List<AccountDomain>,
    tags: List<Tag>,
    members: List<Member>,
    merchants: List<Merchant>,
    initialCategoryId: Long,
    initialAccountId: Long,
    initialTagIds: List<Long>,
    expanded: Boolean,
    fullScreen: Boolean,
    onDragBall: (Float, Float) -> Unit,
    onBallDragEnd: () -> Unit,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onToggleFull: () -> Unit,
    onConfirm: (OverlayChanges) -> Unit,
    onUndo: () -> Unit
) {
    if (!expanded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                onClick = onExpand,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp).pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, amount -> change.consume(); onDragBall(amount.x, amount.y) },
                        onDragEnd = { onBallDragEnd() },
                        onDragCancel = { onBallDragEnd() }
                    )
                }
            ) {
                Box(contentAlignment = Alignment.Center) { Text("¥", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
            }
        }
        return
    }
    val isIncome = c.billType == BillType.INCOME
    val type = if (isIncome) CategoryType.INCOME else CategoryType.EXPENSE
    val parents = categories.filter { it.type == type && it.parentId == null }
    val initParentId = categories.firstOrNull { it.id == initialCategoryId }?.parentId
    var activeParentId by remember { mutableStateOf(if (parents.any { it.id == initParentId }) initParentId else parents.firstOrNull()?.id) }
    var selChildId by remember { mutableStateOf(if (initialCategoryId != 0L && categories.any { it.id == initialCategoryId && it.parentId != null }) initialCategoryId else 0L) }
    var amountText by remember { mutableStateOf(fmtYuan(c.amount)) }
    var noteText by remember { mutableStateOf(c.note) }
    var merchantText by remember { mutableStateOf("") }
    var showCategoryPanel by remember { mutableStateOf(false) }
    var showKeypad by remember { mutableStateOf(false) }
    var calcMode by remember { mutableStateOf(false) }
    var baseExpr by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(if (accounts.any { it.id == initialAccountId }) initialAccountId else accounts.firstOrNull()?.id ?: 0L) }
    var tagIds by remember { mutableStateOf(initialTagIds) }
    var discountText by remember { mutableStateOf("") }
    var memberId by remember { mutableStateOf<Long?>(null) }
    var cardBounds by remember { mutableStateOf<Rect?>(null) }
    var payStatus by remember { mutableStateOf(PaymentStatus.PAID) }
    var reimbStatus by remember { mutableStateOf(ReimbursementStatus.NONE) }
    var refundStatus by remember { mutableStateOf(RefundStatus.NONE) }
    var includeSum by remember { mutableStateOf(true) }
    var includeBudget by remember { mutableStateOf(true) }
    var activePicker by remember { mutableStateOf<String?>(null) }
    val children = categories.filter { it.parentId == activeParentId }
    val confirmId = if (selChildId != 0L && categories.any { it.id == selChildId }) selChildId else activeParentId
    val selectedCat = categories.firstOrNull { it.id == confirmId }
    Box(modifier = Modifier.fillMaxSize().pointerInput(cardBounds) {
        detectTapGestures { offset ->
            val r = cardBounds
            if (r == null || !r.contains(offset)) onCollapse()
        }
    }) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000)))
        Box(modifier = Modifier.fillMaxWidth().padding(top = 120.dp, bottom = 40.dp).padding(horizontal = 20.dp).heightIn(max = LocalConfiguration.current.screenHeightDp.dp - 160.dp).verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 10.dp, modifier = Modifier.fillMaxWidth().onGloballyPositioned { cardBounds = it.boundsInWindow() }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isIncome) "收入" else "支出", style = MaterialTheme.typography.labelLarge, color = if (isIncome) incomeGreen else expenseRed)
                        Spacer(Modifier.weight(1f))
                        Text("${fmtDateTime(c.tradeTime)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        IconButtonSmall(onClick = onCollapse) { Icon(Icons.Filled.Close, contentDescription = "关闭", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { showCategoryPanel = !showCategoryPanel; if (showCategoryPanel) showKeypad = false }.padding(vertical = 4.dp)) {
                            if (selectedCat != null) {
                                CategoryIcon(selectedCat.icon, size = 34.dp, fontSize = 18.sp, selected = true)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(selectedCat.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            } else {
                                Text("选择分类", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.width(4.dp))
                            Icon(if (showCategoryPanel) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("¥$amountText", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if (isIncome) incomeGreen else expenseRed, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.clickable { showKeypad = !showKeypad; if (showKeypad) showCategoryPanel = false }.padding(vertical = 4.dp))
                    }
                    if (showCategoryPanel && !showKeypad) {
                        Spacer(Modifier.height(4.dp))
                        CategoryGrid(parents = parents, children = children, activeParentId = activeParentId, selChildId = selChildId, onParent = { activeParentId = it; selChildId = 0L }, onChild = { selChildId = it }, onChildConfirm = { selChildId = it; showCategoryPanel = false })
                        Spacer(Modifier.height(8.dp))
                    }
                    if (showKeypad) {
                        NumericKeypad(
                            onKey = { k -> if (k.isNotEmpty()) amountText = KeypadCalculator.digest(amountText, k) },
                            onAction = { a ->
                                when (a) {
                                    KeypadAction.DELETE -> amountText = KeypadCalculator.deleteLast(amountText)
                                    KeypadAction.APPLY, KeypadAction.SAVE, KeypadAction.SAVE_AND_CONTINUE -> showKeypad = false
                                    KeypadAction.TOGGLE_CALCULATOR -> { baseExpr = amountText; calcMode = true }
                                }
                            },
                            isCalculatorMode = calcMode,
                            displayText = amountText.ifBlank { "0.00" },
                            secondaryLabel = "完成",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    } else {
                        OutlinedTextField(value = noteText, onValueChange = { noteText = it }, label = { Text("备注") }, singleLine = true, leadingIcon = { Icon(Icons.Filled.Receipt, contentDescription = null) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            val accName = accounts.firstOrNull { it.id == accountId }?.name ?: "选择账户"
                            OverlayChip(accName) { activePicker = if (activePicker == "account") null else "account" }
                            val tagLabel = if (tagIds.isEmpty()) "无标签" else tagIds.mapNotNull { id -> tags.firstOrNull { it.id == id }?.name }.joinToString(" ")
                            OverlayChip(tagLabel) { activePicker = if (activePicker == "tag") null else "tag" }
                            OverlayChip(merchantText.ifBlank { "无商家" }) { activePicker = if (activePicker == "merchant") null else "merchant" }
                            val memberLabel = if (memberId != null) members.firstOrNull { it.id == memberId }?.name ?: "自己" else "自己"
                            OverlayChip(memberLabel) { activePicker = if (activePicker == "member") null else "member" }
                            OverlayChip(if (discountText.isBlank()) "无优惠" else "优惠¥$discountText") { activePicker = if (activePicker == "discount") null else "discount" }
                            OverlayChip("CNY") {}
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            val payLabel = if (isIncome) (if (payStatus == PaymentStatus.UNPAID) "应收款" else "已收款") else (if (payStatus == PaymentStatus.UNPAID) "应付款" else "已付款")
                            OverlayChip(payLabel) { payStatus = if (payStatus == PaymentStatus.UNPAID) PaymentStatus.PAID else PaymentStatus.UNPAID }
                            if (!isIncome) {
                                val reimbLabel = when (reimbStatus) { ReimbursementStatus.REIMBURSABLE -> "待报销"; ReimbursementStatus.REIMBURSED -> "已报销"; else -> "非报销" }
                                OverlayChip(reimbLabel) { reimbStatus = if (reimbStatus == ReimbursementStatus.REIMBURSABLE) ReimbursementStatus.NONE else ReimbursementStatus.REIMBURSABLE }
                            }
                            val refundLabel = if (refundStatus == RefundStatus.HAS_REFUND) "有退款" else "无退款"
                            OverlayChip(refundLabel) { refundStatus = if (refundStatus == RefundStatus.HAS_REFUND) RefundStatus.NONE else RefundStatus.HAS_REFUND }
                            val incLabel = when { includeSum && includeBudget -> "计入收支和预算"; !includeSum && !includeBudget -> "不计入收支和预算"; !includeSum -> "不计入收支"; else -> "不计入预算" }
                            OverlayChip(incLabel) { val all = !(includeSum && includeBudget); includeSum = all; includeBudget = all }
                        }
                        Spacer(Modifier.height(4.dp))
                        if (activePicker == "account") {
                            PickerPanel(title = "选择账户") {
                                accounts.forEach { acc ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { accountId = acc.id; activePicker = null }.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                        Text(acc.name, style = MaterialTheme.typography.bodyLarge, fontWeight = if (acc.id == accountId) FontWeight.Bold else FontWeight.Normal, color = if (acc.id == accountId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
                                        if (acc.id == accountId) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        if (activePicker == "tag") {
                            PickerPanel(title = "选择标签") {
                                tags.forEach { t ->
                                    val sel = tagIds.contains(t.id)
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { tagIds = if (sel) tagIds - t.id else tagIds + t.id }.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                        Text("#${t.name}", style = MaterialTheme.typography.bodyLarge, color = Color(t.color.toInt()), fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                                        if (sel) Icon(Icons.Filled.Check, contentDescription = null, tint = Color(t.color.toInt()), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        if (activePicker == "merchant") {
                            PickerPanel(title = "选择商家") {
                                merchants.forEach { m ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { merchantText = m.name; activePicker = null }.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                        Text(m.name, style = MaterialTheme.typography.bodyLarge, fontWeight = if (merchantText == m.name) FontWeight.Bold else FontWeight.Normal, color = if (merchantText == m.name) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
                                        if (merchantText == m.name) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        if (activePicker == "member") {
                            PickerPanel(title = "选择成员") {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { memberId = null; activePicker = null }.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                    Text("自己", style = MaterialTheme.typography.bodyLarge, fontWeight = if (memberId == null) FontWeight.Bold else FontWeight.Normal, color = if (memberId == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                    if (memberId == null) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                members.forEach { m ->
                                    val sel = m.id == memberId
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { memberId = m.id; activePicker = null }.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                        Text(m.name, style = MaterialTheme.typography.bodyLarge, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
                                        if (sel) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        if (activePicker == "discount") {
                            PickerPanel(title = "优惠金额") {
                                OutlinedTextField(value = discountText, onValueChange = { discountText = it }, label = { Text("优惠金额(元)") }, singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(12.dp)).clickable { onCollapse() }) {
                            Text("撤销", color = expenseRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).clickable(onClick = onUndo))
                            Spacer(Modifier.weight(1f))
                            Text("确认更新", color = if (confirmId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).clickable(enabled = confirmId != null, onClick = {
                                onConfirm(OverlayChanges(
                                    categoryId = confirmId ?: 0L,
                                    amountCents = centsOf(amountText),
                                    accountId = accountId,
                                    note = noteText,
                                    merchant = merchantText,
                                    paymentStatus = payStatus,
                                    reimbursementStatus = reimbStatus,
                                    refundStatus = refundStatus,
                                    includeInSummary = includeSum,
                                    includeInBudget = includeBudget,
                                    tagIds = tagIds,
                                    discount = centsOf(discountText),
                                    memberId = memberId
                                ))
                            }))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconButtonSmall(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(modifier = Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) { content() }
}
@Composable
private fun DragHandle(fullScreen: Boolean, onToggleFull: () -> Unit, onCollapse: () -> Unit, onDragDelta: (Float) -> Unit = {}, onDragEndHandle: (Float) -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxWidth().height(40.dp)
            .pointerInput(Unit) {
                val tracker = VelocityTracker()
                detectDragGestures(
                    onDragStart = { tracker.resetTracking() },
                    onDragCancel = {},
                    onDrag = { change, dragAmount ->
                        change.consume()
                        tracker.addPosition(change.uptimeMillis, change.position)
                        onDragDelta(dragAmount.y)
                    },
                    onDragEnd = { onDragEndHandle(tracker.calculateVelocity().y) }
                )
            }
            .clickable { onToggleFull() },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(width = 44.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.outlineVariant))
    }
}
@Composable
private fun CategoryGrid(parents: List<CategoryDomain>, children: List<CategoryDomain>, activeParentId: Long?, selChildId: Long, onParent: (Long) -> Unit, onChild: (Long) -> Unit, onChildConfirm: (Long) -> Unit) {
    if (parents.isEmpty()) {
        Text("暂无可选分类", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column {
        parents.chunked(5).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { p ->
                    val sel = p.id == activeParentId
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { onParent(p.id) }.padding(vertical = 8.dp)) {
                        CategoryIcon(p.icon, size = 26.dp, fontSize = 16.sp, selected = sel, unselectedTint = GRAY_999)
                        Spacer(Modifier.height(4.dp))
                        Text(p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }
            rowItems.forEach { p ->
                if (p.id == activeParentId && children.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF5F5F5)).padding(vertical = 8.dp)) {
                        children.chunked(5).forEach { subRow ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                subRow.forEach { child ->
                                    val s = child.id == selChildId
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).padding(vertical = 6.dp).pointerInput(child.id) { detectTapGestures(onDoubleTap = { onChildConfirm(child.id) }, onTap = { onChild(child.id) }) }) {
                                        CategoryIcon(child.icon, size = 24.dp, fontSize = 14.sp, selected = s, unselectedTint = GRAY_999)
                                        Spacer(Modifier.height(4.dp))
                                        Text(child.name, style = MaterialTheme.typography.bodySmall, fontWeight = if (s) FontWeight.Bold else FontWeight.Normal, color = if (s) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun PickerPanel(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F5F5)).padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        content()
    }
}
@Composable
private fun OverlayChip(text: String, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 9.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}
private fun fmtYuan(amount: Long): String = String.format(Locale.CHINA, "%.2f", amount / 100.0)
private fun centsOf(text: String): Long = (text.toDoubleOrNull() ?: 0.0).times(100).toLong()
private fun fmtDateTime(ts: Long): String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(ts))
