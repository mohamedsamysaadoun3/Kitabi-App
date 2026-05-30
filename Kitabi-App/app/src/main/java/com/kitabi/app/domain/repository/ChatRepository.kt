package com.kitabi.app.domain.repository

import com.kitabi.app.domain.model.ChatMessage
import com.kitabi.app.domain.model.ChatRoom
import kotlinx.coroutines.flow.Flow

/**
 * واجهة مستودع المحادثات
 * تعرف عمليات إدارة غرف ورسائل المحادثات
 */
interface ChatRepository {

    /** الحصول على جميع غرف المحادثة */
    fun getChatRooms(): Flow<List<ChatRoom>>

    /** الحصول على رسائل غرفة محادثة */
    fun getChatMessages(roomId: String): Flow<List<ChatMessage>>

    /** إرسال رسالة */
    suspend fun sendMessage(roomId: String, text: String)

    /** إنشاء غرفة محادثة جديدة */
    suspend fun createChatRoom(room: ChatRoom): String

    /** الانضمام لغرفة محادثة */
    suspend fun joinChatRoom(roomId: String)

    /** مغادرة غرفة محادثة */
    suspend fun leaveChatRoom(roomId: String)
}
