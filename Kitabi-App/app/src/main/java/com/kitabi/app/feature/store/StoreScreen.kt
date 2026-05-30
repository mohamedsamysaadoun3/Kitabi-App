package com.kitabi.app.feature.store

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.OnlineBook
import com.kitabi.app.feature.store.components.BookCard
import com.kitabi.app.feature.store.components.CategoryChips
import com.kitabi.app.feature.store.components.FeaturedCarousel
import com.kitabi.app.feature.store.components.SectionHeader
import com.kitabi.app.feature.store.components.storeCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    onBookClick: (OnlineBook) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    viewModel: StoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "\u0627\u0644\u0645\u062A\u062C\u0631",
                        fontWeight = FontWeight.Bold,
                        color = KitabiTheme.colors.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadStoreData() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "\u062A\u062D\u062F\u064A\u062B",
                            tint = KitabiTheme.colors.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KitabiTheme.colors.surface.copy(alpha = 0.95f)
                )
            )
        },
        containerColor = KitabiTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.featuredBooks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(color = KitabiTheme.colors.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "\u062C\u0627\u0631\u064A \u062A\u062D\u062F\u064A\u062B...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KitabiTheme.colors.onSurfaceVariant
                            )
                        } else {
                            CircularProgressIndicator(color = KitabiTheme.colors.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "\u062C\u0627\u0631\u064A \u062A\u062D\u0645\u064A\u0644 \u0627\u0644\u0643\u062A\u0628...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KitabiTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Error banner
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.error?.let { error ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = KitabiTheme.colors.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ErrorOutline,
                                        contentDescription = null,
                                        tint = KitabiTheme.colors.onErrorContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KitabiTheme.colors.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.loadStoreData() }) {
                                        Text("\u0625\u0639\u0627\u062F\u0629")
                                    }
                                }
                            }
                        }
                    }

                    // Category chips
                    CategoryChips(
                        categories = storeCategories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Featured
                    if (uiState.featuredBooks.isNotEmpty()) {
                        SectionHeader(title = "\u0645\u0645\u064A\u0632 \u2B50")
                        FeaturedCarousel(
                            books = uiState.featuredBooks,
                            onBookClick = onBookClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Most read
                    if (uiState.mostReadBooks.isNotEmpty()) {
                        SectionHeader(title = "\u0627\u0644\u0623\u0643\u062B\u0631 \u0642\u0631\u0627\u0621\u0629 \uD83D\uDCCA")
                        BookHorizontalList(
                            books = uiState.mostReadBooks,
                            onBookClick = onBookClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // New
                    if (uiState.newBooks.isNotEmpty()) {
                        SectionHeader(title = "\u062C\u062F\u064A\u062F \uD83C\uDD95")
                        BookHorizontalList(
                            books = uiState.newBooks,
                            onBookClick = onBookClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Translated
                    if (uiState.translatedBooks.isNotEmpty()) {
                        SectionHeader(title = "\u0645\u062A\u0631\u062C\u0645 \uD83C\uDF10")
                        BookHorizontalList(
                            books = uiState.translatedBooks,
                            onBookClick = onBookClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Arabic original
                    if (uiState.arabicOriginalBooks.isNotEmpty()) {
                        SectionHeader(title = "\u0639\u0631\u0628\u064A \u0623\u0635\u064A\u0644 \uD83C\uDDEA\uD83C\uDDEC")
                        BookHorizontalList(
                            books = uiState.arabicOriginalBooks,
                            onBookClick = onBookClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Public domain
                    if (uiState.publicDomainBooks.isNotEmpty()) {
                        SectionHeader(title = "\u0645\u0644\u0643\u064A\u0629 \u0639\u0627\u0645\u0629 \uD83D\uDCDC")
                        BookHorizontalList(
                            books = uiState.publicDomainBooks,
                            onBookClick = onBookClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Copyright notice
                    Text(
                        text = "\u062C\u0645\u064A\u0639 \u0627\u0644\u0643\u062A\u0628 \u0641\u064A \u0642\u0633\u0645 \u0645\u0644\u0643\u064A\u0629 \u0639\u0627\u0645\u0629 \u0645\u062A\u0627\u062D\u0629 \u0642\u0627\u0646\u0648\u0646\u064A\u0627\u064B",
                        style = MaterialTheme.typography.labelSmall,
                        color = KitabiTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun BookHorizontalList(
    books: List<OnlineBook>,
    onBookClick: (OnlineBook) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books) { book ->
            BookCard(
                book = book,
                onClick = onBookClick
            )
        }
    }
}
