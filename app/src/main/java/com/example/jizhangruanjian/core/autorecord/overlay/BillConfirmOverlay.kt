package com.example.jizhangruanjian.core.autorecord.overlay
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.jizhangruanjian.core.autorecord.AutoRecordPipeline
import com.example.jizhangruanjian.core.autorecord.BillCandidate
import com.example.jizhangruanjian.core.autorecord.BillType
import com.example.jizhangruanjian.data.model.TransactionSource
import com.example.jizhangruanjian.data.model.TransactionType
import com.example.jizhangruanjian.data.repository.AccountRepository
import com.example.jizhangruanjian.data.repository.CategoryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OverlayEntryPoint {
    fun pipeline(): AutoRecordPipeline
    fun accountRepository(): AccountRepository
    fun categoryRepository(): CategoryRepository
}

@Singleton
class BillConfirmOverlay @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val entryPoint = EntryPointAccessors.fromApplication(context, OverlayEntryPoint::class.java)
    private val pipeline get() = entryPoint.pipeline()
    private val accountRepository get() = entryPoint.accountRepository()
    private val categoryRepository get() = entryPoint.categoryRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var owner: OverlayLifecycleOwner? = null
    private var dismissJob: Job? = null

    fun show(candidate: BillCandidate) {
        if (!Settings.canDrawOverlays(context)) {
            scope.launch { pipeline.notifyConfirm(candidate, TransactionSource.AUTO_ACCESSIBILITY) }
            return
        }
        dismissJob?.cancel()
        dismissJob = scope.launch {
            val accountId = pipeline.resolveAccountId(candidate.packageName)
            val categoryId = pipeline.resolveCategoryId(candidate)
            val accountName = accountRepository.observeAll().first().firstOrNull { it.id == accountId }?.name ?: ""
            val categoryName = categoryRepository.getAll().firstOrNull { it.id == categoryId }?.name ?: ""
            val owner = OverlayLifecycleOwner()
            this@BillConfirmOverlay.owner = owner
            val view = ComposeView(context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setViewTreeLifecycleOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
                setContent {
                    BillConfirmContent(
                        candidate = candidate,
                        accountName = accountName,
                        categoryName = categoryName,
                        onConfirm = { confirm(candidate) },
                        onIgnore = { ignore(candidate) },
                        onEdit = { edit(candidate) }
                    )
                }
            }
            composeView = view
            val added = runCatching { windowManager.addView(view, buildParams()) }.isSuccess
            if (!added) {
                owner.destroy()
                this@BillConfirmOverlay.owner = null
                composeView = null
                pipeline.notifyConfirm(candidate, TransactionSource.AUTO_ACCESSIBILITY)
                return@launch
            }
            startAutoDismiss(candidate)
            verifyVisible(candidate)
        }
    }
    private fun confirm(candidate: BillCandidate) {
        dismissJob?.cancel()
        scope.launch {
            pipeline.confirmRecord(candidate)
            dismiss()
        }
    }
    private fun ignore(candidate: BillCandidate) {
        dismissJob?.cancel()
        scope.launch {
            pipeline.ignore(candidate)
            dismiss()
        }
    }
    private fun edit(candidate: BillCandidate) {
        dismissJob?.cancel()
        dismiss()
        val type = if (candidate.billType == BillType.INCOME) TransactionType.INCOME else TransactionType.EXPENSE
        val amountYuan = String.format(Locale.CHINA, "%.2f", candidate.amount / 100.0)
        val url = "jizhang://record?amount=$amountYuan&type=${type.name}&merchant=${Uri.encode(candidate.merchant ?: "")}&source=${TransactionSource.AUTO_ACCESSIBILITY.name}"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    private fun dismiss() {
        dismissJob?.cancel()
        composeView?.let { runCatching { windowManager.removeView(it) } }
        composeView = null
        owner?.destroy()
        owner = null
    }
    private fun startAutoDismiss(candidate: BillCandidate) {
        dismissJob?.cancel()
        dismissJob = scope.launch {
            delay(15_000)
            dismiss()
            pipeline.notifyConfirm(candidate, TransactionSource.AUTO_ACCESSIBILITY)
        }
    }
    // 支付保护拦截检测：addView 成功但窗口未显示 → 降级通知确认
    private fun verifyVisible(candidate: BillCandidate) {
        scope.launch {
            delay(700)
            val view = composeView
            if (view == null || !view.isShown) {
                dismiss()
                pipeline.notifyConfirm(candidate, TransactionSource.AUTO_ACCESSIBILITY)
            }
        }
    }
    private fun buildParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        return WindowManager.LayoutParams(
            (context.resources.displayMetrics.widthPixels * 0.92f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (80 * context.resources.displayMetrics.density).toInt()
        }
    }
}
