package com.example.jizhangruanjian.data.repository
import com.example.jizhangruanjian.core.database.CategoryDao
import com.example.jizhangruanjian.data.model.Category
import com.example.jizhangruanjian.data.model.CategoryType
import com.example.jizhangruanjian.domain.model.CategoryDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class CategoryRepository @Inject constructor(private val categoryDao: CategoryDao, private val seed: com.example.jizhangruanjian.core.database.DatabaseSeed) {
    fun observeByType(type: CategoryType): Flow<List<CategoryDomain>> =
        categoryDao.observeByType(type).map { list -> list.map { it.toDomain() } }
    // P3 记账表单用（含全部类型），备 R13 与后续
    fun observeAll(): Flow<List<CategoryDomain>> = categoryDao.observeAll().map { list -> list.map { it.toDomain() } }
    suspend fun getAll(): List<CategoryDomain> = categoryDao.getAll().map { it.toDomain() }
    // R13 记忆关键词到分类：追加到 keyword_match(JSON 数组)
    suspend fun rememberKeyword(categoryId: Long, keyword: String) {
        if (keyword.isBlank()) return
        val c = categoryDao.getById(categoryId) ?: return
        val arr = org.json.JSONArray(c.keywordMatch ?: "[]")
        val exists = (0 until arr.length()).any { i -> arr.getString(i) == keyword }
        if (!exists) { arr.put(keyword); categoryDao.update(c.copy(keywordMatch = arr.toString())) }
    }
    fun observeFavorites(): Flow<List<CategoryDomain>> = categoryDao.observeFavorites().map { list -> list.map { it.toDomain() } }
    fun observeByTypeAndParent(type: CategoryType, parentId: Long?): Flow<List<CategoryDomain>> =
        categoryDao.observeByTypeAndParent(type, parentId).map { list -> list.map { it.toDomain() } }
    suspend fun toggleFavorite(id: Long) {
        val c = categoryDao.getById(id) ?: return
        categoryDao.updateFavorite(id, !c.isFavorite)
    }
    suspend fun reorderCategories(ids: List<Long>) {
        ids.forEachIndexed { index, id -> categoryDao.updateSortOrder(id, index) }
    }
    suspend fun resetToSeed(type: CategoryType) = seed.resetCategories(type)
    suspend fun addCategory(name: String, type: CategoryType, parentId: Long?, icon: String = "📁") {
        val order = (categoryDao.getAll().count { it.type == type && it.parentId == parentId }) + 1
        categoryDao.insert(Category(parentId = parentId, name = name, type = type, icon = icon, sortOrder = order))
    }
    private fun Category.toDomain() = CategoryDomain(id, parentId, name, type, icon, sortOrder, keywordMatch, isFavorite)
}