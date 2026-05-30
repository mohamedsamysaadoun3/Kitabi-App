package com.kitabi.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * كيان تقدم القراءة
 * يتتبع تقدم المستخدم في قراءة كل كتاب
 */
@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId"], name = "index_reading_progress_book_id"),
        Index(value = ["lastReadAt"], name = "index_reading_progress_last_read")
    ]
)
data class ReadingProgressEntity(
    /** المعرف الفريد */
    @PrimaryKey
    val id: String,

    /** معرف الكتاب */
    val bookId: String,

    /** الصفحة الحالية */
    val currentPage: Int = 0,

    /** إجمالي الصفحات */
    val totalPages: Int = 0,

    /** نسبة التقدم (0-100) */
    val progressPercent: Int = 0,

    /** مدة القراءة بالثواني */
    val readingTimeSeconds: Long = 0L,

    /** تاريخ آخر قراءة */
    val lastReadAt: Long = System.currentTimeMillis(),

    /** تاريخ بدء القراءة */
    val startedAt: Long? = null,

    /** تاريخ إكمال القراءة */
    val completedAt: Long? = null
)
