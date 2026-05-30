package com.kitabi.app.feature.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitabi.app.domain.model.OnlineBook
import com.kitabi.app.domain.repository.OnlineStoreRepository
import com.kitabi.app.domain.usecase.GetFeaturedBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * حالة شاشة المتجر
 */
data class StoreUiState(
    /** الكتب المميزة */
    val featuredBooks: List<OnlineBook> = emptyList(),

    /** الأكثر قراءة */
    val mostReadBooks: List<OnlineBook> = emptyList(),

    /** الكتب الجديدة */
    val newBooks: List<OnlineBook> = emptyList(),

    /** الكتب المترجمة */
    val translatedBooks: List<OnlineBook> = emptyList(),

    /** الكتب العربية الأصيلة */
    val arabicOriginalBooks: List<OnlineBook> = emptyList(),

    /** كتب ملكية عامة */
    val publicDomainBooks: List<OnlineBook> = emptyList(),

    /** نتائج البحث */
    val searchResults: List<OnlineBook> = emptyList(),

    /** التصنيف المختار */
    val selectedCategory: String = "all",

    /** هل يتم التحميل */
    val isLoading: Boolean = false,

    /** هل يتم تحميل المزيد */
    val isLoadingMore: Boolean = false,

    /** هل يتم البحث */
    val isSearching: Boolean = false,

    /** هل يتم التحديث */
    val isRefreshing: Boolean = false,

    /** رسالة الخطأ */
    val error: String? = null
)

/**
 * نموذج عرض شاشة المتجر
 * يدير بيانات واجهة مستخدم المتجر
 */
@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: OnlineStoreRepository,
    private val getFeaturedBooksUseCase: GetFeaturedBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init {
        loadStoreData()
    }

    /**
     * تحميل بيانات المتجر
     */
    fun loadStoreData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            loadStoreDataInternal()
        }
    }

    /**
     * المنطق الداخلي لتحميل البيانات (suspend)
     */
    private suspend fun loadStoreDataInternal() {
        _uiState.value = _uiState.value.copy(error = null)

        // تحميل الكتب المميزة
        getFeaturedBooksUseCase().onSuccess { featured ->
            _uiState.value = _uiState.value.copy(featuredBooks = featured)
        }.onFailure { e ->
            _uiState.value = _uiState.value.copy(error = e.message)
        }

        // تحميل الأكثر قراءة
        storeRepository.getMostReadBooks().onSuccess { books ->
            _uiState.value = _uiState.value.copy(mostReadBooks = books)
        }

        // تحميل الكتب الجديدة
        storeRepository.getNewBooks().onSuccess { books ->
            _uiState.value = _uiState.value.copy(newBooks = books)
        }

        // تحميل الكتب المترجمة
        storeRepository.getTranslatedBooks().onSuccess { books ->
            _uiState.value = _uiState.value.copy(translatedBooks = books)
        }

        // تحميل الكتب العربية الأصيلة
        storeRepository.getArabicOriginalBooks().onSuccess { books ->
            _uiState.value = _uiState.value.copy(arabicOriginalBooks = books)
        }

        // تحميل كتب ملكية عامة
        storeRepository.getPublicDomainBooks().onSuccess { books ->
            _uiState.value = _uiState.value.copy(publicDomainBooks = books)
        }

        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    /**
     * تحديث البيانات (سحب للتحديث)
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadStoreDataInternal()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    /**
     * اختيار تصنيف
     */
    fun selectCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedCategory = categoryId,
                isLoading = true
            )

            when (categoryId) {
                "all" -> loadStoreData()
                "translated" -> {
                    storeRepository.getTranslatedBooks().onSuccess { books ->
                        _uiState.value = _uiState.value.copy(
                            featuredBooks = books,
                            isLoading = false
                        )
                    }
                }
                "public_domain" -> {
                    storeRepository.getPublicDomainBooks().onSuccess { books ->
                        _uiState.value = _uiState.value.copy(
                            featuredBooks = books,
                            isLoading = false
                        )
                    }
                }
                "arabic_original" -> {
                    storeRepository.getArabicOriginalBooks().onSuccess { books ->
                        _uiState.value = _uiState.value.copy(
                            featuredBooks = books,
                            isLoading = false
                        )
                    }
                }
                else -> {
                    storeRepository.getBooksByCategory(categoryId).onSuccess { books ->
                        _uiState.value = _uiState.value.copy(
                            featuredBooks = books,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * البحث عن كتب
     */
    fun searchBooks(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isSearching = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)

            storeRepository.searchBooks(query).onSuccess { books ->
                _uiState.value = _uiState.value.copy(
                    searchResults = books,
                    isSearching = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }

    /**
     * إعادة تعيين البحث
     */
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchResults = emptyList(),
            isSearching = false
        )
    }
}
