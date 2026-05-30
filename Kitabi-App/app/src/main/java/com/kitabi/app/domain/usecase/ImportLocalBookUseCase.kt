package com.kitabi.app.domain.usecase

import com.kitabi.app.domain.model.Book
import com.kitabi.app.domain.repository.BookRepository
import javax.inject.Inject

/**
 * حالة استخدام استيراد كتاب محلي
 * تتعامل مع استيراد ملفات PDF/EPUB من جهاز المستخدم
 */
class ImportLocalBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    /**
     * استيراد كتاب من مسار ملف محلي
     * @param filePath مسار الملف على الجهاز
     * @return الكتاب المستورد أو خطأ
     */
    suspend operator fun invoke(filePath: String): Result<Book> {
        return try {
            bookRepository.importLocalBook(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
