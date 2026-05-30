package com.kitabi.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitabi.app.domain.model.User
import com.kitabi.app.domain.repository.AuthRepository
import com.kitabi.app.domain.repository.UserCounterRepository
import com.kitabi.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * نموذج عرض الإعدادات
 * يدير تفضيلات المستخدم والعداد وإعدادات الذكاء الاصطناعي
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userCounterRepository: UserCounterRepository
) : ViewModel() {

    /** المستخدم الحالي */
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** الوضع الداكن */
    val isDarkMode: StateFlow<Boolean> = userPreferencesRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** حجم خط القراءة */
    val fontSize: StateFlow<Int> = userPreferencesRepository.readerFontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 18)

    /** سمة القراءة */
    val readerTheme: StateFlow<String> = userPreferencesRepository.readerTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "light")

    /** عدد المستخدمين النشطين */
    val activeUserCount: StateFlow<Int> = userCounterRepository.getActiveUserCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** مزود الذكاء الاصطناعي المختار */
    private val _aiProvider = kotlinx.coroutines.flow.MutableStateFlow("mistral")
    val aiProvider: StateFlow<String> = _aiProvider

    /**
     * تحديث الوضع الداكن
     */
    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(isDark)
        }
    }

    /**
     * تحديث حجم الخط
     */
    fun setFontSize(size: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setReaderFontSize(size)
        }
    }

    /**
     * تحديث سمة القارئ
     */
    fun setReaderTheme(theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.setReaderTheme(theme)
        }
    }

    /**
     * تحديث مزود الذكاء الاصطناعي
     */
    fun setAiProvider(provider: String) {
        _aiProvider.value = provider
    }
}
