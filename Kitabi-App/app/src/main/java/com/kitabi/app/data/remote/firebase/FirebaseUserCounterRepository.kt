package com.kitabi.app.data.remote.firebase

import com.google.firebase.database.FirebaseDatabase
import com.kitabi.app.domain.repository.UserCounterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تنفيذ مستودع عداد المستخدمين عبر Firebase
 */
@Singleton
class FirebaseUserCounterRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) : UserCounterRepository {

    private val _activeUserCount = MutableStateFlow(0)

    override fun getActiveUserCount(): Flow<Int> {
        return _activeUserCount.asStateFlow()
    }

    override suspend fun updateActiveStatus(isActive: Boolean) {
        try {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
            val ref = firebaseDatabase.getReference("active_users").child(userId)
            if (isActive) {
                ref.setValue(true).await()
                ref.onDisconnect().removeValue()
            } else {
                ref.removeValue().await()
            }
        } catch (_: Exception) { }
    }

    override suspend fun registerNewUser() {
        try {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
            firebaseDatabase.getReference("users").child(userId).child("joinedAt")
                .setValue(System.currentTimeMillis()).await()
        } catch (_: Exception) { }
    }
}
