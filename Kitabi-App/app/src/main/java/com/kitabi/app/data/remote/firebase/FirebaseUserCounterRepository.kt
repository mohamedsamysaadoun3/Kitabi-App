package com.kitabi.app.data.remote.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kitabi.app.domain.repository.UserCounterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تنفيذ مستودع عداد المستخدمين عبر Firebase
 * يستمع لعدد المستخدمين النشطين في الوقت الحقيقي
 * ويسجل حالة الاتصال والانقطاع تلقائياً
 */
@Singleton
class FirebaseUserCounterRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) : UserCounterRepository {

    companion object {
        private const val TAG = "FirebaseUserCounter"
        private const val ACTIVE_USERS_PATH = "active_users"
    }

    private val _activeUserCount = MutableStateFlow(0)

    init {
        listenToActiveUsers()
    }

    /**
     * الاستماع لعدد المستخدمين النشطين في الوقت الحقيقي
     */
    private fun listenToActiveUsers() {
        val activeUsersRef = firebaseDatabase.getReference(ACTIVE_USERS_PATH)

        activeUsersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                _activeUserCount.value = count
                Log.d(TAG, "عدد المستخدمين النشطين: $count")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "خطأ في الاستماع للمستخدمين النشطين: ${error.message}")
            }
        })
    }

    override fun getActiveUserCount(): Flow<Int> {
        return _activeUserCount.asStateFlow()
    }

    override suspend fun updateActiveStatus(isActive: Boolean) {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return
            val ref = firebaseDatabase.getReference(ACTIVE_USERS_PATH).child(userId)
            if (isActive) {
                ref.setValue(true).await()
                ref.onDisconnect().removeValue()
            } else {
                ref.removeValue().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في تحديث حالة النشاط: ${e.message}")
        }
    }

    override suspend fun registerNewUser() {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return
            firebaseDatabase.getReference("users").child(userId).child("joinedAt")
                .setValue(System.currentTimeMillis()).await()
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في تسجيل مستخدم جديد: ${e.message}")
        }
    }
}
