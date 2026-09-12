package com.example.jizhangruanjian.core.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jizhangruanjian.data.model.Category
import com.example.jizhangruanjian.data.model.CategoryType
import kotlinx.coroutines.flow.Flow
@Dao
interface CategoryDao {
    @Insert suspend fun insert(category: Category): Long
    @Update suspend fun update(category: Category)
    @Delete suspend fun delete(category: Category)
    @Query("SELECT * FROM category WHERE id = :id") suspend fun getById(id: Long): Category?
    @Query("SELECT * FROM category ORDER BY sort_order ASC") fun observeAll(): Flow<List<Category>>
    // 记账表单：取某类收支的所有分类（含二级，parent_id 为 null 或非 null，按层级排）
    @Query("SELECT * FROM category WHERE type = :type ORDER BY sort_order ASC, parent_id ASC")
    fun observeByType(type: CategoryType): Flow<List<Category>>
    // R13 全量分类（关键词匹配用）
    @Query("SELECT * FROM category") suspend fun getAll(): List<Category>
    @Query("SELECT * FROM category WHERE is_favorite = 1 ORDER BY sort_order ASC") fun observeFavorites(): Flow<List<Category>>
    @Query("UPDATE category SET is_favorite = :isFavorite WHERE id = :id") suspend fun updateFavorite(id: Long, isFavorite: Boolean)
    @Query("UPDATE category SET sort_order = :sortOrder WHERE id = :id") suspend fun updateSortOrder(id: Long, sortOrder: Int)
    @Query("SELECT * FROM category WHERE type = :type AND parent_id IS :parentId ORDER BY sort_order ASC")
    fun observeByTypeAndParent(type: CategoryType, parentId: Long?): Flow<List<Category>>
    @Query("DELETE FROM category WHERE type = :type") suspend fun deleteByType(type: CategoryType)
}
