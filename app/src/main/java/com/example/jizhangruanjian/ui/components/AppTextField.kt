package com.example.jizhangruanjian.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.example.jizhangruanjian.ui.theme.AppSpacing
import com.example.jizhangruanjian.ui.theme.MoneyBookTheme
// 统一输入框：基于 M3 OutlinedTextField，错误态走主题 error 色
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = OutlinedTextFieldDefaults.shape
) {
    val showError = isError || !errorMessage.isNullOrEmpty()
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = showError,
            enabled = enabled,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = shape,
            modifier = Modifier.fillMaxWidth()
        )
        if (showError && !errorMessage.isNullOrEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = AppSpacing.lg, top = AppSpacing.xs))
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun AppTextFieldNormalPreview() {
    MoneyBookTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md), modifier = Modifier.padding(AppSpacing.lg)) {
            var v by remember { mutableStateOf("") }
            AppTextField(value = v, onValueChange = { v = it }, label = "普通态", placeholder = "请输入")
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun AppTextFieldErrorPreview() {
    MoneyBookTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md), modifier = Modifier.padding(AppSpacing.lg)) {
            var v by remember { mutableStateOf("") }
            AppTextField(value = v, onValueChange = { v = it }, label = "错误态", isError = true, errorMessage = "输入有误，请检查")
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun AppTextFieldSearchPreview() {
    MoneyBookTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md), modifier = Modifier.padding(AppSpacing.lg)) {
            var v by remember { mutableStateOf("") }
            AppTextField(value = v, onValueChange = { v = it }, label = "搜索", placeholder = "回车触发搜索", keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { v = "搜索:$v" }))
        }
    }
}