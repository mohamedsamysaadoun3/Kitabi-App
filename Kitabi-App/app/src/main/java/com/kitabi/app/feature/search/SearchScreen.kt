package com.kitabi.app.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.Book
import com.kitabi.app.domain.model.OnlineBook

/**
 * شاشة البحث
 * تدعم البحث المحلي والإلكتروني مع تبويبات
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBookClick: (String) -> Unit = {},
    onOnlineBookClick: (OnlineBook) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("جهازي 📱", "متجر 🏪")

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // شريط البحث
        SearchBar(
            query = uiState.query,
            onQueryChange = { viewModel.updateQuery(it) },
            onSearch = { viewModel.search() },
            onClear = { viewModel.clearSearch() }
        )

        // التبويبات
        TabRow(
            selectedTabIndex = uiState.activeTab,
            containerColor = KitabiTheme.colors.background,
            contentColor = KitabiTheme.colors.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.activeTab == index,
                    onClick = { viewModel.setTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (uiState.activeTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // محتوى البحث
        if (uiState.isSearching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KitabiTheme.colors.primary)
            }
        } else if (uiState.query.isBlank()) {
            // البحث الأخير والاقتراحات
            RecentSearchesAndSuggestions(
                recentSearches = uiState.recentSearches,
                suggestions = uiState.suggestions,
                onRecentSearchClick = { query ->
                    viewModel.updateQuery(query)
                    viewModel.search()
                },
                onDeleteRecentSearch = { viewModel.deleteRecentSearch(it) },
                onSuggestionClick = { query ->
                    viewModel.updateQuery(query)
                    viewModel.search()
                }
            )
        } else {
            // نتائج البحث
            when (uiState.activeTab) {
                0 -> LocalSearchResults(
                    books = uiState.localResults,
                    onBookClick = onBookClick
                )
                1 -> OnlineSearchResults(
                    books = uiState.onlineResults,
                    onBookClick = onOnlineBookClick
                )
            }
        }
    }
}

/**
 * شريط البحث
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        placeholder = {
            Text(
                text = "ابحث عن كتاب...",
                textAlign = TextAlign.Right
            )
        },
        leadingIcon = {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "بحث"
                )
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "مسح"
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

/**
 * البحث الأخير والاقتراحات
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentSearchesAndSuggestions(
    recentSearches: List<String>,
    suggestions: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onDeleteRecentSearch: (String) -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // عمليات البحث الأخيرة
        if (recentSearches.isNotEmpty()) {
            Text(
                text = "البحث الأخير",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn {
                items(recentSearches) { search ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRecentSearchClick(search) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = KitabiTheme.colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = search,
                                style = MaterialTheme.typography.bodyMedium,
                                color = KitabiTheme.colors.onSurface
                            )
                        }
                        IconButton(
                            onClick = { onDeleteRecentSearch(search) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "حذف",
                                modifier = Modifier.size(16.dp),
                                tint = KitabiTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // اقتراحات البحث
        if (suggestions.isNotEmpty()) {
            Text(
                text = "اقتراحات",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { suggestion ->
                    Surface(
                        modifier = Modifier.clickable { onSuggestionClick(suggestion) },
                        shape = RoundedCornerShape(16.dp),
                        color = KitabiTheme.colors.surfaceVariant
                    ) {
                        Text(
                            text = suggestion,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = KitabiTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // اقتراحات شائعة ثابتة
        if (recentSearches.isEmpty() && suggestions.isEmpty()) {
            Text(
                text = "اقتراحات شائعة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val popularSuggestions = listOf(
                "روايات عربية", "كتب تاريخ", "أدب إسلامي",
                "فلسفة", "علم نفس", "كتب أطفال",
                "تطوير ذات", "أعمال", "شعر عربي"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                popularSuggestions.forEach { suggestion ->
                    Surface(
                        modifier = Modifier.clickable {
                            onSuggestionClick(suggestion)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = KitabiTheme.colors.surfaceVariant
                    ) {
                        Text(
                            text = suggestion,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = KitabiTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * نتائج البحث المحلية
 */
@Composable
private fun LocalSearchResults(
    books: List<Book>,
    onBookClick: (String) -> Unit
) {
    if (books.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📱", style = MaterialTheme.typography.displaySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "لا توجد كتب على جهازك",
                    style = MaterialTheme.typography.bodyLarge,
                    color = KitabiTheme.colors.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(books) { book ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookClick(book.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KitabiTheme.colors.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        AsyncImage(
                            model = book.coverPath.ifBlank { null },
                            contentDescription = book.title,
                            modifier = Modifier
                                .size(56.dp, 80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = KitabiTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.bodySmall,
                                color = KitabiTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * نتائج البحث الإلكترونية
 */
@Composable
private fun OnlineSearchResults(
    books: List<OnlineBook>,
    onBookClick: (OnlineBook) -> Unit
) {
    if (books.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🔍", style = MaterialTheme.typography.displaySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ابحث عن كتب في المتجر",
                    style = MaterialTheme.typography.bodyLarge,
                    color = KitabiTheme.colors.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(books) { book ->
                Card(
                    modifier = Modifier.clickable { onBookClick(book) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KitabiTheme.colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        AsyncImage(
                            model = book.coverUrl.ifBlank { null },
                            contentDescription = book.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = KitabiTheme.colors.onSurface
                            )
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.labelSmall,
                                color = KitabiTheme.colors.onSurfaceVariant,
                                maxLines = 1
                            )
                            if (book.isPublicDomain) {
                                Text(
                                    text = "📜 ملكية عامة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KitabiTheme.colors.tertiary
                                )
                            }
                            if (book.isTranslated) {
                                Text(
                                    text = "🌐 مترجم",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KitabiTheme.colors.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
