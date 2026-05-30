package com.kitabi.app.domain.model

import java.util.UUID

/**
 * نموذج مراجعة الكتاب
 */
data class Review(
    /** المعرف الفريد */
    val id: String = UUID.randomUUID().toString(),

    /** معرف الكتاب */
    val bookId: String,

    /** معرف المستخدم */
    val userId: String,

    /** اسم المستخدم */
    val userName: String,

    /** صورة المستخدم */
    val userPhotoUrl: String = "",

    /** التقييم (1-5) */
    val rating: Int,

    /** عنوان المراجعة */
    val title: String = "",

    /** نص المراجعة */
    val content: String = "",

    /** عدد الإعجابات */
    val likesCount: Int = 0,

    /** هل أعجب المستخدم الحالي */
    val isLikedByCurrentUser: Boolean = false,

    /** تاريخ الإنشاء */
    val createdAt: Long = System.currentTimeMillis(),

    /** تاريخ التحديث */
    val updatedAt: Long = System.currentTimeMillis()
)
