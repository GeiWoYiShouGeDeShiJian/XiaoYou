package com.example.jizhangruanjian.ui.components
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.jizhangruanjian.core.util.ImageSaver
@Composable
fun ImageViewer(images: List<String>, initialIndex: Int = 0, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                var scale by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                val bmp by produceState<Bitmap?>(initialValue = null, key1 = images[page]) { value = ImageSaver.loadBitmap(images[page], 1600) }
                Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset += pan
                    }
                }, contentAlignment = Alignment.Center) {
                    if (bmp != null) {
                        Image(bitmap = bmp!!.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y })
                    } else Text("加载中…", color = Color.White)
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) { Icon(Icons.Filled.Close, contentDescription = "关闭", tint = Color.White) }
            Text("${currentPage + 1} / ${images.size}", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }
    }
}