package com.kitabi.app.domain.repository

import com.kitabi.app.domain.model.Book
import kotlinx.coroutines.flow.Flow

/**
 * واجهة مستودع الكتب
 * تعرف عمليات إدارة الكتب المحلية
 */
interface BookRepository {

    /** الحصول على جميع الكتب */
    fun getAllBooks(): Flow<List<Book>>

    /** الحصول على كتاب بالمعرف */
    suspend fun getBookById(id: String): Book?

    /** الحصول على كتاب بالمعرف كـ Flow */
    fun getBookByIdFlow(id: String): Flow<Book?>

    /** إضافة كتاب جديد */
    suspend fun addBook(book: Book): Book

    /** تحديث كتاب */
    suspend fun updateBook(book: Book)

    /** حذف كتاب */
    suspend fun deleteBook(id: String)

    /** البحث في الكتب المحلية */
    fun searchBooks(query: String): Flow<List<Book>>

    /** الحصول على الكتب قيد القراءة حالياً */
    fun getCurrentlyReadingBooks(): Flow<List<Book>>

    /** الحصول على الكتب حسب التصنيف */
    fun getBooksByCategory(category: String): Flow<List<Book>>

    /** الحصول على آخر الكتب المضافة */
    fun getRecentlyAddedBooks(limit: Int = 10): Flow<List<Book>>

    /** الحصول على كتب ملكية عامة */
    fun getPublicDomainBooks(): Flow<List<Book>>

    /** الحصول على كتب مترجمة */
    fun getTranslatedBooks(): Flow<List<Book>>

    /** عدد الكتب */
    fun getBookCount(): Flow<Int>

    /** تحديث حالة التحميل */
    suspend fun updateDownloadStatus(bookId: String, isDownloaded: Boolean)

    /** تحديث تقدم التحميل */
    suspend fun updateDownloadProgress(bookId: String, progress: Int, state: String)

    /** تحديث آخر قراءة */
    suspend fun updateLastReadAt(bookId: String)

    /** استيراد كتاب من ملف محلي */
    suspend fun importLocalBook(filePath: String): Result<Book>
}
