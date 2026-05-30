package com.kitabi.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.kitabi.app.domain.model.Review
import com.kitabi.app.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تنفيذ مستودع المراجعات عبر Firebase Firestore
 */
@Singleton
class FirebaseReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReviewRepository {

    override fun getBookReviews(bookId: String): Flow<List<Review>> {
        val flow = MutableStateFlow<List<Review>>(emptyList())
        firestore.collection("reviews")
            .whereEqualTo("bookId", bookId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ReviewFirestoreModel::class.java)?.toReview(doc.id)
                } ?: emptyList()
                flow.value = reviews
            }
        return flow.asStateFlow()
    }

    override suspend fun addReview(review: Review): Result<Unit> {
        return try {
            val model = ReviewFirestoreModel.fromReview(review)
            firestore.collection("reviews").add(model).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReview(review: Review): Result<Unit> {
        return try {
            val model = ReviewFirestoreModel.fromReview(review)
            firestore.collection("reviews").document(review.id).set(model).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            firestore.collection("reviews").document(reviewId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likeReview(reviewId: String): Result<Unit> {
        return try {
            firestore.collection("reviews").document(reviewId)
                .update("likesCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlikeReview(reviewId: String): Result<Unit> {
        return try {
            firestore.collection("reviews").document(reviewId)
                .update("likesCount", com.google.firebase.firestore.FieldValue.increment(-1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * نموذج مراجعة لـ Firestore
     */
    data class ReviewFirestoreModel(
        val bookId: String = "",
        val userId: String = "",
        val userName: String = "",
        val userPhotoUrl: String = "",
        val rating: Int = 0,
        val title: String = "",
        val content: String = "",
        val likesCount: Int = 0,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    ) {
        fun toReview(id: String) = Review(
            id = id,
            bookId = bookId,
            userId = userId,
            userName = userName,
            userPhotoUrl = userPhotoUrl,
            rating = rating,
            title = title,
            content = content,
            likesCount = likesCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        companion object {
            fun fromReview(review: Review) = ReviewFirestoreModel(
                bookId = review.bookId,
                userId = review.userId,
                userName = review.userName,
                userPhotoUrl = review.userPhotoUrl,
                rating = review.rating,
                title = review.title,
                content = review.content,
                likesCount = review.likesCount,
                createdAt = review.createdAt,
                updatedAt = review.updatedAt
            )
        }
    }
}
