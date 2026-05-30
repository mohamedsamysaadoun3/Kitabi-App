package com.kitabi.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * كيان الكتاب في قاعدة البيانات المحلية
 * يمثل كتاباً محفوظاً في مكتبة المستخدم
 */
@Entity(
    tableName = "books",
    indices = [
        Index(value = ["title"], name = "index_books_title"),
        Index(value = ["author"], name = "index_books_author"),
        Index(value = ["category"], name = "index_books_category"),
        Index(value = ["lastReadAt"], name = "index_books_last_read"),
        Index(value = ["addedAt"], name = "index_books_added_at"),
        Index(value = ["isDownloaded"], name = "index_books_downloaded")
    ]
)
data class BookEntity(
    /** المعرف الفريد للكتاب */
    @PrimaryKey
    val id: String,

    /** عنوان الكتاب */
    val title: String,

    /** اسم المؤلف */
    val author: String,

    /** مصدر الكتاب (محلي أو إلكتروني) */
    val source: String = "LOCAL",

    /** صيغة الملف (PDF، EPUB، TXT) */
    val format: String = "PDF",

    /** مسار صورة الغلاف */
    val coverPath: String = "",

    /** رابط صورة الغلاف من الإنترنت */
    val coverUrl: String = "",

    /** مسار ملف الكتاب على الجهاز */
    val filePath: String = "",

    /** المعرف الإلكتروني للكتاب */
    val onlineId: String = "",

    /** مصدر الكتاب الإلكتروني (openlibrary, google, gutenberg) */
    val onlineSource: String = "",

    /** رابط تحميل الكتاب */
    val downloadUrl: String = "",

    /** نسبة تقدم التحميل (0-100) */
    val downloadProgress: Int = 0,

    /** حجم الملف بالبايت */
    val fileSize: Long = 0L,

    /** عدد صفحات الكتاب */
    val pageCount: Int = 0,

    /** تاريخ آخر قراءة (طابع زمني) */
    val lastReadAt: Long? = null,

    /** هل تم تحميل الكتاب بالكامل */
    val isDownloaded: Boolean = false,

    /** تصنيف الكتاب */
    val category: String = "GENERAL",

    /** تقييم الكتاب (0-5) */
    val rating: Float = 0f,

    /** وصف الكتاب */
    val description: String = "",

    /** لغة الكتاب */
    val language: String = "ARABIC",

    /** هل الكتاب مترجم */
    val isTranslated: Boolean = false,

    /** دار النشر */
    val publisher: String = "",

    /** سنة النشر */
    val year: Int = 0,

    /** الرقم الدولي المعياري للكتاب */
    val isbn: String = "",

    /** حقوق النشر */
    val copyright: String = "",

    /** هل الكتاب ملكية عامة */
    val isPublicDomain: Boolean = false,

    /** تاريخ إضافة الكتاب للمكتبة */
    val addedAt: Long = System.currentTimeMillis(),

    /** حالة التحميل */
    val downloadState: String = "NOT_DOWNLOADED"
)
