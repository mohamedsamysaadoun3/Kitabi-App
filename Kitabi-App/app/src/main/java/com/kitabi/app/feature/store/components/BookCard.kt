package com.kitabi.app.feature.store.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.OnlineBook

/**
 * بطاقة الكتاب الغنية
 * تعرض صورة الغلاف والعنوان والمؤلف والتقييم والتصنيف
 */
@Composable
fun BookCard(
    book: OnlineBook,
    onClick: (OnlineBook) -> Unit,
    onDownloadClick: (OnlineBook) -> Unit = {},
    downloadProgress: Int = -1, // -1 يعني غير محمل
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable { onClick(book) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = KitabiTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // صورة الغلاف
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = book.coverUrl.ifBlank { null },
                    contentDescription = book.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // شارة مترجم
                if (book.isTranslated) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = KitabiTheme.colors.secondaryContainer
                    ) {
                        Text(
                            text = "🌐 مترجم",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = KitabiTheme.colors.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // شارة ملكية عامة
                if (book.isPublicDomain) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = KitabiTheme.colors.tertiaryContainer
                    ) {
                        Text(
                            text = "📜",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // مؤشر التحميل
                if (downloadProgress in 0..99) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = KitabiTheme.colors.surface.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = KitabiTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$downloadProgress%",
                                style = MaterialTheme.typography.labelSmall,
                                color = KitabiTheme.colors.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // عنوان الكتاب
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = KitabiTheme.colors.onSurface,
                textAlign = TextAlign.Right
            )

            Spacer(modifier = Modifier.height(2.dp))

            // اسم المؤلف
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = KitabiTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Right
            )

            Spacer(modifier = Modifier.height(4.dp))

            // التقييم والتصنيف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // التقييم
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

                // شارة التصنيف
                if (book.subjects.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = KitabiTheme.colors.primaryContainer
                    ) {
                        Text(
                            text = book.subjects.first(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = KitabiTheme.colors.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // حقوق النشر للملكية العامة
            if (book.isPublicDomain) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "حقوق النشر محفوظة - ملكية عامة",
                    style = MaterialTheme.typography.labelSmall,
                    color = KitabiTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
