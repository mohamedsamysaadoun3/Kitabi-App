package com.kitabi.app.domain.repository

import com.kitabi.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * واجهة مستودع المصادقة
 * تعرف عمليات تسجيل الدخول والخروج
 */
interface AuthRepository {

    /** المستخدم الحالي */
    val currentUser: Flow<User?>

    /** هل المستخدم مسجل الدخول */
    val isLoggedIn: Flow<Boolean>

    /** تسجيل الدخول بحساب Google */
    suspend fun signInWithGoogle(idToken: String): Result<User>

    /** تسجيل الدخول بشكل مجهول */
    suspend fun signInAnonymously(): Result<User>

    /** تسجيل الخروج */
    suspend fun signOut(): Result<Unit>

    /** تحديث بيانات المستخدم */
    suspend fun updateUser(user: User): Result<Unit>

    /** حذف الحساب */
    suspend fun deleteAccount(): Result<Unit>
}
