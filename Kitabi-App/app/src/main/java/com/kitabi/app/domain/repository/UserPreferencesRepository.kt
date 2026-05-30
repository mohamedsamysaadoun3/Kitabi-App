package com.kitabi.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * واجهة مستودع تفضيلات المستخدم
 * تعرف عمليات إدارة إعدادات التطبيق
 */
interface UserPreferencesRepository {

    /** الوضع الداكن */
    val isDarkMode: Flow<Boolean>

    /** لغة التطبيق */
    val appLanguage: Flow<String>

    /** حجم خط القراءة */
    val readerFontSize: Flow<Int>

    /** موضوع القراءة */
    val readerTheme: Flow<String>

    /** هل عرض الشبكة */
    val isGridView: Flow<Boolean>

    /** التصنيف المختار */
    val selectedCategory: Flow<String>

    /** مزود الذكاء الاصطناعي */
    val aiProvider: Flow<String>

    /** تحديث الوضع الداكن */
    suspend fun setDarkMode(isDark: Boolean)

    /** تحديث لغة التطبيق */
    suspend fun setAppLanguage(language: String)

    /** تحديث حجم خط القراءة */
    suspend fun setReaderFontSize(size: Int)

    /** تحديث موضوع القراءة */
    suspend fun setReaderTheme(theme: String)

    /** تحديث وضع العرض */
    suspend fun setGridView(isGrid: Boolean)

    /** تحديث التصنيف المختار */
    suspend fun setSelectedCategory(category: String)

    /** تحديث مزود الذكاء الاصطناعي */
    suspend fun setAiProvider(provider: String)
}
