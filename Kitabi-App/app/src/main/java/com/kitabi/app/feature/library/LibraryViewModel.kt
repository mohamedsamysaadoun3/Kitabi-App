package com.kitabi.app.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitabi.app.domain.model.Book
import com.kitabi.app.domain.model.Category
import com.kitabi.app.domain.repository.BookRepository
import com.kitabi.app.domain.repository.ReadingProgress
import com.kitabi.app.domain.repository.ReadingProgressRepository
import com.kitabi.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * حالة شاشة المكتبة
 */
data class LibraryUiState(
    /** الكتب قيد القراءة حالياً */
    val currentlyReading: List<Book> = emptyList(),

    /** آخر الكتب المضافة */
    val recentlyAdded: List<Book> = emptyList(),

    /** الكتب المميزة */
    val featured: List<Book> = emptyList(),

    /** جميع الكتب */
    val allBooks: List<Book> = emptyList(),

    /** تقدم القراءة */
    val readingProgress: Map<String, ReadingProgress> = emptyMap(),

    /** التصنيف المختار */
    val selectedCategory: Category = Category.GENERAL,

    /** هل عرض الشبكة */
    val isGridView: Boolean = true,

    /** هل يتم التحميل */
    val isLoading: Boolean = false,

    /** رسالة الخطأ */
    val error: String? = null,

    /** عدد الكتب */
    val bookCount: Int = 0,

    /** إجمالي وقت القراءة بالثواني */
    val totalReadingTime: Long = 0L,

    /** عدد الكتب المكتملة */
    val completedBooksCount: Int = 0
)

/**
 * نموذج عرض شاشة المكتبة
 * يدير بيانات واجهة مستخدم المكتبة الرئيسية
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibraryData()
    }

    /**
     * تحميل بيانات المكتبة
     */
    private fun loadLibraryData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // دمج التدفقات الرئيسية
            combine(
                bookRepository.getAllBooks(),
                bookRepository.getCurrentlyReadingBooks(),
                bookRepository.getRecentlyAddedBooks(10),
                userPreferencesRepository.isGridView,
                userPreferencesRepository.selectedCategory
            ) { allBooks, currentlyReading, recentlyAdded, isGridView, selectedCategory ->
                LibraryUiState(
                    allBooks = allBooks,
                    currentlyReading = currentlyReading,
                    recentlyAdded = recentlyAdded,
                    isGridView = isGridView,
                    selectedCategory = try { Category.valueOf(selectedCategory) } catch (_: Exception) { Category.GENERAL },
                    isLoading = false,
                    bookCount = allBooks.size
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = LibraryUiState(isLoading = true)
            ).collect { state ->
                _uiState.value = state
            }
        }

        // تحميل الإحصائيات بشكل منفصل
        viewModelScope.launch {
            readingProgressRepository.getTotalReadingTime().collect { time ->
                _uiState.value = _uiState.value.copy(totalReadingTime = time)
            }
        }
        viewModelScope.launch {
            readingProgressRepository.getCompletedBooksCount().collect { count ->
                _uiState.value = _uiState.value.copy(completedBooksCount = count)
            }
        }
    }

    /**
     * تبديل وضع العرض (شبكة/قائمة)
     */
    fun toggleViewMode() {
        viewModelScope.launch {
            userPreferencesRepository.setGridView(!_uiState.value.isGridView)
        }
    }

    /**
     * اختيار تصنيف
     */
    fun selectCategory(category: Category) {
        viewModelScope.launch {
            userPreferencesRepository.setSelectedCategory(category.name)
        }
    }

    /**
     * تحديث آخر قراءة
     */
    fun updateLastRead(bookId: String) {
        viewModelScope.launch {
            bookRepository.updateLastReadAt(bookId)
        }
    }

    /**
     * حذف كتاب
     */
    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            bookRepository.deleteBook(bookId)
        }
    }
}
