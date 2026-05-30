package com.kitabi.app.feature.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

/**
 * شاشة المتجر
 * تعرض كتباً إلكترونية من مصادر مجانية متعددة
 */
@Composable
fun StoreScreen(
    onBookClick: (OnlineBook) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    viewModel: StoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.isLoading && uiState.featuredBooks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KitabiTheme.colors.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // عنوان المتجر
                Text(
                    text = "المتجر 🏪",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = KitabiTheme.colors.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )

                // شرائح التصنيفات
                CategoryChips(
                    categories = storeCategories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // قسم الكتب المميزة - دائرة العرض
                if (uiState.featuredBooks.isNotEmpty()) {
                    SectionHeader(title = "مميز ⭐")
                    FeaturedCarousel(
                        books = uiState.featuredBooks,
                        onBookClick = onBookClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // قسم الأكثر قراءة
                if (uiState.mostReadBooks.isNotEmpty()) {
                    SectionHeader(title = "الأكثر قراءة 📊")
                    BookHorizontalList(
                        books = uiState.mostReadBooks,
                        onBookClick = onBookClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // قسم جديد
                if (uiState.newBooks.isNotEmpty()) {
                    SectionHeader(title = "جديد 🆕")
                    BookHorizontalList(
                        books = uiState.newBooks,
                        onBookClick = onBookClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // قسم مترجم
                if (uiState.translatedBooks.isNotEmpty()) {
                    SectionHeader(title = "مترجم 🌐")
                    BookHorizontalList(
                        books = uiState.translatedBooks,
                        onBookClick = onBookClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // قسم عربي أصيل
                if (uiState.arabicOriginalBooks.isNotEmpty()) {
                    SectionHeader(title = "عربي أصيل 🇪🇬")
                    BookHorizontalList(
                        books = uiState.arabicOriginalBooks,
                        onBookClick = onBookClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // قسم ملكية عامة
                if (uiState.publicDomainBooks.isNotEmpty()) {
                    SectionHeader(title = "ملكية عامة 📜")
                    BookHorizontalList(
                        books = uiState.publicDomainBooks,
                        onBookClick = onBookClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // إشعار حقوق النشر
                Text(
                    text = "جميع الكتب في قسم ملكية عامة متاحة قانونياً للتحميل والقراءة\nحقوق الطبع والنشر محفوظة",
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

/**
 * قائمة كتب أفقية
 */
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
