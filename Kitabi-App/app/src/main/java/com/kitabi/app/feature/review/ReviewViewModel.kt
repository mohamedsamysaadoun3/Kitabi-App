package com.kitabi.app.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kitabi.app.domain.model.Review
import com.kitabi.app.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * نموذج عرض المراجعات
 * يدير مراجعات الكتب وإضافة مراجعات جديدة
 */
@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    /** متوسط التقييم */
    private val _averageRating = MutableStateFlow(0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()

    /** عدد التقييمات */
    private val _ratingsCount = MutableStateFlow(0)
    val ratingsCount: StateFlow<Int> = _ratingsCount.asStateFlow()

    /**
     * الحصول على مراجعات كتاب
     */
    fun getReviews(bookId: String): StateFlow<List<Review>> {
        return reviewRepository.getBookReviews(bookId)
            .also { flow ->
                viewModelScope.launch {
                    flow.collect { reviews ->
                        _averageRating.value = if (reviews.isNotEmpty()) {
                            reviews.map { it.rating }.average().toFloat()
                        } else 0f
                        _ratingsCount.value = reviews.size
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /**
     * الحصول على توزيع التقييمات
     */
    fun getRatingDistribution(reviews: List<Review>): Map<Int, Int> {
        return reviews.groupingBy { it.rating }.eachCount()
    }

    /**
     * إرسال مراجعة جديدة
     */
    fun submitReview(
        bookId: String,
        rating: Int,
        title: String,
        content: String,
        onResult: (Boolean) -> Unit
    ) {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            onResult(false)
            return
        }
        val userName = firebaseAuth.currentUser?.displayName ?: "مستخدم مجهول"
        val photoUrl = firebaseAuth.currentUser?.photoUrl?.toString() ?: ""

        viewModelScope.launch {
            val result = reviewRepository.addReview(
                Review(
                    bookId = bookId,
                    userId = userId,
                    userName = userName,
                    userPhotoUrl = photoUrl,
                    rating = rating,
                    title = title,
                    content = content
                )
            )
            onResult(result.isSuccess)
        }
    }

    /**
     * الإعجاب بمراجعة
     */
    fun likeReview(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.likeReview(reviewId)
        }
    }

    /**
     * إلغاء الإعجاب بمراجعة
     */
    fun unlikeReview(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.unlikeReview(reviewId)
        }
    }
}
