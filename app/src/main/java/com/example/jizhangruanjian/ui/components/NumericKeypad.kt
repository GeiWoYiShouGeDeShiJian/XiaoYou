package com.example.jizhangruanjian.ui.components
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
import com.example.jizhangruanjian.ui.theme.KeypadColors
enum class KeypadAction { DELETE, SAVE, SAVE_AND_CONTINUE, TOGGLE_CALCULATOR, APPLY }
private val gap = AppSpacing.sm
private val keyShape = RoundedCornerShape(26.dp)
private val deleteBg = KeypadColors.deleteBg
private val againBg = KeypadColors.repeatBg
private val saveBg = KeypadColors.saveBg
private val saveText = KeypadColors.saveText
private val eqBg = KeypadColors.equalBg
// 偏好：hapticEnabled 触感反馈 / reversed 倒序数字 / keySize 按键高度
@Composable
fun NumericKeypad(onKey: (String) -> Unit, onAction: (KeypadAction) -> Unit, isCalculatorMode: Boolean, displayText: String, modifier: Modifier = Modifier, secondaryLabel: String = "再记", hapticEnabled: Boolean = false, reversed: Boolean = false, keySize: Dp = 52.dp) {
    val view = LocalView.current
    val tap = { if (hapticEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    val kh = keySize
    val rows = if (reversed) listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3")) else listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        if (isCalculatorMode) {
            Text(displayText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                Column(modifier = Modifier.weight(3f), verticalArrangement = Arrangement.spacedBy(gap)) {
                    rows.forEach { r -> KeypadRow { r.forEach { d -> DigitKey(onKey, d, kh, tap) } } }
                    KeypadRow { IconKey({ tap(); onAction(KeypadAction.TOGGLE_CALCULATOR) }, kh, tap); DigitKey(onKey, "0", kh, tap); DigitKey(onKey, ".", kh, tap) }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(gap)) {
                    KeypadRow { OpKey("+", { tap(); onKey("+") }, againBg, saveText, kh) }
                    KeypadRow { OpKey("−", { tap(); onKey("-") }, againBg, saveText, kh) }
                    KeypadRow { OpKey("×", { tap(); onKey("*") }, againBg, saveText, kh) }
                    KeypadRow { OpKey("÷", { tap(); onKey("/") }, againBg, saveText, kh) }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(gap)) {
                    KeypadRow { ActionKey("⌫", { tap(); onAction(KeypadAction.DELETE) }, deleteBg, h = kh) }
                    KeypadRow { OpKey("＝", { tap(); onKey("=") }, eqBg, Color.White, kh) }
                    KeypadRow { SaveKey(onAction, text = "✓", height = kh * 2 + gap, applyMode = true, tap = tap) }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                Column(modifier = Modifier.weight(3f), verticalArrangement = Arrangement.spacedBy(gap)) {
                    rows.forEach { r -> KeypadRow { r.forEach { d -> DigitKey(onKey, d, kh, tap) } } }
                    KeypadRow { IconKey({ tap(); onAction(KeypadAction.TOGGLE_CALCULATOR) }, kh, tap); DigitKey(onKey, "0", kh, tap); DigitKey(onKey, ".", kh, tap) }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(gap)) {
                    KeypadRow { ActionKey("⌫", { tap(); onAction(KeypadAction.DELETE) }, deleteBg, h = kh) }
                    KeypadRow { ActionKey(secondaryLabel, { tap(); onAction(KeypadAction.SAVE_AND_CONTINUE) }, againBg, h = kh) }
                    KeypadRow { SaveKey(onAction, height = kh * 2 + gap, tap = tap) }
                }
            }
        }
    }
}
@Composable
private fun KeypadRow(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap), content = content)
}
@Composable
private fun RowScope.DigitKey(onKey: (String) -> Unit, k: String, h: Dp, tap: () -> Unit) {
    Box(modifier = Modifier.weight(1f).height(h).clip(keyShape).background(MaterialTheme.colorScheme.surface).clickable { tap(); onKey(k) }, contentAlignment = Alignment.Center) {
        Text(k, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
    }
}
@Composable
private fun RowScope.IconKey(onClick: () -> Unit, h: Dp, tap: () -> Unit) {
    Box(modifier = Modifier.weight(1f).height(h).clip(keyShape).background(MaterialTheme.colorScheme.surface).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.Calculate, contentDescription = "计算器", modifier = Modifier.height(AppSize.iconLarge), tint = MaterialTheme.colorScheme.onSurface)
    }
}
@Composable
private fun RowScope.OpKey(text: String, onClick: () -> Unit, bg: Color, contentColor: Color, h: Dp) {
    Box(modifier = Modifier.weight(1f).height(h).clip(keyShape).background(bg).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.titleLarge, color = contentColor, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}
@Composable
private fun RowScope.ActionKey(text: String, onClick: () -> Unit, bg: Color, contentColor: Color = MaterialTheme.colorScheme.onSurface, h: Dp = 52.dp) {
    Box(modifier = Modifier.weight(1f).height(h).clip(keyShape).background(bg).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = contentColor, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
    }
}
@Composable
private fun RowScope.SaveKey(onAction: (KeypadAction) -> Unit, height: Dp, text: String = "保存", applyMode: Boolean = false, tap: () -> Unit = {}) {
    Box(modifier = Modifier.weight(1f).height(height).clip(keyShape).background(saveBg).clickable { tap(); onAction(if (applyMode) KeypadAction.APPLY else KeypadAction.SAVE) }, contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.titleLarge, color = saveText, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}
