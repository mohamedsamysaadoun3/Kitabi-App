package com.kitabi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kitabi.app.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

/**
 * كائن الوصول لبيانات الكتب
 * يوفر عمليات CRUD لجدول الكتب
 */
@Dao
interface BookDao {

    /**
     * إدراج كتاب جديد
     * @param book كيان الكتاب
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    /**
     * إدراج قائمة كتب
     * @param books قائمة كيانات الكتب
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    /**
     * تحديث كتاب موجود
     * @param book كيان الكتاب المحدث
     */
    @Update
    suspend fun updateBook(book: BookEntity)

    /**
     * حذف كتاب
     * @param book كيان الكتاب المراد حذفه
     */
    @Delete
    suspend fun deleteBook(book: BookEntity)

    /**
     * حذف كتاب بالمعرف
     * @param bookId معرف الكتاب
     */
    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: String)

    /**
     * الحصول على كتاب بالمعرف
     * @param bookId معرف الكتاب
     * @return كيان الكتاب أو null
     */
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): BookEntity?

    /**
     * الحصول على كتاب بالمعرف كـ Flow
     * @param bookId معرف الكتاب
     * @return تدفق كيان الكتاب
     */
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookByIdFlow(bookId: String): Flow<BookEntity?>

    /**
     * الحصول على جميع الكتب
     * @return تدفق قائمة الكتب مرتبة بتاريخ الإضافة
     */
    @Query("SELECT * FROM books ORDER BY addedAt DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    /**
     * الحصول على الكتب المحملة فقط
     * @return تدفق قائمة الكتب المحملة
     */
    @Query("SELECT * FROM books WHERE isDownloaded = 1 ORDER BY addedAt DESC")
    fun getDownloadedBooks(): Flow<List<BookEntity>>

    /**
     * الحصول على الكتب قيد القراءة حالياً
     * @return تدفق قائمة الكتب المقروءة مؤخراً
     */
    @Query("SELECT * FROM books WHERE lastReadAt IS NOT NULL AND isDownloaded = 1 ORDER BY lastReadAt DESC")
    fun getCurrentlyReadingBooks(): Flow<List<BookEntity>>

    /**
     * البحث في الكتب المحلية
     * @param query نص البحث
     * @return تدفق قائمة الكتب المتطابقة
     */
    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY addedAt DESC")
    fun searchBooks(query: String): Flow<List<BookEntity>>

    /**
     * الحصول على كتب حسب التصنيف
     * @param category التصنيف
     * @return تدفق قائمة الكتب
     */
    @Query("SELECT * FROM books WHERE category = :category ORDER BY addedAt DESC")
    fun getBooksByCategory(category: String): Flow<List<BookEntity>>

    /**
     * الحصول على كتب ملكية عامة
     * @return تدفق قائمة الكتب
     */
    @Query("SELECT * FROM books WHERE isPublicDomain = 1 ORDER BY addedAt DESC")
    fun getPublicDomainBooks(): Flow<List<BookEntity>>

    /**
     * الحصول على كتب مترجمة
     * @return تدفق قائمة الكتب
     */
    @Query("SELECT * FROM books WHERE isTranslated = 1 ORDER BY addedAt DESC")
    fun getTranslatedBooks(): Flow<List<BookEntity>>

    /**
     * عدد الكتب في المكتبة
     * @return عدد الكتب
     */
    @Query("SELECT COUNT(*) FROM books")
    fun getBookCount(): Flow<Int>

    /**
     * تحديث حالة التحميل
     * @param bookId معرف الكتاب
     * @param isDownloaded هل تم التحميل
     */
    @Query("UPDATE books SET isDownloaded = :isDownloaded WHERE id = :bookId")
    suspend fun updateDownloadStatus(bookId: String, isDownloaded: Boolean)

    /**
     * تحديث تقدم التحميل
     * @param bookId معرف الكتاب
     * @param progress نسبة التقدم
     * @param state حالة التحميل
     */
    @Query("UPDATE books SET downloadProgress = :progress, downloadState = :state WHERE id = :bookId")
    suspend fun updateDownloadProgress(bookId: String, progress: Int, state: String)

    /**
     * تحديث آخر قراءة
     * @param bookId معرف الكتاب
     * @param lastReadAt طابع زمني آخر قراءة
     */
    @Query("UPDATE books SET lastReadAt = :lastReadAt WHERE id = :bookId")
    suspend fun updateLastReadAt(bookId: String, lastReadAt: Long = System.currentTimeMillis())

    /**
     * آخر الكتب المضافة
     * @param limit الحد الأقصى
     * @return قائمة الكتب
     */
    @Query("SELECT * FROM books WHERE isDownloaded = 1 ORDER BY addedAt DESC LIMIT :limit")
    fun getRecentlyAddedBooks(limit: Int = 10): Flow<List<BookEntity>>
}
