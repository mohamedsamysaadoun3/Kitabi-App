package com.kitabi.app.data.remote.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kitabi.app.domain.model.ChatMessage
import com.kitabi.app.domain.model.ChatRoom
import com.kitabi.app.domain.model.MessageType
import com.kitabi.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تنفيذ مستودع المحادثات عبر Firebase Realtime Database
 * يدعم إنشاء الغرف والمشاركة وإرسال الرسائل والاستماع في الوقت الحقيقي
 */
@Singleton
class FirebaseChatRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) : ChatRepository {

    companion object {
        private const val TAG = "FirebaseChatRepository"
        private const val CHAT_ROOMS_PATH = "chat_rooms"
        private const val CHAT_MESSAGES_PATH = "chat_messages"
        private const val CHAT_MEMBERS_PATH = "chat_members"
    }

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    private val roomsListener: ValueEventListener? = null

    init {
        listenToChatRooms()
    }

    /**
     * الاستماع لغرف المحادثة في الوقت الحقيقي
     */
    private fun listenToChatRooms() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val membersRef = firebaseDatabase.getReference(CHAT_MEMBERS_PATH)

        membersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = mutableListOf<ChatRoom>()
                val roomIds = mutableSetOf<String>()

                // جمع معرفات الغرف التي ينتمي إليها المستخدم
                for (memberSnapshot in snapshot.children) {
                    val roomId = memberSnapshot.key ?: continue
                    val memberData = memberSnapshot.children.firstOrNull {
                        it.key == userId
                    }
                    if (memberData != null) {
                        roomIds.add(roomId)
                    }
                }

                // جلب بيانات الغرف
                if (roomIds.isEmpty()) {
                    _chatRooms.value = emptyList()
                    return
                }

                val roomsRef = firebaseDatabase.getReference(CHAT_ROOMS_PATH)
                roomsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(roomsSnapshot: DataSnapshot) {
                        val chatRooms = mutableListOf<ChatRoom>()
                        for (roomSnapshot in roomsSnapshot.children) {
                            val roomId = roomSnapshot.key ?: continue
                            if (roomId !in roomIds) continue

                            val name = roomSnapshot.child("name").getValue(String::class.java) ?: ""
                            val bookId = roomSnapshot.child("bookId").getValue(String::class.java) ?: ""
                            val createdBy = roomSnapshot.child("createdBy").getValue(String::class.java) ?: ""
                            val createdAt = roomSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                            val memberCount = roomSnapshot.child("memberCount").getValue(Int::class.java) ?: 0
                            val lastMessage = roomSnapshot.child("lastMessage").getValue(String::class.java) ?: ""
                            val lastMessageTime = roomSnapshot.child("lastMessageTime").getValue(Long::class.java) ?: 0L

                            chatRooms.add(
                                ChatRoom(
                                    id = roomId,
                                    name = name,
                                    bookId = bookId,
                                    createdBy = createdBy,
                                    createdAt = createdAt,
                                    memberCount = memberCount,
                                    lastMessage = lastMessage,
                                    lastMessageTime = lastMessageTime
                                )
                            )
                        }
                        _chatRooms.value = chatRooms.sortedByDescending { it.lastMessageTime }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "خطأ في جلب بيانات الغرف: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "خطأ في الاستماع لعضوية الغرف: ${error.message}")
            }
        })
    }

    override fun getChatRooms(): Flow<List<ChatRoom>> {
        return _chatRooms.asStateFlow()
    }

    override fun getChatMessages(roomId: String): Flow<List<ChatMessage>> {
        val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())

        val messagesRef = firebaseDatabase.getReference(CHAT_MESSAGES_PATH)
            .orderByChild("roomId")
            .equalTo(roomId)

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                    val senderName = child.child("senderName").getValue(String::class.java) ?: ""
                    val text = child.child("text").getValue(String::class.java) ?: ""
                    val type = child.child("type").getValue(String::class.java) ?: "TEXT"
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L

                    messages.add(
                        ChatMessage(
                            id = id,
                            roomId = roomId,
                            senderId = senderId,
                            senderName = senderName,
                            text = text,
                            type = try { MessageType.valueOf(type) } catch (_: Exception) { MessageType.TEXT },
                            timestamp = timestamp
                        )
                    )
                }
                messagesFlow.value = messages.sortedBy { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "خطأ في جلب الرسائل: ${error.message}")
            }
        })

        return messagesFlow.asStateFlow()
    }

    override suspend fun sendMessage(roomId: String, text: String) {
        try {
            val user = firebaseAuth.currentUser ?: return
            val messageRef = firebaseDatabase.getReference(CHAT_MESSAGES_PATH).push()

            val messageMap = mapOf(
                "id" to messageRef.key,
                "roomId" to roomId,
                "senderId" to user.uid,
                "senderName" to (user.displayName ?: "مستخدم"),
                "text" to text,
                "type" to MessageType.TEXT.name,
                "timestamp" to System.currentTimeMillis()
            )

            messageRef.setValue(messageMap).await()

            // تحديث آخر رسالة في الغرفة
            val roomRef = firebaseDatabase.getReference(CHAT_ROOMS_PATH).child(roomId)
            val updates = mapOf(
                "lastMessage" to text.take(50),
                "lastMessageTime" to System.currentTimeMillis()
            )
            roomRef.updateChildren(updates).await()

        } catch (e: Exception) {
            Log.e(TAG, "خطأ في إرسال الرسالة: ${e.message}")
        }
    }

    override suspend fun createChatRoom(room: ChatRoom): String {
        try {
            val roomRef = firebaseDatabase.getReference(CHAT_ROOMS_PATH).push()
            val roomId = roomRef.key ?: return ""

            val roomMap = mapOf(
                "id" to roomId,
                "name" to room.name,
                "bookId" to room.bookId,
                "createdBy" to room.createdBy,
                "createdAt" to System.currentTimeMillis(),
                "memberCount" to 1,
                "lastMessage" to "",
                "lastMessageTime" to System.currentTimeMillis()
            )

            roomRef.setValue(roomMap).await()

            // إضافة المنشئ كعضو
            val userId = firebaseAuth.currentUser?.uid ?: return roomId
            firebaseDatabase.getReference(CHAT_MEMBERS_PATH)
                .child(roomId)
                .child(userId)
                .setValue(true)
                .await()

            return roomId
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في إنشاء الغرفة: ${e.message}")
            return room.id
        }
    }

    override suspend fun joinChatRoom(roomId: String) {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return

            // إضافة المستخدم كعضو
            firebaseDatabase.getReference(CHAT_MEMBERS_PATH)
                .child(roomId)
                .child(userId)
                .setValue(true)
                .await()

            // زيادة عدد الأعضاء
            val roomRef = firebaseDatabase.getReference(CHAT_ROOMS_PATH).child(roomId)
            val snapshot = roomRef.get().await()
            val currentCount = snapshot.child("memberCount").getValue(Int::class.java) ?: 0
            roomRef.child("memberCount").setValue(currentCount + 1).await()

        } catch (e: Exception) {
            Log.e(TAG, "خطأ في الانضمام للغرفة: ${e.message}")
        }
    }

    override suspend fun leaveChatRoom(roomId: String) {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return

            // إزالة المستخدم من الأعضاء
            firebaseDatabase.getReference(CHAT_MEMBERS_PATH)
                .child(roomId)
                .child(userId)
                .removeValue()
                .await()

            // تقليل عدد الأعضاء
            val roomRef = firebaseDatabase.getReference(CHAT_ROOMS_PATH).child(roomId)
            val snapshot = roomRef.get().await()
            val currentCount = snapshot.child("memberCount").getValue(Int::class.java) ?: 1
            roomRef.child("memberCount").setValue((currentCount - 1).coerceAtLeast(0)).await()

        } catch (e: Exception) {
            Log.e(TAG, "خطأ في مغادرة الغرفة: ${e.message}")
        }
    }
}
