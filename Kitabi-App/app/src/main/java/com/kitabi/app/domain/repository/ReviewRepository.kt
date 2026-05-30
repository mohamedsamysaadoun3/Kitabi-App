package com.kitabi.app.domain.repository

import com.kitabi.app.domain.model.Review
import kotlinx.coroutines.flow.Flow

/**
 * واجهة مستودع المراجعات
 * تعرف عمليات إدارة مراجعات الكتب
 */
interface ReviewRepository {

    /** الحصول على مراجعات كتاب */
    fun getBookReviews(bookId: String): Flow<List<Review>>

    /** إضافة مراجعة */
    suspend fun addReview(review: Review): Result<Unit>

    /** تحديث مراجعة */
    suspend fun updateReview(review: Review): Result<Unit>

    /** حذف مراجعة */
    suspend fun deleteReview(reviewId: String): Result<Unit>

    /** الإعجاب بمراجعة */
    suspend fun likeReview(reviewId: String): Result<Unit>

    /** إلغاء الإعجاب بمراجعة */
    suspend fun unlikeReview(reviewId: String): Result<Unit>
}
