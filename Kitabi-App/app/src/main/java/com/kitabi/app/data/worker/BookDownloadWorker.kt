package com.kitabi.app.data.worker

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kitabi.app.domain.model.DownloadState
import com.kitabi.app.domain.repository.BookRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * عامل تحميل الكتب في الخلفية
 * يستخدم WorkManager لتحميل ملفات الكتب من الإنترنت
 * يدعم إعادة المحاولة عند الفشل والإبلاغ عن التقدم
 * يعمل حتى بعد إغلاق التطبيق
 */
@HiltWorker
class BookDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val bookRepository: BookRepository,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "BookDownloadWorker"
        /** مفتاح معرف الكتاب */
        const val KEY_BOOK_ID = "book_id"
        /** مفتاح رابط التحميل */
        const val KEY_DOWNLOAD_URL = "download_url"
        /** مفتاح اسم الملف */
        const val KEY_FILE_NAME = "file_name"
        /** مفتاح صيغة الملف */
        const val KEY_FORMAT = "format"
        /** مفتاح التقدم */
        const val KEY_PROGRESS = "progress"
        /** مفتاح مسار الملف المحمل */
        const val KEY_FILE_PATH = "file_path"
        /** الحد الأقصى لمحاولات إعادة المحاولة */
        private const val MAX_RETRY_ATTEMPTS = 3

        /**
         * جدولة عملية تحميل كتاب
         * @param context سياق التطبيق
         * @param bookId معرف الكتاب
         * @param downloadUrl رابط التحميل
         * @param fileName اسم الملف
         * @param format صيغة الملف
         */
        fun schedule(
            context: Context,
            bookId: String,
            downloadUrl: String,
            fileName: String,
            format: String
        ) {
            val workRequest = OneTimeWorkRequestBuilder<BookDownloadWorker>()
                .setInputData(
                    workDataOf(
                        KEY_BOOK_ID to bookId,
                        KEY_DOWNLOAD_URL to downloadUrl,
                        KEY_FILE_NAME to fileName,
                        KEY_FORMAT to format
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresStorageNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "download_$bookId",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }

        /**
         * إلغاء عملية تحميل كتاب
         * @param context سياق التطبيق
         * @param bookId معرف الكتاب
         */
        fun cancel(context: Context, bookId: String) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("download_$bookId")
        }
    }

    /**
     * تنفيذ عملية التحميل
     * @return نتيجة العملية
     */
    override suspend fun doWork(): Result {
        val bookId = inputData.getString(KEY_BOOK_ID) ?: return Result.failure()
        val downloadUrl = inputData.getString(KEY_DOWNLOAD_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: "book.epub"
        val format = inputData.getString(KEY_FORMAT) ?: "epub"

        Log.d(TAG, "بدء تحميل الكتاب: $bookId - $fileName")

        // تحديث حالة التحميل
        bookRepository.updateDownloadProgress(
            bookId = bookId,
            progress = 0,
            state = DownloadState.DOWNLOADING.name
        )

        return try {
            // إنشاء عميل HTTP مع مهلة أطول للتحميل
            val downloadClient = okHttpClient.newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(downloadUrl)
                .build()

            val response = downloadClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e(TAG, "فشل الاتصال بالخادم: ${response.code}")
                return if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    bookRepository.updateDownloadProgress(
                        bookId = bookId,
                        progress = 0,
                        state = DownloadState.FAILED.name
                    )
                    Result.failure()
                }
            }

            val responseBody = response.body ?: run {
                Log.e(TAG, "استجابة فارغة من الخادم")
                return if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }

            // إنشاء مجلد الكتب
            val booksDir = File(
                applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "kitabi_books"
            )
            booksDir.mkdirs()

            // التأكد من اسم ملف فريد
            val safeFileName = sanitizeFileName(fileName, format)
            val outputFile = File(booksDir, safeFileName)

            // تحميل الملف مع تتبع التقدم
            responseBody.byteStream().use { input ->
                outputFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytes = 0L
                    val contentLength = responseBody.contentLength()

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead

                        // الإبلاغ عن التقدم
                        if (contentLength > 0) {
                            val progress = ((totalBytes * 100) / contentLength).toInt()
                                .coerceIn(0, 100)
                            setProgressAsync(workDataOf(KEY_PROGRESS to progress))

                            // تحديث التقدم في قاعدة البيانات كل 10%
                            if (progress % 10 == 0) {
                                try {
                                    bookRepository.updateDownloadProgress(
                                        bookId = bookId,
                                        progress = progress,
                                        state = DownloadState.DOWNLOADING.name
                                    )
                                } catch (_: Exception) {
                                    // تجاهل أخطاء تحديث التقدم
                                }
                            }
                        }
                    }
                }
            }

            // التحقق من اكتمال الملف
            if (outputFile.length() == 0L) {
                outputFile.delete()
                Log.e(TAG, "الملف المحمل فارغ")
                return if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }

            Log.d(TAG, "تم تحميل الكتاب بنجاح: ${outputFile.absolutePath}")

            // تحديث حالة التحميل في قاعدة البيانات
            bookRepository.updateDownloadProgress(
                bookId = bookId,
                progress = 100,
                state = DownloadState.DOWNLOADED.name
            )
            bookRepository.updateDownloadStatus(bookId, true)

            Result.success(
                workDataOf(
                    KEY_FILE_PATH to outputFile.absolutePath,
                    KEY_BOOK_ID to bookId
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "فشل تحميل الكتاب: ${e.message}")

            // تحديث حالة الفشل
            try {
                if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                    bookRepository.updateDownloadProgress(
                        bookId = bookId,
                        progress = 0,
                        state = DownloadState.FAILED.name
                    )
                }
            } catch (_: Exception) {
                // تجاهل أخطاء تحديث الحالة
            }

            return if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * تنظيف اسم الملف وإضافة الامتداد المناسب
     * @param fileName اسم الملف الأصلي
     * @param format صيغة الملف
     * @return اسم ملف آمن
     */
    private fun sanitizeFileName(fileName: String, format: String): String {
        // إزالة الأحرف غير المسموحة
        val safeName = fileName.replace(Regex("[^a-zA-Z0-9_.\\-آ-ي\\s]"), "_")
            .trim()

        // إضافة الامتداد إذا لم يكن موجوداً
        val extension = when (format.lowercase()) {
            "pdf" -> ".pdf"
            "epub" -> ".epub"
            "txt" -> ".txt"
            else -> ".epub"
        }

        return if (safeName.endsWith(extension, ignoreCase = true)) {
            safeName
        } else {
            "$safeName$extension"
        }
    }
}
