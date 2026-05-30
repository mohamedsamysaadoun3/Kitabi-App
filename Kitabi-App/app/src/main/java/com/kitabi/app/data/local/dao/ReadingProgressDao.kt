package com.kitabi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kitabi.app.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * كائن الوصول لبيانات تقدم القراءة
 * يوفر عمليات تتبع تقدم قراءة الكتب
 */
@Dao
interface ReadingProgressDao {

    /**
     * إدراج أو تحديث تقدم القراءة
     * @param progress كيان تقدم القراءة
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ReadingProgressEntity)

    /**
     * الحصول على تقدم كتاب معين
     * @param bookId معرف الكتاب
     * @return كيان تقدم القراءة أو null
     */
    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    suspend fun getProgressByBookId(bookId: String): ReadingProgressEntity?

    /**
     * الحصول على تقدم كتاب معين كـ Flow
     * @param bookId معرف الكتاب
     * @return تدفق كيان تقدم القراءة
     */
    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    fun getProgressByBookIdFlow(bookId: String): Flow<ReadingProgressEntity?>

    /**
     * الحصول على الكتب قيد القراءة حالياً
     * @return تدفق قائمة تقدم القراءة
     */
    @Query("SELECT * FROM reading_progress WHERE completedAt IS NULL ORDER BY lastReadAt DESC")
    fun getCurrentlyReading(): Flow<List<ReadingProgressEntity>>

    /**
     * الحصول على الكتب المكتملة
     * @return تدفق قائمة تقدم القراءة المكتمل
     */
    @Query("SELECT * FROM reading_progress WHERE completedAt IS NOT NULL ORDER BY completedAt DESC")
    fun getCompletedBooks(): Flow<List<ReadingProgressEntity>>

    /**
     * تحديث الصفحة الحالية
     * @param bookId معرف الكتاب
     * @param currentPage الصفحة الحالية
     * @param progressPercent نسبة التقدم
     */
    @Query("UPDATE reading_progress SET currentPage = :currentPage, progressPercent = :progressPercent, lastReadAt = :lastReadAt WHERE bookId = :bookId")
    suspend fun updateCurrentPage(bookId: String, currentPage: Int, progressPercent: Int, lastReadAt: Long = System.currentTimeMillis())

    /**
     * تحديث مدة القراءة
     * @param bookId معرف الكتاب
     * @param additionalSeconds الثواني الإضافية
     */
    @Query("UPDATE reading_progress SET readingTimeSeconds = readingTimeSeconds + :additionalSeconds WHERE bookId = :bookId")
    suspend fun addReadingTime(bookId: String, additionalSeconds: Long)

    /**
     * إكمال كتاب
     * @param bookId معرف الكتاب
     * @param completedAt طابع زمني الإكمال
     */
    @Query("UPDATE reading_progress SET completedAt = :completedAt, progressPercent = 100 WHERE bookId = :bookId")
    suspend fun completeBook(bookId: String, completedAt: Long = System.currentTimeMillis())

    /**
     * إجمالي مدة القراءة
     * @return إجمالي الثواني
     */
    @Query("SELECT COALESCE(SUM(readingTimeSeconds), 0) FROM reading_progress")
    fun getTotalReadingTime(): Flow<Long>

    /**
     * عدد الكتب المكتملة
     * @return عدد الكتب
     */
    @Query("SELECT COUNT(*) FROM reading_progress WHERE completedAt IS NOT NULL")
    fun getCompletedBooksCount(): Flow<Int>
}
