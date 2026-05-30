package com.kitabi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kitabi.app.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

/**
 * كائن الوصول لبيانات الإشارات المرجعية
 * يوفر عمليات إدارة إشارات الكتب المرجعية
 */
@Dao
interface BookmarkDao {

    /**
     * إدراج إشارة مرجعية جديدة
     * @param bookmark كيان الإشارة المرجعية
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    /**
     * حذف إشارة مرجعية
     * @param bookmark كيان الإشارة المرجعية
     */
    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    /**
     * حذف إشارة مرجعية بالمعرف
     * @param id معرف الإشارة المرجعية
     */
    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)

    /**
     * الحصول على إشارات كتاب معين
     * @param bookId معرف الكتاب
     * @return تدفق قائمة الإشارات المرجعية
     */
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY page ASC")
    fun getBookmarksForBook(bookId: String): Flow<List<BookmarkEntity>>

    /**
     * الحصول على جميع الإشارات المرجعية
     * @return تدفق قائمة الإشارات المرجعية
     */
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    /**
     * عدد الإشارات المرجعية لكتاب معين
     * @param bookId معرف الكتاب
     * @return عدد الإشارات المرجعية
     */
    @Query("SELECT COUNT(*) FROM bookmarks WHERE bookId = :bookId")
    fun getBookmarkCountForBook(bookId: String): Flow<Int>
}
