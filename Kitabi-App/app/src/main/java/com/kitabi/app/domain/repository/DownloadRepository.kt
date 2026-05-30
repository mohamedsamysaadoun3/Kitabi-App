package com.kitabi.app.domain.repository

import com.kitabi.app.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow

/**
 * واجهة مستودع التحميل
 * تعرف عمليات تحميل الكتب
 */
interface DownloadRepository {

    /** تحميل كتاب من رابط */
    suspend fun downloadBook(bookId: String, downloadUrl: String, fileName: String, format: String)

    /** إلغاء تحميل كتاب */
    suspend fun cancelDownload(bookId: String)

    /** مراقبة حالة التحميل */
    fun observeDownloadState(bookId: String): Flow<DownloadState>

    /** مراقبة تقدم التحميل */
    fun observeDownloadProgress(bookId: String): Flow<Int>

    /** الحصول على حالة التحميل الحالية */
    suspend fun getDownloadState(bookId: String): DownloadState
}
