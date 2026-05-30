package com.kitabi.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * واجهة مستودع عداد المستخدمين
 * يتعقب عدد المستخدمين النشطين عبر Firebase
 */
interface UserCounterRepository {

    /** الحصول على عدد المستخدمين النشطين */
    fun getActiveUserCount(): Flow<Int>

    /** تحديث حالة النشاط */
    suspend fun updateActiveStatus(isActive: Boolean)

    /** تسجيل مستخدم جديد */
    suspend fun registerNewUser()
}
