package com.kitabi.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitabi.app.domain.model.Book
import com.kitabi.app.domain.model.OnlineBook
import com.kitabi.app.domain.repository.BookRepository
import com.kitabi.app.domain.usecase.SearchBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * حالة شاشة البحث
 */
data class SearchUiState(
    /** نص البحث */
    val query: String = "",

    /** التبويب النشط (0: جهازي، 1: متجر) */
    val activeTab: Int = 0,

    /** نتائج البحث المحلية */
    val localResults: List<Book> = emptyList(),

    /** نتائج البحث الإلكترونية */
    val onlineResults: List<OnlineBook> = emptyList(),

    /** عمليات البحث الأخيرة */
    val recentSearches: List<String> = emptyList(),

    /** اقتراحات البحث */
    val suggestions: List<String> = emptyList(),

    /** هل يتم البحث */
    val isSearching: Boolean = false,

    /** رسالة الخطأ */
    val error: String? = null
)

/**
 * نموذج عرض شاشة البحث
 * يدير عمليات البحث المحلية والإلكترونية
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val searchBooksUseCase: SearchBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /**
     * تحديث نص البحث
     */
    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)

        // اقتراحات البحث
        if (query.isNotBlank()) {
            val suggestions = listOf(
                "روايات عربية",
                "كتب تاريخ",
                "أدب إسلامي",
                "فلسفة",
                "علم نفس",
                "كتب أطفال"
            ).filter { it.contains(query, ignoreCase = true) }
            _uiState.value = _uiState.value.copy(suggestions = suggestions)
        } else {
            _uiState.value = _uiState.value.copy(suggestions = emptyList())
        }
    }

    /**
     * تنفيذ البحث
     */
    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)

            // إضافة للبحث الأخير
            val recentSearches = (_uiState.value.recentSearches - query).toMutableList()
            recentSearches.add(0, query)
            _uiState.value = _uiState.value.copy(
                recentSearches = recentSearches.take(10)
            )

            // البحث المحلي
            try {
                bookRepository.searchBooks(query).collect { books ->
                    _uiState.value = _uiState.value.copy(localResults = books)
                }
            } catch (_: Exception) { }

            // البحث الإلكتروني
            searchBooksUseCase(query).onSuccess { books ->
                _uiState.value = _uiState.value.copy(
                    onlineResults = books,
                    isSearching = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isSearching = false
                )
            }
        }
    }

    /**
     * تبديل التبويب
     */
    fun setTab(tab: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    /**
     * مسح البحث
     */
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            query = "",
            localResults = emptyList(),
            onlineResults = emptyList(),
            suggestions = emptyList(),
            isSearching = false,
            error = null
        )
    }

    /**
     * حذف بحث أخير
     */
    fun deleteRecentSearch(query: String) {
        _uiState.value = _uiState.value.copy(
            recentSearches = _uiState.value.recentSearches - query
        )
    }
}
