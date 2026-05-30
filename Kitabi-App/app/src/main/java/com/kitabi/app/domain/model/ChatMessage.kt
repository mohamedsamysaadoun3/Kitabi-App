package com.kitabi.app.domain.model

import java.util.UUID

/**
 * نموذج رسالة المحادثة
 */
data class ChatMessage(
    /** المعرف الفريد */
    val id: String = UUID.randomUUID().toString(),

    /** معرف الغرفة */
    val roomId: String,

    /** معرف المرسل */
    val senderId: String,

    /** اسم المرسل */
    val senderName: String,

    /** صورة المرسل */
    val senderPhotoUrl: String = "",

    /** نص الرسالة */
    val text: String = "",

    /** نوع الرسالة */
    val messageType: MessageType = MessageType.TEXT,

    /** رابط الصورة (إذا كانت رسالة صورة) */
    val imageUrl: String = "",

    /** اقتباس من كتاب */
    val bookQuote: String = "",

    /** معرف الكتاب المقترح */
    val suggestedBookId: String = "",

    /** وقت الإرسال */
    val timestamp: Long = System.currentTimeMillis(),

    /** هل تمت قراءة الرسالة */
    val isRead: Boolean = false
)
