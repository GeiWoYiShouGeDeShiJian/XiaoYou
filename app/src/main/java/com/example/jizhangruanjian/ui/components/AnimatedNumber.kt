package com.example.jizhangruanjian.ui.components
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import java.text.NumberFormat
import java.util.Locale
@Composable
fun AnimatedMoney(amount: Long, modifier: Modifier = Modifier, style: TextStyle = MaterialTheme.typography.headlineLarge, fontWeight: FontWeight? = null, color: Color = Color.Unspecified, durationMillis: Int = 800) {
    val animated by animateFloatAsState(targetValue = amount.toFloat(), animationSpec = tween(durationMillis), label = "money")
    Text(moneyText(animated.toLong()), modifier = modifier, style = style, fontWeight = fontWeight, color = color)
}
private fun moneyText(v: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale.CHINA).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
    return "¥" + nf.format(v / 100.0)
}