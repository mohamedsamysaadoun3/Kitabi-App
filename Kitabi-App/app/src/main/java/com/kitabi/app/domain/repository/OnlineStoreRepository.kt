package com.kitabi.app.domain.repository

import com.kitabi.app.domain.model.OnlineBook
import kotlinx.coroutines.flow.Flow

/**
 * واجهة مستودع المتجر الإلكتروني
 * تعرف عمليات البحث والتصفح في المتاجر الإلكترونية
 */
interface OnlineStoreRepository {

    /** البحث عن كتب في المتجر */
    suspend fun searchBooks(query: String, page: Int = 1): Result<List<OnlineBook>>

    /** الحصول على كتب مميزة */
    suspend fun getFeaturedBooks(): Result<List<OnlineBook>>

    /** الحصول على كتب حسب التصنيف */
    suspend fun getBooksByCategory(category: String, page: Int = 1): Result<List<OnlineBook>>

    /** الحصول على كتب الأكثر قراءة */
    suspend fun getMostReadBooks(page: Int = 1): Result<List<OnlineBook>>

    /** الحصول على الكتب الجديدة */
    suspend fun getNewBooks(page: Int = 1): Result<List<OnlineBook>>

    /** الحصول على كتب مترجمة */
    suspend fun getTranslatedBooks(page: Int = 1): Result<List<OnlineBook>>

    /** الحصول على كتب عربية أصيلة */
    suspend fun getArabicOriginalBooks(page: Int = 1): Result<List<OnlineBook>>

    /** الحصول على كتب ملكية عامة */
    suspend fun getPublicDomainBooks(page: Int = 1): Result<List<OnlineBook>>

    /** الحصول على تفاصيل كتاب */
    suspend fun getBookDetail(sourceId: String, source: String): Result<OnlineBook>

    /** الحصول على كتب مقترحة */
    fun getSuggestedBooks(): Flow<List<OnlineBook>>
}
