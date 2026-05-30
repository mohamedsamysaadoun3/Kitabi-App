package com.kitabi.app.feature.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitabi.app.data.local.dao.BookmarkDao
import com.kitabi.app.data.local.entity.BookmarkEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * نموذج عرض الإشارات المرجعية
 * يدير قائمة الإشارات وحذفها
 */
@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : ViewModel() {

    /** قائمة جميع الإشارات المرجعية */
    val bookmarks: StateFlow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * حذف إشارة مرجعية
     */
    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmark(bookmark)
        }
    }

    /**
     * الحصول على إشارات كتاب معين
     */
    fun getBookmarksForBook(bookId: String): StateFlow<List<BookmarkEntity>> {
        return bookmarkDao.getBookmarksForBook(bookId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
