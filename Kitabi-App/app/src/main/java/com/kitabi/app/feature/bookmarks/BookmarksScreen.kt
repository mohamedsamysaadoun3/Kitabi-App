package com.kitabi.app.feature.bookmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.data.local.entity.BookmarkEntity

/**
 * شاشة الإشارات المرجعية
 * تعرض جميع الإشارات المرجعية المحفوظة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onBookmarkClick: (String, Int) -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الإشارات المرجعية",
                        fontWeight = FontWeight.Bold,
                        color = KitabiTheme.colors.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KitabiTheme.colors.surface
                )
            )
        },
        containerColor = KitabiTheme.colors.background
    ) { paddingValues ->
        if (bookmarks.isEmpty()) {
            EmptyBookmarksState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bookmarks, key = { it.id }) { bookmark ->
                    BookmarkItem(
                        bookmark = bookmark,
                        onClick = { onBookmarkClick(bookmark.bookId, bookmark.page) },
                        onDelete = { viewModel.deleteBookmark(bookmark) }
                    )
                }
            }
        }
    }
}

/**
 * عنصر إشارة مرجعية
 */
@Composable
private fun BookmarkItem(
    bookmark: BookmarkEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // أيقونة الإشارة
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    KitabiTheme.colors.bookmark.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = null,
                tint = KitabiTheme.colors.bookmark,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // معلومات الإشارة
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "صفحة ${bookmark.page}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = KitabiTheme.colors.onSurface
            )
            if (bookmark.note.isNotEmpty()) {
                Text(
                    text = bookmark.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = KitabiTheme.colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (bookmark.selectedText.isNotEmpty()) {
                Text(
                    text = "\"${bookmark.selectedText.take(60)}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = KitabiTheme.colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // زر الحذف
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "حذف",
                tint = KitabiTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * حالة عدم وجود إشارات
 */
@Composable
private fun EmptyBookmarksState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = KitabiTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "لا توجد إشارات مرجعية",
                style = MaterialTheme.typography.titleMedium,
                color = KitabiTheme.colors.onSurfaceVariant
            )
            Text(
                text = "أضف إشارات أثناء القراءة للوصول السريع",
                style = MaterialTheme.typography.bodySmall,
                color = KitabiTheme.colors.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
