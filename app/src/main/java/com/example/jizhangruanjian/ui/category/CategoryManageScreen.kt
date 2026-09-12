package com.example.jizhangruanjian.ui.category
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.domain.model.CategoryDomain
import com.example.jizhangruanjian.ui.components.CategoryIcon
import kotlin.math.roundToInt
@Composable
fun CategoryManageScreen(onBack: () -> Unit, initialType: CategoryType = CategoryType.EXPENSE, viewModel: CategoryManageViewModel = hiltViewModel()) {
    val currentType by viewModel.currentType.collectAsState()
    val mainCats by viewModel.mainCategories.collectAsState()
    val subCats by viewModel.subCategories.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val selectedMainId by viewModel.selectedMainId.collectAsState()
    val incomeParent by viewModel.incomeParent.collectAsState()
    var tipVisible by remember { mutableStateOf(true) }
    LaunchedEffect(initialType) { viewModel.switchType(initialType) }
    LaunchedEffect(mainCats, currentType) {
        if (mainCats.isNotEmpty() && (selectedMainId == null || mainCats.none { it.id == selectedMainId })) viewModel.selectMain(mainCats.first().id)
    }
    val useSplit = currentType == CategoryType.EXPENSE || (currentType == CategoryType.INCOME && incomeParent)
    var menuOpen by remember { mutableStateOf(false) }
    var addDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopBar(if (currentType == CategoryType.EXPENSE) "支出类别管理" else if (currentType == CategoryType.INCOME) "收入类别管理" else if (currentType == CategoryType.TRANSFER) "转账类别管理" else "借贷类别管理", onBack, menuOpen, { menuOpen = false }, currentType == CategoryType.INCOME, incomeParent, viewModel::resetCategories, viewModel::toggleIncomeParent)
        if (tipVisible) TipBar(if (currentType == CategoryType.INCOME && !incomeParent) "点击类别进行编辑，长按类别拖动排序。若需二级分类，可在右上角菜单中开启「收入大类开关」" else "选中大类后可编辑。长按拖动排序。", onClose = { tipVisible = false })
        val typeFavorites = favorites.filter { it.type == currentType }
        if (typeFavorites.isNotEmpty()) FavoritesRow(typeFavorites, onToggle = { viewModel.toggleFavorite(it.id) })
        if (useSplit) {
            CategorySplit(mainCats, subCats, selectedMainId, onSelectMain = { viewModel.selectMain(it) }, onToggleStar = { viewModel.toggleFavorite(it) }, onMove = { from, to -> viewModel.moveCategory(from, to, subCats) }, onAddMain = { addDialog = true })
        } else {
            if (mainCats.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无分类", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                SubCategoryList(Modifier.fillMaxSize(), mainCats, onToggleStar = { viewModel.toggleFavorite(it) }, onMove = { from, to -> viewModel.moveCategory(from, to, mainCats) })
            }
        }
    }
    if (addDialog) {
        var name by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(onDismissRequest = { addDialog = false }, title = { Text("新增大类") }, text = {
            androidx.compose.material3.OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, placeholder = { Text("大类名称") })
        }, confirmButton = {
            androidx.compose.material3.TextButton(onClick = { viewModel.addMainCategory(name); addDialog = false }) { Text("确定") }
        }, dismissButton = {
            androidx.compose.material3.TextButton(onClick = { addDialog = false }) { Text("取消") }
        })
    }
}
@Composable
private fun TopBar(title: String, onBack: () -> Unit, menuOpen: Boolean, onMenuChange: (Boolean) -> Unit, isIncome: Boolean, incomeParent: Boolean, onReset: () -> Unit, onToggleIncomeParent: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick = {}) { Icon(Icons.Filled.Search, contentDescription = "搜索") }
            Box {
                IconButton(onClick = { onMenuChange(true) }) { Icon(Icons.Filled.MoreVert, contentDescription = "菜单") }
                androidx.compose.material3.DropdownMenu(expanded = menuOpen, onDismissRequest = { onMenuChange(false) }) {
                    androidx.compose.material3.DropdownMenuItem(text = { Text("重置类别") }, onClick = { onMenuChange(false); onReset() })
                    if (isIncome) androidx.compose.material3.DropdownMenuItem(text = { Text(if (incomeParent) "收入大类开关 ✓" else "收入大类开关") }, onClick = { onMenuChange(false); onToggleIncomeParent() })
                }
            }
        }
    }
}
@Composable
private fun TipBar(text: String, onClose: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
        .clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.secondaryContainer).padding(start = 12.dp)) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text("×", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable(onClick = onClose).padding(horizontal = 12.dp, vertical = 6.dp))
    }
}
@Composable
private fun FavoritesRow(favorites: List<CategoryDomain>, onToggle: (CategoryDomain) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
        items(favorites, key = { it.id }) { cat ->
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer).clickable { onToggle(cat) }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                CategoryIcon(cat.icon, size = 24.dp, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(cat.name, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
@Composable
private fun CategorySplit(mainCats: List<CategoryDomain>, subCats: List<CategoryDomain>, selectedMainId: Long?,
                          onSelectMain: (Long) -> Unit, onToggleStar: (Long) -> Unit, onMove: (Int, Int) -> Unit, onAddMain: () -> Unit) {
    Row(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxHeight().width(118.dp)) {
            items(mainCats, key = { it.id }) { cat ->
                val selected = cat.id == selectedMainId
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(56.dp)
                    .background(if (selected) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent)
                    .clickable { onSelectMain(cat.id) }) {
                    Box(Modifier.width(3.dp).fillMaxHeight().background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent))
                    Spacer(Modifier.width(8.dp))
                    Text(cat.name, style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(4.dp))
                    CategoryIcon(cat.icon, size = 22.dp, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(56.dp).clickable(onClick = onAddMain)) {
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Filled.AddCircle, contentDescription = "新增大类", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("大类", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        VerticalDivider(modifier = Modifier.fillMaxHeight())
        if (subCats.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { Text("暂无子分类", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            SubCategoryList(Modifier.weight(1f), subCats, onToggleStar, onMove)
        }
    }
}
@Composable
private fun CategoryIconView(icon: String) {
    CategoryIcon(icon, size = 30.dp, fontSize = 18.sp)
}
@Composable
private fun SubCategoryList(modifier: Modifier, subCats: List<CategoryDomain>, onToggleStar: (Long) -> Unit, onMove: (Int, Int) -> Unit) {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 64.dp.toPx() }
    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val latest by rememberUpdatedState(subCats)
    LazyColumn(modifier.fillMaxHeight()) {
        itemsIndexed(subCats, key = { _, c -> c.id }) { index, cat ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp)
                .graphicsLayer { translationY = if (dragIndex == index) dragOffset else 0f }
                .zIndex(if (dragIndex == index) 1f else 0f)
                .pointerInput(cat.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { dragIndex = latest.indexOfFirst { it.id == cat.id } },
                        onDrag = { change, amount -> change.consume(); if (dragIndex >= 0) dragOffset += amount.y },
                        onDragEnd = {
                            if (dragIndex >= 0) {
                                val target = (dragIndex + (dragOffset / itemHeightPx).roundToInt()).coerceIn(0, latest.lastIndex)
                                if (target != dragIndex) onMove(dragIndex, target)
                                dragIndex = -1
                                dragOffset = 0f
                            }
                        },
                        onDragCancel = { dragIndex = -1; dragOffset = 0f })
                }) {
                CategoryIconView(cat.icon)
                Spacer(Modifier.width(12.dp))
                Text(cat.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Star, contentDescription = "收藏",
                    tint = if (cat.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(24.dp).clickable { onToggleStar(cat.id) })
            }
        }
    }
}