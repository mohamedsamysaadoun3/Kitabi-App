package com.kitabi.app.feature.store.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.OnlineBook

/**
 * دائرة عرض الكتب المميزة
 * تعرض كتب مميزة في شريط أفقي مع تأثيرات متحركة
 */
@Composable
fun FeaturedCarousel(
    books: List<OnlineBook>,
    onBookClick: (OnlineBook) -> Unit,
    modifier: Modifier = Modifier
) {
    if (books.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(books.take(10)) { book ->
            FeaturedBookCard(
                book = book,
                onClick = { onBookClick(book) }
            )
        }
    }
}

/**
 * بطاقة الكتاب المميز
 */
@Composable
private fun FeaturedBookCard(
    book: OnlineBook,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(320.dp)
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = KitabiTheme.colors.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // صورة الغلاف
            AsyncImage(
                model = book.coverUrl.ifBlank { null },
                contentDescription = book.title,
                modifier = Modifier
                    .height(148.dp)
                    .width(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // تفاصيل الكتاب
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = KitabiTheme.colors.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = KitabiTheme.colors.onPrimaryContainer.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column {
                    // تقييم
                    if (book.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⭐",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", book.rating),
                                style = MaterialTheme.typography.labelMedium,
                                color = KitabiTheme.colors.onPrimaryContainer
                            )
                        }
                    }

                    // شارة التصنيف
                    if (book.isPublicDomain) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KitabiTheme.colors.tertiaryContainer
                        ) {
                            Text(
                                text = "📜 ملكية عامة",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = KitabiTheme.colors.onTertiaryContainer
                            )
                        }
                    }

                    if (book.isTranslated) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KitabiTheme.colors.secondaryContainer
                        ) {
                            Text(
                                text = "🌐 مترجم",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = KitabiTheme.colors.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
