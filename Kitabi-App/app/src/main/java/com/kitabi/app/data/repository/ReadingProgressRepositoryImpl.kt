package com.kitabi.app.data.repository

import com.kitabi.app.data.local.dao.ReadingProgressDao
import com.kitabi.app.data.local.entity.ReadingProgressEntity
import com.kitabi.app.domain.repository.ReadingProgress
import com.kitabi.app.domain.repository.ReadingProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تنفيذ مستودع تقدم القراءة
 * يتعامل مع تتبع تقدم قراءة الكتب عبر قاعدة البيانات المحلية
 */
@Singleton
class ReadingProgressRepositoryImpl @Inject constructor(
    private val readingProgressDao: ReadingProgressDao
) : ReadingProgressRepository {

    override suspend fun getProgress(bookId: String): ReadingProgress? {
        return readingProgressDao.getProgressByBookId(bookId)?.let { entityToDomain(it) }
    }

    override fun getProgressFlow(bookId: String): Flow<ReadingProgress?> {
        return readingProgressDao.getProgressByBookIdFlow(bookId).map { entity ->
            entity?.let { entityToDomain(it) }
        }
    }

    override fun getCurrentlyReading(): Flow<List<ReadingProgress>> {
        return readingProgressDao.getCurrentlyReading().map { entities ->
            entities.map { entityToDomain(it) }
        }
    }

    override fun getCompletedBooks(): Flow<List<ReadingProgress>> {
        return readingProgressDao.getCompletedBooks().map { entities ->
            entities.map { entityToDomain(it) }
        }
    }

    override suspend fun updateProgress(bookId: String, currentPage: Int, totalPages: Int) {
        val existing = readingProgressDao.getProgressByBookId(bookId)
        val progressPercent = if (totalPages > 0) {
            ((currentPage.toFloat() / totalPages) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        if (existing != null) {
            readingProgressDao.updateCurrentPage(bookId, currentPage, progressPercent)
        } else {
            val entity = ReadingProgressEntity(
                id = UUID.randomUUID().toString(),
                bookId = bookId,
                currentPage = currentPage,
                totalPages = totalPages,
                progressPercent = progressPercent,
                startedAt = System.currentTimeMillis()
            )
            readingProgressDao.upsertProgress(entity)
        }
    }

    override suspend fun addReadingTime(bookId: String, seconds: Long) {
        readingProgressDao.addReadingTime(bookId, seconds)
    }

    override suspend fun completeBook(bookId: String) {
        readingProgressDao.completeBook(bookId)
    }

    override fun getTotalReadingTime(): Flow<Long> {
        return readingProgressDao.getTotalReadingTime()
    }

    override fun getCompletedBooksCount(): Flow<Int> {
        return readingProgressDao.getCompletedBooksCount()
    }

    /**
     * تحويل كيان قاعدة البيانات إلى نموذج النطاق
     */
    private fun entityToDomain(entity: ReadingProgressEntity): ReadingProgress {
        return ReadingProgress(
            id = entity.id,
            bookId = entity.bookId,
            currentPage = entity.currentPage,
            totalPages = entity.totalPages,
            progressPercent = entity.progressPercent,
            readingTimeSeconds = entity.readingTimeSeconds,
            lastReadAt = entity.lastReadAt,
            startedAt = entity.startedAt,
            completedAt = entity.completedAt
        )
    }
}
