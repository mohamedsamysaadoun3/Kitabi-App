package com.kitabi.app.domain.usecase

import com.kitabi.app.domain.model.OnlineBook
import com.kitabi.app.domain.repository.OnlineStoreRepository
import javax.inject.Inject

/**
 * حالة استخدام البحث عن كتب
 * تبحث في جميع المصادر الإلكترونية المتاحة
 */
class SearchBooksUseCase @Inject constructor(
    private val storeRepository: OnlineStoreRepository
) {
    /**
     * البحث عن كتب
     * @param query نص البحث
     * @param page رقم الصفحة
     * @return قائمة الكتب أو خطأ
     */
    suspend operator fun invoke(query: String, page: Int = 1): Result<List<OnlineBook>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        return storeRepository.searchBooks(query.trim(), page)
    }
}
