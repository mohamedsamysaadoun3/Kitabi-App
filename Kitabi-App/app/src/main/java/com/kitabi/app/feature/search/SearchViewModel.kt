package com.kitabi.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitabi.app.domain.model.Book
import com.kitabi.app.domain.model.OnlineBook
import com.kitabi.app.domain.repository.BookRepository
import com.kitabi.app.domain.usecase.SearchBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val activeTab: Int = 1,  // Default to online store tab
    val localResults: List<Book> = emptyList(),
    val onlineResults: List<OnlineBook> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false  // Track if user has searched
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val searchBooksUseCase: SearchBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        // Auto-search with 500ms debounce
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        _searchQuery.value = query

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                suggestions = emptyList(),
                onlineResults = emptyList(),
                localResults = emptyList(),
                hasSearched = false
            )
        } else {
            // Dynamic suggestions based on common Arabic book categories
            val allSuggestions = listOf(
                "روايات عربية", "كتب تاريخ", "أدب إسلامي",
                "فلسفة", "علم نفس", "كتب أطفال",
                "تطوير ذات", "أعمال", "شعر عربي",
                "كتب طبخ", "رحلات", "سياسة",
                "أندرسن", "شكسبير", "نجيب محفوظ",
                "طه حسين", "العقاد", "المتنبي"
            )
            val filtered = allSuggestions.filter { 
                it.contains(query, ignoreCase = true) 
            }.take(6)
            _uiState.value = _uiState.value.copy(suggestions = filtered)
        }
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        searchJob?.cancel()
        performSearch(query)
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null, hasSearched = true)

            // Add to recent searches
            val recentSearches = (_uiState.value.recentSearches - query).toMutableList()
            recentSearches.add(0, query)
            _uiState.value = _uiState.value.copy(recentSearches = recentSearches.take(10))

            // Search locally
            try {
                bookRepository.searchBooks(query).collect { books ->
                    _uiState.value = _uiState.value.copy(localResults = books)
                }
            } catch (_: Exception) { }

            // Search online
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

    fun setTab(tab: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            query = "",
            localResults = emptyList(),
            onlineResults = emptyList(),
            suggestions = emptyList(),
            isSearching = false,
            error = null,
            hasSearched = false
        )
        _searchQuery.value = ""
    }

    fun deleteRecentSearch(query: String) {
        _uiState.value = _uiState.value.copy(
            recentSearches = _uiState.value.recentSearches - query
        )
    }
}
