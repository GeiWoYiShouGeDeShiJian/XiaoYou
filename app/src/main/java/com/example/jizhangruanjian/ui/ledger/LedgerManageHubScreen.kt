package com.example.jizhangruanjian.ui.ledger
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jizhangruanjian.core.database.MemberDao
import com.example.jizhangruanjian.core.database.MerchantDao
import com.example.jizhangruanjian.core.database.MerchantGroupDao
import com.example.jizhangruanjian.core.database.TagDao
import com.example.jizhangruanjian.core.database.TagGroupDao
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.data.model.Member
import com.example.jizhangruanjian.data.model.Merchant
import com.example.jizhangruanjian.data.model.MerchantGroup
import com.example.jizhangruanjian.data.model.Tag
import com.example.jizhangruanjian.data.model.TagGroup
import com.example.jizhangruanjian.data.repository.CategoryRepository
import com.example.jizhangruanjian.ui.record.MerchantEditScreen
import com.example.jizhangruanjian.ui.record.MerchantManageScreen
import com.example.jizhangruanjian.ui.record.RoleEditScreen
import com.example.jizhangruanjian.ui.record.RoleManageScreen
import com.example.jizhangruanjian.ui.record.TagEditScreen
import com.example.jizhangruanjian.ui.record.TagManageScreen
import com.example.jizhangruanjian.ui.components.CategoryIcon
import com.example.jizhangruanjian.ui.components.CollapsingTitleScaffold
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@HiltViewModel
class LedgerManageHubViewModel @Inject constructor(categoryRepository: CategoryRepository, private val tagDao: TagDao, private val tagGroupDao: TagGroupDao, private val memberDao: MemberDao, private val merchantDao: MerchantDao, private val merchantGroupDao: MerchantGroupDao) : ViewModel() {
    val categories = categoryRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tags = tagDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tagGroups = tagGroupDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val members = memberDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val merchants = merchantDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val merchantGroups = merchantGroupDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun saveTag(tag: Tag) = viewModelScope.launch { if (tag.id == 0L) tagDao.insert(tag) else tagDao.update(tag) }
    fun deleteTag(id: Long) = viewModelScope.launch { tagDao.getById(id)?.let { tagDao.delete(it) } }
    fun saveTagGroup(group: TagGroup) = viewModelScope.launch { if (group.id == 0L) tagGroupDao.insert(group) else tagGroupDao.update(group) }
    fun saveMember(member: Member) = viewModelScope.launch { if (member.id == 0L) memberDao.insert(member) else memberDao.update(member) }
    fun deleteMember(id: Long) = viewModelScope.launch { memberDao.delete(id) }
    fun saveMerchant(merchant: Merchant) = viewModelScope.launch { if (merchant.id == 0L) merchantDao.insert(merchant) else merchantDao.update(merchant) }
    fun saveMerchantGroup(group: MerchantGroup) = viewModelScope.launch { if (group.id == 0L) merchantGroupDao.insert(group) else merchantGroupDao.update(group) }
    fun reorderMerchants(ids: List<Long>) = viewModelScope.launch { ids.forEachIndexed { i, id -> merchantDao.updateSort(id, i) } }
}
@Composable
fun LedgerManageHubScreen(viewModel: LedgerManageHubViewModel = hiltViewModel(), onBack: () -> Unit, onOpenCategoryManage: (CategoryType) -> Unit) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val tagGroups by viewModel.tagGroups.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val merchants by viewModel.merchants.collectAsStateWithLifecycle()
    val merchantGroups by viewModel.merchantGroups.collectAsStateWithLifecycle()
    var overlay by remember { mutableStateOf("") }
    var tagEditTarget by remember { mutableStateOf<Tag?>(null) }
    var roleEditTarget by remember { mutableStateOf<Member?>(null) }
    var merchantEditTarget by remember { mutableStateOf<Merchant?>(null) }
    BackHandler(enabled = overlay.isNotEmpty()) {
        overlay = when (overlay) {
            "tag_edit" -> "tag_manage"
            "role_edit" -> "role_manage"
            "merchant_edit" -> "merchant_manage"
            else -> ""
        }
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CollapsingTitleScaffold(title = "账本管理", onBack = onBack) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("类别管理", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                HubRow("支出", categories.count { it.type == CategoryType.EXPENSE }.toString()) { onOpenCategoryManage(CategoryType.EXPENSE) }
                HubRow("收入", categories.count { it.type == CategoryType.INCOME }.toString()) { onOpenCategoryManage(CategoryType.INCOME) }
                HubRow("转账", categories.count { it.type == CategoryType.TRANSFER }.toString()) { onOpenCategoryManage(CategoryType.TRANSFER) }
                HubRow("借贷", categories.count { it.type == CategoryType.LOAN }.toString()) { onOpenCategoryManage(CategoryType.LOAN) }
                HubRow("系统", "3") { overlay = "system" }
                Text("标签管理", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                HubRow("标签", tags.size.toString()) { overlay = "tag_manage" }
                HubRow("角色", members.size.toString()) { overlay = "role_manage" }
                HubRow("商家", merchants.size.toString()) { overlay = "merchant_manage" }
                Text("账本币种", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                HubRow("本位币", "人民币") { android.widget.Toast.makeText(context, "币种管理开发中", android.widget.Toast.LENGTH_SHORT).show() }
                Spacer(Modifier.height(24.dp))
            }
        }
        if (overlay.isNotEmpty()) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                when (overlay) {
                    "tag_edit" -> TagEditScreen(initial = tagEditTarget, groups = tagGroups, onSave = { viewModel.saveTag(it); overlay = "tag_manage" }, onClose = { overlay = "tag_manage" })
                    "tag_manage" -> TagManageScreen(groups = tagGroups, tags = tags, onBack = { overlay = "" }, onAddTag = { tagEditTarget = null; overlay = "tag_edit" }, onAddGroup = { name -> viewModel.saveTagGroup(TagGroup(name = name)) }, onRenameGroup = { g, n -> viewModel.saveTagGroup(g.copy(name = n)) }, onEditTag = { tagEditTarget = it; overlay = "tag_edit" }, onDeleteTag = { viewModel.deleteTag(it) })
                    "role_edit" -> RoleEditScreen(initial = roleEditTarget, onSave = { viewModel.saveMember(it); overlay = "role_manage" }, onClose = { overlay = "role_manage" })
                    "role_manage" -> RoleManageScreen(members = members, onBack = { overlay = "" }, onAddNew = { roleEditTarget = null; overlay = "role_edit" }, onEdit = { roleEditTarget = it; overlay = "role_edit" }, onDelete = { viewModel.deleteMember(it.id) })
                    "merchant_edit" -> MerchantEditScreen(initial = merchantEditTarget, groups = merchantGroups, onSave = { viewModel.saveMerchant(it); overlay = "merchant_manage" }, onClose = { overlay = "merchant_manage" })
                    "system" -> SystemCategoryScreen(onBack = { overlay = "" })
                    else -> MerchantManageScreen(groups = merchantGroups, merchants = merchants, onBack = { overlay = "" }, onAddMerchant = { merchantEditTarget = null; overlay = "merchant_edit" }, onAddGroup = { name -> viewModel.saveMerchantGroup(MerchantGroup(name = name)) }, onRenameGroup = { g, n -> viewModel.saveMerchantGroup(g.copy(name = n)) }, onEditMerchant = { merchantEditTarget = it; overlay = "merchant_edit" }, onReorderMerchants = { viewModel.reorderMerchants(it) })
                }
            }
        }
    }
}
// 系统类别页：与角色/商家管理页同风格（Scaffold+TopAppBar+列表行），展示内置系统分类，不可编辑
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SystemCategoryScreen(onBack: () -> Unit) {
    val items = listOf(
        Triple("system/01", "余额变更", "账户余额初始化、盘点调整时由 App 自动生成"),
        Triple("system/02", "退款", "退款操作关联生成的系统记录"),
        Triple("system/03", "账单分期", "大额消费分期时由 App 自动拆分生成")
    )
    Scaffold(topBar = { TopAppBar(title = { Text("系统类别") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Text("系统类别为内置分类，暂不支持编辑", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            items.forEach { (icon, name, desc) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    CategoryIcon(icon, size = 40.dp, fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
@Composable
private fun HubRow(label: String, value: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 18.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp).size(18.dp))
    }
}
