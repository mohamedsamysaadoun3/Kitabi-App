package com.kitabi.app.domain.model

import java.util.UUID

/**
 * نموذج غرفة المحادثة
 */
data class ChatRoom(
    /** المعرف الفريد */
    val id: String = UUID.randomUUID().toString(),

    /** اسم الغرفة */
    val name: String,

    /** وصف الغرفة */
    val description: String = "",

    /** معرف الكتاب المرتبط (اختياري) */
    val bookId: String? = null,

    /** صورة الغرفة */
    val imageUrl: String = "",

    /** عدد الأعضاء */
    val memberCount: Int = 0,

    /** آخر رسالة */
    val lastMessage: String = "",

    /** وقت آخر رسالة */
    val lastMessageAt: Long = 0L,

    /** هل الغرفة عامة */
    val isPublic: Boolean = true,

    /** تاريخ الإنشاء */
    val createdAt: Long = System.currentTimeMillis()
)
