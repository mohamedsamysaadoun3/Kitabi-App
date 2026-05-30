package com.kitabi.app.domain.usecase

import com.kitabi.app.domain.model.Book
import com.kitabi.app.domain.model.BookSource
import com.kitabi.app.domain.model.OnlineBook
import com.kitabi.app.domain.repository.BookRepository
import javax.inject.Inject

/**
 * حالة استخدام تحميل كتاب من المتجر
 * تتعامل مع تحميل الكتاب وحفظه محلياً
 */
class DownloadBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    /**
     * تحميل كتاب إلكتروني
     * @param onlineBook الكتاب الإلكتروني المراد تحميله
     * @return الكتاب المحمل أو خطأ
     */
    suspend operator fun invoke(onlineBook: OnlineBook): Result<Book> {
        return try {
            // إنشاء كتاب محلي من الكتاب الإلكتروني
            val book = Book(
                title = onlineBook.title,
                author = onlineBook.author,
                source = BookSource.ONLINE,
                coverPath = onlineBook.coverUrl,
                onlineId = onlineBook.sourceId,
                downloadUrl = onlineBook.downloadUrl,
                pageCount = onlineBook.pageCount,
                category = try {
                    com.kitabi.app.domain.model.Category.valueOf(onlineBook.category)
                } catch (_: Exception) {
                    com.kitabi.app.domain.model.Category.GENERAL
                },
                rating = onlineBook.rating,
                description = onlineBook.description,
                publisher = onlineBook.publisher,
                isbn = onlineBook.isbn,
                copyright = if (onlineBook.isPublicDomain) "ملكية عامة" else onlineBook.copyright
            )

            val savedBook = bookRepository.addBook(book)
            Result.success(savedBook)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
