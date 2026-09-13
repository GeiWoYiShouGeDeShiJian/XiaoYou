package com.example.jizhangruanjian.ui.ledger
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jizhangruanjian.R
import com.example.jizhangruanjian.domain.model.LedgerDomain
import com.example.jizhangruanjian.ui.account.LedgerViewModel
private val TEMPLATE_EMOJI_COVERS = listOf(
    "e:📕|0xFF5B8DB8", "e:💰|0xFFC9A227", "e:🧧|0xFFD96A6A", "e:✈️|0xFF4A90D9",
    "e:👶|0xFF9CCC65", "e:🚗|0xFF4A4A55", "e:🏝️|0xFF5BC8C0", "cover_pencils"
)
private fun coverEmoji(cover: String): String? = if (cover.startsWith("e:")) cover.drop(2).substringBefore("|") else null
private fun coverBg(cover: String): Color = if (cover.startsWith("e:")) Color(cover.substringAfter("|", "0xFF8D6E63").toLong()) else Color.Transparent
@Composable
fun LedgerCoverImage(cover: String, modifier: Modifier, emojiSize: Int = 28) {
    val emoji = coverEmoji(cover)
    if (emoji != null) {
        Box(modifier = modifier.background(coverBg(cover)), contentAlignment = Alignment.Center) { Text(emoji, fontSize = emojiSize.sp) }
    } else {
        val res = if (cover == "cover_pencils") R.drawable.cover_pencils else R.drawable.cover_pencils
        Image(painter = painterResource(id = res), contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier)
    }
}
data class LedgerTemplate(val name: String, val emoji: String, val bg: Long, val desc: String, val categories: List<String>)
private val TEMPLATES = listOf(
    LedgerTemplate("日常账本", "📕", 0xFF5B8DB8, "餐饮、交通、购物、居家、医疗、教育等", listOf("餐饮", "交通", "购物", "居家", "医疗", "教育")),
    LedgerTemplate("生意账本", "💰", 0xFFC9A227, "进货采购、人工支出、店面租金等", listOf("进货采购", "人工支出", "店面租金", "营业收入")),
    LedgerTemplate("人情账本", "🧧", 0xFFD96A6A, "礼金红包、物品、孝敬、请客等", listOf("礼金红包", "请客", "孝敬")),
    LedgerTemplate("旅行账本", "✈️", 0xFF4A90D9, "旅游团费、酒店住宿、吃喝、交通出行等", listOf("旅游团费", "酒店住宿", "吃喝", "交通出行")),
    LedgerTemplate("宝宝账本", "👶", 0xFF9CCC65, "宝宝生活、宝宝服饰、宝宝玩具等", listOf("宝宝生活", "宝宝服饰", "宝宝玩具")),
    LedgerTemplate("汽车账本", "🚗", 0xFF4A4A55, "油费电费、保养维修、车款车贷等", listOf("油费电费", "保养维修", "车款车贷")),
    LedgerTemplate("空白账本", "🏝️", 0xFF5BC8C0, "无内置收支类别", emptyList())
)
@Composable
private fun LedgerTopBar(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        actions()
    }
}
@Composable
private fun LedgerFormFields(name: String, onName: (String) -> Unit, cover: String, onCover: (String) -> Unit, coverDark: Boolean, onCoverDark: (Boolean) -> Unit, hidden: Boolean, onHidden: (Boolean) -> Unit, nameError: Boolean = false) {
    var coverDialog by remember { mutableStateOf(false) }
    var textMenu by remember { mutableStateOf(false) }
    Spacer(Modifier.height(20.dp))
    OutlinedTextField(value = name, onValueChange = onName, label = { Text("账本名称") }, isError = nameError, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(20.dp))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { coverDialog = true }.padding(vertical = 12.dp)) {
        Text("账本封面", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        LedgerCoverImage(cover, Modifier.size(width = 52.dp, height = 32.dp).clip(RoundedCornerShape(6.dp)))
        Text(" ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text("封面文字", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Box {
            Text(if (coverDark) "深色" else "浅色", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { textMenu = true })
            DropdownMenu(expanded = textMenu, onDismissRequest = { textMenu = false }) {
                DropdownMenuItem(text = { Text("浅色") }, onClick = { onCoverDark(false); textMenu = false })
                DropdownMenuItem(text = { Text("深色") }, onClick = { onCoverDark(true); textMenu = false })
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text("隐藏账本", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = hidden, onCheckedChange = onHidden)
    }
    if (coverDialog) {
        AlertDialog(onDismissRequest = { coverDialog = false }, title = { Text("选择封面") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TEMPLATE_EMOJI_COVERS.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onCover(c); coverDialog = false }.padding(vertical = 4.dp)) {
                        LedgerCoverImage(c, Modifier.size(width = 56.dp, height = 34.dp).clip(RoundedCornerShape(6.dp)))
                        Spacer(Modifier.width(12.dp))
                        Text(if (c == "cover_pencils") "彩色铅笔" else "emoji ${coverEmoji(c)}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.weight(1f))
                        if (c == cover) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { coverDialog = false }) { Text("取消") } })
    }
}
@Composable
fun LedgerPickerScreen(viewModel: LedgerViewModel = hiltViewModel(), onBack: () -> Unit, onAdd: () -> Unit, onEdit: (Long) -> Unit) {
    val ledgers by viewModel.ledgers.collectAsState()
    val counts by viewModel.counts.collectAsState()
    val currentId by viewModel.currentId.collectAsState()
    var moreMenu by remember { mutableStateOf(false) }
    var showHidden by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        LedgerTopBar("选择账本", onBack, actions = {
            IconButton(onClick = onAdd) { Icon(Icons.Filled.AddCircle, contentDescription = "添加账本") }
            Box {
                IconButton(onClick = { moreMenu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "更多") }
                DropdownMenu(expanded = moreMenu, onDismissRequest = { moreMenu = false }) {
                    DropdownMenuItem(text = { Text(if (showHidden) "查看普通账本" else "查看隐藏账本") }, onClick = { showHidden = !showHidden; moreMenu = false })
                }
            }
        })
        val list = ledgers.filter { it.hidden == showHidden }
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(list.size, key = { list[it].id }) { idx ->
                val l = list[idx]
                Column(modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).clickable { viewModel.switch(l.id); onBack() }) {
                    Box {
                        LedgerCoverImage(l.cover, Modifier.fillMaxWidth().aspectRatio(1.55f))
                        if (l.id == currentId) {
                            Box(modifier = Modifier.padding(6.dp).clip(RoundedCornerShape(6.dp)).background(Color(0x66000000)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("● 当前账本", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(l.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
                            Text("编辑", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.clickable { onEdit(l.id) }.padding(2.dp))
                        }
                        Spacer(Modifier.height(2.dp))
                        Text("共 ${counts[l.id] ?: 0} 笔明细", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (list.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(if (showHidden) "暂无隐藏账本" else "暂无账本", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
@Composable
fun LedgerTemplateScreen(onBack: () -> Unit, onPick: (LedgerTemplate) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        LedgerTopBar("选择账本模板", onBack)
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
            TEMPLATES.forEach { t ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onPick(t) }.padding(vertical = 10.dp, horizontal = 4.dp)) {
                    Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(coverBg("e:${t.emoji}|${t.bg}")), contentAlignment = Alignment.Center) { Text(t.emoji, fontSize = 24.sp) }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(t.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(t.desc, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
@Composable
fun LedgerAddScreen(viewModel: LedgerViewModel = hiltViewModel(), template: LedgerTemplate?, onBack: () -> Unit) {
    var name by remember { mutableStateOf(template?.name ?: "") }
    var cover by remember { mutableStateOf(template?.let { "e:${it.emoji}|${it.bg}" } ?: "cover_pencils") }
    var coverDark by remember { mutableStateOf(false) }
    var hidden by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        LedgerTopBar("", onBack, actions = {
            IconButton(onClick = {
                if (name.isBlank()) { nameError = true; return@IconButton }
                viewModel.create(name.trim(), cover, coverDark, "CNY", hidden, template?.categories ?: emptyList())
                onBack()
            }) { Icon(Icons.Filled.Check, contentDescription = "保存") }
        })
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("添加账本", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            LedgerFormFields(name, { name = it; nameError = false }, cover, { cover = it }, coverDark, { coverDark = it }, hidden, { hidden = it }, nameError)
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primary).clickable {
                if (name.isBlank()) { nameError = true; return@clickable }
                viewModel.create(name.trim(), cover, coverDark, "CNY", hidden, template?.categories ?: emptyList())
                onBack()
            }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                Text("保存", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
@Composable
fun LedgerEditScreen(viewModel: LedgerViewModel = hiltViewModel(), ledgerId: Long, onBack: () -> Unit) {
    val ledgers by viewModel.ledgers.collectAsState()
    val l = ledgers.firstOrNull { it.id == ledgerId } ?: LedgerDomain(ledgerId, "", 0, "📖", false, 0)
    var name by remember(l.id) { mutableStateOf(l.name) }
    var cover by remember(l.id) { mutableStateOf(l.cover) }
    var coverDark by remember(l.id) { mutableStateOf(l.coverDark) }
    var hidden by remember(l.id) { mutableStateOf(l.hidden) }
    var nameError by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        LedgerTopBar("", onBack, actions = {
            IconButton(onClick = {
                if (name.isBlank()) { nameError = true; return@IconButton }
                viewModel.update(ledgerId, name.trim(), cover, coverDark, l.currency, hidden)
                onBack()
            }) { Icon(Icons.Filled.Check, contentDescription = "保存") }
        })
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("编辑账本", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            LedgerFormFields(name, { name = it; nameError = false }, cover, { cover = it }, coverDark, { coverDark = it }, hidden, { hidden = it }, nameError)
            Spacer(Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).clickable { confirmDelete = true }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("删除", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primary).clickable {
                    if (name.isBlank()) { nameError = true; return@clickable }
                    viewModel.update(ledgerId, name.trim(), cover, coverDark, l.currency, hidden)
                    onBack()
                }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("保存", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    if (confirmDelete) {
        AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("删除账本") }, text = { Text("确定删除「${l.name}」吗？该账本下的账户关联会一并移除。") }, confirmButton = {
            TextButton(onClick = { viewModel.delete(ledgerId); confirmDelete = false; onBack() }) { Text("删除", color = MaterialTheme.colorScheme.error) }
        }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("取消") } })
    }
}
