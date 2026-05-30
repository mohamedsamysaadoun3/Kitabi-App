package com.kitabi.app.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kitabi.app.data.worker.BookDownloadWorker
import com.kitabi.app.domain.model.DownloadState
import com.kitabi.app.domain.repository.BookRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

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

/**
 * تنفيذ مستودع التحميل
 * يستخدم WorkManager لعمليات التحميل في الخلفية
 * يراقب حالة التحميل وتقدمه
 */
@Singleton
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookRepository: BookRepository
) : DownloadRepository {

    companion object {
        private const val TAG = "DownloadRepository"
    }

    /**
     * بدء تحميل كتاب
     * @param bookId معرف الكتاب
     * @param downloadUrl رابط التحميل
     * @param fileName اسم الملف
     * @param format صيغة الملف
     */
    override suspend fun downloadBook(
        bookId: String,
        downloadUrl: String,
        fileName: String,
        format: String
    ) {
        Log.d(TAG, "بدء تحميل الكتاب: $bookId")

        // تحديث حالة الكتاب إلى "قيد التحميل"
        bookRepository.updateDownloadProgress(
            bookId = bookId,
            progress = 0,
            state = DownloadState.DOWNLOADING.name
        )

        // جدولة عملية التحميل
        BookDownloadWorker.schedule(
            context = context,
            bookId = bookId,
            downloadUrl = downloadUrl,
            fileName = fileName,
            format = format
        )
    }

    /**
     * إلغاء تحميل كتاب
     * @param bookId معرف الكتاب
     */
    override suspend fun cancelDownload(bookId: String) {
        Log.d(TAG, "إلغاء تحميل الكتاب: $bookId")

        BookDownloadWorker.cancel(context, bookId)

        // تحديث حالة الكتاب
        bookRepository.updateDownloadProgress(
            bookId = bookId,
            progress = 0,
            state = DownloadState.PAUSED.name
        )
    }

    /**
     * مراقبة حالة التحميل
     * @param bookId معرف الكتاب
     * @return تدفق حالة التحميل
     */
    override fun observeDownloadState(bookId: String): Flow<DownloadState> {
        val workName = "download_$bookId"
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(workName)
            .asFlow()
            .map { workInfos ->
                val workInfo = workInfos.firstOrNull()
                when {
                    workInfo == null -> DownloadState.NOT_DOWNLOADED
                    workInfo.state == WorkInfo.State.RUNNING -> DownloadState.DOWNLOADING
                    workInfo.state == WorkInfo.State.SUCCEEDED -> DownloadState.DOWNLOADED
                    workInfo.state == WorkInfo.State.FAILED -> DownloadState.FAILED
                    workInfo.state == WorkInfo.State.CANCELLED -> DownloadState.PAUSED
                    workInfo.state == WorkInfo.State.ENQUEUED -> DownloadState.NOT_DOWNLOADED
                    workInfo.state == WorkInfo.State.BLOCKED -> DownloadState.NOT_DOWNLOADED
                    else -> DownloadState.NOT_DOWNLOADED
                }
            }
    }

    /**
     * مراقبة تقدم التحميل
     * @param bookId معرف الكتاب
     * @return تدفق نسبة التقدم (0-100)
     */
    override fun observeDownloadProgress(bookId: String): Flow<Int> {
        val workName = "download_$bookId"
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(workName)
            .asFlow()
            .map { workInfos ->
                val workInfo = workInfos.firstOrNull()
                workInfo?.progress?.getInt(BookDownloadWorker.KEY_PROGRESS, 0) ?: 0
            }
    }

    /**
     * الحصول على حالة التحميل الحالية
     * @param bookId معرف الكتاب
     * @return حالة التحميل
     */
    override suspend fun getDownloadState(bookId: String): DownloadState {
        val book = bookRepository.getBookById(bookId)
        return when {
            book == null -> DownloadState.NOT_DOWNLOADED
            book.isDownloaded -> DownloadState.DOWNLOADED
            book.downloadState == DownloadState.DOWNLOADING -> DownloadState.DOWNLOADING
            book.downloadState == DownloadState.FAILED -> DownloadState.FAILED
            book.downloadState == DownloadState.PAUSED -> DownloadState.PAUSED
            else -> DownloadState.NOT_DOWNLOADED
        }
    }
}

/**
 * تحويل LiveData إلى Flow
 * دالة امتداد لتحويل أي LiveData إلى Flow
 */
private fun <T> LiveData<T>.asFlow(): Flow<T> = callbackFlow {
    val observer = Observer<T> { value ->
        trySend(value ?: return@Observer)
    }
    observeForever(observer)
    awaitClose {
        removeObserver(observer)
    }
}
