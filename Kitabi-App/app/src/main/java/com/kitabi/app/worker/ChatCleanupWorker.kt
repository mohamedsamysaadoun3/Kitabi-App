package com.kitabi.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

/**
 * عامل تنظيف المحادثات الدوري
 * يحذف الرسائل الأقدم من 30 يوماً
 * ويحافظ على حد أقصى 200 رسالة لكل غرفة
 * يعمل كل 24 ساعة
 */
@HiltWorker
class ChatCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ChatCleanupWorker"
        private const val MAX_MESSAGES_PER_ROOM = 200
        private const val MESSAGE_RETENTION_DAYS = 30L
    }

    /**
     * تنفيذ عملية التنظيف
     */
    override suspend fun doWork(): Result {
        Log.d(TAG, "بدء تنظيف المحادثات")

        return try {
            val userId = firebaseAuth.currentUser?.uid
            if (userId == null) {
                Log.w(TAG, "لا يوجد مستخدم مسجل الدخول، تخطي التنظيف")
                return Result.success()
            }

            // حساب الوقت الأقدم المسموح به (قبل 30 يوماً)
            val cutoffTime = System.currentTimeMillis() - (MESSAGE_RETENTION_DAYS * 24 * 60 * 60 * 1000)

            // حذف الرسائل القديمة
            cleanupOldMessages(cutoffTime)

            // تقييد عدد الرسائل لكل غرفة
            limitMessagesPerRoom()

            Log.d(TAG, "تم تنظيف المحادثات بنجاح")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "فشل تنظيف المحادثات: ${e.message}")
            Result.retry()
        }
    }

    /**
     * حذف الرسائل الأقدم من الوقت المحدد
     */
    private suspend fun cleanupOldMessages(cutoffTime: Long) {
        try {
            val messagesRef = firebaseDatabase.getReference("chat_messages")
            val snapshot = messagesRef.orderByChild("timestamp").endAt(cutoffTime.toDouble()).get().await()

            if (snapshot.exists()) {
                val updates = mutableMapOf<String, Any?>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    updates[key] = null
                }
                if (updates.isNotEmpty()) {
                    messagesRef.updateChildren(updates)
                    Log.d(TAG, "تم حذف ${updates.size} رسالة قديمة")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في حذف الرسائل القديمة: ${e.message}")
        }
    }

    /**
     * تقييد عدد الرسائل لكل غرفة
     */
    private suspend fun limitMessagesPerRoom() {
        try {
            val roomsRef = firebaseDatabase.getReference("chat_rooms")
            val roomsSnapshot = roomsRef.get().await()

            if (roomsSnapshot.exists()) {
                for (roomSnapshot in roomsSnapshot.children) {
                    val roomId = roomSnapshot.key ?: continue
                    val messagesRef = firebaseDatabase.getReference("chat_messages")
                    val messagesSnapshot = messagesRef
                        .orderByChild("roomId")
                        .equalTo(roomId)
                        .get()
                        .await()

                    val messageCount = messagesSnapshot.childrenCount.toInt()
                    if (messageCount > MAX_MESSAGES_PER_ROOM) {
                        // ترتيب الرسائل حسب الوقت وحذف الأقدم
                        val sortedMessages = messagesSnapshot.children
                            .sortedBy { it.child("timestamp").getValue(Long::class.java) ?: 0L }

                        val messagesToDelete = sortedMessages
                            .take(messageCount - MAX_MESSAGES_PER_ROOM)

                        val updates = mutableMapOf<String, Any?>()
                        for (msg in messagesToDelete) {
                            val key = msg.key ?: continue
                            updates[key] = null
                        }
                        if (updates.isNotEmpty()) {
                            messagesRef.updateChildren(updates)
                            Log.d(TAG, "تم حذف ${updates.size} رسالة من الغرفة $roomId")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في تقييد عدد الرسائل: ${e.message}")
        }
    }
}
