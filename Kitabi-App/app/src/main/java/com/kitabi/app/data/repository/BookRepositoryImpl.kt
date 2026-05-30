package com.kitabi.app.data.repository

import com.kitabi.app.data.local.dao.BookDao
import com.kitabi.app.data.mapper.BookMapper
import com.kitabi.app.domain.model.Book
import com.kitabi.app.domain.model.BookFormat
import com.kitabi.app.domain.model.BookSource
import com.kitabi.app.domain.model.Category
import com.kitabi.app.domain.model.Language
import com.kitabi.app.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تنفيذ مستودع الكتب
 * يتعامل مع الكتب المحلية عبر قاعدة البيانات
 */
@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            BookMapper.entityListToDomainList(entities)
        }
    }

    override suspend fun getBookById(id: String): Book? {
        return bookDao.getBookById(id)?.let { BookMapper.entityToDomain(it) }
    }

    override fun getBookByIdFlow(id: String): Flow<Book?> {
        return bookDao.getBookByIdFlow(id).map { entity ->
            entity?.let { BookMapper.entityToDomain(it) }
        }
    }

    override suspend fun addBook(book: Book): Book {
        val entity = BookMapper.domainToEntity(book)
        bookDao.insertBook(entity)
        return book
    }

    override suspend fun updateBook(book: Book) {
        val entity = BookMapper.domainToEntity(book)
        bookDao.updateBook(entity)
    }

    override suspend fun deleteBook(id: String) {
        bookDao.deleteBookById(id)
    }

    override fun searchBooks(query: String): Flow<List<Book>> {
        return bookDao.searchBooks(query).map { entities ->
            BookMapper.entityListToDomainList(entities)
        }
    }

    override fun getCurrentlyReadingBooks(): Flow<List<Book>> {
        return bookDao.getCurrentlyReadingBooks().map { entities ->
            BookMapper.entityListToDomainList(entities)
        }
    }

    override fun getBooksByCategory(category: String): Flow<List<Book>> {
        return bookDao.getBooksByCategory(category).map { entities ->
            BookMapper.entityListToDomainList(entities)
        }
    }

    override fun getRecentlyAddedBooks(limit: Int): Flow<List<Book>> {
        return bookDao.getRecentlyAddedBooks(limit).map { entities ->
            BookMapper.entityListToDomainList(entities)
        }
    }

    override fun getPublicDomainBooks(): Flow<List<Book>> {
        return bookDao.getPublicDomainBooks().map { entities ->
            BookMapper.entityListToDomainList(entities)
        }
    }

    override fun getTranslatedBooks(): Flow<List<Book>> {
        return bookDao.getTranslatedBooks().map { entities ->
            BookMapper.entityListToDomainList(entities)
        }
    }

    override fun getBookCount(): Flow<Int> {
        return bookDao.getBookCount()
    }

    override suspend fun updateDownloadStatus(bookId: String, isDownloaded: Boolean) {
        bookDao.updateDownloadStatus(bookId, isDownloaded)
    }

    override suspend fun updateDownloadProgress(bookId: String, progress: Int, state: String) {
        bookDao.updateDownloadProgress(bookId, progress, state)
    }

    override suspend fun updateLastReadAt(bookId: String) {
        bookDao.updateLastReadAt(bookId)
    }

    override suspend fun importLocalBook(filePath: String): Result<Book> {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return Result.failure(Exception("الملف غير موجود"))
            }

            // تحديد صيغة الملف
            val format = when (file.extension.lowercase()) {
                "pdf" -> BookFormat.PDF
                "epub" -> BookFormat.EPUB
                "txt" -> BookFormat.TXT
                else -> BookFormat.PDF
            }

            // إنشاء كتاب من الملف المحلي
            val fileName = file.nameWithoutExtension
            val book = Book(
                id = UUID.randomUUID().toString(),
                title = fileName,
                author = "غير معروف",
                source = BookSource.LOCAL,
                format = format,
                filePath = filePath,
                fileSize = file.length(),
                isDownloaded = true,
                category = Category.GENERAL,
                language = Language.ARABIC,
                downloadState = com.kitabi.app.domain.model.DownloadState.DOWNLOADED
            )

            val entity = BookMapper.domainToEntity(book)
            bookDao.insertBook(entity)
            Result.success(book)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
