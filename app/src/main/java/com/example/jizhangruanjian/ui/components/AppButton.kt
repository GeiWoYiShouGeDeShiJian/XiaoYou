package com.example.jizhangruanjian.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.jizhangruanjian.ui.theme.AppRadius
import com.example.jizhangruanjian.ui.theme.AppSize
import com.example.jizhangruanjian.ui.theme.AppSpacing
import com.example.jizhangruanjian.ui.theme.MoneyBookTheme
// 统一按钮：四变体，高度/圆角/内边距全部走 token，颜色走主题
enum class AppButtonVariant { Primary, Tonal, Outlined, Text }
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(AppRadius.medium)
    val contentPadding = PaddingValues(horizontal = AppSpacing.lg)
    when (variant) {
        AppButtonVariant.Primary -> Button(onClick = onClick, shape = shape, contentPadding = contentPadding, enabled = enabled, modifier = modifier.height(AppSize.buttonHeight)) { AppButtonContent(leadingIcon, text, trailingIcon) }
        AppButtonVariant.Tonal -> FilledTonalButton(onClick = onClick, shape = shape, contentPadding = contentPadding, enabled = enabled, modifier = modifier.height(AppSize.buttonHeight)) { AppButtonContent(leadingIcon, text, trailingIcon) }
        AppButtonVariant.Outlined -> OutlinedButton(onClick = onClick, shape = shape, contentPadding = contentPadding, enabled = enabled, modifier = modifier.height(AppSize.buttonHeight)) { AppButtonContent(leadingIcon, text, trailingIcon) }
        AppButtonVariant.Text -> TextButton(onClick = onClick, shape = shape, contentPadding = contentPadding, enabled = enabled, modifier = modifier.height(AppSize.buttonHeight)) { AppButtonContent(leadingIcon, text, trailingIcon) }
    }
}
@Composable
private fun AppButtonContent(leadingIcon: (@Composable (() -> Unit))? = null, text: String, trailingIcon: (@Composable (() -> Unit))? = null) {
    leadingIcon?.let { it() }
    Text(text)
    trailingIcon?.let { it() }
}
@Preview(showBackground = true)
@Composable
private fun AppButtonPreview() {
    MoneyBookTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md), modifier = Modifier.padding(AppSpacing.lg)) {
            AppButton(text = "Primary", onClick = {}, variant = AppButtonVariant.Primary)
            AppButton(text = "Tonal", onClick = {}, variant = AppButtonVariant.Tonal)
            AppButton(text = "Outlined", onClick = {}, variant = AppButtonVariant.Outlined)
            AppButton(text = "Text", onClick = {}, variant = AppButtonVariant.Text)
        }
    }
}