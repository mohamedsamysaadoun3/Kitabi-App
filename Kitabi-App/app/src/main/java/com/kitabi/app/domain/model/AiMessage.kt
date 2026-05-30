package com.kitabi.app.domain.model

import java.util.UUID

/**
 * نموذج رسالة الذكاء الاصطناعي
 */
data class AiMessage(
    /** المعرف الفريد */
    val id: String = UUID.randomUUID().toString(),

    /** معرف المحادثة */
    val conversationId: String,

    /** معرف الكتاب المرتبط */
    val bookId: String,

    /** نص الرسالة */
    val content: String,

    /** هل الرسالة من المستخدم */
    val isFromUser: Boolean,

    /** ميزة الذكاء الاصطناعي المستخدمة */
    val feature: AiFeature = AiFeature.Q_AND_A,

    /** وقت الإنشاء */
    val timestamp: Long = System.currentTimeMillis()
)
