package com.kitabi.app.domain.model

import java.util.UUID

/**
 * نموذج المستخدم - يمثل مستخدم تطبيق كتابي
 * يدعم المستخدمين المسجلين والمجهولين
 */
data class User(
    /** المعرف الفريد للمستخدم */
    val id: String = UUID.randomUUID().toString(),

    /** اسم العرض */
    val displayName: String = "",

    /** البريد الإلكتروني */
    val email: String = "",

    /** رابط الصورة الشخصية */
    val photoUrl: String = "",

    /** هل المستخدم مجهول (لم يسجل حساباً) */
    val isAnonymous: Boolean = true,

    /** تاريخ الانضمام */
    val joinedAt: Long = System.currentTimeMillis(),

    /** سجل القراءة المتتالية (عدد الأيام) */
    val readingStreak: Int = 0,

    /** عدد الكتب المقروءة بالكامل */
    val booksRead: Int = 0,

    /** إجمالي الصفحات المقروءة */
    val totalPagesRead: Int = 0,

    /** التصنيف المفضل */
    val favoriteGenre: Category = Category.GENERAL
) {
    /**
     * مستوى القارئ بناءً على عدد الكتب المقروءة
     */
    val readerLevel: ReaderLevel
        get() = when {
            booksRead >= 100 -> ReaderLevel.MASTER
            booksRead >= 50 -> ReaderLevel.EXPERT
            booksRead >= 25 -> ReaderLevel.ADVANCED
            booksRead >= 10 -> ReaderLevel.INTERMEDIATE
            booksRead >= 3 -> ReaderLevel.BEGINNER
            else -> ReaderLevel.NEWCOMER
        }

    /**
     * اسم مستوى القارئ بالعربية
     */
    val readerLevelName: String
        get() = when (readerLevel) {
            ReaderLevel.NEWCOMER -> "قارئ جديد"
            ReaderLevel.BEGINNER -> "قارئ مبتدئ"
            ReaderLevel.INTERMEDIATE -> "قارئ متوسط"
            ReaderLevel.ADVANCED -> "قارئ متقدم"
            ReaderLevel.EXPERT -> "قارئ خبير"
            ReaderLevel.MASTER -> "قارئ متمرس"
        }
}

/**
 * مستوى القارئ
 */
enum class ReaderLevel {
    /** قارئ جديد */
    NEWCOMER,

    /** قارئ مبتدئ */
    BEGINNER,

    /** قارئ متوسط */
    INTERMEDIATE,

    /** قارئ متقدم */
    ADVANCED,

    /** قارئ خبير */
    EXPERT,

    /** قارئ متمرس */
    MASTER
}
