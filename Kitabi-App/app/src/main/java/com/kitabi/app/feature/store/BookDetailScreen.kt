package com.kitabi.app.feature.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.OnlineBook

/**
 * شاشة تفاصيل الكتاب الإلكتروني
 * تعرض معلومات مفصلة عن الكتاب مع خيارات التحميل والقراءة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    source: String,
    onBack: () -> Unit,
    onReadBook: () -> Unit,
    viewModel: StoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // البحث عن الكتاب في القوائم المتاحة
    val book = remember(uiState, bookId) {
        uiState.featuredBooks.find { it.sourceId == bookId && it.source == source }
            ?: uiState.mostReadBooks.find { it.sourceId == bookId && it.source == source }
            ?: uiState.newBooks.find { it.sourceId == bookId && it.source == source }
            ?: uiState.translatedBooks.find { it.sourceId == bookId && it.source == source }
            ?: uiState.arabicOriginalBooks.find { it.sourceId == bookId && it.source == source }
            ?: uiState.publicDomainBooks.find { it.sourceId == bookId && it.source == source }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = KitabiTheme.colors.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* bookmark */ }) {
                        Icon(
                            imageVector = Icons.Filled.BookmarkBorder,
                            contentDescription = "حفظ",
                            tint = KitabiTheme.colors.onSurface
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
        if (book != null) {
            BookDetailContent(
                book = book,
                onReadBook = onReadBook,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // محاولة تحميل الكتاب
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = KitabiTheme.colors.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "جاري تحميل تفاصيل الكتاب...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KitabiTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookDetailContent(
    book: OnlineBook,
    onReadBook: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // قسم الغلاف والمعلومات الأساسية
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    KitabiTheme.colors.primary.copy(alpha = 0.08f)
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // صورة الغلاف
                AsyncImage(
                    model = book.coverUrl.ifBlank { null },
                    contentDescription = book.title,
                    modifier = Modifier
                        .size(140.dp, 200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // المعلومات الأساسية
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = KitabiTheme.colors.onBackground,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyLarge,
                        color = KitabiTheme.colors.primary,
                        fontWeight = FontWeight.Medium
                    )

                    if (book.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = KitabiTheme.colors.ratingStar
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", book.rating),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = KitabiTheme.colors.ratingStar
                            )
                            if (book.ratingsCount > 0) {
                                Text(
                                    text = " (${book.ratingsCount})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KitabiTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // شارات
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (book.isPublicDomain) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = KitabiTheme.colors.tertiaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Public,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = KitabiTheme.colors.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ملكية عامة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = KitabiTheme.colors.onTertiaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        if (book.isTranslated) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = KitabiTheme.colors.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Translate,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = KitabiTheme.colors.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "مترجم",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = KitabiTheme.colors.onSecondaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (book.pageCount > 0) {
                        Text(
                            text = "${book.pageCount} صفحة",
                            style = MaterialTheme.typography.bodySmall,
                            color = KitabiTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // أزرار التحكم
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (book.isDownloadable) {
                Button(
                    onClick = { /* download */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KitabiTheme.colors.primary,
                        contentColor = KitabiTheme.colors.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تحميل الكتاب",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (book.previewUrl.isNotBlank()) {
                OutlinedButton(
                    onClick = onReadBook,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "معاينة الكتاب",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // وصف الكتاب
        if (book.hasDescription) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "نبذة عن الكتاب",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KitabiTheme.colors.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = KitabiTheme.colors.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f
                )
            }
        }

        // معلومات إضافية
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "معلومات إضافية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = KitabiTheme.colors.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (book.publisher.isNotBlank()) {
                        InfoRow(label = "دار النشر", value = book.publisher)
                    }
                    if (book.publishYear > 0) {
                        InfoRow(label = "سنة النشر", value = book.publishYear.toString())
                    }
                    if (book.language.isNotBlank()) {
                        InfoRow(label = "اللغة", value = when(book.language) {
                            "ar" -> "العربية"
                            "en" -> "الإنجليزية"
                            "fr" -> "الفرنسية"
                            else -> book.language
                        })
                    }
                    if (book.isbn.isNotBlank()) {
                        InfoRow(label = "ISBN", value = book.isbn)
                    }
                    if (book.downloadCount > 0) {
                        InfoRow(label = "التحميلات", value = "${book.downloadCount}")
                    }
                }
            }
        }

        // التصنيفات
        if (book.subjects.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "التصنيفات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KitabiTheme.colors.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    book.subjects.take(8).forEach { subject ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = KitabiTheme.colors.surfaceVariant
                        ) {
                            Text(
                                text = subject,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = KitabiTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = KitabiTheme.colors.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = KitabiTheme.colors.onSurface
        )
    }
}
