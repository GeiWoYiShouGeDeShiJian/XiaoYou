package com.example.jizhangruanjian.ui.record
import com.example.jizhangruanjian.ui.theme.SemanticColors
import com.example.jizhangruanjian.ui.theme.AppSpacing
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.TypeBadgeColors
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.RadioButton
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material3.SuggestionChip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import com.example.jizhangruanjian.core.parser.SpeechController
import com.example.jizhangruanjian.core.parser.TextParser
import com.example.jizhangruanjian.core.util.AmountCalculator
import com.example.jizhangruanjian.core.util.Formatters
import com.example.jizhangruanjian.core.util.ImageSaver
import com.example.jizhangruanjian.core.util.KeypadCalculator
import com.example.jizhangruanjian.core.util.KeywordMatcher
import com.example.jizhangruanjian.ui.components.NumericKeypad
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.components.SourceBadge
import com.example.jizhangruanjian.ui.components.KeypadAction
import com.example.jizhangruanjian.ui.components.AppButton
import com.example.jizhangruanjian.ui.components.AppButtonVariant
import com.example.jizhangruanjian.ui.components.grayscale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.example.jizhangruanjian.data.model.Transaction
import com.example.jizhangruanjian.data.model.RecordTemplate
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.Prefill
import com.example.jizhangruanjian.R
import com.example.jizhangruanjian.data.model.Ledger
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.model.PaymentStatus
import com.example.jizhangruanjian.data.model.ReimbursementStatus
import com.example.jizhangruanjian.data.model.RefundStatus
import com.example.jizhangruanjian.data.model.LoanDirection
import com.example.jizhangruanjian.data.model.TransactionSource
import com.example.jizhangruanjian.data.model.AccountType
import com.example.jizhangruanjian.domain.model.AccountDomain
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.domain.model.TransactionDomain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
private val incomeGreen = SemanticColors.IncomeGreen
private val expenseRed = SemanticColors.ExpenseRed
private val currencyOptions = listOf("CNY" to "人民币", "HKD" to "港币", "MOP" to "澳门元", "TWD" to "新台币", "USD" to "美元", "EUR" to "欧元", "GBP" to "英镑", "JPY" to "日元", "KRW" to "韩元")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    accounts: List<AccountDomain>,
    categories: List<CategoryDomain>,
    members: List<com.example.jizhangruanjian.data.model.Member>,
    tags: List<com.example.jizhangruanjian.data.model.Tag>,
    tagGroups: List<com.example.jizhangruanjian.data.model.TagGroup>,
    onSaveTag: (com.example.jizhangruanjian.data.model.Tag) -> Unit,
    onDeleteTag: (Long) -> Unit,
    onSaveTagGroup: (com.example.jizhangruanjian.data.model.TagGroup) -> Unit,
    merchants: List<com.example.jizhangruanjian.data.model.Merchant>,
    merchantGroups: List<com.example.jizhangruanjian.data.model.MerchantGroup>,
    onSaveMerchant: (com.example.jizhangruanjian.data.model.Merchant) -> Unit,
    onSaveMerchantGroup: (com.example.jizhangruanjian.data.model.MerchantGroup) -> Unit,
    onSaveMerchantOrder: (List<Long>) -> Unit,
    selectedType: TransactionType,
    editing: Transaction?,
    initialPrefill: Prefill?,
    onPrefillConsumed: () -> Unit,
    selectedImagePaths: List<String>,
    editingTagIds: List<Long>,
    currentLedgerName: String,
    ledgers: List<Ledger>,
    selectedLedgerId: Long,
    onSelectLedger: (Long) -> Unit,
    onCreateCategory: (String, Long?) -> Unit,
    onReorderCategories: (List<Long>) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onSave: (TransactionDomain, Long?, List<String>) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSaveFinished: () -> Unit,
    onCreateAccount: (String) -> Unit,
    onSaveMember: (com.example.jizhangruanjian.data.model.Member) -> Unit,
    onDeleteMember: (Long) -> Unit,
    onAddImage: (String) -> Unit,
    onRemoveImage: (String) -> Unit,
    onLoadImages: (Long) -> Unit,
    onClearImages: () -> Unit,
    onLoadTags: (Long) -> Unit,
    onClearTags: () -> Unit,
    onRememberKeyword: (Long, String) -> Unit
) {
    val context = LocalContext.current
    var amountExpr by remember { mutableStateOf(editing?.let { Formatters.yuanText(it.amount) } ?: "") }
    var note by remember { mutableStateOf(editing?.note ?: "") }
    var categoryId by remember { mutableStateOf(editing?.categoryId ?: 0L) }
    // 用户是否手动指定过分类（手动选/chips/面板/模板/备注联想）；没选过则默认小类一直跟随所选时间
    var catChosenManually by remember { mutableStateOf(editing != null) }
    val remarkVm: NoteSettingsViewModel = hiltViewModel()
    val remarkScope = rememberCoroutineScope()
    var accountId by remember { mutableStateOf(editing?.accountId ?: (accounts.firstOrNull()?.id ?: 0L)) }
    // 账户列表异步加载完成后兜底选中第一个（重装后首次打开时列表为空，accountId 停在 0 会导致保存被拦）
    LaunchedEffect(accounts) { if (accountId == 0L) accounts.firstOrNull()?.let { accountId = it.id } }
    var toAccountId by remember { mutableStateOf(editing?.toAccountId ?: 0L) }
    var tradeDate by remember { mutableStateOf(editing?.tradeDate ?: System.currentTimeMillis()) }
    var include by remember { mutableStateOf(editing?.includeInSummary ?: (selectedType != TransactionType.TRANSFER)) }
    var merchant by remember { mutableStateOf(editing?.merchant ?: "") }
    var prefillSource by remember { mutableStateOf(editing?.source ?: com.example.jizhangruanjian.data.model.TransactionSource.MANUAL) }
    var discountCents by remember { mutableStateOf(editing?.discount ?: 0L) }
    var currency by remember { mutableStateOf(editing?.currency ?: "CNY") }
    var paymentStatus by remember { mutableStateOf(editing?.paymentStatus) }
    var reimbursementStatus by remember { mutableStateOf(editing?.reimbursementStatus) }
    var refundStatus by remember { mutableStateOf(editing?.refundStatus) }
    var includeBudget by remember { mutableStateOf(editing?.includeInBudget ?: true) }
    var refundAmountExpr by remember { mutableStateOf(if ((editing?.refundAmount ?: 0L) > 0L) Formatters.yuanText(editing!!.refundAmount) else "") }
    var refundAccountId by remember { mutableStateOf(editing?.refundAccountId) }
    var refundDate by remember { mutableStateOf(editing?.refundDate ?: System.currentTimeMillis()) }
    var refundNote by remember { mutableStateOf(editing?.refundNote ?: "") }
    var lockRefundAccount by remember { mutableStateOf(false) }
    var memberSel by remember { mutableStateOf<List<Long?>>(editing?.memberIds?.split(",")?.mapNotNull { it.toLongOrNull() }?.ifEmpty { null }?.map { m -> m as Long? } ?: editing?.memberId?.let { listOf<Long?>(it) } ?: listOf(null)) }
    var loanDirection by remember { mutableStateOf(editing?.loanDirection ?: LoanDirection.IN) }
    var selectedTagIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showMemberDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showMerchantDialog by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    // false=优惠金额模式，true=原价模式；跨弹窗记住上次使用的模式
    var discountMode by remember { mutableStateOf(false) }
    var showFeeDialog by remember { mutableStateOf(false) }
    var transferFee by remember { mutableStateOf(editing?.fee ?: 0L) }
    var transferFeePayer by remember { mutableStateOf(editing?.feePayer ?: "SELF") }
    var showDueDateDialog by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf(editing?.dueDate) }
    var showCurrencyPage by remember { mutableStateOf(false) }
    var showRefundPage by remember { mutableStateOf(false) }
    var showRefundAccountDialog by remember { mutableStateOf(false) }
    var showRefundDateDialog by remember { mutableStateOf(false) }
    var showRefundTimeDialog by remember { mutableStateOf(false) }
    var showRefundNoteDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showLedgerDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showAccountAdd by remember { mutableStateOf(false) }
    var accountPickTarget by remember { mutableStateOf("from") }
    var noteSheet by remember { mutableStateOf(false) }
    var noteSettingsPage by remember { mutableStateOf(false) }
    var prefPage by remember { mutableStateOf(false) }
    var templateSheet by remember { mutableStateOf(false) }
    var templateEdit by remember { mutableStateOf<RecordTemplate?>(null) }
    val templateVm: RecordTemplateViewModel = hiltViewModel()
    val recordTemplates by templateVm.templates.collectAsStateWithLifecycle()
    val prefsVm: RecordPrefsViewModel = hiltViewModel()
    val freqVm: QuickCatViewModel = hiltViewModel()
    val arVm: com.example.jizhangruanjian.ui.autorecord.AutoRecordSettingsViewModel = hiltViewModel()
    val showFavCategory by arVm.showFavCategory.collectAsState()
    LaunchedEffect(arVm) { arVm.loadExtended() }
    val prefs by prefsVm.prefs.collectAsStateWithLifecycle()
    var calcMode by remember { mutableStateOf(prefs["rp_show_ops"] == "1") }
    var baseExpr by remember { mutableStateOf("") }
    var showKeypad by remember { mutableStateOf(editing == null) }
    var detached by remember(editing?.id) { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        uri?.let { runCatching { ImageSaver.saveImage(context, it, imageQuality(prefs)) }.onSuccess { path -> onAddImage(path); if (prefs["rp_del_album"] == "1") deleteAlbumImage(context, it) } }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) cameraUri?.let { runCatching { ImageSaver.saveImage(context, it, imageQuality(prefs)) }.onSuccess { path -> onAddImage(path) } }
        cameraUri = null
    }
    LaunchedEffect(editing?.id) {
        if (editing != null) onLoadImages(editing!!.id) else onClearImages()
        if (editing != null) onLoadTags(editing!!.id) else onClearTags()
    }
    LaunchedEffect(editingTagIds) { selectedTagIds = editingTagIds }
    // 切换类型时重置扩展属性默认值（支出已付款/非报销/无退款，收入已收款/无退款）
    LaunchedEffect(selectedType) {
        if (editing == null) {
            paymentStatus = if (selectedType == TransactionType.EXPENSE || selectedType == TransactionType.INCOME) PaymentStatus.PAID else null
            reimbursementStatus = if (selectedType == TransactionType.EXPENSE) ReimbursementStatus.NONE else null
            refundStatus = if (selectedType == TransactionType.EXPENSE || selectedType == TransactionType.INCOME) RefundStatus.NONE else null
            includeBudget = true
            include = selectedType != TransactionType.TRANSFER
        }
    }
    // 打开时自动选中默认分类：按时段的餐饮小类(早5:01-11/午11:01-16/晚16:01-21/夜21:01-5) → 兜底
    val expenseParents = categories.filter { it.parentId == null }
    LaunchedEffect(selectedType, categories, tradeDate, editing?.id) {
        if (editing == null && !catChosenManually) {
            val t = java.time.Instant.ofEpochMilli(tradeDate).atZone(java.time.ZoneId.systemDefault()); val m = t.hour * 60 + t.minute
            // 时段 → 候选小类名（按顺序匹配，夜宵缺省时归晚餐）
            val slotNames = when {
                m in 301..660 -> listOf("早餐"); m in 661..960 -> listOf("午餐"); m in 961..1260 -> listOf("晚餐"); else -> listOf("夜宵", "晚餐")
            }
            val diningParent = categories.firstOrNull { it.parentId == null && it.name == "餐饮" }
            val timeDef = slotNames.firstNotNullOfOrNull { slotName -> categories.firstOrNull { it.parentId == diningParent?.id && it.name == slotName }?.id } ?: 0L
            // 兜底也选小类：第一个大类下的第一个小类
            val fallbackDef = categories.firstOrNull { it.parentId == expenseParents.firstOrNull()?.id }?.id ?: 0L
            val def = timeDef.takeIf { it != 0L } ?: fallbackDef.takeIf { it != 0L } ?: expenseParents.firstOrNull()?.id ?: 0L
            if (def != 0L) categoryId = def
        }
    }
    // T10 URL Scheme 预填：一次性填充表单（待账户/分类列表加载后重跑匹配）
    LaunchedEffect(initialPrefill, accounts, categories) {
        val p = initialPrefill ?: return@LaunchedEffect
        val acc = accounts.firstOrNull { it.name == p.account }
        val typeCat = categories.firstOrNull { it.type.name == p.type.name && it.name == p.category }
        amountExpr = com.example.jizhangruanjian.core.util.Formatters.yuanText(p.amountCents)
        if (p.note?.isNotBlank() == true) note = p.note
        if (p.merchant?.isNotBlank() == true) merchant = p.merchant
        prefillSource = p.source
        if (acc != null) accountId = acc.id
        if (typeCat != null) categoryId = typeCat.id
        if (acc != null || p.account == null) onPrefillConsumed()
    }
    var speech: SpeechController? by remember { mutableStateOf(null) }
    DisposableEffect(Unit) { onDispose { speech?.destroy() } }
    fun applySpeech(text: String) {
        val parsed = TextParser.parse(text, categories.filter { it.type.name == selectedType.name })
        parsed.amountCents?.let { amountExpr = Formatters.yuanText(it) }
        if (parsed.note.isNotBlank()) note = parsed.note
        parsed.category?.let { categoryId = it.id }
        listening = false
    }
    lateinit var startSpeech: () -> Unit
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) startSpeech() }
    startSpeech = {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            val c = speech ?: SpeechController(context, onResult = { applySpeech(it) }, onError = { listening = false }).also { speech = it }
            listening = true; c.start()
        }
    }
    LaunchedEffect(note) {
        if (categoryId == 0L) KeywordMatcher.match(note, categories.filter { it.type.name == selectedType.name })?.let { categoryId = it.id; catChosenManually = true }
    }
    val fallbackCategoryId = categories.firstOrNull()?.id ?: 0L
    fun doSave(close: Boolean) {
        val cents = if (amountExpr.isBlank()) 0L else KeypadCalculator.toCents(amountExpr)
        val isTransfer = selectedType == TransactionType.TRANSFER
        val isLoan = selectedType == TransactionType.LOAN
        if (cents < 0L || accountId == 0L) { Toast.makeText(context, if (accountId == 0L) "请先选择付款账户" else "金额格式有误", Toast.LENGTH_SHORT).show(); return }
        if (!isTransfer && !isLoan && categoryId == 0L) { Toast.makeText(context, "请先选择分类", Toast.LENGTH_SHORT).show(); return }
        if (isTransfer && (toAccountId == 0L || toAccountId == accountId)) { Toast.makeText(context, "请选择与转出不同的转入账户", Toast.LENGTH_SHORT).show(); return }
        val saveId = if (detached) 0L else editing?.id
        onSave(TransactionDomain(id = saveId ?: 0L, ledgerId = editing?.ledgerId ?: selectedLedgerId, accountId = accountId, toAccountId = if (isTransfer) toAccountId else null, categoryId = if (isTransfer || isLoan) fallbackCategoryId else categoryId, type = selectedType, amount = cents, note = note, tradeDate = tradeDate, includeInSummary = include, merchant = merchant.ifBlank { null }, currency = currency, discount = if (isTransfer || isLoan) 0L else discountCents, fee = if (isTransfer) transferFee else 0L, feePayer = if (isTransfer) transferFeePayer.takeIf { transferFee != 0L } else null, dueDate = if (isLoan) dueDate else null, paymentStatus = paymentStatus, reimbursementStatus = reimbursementStatus, refundStatus = refundStatus, includeInBudget = includeBudget, refundAmount = if (refundStatus == RefundStatus.HAS_REFUND) (KeypadCalculator.toCents(refundAmountExpr).coerceAtLeast(0L)) else 0L, refundAccountId = if (refundStatus == RefundStatus.HAS_REFUND) (refundAccountId ?: accountId) else null, refundDate = if (refundStatus == RefundStatus.HAS_REFUND) refundDate else null, refundNote = if (refundStatus == RefundStatus.HAS_REFUND) refundNote.ifBlank { null } else null, memberId = memberSel.firstOrNull(), memberIds = memberSel.filterNotNull(), loanDirection = loanDirection.takeIf { isLoan }, tagIds = selectedTagIds, source = prefillSource), saveId, selectedImagePaths)
        if (!isTransfer && !isLoan) remarkScope.launch { remarkVm.rememberNoteCategory(selectedType, note, categoryId, tradeDate) }
        if (close) onSaveFinished() else amountExpr = ""
    }
    val selectedCat = categories.firstOrNull { it.id == categoryId }
    // 快捷小类：共三排，第一排固定时段餐饮四类，其余按当前账本使用频率排序
    var freqMap by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }
    LaunchedEffect(selectedType) { freqMap = freqVm.counts(selectedType.name) }
    val quickCats: List<CategoryDomain> = if (selectedType == TransactionType.TRANSFER || selectedType == TransactionType.LOAN) emptyList() else {
        val pool0 = categories.filter { it.type == CategoryType.valueOf(selectedType.name) }
        val pool = pool0.filter { it.parentId != null }.ifEmpty { pool0 }
        val fixed = if (selectedType == TransactionType.EXPENSE) listOf("早餐", "午餐", "晚餐", "夜宵").mapNotNull { n -> pool.firstOrNull { it.name == n } } else emptyList()
        val rest = pool.filter { c -> fixed.none { it.id == c.id } }.sortedByDescending { freqMap[it.id] ?: 0L }
        (fixed + rest).take(12)
    }
    BackHandler {
        when {
            templateEdit != null -> { templateEdit = null; templateSheet = true }
            prefPage -> prefPage = false
            noteSettingsPage -> noteSettingsPage = false
            showCurrencyPage -> showCurrencyPage = false
            showRefundPage -> showRefundPage = false
            showAccountAdd -> { showAccountAdd = false; showAccountDialog = true }
            else -> onDismiss()
        }
    }
    val currentAmount = AmountCalculator.evaluate(if (calcMode) baseExpr else amountExpr)?.let { String.format(java.util.Locale.CHINA, "%.2f", it) } ?: "0.00"
    val amountColor = when { selectedType == TransactionType.EXPENSE && reimbursementStatus == ReimbursementStatus.REIMBURSABLE -> MaterialTheme.colorScheme.onSurface; selectedType == TransactionType.EXPENSE -> expenseRed; selectedType == TransactionType.INCOME -> incomeGreen; else -> MaterialTheme.colorScheme.onSurface }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs)) {
            IconButton(onClick = onDismiss) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clip(MaterialTheme.shapes.extraLarge).clickable { showLedgerDialog = true }.padding(horizontal = AppSpacing.sm, vertical = 6.dp)) {
                Text(currentLedgerName.ifBlank { "选择账本" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "切换账本", modifier = Modifier.size(AppSize.iconSize))
            }
            if (editing != null && prefillSource != TransactionSource.MANUAL) SourceBadge(prefillSource)
            IconButton(onClick = { templateSheet = true }) { Icon(painterResource(R.drawable.ic_template_bookmark), contentDescription = "快捷模板", tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = { prefPage = true }) { Icon(Icons.Filled.Settings, contentDescription = "记账设置") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg)) {
            listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER, TransactionType.LOAN).forEach { t ->
                TypeTab(Modifier.weight(1f), t, selectedType, onTypeChange)
            }
        }
        Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = AppSpacing.lg)) {
            Spacer(Modifier.height(AppSpacing.sm))
            // 分类行：仅图标+文字区域触发分类面板，其余留给金额
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(MaterialTheme.shapes.medium).clickable(enabled = selectedType == TransactionType.EXPENSE || selectedType == TransactionType.INCOME) { showCategoryDialog = true }.padding(vertical = AppSpacing.xs)) {
                    if (selectedType == TransactionType.EXPENSE || selectedType == TransactionType.INCOME) {
                        CategoryIcon(selectedCat?.icon ?: "❓", size = 40.dp, fontSize = 20.sp, selected = true, modifier = if (reimbursementStatus == ReimbursementStatus.REIMBURSABLE) Modifier.grayscale() else Modifier, fallbackContainer = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(Modifier.width(10.dp))
                        Text(selectedCat?.name ?: "选择分类", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(" ›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if (selectedType == TransactionType.TRANSFER) TypeBadgeColors.transfer else TypeBadgeColors.expense), contentAlignment = Alignment.Center) {
                            Text(if (selectedType == TransactionType.TRANSFER) "🔄" else "💰", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(if (selectedType == TransactionType.TRANSFER) "账户互转" else if (loanDirection == LoanDirection.IN) "借入" else "借出", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(" ›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(currentAmount, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = amountColor, modifier = Modifier.clickable(enabled = editing != null) { showKeypad = !showKeypad })
            }
            Spacer(Modifier.height(AppSpacing.sm))
            // 分类 chips（4 列网格）/ 转账·借贷类型 chips
            when (selectedType) {
                TransactionType.TRANSFER -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        TextChip("账户互转", true, Modifier.weight(1f)) { }
                    }
                }
                TransactionType.LOAN -> {
                    listOf("借入" to LoanDirection.IN, "借出" to LoanDirection.OUT, "还款" to LoanDirection.OUT, "收款" to LoanDirection.IN).chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            row.forEach { (label, dir) ->
                                TextChip(label, loanDirection == dir && label in listOf("借入", "借出"), Modifier.weight(1f)) { loanDirection = dir }
                            }
                        }
                    }
                }
                else -> if (showFavCategory) {
                    quickCats.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.padding(bottom = AppSpacing.sm)) {
                            row.forEach { c ->
                                TextChip(c.name, categoryId == c.id, Modifier.weight(1f)) { categoryId = c.id; catChosenManually = true }
                            }
                            repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(AppSpacing.sm))
            // 账户区
            when (selectedType) {
                TransactionType.TRANSFER -> {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        val from = accounts.firstOrNull { it.id == accountId }
                        val to = accounts.firstOrNull { it.id == toAccountId }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { accountPickTarget = "from"; showAccountDialog = true }) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(AppSpacing.xs))
                            Text(from?.name ?: "转出账户", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        }
                        IconButton(onClick = { val t = accountId; accountId = toAccountId; toAccountId = t }) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = "交换账户", tint = MaterialTheme.colorScheme.primary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { accountPickTarget = "to"; showAccountDialog = true }, horizontalArrangement = Arrangement.End) {
                            Text(to?.name ?: "转入账户", style = MaterialTheme.typography.bodyMedium, maxLines = 1, textAlign = TextAlign.End)
                            Spacer(Modifier.width(AppSpacing.xs))
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                TransactionType.LOAN -> {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        val member = memberSel.firstOrNull()?.let { mid -> members.firstOrNull { it.id == mid } }
                        Text(if (memberSel.size > 1) memberSel.mapNotNull { mid -> members.firstOrNull { it.id == mid }?.name }.joinToString("，") else (member?.name ?: "未选择"), style = MaterialTheme.typography.bodyMedium, color = if (member == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f).clickable { showMemberDialog = true })
                        Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(AppSpacing.md))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { accountPickTarget = "from"; showAccountDialog = true }) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(AppSpacing.xs))
                            Text(accounts.firstOrNull { it.id == accountId }?.name ?: "账户", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        }
                    }
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { accountPickTarget = "from"; showAccountDialog = true }) {
                        Text(if (selectedType == TransactionType.EXPENSE) "付款账户" else "收款账户", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(AppSpacing.xs))
                            Text(accounts.firstOrNull { it.id == accountId }?.name ?: "选择账户", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1)
                        }
                    }
                }
            }
            Spacer(Modifier.height(AppSpacing.sm))
            // 备注行 + 拍照（点击整行打开备注面板）
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { noteSheet = true }) {
                Text(note.ifEmpty { "备注..." }, style = MaterialTheme.typography.bodyLarge, color = if (note.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
                IconButton(onClick = { showImageSourceDialog = true }, enabled = selectedImagePaths.size < 4) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = "拍照/选图", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (selectedImagePaths.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    selectedImagePaths.forEach { p ->
                        Box {
                            ThumbnailImage(path = p, modifier = Modifier.size(64.dp).clip(CircleShape))
                            Box(modifier = Modifier.align(Alignment.TopEnd).size(AppSize.iconSize).background(MaterialTheme.colorScheme.error, CircleShape).clickable { onRemoveImage(p) }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Close, contentDescription = "删除", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                    if (selectedImagePaths.size < 4) {
                        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showImageSourceDialog = true }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = "添加", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(AppSpacing.sm))
            // 属性 chips 第 1 排：顺序/显隐由偏好「记账选项」控制
            val tagChipAnnotated = if (selectedTagIds.isEmpty()) null else buildAnnotatedString {
                selectedTagIds.mapNotNull { id -> tags.firstOrNull { it.id == id } }.forEachIndexed { index, t ->
                    if (index > 0) append(" ")
                    withStyle(SpanStyle(color = Color(t.color))) { append("#${t.name}") }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                (prefs["rp_chip_order"] ?: "date,time,member,tag,merchant,discount,fee,due,currency").split(",").forEach { key ->
                    if (key in (prefs["rp_chip_hide"] ?: "").split(",")) return@forEach
                    when (key) {
                        "date" -> TextChip(dateChipText(tradeDate), false) { showDateDialog = true }
                        "time" -> TextChip(formatTime(tradeDate), false) { showTimeDialog = true }
                        "member" -> if (selectedType == TransactionType.EXPENSE || selectedType == TransactionType.INCOME) {
                            TextChip(memberSel.joinToString("，") { mid -> if (mid == null) "自己" else (members.firstOrNull { m -> m.id == mid }?.name ?: "") }.ifEmpty { "自己" }, false) { showMemberDialog = true }
                        }
                        "tag" -> TextChip(if (selectedTagIds.isEmpty()) "无标签" else "", false, annotated = tagChipAnnotated) { showTagDialog = true }
                        "merchant" -> if (selectedType == TransactionType.EXPENSE || selectedType == TransactionType.INCOME) {
                            TextChip(merchant.ifBlank { "无商家" }, false, containerColor = if (merchant.isBlank()) null else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) { showMerchantDialog = true }
                        }
                        "discount" -> if (selectedType == TransactionType.EXPENSE) {
                            TextChip(if (discountCents > 0L) "优惠 ¥${Formatters.yuanText(discountCents)}" else "优惠", false) { showDiscountDialog = true }
                        }
                        "fee" -> if (selectedType == TransactionType.TRANSFER) {
                            TextChip(when { transferFee > 0L -> "手续费 ¥${Formatters.yuanText(transferFee)}"; transferFee < 0L -> "补贴 ¥${Formatters.yuanText(-transferFee)}"; else -> "无手续费/补贴" }, false, containerColor = if (transferFee != 0L) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else null) { if ((KeypadCalculator.toCents(amountExpr) ?: 0) > 0) showFeeDialog = true else Toast.makeText(context, "请先输入明细金额", Toast.LENGTH_SHORT).show() }
                        }
                        "due" -> if (selectedType == TransactionType.LOAN) {
                            TextChip(if (dueDate != null) "还款日 " + java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA).format(java.util.Date(dueDate!!)) else "还款日", false) { showDueDateDialog = true }
                        }
                        "currency" -> TextChip(currencyOptions.firstOrNull { it.first == currency }?.second ?: currency, false) { showCurrencyPage = true }
                    }
                }
            }
            // 属性 chips 第 2 排：收付款/报销/退款/计入收支与预算，顺序/显隐由「记账选项」控制
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                if (selectedType == TransactionType.EXPENSE || selectedType == TransactionType.INCOME) {
                    (prefs["rp_row2_order"] ?: "pay,reimburse,refund,budget").split(",").forEach { key ->
                        if (key in (prefs["rp_row2_hide"] ?: "").split(",")) return@forEach
                        when (key) {
                            "pay" -> TextChip(if (selectedType == TransactionType.EXPENSE) (if (paymentStatus == PaymentStatus.UNPAID) "应付款" else "已付款") else (if (paymentStatus == PaymentStatus.UNPAID) "应收款" else "已收款"), false, containerColor = if (paymentStatus == PaymentStatus.UNPAID) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else null) { paymentStatus = if (paymentStatus == PaymentStatus.UNPAID) PaymentStatus.PAID else PaymentStatus.UNPAID }
                            "reimburse" -> if (selectedType == TransactionType.EXPENSE) {
                                TextChip(when (reimbursementStatus) { ReimbursementStatus.REIMBURSABLE -> "待报销"; ReimbursementStatus.REIMBURSED -> "已报销"; else -> "非报销" }, false, containerColor = if (reimbursementStatus == ReimbursementStatus.REIMBURSABLE) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else null) { reimbursementStatus = if (reimbursementStatus == ReimbursementStatus.REIMBURSABLE) ReimbursementStatus.NONE else ReimbursementStatus.REIMBURSABLE }
                            }
                            "refund" -> TextChip(if (refundStatus == RefundStatus.HAS_REFUND) "有退款" else "无退款", false) { if (refundStatus != null) { if ((KeypadCalculator.toCents(amountExpr) ?: 0) > 0) showRefundPage = true else Toast.makeText(context, "请先输入明细金额", Toast.LENGTH_SHORT).show() } }
                            "budget" -> TextChip(when { include && includeBudget -> "计入收支和预算"; !include && !includeBudget -> "不计入收支和预算"; !include -> "不计入收支"; else -> "不计入预算" }, false, containerColor = if (!include || !includeBudget) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else null) { showStatsDialog = true }
                        }
                    }
                }
            }
        }
        if (editing != null && !showKeypad) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)) {
                if (!detached) {
                    Text("删除", style = MaterialTheme.typography.titleMedium, color = expenseRed, modifier = Modifier.clickable { onDelete(); onDismiss() }.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.md))
                    Spacer(Modifier.weight(1f))
                    Text("复制", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable {
                        val cents2 = if (amountExpr.isBlank()) 0L else KeypadCalculator.toCents(amountExpr)
                        val isT = selectedType == TransactionType.TRANSFER
                        onSave(TransactionDomain(id = 0L, ledgerId = editing.ledgerId, accountId = accountId, toAccountId = if (isT) toAccountId else null, categoryId = if (isT || selectedType == TransactionType.LOAN) fallbackCategoryId else categoryId, type = selectedType, amount = cents2, note = note, tradeDate = tradeDate, includeInSummary = include, merchant = merchant.ifBlank { null }, currency = currency, discount = discountCents, fee = if (isT) transferFee else 0L, feePayer = if (isT) transferFeePayer.takeIf { transferFee != 0L } else null, dueDate = if (selectedType == TransactionType.LOAN) dueDate else null, paymentStatus = paymentStatus, reimbursementStatus = reimbursementStatus, refundStatus = refundStatus, memberId = memberSel.firstOrNull(), memberIds = memberSel.filterNotNull(), loanDirection = loanDirection.takeIf { selectedType == TransactionType.LOAN }, tagIds = selectedTagIds), null, selectedImagePaths)
                        onSaveFinished()
                    }.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.md))
                    Spacer(Modifier.weight(1f))
                    Text("再记", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable {
                        val cents2 = if (amountExpr.isBlank()) 0L else KeypadCalculator.toCents(amountExpr)
                        val isT = selectedType == TransactionType.TRANSFER
                        onSave(TransactionDomain(id = 0L, ledgerId = editing.ledgerId, accountId = accountId, toAccountId = if (isT) toAccountId else null, categoryId = if (isT || selectedType == TransactionType.LOAN) fallbackCategoryId else categoryId, type = selectedType, amount = cents2, note = note, tradeDate = tradeDate, includeInSummary = include, merchant = merchant.ifBlank { null }, currency = currency, discount = discountCents, fee = if (isT) transferFee else 0L, feePayer = if (isT) transferFeePayer.takeIf { transferFee != 0L } else null, dueDate = if (selectedType == TransactionType.LOAN) dueDate else null, paymentStatus = paymentStatus, reimbursementStatus = reimbursementStatus, refundStatus = refundStatus, memberId = memberSel.firstOrNull(), memberIds = memberSel.filterNotNull(), loanDirection = loanDirection.takeIf { selectedType == TransactionType.LOAN }, tagIds = selectedTagIds), null, selectedImagePaths)
                        detached = true
                        amountExpr = ""
                        tradeDate = System.currentTimeMillis()
                    }.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.md))
                    Spacer(Modifier.weight(1f))
                } else {
                    Text("已保存为新记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = AppSpacing.sm))
                    Spacer(Modifier.weight(1f))
                }
                AppButton(text = "保存", onClick = { doSave(true) }, variant = AppButtonVariant.Primary, modifier = Modifier.weight(1.2f))
            }
        } else {
            NumericKeypad(onKey = { k -> if (k.isNotEmpty()) amountExpr = KeypadCalculator.digest(amountExpr, k) }, onAction = { a ->
                when (a) {
                    KeypadAction.DELETE -> amountExpr = KeypadCalculator.deleteLast(amountExpr)
                    KeypadAction.SAVE -> doSave(true)
                    KeypadAction.SAVE_AND_CONTINUE -> doSave(false)
                    KeypadAction.TOGGLE_CALCULATOR -> if (!calcMode) { baseExpr = amountExpr; calcMode = true } else { amountExpr = baseExpr; calcMode = false }
                    KeypadAction.APPLY -> { KeypadCalculator.toCents(amountExpr).takeIf { it > 0L }?.let { amountExpr = Formatters.yuanText(it) }; calcMode = false }
                }
            }, isCalculatorMode = calcMode, displayText = amountExpr.ifBlank { "0.00" }, modifier = Modifier.fillMaxWidth().padding(AppSpacing.md), hapticEnabled = prefs["rp_haptic"] != "0", reversed = prefs["rp_reverse"] == "1", keySize = keypadKeySize(prefs))
        }
    }
    if (showLedgerDialog) {
        var ledgerSearch by remember { mutableStateOf("") }
        var ledgerSearchVisible by remember { mutableStateOf(false) }
        ModalBottomSheet(onDismissRequest = { showLedgerDialog = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), dragHandle = null, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(Modifier.fillMaxWidth().padding(bottom = AppSpacing.xxl)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs)) {
                    IconButton(onClick = { showLedgerDialog = false }) { Icon(Icons.Filled.ArrowBack, contentDescription = "关闭") }
                    Text("账本", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { ledgerSearchVisible = !ledgerSearchVisible; ledgerSearch = "" }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
                    IconButton(onClick = { showLedgerDialog = false; Toast.makeText(context, "请在『我的-账本管理』中添加账本", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Filled.AddCircle, contentDescription = "添加账本") }
                    IconButton(onClick = { showLedgerDialog = false; Toast.makeText(context, "请在『我的-账本管理』中维护账本", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Filled.Settings, contentDescription = "账本设置") }
                }
                if (ledgerSearchVisible) {
                    OutlinedTextField(value = ledgerSearch, onValueChange = { ledgerSearch = it }, placeholder = { Text("搜索账本") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg))
                    Spacer(Modifier.height(AppSpacing.xs))
                }
                ledgers.filter { ledgerSearch.isBlank() || it.name.contains(ledgerSearch) }.forEach { l ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onSelectLedger(l.id); showLedgerDialog = false }.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.md)) {
                        Box(Modifier.size(width = 46.dp, height = 30.dp).clip(MaterialTheme.shapes.small).background(Color(l.color)))
                        Spacer(Modifier.width(14.dp))
                        Text(l.name, style = MaterialTheme.typography.bodyLarge, fontWeight = if (l.id == selectedLedgerId) FontWeight.Bold else FontWeight.Normal, color = if (l.id == selectedLedgerId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        if (l.id == selectedLedgerId) Icon(Icons.Filled.Check, contentDescription = "当前账本", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppSize.iconSize))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showLedgerDialog = false; Toast.makeText(context, "请在『我的-账本管理』中添加账本", Toast.LENGTH_SHORT).show() }.padding(horizontal = AppSpacing.xl, vertical = 14.dp)) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "添加账本", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppSize.iconLarge))
                    Spacer(Modifier.width(14.dp))
                    Text("添加账本", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
    if (showFeeDialog) {
        var feeTab by remember { mutableStateOf(false) }
        var feeInput by remember { mutableStateOf("") }
        var feeByRate by remember { mutableStateOf(false) }
        var feePayerSel by remember { mutableStateOf(transferFeePayer) }
        val base = KeypadCalculator.toCents(amountExpr).coerceAtLeast(0L)
        val feePreview = run {
            val v = feeInput.toDoubleOrNull()
            when {
                v == null || v <= 0.0 -> 0L
                feeByRate -> (base * v / 100.0).toLong()
                else -> (v * 100).toLong()
            }
        }
        val yuan = Formatters.yuanText(base)
        val feeText = Formatters.yuanText(feePreview)
        AlertDialog(onDismissRequest = { showFeeDialog = false }, title = {
            Row(Modifier.fillMaxWidth()) {
                listOf("手续费" to false, "补贴" to true).forEach { (label, tab) ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = if (feeTab == tab) FontWeight.Bold else FontWeight.Normal, color = if (feeTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { feeTab = tab; feeInput = "" }.padding(vertical = AppSpacing.sm))
                        Box(Modifier.fillMaxWidth().height(2.dp).background(if (feeTab == tab) MaterialTheme.colorScheme.primary else Color.Transparent))
                    }
                }
            }
        }, text = {
            Column {
                listOf("SELF", "OTHER").forEach { p ->
                    val line = when {
                        !feeTab && p == "SELF" -> "转出 ${Formatters.yuanText(base + feePreview)} = 到账 $yuan + 手续费 $feeText"
                        !feeTab -> "转出 $yuan = 到账 ${Formatters.yuanText((base - feePreview).coerceAtLeast(0L))} + 手续费 $feeText"
                        p == "SELF" -> "到账 $yuan = 转出 ${Formatters.yuanText((base - feePreview).coerceAtLeast(0L))} + 补贴 $feeText"
                        else -> "到账 ${Formatters.yuanText(base + feePreview)} = 转出 $yuan + 补贴 $feeText"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { feePayerSel = p }) {
                        RadioButton(selected = feePayerSel == p, onClick = { feePayerSel = p })
                        Text(line, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                OutlinedTextField(value = feeInput, onValueChange = { feeInput = it }, label = { Text(if (feeByRate) "请输入费率(%)" else "请输入金额") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    listOf("输入金额" to false, "输入费率" to true).forEach { (label, mode) ->
                        Box(Modifier.clip(MaterialTheme.shapes.large).background(if (feeByRate == mode) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant).clickable { feeByRate = mode; feeInput = "" }.padding(horizontal = 14.dp, vertical = AppSpacing.sm)) {
                            Text(label, style = MaterialTheme.typography.labelLarge, color = if (feeByRate == mode) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                transferFee = if (feeTab) -feePreview else feePreview
                transferFeePayer = feePayerSel
                showFeeDialog = false
            }) { Text("确定") }
        }, dismissButton = {
            Row {
                TextButton(onClick = { feeInput = "" }) { Text("切换") }
                TextButton(onClick = { showFeeDialog = false }) { Text("取消") }
            }
        })
    }
    if (showDueDateDialog) {
        val minDayKey = java.time.Instant.ofEpochMilli(tradeDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay()
        val dpState = rememberDatePickerState(initialSelectedDateMillis = dueDate ?: tradeDate, selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis / 86400000L >= minDayKey
        })
        DatePickerDialog(onDismissRequest = { showDueDateDialog = false }, confirmButton = { TextButton(onClick = { dpState.selectedDateMillis?.let { dueDate = it }; showDueDateDialog = false }) { Text("确定") } }, dismissButton = { TextButton(onClick = { showDueDateDialog = false }) { Text("取消") } }) { DatePicker(state = dpState) }
    }
    if (showImageSourceDialog) {
        ModalBottomSheet(onDismissRequest = { showImageSourceDialog = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), dragHandle = null, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(Modifier.fillMaxWidth().padding(bottom = AppSpacing.lg)) {
                Text("拍照", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clickable {
                    showImageSourceDialog = false
                    runCatching {
                        val dir = java.io.File(context.cacheDir, "camera").apply { mkdirs() }
                        val file = java.io.File(dir, "${System.currentTimeMillis()}.jpg")
                        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                }.padding(horizontal = AppSpacing.xxl, vertical = 18.dp))
                Text("从相册中选择", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clickable {
                    showImageSourceDialog = false
                    imagePicker.launch(android.content.Intent(android.content.Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
                }.padding(horizontal = AppSpacing.xxl, vertical = 18.dp))
            }
        }
    }
    if (showCategoryDialog) {
        val allParents = categories.filter { it.parentId == null }.sortedBy { it.sortOrder }
        var selectedParentId by remember { mutableStateOf(selectedCat?.parentId?.takeIf { it != 0L } ?: allParents.firstOrNull()?.id ?: 0L) }
        var searchQuery by remember { mutableStateOf("") }
        var searchVisible by remember { mutableStateOf(false) }
        var showAddDialog by remember { mutableStateOf(false) }
        var addParentMode by remember { mutableStateOf(true) }
        var newCatName by remember { mutableStateOf("") }
        var parentOrder by remember(categories) { mutableStateOf(allParents) }
        var childOrder by remember(categories, selectedParentId) { mutableStateOf(categories.filter { it.parentId == selectedParentId }.sortedBy { it.sortOrder }) }
        val shownParents = if (searchQuery.isBlank()) parentOrder else parentOrder.filter { it.name.contains(searchQuery) }
        val shownChildren = if (searchQuery.isBlank()) childOrder else childOrder.filter { it.name.contains(searchQuery) }
        ModalBottomSheet(onDismissRequest = { showCategoryDialog = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxWidth().height(560.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs)) {
                    IconButton(onClick = { showCategoryDialog = false }) { Icon(Icons.Filled.ArrowBack, contentDescription = "关闭") }
                    Text(if (selectedType == TransactionType.EXPENSE) "支出" else "收入", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { searchVisible = !searchVisible; if (!searchVisible) searchQuery = "" }) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
                    IconButton(onClick = { showAddDialog = true; addParentMode = selectedParentId == 0L }) { Icon(Icons.Filled.AddCircle, contentDescription = "添加分类") }
                    IconButton(onClick = { Toast.makeText(context, "请在抽屉-分类管理中维护分类", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Filled.Settings, contentDescription = "分类设置") }
                }
                if (searchVisible) {
                    OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("搜索分类") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg))
                    Spacer(Modifier.height(AppSpacing.xs))
                }
                Row(modifier = Modifier.weight(1f)) {
                    // 左列：大类（可拖动）
                    Box(modifier = Modifier.width(104.dp).fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                        val listState = rememberLazyListState()
                        val reorderState = rememberReorderableLazyColumnState(listState) { from, to ->
                            if (searchQuery.isBlank()) {
                                parentOrder = parentOrder.toMutableList().apply { add(to.index, removeAt(from.index)) }
                                onReorderCategories(parentOrder.map { it.id })
                            }
                        }
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            items(shownParents, key = { it.id }) { p ->
                                ReorderableItem(reorderState, key = p.id) { dragging ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(10.dp)).background(if (selectedParentId == p.id || dragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent).zIndex(if (dragging) 1f else 0f).longPressDraggableHandle().clickable { selectedParentId = p.id; searchQuery = ""; if (categories.none { it.parentId == p.id }) { categoryId = p.id; catChosenManually = true; showCategoryDialog = false } }) {
                                        Box(modifier = Modifier.width(3.dp).height(AppSpacing.xl).background(if (selectedParentId == p.id) MaterialTheme.colorScheme.primary else Color.Transparent))
                                        Text(p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selectedParentId == p.id) FontWeight.Bold else FontWeight.Normal, color = if (selectedParentId == p.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.padding(start = AppSpacing.sm))
                                    }
                                }
                            }
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(52.dp).clickable { addParentMode = true; newCatName = ""; showAddDialog = true }.padding(start = AppSpacing.md)) {
                                    Icon(Icons.Filled.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppSize.iconSize))
                                    Spacer(Modifier.width(AppSpacing.xs))
                                    Text("大类", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    // 右列：小类（可拖动）
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        if (shownChildren.isEmpty()) {
                            Text("暂无小类，点右上角 ⊕ 添加", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                        } else {
                            val listState = rememberLazyListState()
                            val reorderState = rememberReorderableLazyColumnState(listState) { from, to ->
                                if (searchQuery.isBlank()) {
                                    childOrder = childOrder.toMutableList().apply { add(to.index, removeAt(from.index)) }
                                    onReorderCategories(childOrder.map { it.id })
                                }
                            }
                            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                                items(shownChildren, key = { it.id }) { c ->
                                    ReorderableItem(reorderState, key = c.id) { dragging ->
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(10.dp)).background(if (dragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent).zIndex(if (dragging) 1f else 0f).longPressDraggableHandle().clickable { categoryId = c.id; catChosenManually = true; showCategoryDialog = false }.padding(start = AppSpacing.lg)) {
                                            CategoryIcon(c.icon, size = 34.dp, fontSize = 18.sp, selected = categoryId == c.id, fallbackContainer = SemanticColors.ExpenseRed.copy(alpha = 0.08f))
                                            Spacer(Modifier.width(AppSpacing.md))
                                            Text(c.name, style = MaterialTheme.typography.bodyLarge, color = if (categoryId == c.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontWeight = if (categoryId == c.id) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(if (addParentMode) "新建大类" else "新建小类") },
                text = { OutlinedTextField(value = newCatName, onValueChange = { newCatName = it }, label = { Text("分类名称") }, singleLine = true) },
                confirmButton = { TextButton(onClick = {
                    val parent = if (addParentMode) null else selectedParentId.takeIf { it != 0L }
                    onCreateCategory(newCatName, parent)
                    newCatName = ""; showAddDialog = false
                }) { Text("添加") } },
                dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("取消") } })
        }
    }
    if (showAccountDialog) {
        AccountPickerSheet(accounts = accounts, currentId = if (accountPickTarget == "to") toAccountId else accountId, onPick = { id -> if (accountPickTarget == "to") toAccountId = id else { accountId = id; if (selectedType == TransactionType.TRANSFER && toAccountId == id) toAccountId = accounts.firstOrNull { it.id != id }?.id ?: 0L }; showAccountDialog = false }, onDismiss = { showAccountDialog = false }, onAdd = { showAccountDialog = false; showAccountAdd = true })
    }
    if (showAccountAdd) {
        com.example.jizhangruanjian.ui.account.AddAccountScreen(onBack = { showAccountAdd = false; showAccountDialog = true })
    }
    val resolveCategory: (String) -> Unit = { raw ->
        remarkScope.launch {
            remarkVm.categoryForNote(raw, selectedType, tradeDate, categories)?.let { cid ->
                if (cid != 0L && cid != categoryId) { categoryId = cid; catChosenManually = true }
            }
        }
    }
    if (noteSheet) {
        NoteSheet(note = note, selectedType = selectedType, onDone = { note = it; noteSheet = false; resolveCategory(it) }, onDismiss = { noteSheet = false }, onOpenSettings = { noteSheet = false; noteSettingsPage = true }, onNoteChanged = { resolveCategory(it) })
    }
    if (prefPage) {
        RecordPreferenceScreen(onBack = { prefPage = false }, onOpenNoteSettings = { noteSettingsPage = true }, canRememberKeyword = categoryId != 0L && note.isNotBlank(), onRememberKeyword = { onRememberKeyword(categoryId, note); Toast.makeText(context, "已记忆关键词", Toast.LENGTH_SHORT).show() })
    }
    if (noteSettingsPage) {
        NoteSettingsScreen(onBack = { noteSettingsPage = false })
    }
    if (templateSheet) {
        TemplateSheet(
            templates = recordTemplates,
            iconOf = { id -> categories.firstOrNull { it.id == id }?.icon ?: "📒" },
            catNameOf = { id -> categories.firstOrNull { it.id == id }?.name ?: "" },
            accNameOf = { id -> accounts.firstOrNull { it.id == id }?.name ?: "" },
            onClose = { templateSheet = false },
            onAddNew = {
                templateSheet = false
                templateEdit = RecordTemplate(name = note.ifBlank { selectedCat?.name ?: "模板" }, type = selectedType.name, amountCents = if (amountExpr.isBlank()) 0L else KeypadCalculator.toCents(amountExpr), categoryId = categoryId, accountId = accountId, toAccountId = toAccountId, note = note, memberIds = memberSel.filterNotNull().joinToString(","), merchant = merchant, feeCents = transferFee, currency = currency, discountCents = discountCents, tagIds = selectedTagIds.joinToString(","), paymentUnpaid = paymentStatus == PaymentStatus.UNPAID, reimbursable = reimbursementStatus == ReimbursementStatus.REIMBURSABLE, includeBudget = includeBudget, includeSummary = include)
            },
            onApply = { tpl ->
                val ttype = TransactionType.valueOf(tpl.type)
                onTypeChange(ttype)
                if (tpl.quickSave && tpl.amountCents > 0L && editing == null) {
                    onSave(TransactionDomain(id = 0L, ledgerId = selectedLedgerId, accountId = tpl.accountId, toAccountId = tpl.toAccountId, categoryId = tpl.categoryId, type = ttype, amount = tpl.amountCents, note = tpl.note, tradeDate = System.currentTimeMillis(), includeInSummary = tpl.includeSummary, includeInBudget = tpl.includeBudget, merchant = tpl.merchant.ifBlank { null }, currency = tpl.currency, discount = if (ttype == TransactionType.EXPENSE) tpl.discountCents else 0L, paymentStatus = if (tpl.paymentUnpaid) PaymentStatus.UNPAID else null, reimbursementStatus = if (tpl.reimbursable) ReimbursementStatus.REIMBURSABLE else null, memberId = tpl.memberIds.split(",").firstNotNullOfOrNull { it.toLongOrNull() }, memberIds = tpl.memberIds.split(",").mapNotNull { it.toLongOrNull() }, tagIds = tpl.tagIds.split(",").mapNotNull { it.toLongOrNull() }), null, emptyList())
                    onSaveFinished()
                    templateSheet = false
                } else {
                    amountExpr = if (tpl.amountCents != 0L) Formatters.yuanText(tpl.amountCents) else ""
                    categoryId = tpl.categoryId
                    catChosenManually = true
                    accountId = tpl.accountId
                    toAccountId = tpl.toAccountId
                    note = tpl.note
                    merchant = tpl.merchant
                    currency = tpl.currency
                    discountCents = tpl.discountCents
                    transferFee = tpl.feeCents
                    selectedTagIds = tpl.tagIds.split(",").mapNotNull { it.toLongOrNull() }
                    paymentStatus = if (tpl.paymentUnpaid) PaymentStatus.UNPAID else null
                    reimbursementStatus = if (tpl.reimbursable) ReimbursementStatus.REIMBURSABLE else null
                    include = tpl.includeSummary
                    includeBudget = tpl.includeBudget
                    memberSel = tpl.memberIds.split(",").mapNotNull { it.toLongOrNull() }.ifEmpty { listOf<Long?>(null) }.map { m -> m as Long? }
                    templateSheet = false
                }
            },
            onDelete = { templateVm.delete(it) },
            onEdit = { templateSheet = false; templateEdit = it })
    }
    if (templateEdit != null) {
        TemplateEditScreen(
            initial = templateEdit!!,
            categories = categories,
            accounts = accounts,
            members = members,
            tags = tags,
            merchants = merchants,
            onSave = { tpl -> if (tpl.id == 0L) templateVm.add(tpl) else templateVm.update(tpl); templateEdit = null; templateSheet = true },
            onDelete = { templateVm.delete(it); templateEdit = null; templateSheet = true },
            onBack = { templateEdit = null; templateSheet = true })
    }
    if (showDateDialog) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = tradeDate)
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = { TextButton(onClick = {
                dpState.selectedDateMillis?.let {
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = tradeDate
                    val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
                    val m = cal.get(java.util.Calendar.MINUTE)
                    cal.timeInMillis = it
                    cal.set(java.util.Calendar.HOUR_OF_DAY, h)
                    cal.set(java.util.Calendar.MINUTE, m)
                    cal.set(java.util.Calendar.SECOND, 0)
                    tradeDate = cal.timeInMillis
                }
                showDateDialog = false
            }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showDateDialog = false }) { Text("取消") } }) { DatePicker(state = dpState) }
    }
    if (showTimeDialog) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = tradeDate }
        val tpState = rememberTimePickerState(initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY), initialMinute = cal.get(java.util.Calendar.MINUTE), is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("选择时间") },
            text = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = tpState) } },
            confirmButton = { TextButton(onClick = {
                val c = java.util.Calendar.getInstance().apply { timeInMillis = tradeDate }
                c.set(java.util.Calendar.HOUR_OF_DAY, tpState.hour)
                c.set(java.util.Calendar.MINUTE, tpState.minute)
                c.set(java.util.Calendar.SECOND, 0)
                tradeDate = c.timeInMillis
                showTimeDialog = false
            }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showTimeDialog = false }) { Text("取消") } }
        )
    }
    if (showMemberDialog) {
        var rolePage by remember { mutableStateOf(0) }
        BackHandler(enabled = rolePage > 0) { rolePage = 0 }
        var editTarget by remember { mutableStateOf<com.example.jizhangruanjian.data.model.Member?>(null) }
        when (rolePage) {
            1 -> RoleEditScreen(initial = editTarget, onSave = { onSaveMember(it) }, onClose = { rolePage = 0 })
            2 -> RoleManageScreen(members = members, onBack = { rolePage = 0 }, onAddNew = { editTarget = null; rolePage = 1 }, onEdit = { m -> editTarget = m; rolePage = 1 }, onDelete = { onDeleteMember(it.id) })
            else -> RolePickerScreen(members = members, selectedId = memberSel.firstOrNull(), selectedMembers = memberSel, onPick = { memberSel = listOf(it); showMemberDialog = false }, onClose = { showMemberDialog = false }, onAddNew = { editTarget = null; rolePage = 1 }, onManage = { rolePage = 2 }, onLivePick = { memberSel = it })
        }
    }
    if (showTagDialog) {
        var tagPage by remember { mutableStateOf(0) }
        BackHandler(enabled = tagPage > 0) { tagPage = 0 }
        var tagEditTarget by remember { mutableStateOf<com.example.jizhangruanjian.data.model.Tag?>(null) }
        when (tagPage) {
            1 -> TagEditScreen(initial = tagEditTarget, groups = tagGroups, onSave = { onSaveTag(it) }, onClose = { tagPage = 0 })
            2 -> TagManageScreen(groups = tagGroups, tags = tags, onBack = { tagPage = 0 }, onAddTag = { tagEditTarget = null; tagPage = 1 }, onAddGroup = { name -> onSaveTagGroup(com.example.jizhangruanjian.data.model.TagGroup(name = name)) }, onRenameGroup = { g, n -> onSaveTagGroup(g.copy(name = n)) }, onEditTag = { t -> tagEditTarget = t; tagPage = 1 }, onDeleteTag = { onDeleteTag(it) })
            else -> TagPickerScreen(tags = tags, groups = tagGroups, selectedIds = selectedTagIds, onSelectionChange = { selectedTagIds = it }, defaultMulti = prefs["rp_tag_multi"] == "1", onClose = { showTagDialog = false }, onAddTag = { tagEditTarget = null; tagPage = 1 }, onManage = { tagPage = 2 }, onSaveTagGroup = { onSaveTagGroup(it) })
        }
    }
    if (showMerchantDialog) {
        var merchantPage by remember { mutableStateOf(0) }
        BackHandler(enabled = merchantPage > 0) { merchantPage = 0 }
        var merchantEditTarget by remember { mutableStateOf<com.example.jizhangruanjian.data.model.Merchant?>(null) }
        when (merchantPage) {
            1 -> MerchantEditScreen(initial = merchantEditTarget, groups = merchantGroups, onSave = { onSaveMerchant(it) }, onClose = { merchantPage = 0 })
            2 -> MerchantManageScreen(groups = merchantGroups, merchants = merchants, onBack = { merchantPage = 0 }, onAddMerchant = { merchantEditTarget = null; merchantPage = 1 }, onAddGroup = { name -> onSaveMerchantGroup(com.example.jizhangruanjian.data.model.MerchantGroup(name = name)) }, onRenameGroup = { g, n -> onSaveMerchantGroup(g.copy(name = n)) }, onEditMerchant = { m -> merchantEditTarget = m; merchantPage = 1 }, onReorderMerchants = { onSaveMerchantOrder(it) })
            else -> MerchantPickerScreen(merchants = merchants, groups = merchantGroups, onClose = { showMerchantDialog = false }, onPick = { merchant = it; showMerchantDialog = false }, onUseNone = { merchant = ""; showMerchantDialog = false }, onAddMerchant = { merchantEditTarget = null; merchantPage = 1 }, onManage = { merchantPage = 2 }, onSaveMerchantGroup = { onSaveMerchantGroup(it) })
        }
    }
    if (showDiscountDialog) {
        // 当前金额视为实付；原价模式下原价低于实付时标红提示
        var discountInput by remember { mutableStateOf(if (!discountMode && discountCents > 0L) Formatters.yuanText(discountCents) else "") }
        val paidCents = if (amountExpr.isBlank()) 0L else KeypadCalculator.toCents(amountExpr).coerceAtLeast(0L)
        val inputCents = if (discountInput.isBlank()) 0L else KeypadCalculator.toCents(discountInput).coerceAtLeast(0L)
        val invalid = discountMode && inputCents in 1 until paidCents
        val hintText = if (inputCents > 0L) "优惠 ${yuanShort(if (discountMode) (inputCents - paidCents).coerceAtLeast(0L) else inputCents)} = 原价 ${yuanShort(if (discountMode) inputCents else inputCents + paidCents)} - 实付 ${yuanShort(paidCents)}" else "可在「首页-搜索-高级筛选」中选择「有优惠」查看所有优惠汇总金额"
        AlertDialog(
            onDismissRequest = { showDiscountDialog = false },
            title = { Text(if (discountMode) "原价" else "优惠") },
            text = {
                Box {
                    OutlinedTextField(value = discountInput, onValueChange = { discountInput = it }, isError = invalid, trailingIcon = { if (invalid) Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error) }, supportingText = { Text(if (invalid) "原价不能低于实付金额" else hintText, style = MaterialTheme.typography.bodySmall, color = if (invalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text(if (discountMode) "输入原价，自动计算优惠金额" else "请输入优惠金额", style = MaterialTheme.typography.bodySmall, color = if (invalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.TopStart).offset(x = AppSpacing.md, y = (-8).dp).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(horizontal = AppSpacing.xs))
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { discountMode = !discountMode; discountInput = "" }) { Text("切换") }
                    Spacer(Modifier.width(AppSpacing.md))
                    TextButton(onClick = { showDiscountDialog = false }) { Text("取消") }
                }
            },
            confirmButton = {
                TextButton(enabled = !invalid, onClick = {
                    val input = if (discountInput.isBlank()) 0L else KeypadCalculator.toCents(discountInput).coerceAtLeast(0L)
                    discountCents = if (discountMode) (input - paidCents).coerceAtLeast(0L) else input
                    showDiscountDialog = false
                }) { Text("确定") }
            })
    }
    if (showCurrencyPage) {
        CurrencyScreen(currentAmountText = amountExpr, currency = currency, onAmountChange = { amountExpr = it }, onCurrencyChange = { currency = it }, onBack = { showCurrencyPage = false })
    }
    if (showStatsDialog) {
        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = { Text("统计设置") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { include = !include }.padding(vertical = AppSpacing.sm)) {
                        Checkbox(checked = !include, onCheckedChange = { include = it != true })
                        Text("不计入收支", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = AppSpacing.sm))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { includeBudget = !includeBudget }.padding(vertical = AppSpacing.sm)) {
                        Checkbox(checked = !includeBudget, onCheckedChange = { includeBudget = it != true })
                        Text("不计入预算", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = AppSpacing.sm))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showStatsDialog = false }) { Text("关闭") } })
    }
    AnimatedVisibility(visible = showRefundPage, enter = slideInHorizontally { it } + fadeIn(), exit = slideOutHorizontally { it } + fadeOut()) {
        RefundPage(
            accounts = accounts, initialAmount = refundAmountExpr,
            maxCents = (if (amountExpr.isBlank()) 0L else KeypadCalculator.toCents(amountExpr)).coerceAtLeast(0L),
            currentAccountId = refundAccountId ?: accountId,
            onPickAccount = { refundAccountId = it },
            refundDate = refundDate,
            onPickDate = { refundDate = it },
            onPickTime = { refundDate = it },
            note = refundNote, onNoteChange = { refundNote = it },
            minDateMillis = tradeDate,
            lockAccount = lockRefundAccount, onLockChange = { lockRefundAccount = it },
            onOpenNoteSettings = { showRefundPage = false; noteSettingsPage = true },
            onConfirm = { refundCents ->
                refundAmountExpr = if (refundCents > 0L) Formatters.yuanText(refundCents) else ""
                refundStatus = if (refundCents > 0L) RefundStatus.HAS_REFUND else RefundStatus.NONE
                showRefundPage = false
            },
            onBack = { showRefundPage = false })
    }
}
@Composable
private fun TypeTab(modifier: Modifier, t: TransactionType, selectedType: TransactionType, onTypeChange: (TransactionType) -> Unit) {
    val selected = selectedType == t
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable { onTypeChange(t) }) {
        Text(when (t) { TransactionType.EXPENSE -> "支出"; TransactionType.INCOME -> "收入"; TransactionType.TRANSFER -> "转账"; TransactionType.LOAN -> "借贷" }, style = MaterialTheme.typography.titleMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(AppSpacing.xs))
        Spacer(Modifier.size(width = 48.dp, height = 3.dp).clip(RoundedCornerShape(2.dp)).background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent))
        Spacer(Modifier.height(2.dp))
    }
}
@Composable
private fun TextChip(text: String, selected: Boolean, modifier: Modifier = Modifier, annotated: AnnotatedString? = null, containerColor: Color? = null, onClick: () -> Unit) {
    Box(modifier = modifier.clip(MaterialTheme.shapes.extraLarge).background(containerColor ?: if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 9.dp), contentAlignment = Alignment.Center) {
        Text(annotated ?: AnnotatedString(text), style = MaterialTheme.typography.bodyMedium, color = if (annotated != null) Color.Unspecified else if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}
private fun formatDate(millis: Long): String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA).format(java.util.Date(millis))
private fun formatTime(millis: Long): String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.CHINA).format(java.util.Date(millis))
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountPickerSheet(accounts: List<AccountDomain>, currentId: Long, onPick: (Long) -> Unit, onDismiss: () -> Unit, onAdd: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    var search by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs)) {
            IconButton(onClick = onDismiss) { Icon(Icons.Filled.ArrowBack, "返回") }
            Text("账户", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            IconButton(onClick = { search = !search; if (!search) query = "" }) { Icon(Icons.Filled.Search, "搜索") }
            IconButton(onClick = onAdd) { Icon(Icons.Filled.AddCircle, "新建账户") }
            Box { IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "更多") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) { DropdownMenuItem(text = { Text("新建账户") }, onClick = { menu = false; onAdd() }) }
            }
        }
        if (search) {
            OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("搜索账户") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg))
        }
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) {
            FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("单选") }, modifier = Modifier.padding(end = AppSpacing.sm))
            FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("组合") })
        }
        if (tab == 1) {
            Text("组合账户暂不支持记账选择，请在「单选」模式下选择账户", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xxxl))
        } else {
            val filtered = accounts.filter { query.isBlank() || it.name.contains(query) }
            val groups = listOf(AccountType.CASH to "现金", AccountType.BANK to "储蓄卡", AccountType.WECHAT to "微信钱包", AccountType.ALIPAY to "支付宝", AccountType.OTHER to "其他")
            LazyColumn(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm).heightIn(max = 480.dp)) {
                groups.forEach { (type, label) ->
                    val list = filtered.filter { it.type == type }
                    if (list.isNotEmpty()) {
                        item(key = "g-${type.name}") {
                            var expand by remember { mutableStateOf(true) }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { expand = !expand }.padding(horizontal = AppSpacing.md, vertical = 10.dp)) {
                                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text(if (expand) "⌄" else "›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (!expand) return@item
                            list.forEach { a ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(if (a.id == currentId) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent).clickable { onPick(a.id) }.padding(horizontal = AppSpacing.md, vertical = AppSpacing.md)) {
                                    Box(Modifier.size(28.dp).background(Color(a.color), CircleShape), contentAlignment = Alignment.Center) { Text(typeEmoji(type), style = MaterialTheme.typography.bodySmall) }
                                    Spacer(Modifier.width(10.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(a.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                        if (a.note.isNotBlank()) { Spacer(Modifier.width(6.dp)); Text(a.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Text(Formatters.yuanText(a.balance), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = if (a.balance < 0) SemanticColors.ExpenseRed else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(AppSpacing.xxl)) }
            }
        }
    }
}
private fun typeEmoji(t: AccountType): String = when (t) { AccountType.CASH -> "💵"; AccountType.BANK -> "🏦"; AccountType.WECHAT -> "💬"; AccountType.ALIPAY -> "🅰️"; else -> "📁" }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteSheet(note: String, selectedType: com.example.jizhangruanjian.data.model.TransactionType, onDone: (String) -> Unit, onDismiss: () -> Unit, onOpenSettings: () -> Unit, onNoteChanged: (String) -> Unit = {}) {
    val vm: NoteSettingsViewModel = hiltViewModel()
    val text = remember { mutableStateOf(TextFieldValue(note, TextRange(note.length))) }
    val showHistory by vm.showHistory.collectAsStateWithLifecycle()
    val afterInput by vm.historyAfterInput.collectAsStateWithLifecycle()
    val showCommon by vm.showCommon.collectAsStateWithLifecycle()
    val common by vm.commonNotes.collectAsStateWithLifecycle()
    val history by vm.remarkHistory.collectAsStateWithLifecycle()
    val scopesMap by vm.commonNoteScopes.collectAsStateWithLifecycle()
    val defaultScopes = com.example.jizhangruanjian.data.model.TransactionType.entries.toSet()
    val focus = remember { FocusRequester() }
    val submitOrDismiss = { if (text.value.text.isNotBlank()) onDone(text.value.text.trim()) else onDismiss() }
    ModalBottomSheet(onDismissRequest = submitOrDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), dragHandle = null, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs)) {
            IconButton(onClick = submitOrDismiss) { Icon(Icons.Filled.ArrowBack, "返回") }
            Text("备注", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, "备注设置") }
        }
        val options = buildList {
            if (showCommon && text.value.text.isBlank()) addAll(common.filter { selectedType in (scopesMap[it] ?: defaultScopes) })
            if (showHistory && (!afterInput || text.value.text.isNotBlank())) addAll(history.filter { it.first == selectedType }.map { it.second }.filter { text.value.text.isBlank() || it.contains(text.value.text) }.filter { it !in this })
        }
        
        if (options.isNotEmpty()) LazyColumn(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md).heightIn(max = 200.dp).clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
            items(options.size) { i ->
                val c = options[i]
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onDone(c) }.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg)) {
                    Text(c, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)) {
            BasicTextField(value = text.value, onValueChange = { text.value = it; onNoteChanged(it.text) }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), modifier = Modifier.weight(1f).focusRequester(focus), decorationBox = { inner -> if (text.value.text.isEmpty()) Text("备注...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); inner() })
            Spacer(Modifier.width(AppSpacing.md))
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape).clickable { onDone(text.value.text.trim()) }, contentAlignment = Alignment.Center) { Icon(Icons.Filled.Check, "确定", tint = MaterialTheme.colorScheme.primary) }
        }
        Spacer(Modifier.height(AppSpacing.lg))
    }
    LaunchedEffect(Unit) { focus.requestFocus() }
}
private fun yuanShort(cents: Long): String = if (cents % 100L == 0L) (cents / 100L).toString() else Formatters.yuanText(cents)
// 自动删除相册原图：Android 11+ 需系统删除确认对话框，低版本直接删除
private fun deleteAlbumImage(context: android.content.Context, uri: android.net.Uri) {
    runCatching {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            val p = android.provider.MediaStore.createDeleteRequest(context.contentResolver, listOf(uri))
            (context as? android.app.Activity)?.startIntentSenderForResult(p.intentSender, 10021, null, 0, 0, 0)
        } else context.contentResolver.delete(uri, null, null)
    }
}
private fun dateChipText(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-M-d", java.util.Locale.CHINA)
    val today = sdf.format(java.util.Date())
    val target = sdf.format(java.util.Date(millis))
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    val dow = java.text.SimpleDateFormat("E", java.util.Locale.CHINA).format(cal.time)
    return when (target) {
        today -> "今天 $dow"
        sdf.format(java.util.Date(System.currentTimeMillis() - 86400000L)) -> "昨天 $dow"
        else -> "$target $dow"
    }
}
@Composable
private fun ThumbnailImage(path: String, modifier: Modifier) {
    val bmp by produceState<Bitmap?>(initialValue = null, key1 = path) { value = ImageSaver.loadBitmap(path, 128) }
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (bmp != null) Image(bitmap = bmp!!.asImageBitmap(), contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Text("图片", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
// 明细币种页：金额行（弹键盘）、币种行（弹选择）、确定按钮
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyScreen(currentAmountText: String, currency: String, onAmountChange: (String) -> Unit, onCurrencyChange: (String) -> Unit, onBack: () -> Unit) {
    var showKeypad by remember { mutableStateOf(false) }
    var showPick by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = AppSpacing.xs, top = AppSpacing.xs)) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") }
        Text("明细币种", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = AppSpacing.xl))
        Spacer(Modifier.height(AppSpacing.xxl))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showKeypad = true }.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg)) {
            Text("金额", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f))
            Text(currentAmountText.ifBlank { "0.00" }, style = MaterialTheme.typography.bodyLarge)
            Text(" ›", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showPick = true }.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg)) {
            Text("币种", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f))
            Text(currencyOptions.firstOrNull { it.first == currency }?.second ?: currency, style = MaterialTheme.typography.bodyLarge)
            Text(" ›", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(AppSpacing.xxl))
        AppButton(text = "确定", onClick = onBack, variant = AppButtonVariant.Primary, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xl))
    }
    if (showKeypad) {
        var draft by remember { mutableStateOf(currentAmountText) }
        val prefsVm: RecordPrefsViewModel = hiltViewModel()
        val prefs by prefsVm.prefs.collectAsStateWithLifecycle()
        var calcMode by remember { mutableStateOf(prefs["rp_show_ops"] == "1") }
        var baseExpr by remember { mutableStateOf("") }
        ModalBottomSheet(onDismissRequest = { showKeypad = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), dragHandle = null) {
            Column {
                Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) { Box(Modifier.width(40.dp).height(AppSpacing.xs).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) }
                Text(draft.ifBlank { "0.00" }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm), textAlign = TextAlign.End)
                NumericKeypad(onKey = { k -> if (k.isNotEmpty()) draft = KeypadCalculator.digest(draft, k) }, onAction = { a ->
                    when (a) {
                        KeypadAction.DELETE -> draft = KeypadCalculator.deleteLast(draft)
                        KeypadAction.SAVE, KeypadAction.SAVE_AND_CONTINUE -> { onAmountChange(draft); showKeypad = false }
                        KeypadAction.TOGGLE_CALCULATOR -> if (!calcMode) { baseExpr = draft; calcMode = true } else { draft = baseExpr; calcMode = false }
                        KeypadAction.APPLY -> { KeypadCalculator.toCents(draft).takeIf { it > 0L }?.let { draft = Formatters.yuanText(it) }; calcMode = false }
                    }
                }, isCalculatorMode = calcMode, displayText = draft.ifBlank { "0.00" }, modifier = Modifier.fillMaxWidth().padding(AppSpacing.md), hapticEnabled = prefs["rp_haptic"] != "0", reversed = prefs["rp_reverse"] == "1", keySize = keypadKeySize(prefs))
            }
        }
    }
    if (showPick) {
        val context = LocalContext.current
        AlertDialog(onDismissRequest = { showPick = false }, title = { Text("选择币种") }, text = {
            Column {
                currencyOptions.forEach { (code, name) ->
                    Text("$name $code", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clickable { onCurrencyChange(code); showPick = false }.padding(vertical = AppSpacing.md))
                }
            }
        }, dismissButton = { TextButton(onClick = { Toast.makeText(context, "币种管理暂未开放", Toast.LENGTH_SHORT).show() }) { Text("币种管理") } }, confirmButton = { TextButton(onClick = { showPick = false }) { Text("取消") } })
    }
}
// 退款页：金额≤记账金额，账户/日期/时间/备注可编辑，锁定账户只记忆不弹默认
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefundPage(accounts: List<AccountDomain>, initialAmount: String, maxCents: Long, minDateMillis: Long, currentAccountId: Long, onPickAccount: (Long) -> Unit, refundDate: Long, onPickDate: (Long) -> Unit, onPickTime: (Long) -> Unit, note: String, onNoteChange: (String) -> Unit, lockAccount: Boolean, onLockChange: (Boolean) -> Unit, onOpenNoteSettings: () -> Unit, onConfirm: (Long) -> Unit, onBack: () -> Unit) {
    var draft by remember { mutableStateOf(initialAmount) }
    val prefsVm: RecordPrefsViewModel = hiltViewModel()
    val prefs by prefsVm.prefs.collectAsStateWithLifecycle()
    var calcMode by remember { mutableStateOf(prefs["rp_show_ops"] == "1") }
    var baseExpr by remember { mutableStateOf("") }
    var showAccount by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var showNote by remember { mutableStateOf(false) }
    var showLockHelp by remember { mutableStateOf(false) }
    var toastHint by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toastHint) { toastHint?.let { delay(1500); toastHint = null } }
    val refundCents = if (draft.isBlank()) 0L else KeypadCalculator.toCents(draft).coerceAtLeast(0L)
    val overLimit = refundCents > maxCents
    val tradeKey = java.time.Instant.ofEpochMilli(minDateMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay()
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") }
            Text("退款", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) {
            Text("退款金额", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(draft.ifBlank { "0.00" }, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = if (overLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            if (overLimit) Text("退款金额不能超过明细金额", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = AppSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)).clickable { showAccount = true }.padding(horizontal = AppSpacing.lg, vertical = 14.dp)) {
                Text("转入账户", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                Text(accounts.firstOrNull { it.id == currentAccountId }?.name ?: "", style = MaterialTheme.typography.bodyLarge)
                Text(" ›", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(AppSpacing.sm))
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showDate = true }.padding(horizontal = AppSpacing.lg, vertical = 14.dp)) {
                    Text("退款日期", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.weight(1f))
                    Text(String.format(java.util.Locale.CHINA, "%tF", refundDate), style = MaterialTheme.typography.bodyLarge)
                    Text(" ›", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showTime = true }.padding(horizontal = AppSpacing.lg, vertical = 14.dp)) {
                    Text("退款时间", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.weight(1f))
                    Text(String.format(java.util.Locale.CHINA, "%tR", refundDate), style = MaterialTheme.typography.bodyLarge)
                    Text(" ›", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showNote = true }.padding(horizontal = AppSpacing.lg, vertical = 14.dp)) {
                    Text("备注", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.weight(1f))
                    Text(note, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(" ›", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm)) {
                Checkbox(checked = lockAccount, onCheckedChange = { onLockChange(it) })
                Text("锁定退款转入账户", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(AppSpacing.xs))
                Box(Modifier.size(18.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showLockHelp = true }, contentAlignment = Alignment.Center) {
                    Text("?", fontSize = 11.sp, lineHeight = 11.sp, style = LocalTextStyle.current.copy(platformStyle = PlatformTextStyle(includeFontPadding = false)), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (showLockHelp) {
            AlertDialog(onDismissRequest = { showLockHelp = false }, title = { Text("锁定退款账户") }, text = {
                Text("勾选后，后续所有退款都将固定转入【此次选择的账户】；不勾选则默认退回原账户。", style = MaterialTheme.typography.bodyMedium)
            }, confirmButton = { TextButton(onClick = { showLockHelp = false }) { Text("好的") } })
        }
        NumericKeypad(onKey = { k -> if (k.isNotEmpty()) draft = KeypadCalculator.digest(draft, k) }, onAction = { a ->
            when (a) {
                KeypadAction.DELETE -> draft = KeypadCalculator.deleteLast(draft)
                KeypadAction.SAVE -> when {
                    refundCents <= 0L -> toastHint = "请先输入明细金额"
                    overLimit -> toastHint = "退款金额不能超过明细金额"
                    else -> onConfirm(refundCents)
                }
                KeypadAction.SAVE_AND_CONTINUE -> draft = ""
                KeypadAction.TOGGLE_CALCULATOR -> if (!calcMode) { baseExpr = draft; calcMode = true } else { draft = baseExpr; calcMode = false }
                KeypadAction.APPLY -> { KeypadCalculator.toCents(draft).takeIf { it > 0L }?.let { draft = Formatters.yuanText(it) }; calcMode = false }
            }
        }, isCalculatorMode = calcMode, displayText = draft.ifBlank { "0.00" }, secondaryLabel = "清空", modifier = Modifier.fillMaxWidth().padding(AppSpacing.md), hapticEnabled = prefs["rp_haptic"] != "0", reversed = prefs["rp_reverse"] == "1", keySize = keypadKeySize(prefs))
    }
    toastHint?.let {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = Color.Black.copy(alpha = 0.75f), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 240.dp)) {
            Text(it, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = AppSpacing.xl, vertical = 10.dp))
        }
    }
    }
    if (showAccount) {
        AlertDialog(onDismissRequest = { showAccount = false }, title = { Text("转入账户") }, text = {
            Column {
                accounts.forEach { acc ->
                    Text(acc.name, style = MaterialTheme.typography.bodyLarge, color = if (acc.id == currentAccountId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().clickable { onPickAccount(acc.id); showAccount = false }.padding(vertical = AppSpacing.md))
                }
            }
        }, confirmButton = { TextButton(onClick = { showAccount = false }) { Text("取消") } })
    }
    if (showDate) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = refundDate, selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis / 86400000L >= tradeKey
        })
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = { TextButton(onClick = {
                dpState.selectedDateMillis?.let { picked ->
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = refundDate
                    val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
                    val m = cal.get(java.util.Calendar.MINUTE)
                    cal.timeInMillis = picked
                    cal.set(java.util.Calendar.HOUR_OF_DAY, h)
                    cal.set(java.util.Calendar.MINUTE, m)
                    cal.set(java.util.Calendar.SECOND, 0)
                    onPickDate(cal.timeInMillis)
                }
                showDate = false
            }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("取消") } }) { DatePicker(state = dpState) }
    }
    if (showTime) {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = refundDate
        val tpState = rememberTimePickerState(initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY), initialMinute = cal.get(java.util.Calendar.MINUTE), is24Hour = true)
        AlertDialog(onDismissRequest = { showTime = false }, title = { Text("选择时间") }, text = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = tpState) } }, confirmButton = { TextButton(onClick = {
            val c2 = java.util.Calendar.getInstance()
            c2.timeInMillis = refundDate
            c2.set(java.util.Calendar.HOUR_OF_DAY, tpState.hour)
            c2.set(java.util.Calendar.MINUTE, tpState.minute)
            c2.set(java.util.Calendar.SECOND, 0)
            onPickTime(c2.timeInMillis)
            showTime = false
        }) { Text("确定") } }, dismissButton = { TextButton(onClick = { showTime = false }) { Text("取消") } })
    }
    if (showNote) {
        val text = remember { mutableStateOf("") }
        val focus = remember { FocusRequester() }
        ModalBottomSheet(onDismissRequest = { showNote = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), dragHandle = null, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs)) {
                IconButton(onClick = { showNote = false }) { Icon(Icons.Filled.ArrowBack, "返回") }
                Text("备注", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = { showNote = false; onOpenNoteSettings() }) { Icon(Icons.Filled.Settings, "备注设置") }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)) {
                BasicTextField(value = text.value, onValueChange = { text.value = it }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), modifier = Modifier.weight(1f).focusRequester(focus), decorationBox = { inner -> if (text.value.isEmpty()) Text("备注...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); inner() })
                Spacer(Modifier.width(AppSpacing.md))
                Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape).clickable { onNoteChange(text.value.trim()); showNote = false }, contentAlignment = Alignment.Center) { Icon(Icons.Filled.Check, "确定", tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.height(AppSpacing.lg))
        }
        LaunchedEffect(Unit) { focus.requestFocus() }
    }
}
