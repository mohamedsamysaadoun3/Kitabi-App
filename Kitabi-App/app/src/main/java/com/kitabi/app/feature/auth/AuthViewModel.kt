package com.kitabi.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitabi.app.domain.model.User
import com.kitabi.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * حالة شاشة المصادقة
 */
data class AuthUiState(
    /** هل يتم التحميل */
    val isLoading: Boolean = false,

    /** هل تم تسجيل الدخول */
    val isLoggedIn: Boolean = false,

    /** المستخدم الحالي */
    val user: User? = null,

    /** رسالة الخطأ */
    val error: String? = null
)

/**
 * نموذج عرض شاشة المصادقة
 * يدير عمليات تسجيل الدخول والخروج
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isLoggedIn = user != null,
                    error = null  // مسح الخطأ عند تغير حالة المستخدم
                )
            }
        }
    }

    /**
     * تسجيل الدخول بحساب Google
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = user
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    /**
     * تسجيل الدخول بشكل مجهول
     */
    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.signInAnonymously()
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = user
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    /**
     * تسجيل الخروج
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState()
        }
    }

    /**
     * مسح الخطأ
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
