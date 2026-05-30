package com.kitabi.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kitabi.app.domain.model.User
import com.kitabi.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تنفيذ مستودع المصادقة عبر Firebase
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser?.toUser()
        }
    }

    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override val isLoggedIn: Flow<Boolean> = MutableStateFlow(firebaseAuth.currentUser != null)

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user?.toUser() ?: throw Exception("فشل تسجيل الدخول")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<User> {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            val user = result.user?.toUser() ?: throw Exception("فشل تسجيل الدخول المجهول")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(user.displayName)
                .build()
            firebaseUser?.updateProfile(profileUpdates)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * تحويل مستخدم Firebase إلى نموذج النطاق
     */
    private fun com.google.firebase.FirebaseUser.toUser(): User {
        return User(
            id = uid,
            displayName = displayName ?: "",
            email = email ?: "",
            photoUrl = photoUrl?.toString() ?: "",
            isAnonymous = isAnonymous
        )
    }
}
