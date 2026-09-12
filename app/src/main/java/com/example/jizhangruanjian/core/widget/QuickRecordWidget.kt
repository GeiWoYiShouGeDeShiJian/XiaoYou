package com.example.jizhangruanjian.core.widget
import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.example.jizhangruanjian.MainActivity
// P11 快捷记账 Widget：点击直接打开记账表单
private val quickRecordKey = ActionParameters.Key<Boolean>(MainActivity.EXTRA_QUICK_RECORD)
class QuickRecordWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text("快捷记账")
                Button(text = "记一笔", onClick = actionStartActivity<MainActivity>(actionParametersOf(quickRecordKey to true)))
            }
        }
    }
}
class QuickRecordWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickRecordWidget()
}
