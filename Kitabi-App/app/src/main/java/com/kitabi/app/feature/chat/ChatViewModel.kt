package com.kitabi.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kitabi.app.domain.model.ChatMessage
import com.kitabi.app.domain.model.ChatRoom
import com.kitabi.app.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * نموذج عرض المحادثات
 * يدير غرف المحادثة والرسائل والاتصال
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    /** غرف المحادثة */
    val chatRooms: StateFlow<List<ChatRoom>> = chatRepository.getChatRooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** عدد المتصلين */
    private val _onlineCount = MutableStateFlow(0)
    val onlineCount: StateFlow<Int> = _onlineCount.asStateFlow()

    /** معرف المستخدم الحالي */
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    /**
     * الحصول على رسائل غرفة محادثة
     */
    fun getMessages(roomId: String): StateFlow<List<ChatMessage>> {
        return chatRepository.getChatMessages(roomId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /**
     * الحصول على معلومات الغرفة
     */
    fun getRoomInfo(roomId: String): StateFlow<ChatRoom?> {
        return chatRooms.map { rooms ->
            rooms.find { it.id == roomId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    /**
     * إرسال رسالة
     */
    fun sendMessage(roomId: String, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            chatRepository.sendMessage(roomId, text.trim())
        }
    }

    /**
     * هل الرسالة من المستخدم الحالي
     */
    fun isCurrentUser(senderId: String): Boolean {
        return senderId == currentUserId
    }
}
