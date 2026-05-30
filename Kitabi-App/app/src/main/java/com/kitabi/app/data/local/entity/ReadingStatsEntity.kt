package com.kitabi.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان إحصائيات القراءة
 * يتتبع إحصائيات القراءة اليومية للمستخدم
 */
@Entity(tableName = "reading_stats")
data class ReadingStatsEntity(
    /** المعرف الفريد (التاريخ بصيغة yyyy-MM-dd) */
    @PrimaryKey
    val date: String,

    /** عدد الصفحات المقروءة */
    val pagesRead: Int = 0,

    /** مدة القراءة بالثواني */
    val readingTimeSeconds: Long = 0L,

    /** عدد الكتب المقروءة */
    val booksRead: Int = 0,

    /** تابع القراءة المتتالية (عدد الأيام) */
    val streak: Int = 0
)
