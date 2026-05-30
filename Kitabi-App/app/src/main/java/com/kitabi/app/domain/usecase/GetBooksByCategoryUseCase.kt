package com.kitabi.app.domain.usecase

import com.kitabi.app.domain.model.OnlineBook
import com.kitabi.app.domain.repository.OnlineStoreRepository
import javax.inject.Inject

/**
 * حالة استخدام الحصول على كتب حسب التصنيف
 */
class GetBooksByCategoryUseCase @Inject constructor(
    private val storeRepository: OnlineStoreRepository
) {
    /**
     * الحصول على كتب حسب التصنيف
     * @param category التصنيف
     * @param page رقم الصفحة
     * @return قائمة الكتب أو خطأ
     */
    suspend operator fun invoke(category: String, page: Int = 1): Result<List<OnlineBook>> {
        return storeRepository.getBooksByCategory(category, page)
    }
}
