package com.kitabi.app.domain.usecase

import com.kitabi.app.domain.model.OnlineBook
import com.kitabi.app.domain.repository.OnlineStoreRepository
import javax.inject.Inject

/**
 * حالة استخدام الحصول على كتب مميزة
 */
class GetFeaturedBooksUseCase @Inject constructor(
    private val storeRepository: OnlineStoreRepository
) {
    /**
     * الحصول على كتب مميزة
     * @return قائمة الكتب المميزة أو خطأ
     */
    suspend operator fun invoke(): Result<List<OnlineBook>> {
        return storeRepository.getFeaturedBooks()
    }
}
