package com.kitabi.app.feature.review.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.Review

/**
 * بطاقة مراجعة واحدة
 * تعرض تفاصيل المراجعة مع التقييم والإعجاب
 */
@Composable
fun ReviewCard(
    review: Review,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            // معلومات المستخدم
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // صورة المستخدم
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            KitabiTheme.colors.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (review.userPhotoUrl.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = KitabiTheme.colors.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = review.userName.take(1),
                            style = MaterialTheme.typography.labelLarge,
                            color = KitabiTheme.colors.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = review.userName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = KitabiTheme.colors.onSurface
                    )
                    Text(
                        text = formatDate(review.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = KitabiTheme.colors.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // التقييم
                ReadOnlyRatingBar(rating = review.rating.toFloat())
            }

            // عنوان المراجعة
            if (review.title.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = KitabiTheme.colors.onSurface
                )
            }

            // محتوى المراجعة
            if (review.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = review.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = KitabiTheme.colors.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // زر الإعجاب
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = if (review.isLikedByCurrentUser) onUnlikeClick else onLikeClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (review.isLikedByCurrentUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "إعجاب",
                        tint = if (review.isLikedByCurrentUser) KitabiTheme.colors.error else KitabiTheme.colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "${review.likesCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = KitabiTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * تنسيق التاريخ
 */
private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("ar"))
    return sdf.format(java.util.Date(timestamp))
}
