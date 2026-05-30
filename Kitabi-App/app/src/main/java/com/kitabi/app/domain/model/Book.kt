package com.kitabi.app.domain.model

import java.util.UUID

/**
 * نموذج الكتاب - يمثل كتاباً في تطبيق كتابي
 * يدعم الكتب المحلية والإلكترونية بصيغ مختلفة
 */
data class Book(
    /** المعرف الفريد للكتاب */
    val id: String = UUID.randomUUID().toString(),

    /** عنوان الكتاب */
    val title: String,

    /** اسم المؤلف */
    val author: String,

    /** مصدر الكتاب (محلي أو إلكتروني) */
    val source: BookSource = BookSource.LOCAL,

    /** صيغة الملف (PDF، EPUB، TXT) */
    val format: BookFormat = BookFormat.PDF,

    /** مسار صورة الغلاف */
    val coverPath: String = "",

    /** مسار ملف الكتاب على الجهاز */
    val filePath: String = "",

    /** المعرف الإلكتروني للكتاب في Firebase */
    val onlineId: String = "",

    /** رابط تحميل الكتاب */
    val downloadUrl: String = "",

    /** نسبة تقدم التحميل (0-100) */
    val downloadProgress: Int = 0,

    /** حجم الملف بالبايت */
    val fileSize: Long = 0L,

    /** عدد صفحات الكتاب */
    val pageCount: Int = 0,

    /** الصفحة الحالية التي وصل إليها القارئ */
    val currentPage: Int = 0,

    /** تاريخ آخر قراءة */
    val lastReadAt: Long? = null,

    /** هل تم تحميل الكتاب بالكامل */
    val isDownloaded: Boolean = false,

    /** تصنيف الكتاب */
    val category: Category = Category.GENERAL,

    /** تقييم الكتاب (0-5) */
    val rating: Float = 0f,

    /** وصف الكتاب */
    val description: String = "",

    /** لغة الكتاب */
    val language: Language = Language.ARABIC,

    /** دار النشر */
    val publisher: String = "",

    /** سنة النشر */
    val year: Int = 0,

    /** الرقم الدولي المعياري للكتاب */
    val isbn: String = "",

    /** حقوق النشر */
    val copyright: String = "",

    /** تاريخ إضافة الكتاب للمكتبة */
    val addedAt: Long = System.currentTimeMillis(),

    /** حالة التحميل */
    val downloadState: DownloadState = DownloadState.NOT_DOWNLOADED
) {
    /**
     * نسبة التقدم في القراءة
     * @return نسبة مئوية من 0 إلى 100
     */
    val readingProgress: Int
        get() = if (pageCount > 0) {
            ((currentPage.toFloat() / pageCount) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

    /**
     * هل تم قراءة الكتاب بالكامل
     */
    val isCompleted: Boolean
        get() = pageCount > 0 && currentPage >= pageCount

    /**
     * هل الكتاب قيد القراءة حالياً
     */
    val isCurrentlyReading: Boolean
        get() = currentPage > 0 && !isCompleted

    /**
     * هل الكتاب لم يُقرأ بعد
     */
    val isUnread: Boolean
        get() = currentPage == 0

    /**
     * حجم الملف بصيغة مقروءة
     * @return سلسلة نصية تمثل الحجم (مثل "2.5 ميجابايت")
     */
    fun getFormattedFileSize(): String {
        return when {
            fileSize < 1024 -> "$fileSize بايت"
            fileSize < 1024 * 1024 -> String.format("%.1f كيلوبايت", fileSize / 1024.0)
            fileSize < 1024 * 1024 * 1024 -> String.format("%.1f ميجابايت", fileSize / (1024.0 * 1024.0))
            else -> String.format("%.1f جيجابايت", fileSize / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
