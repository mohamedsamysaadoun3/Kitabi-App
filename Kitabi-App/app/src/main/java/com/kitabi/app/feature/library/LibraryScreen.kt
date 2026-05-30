package com.kitabi.app.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.Book
import com.kitabi.app.domain.model.Category

/**
 * شاشة المكتبة الرئيسية
 * تعرض كتب المستخدم مع تقدم القراءة والإحصائيات
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToReader: (String) -> Unit = {},
    onImportBook: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onImportBook,
                containerColor = KitabiTheme.colors.primary,
                contentColor = KitabiTheme.colors.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "استيراد كتاب"
                )
            }
        },
        containerColor = KitabiTheme.colors.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = KitabiTheme.colors.primary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // ترويسة الترحيب
                LibraryHeader(
                    bookCount = uiState.bookCount,
                    completedBooksCount = uiState.completedBooksCount
                )

                // شريط البحث
                SearchBarRow(onClick = onNavigateToSearch)

                // شرائح التصنيفات
                CategoryChips(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )

                // قسم القراءة الحالية
                if (uiState.currentlyReading.isNotEmpty()) {
                    CurrentlyReadingSection(
                        books = uiState.currentlyReading,
                        onBookClick = { onNavigateToReader(it.id) }
                    )
                }

                // قسم أضيف مؤخراً
                if (uiState.recentlyAdded.isNotEmpty()) {
                    RecentlyAddedSection(
                        books = uiState.recentlyAdded,
                        isGridView = uiState.isGridView,
                        onBookClick = { onNavigateToReader(it.id) }
                    )
                }

                // حالة المكتبة الفارغة
                if (uiState.allBooks.isEmpty()) {
                    EmptyLibraryState()
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * ترويسة المكتبة مع إحصائيات القراءة
 */
@Composable
private fun LibraryHeader(
    bookCount: Int,
    completedBooksCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "مرحباً 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = KitabiTheme.colors.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "لديك $bookCount كتاب في مكتبتك • أكملت $completedBooksCount كتاب",
            style = MaterialTheme.typography.bodyMedium,
            color = KitabiTheme.colors.onSurfaceVariant
        )
    }
}

/**
 * شريط البحث
 */
@Composable
private fun SearchBarRow(onClick: () -> Unit) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        placeholder = {
            Text(
                text = "ابحث في مكتبتك...",
                textAlign = TextAlign.Right
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "بحث"
            )
        },
        enabled = false,
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

/**
 * شرائح التصنيفات الأفقية
 */
@Composable
private fun CategoryChips(
    selectedCategory: Category,
    onCategorySelected: (Category) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val categories = listOf(
            Category.GENERAL to "📚 الكل",
            Category.LITERATURE to "📖 روايات",
            Category.HISTORY to "🏛️ تاريخ",
            Category.PHILOSOPHY to "🤔 فلسفة",
            Category.PSYCHOLOGY to "🧠 علم نفس",
            Category.SCIENCE to "🔬 علوم",
            Category.TECHNOLOGY to "💻 تقنية",
            Category.RELIGION to "🕌 دين",
            Category.POETRY to "✍️ شعر",
            Category.CHILDREN to "🧒 أطفال",
            Category.BUSINESS to "💼 أعمال",
            Category.EDUCATION to "🎓 تعليم"
        )

        items(categories) { (category, label) ->
            val isSelected = category == selectedCategory
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category) },
                shape = RoundedCornerShape(24.dp),
                color = if (isSelected) {
                    KitabiTheme.colors.primary
                } else {
                    KitabiTheme.colors.surfaceVariant
                },
                contentColor = if (isSelected) {
                    KitabiTheme.colors.onPrimary
                } else {
                    KitabiTheme.colors.onSurfaceVariant
                }
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * قسم القراءة الحالية
 */
@Composable
private fun CurrentlyReadingSection(
    books: List<Book>,
    onBookClick: (Book) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "يتم القراءة الآن 📖",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.onBackground
            )
            TextButton(onClick = { /* عرض الكل */ }) {
                Text(text = "عرض الكل")
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(books) { book ->
                CurrentlyReadingCard(
                    book = book,
                    onClick = { onBookClick(book) }
                )
            }
        }
    }
}

/**
 * بطاقة الكتاب قيد القراءة
 */
@Composable
private fun CurrentlyReadingCard(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = KitabiTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // صورة الغلاف
            AsyncImage(
                model = book.coverPath.ifBlank { null },
                contentDescription = book.title,
                modifier = Modifier
                    .size(80.dp, 120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // تفاصيل الكتاب
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = KitabiTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = KitabiTheme.colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // شريط التقدم
                Column {
                    LinearProgressIndicator(
                        progress = { book.readingProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = KitabiTheme.colors.primary,
                        trackColor = KitabiTheme.colors.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${book.readingProgress}% • صفحة ${book.currentPage} من ${book.pageCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = KitabiTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * قسم أضيف مؤخراً
 */
@Composable
private fun RecentlyAddedSection(
    books: List<Book>,
    isGridView: Boolean,
    onBookClick: (Book) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "أضيف مؤخراً 🆕",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.onBackground
            )
            IconButton(onClick = { /* toggle view */ }) {
                Icon(
                    imageVector = if (isGridView) Icons.Filled.List else Icons.Filled.GridView,
                    contentDescription = if (isGridView) "عرض قائمة" else "عرض شبكة",
                    tint = KitabiTheme.colors.onSurfaceVariant
                )
            }
        }

        // عرض شبكة للكتب
        if (isGridView) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height((((books.size / 3) + 1) * 220).dp)
            ) {
                items(books) { book ->
                    BookGridItem(
                        book = book,
                        onClick = { onBookClick(book) }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                books.forEach { book ->
                    BookListItem(
                        book = book,
                        onClick = { onBookClick(book) }
                    )
                }
            }
        }
    }
}

/**
 * عنصر كتاب في عرض الشبكة
 */
@Composable
private fun BookGridItem(
    book: Book,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        // صورة الغلاف
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = KitabiTheme.colors.surfaceVariant)
        ) {
            AsyncImage(
                model = book.coverPath.ifBlank { null },
                contentDescription = book.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.67f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = book.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = KitabiTheme.colors.onSurface,
            textAlign = TextAlign.Right
        )
        Text(
            text = book.author,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = KitabiTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Right
        )
    }
}

/**
 * عنصر كتاب في عرض القائمة
 */
@Composable
private fun BookListItem(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = KitabiTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    color = KitabiTheme.colors.onSurfaceVariant,
                    maxLines = 1
                )
                if (book.rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = KitabiTheme.colors.ratingStar
                        )
                        Text(
                            text = String.format("%.1f", book.rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = KitabiTheme.colors.ratingStar
                        )
                    }
                }
            }
        }
    }
}

/**
 * حالة المكتبة الفارغة
 */
@Composable
private fun EmptyLibraryState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📚",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "مكتبتك فارغة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ابدأ بإضافة كتب من المتجر أو استيراد كتب من جهازك",
                style = MaterialTheme.typography.bodyMedium,
                color = KitabiTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}
