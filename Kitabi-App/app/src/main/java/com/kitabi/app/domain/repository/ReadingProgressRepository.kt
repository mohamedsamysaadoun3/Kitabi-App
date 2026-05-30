package com.kitabi.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * واجهة مستودع تقدم القراءة
 * تعرف عمليات تتبع تقدم قراءة الكتب
 */
interface ReadingProgressRepository {

    /** الحصول على تقدم كتاب معين */
    suspend fun getProgress(bookId: String): ReadingProgress?

    /** الحصول على تقدم كتاب كـ Flow */
    fun getProgressFlow(bookId: String): Flow<ReadingProgress?>

    /** الحصول على الكتب قيد القراءة حالياً */
    fun getCurrentlyReading(): Flow<List<ReadingProgress>>

    /** الحصول على الكتب المكتملة */
    fun getCompletedBooks(): Flow<List<ReadingProgress>>

    /** تحديث تقدم القراءة */
    suspend fun updateProgress(bookId: String, currentPage: Int, totalPages: Int)

    /** إضافة وقت قراءة */
    suspend fun addReadingTime(bookId: String, seconds: Long)

    /** إكمال كتاب */
    suspend fun completeBook(bookId: String)

    /** إجمالي وقت القراءة */
    fun getTotalReadingTime(): Flow<Long>

    /** عدد الكتب المكتملة */
    fun getCompletedBooksCount(): Flow<Int>
}

/**
 * نموذج تقدم القراءة
 */
data class ReadingProgress(
    val id: String,
    val bookId: String,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val progressPercent: Int = 0,
    val readingTimeSeconds: Long = 0L,
    val lastReadAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null
)
